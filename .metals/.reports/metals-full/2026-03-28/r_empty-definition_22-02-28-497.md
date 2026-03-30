error id: file://<WORKSPACE>/doto-api/app/controllers/AuthController.scala:ProfileRepository#
file://<WORKSPACE>/doto-api/app/controllers/AuthController.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -repositories/ProfileRepository#
	 -io/circe/syntax/ProfileRepository#
	 -play/api/mvc/ProfileRepository#
	 -ProfileRepository#
	 -scala/Predef.ProfileRepository#
offset: 534
uri: file://<WORKSPACE>/doto-api/app/controllers/AuthController.scala
text:
```scala
package controllers

import actions.AuthenticatedAction
import models.{Profile, RegisterRequest, LoginRequest}
import repositories.ProfileRepository
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
  profileRepo: P@@rofileRepository,
  jwtUtils:    JwtUtils
)(implicit ec: ExecutionContext) extends AbstractController(cc):

  import JsonHelper.*

  def register: Action[AnyContent] = Action.async { request =>
    val body = readBody(request)
    decode[RegisterRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        if req.username.length < 6 || req.username.length > 12 then
          Future.successful(badRequest("username must be 3–50 characters"))
        else if !req.username.matches("[a-zA-Z0-9_]+") then
          Future.successful(badRequest("username must be alphanumeric or underscore"))
        else if req.password.length < 8 then
          Future.successful(badRequest("password must be at least 8 characters"))
        else if req.displayName.isEmpty || req.displayName.length > 100 then
          Future.successful(badRequest("displayName must be 1–100 characters"))
        else
          profileRepo.findByUsername(req.username).flatMap {
            case Some(_) => Future.successful(conflict("Username already taken"))
            case None =>
              val profile = Profile(
                username     = Some(req.username),
                passwordHash = Some(PasswordUtils.hash(req.password)),
                displayName  = req.displayName,
                role         = "parent"
              )
              profileRepo.create(profile).map { created =>
                val token = jwtUtils.issue(created.id, created.familyId)
                Created(buildAuthResponse(token, created).noSpaces).as("application/json")
              }
          }
  }

  def login: Action[AnyContent] = Action.async { request =>
    val body = readBody(request)
    decode[LoginRequest](body) match
      case Left(_)    => Future.successful(badRequest("Invalid request body"))
      case Right(req) =>
        profileRepo.findByUsername(req.username).map {
          case None => Unauthorized(errorBody("unauthorized", "Invalid username or password")).as("application/json")
          case Some(profile) =>
            val hashOk = profile.passwordHash.exists(h => PasswordUtils.verify(req.password, h))
            if !hashOk then
              Unauthorized(errorBody("unauthorized", "Invalid username or password")).as("application/json")
            else
              val token = jwtUtils.issue(profile.id, profile.familyId)
              Ok(buildAuthResponse(token, profile).noSpaces).as("application/json")
        }
  }

  def me: Action[AnyContent] = auth.async { request =>
    profileRepo.findById(request.userId).map {
      case None    => notFound("Profile not found")
      case Some(p) => ok(profileView(p))
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
      "familyId"      -> p.familyId.map(_.toString).asJson,
      "isAuthAccount" -> p.isAuthAccount.asJson,
      "createdAt"     -> p.createdAt.toString.asJson
    )

  private def buildAuthResponse(token: String, p: Profile): Json =
    Json.obj(
      "token"   -> token.asJson,
      "profile" -> profileView(p)
    )

  private def errorBody(code: String, msg: String): String =
    Json.obj("code" -> code.asJson, "message" -> msg.asJson).noSpaces

```


#### Short summary: 

empty definition using pc, found symbol in pc: 