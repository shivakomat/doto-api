error id: file://<WORKSPACE>/doto-api/app/tables/ProfileTable.scala:
file://<WORKSPACE>/doto-api/app/tables/ProfileTable.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -AppPostgresProfile.api.familyId.
	 -AppPostgresProfile.api.familyId#
	 -AppPostgresProfile.api.familyId().
	 -familyId.
	 -familyId#
	 -familyId().
	 -scala/Predef.familyId.
	 -scala/Predef.familyId#
	 -scala/Predef.familyId().
offset: 870
uri: file://<WORKSPACE>/doto-api/app/tables/ProfileTable.scala
text:
```scala
package tables

import models.Profile
import AppPostgresProfile.api.*
import java.util.UUID
import java.time.Instant

class ProfileTable(tag: Tag) extends Table[Profile](tag, "profiles"):
  def id            = column[UUID]("id", O.PrimaryKey)
  def familyId      = column[Option[UUID]]("family_id")
  def username      = column[Option[String]]("username")
  def passwordHash  = column[Option[String]]("password_hash")
  def displayName   = column[String]("display_name")
  def role          = column[String]("role")
  def color         = column[String]("color")
  def points        = column[Int]("points")
  def isAuthAccount = column[Boolean]("is_auth_account")
  def createdAt     = column[Instant]("created_at")
  def updatedAt     = column[Instant]("updated_at")

  def familyFk = foreignKey("fk_profile_family", familyId, Families)(_.id.?)

  def * = (
    id, fami@@lyId, username, passwordHash, displayName,
    role, color, points, isAuthAccount, createdAt, updatedAt
  ).mapTo[Profile]

val Profiles = TableQuery[ProfileTable]

```


#### Short summary: 

empty definition using pc, found symbol in pc: 