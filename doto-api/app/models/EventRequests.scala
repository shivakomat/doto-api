package models

import io.circe.Decoder
import io.circe.generic.semiauto.*

case class CreateEventRequest(
  title:       String,
  description: Option[String]       = None,
  startAt:     String,
  endAt:       String,
  location:    Option[String]       = None,
  color:       Option[String]       = None,
  assignedTo:  Option[List[String]] = None
)
object CreateEventRequest:
  given Decoder[CreateEventRequest] = deriveDecoder

case class UpdateEventRequest(
  title:       Option[String]       = None,
  description: Option[String]       = None,
  startAt:     Option[String]       = None,
  endAt:       Option[String]       = None,
  location:    Option[String]       = None,
  color:       Option[String]       = None,
  assignedTo:  Option[List[String]] = None
)
object UpdateEventRequest:
  given Decoder[UpdateEventRequest] = deriveDecoder
