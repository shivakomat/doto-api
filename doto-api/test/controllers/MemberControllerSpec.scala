package controllers

import play.api.test.Helpers.*

class MemberControllerSpec extends BaseSpec:

  private val uniq        = System.nanoTime().toString.takeRight(5)
  private var parentToken = ""
  private var childId     = ""

  override def beforeAll(): Unit =
    super.beforeAll()
    parentToken = registerUser(s"mem_p_$uniq")
    createFamily(parentToken, s"Member Family $uniq")
    parentToken = loginUser(s"mem_p_$uniq")

  "GET /api/members" should {

    "return 200 with the list of family members" in {
      val result = makeGet("/api/members", Some(parentToken))
      status(result) mustBe OK
      val members = parseBody(result).as[List[io.circe.Json]].getOrElse(Nil)
      members must not be empty
    }

    "return 401 with no token" in {
      val result = makeGet("/api/members")
      status(result) mustBe UNAUTHORIZED
    }
  }

  "POST /api/members" should {

    "return 201 creating a child member as a parent" in {
      val result = makePost("/api/members",
        """{"displayName":"Junior","color":"#FF6B6B"}""", Some(parentToken))
      status(result) mustBe CREATED
      val json = parseBody(result)
      childId = field(json, "id")
      childId must not be empty
      field(json, "displayName") mustBe "Junior"
      field(json, "role") mustBe "child"
      json.hcursor.downField("isAuthAccount").as[Boolean].getOrElse(true) mustBe false
    }

    "return 400 for an invalid hex colour" in {
      val result = makePost("/api/members",
        """{"displayName":"Kid","color":"red"}""", Some(parentToken))
      status(result) mustBe BAD_REQUEST
    }

    "return 400 for an empty displayName" in {
      val result = makePost("/api/members",
        """{"displayName":"","color":"#FF6B6B"}""", Some(parentToken))
      status(result) mustBe BAD_REQUEST
    }

  }

  "PUT /api/members/:id" should {

    "return 200 updating a member's displayName and colour" in {
      val result = makePut(s"/api/members/$childId",
        """{"displayName":"Junior Jr","color":"#123456"}""", Some(parentToken))
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "displayName") mustBe "Junior Jr"
      field(json, "color") mustBe "#123456"
    }

    "return 404 for a non-existent member id" in {
      val result = makePut("/api/members/00000000-0000-0000-0000-000000000000",
        """{"displayName":"Ghost"}""", Some(parentToken))
      status(result) mustBe NOT_FOUND
    }
  }

  "DELETE /api/members/:id" should {

    "return 403 when trying to delete an auth account" in {
      val meResult = makeGet("/api/auth/me", Some(parentToken))
      val myId     = field(parseBody(meResult), "id")
      val result   = makeDelete(s"/api/members/$myId", Some(parentToken))
      status(result) mustBe FORBIDDEN
    }

    "return 204 deleting a non-auth child member" in {
      val result = makeDelete(s"/api/members/$childId", Some(parentToken))
      status(result) mustBe NO_CONTENT
    }

    "return 404 for an already-deleted member" in {
      val result = makeDelete(s"/api/members/$childId", Some(parentToken))
      status(result) mustBe NOT_FOUND
    }
  }

