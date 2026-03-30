package controllers

import play.api.test.Helpers.*

class EventControllerSpec extends BaseSpec:

  private val uniq    = System.nanoTime().toString.takeRight(5)
  private var token   = ""
  private var eventId = ""

  override def beforeAll(): Unit =
    super.beforeAll()
    token = registerUser(s"evt_p_$uniq")
    createFamily(token, s"Event Family $uniq")
    token = loginUser(s"evt_p_$uniq")

  private val startAt = "2030-06-01T10:00:00Z"
  private val endAt   = "2030-06-01T12:00:00Z"

  "POST /api/events" should {

    "return 201 creating an event with valid data" in {
      val result = makePost("/api/events",
        s"""{"title":"School Run","startAt":"$startAt","endAt":"$endAt","description":"Morning drop-off"}""",
        Some(token))
      status(result) mustBe CREATED
      val json = parseBody(result)
      eventId = field(json, "id")
      eventId must not be empty
      field(json, "title") mustBe "School Run"
      json.hcursor.downField("assignedTo").as[List[String]].getOrElse(Nil) mustBe Nil
    }

    "return 400 when endAt is before startAt" in {
      val result = makePost("/api/events",
        s"""{"title":"Bad Event","startAt":"$endAt","endAt":"$startAt"}""",
        Some(token))
      status(result) mustBe BAD_REQUEST
    }

    "return 400 when title is empty" in {
      val result = makePost("/api/events",
        s"""{"title":"","startAt":"$startAt","endAt":"$endAt"}""",
        Some(token))
      status(result) mustBe BAD_REQUEST
    }

    "return 401 with no token" in {
      val result = makePost("/api/events",
        s"""{"title":"X","startAt":"$startAt","endAt":"$endAt"}""")
      status(result) mustBe UNAUTHORIZED
    }
  }

  "GET /api/events" should {

    "return 200 with list of family events" in {
      val result = makeGet("/api/events", Some(token))
      status(result) mustBe OK
      val events = parseBody(result).as[List[io.circe.Json]].getOrElse(Nil)
      events must not be empty
    }

    "return 200 filtering by from/to date range" in {
      val result = makeGet("/api/events?from=2030-01-01T00:00:00Z&to=2031-01-01T00:00:00Z", Some(token))
      status(result) mustBe OK
      parseBody(result).as[List[io.circe.Json]].getOrElse(Nil) must not be empty
    }

    "return 200 with empty list when no events match filter" in {
      val result = makeGet("/api/events?from=2020-01-01T00:00:00Z&to=2020-12-31T00:00:00Z", Some(token))
      status(result) mustBe OK
      parseBody(result).as[List[io.circe.Json]].getOrElse(List("x")) mustBe Nil
    }
  }

  "GET /api/events/:id" should {

    "return 200 with the event details" in {
      val result = makeGet(s"/api/events/$eventId", Some(token))
      status(result) mustBe OK
      field(parseBody(result), "id") mustBe eventId
    }

    "return 404 for a non-existent event id" in {
      val result = makeGet("/api/events/00000000-0000-0000-0000-000000000000", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }

  "PUT /api/events/:id" should {

    "return 200 updating an event's title and location" in {
      val result = makePut(s"/api/events/$eventId",
        """{"title":"Updated Run","location":"School Gate"}""", Some(token))
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "title") mustBe "Updated Run"
      field(json, "location") mustBe "School Gate"
    }

    "return 404 for a non-existent event id" in {
      val result = makePut("/api/events/00000000-0000-0000-0000-000000000000",
        """{"title":"Ghost"}""", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }

  "DELETE /api/events/:id" should {

    "return 204 on successful deletion" in {
      val result = makeDelete(s"/api/events/$eventId", Some(token))
      status(result) mustBe NO_CONTENT
    }

    "return 404 for an already-deleted event" in {
      val result = makeDelete(s"/api/events/$eventId", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }
