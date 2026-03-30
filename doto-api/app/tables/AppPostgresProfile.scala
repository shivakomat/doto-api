package tables

import com.github.tminglei.slickpg.*
import java.util.UUID

trait AppPostgresProfile
    extends ExPostgresProfile
    with PgDate2Support
    with PgCirceJsonSupport
    with PgArraySupport:

  def pgjson = "jsonb"

  override val api: AppAPI = new AppAPI {}

  trait AppAPI
      extends ExtPostgresAPI
      with Date2DateTimeImplicitsDuration
      with CirceImplicits
      with ArrayImplicits:

    given uuidArrayMapper: BaseColumnType[List[UUID]] =
      MappedColumnType.base[List[UUID], List[String]](
        list => list.map(_.toString),
        strs => strs.map(UUID.fromString)
      )

object AppPostgresProfile extends AppPostgresProfile
