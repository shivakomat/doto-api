// @GENERATOR:play-routes-compiler
// @SOURCE:conf/routes

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseEventController EventController = new controllers.ReverseEventController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseDashboardController DashboardController = new controllers.ReverseDashboardController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseMemberController MemberController = new controllers.ReverseMemberController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseShoppingController ShoppingController = new controllers.ReverseShoppingController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseAuthController AuthController = new controllers.ReverseAuthController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseRewardController RewardController = new controllers.ReverseRewardController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseFamilyController FamilyController = new controllers.ReverseFamilyController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseHealthController HealthController = new controllers.ReverseHealthController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseTaskController TaskController = new controllers.ReverseTaskController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseEventController EventController = new controllers.javascript.ReverseEventController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseDashboardController DashboardController = new controllers.javascript.ReverseDashboardController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseMemberController MemberController = new controllers.javascript.ReverseMemberController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseShoppingController ShoppingController = new controllers.javascript.ReverseShoppingController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseAuthController AuthController = new controllers.javascript.ReverseAuthController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseRewardController RewardController = new controllers.javascript.ReverseRewardController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseFamilyController FamilyController = new controllers.javascript.ReverseFamilyController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseHealthController HealthController = new controllers.javascript.ReverseHealthController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseTaskController TaskController = new controllers.javascript.ReverseTaskController(RoutesPrefix.byNamePrefix());
  }

}
