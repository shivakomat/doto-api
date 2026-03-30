package controllers

import io.circe.{Encoder, Json}
import io.circe.syntax.*
import play.api.mvc.{AnyContent, Request, Result, Results}

object JsonHelper extends Results:

  def readBody(request: Request[AnyContent]): String =
    request.body.asText
      .orElse(request.body.asJson.map(_.toString))
      .getOrElse("")


  def ok[A: Encoder](value: A): Result =
    Ok(value.asJson.noSpaces).as("application/json")

  def created[A: Encoder](value: A): Result =
    Created(value.asJson.noSpaces).as("application/json")

  def err(status: Status, code: String, message: String): Result =
    status(Json.obj("code" -> code.asJson, "message" -> message.asJson).noSpaces)
      .as("application/json")

  def notFound(message: String): Result   = err(NotFound,            "not_found",        message)
  def forbidden(message: String): Result  = err(Forbidden,           "forbidden",        message)
  def conflict(message: String): Result   = err(Conflict,            "conflict",         message)
  def badRequest(message: String): Result = err(BadRequest,          "validation_error", message)
  def serverErr(message: String): Result  = err(InternalServerError, "internal_error",   message)
