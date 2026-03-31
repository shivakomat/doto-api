// @GENERATOR:play-routes-compiler
// @SOURCE:conf/routes

import play.api.mvc.Call


import _root_.controllers.Assets.Asset

// @LINE:2
package controllers {

  // @LINE:28
  class ReverseEventController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:29
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/events")
    }
  
    // @LINE:28
    def list(from:Option[String] = None, to:Option[String] = None, memberId:Option[String] = None): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/events" + play.core.routing.queryString(List(if(from == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("from", from)), if(to == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("to", to)), if(memberId == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("memberId", memberId)))))
    }
  
    // @LINE:32
    def delete(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/events/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:31
    def update(id:String): Call = {
      
      Call("PUT", _prefix + { _defaultPrefix } + "api/events/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:30
    def get(id:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/events/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
  }

  // @LINE:61
  class ReverseDashboardController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:61
    def get: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/dashboard")
    }
  
  }

  // @LINE:21
  class ReverseMemberController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:22
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/members")
    }
  
    // @LINE:21
    def list: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/members")
    }
  
    // @LINE:25
    def delete(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/members/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:23
    def claimStatus(id:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/members/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/claim-status")
    }
  
    // @LINE:24
    def update(id:String): Call = {
      
      Call("PUT", _prefix + { _defaultPrefix } + "api/members/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
  }

  // @LINE:43
  class ReverseShoppingController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:45
    def deleteList(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:47
    def addItem(id:String): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/items")
    }
  
    // @LINE:43
    def listLists: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/shopping/lists")
    }
  
    // @LINE:49
    def checkItem(listId:String, itemId:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("listId", listId)) + "/items/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("itemId", itemId)) + "/check")
    }
  
    // @LINE:48
    def clearChecked(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/items/checked")
    }
  
    // @LINE:44
    def createList: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/shopping/lists")
    }
  
    // @LINE:50
    def deleteItem(listId:String, itemId:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("listId", listId)) + "/items/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("itemId", itemId)))
    }
  
    // @LINE:46
    def listItems(id:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/items")
    }
  
  }

  // @LINE:5
  class ReverseAuthController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:7
    def claimProfile: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/auth/claim-profile")
    }
  
    // @LINE:8
    def me: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/auth/me")
    }
  
    // @LINE:5
    def register: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/auth/register")
    }
  
    // @LINE:10
    def changePassword: Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/auth/change-password")
    }
  
    // @LINE:9
    def updateProfile: Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/auth/profile")
    }
  
    // @LINE:6
    def login: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/auth/login")
    }
  
  }

  // @LINE:53
  class ReverseRewardController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:53
    def list(memberId:Option[String] = None, status:Option[String] = None): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/rewards" + play.core.routing.queryString(List(if(memberId == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("memberId", memberId)), if(status == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("status", status)))))
    }
  
    // @LINE:54
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/rewards")
    }
  
    // @LINE:56
    def approve(id:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/rewards/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/approve")
    }
  
    // @LINE:57
    def redeem(id:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/rewards/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/redeem")
    }
  
    // @LINE:55
    def request(id:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/rewards/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/request")
    }
  
    // @LINE:58
    def delete(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/rewards/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
  }

  // @LINE:13
  class ReverseFamilyController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:14
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/families")
    }
  
    // @LINE:16
    def mine: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/families/mine")
    }
  
    // @LINE:13
    def preview(code:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/families/preview/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("code", code)))
    }
  
    // @LINE:17
    def updateName: Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/families/mine")
    }
  
    // @LINE:15
    def join: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/families/join")
    }
  
    // @LINE:18
    def inviteCode: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/families/mine/invite-code")
    }
  
  }

  // @LINE:2
  class ReverseHealthController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:2
    def check: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/health")
    }
  
  }

  // @LINE:35
  class ReverseTaskController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:36
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/tasks")
    }
  
    // @LINE:35
    def list(assignedTo:Option[String] = None, status:Option[String] = None, priority:Option[String] = None): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/tasks" + play.core.routing.queryString(List(if(assignedTo == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("assignedTo", assignedTo)), if(status == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("status", status)), if(priority == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("priority", priority)))))
    }
  
    // @LINE:40
    def delete(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/tasks/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:38
    def update(id:String): Call = {
      
      Call("PUT", _prefix + { _defaultPrefix } + "api/tasks/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:39
    def complete(id:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/tasks/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/complete")
    }
  
    // @LINE:37
    def get(id:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/tasks/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
  }


}
