package repositories

import models.{DotoEvent, UpdateEventRequest}
import tables.*
import AppPostgresProfile.api.*
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EventRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[AppPostgresProfile]:

  def list(familyId: UUID, from: Option[Instant], to: Option[Instant], memberId: Option[UUID]): Future[Seq[DotoEvent]] =
    val base = Events.filter(_.familyId === familyId)
    val withFrom = from.fold(base)(f => base.filter(_.startAt >= f))
    val withTo   = to.fold(withFrom)(t => withFrom.filter(_.startAt < t))
    db.run(withTo.sortBy(_.startAt.asc).result).map { events =>
      memberId.fold(events)(mid => events.filter(_.assignedTo.contains(mid)))
    }

  def findById(id: UUID): Future[Option[DotoEvent]] =
    db.run(Events.filter(_.id === id).result.headOption)

  def create(event: DotoEvent): Future[DotoEvent] =
    db.run((Events += event).map(_ => event))

  def update(id: UUID, familyId: UUID, req: models.UpdateEventRequest): Future[Option[DotoEvent]] =
    findById(id).flatMap {
      case None => Future.successful(None)
      case Some(e) if e.familyId != familyId => Future.successful(None)
      case Some(e) =>
        val updated = e.copy(
          title       = req.title.getOrElse(e.title),
          description = req.description.orElse(e.description),
          startAt     = req.startAt.map(Instant.parse).getOrElse(e.startAt),
          endAt       = req.endAt.map(Instant.parse).getOrElse(e.endAt),
          location    = req.location.orElse(e.location),
          color       = req.color.orElse(e.color),
          assignedTo  = req.assignedTo.map(_.map(UUID.fromString)).getOrElse(e.assignedTo),
          updatedAt   = Instant.now()
        )
        db.run(Events.filter(_.id === id).update(updated)).map(_ => Some(updated))
    }

  def delete(id: UUID): Future[Boolean] =
    db.run(Events.filter(_.id === id).delete).map(_ > 0)

  def listTodayByFamily(familyId: UUID, dayStart: Instant, dayEnd: Instant): Future[Seq[DotoEvent]] =
    db.run(
      Events
        .filter(e => e.familyId === familyId && e.startAt >= dayStart && e.startAt < dayEnd)
        .sortBy(_.startAt.asc)
        .result
    )

  def listUpcomingByFamily(familyId: UUID, from: Instant, to: Instant): Future[Seq[DotoEvent]] =
    db.run(
      Events
        .filter(e => e.familyId === familyId && e.startAt >= from && e.startAt < to)
        .sortBy(_.startAt.asc)
        .result
    )
