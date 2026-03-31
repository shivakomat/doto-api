package repositories

import models.Profile
import tables.*
import AppPostgresProfile.api.*
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[AppPostgresProfile]:

  def findById(id: UUID): Future[Option[Profile]] =
    db.run(Profiles.filter(_.id === id).result.headOption)

  def findByUsername(username: String): Future[Option[Profile]] =
    db.run(Profiles.filter(_.username === Option(username.toLowerCase)).result.headOption)

  def findUnclaimedChildren(familyId: UUID): Future[Seq[Profile]] =
    db.run(
      Profiles
        .filter(p => p.familyId === familyId && p.role === "child" && !p.isAuthAccount)
        .result
    )

  def claimProfile(profileId: UUID, username: String, passwordHash: String): Future[Option[Profile]] =
    db.run(
      Profiles
        .filter(p => p.id === profileId && !p.isAuthAccount)
        .map(p => (p.username, p.passwordHash, p.isAuthAccount, p.updatedAt))
        .update((Some(username), Some(passwordHash), true, Instant.now()))
    ).flatMap { rows =>
      if rows == 0 then Future.successful(None)
      else findById(profileId)
    }

  def listByFamily(familyId: UUID): Future[Seq[Profile]] =
    db.run(Profiles.filter(_.familyId === familyId).result)

  def create(profile: Profile): Future[Profile] =
    db.run((Profiles += profile).map(_ => profile))

  def update(id: UUID, displayName: Option[String], color: Option[String]): Future[Option[Profile]] =
    findById(id).flatMap {
      case None => Future.successful(None)
      case Some(p) =>
        val updated = p.copy(
          displayName = displayName.getOrElse(p.displayName),
          color       = color.getOrElse(p.color),
          updatedAt   = Instant.now()
        )
        db.run(Profiles.filter(_.id === id).update(updated)).map(_ => Some(updated))
    }

  def setFamily(id: UUID, familyId: UUID): Future[Unit] =
    db.run(
      Profiles.filter(_.id === id)
        .map(p => (p.familyId, p.updatedAt))
        .update((Some(familyId), Instant.now()))
    ).map(_ => ())

  def joinFamily(id: UUID, familyId: UUID, role: String, color: String): Future[Unit] =
    db.run(
      Profiles.filter(_.id === id)
        .map(p => (p.familyId, p.role, p.color, p.updatedAt))
        .update((Some(familyId), role, color, Instant.now()))
    ).map(_ => ())

  def addPoints(id: UUID, pts: Int): Future[Unit] =
    findById(id).flatMap {
      case None    => Future.unit
      case Some(p) =>
        db.run(
          Profiles.filter(_.id === id).map(_.points).update(p.points + pts)
        ).map(_ => ())
    }

  def changePassword(id: UUID, newHash: String): Future[Unit] =
    db.run(
      Profiles.filter(_.id === id)
        .map(p => (p.passwordHash, p.updatedAt))
        .update((Some(newHash), Instant.now()))
    ).map(_ => ())

  def delete(id: UUID): Future[Boolean] =
    db.run(Profiles.filter(_.id === id).delete).map(_ > 0)
