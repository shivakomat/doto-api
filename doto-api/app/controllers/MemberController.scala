package controllers

import actions.AuthenticatedAction
import models.{Profile, CreateMemberRequest, UpdateMemberRequest}
import repositories.ProfileRepository

import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Json

import play.api.mvc.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

@Singleton
class MemberController @Inject()(
  cc:          ControllerComponents,
  auth:        AuthenticatedAction,
  profileRepo: ProfileRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc):

  import JsonHelper.*

  def list: Action[AnyContent] = auth.async { request =>
    request.familyId match
      case None      => Future.successful(notFound("User has no family"))
      case Some(fid) =>
        profileRepo.listByFamily(fid).map(members => ok(members.map(memberView)))
  }

  def create: Action[AnyContent] = auth.async { request =>
    ensureParent(request) {
      val body = readBody(request)
      decode[CreateMemberRequest](body) match
        case Left(_)    => Future.successful(badRequest("Invalid request body"))
        case Right(req) =>
          if req.displayName.isEmpty || req.displayName.length > 100 then
            Future.successful(badRequest("displayName must be 1–100 characters"))
          else if !req.color.matches("^#[0-9A-Fa-f]{6}$") then
            Future.successful(badRequest("color must be a valid hex colour, e.g. #FF6B6B"))
          else
            request.familyId match
              case None      => Future.successful(forbidden("User has no family"))
              case Some(fid) =>
                val placeholderId = java.util.UUID.randomUUID().toString.replace("-", "").take(16)
                val profile = Profile(
                  familyId      = Some(fid),
                  username      = s"member_$placeholderId",
                  passwordHash  = "",
                  displayName   = req.displayName,
                  color         = req.color,
                  role          = "child",
                  isAuthAccount = false
                )
                profileRepo.create(profile).map(p => created(memberView(p)))
    }
  }

  def update(id: String): Action[AnyContent] = auth.async { request =>
    ensureParent(request) {
      withUUID(id) { uid =>
        val body = readBody(request)
        decode[UpdateMemberRequest](body) match
          case Left(_)    => Future.successful(badRequest("Invalid request body"))
          case Right(req) =>
            profileRepo.findById(uid).flatMap {
              case None => Future.successful(notFound(s"Member $id not found"))
              case Some(p) if p.familyId != request.familyId =>
                Future.successful(forbidden("Member does not belong to your family"))
              case Some(_) =>
                profileRepo.update(uid, req.displayName, req.color).map {
                  case None    => notFound(s"Member $id not found")
                  case Some(p) => ok(memberView(p))
                }
            }
      }
    }
  }

  def delete(id: String): Action[AnyContent] = auth.async { request =>
    ensureParent(request) {
      withUUID(id) { uid =>
        profileRepo.findById(uid).flatMap {
          case None => Future.successful(notFound(s"Member $id not found"))
          case Some(p) if p.isAuthAccount =>
            Future.successful(forbidden("Cannot delete an auth account via this endpoint"))
          case Some(p) if p.familyId != request.familyId =>
            Future.successful(forbidden("Member does not belong to your family"))
          case Some(_) =>
            profileRepo.delete(uid).map {
              case true  => NoContent
              case false => notFound(s"Member $id not found")
            }
        }
      }
    }
  }

  private def ensureParent(request: actions.AuthRequest[AnyContent])(block: => Future[Result]): Future[Result] =
    profileRepo.findById(request.userId).flatMap {
      case None                       => Future.successful(notFound("Profile not found"))
      case Some(p) if p.role != "parent" => Future.successful(forbidden("Only parents can perform this action"))
      case _                          => block
    }

  private def withUUID(id: String)(block: UUID => Future[Result]): Future[Result] =
    try block(UUID.fromString(id))
    catch case _: IllegalArgumentException => Future.successful(badRequest(s"Invalid UUID: $id"))

  private def memberView(p: Profile): Json =
    Json.obj(
      "id"            -> p.id.toString.asJson,
      "username"      -> (if p.isAuthAccount then p.username.asJson else Json.Null),
      "displayName"   -> p.displayName.asJson,
      "role"          -> p.role.asJson,
      "color"         -> p.color.asJson,
      "points"        -> p.points.asJson,
      "streak"        -> p.streak.asJson,
      "isAuthAccount" -> p.isAuthAccount.asJson
    )
