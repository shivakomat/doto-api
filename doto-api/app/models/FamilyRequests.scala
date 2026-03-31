package models

import io.circe.Decoder
import io.circe.generic.semiauto.*

case class CreateFamilyRequest(name: String)
object CreateFamilyRequest:
  given Decoder[CreateFamilyRequest] = deriveDecoder

case class JoinFamilyRequest(inviteCode: String, role: String)
object JoinFamilyRequest:
  given Decoder[JoinFamilyRequest] = deriveDecoder

case class UpdateFamilyRequest(name: String)
object UpdateFamilyRequest:
  given Decoder[UpdateFamilyRequest] = deriveDecoder
