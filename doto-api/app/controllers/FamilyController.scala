package controllers

import actions.AuthenticatedAction
import models.{Family, CreateFamilyRequest, JoinFamilyRequest, UpdateFamilyRequest}
import repositories.{FamilyRepository, ProfileRepository}
import utils.InviteCodeUtils

import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Json

import play.api.mvc.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

@Singleton
class FamilyController @Inject()(
  cc:          ControllerComponents,
  auth:        AuthenticatedAction,
  familyRepo:  FamilyRepository,
  profileRepo: ProfileRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc):

  import JsonHelper.*

  def create: Action[AnyContent] = auth.async { request =>
    val body = readBody(request)
    decode[CreateFamilyRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        if req.name.trim.isEmpty then
          Future.successful(badRequest("name is required"))
        else
          profileRepo.findById(request.userId).flatMap {
            case None => Future.successful(notFound("Profile not found"))
            case Some(profile) if profile.familyId.isDefined =>
              Future.successful(conflict("User already belongs to a family"))
            case Some(profile) =>
              generateUniqueCode().flatMap { code =>
                val family = Family(name = req.name.trim, inviteCode = code)
                familyRepo.create(family).flatMap { created =>
                  profileRepo.setFamily(profile.id, created.id).flatMap { _ =>
                    val updatedProfile = profile.copy(familyId = Some(created.id))
                    buildFamilyResponse(created, Seq(updatedProfile)).map(j =>
                      Created(j.noSpaces).as("application/json")
                    )
                  }
                }
              }
          }
  }

  def join: Action[AnyContent] = auth.async { request =>
    val body = readBody(request)
    decode[JoinFamilyRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        profileRepo.findById(request.userId).flatMap {
          case None => Future.successful(notFound("Profile not found"))
          case Some(profile) if profile.familyId.isDefined =>
            Future.successful(conflict("User already belongs to a family"))
          case Some(profile) =>
            familyRepo.findByInviteCode(req.inviteCode.toUpperCase).flatMap {
              case None => Future.successful(notFound("Invite code not found"))
              case Some(family) =>
                profileRepo.setFamily(profile.id, family.id).flatMap { _ =>
                  profileRepo.listByFamily(family.id).flatMap { members =>
                    val allMembers = if members.exists(_.id == profile.id) then members
                                    else members :+ profile.copy(familyId = Some(family.id))
                    buildFamilyResponse(family, allMembers).map(j => Ok(j.noSpaces).as("application/json"))
                  }
                }
            }
        }
  }

  def mine: Action[AnyContent] = auth.async { request =>
    request.familyId match
      case None => Future.successful(notFound("User has no family"))
      case Some(fid) =>
        familyRepo.findById(fid).flatMap {
          case None => Future.successful(notFound("Family not found"))
          case Some(family) =>
            profileRepo.listByFamily(fid).flatMap { members =>
              buildFamilyResponse(family, members).map(j => Ok(j.noSpaces).as("application/json"))
            }
        }
  }

  def updateName: Action[AnyContent] = auth.async { request =>
    val body = readBody(request)
    decode[UpdateFamilyRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        if req.name.trim.isEmpty then
          Future.successful(badRequest("name is required"))
        else
          ensureParent(request) {
            request.familyId match
              case None      => Future.successful(notFound("User has no family"))
              case Some(fid) =>
                familyRepo.updateName(fid, req.name.trim).flatMap {
                  case None         => Future.successful(notFound("Family not found"))
                  case Some(family) =>
                    profileRepo.listByFamily(fid).flatMap { members =>
                      buildFamilyResponse(family, members).map(j => Ok(j.noSpaces).as("application/json"))
                    }
                }
          }
  }

  def inviteCode: Action[AnyContent] = auth.async { request =>
    request.familyId match
      case None => Future.successful(notFound("User has no family"))
      case Some(fid) =>
        familyRepo.findById(fid).map {
          case None         => notFound("Family not found")
          case Some(family) => Ok(Json.obj("inviteCode" -> family.inviteCode.asJson).noSpaces).as("application/json")
        }
  }

  private def ensureParent(request: actions.AuthRequest[AnyContent])(block: => Future[Result]): Future[Result] =
    profileRepo.findById(request.userId).flatMap {
      case None                             => Future.successful(notFound("Profile not found"))
      case Some(p) if p.role != "parent"    => Future.successful(forbidden("Only parents can perform this action"))
      case _                                => block
    }

  private def generateUniqueCode(attempts: Int = 5): Future[String] =
    if attempts <= 0 then Future.failed(new RuntimeException("Failed to generate unique invite code"))
    else
      val code = InviteCodeUtils.generate()
      familyRepo.inviteCodeExists(code).flatMap {
        case true  => generateUniqueCode(attempts - 1)
        case false => Future.successful(code)
      }

  private def buildFamilyResponse(family: Family, members: Seq[models.Profile]): Future[Json] =
    Future.successful(
      Json.obj(
        "id"         -> family.id.toString.asJson,
        "name"       -> family.name.asJson,
        "inviteCode" -> family.inviteCode.asJson,
        "members"    -> members.map(memberView).asJson,
        "createdAt"  -> family.createdAt.toString.asJson
      )
    )

  private def memberView(p: models.Profile): Json =
    Json.obj(
      "id"            -> p.id.toString.asJson,
      "displayName"   -> p.displayName.asJson,
      "role"          -> p.role.asJson,
      "color"         -> p.color.asJson,
      "points"        -> p.points.asJson,
      "isAuthAccount" -> p.isAuthAccount.asJson
    )
