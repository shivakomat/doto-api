# Doto — API Security Spec
**Version:** 1.0
**Feature:** Backend hardening — rate limiting, lockout, JWT, headers, validation
**Scope:** Scala Play Framework
**Depends on:** Core API_SPEC.md, ONBOARDING_API_SPEC.md

---

## 1. Overview

This document defines all security hardening for the Doto Play backend.
Implementation is split into eight areas, ordered by priority. Build the
🔴 must-have items before any real users touch the app. 🟡 should-have items
before App Store submission. 🟠 nice-to-have items can follow post-launch.

| Priority | Area |
|---|---|
| 🔴 | Rate limiting on auth endpoints |
| 🔴 | Account lockout on repeated login failures |
| 🔴 | Request payload size limits |
| 🔴 | Input validation and length enforcement |
| 🟡 | HTTPS enforcement and HSTS |
| 🟡 | JWT hardening and true logout |
| 🟡 | Security response headers |
| 🟠 | Redis-backed rate limiting (multi-instance) |

---

## 2. Database Changes

### 2.1 Evolution 12.sql — Login Lockout Fields

```sql
-- conf/evolutions/default/12.sql

-- !Ups

ALTER TABLE profiles
ADD COLUMN IF NOT EXISTS login_attempts  INTEGER     NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS locked_until    TIMESTAMPTZ;

-- !Downs
ALTER TABLE profiles
DROP COLUMN IF EXISTS login_attempts;
DROP COLUMN IF EXISTS locked_until;
```

### 2.2 Evolution 13.sql — JWT Blocklist

```sql
-- conf/evolutions/default/13.sql

-- !Ups

CREATE TABLE jwt_blocklist (
    jti        UUID        PRIMARY KEY,
    expires_at TIMESTAMPTZ NOT NULL
);

-- Index for pruning expired entries
CREATE INDEX idx_jwt_blocklist_expires ON jwt_blocklist(expires_at);

-- !Downs
DROP TABLE IF EXISTS jwt_blocklist;
```

---

## 3. Rate Limiting 🔴

### 3.1 Approach — In-Memory with Caffeine

For MVP (single server instance), use Caffeine cache for rate limit state.
No new infrastructure required — Caffeine is already a transitive dependency
of Play via Akka.

Add to `build.sbt`:
```scala
libraryDependencies += "com.github.ben-manes.caffeine" % "caffeine" % "3.1.8"
```

### 3.2 RateLimiter Service

```scala
// app/services/RateLimiter.scala
import com.github.benmanes.caffeine.cache.{Caffeine, Cache}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.FiniteDuration

case class RateLimit(maxRequests: Int, window: FiniteDuration)

@Singleton
class RateLimiter @Inject()() {

  // Separate caches per limit profile — each has its own TTL window
  private def makeCache(window: FiniteDuration): Cache[String, AtomicInteger] =
    Caffeine.newBuilder()
      .expireAfterWrite(window.toSeconds, TimeUnit.SECONDS)
      .maximumSize(100_000)
      .build()

  // One cache per rate limit profile
  private val authLoginCache      = makeCache(15.minutes)
  private val authRegisterCache   = makeCache(60.minutes)
  private val codePreviewCache    = makeCache(5.minutes)
  private val familyJoinCache     = makeCache(15.minutes)
  private val claimProfileCache   = makeCache(15.minutes)
  private val authenticatedCache  = makeCache(1.minute)
  private val unauthenticatedCache= makeCache(1.minute)

  // Rate limit profiles — (cache, maxRequests)
  sealed trait LimitProfile
  case object AuthLogin       extends LimitProfile  // 5 / 15 min  per IP
  case object AuthRegister    extends LimitProfile  // 3 / 60 min  per IP
  case object CodePreview     extends LimitProfile  // 10 / 5 min  per IP
  case object FamilyJoin      extends LimitProfile  // 5 / 15 min  per IP
  case object ClaimProfile    extends LimitProfile  // 5 / 15 min  per IP
  case object Authenticated   extends LimitProfile  // 200 / 1 min per userId
  case object Unauthenticated extends LimitProfile  // 30 / 1 min  per IP

  private def resolveProfile(p: LimitProfile): (Cache[String, AtomicInteger], Int) =
    p match {
      case AuthLogin       => (authLoginCache,       5)
      case AuthRegister    => (authRegisterCache,    3)
      case CodePreview     => (codePreviewCache,    10)
      case FamilyJoin      => (familyJoinCache,      5)
      case ClaimProfile    => (claimProfileCache,    5)
      case Authenticated   => (authenticatedCache, 200)
      case Unauthenticated => (unauthenticatedCache, 30)
    }

  // Returns true if the request is allowed, false if rate limit exceeded
  def isAllowed(key: String, profile: LimitProfile): Boolean = {
    val (cache, maxRequests) = resolveProfile(profile)
    val counter = cache.get(key, _ => new AtomicInteger(0))
    counter.incrementAndGet() <= maxRequests
  }

  // Returns seconds until window resets (approximate — Caffeine doesn't expose this directly)
  def retryAfterSeconds(profile: LimitProfile): Int =
    profile match {
      case AuthLogin       => 15 * 60
      case AuthRegister    => 60 * 60
      case CodePreview     =>  5 * 60
      case FamilyJoin      => 15 * 60
      case ClaimProfile    => 15 * 60
      case Authenticated   =>      60
      case Unauthenticated =>      60
    }
}
```

### 3.3 RateLimitAction — Play Action Wrapper

```scala
// app/actions/RateLimitAction.scala
import play.api.mvc._
import play.api.libs.json.Json
import services.{RateLimiter, RateLimiter.LimitProfile}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RateLimitAction @Inject()(
  rateLimiter: RateLimiter,
  parser:      BodyParsers.Default
)(implicit ec: ExecutionContext) {

  // Extract client IP — respects X-Forwarded-For if behind a proxy
  private def clientIp(request: RequestHeader): String =
    request.headers.get("X-Forwarded-For")
      .flatMap(_.split(",").headOption)
      .map(_.trim)
      .getOrElse(request.remoteAddress)

  def apply(profile: LimitProfile): ActionBuilder[Request, AnyContent] =
    new ActionBuilder[Request, AnyContent] {
      def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
        val key = clientIp(request)
        if (rateLimiter.isAllowed(key, profile)) {
          block(request)
        } else {
          val retryAfter = rateLimiter.retryAfterSeconds(profile)
          Future.successful(
            TooManyRequests(Json.obj(
              "code"    -> "rate_limited",
              "message" -> "Too many requests. Please try again later."
            )).withHeaders("Retry-After" -> retryAfter.toString)
          )
        }
      }
      override def parser           = RateLimitAction.this.parser
      override def executionContext = ec
    }

  // For authenticated routes — key on userId instead of IP
  def authenticated(userId: String, profile: LimitProfile)(
    block: => Future[Result]
  ): Future[Result] = {
    if (rateLimiter.isAllowed(userId, profile)) {
      block
    } else {
      val retryAfter = rateLimiter.retryAfterSeconds(profile)
      Future.successful(
        TooManyRequests(Json.obj(
          "code"    -> "rate_limited",
          "message" -> "Too many requests. Please slow down."
        )).withHeaders("Retry-After" -> retryAfter.toString)
      )
    }
  }
}
```

### 3.4 Applying Rate Limits in Controllers

```scala
// AuthController — rate-limited endpoints
class AuthController @Inject()(
  rateLimitAction: RateLimitAction,
  // ... other deps
) extends AbstractController(cc) {

  def login: Action[JsValue] =
    rateLimitAction(RateLimiter.AuthLogin)(parse.json).async { request =>
      // login logic
    }

  def register: Action[JsValue] =
    rateLimitAction(RateLimiter.AuthRegister)(parse.json).async { request =>
      // register logic
    }

  def claimProfile: Action[JsValue] =
    rateLimitAction(RateLimiter.ClaimProfile)(parse.json).async { request =>
      // claim logic
    }
}

// FamilyController
def preview(code: String): Action[AnyContent] =
  rateLimitAction(RateLimiter.CodePreview).async { request =>
    // preview logic
  }

def join: Action[JsValue] =
  rateLimitAction(RateLimiter.FamilyJoin)(parse.json).async { request =>
    // join logic
  }
```

### 3.5 General Authenticated Rate Limit

Apply the authenticated limit inside `AuthenticatedAction`:

```scala
// app/actions/AuthenticatedAction.scala — add rate limit check
def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
  // ... JWT validation ...
  // After extracting userId:
  if (!rateLimiter.isAllowed(userId.toString, RateLimiter.Authenticated)) {
    Future.successful(
      TooManyRequests(Json.obj(
        "code"    -> "rate_limited",
        "message" -> "Too many requests."
      )).withHeaders("Retry-After" -> "60")
    )
  } else {
    block(new AuthenticatedRequest(userId, familyId, request))
  }
}
```

---

## 4. Account Lockout 🔴

### 4.1 Logic

Applied only to `POST /api/auth/login`. After 10 failed attempts for a specific
username, the account is locked for 30 minutes.

```scala
// app/services/LoginLockoutService.scala
@Singleton
class LoginLockoutService @Inject()(
  profileRepo: ProfileRepository
)(implicit ec: ExecutionContext) {

  val MAX_ATTEMPTS  = 10
  val LOCKOUT_MINS  = 30

  def checkAndRecord(username: String, success: Boolean): Future[LockoutStatus] = {
    profileRepo.findByUsername(username) flatMap {
      case None =>
        // Username doesn't exist — return same response as wrong password
        Future.successful(LockoutStatus.NotLocked)

      case Some(profile) =>
        val now = Instant.now()

        // Check if currently locked
        if (profile.lockedUntil.exists(_.isAfter(now))) {
          val minutesLeft = Duration.between(now, profile.lockedUntil.get).toMinutes + 1
          Future.successful(LockoutStatus.Locked(minutesLeft.toInt))
        } else if (success) {
          // Successful login — reset counter
          profileRepo.resetLoginAttempts(profile.id)
            .map(_ => LockoutStatus.NotLocked)
        } else {
          // Failed login — increment counter
          val newAttempts = profile.loginAttempts + 1
          if (newAttempts >= MAX_ATTEMPTS) {
            val lockUntil = now.plus(LOCKOUT_MINS, ChronoUnit.MINUTES)
            profileRepo.lockAccount(profile.id, lockUntil)
              .map(_ => LockoutStatus.JustLocked)
          } else {
            profileRepo.incrementLoginAttempts(profile.id)
              .map(_ => LockoutStatus.NotLocked)
          }
        }
    }
  }
}

sealed trait LockoutStatus
object LockoutStatus {
  case object NotLocked                  extends LockoutStatus
  case object JustLocked                 extends LockoutStatus
  case class  Locked(minutesLeft: Int)   extends LockoutStatus
}
```

### 4.2 AuthController Login Flow with Lockout

```scala
def login: Action[JsValue] =
  rateLimitAction(RateLimiter.AuthLogin)(parse.json).async { request =>
    request.body.validate[LoginRequest] match {
      case JsError(_) =>
        Future.successful(BadRequest(errorJson("validation_error", "Invalid request")))

      case JsSuccess(req, _) =>
        // 1. Check lockout BEFORE attempting password verification
        lockoutService.checkAndRecord(req.username, success = false) flatMap {
          case LockoutStatus.Locked(mins) =>
            Future.successful(
              // 423 Locked — but message doesn't confirm the account exists
              Locked(errorJson("account_locked",
                s"Too many failed attempts. Try again in $mins minutes."))
            )
          case _ =>
            // 2. Attempt authentication
            authService.authenticate(req.username, req.password) flatMap {
              case None =>
                // Wrong credentials — lockout already incremented above
                Future.successful(
                  Unauthorized(errorJson("unauthorized", "Incorrect username or password."))
                )
              case Some(profile) =>
                // 3. Successful — reset lockout counter
                lockoutService.checkAndRecord(req.username, success = true) map { _ =>
                  val token = jwtService.generateToken(profile.id, profile.familyId)
                  Ok(Json.toJson(AuthResponse(token, profile)))
                }
            }
        }
    }
  }
```

### 4.3 ProfileRepository Additions

```scala
// In ProfileRepository:

def resetLoginAttempts(profileId: UUID): Future[Unit] =
  db.run(
    profiles.filter(_.id === profileId)
      .map(p => (p.loginAttempts, p.lockedUntil))
      .update((0, None))
  ).map(_ => ())

def incrementLoginAttempts(profileId: UUID): Future[Unit] =
  db.run(sqlu"""
    UPDATE profiles
    SET login_attempts = login_attempts + 1,
        updated_at     = NOW()
    WHERE id = ${profileId}
  """).map(_ => ())

def lockAccount(profileId: UUID, until: Instant): Future[Unit] =
  db.run(
    profiles.filter(_.id === profileId)
      .map(p => (p.loginAttempts, p.lockedUntil, p.updatedAt))
      .update((0, Some(until), Instant.now()))
  ).map(_ => ())
```

---

## 5. Payload Size Limits 🔴

### 5.1 application.conf

```hocon
# Limit all request bodies to 64KB
# Requests exceeding this return 413 immediately — Play never parses the body
play.http.parser.maxMemoryBuffer = 64k
play.http.parser.maxDiskBuffer   = 64k

# Also set at the Akka HTTP level
akka.http.parsing.max-content-length = 64k
```

### 5.2 Field-Level Length Validation

Enforce these limits in every request DTO's validation logic. Return `400 validation_error`
with the specific field name if a limit is exceeded:

```scala
// app/models/requests/Validation.scala
object Validation {

  case class ValidationError(field: String, message: String)

  def validateLength(value: String, field: String, max: Int): Option[ValidationError] =
    if (value.length > max)
      Some(ValidationError(field, s"$field must be $max characters or fewer"))
    else None

  def validateNonEmpty(value: String, field: String): Option[ValidationError] =
    if (value.trim.isEmpty)
      Some(ValidationError(field, s"$field is required"))
    else None

  // Reusable: collect all errors, return first if any
  def validate(checks: Option[ValidationError]*): Option[ValidationError] =
    checks.flatten.headOption
}

// Field length limits — single source of truth
object FieldLimits {
  val USERNAME      =  50
  val DISPLAY_NAME  = 100
  val PASSWORD_MAX  = 128
  val FAMILY_NAME   = 100
  val TASK_TITLE    = 200
  val EVENT_TITLE   = 200
  val ITEM_NAME     = 200
  val NOTES         = 1000
  val LOCATION      = 300
  val INVITE_CODE   =   6
}
```

Apply in every controller before doing any database work:

```scala
// Example in TaskController.create:
val titleError = Validation.validateLength(req.title, "title", FieldLimits.TASK_TITLE)
  .orElse(Validation.validateNonEmpty(req.title, "title"))

titleError match {
  case Some(err) =>
    Future.successful(BadRequest(errorJson("validation_error", err.message)))
  case None =>
    taskRepo.createTask(req)
      .map(t => Created(Json.toJson(t)))
}
```

### 5.3 HTML / Script Stripping

Reject any field value that contains HTML tags. No field in Doto accepts rich text,
so there's no legitimate use case for `<`, `>`, or `script`:

```scala
// app/models/requests/Validation.scala — add:
private val htmlPattern = """<[^>]+>""".r

def validateNoHtml(value: String, field: String): Option[ValidationError] =
  if (htmlPattern.findFirstIn(value).isDefined)
    Some(ValidationError(field, s"$field contains invalid characters"))
  else None
```

---

## 6. HTTPS Enforcement 🟡

### 6.1 Redirect Filter (Production Only)

```scala
// app/filters/HttpsRedirectFilter.scala
import play.api.mvc._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HttpsRedirectFilter @Inject()(implicit ec: ExecutionContext) extends EssentialFilter {
  def apply(next: EssentialAction) = EssentialAction { request =>
    val isHttps = request.headers.get("X-Forwarded-Proto").contains("https") ||
                  request.secure

    if (!isHttps && !request.host.contains("localhost")) {
      val httpsUrl = "https://" + request.host + request.uri
      Accumulator.done(Results.MovedPermanently(httpsUrl))
    } else {
      next(request)
    }
  }
}
```

Enable only in production via `application.prod.conf`:

```hocon
# conf/application.prod.conf
include "application.conf"

play.filters.enabled += "filters.HttpsRedirectFilter"
play.filters.enabled += "play.filters.headers.SecurityHeadersFilter"
```

### 6.2 HSTS Header

```hocon
# conf/application.conf — security headers
play.filters.headers {
  strictTransportSecurity = "max-age=31536000; includeSubDomains"
  contentTypeOptions      = "nosniff"
  frameOptions            = "DENY"
  xssProtection           = "1; mode=block"
  referrerPolicy          = "no-referrer"
  contentSecurityPolicy   = null   # Set explicitly in SecurityHeadersFilter
}
```

---

## 7. JWT Hardening 🟡

### 7.1 Shorten Expiry

Change JWT expiry from 30 days to **7 days** in `application.conf`:

```hocon
jwt.expiry.days = 7
```

### 7.2 Add JTI Claim

Every issued token gets a unique `jti` (JWT ID) stored in the blocklist table on logout.

```scala
// app/utils/JwtUtils.scala — updated generateToken
import java.util.{Date, UUID}
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

def generateToken(userId: UUID, familyId: Option[UUID]): (String, UUID) = {
  val jti       = UUID.randomUUID()
  val expiryMs  = config.get[Int]("jwt.expiry.days") * 24 * 60 * 60 * 1000L
  val token = JWT.create()
    .withJWTId(jti.toString)
    .withSubject(userId.toString)
    .withClaim("familyId", familyId.map(_.toString).orNull)
    .withIssuedAt(new Date())
    .withExpiresAt(new Date(System.currentTimeMillis() + expiryMs))
    .sign(algorithm)
  (token, jti)   // return both — jti stored on logout
}

def validateToken(token: String): Either[String, (UUID, Option[UUID], UUID)] =
  try {
    val decoded  = JWT.require(algorithm).build().verify(token)
    val userId   = UUID.fromString(decoded.getSubject)
    val familyId = Option(decoded.getClaim("familyId").asString()).map(UUID.fromString)
    val jti      = UUID.fromString(decoded.getId)
    Right((userId, familyId, jti))
  } catch {
    case ex: Exception => Left(ex.getMessage)
  }
```

### 7.3 Logout Endpoint

```scala
// Add to AuthController:
def logout: Action[AnyContent] = authAction.async { request =>
  // Add jti to blocklist with the token's expiry date
  jwtBlocklistRepo.block(request.jti, request.tokenExpiry) map { _ =>
    Ok(Json.obj("loggedOut" -> true))
  }
}
```

Add to routes:
```
POST    /api/auth/logout    controllers.AuthController.logout
```

### 7.4 Blocklist Check in AuthenticatedAction

```scala
// app/actions/AuthenticatedAction.scala — add blocklist check
def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
  request.headers.get("Authorization")
    .flatMap(_.stripPrefix("Bearer ").trim.some) match {
      case None => Future.successful(Unauthorized(errorJson("unauthorized", "Missing token")))
      case Some(token) =>
        jwtUtils.validateToken(token) match {
          case Left(_) =>
            Future.successful(Unauthorized(errorJson("unauthorized", "Invalid token")))
          case Right((userId, familyId, jti)) =>
            // Check blocklist
            jwtBlocklistRepo.isBlocked(jti) flatMap {
              case true =>
                Future.successful(Unauthorized(errorJson("unauthorized", "Token has been revoked")))
              case false =>
                // Check rate limit
                if (!rateLimiter.isAllowed(userId.toString, RateLimiter.Authenticated)) {
                  Future.successful(TooManyRequests(errorJson("rate_limited", "Too many requests"))
                    .withHeaders("Retry-After" -> "60"))
                } else {
                  block(new AuthenticatedRequest(userId, familyId, jti, request))
                }
            }
        }
  }
}
```

### 7.5 JwtBlocklistRepository

```scala
// app/repositories/JwtBlocklistRepository.scala
@Singleton
class JwtBlocklistRepository @Inject()(
  dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class BlocklistTable(tag: Tag)
    extends Table[(UUID, Instant)](tag, "jwt_blocklist") {
    def jti       = column[UUID]("jti", O.PrimaryKey)
    def expiresAt = column[Instant]("expires_at")
    def * = (jti, expiresAt)
  }

  private val blocklist = TableQuery[BlocklistTable]

  def block(jti: UUID, expiresAt: Instant): Future[Unit] =
    db.run(blocklist += (jti, expiresAt)).map(_ => ())

  def isBlocked(jti: UUID): Future[Boolean] =
    db.run(blocklist.filter(_.jti === jti).exists.result)

  // Run daily — remove entries past their expiry date
  def pruneExpired(): Future[Int] =
    db.run(blocklist.filter(_.expiresAt < Instant.now()).delete)
}
```

### 7.6 Daily Pruning Task

```scala
// app/tasks/JwtPruneTask.scala
import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class JwtPruneTask @Inject()(
  actorSystem:   ActorSystem,
  blocklistRepo: JwtBlocklistRepository
)(implicit ec: ExecutionContext) {

  actorSystem.scheduler.scheduleWithFixedDelay(
    initialDelay = 1.hour,
    delay        = 24.hours
  ) { () =>
    blocklistRepo.pruneExpired()
  }
}
```

Register in `Module.scala`:
```scala
bind(classOf[JwtPruneTask]).asEagerSingleton()
```

---

## 8. Security Response Headers 🟡

### 8.1 SecurityHeadersFilter

Play's built-in `SecurityHeadersFilter` adds headers to every response automatically.

Enable in `application.conf`:
```hocon
play.filters.enabled += "play.filters.headers.SecurityHeadersFilter"

play.filters.headers {
  contentTypeOptions       = "nosniff"
  xssProtection            = "1; mode=block"
  frameOptions             = "DENY"
  referrerPolicy           = "no-referrer"
  strictTransportSecurity  = "max-age=31536000; includeSubDomains"
  # Disable CSP for API-only backend (no HTML served)
  contentSecurityPolicy    = null
}
```

### 8.2 Custom Headers Filter

Adds headers not covered by Play's built-in filter:

```scala
// app/filters/ApiSecurityFilter.scala
class ApiSecurityFilter @Inject()(implicit ec: ExecutionContext) extends EssentialFilter {
  def apply(next: EssentialAction) = EssentialAction { request =>
    next(request).map(
      _.withHeaders(
        "X-Content-Type-Options"    -> "nosniff",
        "X-Frame-Options"           -> "DENY",
        "Cache-Control"             -> "no-store",
        "Pragma"                    -> "no-cache",
        "Permissions-Policy"        -> "camera=(), microphone=(), geolocation=()"
      )
    )
  }
}
```

Enable:
```hocon
play.filters.enabled += "filters.ApiSecurityFilter"
```

---

## 9. CORS Hardening 🟠

### 9.1 Development (current)

```hocon
play.filters.cors.allowedOrigins = ["*"]   # fine for localhost only
```

### 9.2 Production

```hocon
# conf/application.prod.conf
play.filters.cors {
  allowedOrigins     = ["https://getdoto.com", "https://www.getdoto.com"]
  allowedHttpMethods = ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
  allowedHttpHeaders = ["Accept", "Content-Type", "Authorization"]
  preflightMaxAge    = 3.days
}
```

The iOS app does not use CORS — it only applies to web browser clients.

---

## 10. Redis-Backed Rate Limiting 🟠

When the app scales to multiple server instances, the in-memory Caffeine cache
no longer works — each instance has its own counter and the limits are not shared.

Replace the Caffeine implementation with Redis:

Add to `build.sbt`:
```scala
libraryDependencies += "com.github.etaty" %% "rediscala" % "1.9.0"
```

The `RateLimiter` service's `isAllowed` method uses Redis `INCR` + `EXPIRE`:

```scala
// Redis-backed isAllowed (pseudocode — implement with rediscala client)
def isAllowed(key: String, profile: LimitProfile): Future[Boolean] = {
  val redisKey = s"ratelimit:${profile.getClass.getSimpleName}:$key"
  val (maxRequests, windowSeconds) = resolveProfile(profile)
  for {
    count <- redis.incr(redisKey)
    _     <- if (count == 1) redis.expire(redisKey, windowSeconds) else Future.unit
  } yield count <= maxRequests
}
```

This migration can happen without changing any controller code — only the `RateLimiter`
service implementation changes.

---

## 11. Error Response Consistency

All security-related errors must use these exact HTTP status codes and response shapes.
Never return different messages for "username not found" vs "wrong password" — that
leaks account existence information.

| Scenario | HTTP | code | message |
|---|---|---|---|
| Rate limited | 429 | `rate_limited` | "Too many requests. Please try again later." |
| Account locked | 423 | `account_locked` | "Too many failed attempts. Try again in X minutes." |
| Wrong credentials | 401 | `unauthorized` | "Incorrect username or password." |
| Token revoked | 401 | `unauthorized` | "Token has been revoked." |
| Token expired | 401 | `unauthorized` | "Session expired. Please sign in again." |
| Token missing | 401 | `unauthorized` | "Authentication required." |
| Payload too large | 413 | `payload_too_large` | "Request body too large." |
| HTML in field | 400 | `validation_error` | "[field] contains invalid characters." |

---

## 12. Build Order

Implement in this sequence — each item is independently deployable:

1. **Payload size limits** — 2-line config change. Do this first, zero risk.
2. **Field validation + HTML stripping** — add to each controller as you build them.
3. **Rate limiting** — add `RateLimiter` service + `RateLimitAction`, apply to auth routes.
4. **Account lockout** — add DB columns + `LoginLockoutService`, wire into login.
5. **Security headers** — enable Play's built-in filter + custom filter.
6. **HTTPS redirect** — enable in production config only.
7. **JWT hardening** — shorten expiry, add jti, add logout endpoint + blocklist.
8. **Redis migration** — only when running multiple server instances.
