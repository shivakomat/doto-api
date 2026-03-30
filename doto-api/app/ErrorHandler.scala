import play.api.http.HttpErrorHandler
import play.api.mvc.*
import play.api.mvc.Results.*

import io.circe.syntax.*
import io.circe.Json

import javax.inject.Singleton
import scala.concurrent.Future

@Singleton
class ErrorHandler extends HttpErrorHandler:

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    val (code, msg) = statusCode match
      case 400 => ("validation_error", if message.nonEmpty then message else "Bad request")
      case 401 => ("unauthorized",     if message.nonEmpty then message else "Unauthorized")
      case 403 => ("forbidden",        if message.nonEmpty then message else "Forbidden")
      case 404 => ("not_found",        if message.nonEmpty then message else "Not found")
      case 409 => ("conflict",         if message.nonEmpty then message else "Conflict")
      case _   => ("client_error",     if message.nonEmpty then message else s"Client error $statusCode")

    Future.successful(
      Status(statusCode)(errorJson(code, msg)).as("application/json")
    )

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    Future.successful(
      InternalServerError(errorJson("internal_error", "An unexpected error occurred"))
        .as("application/json")
    )

  private def errorJson(code: String, message: String): String =
    Json.obj("code" -> code.asJson, "message" -> message.asJson).noSpaces
