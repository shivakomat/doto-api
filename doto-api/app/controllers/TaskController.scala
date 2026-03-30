package controllers

import actions.AuthenticatedAction
import models.{DotoTask, CreateTaskRequest, UpdateTaskRequest}
import repositories.{TaskRepository, ProfileRepository}

import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Json

import play.api.mvc.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import java.util.UUID

@Singleton
class TaskController @Inject()(
  cc:          ControllerComponents,
  auth:        AuthenticatedAction,
  taskRepo:    TaskRepository,
  profileRepo: ProfileRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc):

  import JsonHelper.*

  def list(assignedTo: Option[String], status: Option[String], priority: Option[String]): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      val memberUid = assignedTo.flatMap(m => try Some(UUID.fromString(m)) catch case _ => None)
      taskRepo.list(fid, memberUid, status, priority).map(tasks => ok(tasks.map(taskView)))
    }
  }

  def create: Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      val body = readBody(request)
      decode[CreateTaskRequest](body) match
        case Left(_)    => Future.successful(badRequest("Invalid request body"))
        case Right(req) =>
          if req.title.isEmpty || req.title.length > 200 then
            Future.successful(badRequest("title must be 1–200 characters"))
          else if req.points.exists(_ < 0) then
            Future.successful(badRequest("points must be >= 0"))
          else
            val task = DotoTask(
              familyId    = fid,
              title       = req.title,
              description = req.description,
              assignedTo  = req.assignedTo.map(UUID.fromString),
              priority    = req.priority.getOrElse("medium"),
              points      = req.points.getOrElse(0),
              dueAt       = req.dueAt.map(Instant.parse),
              createdBy   = request.userId
            )
            taskRepo.create(task).map(t => created(taskView(t)))
    }
  }

  def get(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        taskRepo.findById(uid).map {
          case None                          => notFound(s"Task $id not found")
          case Some(t) if t.familyId != fid => forbidden("Access denied")
          case Some(t)                       => ok(taskView(t))
        }
      }
    }
  }

  def update(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        val body = readBody(request)
        decode[UpdateTaskRequest](body) match
          case Left(_)    => Future.successful(badRequest("Invalid request body"))
          case Right(req) =>
            taskRepo.findById(uid).flatMap {
              case None                          => Future.successful(notFound(s"Task $id not found"))
              case Some(t) if t.familyId != fid => Future.successful(forbidden("Access denied"))
              case Some(_) =>
                taskRepo.update(uid, fid, req).map {
                  case None    => notFound(s"Task $id not found")
                  case Some(t) => ok(taskView(t))
                }
            }
      }
    }
  }

  def complete(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        taskRepo.findById(uid).flatMap {
          case None                          => Future.successful(notFound(s"Task $id not found"))
          case Some(t) if t.familyId != fid => Future.successful(forbidden("Access denied"))
          case Some(t) =>
            taskRepo.complete(uid, fid).flatMap {
              case None           => Future.successful(notFound(s"Task $id not found"))
              case Some(updated)  =>
                if updated.points > 0 then
                  updated.assignedTo match
                    case None    => Future.successful(ok(taskView(updated)))
                    case Some(mid) =>
                      profileRepo.addPoints(mid, updated.points).map(_ => ok(taskView(updated)))
                else
                  Future.successful(ok(taskView(updated)))
            }
        }
      }
    }
  }

  def delete(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        taskRepo.findById(uid).flatMap {
          case None                          => Future.successful(notFound(s"Task $id not found"))
          case Some(t) if t.familyId != fid => Future.successful(forbidden("Access denied"))
          case Some(_) =>
            taskRepo.delete(uid).map {
              case true  => NoContent
              case false => notFound(s"Task $id not found")
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

  def taskView(t: DotoTask): Json =
    Json.obj(
      "id"          -> t.id.toString.asJson,
      "familyId"    -> t.familyId.toString.asJson,
      "title"       -> t.title.asJson,
      "description" -> t.description.asJson,
      "assignedTo"  -> t.assignedTo.map(_.toString).asJson,
      "status"      -> t.status.asJson,
      "priority"    -> t.priority.asJson,
      "points"      -> t.points.asJson,
      "dueAt"       -> t.dueAt.map(_.toString).asJson,
      "completedAt" -> t.completedAt.map(_.toString).asJson,
      "createdBy"   -> t.createdBy.toString.asJson,
      "createdAt"   -> t.createdAt.toString.asJson,
      "updatedAt"   -> t.updatedAt.toString.asJson
    )
