package controllers

import play.api.test.Helpers.*

class TaskControllerSpec extends BaseSpec:

  private val uniq    = System.nanoTime().toString.takeRight(5)
  private var token   = ""
  private var taskId  = ""
  private var childId = ""

  override def beforeAll(): Unit =
    super.beforeAll()
    token = registerUser(s"tsk_p_$uniq")
    createFamily(token, s"Task Family $uniq")
    token = loginUser(s"tsk_p_$uniq")
    val childResult = makePost("/api/members",
      """{"displayName":"Task Child","color":"#AABBCC"}""", Some(token))
    childId = field(parseBody(childResult), "id")

  "POST /api/tasks" should {

    "return 201 creating a task with default priority and points" in {
      val result = makePost("/api/tasks",
        s"""{"title":"Buy milk","assignedTo":"$childId","points":10}""",
        Some(token))
      status(result) mustBe CREATED
      val json = parseBody(result)
      taskId = field(json, "id")
      taskId must not be empty
      field(json, "title") mustBe "Buy milk"
      field(json, "status") mustBe "todo"
      field(json, "priority") mustBe "medium"
      json.hcursor.downField("points").as[Int].getOrElse(-1) mustBe 10
    }

    "return 201 creating a high-priority task with due date" in {
      val result = makePost("/api/tasks",
        s"""{"title":"Urgent task","priority":"high","points":25,"dueAt":"2030-12-01T09:00:00Z"}""",
        Some(token))
      status(result) mustBe CREATED
      val json = parseBody(result)
      field(json, "priority") mustBe "high"
      json.hcursor.downField("dueAt").as[Option[String]].getOrElse(None) must not be empty
    }

    "return 400 when title is empty" in {
      val result = makePost("/api/tasks", """{"title":""}""", Some(token))
      status(result) mustBe BAD_REQUEST
    }

    "return 400 when points is negative" in {
      val result = makePost("/api/tasks",
        """{"title":"Bad task","points":-5}""", Some(token))
      status(result) mustBe BAD_REQUEST
    }

    "return 401 with no token" in {
      val result = makePost("/api/tasks", """{"title":"Anon"}""")
      status(result) mustBe UNAUTHORIZED
    }
  }

  "GET /api/tasks" should {

    "return 200 with list of family tasks" in {
      val result = makeGet("/api/tasks", Some(token))
      status(result) mustBe OK
      parseBody(result).as[List[io.circe.Json]].getOrElse(Nil) must not be empty
    }

    "return 200 filtered by status=todo" in {
      val result = makeGet("/api/tasks?status=todo", Some(token))
      status(result) mustBe OK
      val tasks = parseBody(result).as[List[io.circe.Json]].getOrElse(Nil)
      tasks.foreach(t => t.hcursor.downField("status").as[String].getOrElse("") mustBe "todo")
    }

    "return 200 filtered by assignedTo" in {
      val result = makeGet(s"/api/tasks?assignedTo=$childId", Some(token))
      status(result) mustBe OK
      val tasks = parseBody(result).as[List[io.circe.Json]].getOrElse(Nil)
      tasks.foreach(t => t.hcursor.downField("assignedTo").as[Option[String]].getOrElse(None) mustBe Some(childId))
    }

    "return 200 filtered by priority=high" in {
      val result = makeGet("/api/tasks?priority=high", Some(token))
      status(result) mustBe OK
      val tasks = parseBody(result).as[List[io.circe.Json]].getOrElse(Nil)
      tasks.foreach(t => t.hcursor.downField("priority").as[String].getOrElse("") mustBe "high")
    }
  }

  "GET /api/tasks/:id" should {

    "return 200 with the task details" in {
      val result = makeGet(s"/api/tasks/$taskId", Some(token))
      status(result) mustBe OK
      field(parseBody(result), "id") mustBe taskId
    }

    "return 404 for a non-existent task id" in {
      val result = makeGet("/api/tasks/00000000-0000-0000-0000-000000000000", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }

  "PUT /api/tasks/:id" should {

    "return 200 updating a task's title and status" in {
      val result = makePut(s"/api/tasks/$taskId",
        """{"title":"Buy full-fat milk","status":"in_progress"}""", Some(token))
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "title") mustBe "Buy full-fat milk"
      field(json, "status") mustBe "in_progress"
    }

    "return 404 for a non-existent task" in {
      val result = makePut("/api/tasks/00000000-0000-0000-0000-000000000000",
        """{"title":"Ghost"}""", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }

  "PATCH /api/tasks/:id/complete" should {

    "return 200 marking the task as done and awarding points to the assignee" in {
      val beforePoints = {
        val r = makeGet("/api/members", Some(token))
        parseBody(r).as[List[io.circe.Json]].getOrElse(Nil)
          .find(m => field(m, "id") == childId)
          .flatMap(_.hcursor.downField("points").as[Int].toOption)
          .getOrElse(0)
      }
      val result = makePatch(s"/api/tasks/$taskId/complete", token = Some(token))
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "status") mustBe "done"
      json.hcursor.downField("completedAt").as[Option[String]].getOrElse(None) must not be empty
      val afterPoints = {
        val r = makeGet("/api/members", Some(token))
        parseBody(r).as[List[io.circe.Json]].getOrElse(Nil)
          .find(m => field(m, "id") == childId)
          .flatMap(_.hcursor.downField("points").as[Int].toOption)
          .getOrElse(0)
      }
      afterPoints mustBe (beforePoints + 10)
    }
  }

  "DELETE /api/tasks/:id" should {

    "return 204 on successful deletion" in {
      val newTask = makePost("/api/tasks", """{"title":"Delete me"}""", Some(token))
      val tid     = field(parseBody(newTask), "id")
      val result  = makeDelete(s"/api/tasks/$tid", Some(token))
      status(result) mustBe NO_CONTENT
    }

    "return 404 for an already-deleted task" in {
      val result = makeDelete(s"/api/tasks/00000000-0000-0000-0000-000000000000", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }
