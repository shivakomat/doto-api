error id: file://<WORKSPACE>/doto-api/app/tables/FamilyTable.scala:column.
file://<WORKSPACE>/doto-api/app/tables/FamilyTable.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -AppPostgresProfile.api.column.
	 -AppPostgresProfile.api.column#
	 -AppPostgresProfile.api.column().
	 -column.
	 -column#
	 -column().
	 -scala/Predef.column.
	 -scala/Predef.column#
	 -scala/Predef.column().
offset: 256
uri: file://<WORKSPACE>/doto-api/app/tables/FamilyTable.scala
text:
```scala
package tables

import models.Family
import AppPostgresProfile.api.*
import java.util.UUID
import java.time.Instant

class FamilyTable(tag: Tag) extends Table[Family](tag, "families"):
  def id         = column[UUID]("id", O.PrimaryKey)
  def name       = @@column[String]("name")
  def inviteCode = column[String]("invite_code")
  def createdAt  = column[Instant]("created_at")
  def updatedAt  = column[Instant]("updated_at")

  def * = (id, name, inviteCode, createdAt, updatedAt).mapTo[Family]

val Families = TableQuery[FamilyTable]

```


#### Short summary: 

empty definition using pc, found symbol in pc: 