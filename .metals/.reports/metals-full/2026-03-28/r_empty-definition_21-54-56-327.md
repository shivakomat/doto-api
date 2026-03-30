error id: file://<WORKSPACE>/doto-api/app/models/Domain.scala:String#
file://<WORKSPACE>/doto-api/app/models/Domain.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -io/circe/generic/semiauto/String#
	 -String#
	 -scala/Predef.String#
offset: 316
uri: file://<WORKSPACE>/doto-api/app/models/Domain.scala
text:
```scala
package models

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto.*
import java.util.UUID
import java.time.Instant

// ── Family ────────────────────────────────────────────────────────────────────

case class Family(
  id:         UUID   = UUID.randomUUID(),
  name:       String,
  inviteCode: St@@ring,
  createdAt:  Instant = Instant.now(),
  updatedAt:  Instant = Instant.now()
)

object Family:
  given Encoder[Family] = deriveEncoder
  given Decoder[Family] = deriveDecoder

// ── Profile ───────────────────────────────────────────────────────────────────

case class Profile(
  id:            UUID          = UUID.randomUUID(),
  familyId:      Option[UUID]  = None,
  username:      Option[String] = None,
  passwordHash:  Option[String] = None,
  displayName:   String,
  role:          String        = "parent",
  color:         String        = "#6C63FF",
  points:        Int           = 0,
  isAuthAccount: Boolean       = true,
  createdAt:     Instant       = Instant.now(),
  updatedAt:     Instant       = Instant.now()
)

object Profile:
  given Encoder[Profile] = deriveEncoder
  given Decoder[Profile] = deriveDecoder

// ── DotoEvent ─────────────────────────────────────────────────────────────────

case class DotoEvent(
  id:          UUID         = UUID.randomUUID(),
  familyId:    UUID,
  title:       String,
  description: Option[String] = None,
  startAt:     Instant,
  endAt:       Instant,
  location:    Option[String] = None,
  color:       Option[String] = None,
  assignedTo:  List[UUID]   = Nil,
  createdBy:   UUID,
  createdAt:   Instant      = Instant.now(),
  updatedAt:   Instant      = Instant.now()
)

object DotoEvent:
  given Encoder[DotoEvent] = deriveEncoder
  given Decoder[DotoEvent] = deriveDecoder

// ── DotoTask ──────────────────────────────────────────────────────────────────

case class DotoTask(
  id:          UUID          = UUID.randomUUID(),
  familyId:    UUID,
  title:       String,
  description: Option[String] = None,
  assignedTo:  Option[UUID]  = None,
  status:      String        = "todo",
  priority:    String        = "medium",
  points:      Int           = 0,
  dueAt:       Option[Instant] = None,
  completedAt: Option[Instant] = None,
  createdBy:   UUID,
  createdAt:   Instant       = Instant.now(),
  updatedAt:   Instant       = Instant.now()
)

object DotoTask:
  given Encoder[DotoTask] = deriveEncoder
  given Decoder[DotoTask] = deriveDecoder

// ── ShoppingList ──────────────────────────────────────────────────────────────

case class ShoppingList(
  id:        UUID    = UUID.randomUUID(),
  familyId:  UUID,
  name:      String,
  createdBy: UUID,
  createdAt: Instant = Instant.now(),
  updatedAt: Instant = Instant.now()
)

object ShoppingList:
  given Encoder[ShoppingList] = deriveEncoder
  given Decoder[ShoppingList] = deriveDecoder

// ── ShoppingItem ──────────────────────────────────────────────────────────────

case class ShoppingItem(
  id:        UUID          = UUID.randomUUID(),
  listId:    UUID,
  familyId:  UUID,
  name:      String,
  category:  String        = "other",
  quantity:  Option[String] = None,
  isChecked: Boolean       = false,
  checkedBy: Option[UUID]  = None,
  checkedAt: Option[Instant] = None,
  createdBy: UUID,
  createdAt: Instant       = Instant.now(),
  updatedAt: Instant       = Instant.now()
)

object ShoppingItem:
  given Encoder[ShoppingItem] = deriveEncoder
  given Decoder[ShoppingItem] = deriveDecoder

// ── Reward ────────────────────────────────────────────────────────────────────

case class Reward(
  id:          UUID          = UUID.randomUUID(),
  familyId:    UUID,
  memberId:    UUID,
  title:       String,
  pointsCost:  Int,
  status:      String        = "active",
  requestedAt: Option[Instant] = None,
  approvedBy:  Option[UUID]  = None,
  approvedAt:  Option[Instant] = None,
  createdAt:   Instant       = Instant.now(),
  updatedAt:   Instant       = Instant.now()
)

object Reward:
  given Encoder[Reward] = deriveEncoder
  given Decoder[Reward] = deriveDecoder

```


#### Short summary: 

empty definition using pc, found symbol in pc: 