package controllers

import play.api.mvc.*
import javax.inject.{Inject, Singleton}

@Singleton
class HealthController @Inject()(cc: ControllerComponents) extends AbstractController(cc):

  def check: Action[AnyContent] = Action {
    Ok("""{"status":"ok"}""").as("application/json")
  }
