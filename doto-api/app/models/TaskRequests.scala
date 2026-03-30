package models

import io.circe.Decoder
import io.circe.generic.semiauto.*

case class CreateTaskRequest(
  title:       String,
  description: Option[String] = None,
  assignedTo:  Option[String] = None,
  priority:    Option[String] = None,
  points:      Option[Int]    = None,
  dueAt:       Option[String] = None
)
object CreateTaskRequest:
  given Decoder[CreateTaskRequest] = deriveDecoder

case class UpdateTaskRequest(
  title:       Option[String] = None,
  description: Option[String] = None,
  assignedTo:  Option[String] = None,
  status:      Option[String] = None,
  priority:    Option[String] = None,
  points:      Option[Int]    = None,
  dueAt:       Option[String] = None
)
object UpdateTaskRequest:
  given Decoder[UpdateTaskRequest] = deriveDecoder
