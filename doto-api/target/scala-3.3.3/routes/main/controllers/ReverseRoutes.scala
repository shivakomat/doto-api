// @GENERATOR:play-routes-compiler
// @SOURCE:conf/routes

import play.api.mvc.Call


import _root_.controllers.Assets.Asset

// @LINE:2
package controllers {

  // @LINE:24
  class ReverseEventController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:25
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/events")
    }
  
    // @LINE:24
    def list(from:Option[String] = None, to:Option[String] = None, memberId:Option[String] = None): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/events" + play.core.routing.queryString(List(if(from == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("from", from)), if(to == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("to", to)), if(memberId == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("memberId", memberId)))))
    }
  
    // @LINE:28
    def delete(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/events/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:27
    def update(id:String): Call = {
      
      Call("PUT", _prefix + { _defaultPrefix } + "api/events/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:26
    def get(id:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/events/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
  }

  // @LINE:57
  class ReverseDashboardController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:57
    def get: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/dashboard")
    }
  
  }

  // @LINE:18
  class ReverseMemberController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:18
    def list: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/members")
    }
  
    // @LINE:19
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/members")
    }
  
    // @LINE:20
    def update(id:String): Call = {
      
      Call("PUT", _prefix + { _defaultPrefix } + "api/members/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:21
    def delete(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/members/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
  }

  // @LINE:39
  class ReverseShoppingController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:41
    def deleteList(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:43
    def addItem(id:String): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/items")
    }
  
    // @LINE:39
    def listLists: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/shopping/lists")
    }
  
    // @LINE:45
    def checkItem(listId:String, itemId:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("listId", listId)) + "/items/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("itemId", itemId)) + "/check")
    }
  
    // @LINE:44
    def clearChecked(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/items/checked")
    }
  
    // @LINE:40
    def createList: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/shopping/lists")
    }
  
    // @LINE:46
    def deleteItem(listId:String, itemId:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("listId", listId)) + "/items/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("itemId", itemId)))
    }
  
    // @LINE:42
    def listItems(id:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/shopping/lists/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/items")
    }
  
  }

  // @LINE:5
  class ReverseAuthController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:5
    def register: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/auth/register")
    }
  
    // @LINE:6
    def login: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/auth/login")
    }
  
    // @LINE:7
    def me: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/auth/me")
    }
  
    // @LINE:8
    def updateProfile: Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/auth/profile")
    }
  
  }

  // @LINE:49
  class ReverseRewardController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:49
    def list(memberId:Option[String] = None, status:Option[String] = None): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/rewards" + play.core.routing.queryString(List(if(memberId == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("memberId", memberId)), if(status == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("status", status)))))
    }
  
    // @LINE:50
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/rewards")
    }
  
    // @LINE:52
    def approve(id:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/rewards/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/approve")
    }
  
    // @LINE:53
    def redeem(id:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/rewards/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/redeem")
    }
  
    // @LINE:51
    def request(id:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/rewards/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/request")
    }
  
    // @LINE:54
    def delete(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/rewards/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
  }

  // @LINE:11
  class ReverseFamilyController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:11
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/families")
    }
  
    // @LINE:13
    def mine: Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/families/mine")
    }
  
    // @LINE:14
    def updateName: Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/families/mine")
    }
  
    // @LINE:12
    def join: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/families/join")
    }
  
    // @LINE:15
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

  // @LINE:31
  class ReverseTaskController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:32
    def create: Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "api/tasks")
    }
  
    // @LINE:31
    def list(assignedTo:Option[String] = None, status:Option[String] = None, priority:Option[String] = None): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/tasks" + play.core.routing.queryString(List(if(assignedTo == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("assignedTo", assignedTo)), if(status == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("status", status)), if(priority == None) None else Some(implicitly[play.api.mvc.QueryStringBindable[Option[String]]].unbind("priority", priority)))))
    }
  
    // @LINE:36
    def delete(id:String): Call = {
      
      Call("DELETE", _prefix + { _defaultPrefix } + "api/tasks/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:34
    def update(id:String): Call = {
      
      Call("PUT", _prefix + { _defaultPrefix } + "api/tasks/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
    // @LINE:35
    def complete(id:String): Call = {
      
      Call("PATCH", _prefix + { _defaultPrefix } + "api/tasks/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)) + "/complete")
    }
  
    // @LINE:33
    def get(id:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "api/tasks/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("id", id)))
    }
  
  }


}
