import com.google.inject.AbstractModule
import actions.AuthenticatedAction
import repositories.*

class Module extends AbstractModule:

  override def configure(): Unit =

    // ── Repositories ──────────────────────────────────────────────────────────
    bind(classOf[ProfileRepository]).asEagerSingleton()
    bind(classOf[FamilyRepository]).asEagerSingleton()
    bind(classOf[EventRepository]).asEagerSingleton()
    bind(classOf[TaskRepository]).asEagerSingleton()
    bind(classOf[ShoppingRepository]).asEagerSingleton()
    bind(classOf[RewardRepository]).asEagerSingleton()

    // ── Auth ──────────────────────────────────────────────────────────────────
    bind(classOf[AuthenticatedAction]).asEagerSingleton()
