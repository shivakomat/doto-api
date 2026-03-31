// @GENERATOR:play-routes-compiler
// @SOURCE:conf/routes

import play.api.routing.JavaScriptReverseRoute


import _root_.controllers.Assets.Asset

// @LINE:2
package controllers.javascript {

  // @LINE:26
  class ReverseEventController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:27
    def create: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.EventController.create",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/events"})
        }
      """
    )
  
    // @LINE:26
    def list: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.EventController.list",
      """
        function(from0,to1,memberId2) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/events" + _qS([(from0 == null ? null : (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("from", from0)), (to1 == null ? null : (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("to", to1)), (memberId2 == null ? null : (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("memberId", memberId2))])})
        }
      """
    )
  
    // @LINE:30
    def delete: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.EventController.delete",
      """
        function(id0) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "api/events/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
    // @LINE:29
    def update: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.EventController.update",
      """
        function(id0) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "api/events/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
    // @LINE:28
    def get: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.EventController.get",
      """
        function(id0) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/events/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
  }

  // @LINE:59
  class ReverseDashboardController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:59
    def get: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DashboardController.get",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/dashboard"})
        }
      """
    )
  
  }

  // @LINE:20
  class ReverseMemberController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:20
    def list: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.MemberController.list",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/members"})
        }
      """
    )
  
    // @LINE:21
    def create: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.MemberController.create",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/members"})
        }
      """
    )
  
    // @LINE:22
    def update: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.MemberController.update",
      """
        function(id0) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "api/members/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
    // @LINE:23
    def delete: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.MemberController.delete",
      """
        function(id0) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "api/members/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
  }

  // @LINE:41
  class ReverseShoppingController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:43
    def deleteList: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ShoppingController.deleteList",
      """
        function(id0) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "api/shopping/lists/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
    // @LINE:45
    def addItem: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ShoppingController.addItem",
      """
        function(id0) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/shopping/lists/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0)) + "/items"})
        }
      """
    )
  
    // @LINE:41
    def listLists: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ShoppingController.listLists",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/shopping/lists"})
        }
      """
    )
  
    // @LINE:47
    def checkItem: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ShoppingController.checkItem",
      """
        function(listId0,itemId1) {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "api/shopping/lists/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("listId", listId0)) + "/items/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("itemId", itemId1)) + "/check"})
        }
      """
    )
  
    // @LINE:46
    def clearChecked: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ShoppingController.clearChecked",
      """
        function(id0) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "api/shopping/lists/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0)) + "/items/checked"})
        }
      """
    )
  
    // @LINE:42
    def createList: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ShoppingController.createList",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/shopping/lists"})
        }
      """
    )
  
    // @LINE:48
    def deleteItem: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ShoppingController.deleteItem",
      """
        function(listId0,itemId1) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "api/shopping/lists/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("listId", listId0)) + "/items/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("itemId", itemId1))})
        }
      """
    )
  
    // @LINE:44
    def listItems: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.ShoppingController.listItems",
      """
        function(id0) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/shopping/lists/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0)) + "/items"})
        }
      """
    )
  
  }

  // @LINE:5
  class ReverseAuthController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:7
    def me: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.AuthController.me",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/auth/me"})
        }
      """
    )
  
    // @LINE:5
    def register: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.AuthController.register",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/auth/register"})
        }
      """
    )
  
    // @LINE:9
    def changePassword: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.AuthController.changePassword",
      """
        function() {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "api/auth/change-password"})
        }
      """
    )
  
    // @LINE:8
    def updateProfile: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.AuthController.updateProfile",
      """
        function() {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "api/auth/profile"})
        }
      """
    )
  
    // @LINE:6
    def login: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.AuthController.login",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/auth/login"})
        }
      """
    )
  
  }

  // @LINE:51
  class ReverseRewardController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:51
    def list: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RewardController.list",
      """
        function(memberId0,status1) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/rewards" + _qS([(memberId0 == null ? null : (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("memberId", memberId0)), (status1 == null ? null : (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("status", status1))])})
        }
      """
    )
  
    // @LINE:52
    def create: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RewardController.create",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/rewards"})
        }
      """
    )
  
    // @LINE:54
    def approve: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RewardController.approve",
      """
        function(id0) {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "api/rewards/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0)) + "/approve"})
        }
      """
    )
  
    // @LINE:55
    def redeem: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RewardController.redeem",
      """
        function(id0) {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "api/rewards/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0)) + "/redeem"})
        }
      """
    )
  
    // @LINE:53
    def request: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RewardController.request",
      """
        function(id0) {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "api/rewards/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0)) + "/request"})
        }
      """
    )
  
    // @LINE:56
    def delete: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RewardController.delete",
      """
        function(id0) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "api/rewards/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
  }

  // @LINE:12
  class ReverseFamilyController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:13
    def create: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.FamilyController.create",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/families"})
        }
      """
    )
  
    // @LINE:15
    def mine: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.FamilyController.mine",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/families/mine"})
        }
      """
    )
  
    // @LINE:12
    def preview: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.FamilyController.preview",
      """
        function(code0) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/families/preview/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("code", code0))})
        }
      """
    )
  
    // @LINE:16
    def updateName: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.FamilyController.updateName",
      """
        function() {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "api/families/mine"})
        }
      """
    )
  
    // @LINE:14
    def join: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.FamilyController.join",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/families/join"})
        }
      """
    )
  
    // @LINE:17
    def inviteCode: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.FamilyController.inviteCode",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/families/mine/invite-code"})
        }
      """
    )
  
  }

  // @LINE:2
  class ReverseHealthController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:2
    def check: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HealthController.check",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/health"})
        }
      """
    )
  
  }

  // @LINE:33
  class ReverseTaskController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:34
    def create: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TaskController.create",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "api/tasks"})
        }
      """
    )
  
    // @LINE:33
    def list: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TaskController.list",
      """
        function(assignedTo0,status1,priority2) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/tasks" + _qS([(assignedTo0 == null ? null : (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("assignedTo", assignedTo0)), (status1 == null ? null : (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("status", status1)), (priority2 == null ? null : (""" + implicitly[play.api.mvc.QueryStringBindable[Option[String]]].javascriptUnbind + """)("priority", priority2))])})
        }
      """
    )
  
    // @LINE:38
    def delete: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TaskController.delete",
      """
        function(id0) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "api/tasks/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
    // @LINE:36
    def update: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TaskController.update",
      """
        function(id0) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "api/tasks/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
    // @LINE:37
    def complete: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TaskController.complete",
      """
        function(id0) {
          return _wA({method:"PATCH", url:"""" + _prefix + { _defaultPrefix } + """" + "api/tasks/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0)) + "/complete"})
        }
      """
    )
  
    // @LINE:35
    def get: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.TaskController.get",
      """
        function(id0) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "api/tasks/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("id", id0))})
        }
      """
    )
  
  }


}
