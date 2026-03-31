package controllers

import actions.AuthenticatedAction
import models.{Profile, RegisterRequest, LoginRequest, UpdateProfileRequest, ChangePasswordRequest, ClaimProfileRequest}
import repositories.{ProfileRepository, FamilyRepository}
import utils.{JwtUtils, PasswordUtils}

import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Json

import play.api.mvc.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

@Singleton
class AuthController @Inject()(
  cc:          ControllerComponents,
  auth:        AuthenticatedAction,
  profileRepo: ProfileRepository,
  familyRepo:  FamilyRepository,
  jwtUtils:    JwtUtils
)(implicit ec: ExecutionContext) extends AbstractController(cc):

  import JsonHelper.*

  private val palette = List("#185FA5","#1D9E75","#BA7517","#993556","#534AB7","#E24B4A")

  private def assignNextColor(existingMembers: Seq[Profile]): String =
    val used = existingMembers.map(_.color).toSet
    palette.find(c => !used.contains(c)).getOrElse(palette(existingMembers.size % palette.size))

  def register: Action[AnyContent] = Action.async { request =>
    val body = readBody(request)
    decode[RegisterRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        val uname = req.username.toLowerCase
        if uname.length < 3 || uname.length > 50 then
          Future.successful(badRequest("username must be 3\u201350 characters"))
        else if !uname.matches("^[a-z0-9_]+$") then
          Future.successful(badRequest("username must be lowercase alphanumeric or underscore"))
        else if req.password.length < 8 then
          Future.successful(badRequest("password must be at least 8 characters"))
        else if req.displayName.trim.isEmpty || req.displayName.length > 100 then
          Future.successful(badRequest("displayName must be 1\u2013100 characters"))
        else if !Set("parent", "child").contains(req.role) then
          Future.successful(badRequest("role must be parent or child"))
        else if req.role == "child" && req.inviteCode.isEmpty then
          Future.successful(badRequest("inviteCode is required for child accounts"))
        else
          profileRepo.findByUsername(uname).flatMap {
            case Some(_) => Future.successful(conflict("Username already taken"))
            case None =>
              req.inviteCode match
                case None =>
                  val profile = Profile(
                    username     = Some(uname),
                    passwordHash = Some(PasswordUtils.hash(req.password)),
                    displayName  = req.displayName.trim,
                    role         = req.role
                  )
                  profileRepo.create(profile).map { created =>
                    val token = jwtUtils.issue(created.id, created.familyId)
                    Created(buildAuthResponse(token, created).noSpaces).as("application/json")
                  }
                case Some(code) =>
                  familyRepo.findByInviteCode(code.trim.toUpperCase).flatMap {
                    case None => Future.successful(notFound("Invite code not found"))
                    case Some(family) =>
                      profileRepo.listByFamily(family.id).flatMap { members =>
                        val color = assignNextColor(members)
                        val profile = Profile(
                          username     = Some(uname),
                          passwordHash = Some(PasswordUtils.hash(req.password)),
                          displayName  = req.displayName.trim,
                          role         = req.role,
                          color        = color,
                          familyId     = Some(family.id)
                        )
                        profileRepo.create(profile).map { created =>
                          val token = jwtUtils.issue(created.id, created.familyId)
                          Created(buildAuthResponse(token, created).noSpaces).as("application/json")
                        }
                      }
                  }
          }
  }

  def login: Action[AnyContent] = Action.async { request =>
    val body = readBody(request)
    decode[LoginRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        profileRepo.findByUsername(req.username.toLowerCase).map {
          case None => Unauthorized(errorBody("unauthorized", "Invalid username or password")).as("application/json")
          case Some(profile) =>
            profile.passwordHash match
              case None => Unauthorized(errorBody("unauthorized", "Invalid username or password")).as("application/json")
              case Some(hash) =>
                if !PasswordUtils.verify(req.password, hash) then
                  Unauthorized(errorBody("unauthorized", "Invalid username or password")).as("application/json")
                else
                  val token = jwtUtils.issue(profile.id, profile.familyId)
                  Ok(buildAuthResponse(token, profile).noSpaces).as("application/json")
        }
  }

  def updateProfile: Action[AnyContent] = auth.async { request =>
    val body = readBody(request)
    decode[UpdateProfileRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        if req.displayName.exists(n => n.isEmpty || n.length > 100) then
          Future.successful(badRequest("displayName must be 1–100 characters"))
        else if req.color.exists(c => !c.matches("^#[0-9A-Fa-f]{6}$")) then
          Future.successful(badRequest("color must be a valid hex colour, e.g. #FF6B6B"))
        else
          profileRepo.update(request.userId, req.displayName, req.color).map {
            case None    => notFound("Profile not found")
            case Some(p) => ok(profileView(p))
          }
  }

  def me: Action[AnyContent] = auth.async { request =>
    profileRepo.findById(request.userId).map {
      case None    => notFound("Profile not found")
      case Some(p) => ok(profileView(p))
    }
  }

  def changePassword: Action[AnyContent] = auth.async { request =>
    val body = readBody(request)
    decode[ChangePasswordRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        if req.newPassword.length < 8 then
          Future.successful(badRequest("newPassword must be at least 8 characters"))
        else
          profileRepo.findById(request.userId).flatMap {
            case None => Future.successful(notFound("Profile not found"))
            case Some(p) =>
              p.passwordHash match
                case None => Future.successful(Unauthorized(errorBody("unauthorized", "Current password is incorrect")).as("application/json"))
                case Some(hash) =>
                  if !PasswordUtils.verify(req.currentPassword, hash) then
                    Future.successful(Unauthorized(errorBody("unauthorized", "Current password is incorrect")).as("application/json"))
                  else
                    profileRepo.changePassword(p.id, PasswordUtils.hash(req.newPassword)).map { _ =>
                      Ok(Json.obj("updated" -> true.asJson).noSpaces).as("application/json")
                    }
          }
  }

  def claimProfile: Action[AnyContent] = Action.async { request =>
    val body = readBody(request)
    decode[ClaimProfileRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        val uname = req.username.toLowerCase.trim
        if uname.length < 3 || uname.length > 50 then
          Future.successful(badRequest("username must be 3\u201350 characters"))
        else if !uname.matches("^[a-z0-9_]+$") then
          Future.successful(badRequest("username must be lowercase alphanumeric or underscore"))
        else if req.password.length < 8 then
          Future.successful(badRequest("password must be at least 8 characters"))
        else
          val code = req.inviteCode.trim.toUpperCase
          familyRepo.findByInviteCode(code).flatMap {
            case None => Future.successful(notFound("Invite code not found"))
            case Some(family) =>
              val profileId = try Some(UUID.fromString(req.profileId)) catch case _: Exception => None
              profileId match
                case None => Future.successful(badRequest("Invalid profileId format"))
                case Some(pid) =>
                  profileRepo.findById(pid).flatMap {
                    case None => Future.successful(notFound("Profile not found"))
                    case Some(profile) =>
                      if profile.familyId != Some(family.id) then
                        Future.successful(forbidden("Profile does not belong to this family"))
                      else if profile.isAuthAccount then
                        Future.successful(conflict("This profile already has an account"))
                      else
                        profileRepo.findByUsername(uname).flatMap {
                          case Some(_) => Future.successful(conflict("Username already taken"))
                          case None =>
                            val hash = PasswordUtils.hash(req.password)
                            profileRepo.claimProfile(pid, uname, hash).map {
                              case None          => conflict("Profile could not be claimed")
                              case Some(updated) =>
                                val token = jwtUtils.issue(updated.id, updated.familyId)
                                Created(buildAuthResponse(token, updated).noSpaces).as("application/json")
                            }
                        }
                  }
          }
  }

  private def profileView(p: Profile): Json =
    Json.obj(
      "id"            -> p.id.toString.asJson,
      "username"      -> p.username.asJson,
      "displayName"   -> p.displayName.asJson,
      "role"          -> p.role.asJson,
      "color"         -> p.color.asJson,
      "points"        -> p.points.asJson,
      "streak"        -> p.streak.asJson,
      "familyId"      -> p.familyId.map(_.toString).asJson,
      "isAuthAccount" -> p.isAuthAccount.asJson,
      "createdAt"     -> p.createdAt.toString.asJson
    )
    
  private def forbidden(msg: String): Result =
    Forbidden(Json.obj("code" -> "forbidden".asJson, "message" -> msg.asJson).noSpaces).as("application/json")

  private def buildAuthResponse(token: String, p: Profile): Json =
    Json.obj(
      "token"   -> token.asJson,
      "profile" -> profileView(p)
    )

  private def errorBody(code: String, msg: String): String =
    Json.obj("code" -> code.asJson, "message" -> msg.asJson).noSpaces
