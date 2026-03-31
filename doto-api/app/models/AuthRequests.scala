package models

import io.circe.Decoder
import io.circe.generic.semiauto.*

case class RegisterRequest(username: String, password: String, displayName: String, role: String, inviteCode: Option[String] = None)
object RegisterRequest:
  given Decoder[RegisterRequest] = deriveDecoder

case class LoginRequest(username: String, password: String)
object LoginRequest:
  given Decoder[LoginRequest] = deriveDecoder

case class UpdateProfileRequest(displayName: Option[String], color: Option[String])
object UpdateProfileRequest:
  given Decoder[UpdateProfileRequest] = deriveDecoder

case class ChangePasswordRequest(currentPassword: String, newPassword: String)
object ChangePasswordRequest:
  given Decoder[ChangePasswordRequest] = deriveDecoder
