package repositories

import models.Reward
import tables.*
import AppPostgresProfile.api.*
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RewardRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[AppPostgresProfile]:

  def list(familyId: UUID, memberId: Option[UUID], status: Option[String]): Future[Seq[Reward]] =
    var q = Rewards.filter(_.familyId === familyId)
    memberId.foreach(mid => q = q.filter(_.memberId === mid))
    status.foreach(s    => q = q.filter(_.status === s))
    db.run(q.sortBy(_.createdAt.desc).result)

  def findById(id: UUID): Future[Option[Reward]] =
    db.run(Rewards.filter(_.id === id).result.headOption)

  def create(reward: Reward): Future[Reward] =
    db.run((Rewards += reward).map(_ => reward))

  def updateStatus(id: UUID, status: String, requestedAt: Option[Instant] = None, approvedBy: Option[UUID] = None, approvedAt: Option[Instant] = None): Future[Option[Reward]] =
    findById(id).flatMap {
      case None => Future.successful(None)
      case Some(r) =>
        val updated = r.copy(
          status      = status,
          requestedAt = requestedAt.orElse(r.requestedAt),
          approvedBy  = approvedBy.orElse(r.approvedBy),
          approvedAt  = approvedAt.orElse(r.approvedAt),
          updatedAt   = Instant.now()
        )
        db.run(Rewards.filter(_.id === id).update(updated)).map(_ => Some(updated))
    }

  def delete(id: UUID): Future[Boolean] =
    db.run(Rewards.filter(_.id === id).delete).map(_ > 0)

  def listPendingApprovalByFamily(familyId: UUID): Future[Seq[Reward]] =
    db.run(
      Rewards
        .filter(r => r.familyId === familyId && r.status === "pending_approval")
        .sortBy(_.requestedAt.desc)
        .result
    )
