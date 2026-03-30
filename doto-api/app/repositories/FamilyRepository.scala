package repositories

import models.Family
import tables.*
import AppPostgresProfile.api.*
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FamilyRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[AppPostgresProfile]:

  def create(family: Family): Future[Family] =
    db.run((Families += family).map(_ => family))

  def findById(id: UUID): Future[Option[Family]] =
    db.run(Families.filter(_.id === id).result.headOption)

  def findByInviteCode(code: String): Future[Option[Family]] =
    db.run(Families.filter(_.inviteCode === code).result.headOption)

  def inviteCodeExists(code: String): Future[Boolean] =
    db.run(Families.filter(_.inviteCode === code).exists.result)

  def updateName(id: UUID, name: String): Future[Option[Family]] =
    findById(id).flatMap {
      case None => Future.successful(None)
      case Some(f) =>
        val updated = f.copy(name = name, updatedAt = Instant.now())
        db.run(Families.filter(_.id === id).update(updated)).map(_ => Some(updated))
    }
