package tables

import models.Reward
import AppPostgresProfile.api.*
import java.util.UUID
import java.time.Instant

class RewardTable(tag: Tag) extends Table[Reward](tag, "rewards"):
  def id          = column[UUID]("id", O.PrimaryKey)
  def familyId    = column[UUID]("family_id")
  def memberId    = column[UUID]("member_id")
  def title       = column[String]("title")
  def pointsCost  = column[Int]("points_cost")
  def status      = column[String]("status")
  def requestedAt = column[Option[Instant]]("requested_at")
  def approvedBy  = column[Option[UUID]]("approved_by")
  def approvedAt  = column[Option[Instant]]("approved_at")
  def createdAt   = column[Instant]("created_at")
  def updatedAt   = column[Instant]("updated_at")

  def familyFk = foreignKey("fk_reward_family", familyId, Families)(_.id)
  def memberFk = foreignKey("fk_reward_member", memberId, Profiles)(_.id)

  def * = (
    id, familyId, memberId, title, pointsCost, status,
    requestedAt, approvedBy, approvedAt, createdAt, updatedAt
  ).mapTo[Reward]

val Rewards = TableQuery[RewardTable]
