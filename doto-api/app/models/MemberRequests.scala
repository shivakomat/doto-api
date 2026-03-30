package models

import io.circe.Decoder
import io.circe.generic.semiauto.*

case class CreateMemberRequest(displayName: String, color: String)
object CreateMemberRequest:
  given Decoder[CreateMemberRequest] = deriveDecoder

case class UpdateMemberRequest(displayName: Option[String], color: Option[String])
object UpdateMemberRequest:
  given Decoder[UpdateMemberRequest] = deriveDecoder
