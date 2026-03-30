package tables

import models.DotoEvent
import AppPostgresProfile.api.*
import java.util.UUID
import java.time.Instant

class EventTable(tag: Tag) extends Table[DotoEvent](tag, "events"):
  def id          = column[UUID]("id", O.PrimaryKey)
  def familyId    = column[UUID]("family_id")
  def title       = column[String]("title")
  def description = column[Option[String]]("description")
  def startAt     = column[Instant]("start_at")
  def endAt       = column[Instant]("end_at")
  def location    = column[Option[String]]("location")
  def color       = column[Option[String]]("color")
  def assignedTo  = column[List[UUID]]("assigned_to")
  def createdBy   = column[UUID]("created_by")
  def createdAt   = column[Instant]("created_at")
  def updatedAt   = column[Instant]("updated_at")

  def familyFk = foreignKey("fk_event_family", familyId, Families)(_.id)

  def * = (
    id, familyId, title, description, startAt, endAt,
    location, color, assignedTo, createdBy, createdAt, updatedAt
  ).mapTo[DotoEvent]

val Events = TableQuery[EventTable]
