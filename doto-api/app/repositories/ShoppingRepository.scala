package repositories

import models.{ShoppingList, ShoppingItem}
import tables.*
import AppPostgresProfile.api.*
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ShoppingRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[AppPostgresProfile]:

  // ── Lists ─────────────────────────────────────────────────────────────────

  def listLists(familyId: UUID): Future[Seq[ShoppingList]] =
    db.run(ShoppingLists.filter(_.familyId === familyId).sortBy(_.createdAt.desc).result)

  def findListById(id: UUID): Future[Option[ShoppingList]] =
    db.run(ShoppingLists.filter(_.id === id).result.headOption)

  def createList(list: ShoppingList): Future[ShoppingList] =
    db.run((ShoppingLists += list).map(_ => list))

  def deleteList(id: UUID): Future[Boolean] =
    db.run(ShoppingLists.filter(_.id === id).delete).map(_ > 0)

  def countItems(listId: UUID): Future[(Int, Int)] =
    for
      total   <- db.run(ShoppingItems.filter(_.listId === listId).length.result)
      checked <- db.run(ShoppingItems.filter(i => i.listId === listId && i.isChecked === true).length.result)
    yield (total, checked)

  // ── Items ─────────────────────────────────────────────────────────────────

  def listItems(listId: UUID): Future[Seq[ShoppingItem]] =
    db.run(
      ShoppingItems
        .filter(_.listId === listId)
        .sortBy(i => (i.isChecked.asc, i.category.asc, i.name.asc))
        .result
    )

  def findItemById(itemId: UUID): Future[Option[ShoppingItem]] =
    db.run(ShoppingItems.filter(_.id === itemId).result.headOption)

  def addItem(item: ShoppingItem): Future[ShoppingItem] =
    db.run((ShoppingItems += item).map(_ => item))

  def checkItem(itemId: UUID, listId: UUID, isChecked: Boolean, checkedBy: UUID): Future[Option[ShoppingItem]] =
    findItemById(itemId).flatMap {
      case None => Future.successful(None)
      case Some(i) if i.listId != listId => Future.successful(None)
      case Some(i) =>
        val updated = i.copy(
          isChecked = isChecked,
          checkedBy = if isChecked then Some(checkedBy) else None,
          checkedAt = if isChecked then Some(Instant.now()) else None,
          updatedAt = Instant.now()
        )
        db.run(ShoppingItems.filter(_.id === itemId).update(updated)).map(_ => Some(updated))
    }

  def deleteItem(itemId: UUID): Future[Boolean] =
    db.run(ShoppingItems.filter(_.id === itemId).delete).map(_ > 0)

  def clearChecked(listId: UUID): Future[Int] =
    db.run(ShoppingItems.filter(i => i.listId === listId && i.isChecked === true).delete)
