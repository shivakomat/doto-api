package tables

import models.DotoTask
import AppPostgresProfile.api.*
import java.util.UUID
import java.time.Instant

class TaskTable(tag: Tag) extends Table[DotoTask](tag, "tasks"):
  def id          = column[UUID]("id", O.PrimaryKey)
  def familyId    = column[UUID]("family_id")
  def title       = column[String]("title")
  def description = column[Option[String]]("description")
  def assignedTo  = column[Option[UUID]]("assigned_to")
  def status      = column[String]("status")
  def priority    = column[String]("priority")
  def points      = column[Int]("points")
  def dueAt       = column[Option[Instant]]("due_at")
  def completedAt = column[Option[Instant]]("completed_at")
  def createdBy   = column[UUID]("created_by")
  def createdAt   = column[Instant]("created_at")
  def updatedAt   = column[Instant]("updated_at")

  def familyFk = foreignKey("fk_task_family", familyId, Families)(_.id)

  def * = (
    id, familyId, title, description, assignedTo, status,
    priority, points, dueAt, completedAt, createdBy, createdAt, updatedAt
  ).mapTo[DotoTask]

val Tasks = TableQuery[TaskTable]
