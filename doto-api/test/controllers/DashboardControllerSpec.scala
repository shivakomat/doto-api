package controllers

import play.api.test.Helpers.*

class DashboardControllerSpec extends BaseSpec:

  private val uniq  = System.nanoTime().toString.takeRight(5)
  private var token = ""

  override def beforeAll(): Unit =
    super.beforeAll()
    token = registerUser(s"dsh_p_$uniq")
    createFamily(token, s"Dashboard Family $uniq")
    token = loginUser(s"dsh_p_$uniq")
    makePost("/api/events",
      """{"title":"Today Meeting","startAt":"2030-06-01T09:00:00Z","endAt":"2030-06-01T10:00:00Z"}""",
      Some(token))
    makePost("/api/tasks", """{"title":"Dashboard Task","points":5}""", Some(token))

  "GET /api/dashboard" should {

    "return 200 with the full dashboard payload" in {
      val result = makeGet("/api/dashboard", Some(token))
      status(result) mustBe OK
      val json = parseBody(result)
      json.hcursor.downField("family").downField("id").as[String].getOrElse("") must not be empty
      json.hcursor.downField("family").downField("members").as[List[io.circe.Json]].getOrElse(Nil) must not be empty
      json.hcursor.downField("upcomingEvents").as[List[io.circe.Json]] mustBe Right(Nil)
      json.hcursor.downField("pendingTasksCount").as[Int].toOption must not be empty
      json.hcursor.downField("pendingTasks").as[List[io.circe.Json]].getOrElse(Nil) must not be empty
      json.hcursor.downField("pendingApprovals").as[List[io.circe.Json]] mustBe Right(Nil)
    }

    "return 200 with an empty pendingTasks list after all tasks are completed" in {
      val tasksResult = makeGet("/api/tasks?status=todo", Some(token))
      val tasks       = parseBody(tasksResult).as[List[io.circe.Json]].getOrElse(Nil)
      tasks.foreach { t =>
        val tid = t.hcursor.downField("id").as[String].getOrElse("")
        status(makePatch(s"/api/tasks/$tid/complete", token = Some(token)))
      }
      val result   = makeGet("/api/dashboard", Some(token))
      status(result) mustBe OK
      val json     = parseBody(result)
      json.hcursor.downField("pendingTasks").as[List[io.circe.Json]].getOrElse(List("x")) mustBe Nil
    }

    "return 401 when no token is provided" in {
      val result = makeGet("/api/dashboard")
      status(result) mustBe UNAUTHORIZED
    }

    "return 404 when the user has no family" in {
      val loneToken = registerUser(s"dsh_l_$uniq")
      val result    = makeGet("/api/dashboard", Some(loneToken))
      status(result) mustBe NOT_FOUND
    }
  }
