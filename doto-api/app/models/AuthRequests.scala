package models

import io.circe.Decoder
import io.circe.generic.semiauto.*

case class RegisterRequest(username: String, password: String, displayName: String)
object RegisterRequest:
  given Decoder[RegisterRequest] = deriveDecoder

case class LoginRequest(username: String, password: String)
object LoginRequest:
  given Decoder[LoginRequest] = deriveDecoder

case class UpdateProfileRequest(displayName: Option[String], color: Option[String])
object UpdateProfileRequest:
  given Decoder[UpdateProfileRequest] = deriveDecoder
