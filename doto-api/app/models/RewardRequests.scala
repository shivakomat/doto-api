package models

import io.circe.Decoder
import io.circe.generic.semiauto.*

case class CreateRewardRequest(memberId: String, title: String, pointsCost: Int)
object CreateRewardRequest:
  given Decoder[CreateRewardRequest] = deriveDecoder
