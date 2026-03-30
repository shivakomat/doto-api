package controllers

import actions.AuthenticatedAction
import models.{Reward, CreateRewardRequest}
import repositories.{RewardRepository, ProfileRepository}

import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Json

import play.api.mvc.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import java.util.UUID

@Singleton
class RewardController @Inject()(
  cc:          ControllerComponents,
  auth:        AuthenticatedAction,
  rewardRepo:  RewardRepository,
  profileRepo: ProfileRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc):

  import JsonHelper.*

  def list(memberId: Option[String], status: Option[String]): Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      val memberUid = memberId.flatMap(m => try Some(UUID.fromString(m)) catch case _ => None)
      rewardRepo.list(fid, memberUid, status).map(rewards => ok(rewards.map(rewardView)))
    }
  }

  def create: Action[AnyContent] = auth.async { request =>
    withFamily(request) { fid =>
      val body = readBody(request)
      decode[CreateRewardRequest](body) match
        case Left(_)    => Future.successful(badRequest("Invalid request body"))
        case Right(req) =>
          if req.title.isEmpty || req.title.length > 200 then
            Future.successful(badRequest("title must be 1–200 characters"))
          else if req.pointsCost <= 0 then
            Future.successful(badRequest("pointsCost must be > 0"))
          else
            profileRepo.findById(UUID.fromString(req.memberId)).flatMap {
              case None => Future.successful(notFound("Member not found"))
              case Some(m) if m.familyId.map(_.toString) != Some(fid.toString) =>
                Future.successful(forbidden("Member does not belong to your family"))
              case Some(m) if m.role != "child" =>
                Future.successful(badRequest("Rewards can only be created for child members"))
              case Some(_) =>
                val reward = Reward(
                  familyId   = fid,
                  memberId   = UUID.fromString(req.memberId),
                  title      = req.title,
                  pointsCost = req.pointsCost
                )
                rewardRepo.create(reward).map(r => created(rewardView(r)))
            }
    }
  }

  def request(id: String): Action[AnyContent] = auth.async { req =>
    withFamily(req) { fid =>
      withUUID(id) { uid =>
        rewardRepo.findById(uid).flatMap {
          case None                          => Future.successful(notFound(s"Reward $id not found"))
          case Some(r) if r.familyId != fid => Future.successful(forbidden("Access denied"))
          case Some(r) if r.status != "active" =>
            Future.successful(badRequest("Reward must be in active status to request redemption"))
          case Some(r) =>
            profileRepo.findById(r.memberId).flatMap {
              case None => Future.successful(notFound("Member not found"))
              case Some(member) if member.points < r.pointsCost =>
                Future.successful(Conflict(Json.obj(
                  "code"    -> "conflict".asJson,
                  "message" -> "Member does not have enough points".asJson
                ).noSpaces).as("application/json"))
              case Some(_) =>
                rewardRepo.updateStatus(uid, "pending_approval", requestedAt = Some(Instant.now())).map {
                  case None    => notFound(s"Reward $id not found")
                  case Some(r) => ok(rewardView(r))
                }
            }
        }
      }
    }
  }

  def approve(id: String): Action[AnyContent] = auth.async { req =>
    withFamily(req) { fid =>
      ensureParent(req) {
        withUUID(id) { uid =>
          rewardRepo.findById(uid).flatMap {
            case None                          => Future.successful(notFound(s"Reward $id not found"))
            case Some(r) if r.familyId != fid => Future.successful(forbidden("Access denied"))
            case Some(r) if r.status != "pending_approval" =>
              Future.successful(badRequest("Reward must be in pending_approval status to approve"))
            case Some(_) =>
              rewardRepo.updateStatus(uid, "approved", approvedBy = Some(req.userId), approvedAt = Some(Instant.now())).map {
                case None    => notFound(s"Reward $id not found")
                case Some(r) => ok(rewardView(r))
              }
          }
        }
      }
    }
  }

  def redeem(id: String): Action[AnyContent] = auth.async { req =>
    withFamily(req) { fid =>
      ensureParent(req) {
        withUUID(id) { uid =>
          rewardRepo.findById(uid).flatMap {
            case None                          => Future.successful(notFound(s"Reward $id not found"))
            case Some(r) if r.familyId != fid => Future.successful(forbidden("Access denied"))
            case Some(r) if r.status != "approved" =>
              Future.successful(badRequest("Reward must be in approved status to redeem"))
            case Some(_) =>
              rewardRepo.updateStatus(uid, "redeemed").map {
                case None    => notFound(s"Reward $id not found")
                case Some(r) => ok(rewardView(r))
              }
          }
        }
      }
    }
  }

  def delete(id: String): Action[AnyContent] = auth.async { req =>
    withFamily(req) { fid =>
      withUUID(id) { uid =>
        rewardRepo.findById(uid).flatMap {
          case None                          => Future.successful(notFound(s"Reward $id not found"))
          case Some(r) if r.familyId != fid => Future.successful(forbidden("Access denied"))
          case Some(r) =>
            profileRepo.findById(req.userId).flatMap { callerOpt =>
              val caller = callerOpt.getOrElse(throw new RuntimeException("Caller not found"))
              if caller.role == "parent" || r.memberId == req.userId then
                rewardRepo.delete(uid).map {
                  case true  => NoContent
                  case false => notFound(s"Reward $id not found")
                }
              else
                Future.successful(forbidden("Only parents or the reward owner can delete a reward"))
            }
        }
      }
    }
  }

  private def withFamily(request: actions.AuthRequest[AnyContent])(block: UUID => Future[Result]): Future[Result] =
    request.familyId match
      case None      => Future.successful(forbidden("User has no family"))
      case Some(fid) => block(fid)

  private def ensureParent(request: actions.AuthRequest[AnyContent])(block: => Future[Result]): Future[Result] =
    profileRepo.findById(request.userId).flatMap {
      case None                          => Future.successful(notFound("Profile not found"))
      case Some(p) if p.role != "parent" => Future.successful(forbidden("Only parents can perform this action"))
      case _                             => block
    }

  private def withUUID(id: String)(block: UUID => Future[Result]): Future[Result] =
    try block(UUID.fromString(id))
    catch case _: IllegalArgumentException => Future.successful(badRequest(s"Invalid UUID: $id"))

  def rewardView(r: Reward): Json =
    Json.obj(
      "id"          -> r.id.toString.asJson,
      "familyId"    -> r.familyId.toString.asJson,
      "memberId"    -> r.memberId.toString.asJson,
      "title"       -> r.title.asJson,
      "pointsCost"  -> r.pointsCost.asJson,
      "status"      -> r.status.asJson,
      "requestedAt" -> r.requestedAt.map(_.toString).asJson,
      "approvedBy"  -> r.approvedBy.map(_.toString).asJson,
      "approvedAt"  -> r.approvedAt.map(_.toString).asJson,
      "createdAt"   -> r.createdAt.toString.asJson,
      "updatedAt"   -> r.updatedAt.toString.asJson
    )
