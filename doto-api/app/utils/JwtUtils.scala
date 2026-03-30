package utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import play.api.Configuration

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.{Date, UUID}
import javax.inject.{Inject, Singleton}
import scala.util.Try

case class JwtClaims(userId: UUID, familyId: Option[UUID])

@Singleton
class JwtUtils @Inject()(config: Configuration):

  private val secret     = config.get[String]("jwt.secret")
  private val expiryDays = config.getOptional[Int]("jwt.expiry.days").getOrElse(30)
  private val algorithm  = Algorithm.HMAC256(secret)
  private val issuer     = "doto-api"

  def issue(userId: UUID, familyId: Option[UUID]): String =
    val builder = JWT.create()
      .withIssuer(issuer)
      .withSubject(userId.toString)
      .withIssuedAt(Date.from(Instant.now()))
      .withExpiresAt(Date.from(Instant.now().plus(expiryDays, ChronoUnit.DAYS)))
    familyId.foreach(fid => builder.withClaim("familyId", fid.toString))
    builder.sign(algorithm)

  def verify(token: String): Either[String, JwtClaims] =
    Try {
      val verifier  = JWT.require(algorithm).withIssuer(issuer).build()
      val decoded   = verifier.verify(token)
      val userId    = UUID.fromString(decoded.getSubject)
      val familyId  = Option(decoded.getClaim("familyId"))
        .filterNot(c => c.isMissing || c.isNull)
        .map(c => UUID.fromString(c.asString()))
      JwtClaims(userId, familyId)
    }.toEither.left.map {
      case e: JWTVerificationException => e.getMessage
      case e                           => e.getMessage
    }
