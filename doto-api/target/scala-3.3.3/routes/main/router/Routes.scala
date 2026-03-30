// @GENERATOR:play-routes-compiler
// @SOURCE:conf/routes

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:2
  HealthController_6: controllers.HealthController,
  // @LINE:5
  AuthController_4: controllers.AuthController,
  // @LINE:11
  FamilyController_1: controllers.FamilyController,
  // @LINE:18
  MemberController_2: controllers.MemberController,
  // @LINE:24
  EventController_3: controllers.EventController,
  // @LINE:31
  TaskController_8: controllers.TaskController,
  // @LINE:39
  ShoppingController_5: controllers.ShoppingController,
  // @LINE:49
  RewardController_7: controllers.RewardController,
  // @LINE:57
  DashboardController_0: controllers.DashboardController,
  val prefix: String
) extends GeneratedRouter {

  @javax.inject.Inject()
  def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:2
    HealthController_6: controllers.HealthController,
    // @LINE:5
    AuthController_4: controllers.AuthController,
    // @LINE:11
    FamilyController_1: controllers.FamilyController,
    // @LINE:18
    MemberController_2: controllers.MemberController,
    // @LINE:24
    EventController_3: controllers.EventController,
    // @LINE:31
    TaskController_8: controllers.TaskController,
    // @LINE:39
    ShoppingController_5: controllers.ShoppingController,
    // @LINE:49
    RewardController_7: controllers.RewardController,
    // @LINE:57
    DashboardController_0: controllers.DashboardController
  ) = this(errorHandler, HealthController_6, AuthController_4, FamilyController_1, MemberController_2, EventController_3, TaskController_8, ShoppingController_5, RewardController_7, DashboardController_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, HealthController_6, AuthController_4, FamilyController_1, MemberController_2, EventController_3, TaskController_8, ShoppingController_5, RewardController_7, DashboardController_0, prefix)
  }

  private val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/health""", """controllers.HealthController.check"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/auth/register""", """controllers.AuthController.register"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/auth/login""", """controllers.AuthController.login"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/auth/me""", """controllers.AuthController.me"""),
    ("""PATCH""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/auth/profile""", """controllers.AuthController.updateProfile"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/families""", """controllers.FamilyController.create"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/families/join""", """controllers.FamilyController.join"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/families/mine""", """controllers.FamilyController.mine"""),
    ("""PATCH""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/families/mine""", """controllers.FamilyController.updateName"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/families/mine/invite-code""", """controllers.FamilyController.inviteCode"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/members""", """controllers.MemberController.list"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/members""", """controllers.MemberController.create"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/members/""" + "$" + """id<[^/]+>""", """controllers.MemberController.update(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/members/""" + "$" + """id<[^/]+>""", """controllers.MemberController.delete(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/events""", """controllers.EventController.list(from:Option[String] ?= None, to:Option[String] ?= None, memberId:Option[String] ?= None)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/events""", """controllers.EventController.create"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/events/""" + "$" + """id<[^/]+>""", """controllers.EventController.get(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/events/""" + "$" + """id<[^/]+>""", """controllers.EventController.update(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/events/""" + "$" + """id<[^/]+>""", """controllers.EventController.delete(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/tasks""", """controllers.TaskController.list(assignedTo:Option[String] ?= None, status:Option[String] ?= None, priority:Option[String] ?= None)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/tasks""", """controllers.TaskController.create"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/tasks/""" + "$" + """id<[^/]+>""", """controllers.TaskController.get(id:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/tasks/""" + "$" + """id<[^/]+>""", """controllers.TaskController.update(id:String)"""),
    ("""PATCH""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/tasks/""" + "$" + """id<[^/]+>/complete""", """controllers.TaskController.complete(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/tasks/""" + "$" + """id<[^/]+>""", """controllers.TaskController.delete(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/shopping/lists""", """controllers.ShoppingController.listLists"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/shopping/lists""", """controllers.ShoppingController.createList"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/shopping/lists/""" + "$" + """id<[^/]+>""", """controllers.ShoppingController.deleteList(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/shopping/lists/""" + "$" + """id<[^/]+>/items""", """controllers.ShoppingController.listItems(id:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/shopping/lists/""" + "$" + """id<[^/]+>/items""", """controllers.ShoppingController.addItem(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/shopping/lists/""" + "$" + """id<[^/]+>/items/checked""", """controllers.ShoppingController.clearChecked(id:String)"""),
    ("""PATCH""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/shopping/lists/""" + "$" + """listId<[^/]+>/items/""" + "$" + """itemId<[^/]+>/check""", """controllers.ShoppingController.checkItem(listId:String, itemId:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/shopping/lists/""" + "$" + """listId<[^/]+>/items/""" + "$" + """itemId<[^/]+>""", """controllers.ShoppingController.deleteItem(listId:String, itemId:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/rewards""", """controllers.RewardController.list(memberId:Option[String] ?= None, status:Option[String] ?= None)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/rewards""", """controllers.RewardController.create"""),
    ("""PATCH""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/rewards/""" + "$" + """id<[^/]+>/request""", """controllers.RewardController.request(id:String)"""),
    ("""PATCH""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/rewards/""" + "$" + """id<[^/]+>/approve""", """controllers.RewardController.approve(id:String)"""),
    ("""PATCH""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/rewards/""" + "$" + """id<[^/]+>/redeem""", """controllers.RewardController.redeem(id:String)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/rewards/""" + "$" + """id<[^/]+>""", """controllers.RewardController.delete(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """api/dashboard""", """controllers.DashboardController.get"""),
    Nil
  ).foldLeft(Seq.empty[(String, String, String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String, String, String)]
    case l => s ++ l.asInstanceOf[List[(String, String, String)]]
  }}


  // @LINE:2
  private lazy val controllers_HealthController_check0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/health")))
  )
  private lazy val controllers_HealthController_check0_invoker = createInvoker(
    HealthController_6.check,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HealthController",
      "check",
      Nil,
      "GET",
      this.prefix + """api/health""",
      """ Health""",
      Seq()
    )
  )

  // @LINE:5
  private lazy val controllers_AuthController_register1_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/auth/register")))
  )
  private lazy val controllers_AuthController_register1_invoker = createInvoker(
    AuthController_4.register,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.AuthController",
      "register",
      Nil,
      "POST",
      this.prefix + """api/auth/register""",
      """ Auth (no JWT required)""",
      Seq()
    )
  )

  // @LINE:6
  private lazy val controllers_AuthController_login2_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/auth/login")))
  )
  private lazy val controllers_AuthController_login2_invoker = createInvoker(
    AuthController_4.login,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.AuthController",
      "login",
      Nil,
      "POST",
      this.prefix + """api/auth/login""",
      """""",
      Seq()
    )
  )

  // @LINE:7
  private lazy val controllers_AuthController_me3_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/auth/me")))
  )
  private lazy val controllers_AuthController_me3_invoker = createInvoker(
    AuthController_4.me,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.AuthController",
      "me",
      Nil,
      "GET",
      this.prefix + """api/auth/me""",
      """""",
      Seq()
    )
  )

  // @LINE:8
  private lazy val controllers_AuthController_updateProfile4_route = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/auth/profile")))
  )
  private lazy val controllers_AuthController_updateProfile4_invoker = createInvoker(
    AuthController_4.updateProfile,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.AuthController",
      "updateProfile",
      Nil,
      "PATCH",
      this.prefix + """api/auth/profile""",
      """""",
      Seq()
    )
  )

  // @LINE:11
  private lazy val controllers_FamilyController_create5_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/families")))
  )
  private lazy val controllers_FamilyController_create5_invoker = createInvoker(
    FamilyController_1.create,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.FamilyController",
      "create",
      Nil,
      "POST",
      this.prefix + """api/families""",
      """ Family""",
      Seq()
    )
  )

  // @LINE:12
  private lazy val controllers_FamilyController_join6_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/families/join")))
  )
  private lazy val controllers_FamilyController_join6_invoker = createInvoker(
    FamilyController_1.join,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.FamilyController",
      "join",
      Nil,
      "POST",
      this.prefix + """api/families/join""",
      """""",
      Seq()
    )
  )

  // @LINE:13
  private lazy val controllers_FamilyController_mine7_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/families/mine")))
  )
  private lazy val controllers_FamilyController_mine7_invoker = createInvoker(
    FamilyController_1.mine,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.FamilyController",
      "mine",
      Nil,
      "GET",
      this.prefix + """api/families/mine""",
      """""",
      Seq()
    )
  )

  // @LINE:14
  private lazy val controllers_FamilyController_updateName8_route = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/families/mine")))
  )
  private lazy val controllers_FamilyController_updateName8_invoker = createInvoker(
    FamilyController_1.updateName,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.FamilyController",
      "updateName",
      Nil,
      "PATCH",
      this.prefix + """api/families/mine""",
      """""",
      Seq()
    )
  )

  // @LINE:15
  private lazy val controllers_FamilyController_inviteCode9_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/families/mine/invite-code")))
  )
  private lazy val controllers_FamilyController_inviteCode9_invoker = createInvoker(
    FamilyController_1.inviteCode,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.FamilyController",
      "inviteCode",
      Nil,
      "GET",
      this.prefix + """api/families/mine/invite-code""",
      """""",
      Seq()
    )
  )

  // @LINE:18
  private lazy val controllers_MemberController_list10_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/members")))
  )
  private lazy val controllers_MemberController_list10_invoker = createInvoker(
    MemberController_2.list,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.MemberController",
      "list",
      Nil,
      "GET",
      this.prefix + """api/members""",
      """ Members""",
      Seq()
    )
  )

  // @LINE:19
  private lazy val controllers_MemberController_create11_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/members")))
  )
  private lazy val controllers_MemberController_create11_invoker = createInvoker(
    MemberController_2.create,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.MemberController",
      "create",
      Nil,
      "POST",
      this.prefix + """api/members""",
      """""",
      Seq()
    )
  )

  // @LINE:20
  private lazy val controllers_MemberController_update12_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/members/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_MemberController_update12_invoker = createInvoker(
    MemberController_2.update(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.MemberController",
      "update",
      Seq(classOf[String]),
      "PUT",
      this.prefix + """api/members/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:21
  private lazy val controllers_MemberController_delete13_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/members/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_MemberController_delete13_invoker = createInvoker(
    MemberController_2.delete(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.MemberController",
      "delete",
      Seq(classOf[String]),
      "DELETE",
      this.prefix + """api/members/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:24
  private lazy val controllers_EventController_list14_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/events")))
  )
  private lazy val controllers_EventController_list14_invoker = createInvoker(
    EventController_3.list(fakeValue[Option[String]], fakeValue[Option[String]], fakeValue[Option[String]]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.EventController",
      "list",
      Seq(classOf[Option[String]], classOf[Option[String]], classOf[Option[String]]),
      "GET",
      this.prefix + """api/events""",
      """ Events""",
      Seq()
    )
  )

  // @LINE:25
  private lazy val controllers_EventController_create15_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/events")))
  )
  private lazy val controllers_EventController_create15_invoker = createInvoker(
    EventController_3.create,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.EventController",
      "create",
      Nil,
      "POST",
      this.prefix + """api/events""",
      """""",
      Seq()
    )
  )

  // @LINE:26
  private lazy val controllers_EventController_get16_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/events/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_EventController_get16_invoker = createInvoker(
    EventController_3.get(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.EventController",
      "get",
      Seq(classOf[String]),
      "GET",
      this.prefix + """api/events/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:27
  private lazy val controllers_EventController_update17_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/events/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_EventController_update17_invoker = createInvoker(
    EventController_3.update(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.EventController",
      "update",
      Seq(classOf[String]),
      "PUT",
      this.prefix + """api/events/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:28
  private lazy val controllers_EventController_delete18_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/events/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_EventController_delete18_invoker = createInvoker(
    EventController_3.delete(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.EventController",
      "delete",
      Seq(classOf[String]),
      "DELETE",
      this.prefix + """api/events/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:31
  private lazy val controllers_TaskController_list19_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/tasks")))
  )
  private lazy val controllers_TaskController_list19_invoker = createInvoker(
    TaskController_8.list(fakeValue[Option[String]], fakeValue[Option[String]], fakeValue[Option[String]]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TaskController",
      "list",
      Seq(classOf[Option[String]], classOf[Option[String]], classOf[Option[String]]),
      "GET",
      this.prefix + """api/tasks""",
      """ Tasks""",
      Seq()
    )
  )

  // @LINE:32
  private lazy val controllers_TaskController_create20_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/tasks")))
  )
  private lazy val controllers_TaskController_create20_invoker = createInvoker(
    TaskController_8.create,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TaskController",
      "create",
      Nil,
      "POST",
      this.prefix + """api/tasks""",
      """""",
      Seq()
    )
  )

  // @LINE:33
  private lazy val controllers_TaskController_get21_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/tasks/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_TaskController_get21_invoker = createInvoker(
    TaskController_8.get(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TaskController",
      "get",
      Seq(classOf[String]),
      "GET",
      this.prefix + """api/tasks/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:34
  private lazy val controllers_TaskController_update22_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/tasks/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_TaskController_update22_invoker = createInvoker(
    TaskController_8.update(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TaskController",
      "update",
      Seq(classOf[String]),
      "PUT",
      this.prefix + """api/tasks/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:35
  private lazy val controllers_TaskController_complete23_route = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/tasks/"), DynamicPart("id", """[^/]+""", encodeable=true), StaticPart("/complete")))
  )
  private lazy val controllers_TaskController_complete23_invoker = createInvoker(
    TaskController_8.complete(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TaskController",
      "complete",
      Seq(classOf[String]),
      "PATCH",
      this.prefix + """api/tasks/""" + "$" + """id<[^/]+>/complete""",
      """""",
      Seq()
    )
  )

  // @LINE:36
  private lazy val controllers_TaskController_delete24_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/tasks/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_TaskController_delete24_invoker = createInvoker(
    TaskController_8.delete(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.TaskController",
      "delete",
      Seq(classOf[String]),
      "DELETE",
      this.prefix + """api/tasks/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:39
  private lazy val controllers_ShoppingController_listLists25_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/shopping/lists")))
  )
  private lazy val controllers_ShoppingController_listLists25_invoker = createInvoker(
    ShoppingController_5.listLists,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ShoppingController",
      "listLists",
      Nil,
      "GET",
      this.prefix + """api/shopping/lists""",
      """ Shopping""",
      Seq()
    )
  )

  // @LINE:40
  private lazy val controllers_ShoppingController_createList26_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/shopping/lists")))
  )
  private lazy val controllers_ShoppingController_createList26_invoker = createInvoker(
    ShoppingController_5.createList,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ShoppingController",
      "createList",
      Nil,
      "POST",
      this.prefix + """api/shopping/lists""",
      """""",
      Seq()
    )
  )

  // @LINE:41
  private lazy val controllers_ShoppingController_deleteList27_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/shopping/lists/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_ShoppingController_deleteList27_invoker = createInvoker(
    ShoppingController_5.deleteList(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ShoppingController",
      "deleteList",
      Seq(classOf[String]),
      "DELETE",
      this.prefix + """api/shopping/lists/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:42
  private lazy val controllers_ShoppingController_listItems28_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/shopping/lists/"), DynamicPart("id", """[^/]+""", encodeable=true), StaticPart("/items")))
  )
  private lazy val controllers_ShoppingController_listItems28_invoker = createInvoker(
    ShoppingController_5.listItems(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ShoppingController",
      "listItems",
      Seq(classOf[String]),
      "GET",
      this.prefix + """api/shopping/lists/""" + "$" + """id<[^/]+>/items""",
      """""",
      Seq()
    )
  )

  // @LINE:43
  private lazy val controllers_ShoppingController_addItem29_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/shopping/lists/"), DynamicPart("id", """[^/]+""", encodeable=true), StaticPart("/items")))
  )
  private lazy val controllers_ShoppingController_addItem29_invoker = createInvoker(
    ShoppingController_5.addItem(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ShoppingController",
      "addItem",
      Seq(classOf[String]),
      "POST",
      this.prefix + """api/shopping/lists/""" + "$" + """id<[^/]+>/items""",
      """""",
      Seq()
    )
  )

  // @LINE:44
  private lazy val controllers_ShoppingController_clearChecked30_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/shopping/lists/"), DynamicPart("id", """[^/]+""", encodeable=true), StaticPart("/items/checked")))
  )
  private lazy val controllers_ShoppingController_clearChecked30_invoker = createInvoker(
    ShoppingController_5.clearChecked(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ShoppingController",
      "clearChecked",
      Seq(classOf[String]),
      "DELETE",
      this.prefix + """api/shopping/lists/""" + "$" + """id<[^/]+>/items/checked""",
      """""",
      Seq()
    )
  )

  // @LINE:45
  private lazy val controllers_ShoppingController_checkItem31_route = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/shopping/lists/"), DynamicPart("listId", """[^/]+""", encodeable=true), StaticPart("/items/"), DynamicPart("itemId", """[^/]+""", encodeable=true), StaticPart("/check")))
  )
  private lazy val controllers_ShoppingController_checkItem31_invoker = createInvoker(
    ShoppingController_5.checkItem(fakeValue[String], fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ShoppingController",
      "checkItem",
      Seq(classOf[String], classOf[String]),
      "PATCH",
      this.prefix + """api/shopping/lists/""" + "$" + """listId<[^/]+>/items/""" + "$" + """itemId<[^/]+>/check""",
      """""",
      Seq()
    )
  )

  // @LINE:46
  private lazy val controllers_ShoppingController_deleteItem32_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/shopping/lists/"), DynamicPart("listId", """[^/]+""", encodeable=true), StaticPart("/items/"), DynamicPart("itemId", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_ShoppingController_deleteItem32_invoker = createInvoker(
    ShoppingController_5.deleteItem(fakeValue[String], fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.ShoppingController",
      "deleteItem",
      Seq(classOf[String], classOf[String]),
      "DELETE",
      this.prefix + """api/shopping/lists/""" + "$" + """listId<[^/]+>/items/""" + "$" + """itemId<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:49
  private lazy val controllers_RewardController_list33_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/rewards")))
  )
  private lazy val controllers_RewardController_list33_invoker = createInvoker(
    RewardController_7.list(fakeValue[Option[String]], fakeValue[Option[String]]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RewardController",
      "list",
      Seq(classOf[Option[String]], classOf[Option[String]]),
      "GET",
      this.prefix + """api/rewards""",
      """ Rewards""",
      Seq()
    )
  )

  // @LINE:50
  private lazy val controllers_RewardController_create34_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/rewards")))
  )
  private lazy val controllers_RewardController_create34_invoker = createInvoker(
    RewardController_7.create,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RewardController",
      "create",
      Nil,
      "POST",
      this.prefix + """api/rewards""",
      """""",
      Seq()
    )
  )

  // @LINE:51
  private lazy val controllers_RewardController_request35_route = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/rewards/"), DynamicPart("id", """[^/]+""", encodeable=true), StaticPart("/request")))
  )
  private lazy val controllers_RewardController_request35_invoker = createInvoker(
    RewardController_7.request(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RewardController",
      "request",
      Seq(classOf[String]),
      "PATCH",
      this.prefix + """api/rewards/""" + "$" + """id<[^/]+>/request""",
      """""",
      Seq()
    )
  )

  // @LINE:52
  private lazy val controllers_RewardController_approve36_route = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/rewards/"), DynamicPart("id", """[^/]+""", encodeable=true), StaticPart("/approve")))
  )
  private lazy val controllers_RewardController_approve36_invoker = createInvoker(
    RewardController_7.approve(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RewardController",
      "approve",
      Seq(classOf[String]),
      "PATCH",
      this.prefix + """api/rewards/""" + "$" + """id<[^/]+>/approve""",
      """""",
      Seq()
    )
  )

  // @LINE:53
  private lazy val controllers_RewardController_redeem37_route = Route("PATCH",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/rewards/"), DynamicPart("id", """[^/]+""", encodeable=true), StaticPart("/redeem")))
  )
  private lazy val controllers_RewardController_redeem37_invoker = createInvoker(
    RewardController_7.redeem(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RewardController",
      "redeem",
      Seq(classOf[String]),
      "PATCH",
      this.prefix + """api/rewards/""" + "$" + """id<[^/]+>/redeem""",
      """""",
      Seq()
    )
  )

  // @LINE:54
  private lazy val controllers_RewardController_delete38_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/rewards/"), DynamicPart("id", """[^/]+""", encodeable=true)))
  )
  private lazy val controllers_RewardController_delete38_invoker = createInvoker(
    RewardController_7.delete(fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RewardController",
      "delete",
      Seq(classOf[String]),
      "DELETE",
      this.prefix + """api/rewards/""" + "$" + """id<[^/]+>""",
      """""",
      Seq()
    )
  )

  // @LINE:57
  private lazy val controllers_DashboardController_get39_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("api/dashboard")))
  )
  private lazy val controllers_DashboardController_get39_invoker = createInvoker(
    DashboardController_0.get,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DashboardController",
      "get",
      Nil,
      "GET",
      this.prefix + """api/dashboard""",
      """ Dashboard""",
      Seq()
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:2
    case controllers_HealthController_check0_route(params@_) =>
      call { 
        controllers_HealthController_check0_invoker.call(HealthController_6.check)
      }
  
    // @LINE:5
    case controllers_AuthController_register1_route(params@_) =>
      call { 
        controllers_AuthController_register1_invoker.call(AuthController_4.register)
      }
  
    // @LINE:6
    case controllers_AuthController_login2_route(params@_) =>
      call { 
        controllers_AuthController_login2_invoker.call(AuthController_4.login)
      }
  
    // @LINE:7
    case controllers_AuthController_me3_route(params@_) =>
      call { 
        controllers_AuthController_me3_invoker.call(AuthController_4.me)
      }
  
    // @LINE:8
    case controllers_AuthController_updateProfile4_route(params@_) =>
      call { 
        controllers_AuthController_updateProfile4_invoker.call(AuthController_4.updateProfile)
      }
  
    // @LINE:11
    case controllers_FamilyController_create5_route(params@_) =>
      call { 
        controllers_FamilyController_create5_invoker.call(FamilyController_1.create)
      }
  
    // @LINE:12
    case controllers_FamilyController_join6_route(params@_) =>
      call { 
        controllers_FamilyController_join6_invoker.call(FamilyController_1.join)
      }
  
    // @LINE:13
    case controllers_FamilyController_mine7_route(params@_) =>
      call { 
        controllers_FamilyController_mine7_invoker.call(FamilyController_1.mine)
      }
  
    // @LINE:14
    case controllers_FamilyController_updateName8_route(params@_) =>
      call { 
        controllers_FamilyController_updateName8_invoker.call(FamilyController_1.updateName)
      }
  
    // @LINE:15
    case controllers_FamilyController_inviteCode9_route(params@_) =>
      call { 
        controllers_FamilyController_inviteCode9_invoker.call(FamilyController_1.inviteCode)
      }
  
    // @LINE:18
    case controllers_MemberController_list10_route(params@_) =>
      call { 
        controllers_MemberController_list10_invoker.call(MemberController_2.list)
      }
  
    // @LINE:19
    case controllers_MemberController_create11_route(params@_) =>
      call { 
        controllers_MemberController_create11_invoker.call(MemberController_2.create)
      }
  
    // @LINE:20
    case controllers_MemberController_update12_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_MemberController_update12_invoker.call(MemberController_2.update(id))
      }
  
    // @LINE:21
    case controllers_MemberController_delete13_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_MemberController_delete13_invoker.call(MemberController_2.delete(id))
      }
  
    // @LINE:24
    case controllers_EventController_list14_route(params@_) =>
      call(params.fromQuery[Option[String]]("from", Some(None)), params.fromQuery[Option[String]]("to", Some(None)), params.fromQuery[Option[String]]("memberId", Some(None))) { (from, to, memberId) =>
        controllers_EventController_list14_invoker.call(EventController_3.list(from, to, memberId))
      }
  
    // @LINE:25
    case controllers_EventController_create15_route(params@_) =>
      call { 
        controllers_EventController_create15_invoker.call(EventController_3.create)
      }
  
    // @LINE:26
    case controllers_EventController_get16_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_EventController_get16_invoker.call(EventController_3.get(id))
      }
  
    // @LINE:27
    case controllers_EventController_update17_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_EventController_update17_invoker.call(EventController_3.update(id))
      }
  
    // @LINE:28
    case controllers_EventController_delete18_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_EventController_delete18_invoker.call(EventController_3.delete(id))
      }
  
    // @LINE:31
    case controllers_TaskController_list19_route(params@_) =>
      call(params.fromQuery[Option[String]]("assignedTo", Some(None)), params.fromQuery[Option[String]]("status", Some(None)), params.fromQuery[Option[String]]("priority", Some(None))) { (assignedTo, status, priority) =>
        controllers_TaskController_list19_invoker.call(TaskController_8.list(assignedTo, status, priority))
      }
  
    // @LINE:32
    case controllers_TaskController_create20_route(params@_) =>
      call { 
        controllers_TaskController_create20_invoker.call(TaskController_8.create)
      }
  
    // @LINE:33
    case controllers_TaskController_get21_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_TaskController_get21_invoker.call(TaskController_8.get(id))
      }
  
    // @LINE:34
    case controllers_TaskController_update22_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_TaskController_update22_invoker.call(TaskController_8.update(id))
      }
  
    // @LINE:35
    case controllers_TaskController_complete23_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_TaskController_complete23_invoker.call(TaskController_8.complete(id))
      }
  
    // @LINE:36
    case controllers_TaskController_delete24_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_TaskController_delete24_invoker.call(TaskController_8.delete(id))
      }
  
    // @LINE:39
    case controllers_ShoppingController_listLists25_route(params@_) =>
      call { 
        controllers_ShoppingController_listLists25_invoker.call(ShoppingController_5.listLists)
      }
  
    // @LINE:40
    case controllers_ShoppingController_createList26_route(params@_) =>
      call { 
        controllers_ShoppingController_createList26_invoker.call(ShoppingController_5.createList)
      }
  
    // @LINE:41
    case controllers_ShoppingController_deleteList27_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ShoppingController_deleteList27_invoker.call(ShoppingController_5.deleteList(id))
      }
  
    // @LINE:42
    case controllers_ShoppingController_listItems28_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ShoppingController_listItems28_invoker.call(ShoppingController_5.listItems(id))
      }
  
    // @LINE:43
    case controllers_ShoppingController_addItem29_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ShoppingController_addItem29_invoker.call(ShoppingController_5.addItem(id))
      }
  
    // @LINE:44
    case controllers_ShoppingController_clearChecked30_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_ShoppingController_clearChecked30_invoker.call(ShoppingController_5.clearChecked(id))
      }
  
    // @LINE:45
    case controllers_ShoppingController_checkItem31_route(params@_) =>
      call(params.fromPath[String]("listId", None), params.fromPath[String]("itemId", None)) { (listId, itemId) =>
        controllers_ShoppingController_checkItem31_invoker.call(ShoppingController_5.checkItem(listId, itemId))
      }
  
    // @LINE:46
    case controllers_ShoppingController_deleteItem32_route(params@_) =>
      call(params.fromPath[String]("listId", None), params.fromPath[String]("itemId", None)) { (listId, itemId) =>
        controllers_ShoppingController_deleteItem32_invoker.call(ShoppingController_5.deleteItem(listId, itemId))
      }
  
    // @LINE:49
    case controllers_RewardController_list33_route(params@_) =>
      call(params.fromQuery[Option[String]]("memberId", Some(None)), params.fromQuery[Option[String]]("status", Some(None))) { (memberId, status) =>
        controllers_RewardController_list33_invoker.call(RewardController_7.list(memberId, status))
      }
  
    // @LINE:50
    case controllers_RewardController_create34_route(params@_) =>
      call { 
        controllers_RewardController_create34_invoker.call(RewardController_7.create)
      }
  
    // @LINE:51
    case controllers_RewardController_request35_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_RewardController_request35_invoker.call(RewardController_7.request(id))
      }
  
    // @LINE:52
    case controllers_RewardController_approve36_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_RewardController_approve36_invoker.call(RewardController_7.approve(id))
      }
  
    // @LINE:53
    case controllers_RewardController_redeem37_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_RewardController_redeem37_invoker.call(RewardController_7.redeem(id))
      }
  
    // @LINE:54
    case controllers_RewardController_delete38_route(params@_) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_RewardController_delete38_invoker.call(RewardController_7.delete(id))
      }
  
    // @LINE:57
    case controllers_DashboardController_get39_route(params@_) =>
      call { 
        controllers_DashboardController_get39_invoker.call(DashboardController_0.get)
      }
  }
}
