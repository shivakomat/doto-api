package repositories

import models.{DotoTask, UpdateTaskRequest}
import tables.*
import AppPostgresProfile.api.*
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskRepository @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[AppPostgresProfile]:

  def list(familyId: UUID, assignedTo: Option[UUID], status: Option[String], priority: Option[String]): Future[Seq[DotoTask]] =
    var q = Tasks.filter(_.familyId === familyId)
    assignedTo.foreach(mid => q = q.filter(_.assignedTo === mid))
    status.foreach(s    => q = q.filter(_.status === s))
    priority.foreach(p  => q = q.filter(_.priority === p))
    db.run(q.sortBy(_.createdAt.desc).result)

  def findById(id: UUID): Future[Option[DotoTask]] =
    db.run(Tasks.filter(_.id === id).result.headOption)

  def create(task: DotoTask): Future[DotoTask] =
    db.run((Tasks += task).map(_ => task))

  def update(id: UUID, familyId: UUID, req: UpdateTaskRequest): Future[Option[DotoTask]] =
    findById(id).flatMap {
      case None => Future.successful(None)
      case Some(t) if t.familyId != familyId => Future.successful(None)
      case Some(t) =>
        val updated = t.copy(
          title       = req.title.getOrElse(t.title),
          description = req.description.orElse(t.description),
          assignedTo  = req.assignedTo.map(UUID.fromString).orElse(t.assignedTo),
          status      = req.status.getOrElse(t.status),
          priority    = req.priority.getOrElse(t.priority),
          points      = req.points.getOrElse(t.points),
          dueAt       = req.dueAt.map(Instant.parse).orElse(t.dueAt),
          updatedAt   = Instant.now()
        )
        db.run(Tasks.filter(_.id === id).update(updated)).map(_ => Some(updated))
    }

  def complete(id: UUID, familyId: UUID): Future[Option[DotoTask]] =
    findById(id).flatMap {
      case None => Future.successful(None)
      case Some(t) if t.familyId != familyId => Future.successful(None)
      case Some(t) =>
        val updated = t.copy(
          status      = "done",
          completedAt = Some(Instant.now()),
          updatedAt   = Instant.now()
        )
        db.run(Tasks.filter(_.id === id).update(updated)).map(_ => Some(updated))
    }

  def delete(id: UUID): Future[Boolean] =
    db.run(Tasks.filter(_.id === id).delete).map(_ > 0)

  def listPendingByFamily(familyId: UUID): Future[Seq[DotoTask]] =
    db.run(
      Tasks
        .filter(t => t.familyId === familyId && (t.status === "todo" || t.status === "in_progress"))
        .sortBy(_.createdAt.desc)
        .result
    )
