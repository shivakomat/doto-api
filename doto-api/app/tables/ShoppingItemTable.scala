package tables

import models.ShoppingItem
import AppPostgresProfile.api.*
import java.util.UUID
import java.time.Instant

class ShoppingItemTable(tag: Tag) extends Table[ShoppingItem](tag, "shopping_items"):
  def id        = column[UUID]("id", O.PrimaryKey)
  def listId    = column[UUID]("list_id")
  def familyId  = column[UUID]("family_id")
  def name      = column[String]("name")
  def category  = column[String]("category")
  def quantity  = column[Option[String]]("quantity")
  def isChecked = column[Boolean]("is_checked")
  def checkedBy = column[Option[UUID]]("checked_by")
  def checkedAt = column[Option[Instant]]("checked_at")
  def createdBy = column[UUID]("created_by")
  def createdAt = column[Instant]("created_at")
  def updatedAt = column[Instant]("updated_at")

  def listFk   = foreignKey("fk_sitem_list",   listId,   ShoppingLists)(_.id)
  def familyFk = foreignKey("fk_sitem_family", familyId, Families)(_.id)

  def * = (
    id, listId, familyId, name, category, quantity,
    isChecked, checkedBy, checkedAt, createdBy, createdAt, updatedAt
  ).mapTo[ShoppingItem]

val ShoppingItems = TableQuery[ShoppingItemTable]
