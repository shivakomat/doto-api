package tables

import models.ShoppingList
import AppPostgresProfile.api.*
import java.util.UUID
import java.time.Instant

class ShoppingListTable(tag: Tag) extends Table[ShoppingList](tag, "shopping_lists"):
  def id        = column[UUID]("id", O.PrimaryKey)
  def familyId  = column[UUID]("family_id")
  def name      = column[String]("name")
  def createdBy = column[UUID]("created_by")
  def createdAt = column[Instant]("created_at")
  def updatedAt = column[Instant]("updated_at")

  def familyFk = foreignKey("fk_slist_family", familyId, Families)(_.id)

  def * = (id, familyId, name, createdBy, createdAt, updatedAt).mapTo[ShoppingList]

val ShoppingLists = TableQuery[ShoppingListTable]
