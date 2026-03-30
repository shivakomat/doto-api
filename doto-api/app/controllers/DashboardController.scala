package controllers

import actions.AuthenticatedAction
import models.Profile
import repositories.{FamilyRepository, ProfileRepository, EventRepository, TaskRepository, RewardRepository}

import io.circe.syntax.*
import io.circe.Json

import play.api.mvc.*
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import java.time.{Instant, ZoneOffset, LocalDate}
import java.util.UUID

@Singleton
class DashboardController @Inject()(
  cc:          ControllerComponents,
  auth:        AuthenticatedAction,
  familyRepo:  FamilyRepository,
  profileRepo: ProfileRepository,
  eventRepo:   EventRepository,
  taskRepo:    TaskRepository,
  rewardRepo:  RewardRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc):

  import JsonHelper.*

  def get: Action[AnyContent] = auth.async { request =>
    request.familyId match
      case None => Future.successful(notFound("User has no family"))
      case Some(fid) =>
        val now          = Instant.now()
        val upcomingEnd  = LocalDate.now(ZoneOffset.UTC).plusDays(5).atStartOfDay(ZoneOffset.UTC).toInstant

        val familyF          = familyRepo.findById(fid)
        val membersF         = profileRepo.listByFamily(fid)
        val upcomingEventsF  = eventRepo.listUpcomingByFamily(fid, now, upcomingEnd)
        val pendingTasksF    = taskRepo.listPendingByFamily(fid)
        val approvalsF       = rewardRepo.listPendingApprovalByFamily(fid)
        val callerF          = profileRepo.findById(request.userId)

        for
          familyOpt      <- familyF
          members        <- membersF
          upcomingEvents <- upcomingEventsF
          pendingTasks   <- pendingTasksF
          approvals      <- approvalsF
          callerOpt      <- callerF
        yield
          familyOpt match
            case None => notFound("Family not found")
            case Some(family) =>
              val isParent = callerOpt.exists(_.role == "parent")

              val familyJson = Json.obj(
                "id"      -> family.id.toString.asJson,
                "name"    -> family.name.asJson,
                "members" -> members.map(memberView).asJson
              )

              val eventsJson = upcomingEvents.map(e => Json.obj(
                "id"         -> e.id.toString.asJson,
                "title"      -> e.title.asJson,
                "startAt"    -> e.startAt.toString.asJson,
                "endAt"      -> e.endAt.toString.asJson,
                "assignedTo" -> e.assignedTo.map(_.toString).asJson,
                "color"      -> e.color.asJson
              ))

              val tasksJson = pendingTasks.map(t => Json.obj(
                "id"         -> t.id.toString.asJson,
                "title"      -> t.title.asJson,
                "assignedTo" -> t.assignedTo.map(_.toString).asJson,
                "priority"   -> t.priority.asJson,
                "points"     -> t.points.asJson,
                "dueAt"      -> t.dueAt.map(_.toString).asJson
              ))

              val approvalsJson = if isParent then approvals.map(r => Json.obj(
                "id"         -> r.id.toString.asJson,
                "memberId"   -> r.memberId.toString.asJson,
                "title"      -> r.title.asJson,
                "pointsCost" -> r.pointsCost.asJson,
                "status"     -> r.status.asJson
              )) else Seq.empty

              Ok(Json.obj(
                "family"            -> familyJson,
                "upcomingEvents"    -> eventsJson.asJson,
                "pendingTasksCount" -> pendingTasks.size.asJson,
                "pendingTasks"      -> tasksJson.asJson,
                "pendingApprovals"  -> approvalsJson.asJson
              ).noSpaces).as("application/json")
  }

  private def memberView(p: Profile): Json =
    Json.obj(
      "id"          -> p.id.toString.asJson,
      "displayName" -> p.displayName.asJson,
      "role"        -> p.role.asJson,
      "color"       -> p.color.asJson,
      "points"      -> p.points.asJson
    )
