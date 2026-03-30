package actions

import utils.{JwtUtils, JwtClaims}
import play.api.mvc.*
import play.api.mvc.Results.*

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthRequest[A](val userId: UUID, val familyId: Option[UUID], request: Request[A])
    extends WrappedRequest[A](request)

class AuthenticatedAction @Inject()(
  val parser: BodyParsers.Default,
  jwtUtils:   JwtUtils
)(implicit ec: ExecutionContext)
    extends ActionBuilder[AuthRequest, AnyContent]:

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: AuthRequest[A] => Future[Result]): Future[Result] =
    request.headers.get("Authorization") match
      case None =>
        Future.successful(
          Unauthorized(errorJson("unauthorized", "Missing Authorization header"))
        )
      case Some(header) =>
        val token = header.stripPrefix("Bearer ").trim
        jwtUtils.verify(token) match
          case Left(err) =>
            Future.successful(
              Unauthorized(errorJson("unauthorized", s"Invalid or expired token: $err"))
            )
          case Right(claims) =>
            block(AuthRequest(claims.userId, claims.familyId, request))

  private def errorJson(code: String, message: String): play.api.libs.json.JsValue =
    play.api.libs.json.Json.obj("code" -> code, "message" -> message)
