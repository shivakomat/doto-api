package models

import io.circe.Decoder
import io.circe.generic.semiauto.*

case class CreateListRequest(name: String)
object CreateListRequest:
  given Decoder[CreateListRequest] = deriveDecoder

case class AddItemRequest(
  name:     String,
  category: Option[String] = None,
  quantity: Option[String] = None
)
object AddItemRequest:
  given Decoder[AddItemRequest] = deriveDecoder

case class CheckItemRequest(isChecked: Boolean)
object CheckItemRequest:
  given Decoder[CheckItemRequest] = deriveDecoder
