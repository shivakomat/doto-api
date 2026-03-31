package tables

import models.Profile
import AppPostgresProfile.api.*
import java.util.UUID
import java.time.{Instant, LocalDate}

class ProfileTable(tag: Tag) extends Table[Profile](tag, "profiles"):
  def id             = column[UUID]("id", O.PrimaryKey)
  def familyId       = column[Option[UUID]]("family_id")
  def username       = column[String]("username")
  def passwordHash   = column[String]("password_hash")
  def displayName    = column[String]("display_name")
  def role           = column[String]("role")
  def color          = column[String]("color")
  def points         = column[Int]("points")
  def streak         = column[Int]("streak")
  def lastStreakDate  = column[Option[LocalDate]]("last_streak_date")
  def isAuthAccount  = column[Boolean]("is_auth_account")
  def createdAt      = column[Instant]("created_at")
  def updatedAt      = column[Instant]("updated_at")

  def familyFk = foreignKey("fk_profile_family", familyId, Families)(_.id.?)

  def * = (
    id, familyId, username, passwordHash, displayName,
    role, color, points, streak, lastStreakDate, isAuthAccount, createdAt, updatedAt
  ).mapTo[Profile]

val Profiles = TableQuery[ProfileTable]
