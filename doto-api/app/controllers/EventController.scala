package controllers

import actions.AuthenticatedAction
import models.{DotoEvent, CreateEventRequest, UpdateEventRequest}
import repositories.EventRepository

import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Json

import play.api.mvc.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import java.util.UUID

@Singleton
class EventController @Inject()(
  cc:        ControllerComponents,
  auth:      AuthenticatedAction,
  eventRepo: EventRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc):

  import JsonHelper.*

  def list(from: Option[String], to: Option[String], memberId: Option[String]): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      val fromI    = from.map(Instant.parse)
      val toI      = to.map(Instant.parse)
      val memberUid = memberId.flatMap(m => try Some(UUID.fromString(m)) catch case _ => None)
      eventRepo.list(fid, fromI, toI, memberUid).map(events => ok(events.map(eventView)))
    }
  }

  def create: Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      val body = readBody(request)
      decode[CreateEventRequest](body) match
        case Left(_)    => Future.successful(badRequest("Invalid request body"))
        case Right(req) =>
          if req.title.isEmpty || req.title.length > 200 then
            Future.successful(badRequest("title must be 1–200 characters"))
          else
            val startAt = Instant.parse(req.startAt)
            val endAt   = Instant.parse(req.endAt)
            if !endAt.isAfter(startAt) then
              Future.successful(badRequest("endAt must be after startAt"))
            else
              val event = DotoEvent(
                familyId    = fid,
                title       = req.title,
                description = req.description,
                startAt     = startAt,
                endAt       = endAt,
                location    = req.location,
                color       = req.color,
                assignedTo  = req.assignedTo.getOrElse(Nil).map(UUID.fromString),
                createdBy   = request.userId
              )
              eventRepo.create(event).map(e => created(eventView(e)))
    }
  }

  def get(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        eventRepo.findById(uid).map {
          case None                            => notFound(s"Event $id not found")
          case Some(e) if e.familyId != fid   => forbidden("Event belongs to a different family")
          case Some(e)                         => ok(eventView(e))
        }
      }
    }
  }

  def update(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        val body = readBody(request)
        decode[UpdateEventRequest](body) match
          case Left(_)    => Future.successful(badRequest("Invalid request body"))
          case Right(req) =>
            eventRepo.findById(uid).flatMap {
              case None                          => Future.successful(notFound(s"Event $id not found"))
              case Some(e) if e.familyId != fid => Future.successful(forbidden("Access denied"))
              case Some(_) =>
                eventRepo.update(uid, fid, req).map {
                  case None    => notFound(s"Event $id not found")
                  case Some(e) => ok(eventView(e))
                }
            }
      }
    }
  }

  def delete(id: String): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      withUUID(id) { uid =>
        eventRepo.findById(uid).flatMap {
          case None                          => Future.successful(notFound(s"Event $id not found"))
          case Some(e) if e.familyId != fid => Future.successful(forbidden("Access denied"))
          case Some(_) =>
            eventRepo.delete(uid).map {
              case true  => NoContent
              case false => notFound(s"Event $id not found")
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

  def eventView(e: DotoEvent): Json =
    Json.obj(
      "id"          -> e.id.toString.asJson,
      "familyId"    -> e.familyId.toString.asJson,
      "title"       -> e.title.asJson,
      "description" -> e.description.asJson,
      "startAt"     -> e.startAt.toString.asJson,
      "endAt"       -> e.endAt.toString.asJson,
      "location"    -> e.location.asJson,
      "color"       -> e.color.asJson,
      "assignedTo"  -> e.assignedTo.map(_.toString).asJson,
      "createdBy"   -> e.createdBy.toString.asJson,
      "createdAt"   -> e.createdAt.toString.asJson,
      "updatedAt"   -> e.updatedAt.toString.asJson
    )
