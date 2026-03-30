package controllers

import actions.AuthenticatedAction
import models.{ShoppingList, ShoppingItem, CreateListRequest, AddItemRequest, CheckItemRequest}
import repositories.ShoppingRepository

import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Json

import play.api.mvc.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

@Singleton
class ShoppingController @Inject()(
  cc:           ControllerComponents,
  auth:         AuthenticatedAction,
  shoppingRepo: ShoppingRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc):

  import JsonHelper.*

  def listLists: Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      shoppingRepo.listLists(fid).flatMap { lists =>
        Future.sequence(lists.map(l =>
          shoppingRepo.countItems(l.id).map { case (total, checked) =>
            listView(l, total, checked)
          }
        )).map(views => ok(views))
      }
    }
  }

  def createList: Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      val body = readBody(request)
      decode[CreateListRequest](body) match
        case Left(_)    => Future.successful(badRequest("Invalid request body"))
        case Right(req) =>
          if req.name.trim.isEmpty then Future.successful(badRequest("name is required"))
          else
            val list = ShoppingList(familyId = fid, name = req.name.trim, createdBy = request.userId)
            shoppingRepo.createList(list).map(l => created(listView(l, 0, 0)))
    }
  }

  def deleteList(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        shoppingRepo.findListById(uid).flatMap {
          case None                          => Future.successful(notFound(s"List $id not found"))
          case Some(l) if l.familyId != fid => Future.successful(forbidden("Access denied"))
          case Some(_) =>
            shoppingRepo.deleteList(uid).map {
              case true  => NoContent
              case false => notFound(s"List $id not found")
            }
        }
      }
    }
  }

  def listItems(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        shoppingRepo.findListById(uid).flatMap {
          case None                          => Future.successful(notFound(s"List $id not found"))
          case Some(l) if l.familyId != fid => Future.successful(forbidden("Access denied"))
          case Some(_) =>
            shoppingRepo.listItems(uid).map(items => ok(items.map(itemView)))
        }
      }
    }
  }

  def addItem(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        shoppingRepo.findListById(uid).flatMap {
          case None                          => Future.successful(notFound(s"List $id not found"))
          case Some(l) if l.familyId != fid => Future.successful(forbidden("Access denied"))
          case Some(_) =>
            val body = readBody(request)
            decode[AddItemRequest](body) match
              case Left(_)    => Future.successful(badRequest("Invalid request body"))
              case Right(req) =>
                if req.name.isEmpty || req.name.length > 200 then
                  Future.successful(badRequest("name must be 1–200 characters"))
                else
                  val item = ShoppingItem(
                    listId    = uid,
                    familyId  = fid,
                    name      = req.name,
                    category  = req.category.getOrElse("other"),
                    quantity  = req.quantity,
                    createdBy = request.userId
                  )
                  shoppingRepo.addItem(item).map(i => created(itemView(i)))
        }
      }
    }
  }

  def checkItem(listId: String, itemId: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(listId) { lid =>
        withUUID(itemId) { iid =>
          shoppingRepo.findListById(lid).flatMap {
            case None                          => Future.successful(notFound(s"List $listId not found"))
            case Some(l) if l.familyId != fid => Future.successful(forbidden("Access denied"))
            case Some(_) =>
              val body = readBody(request)
              decode[CheckItemRequest](body) match
                case Left(_)    => Future.successful(badRequest("Invalid request body"))
                case Right(req) =>
                  shoppingRepo.checkItem(iid, lid, req.isChecked, request.userId).map {
                    case None    => notFound(s"Item $itemId not found")
                    case Some(i) => ok(itemView(i))
                  }
          }
        }
      }
    }
  }

  def deleteItem(listId: String, itemId: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(listId) { lid =>
        withUUID(itemId) { iid =>
          shoppingRepo.findListById(lid).flatMap {
            case None                          => Future.successful(notFound(s"List $listId not found"))
            case Some(l) if l.familyId != fid => Future.successful(forbidden("Access denied"))
            case Some(_) =>
              shoppingRepo.deleteItem(iid).map {
                case true  => NoContent
                case false => notFound(s"Item $itemId not found")
              }
          }
        }
      }
    }
  }

  def clearChecked(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        shoppingRepo.findListById(uid).flatMap {
          case None                          => Future.successful(notFound(s"List $id not found"))
          case Some(l) if l.familyId != fid => Future.successful(forbidden("Access denied"))
          case Some(_) =>
            shoppingRepo.clearChecked(uid).map { count =>
              Ok(Json.obj("deletedCount" -> count.asJson).noSpaces).as("application/json")
            }
        }
      }
    }
  }

  private def withFamily(request: actions.AuthRequest[AnyContent])(block: UUID => Future[Result]): Future[Result] =
    request.familyId match
      case None      => Future.successful(forbidden("User has no family"))
      case Some(fid) => block(fid)

  private def withUUID(id: String)(block: UUID => Future[Result]): Future[Result] =
    try block(UUID.fromString(id))
    catch case _: IllegalArgumentException => Future.successful(badRequest(s"Invalid UUID: $id"))

  private def listView(l: ShoppingList, itemCount: Int, checkedCount: Int): Json =
    Json.obj(
      "id"           -> l.id.toString.asJson,
      "familyId"     -> l.familyId.toString.asJson,
      "name"         -> l.name.asJson,
      "itemCount"    -> itemCount.asJson,
      "checkedCount" -> checkedCount.asJson,
      "createdBy"    -> l.createdBy.toString.asJson,
      "createdAt"    -> l.createdAt.toString.asJson,
      "updatedAt"    -> l.updatedAt.toString.asJson
    )

  private def itemView(i: ShoppingItem): Json =
    Json.obj(
      "id"        -> i.id.toString.asJson,
      "listId"    -> i.listId.toString.asJson,
      "familyId"  -> i.familyId.toString.asJson,
      "name"      -> i.name.asJson,
      "category"  -> i.category.asJson,
      "quantity"  -> i.quantity.asJson,
      "isChecked" -> i.isChecked.asJson,
      "checkedBy" -> i.checkedBy.map(_.toString).asJson,
      "checkedAt" -> i.checkedAt.map(_.toString).asJson,
      "createdBy" -> i.createdBy.toString.asJson,
      "createdAt" -> i.createdAt.toString.asJson,
      "updatedAt" -> i.updatedAt.toString.asJson
    )
