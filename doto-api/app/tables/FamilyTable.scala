package tables

import models.Family
import AppPostgresProfile.api.*
import java.util.UUID
import java.time.Instant

class FamilyTable(tag: Tag) extends Table[Family](tag, "families"):
  def id         = column[UUID]("id", O.PrimaryKey)
  def name       = column[String]("name")
  def inviteCode = column[String]("invite_code")
  def createdAt  = column[Instant]("created_at")
  def updatedAt  = column[Instant]("updated_at")

  def * = (id, name, inviteCode, createdAt, updatedAt).mapTo[Family]

val Families = TableQuery[FamilyTable]
