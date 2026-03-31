package controllers

import play.api.test.Helpers.*

class FamilyControllerSpec extends BaseSpec:

  private val uniq        = System.nanoTime().toString.takeRight(5)
  private var token       = ""
  private var familyId    = ""
  private var inviteCode  = ""

  "POST /api/families" should {

    "return 201 with family and members list on creation" in {
      token = registerUser(s"fam_p_$uniq")
      val result = makePost("/api/families", s"""{"name":"Smith Family $uniq"}""", Some(token))
      status(result) mustBe CREATED
      val json = parseBody(result)
      familyId   = field(json, "id")
      inviteCode = field(json, "inviteCode")
      familyId must not be empty
      inviteCode must have length 6
      field(json, "name") mustBe s"Smith Family $uniq"
      json.hcursor.downField("members").as[List[io.circe.Json]].getOrElse(Nil) must not be empty
      token = loginUser(s"fam_p_$uniq")
    }

    "return 409 when user already belongs to a family" in {
      val result = makePost("/api/families", s"""{"name":"Another Family"}""", Some(token))
      status(result) mustBe CONFLICT
      field(parseBody(result), "code") mustBe "conflict"
    }

    "return 400 when name is blank" in {
      val t2 = registerUser(s"fam_b_$uniq")
      val result = makePost("/api/families", """{"name":"   "}""", Some(t2))
      status(result) mustBe BAD_REQUEST
    }

    "return 401 with no token" in {
      val result = makePost("/api/families", """{"name":"X"}""")
      status(result) mustBe UNAUTHORIZED
    }
  }

  "POST /api/families/join" should {

    "return 200 and join the family using a valid invite code" in {
      val joinerToken = registerUser(s"fam_j_$uniq")
      val result      = makePost("/api/families/join", s"""{"inviteCode":"$inviteCode","role":"parent"}""", Some(joinerToken))
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "id") mustBe familyId
      val members = json.hcursor.downField("members").as[List[io.circe.Json]].getOrElse(Nil)
      members.length must be >= 2
    }

    "return 409 when user already belongs to a family" in {
      val result = makePost("/api/families/join", s"""{"inviteCode":"$inviteCode","role":"parent"}""", Some(token))
      status(result) mustBe CONFLICT
    }

    "return 404 for an invalid invite code" in {
      val t3     = registerUser(s"fam_i_$uniq")
      val result = makePost("/api/families/join", """{"inviteCode":"XXXXXX","role":"parent"}""", Some(t3))
      status(result) mustBe NOT_FOUND
    }
  }

  "GET /api/families/mine" should {

    "return 200 with family details including members" in {
      val result = makeGet("/api/families/mine", Some(token))
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "id") mustBe familyId
      json.hcursor.downField("members").as[List[io.circe.Json]].getOrElse(Nil) must not be empty
    }

    "return 404 when user has no family" in {
      val loneToken = registerUser(s"fam_l_$uniq")
      val result    = makeGet("/api/families/mine", Some(loneToken))
      status(result) mustBe NOT_FOUND
    }
  }

  "GET /api/families/mine/invite-code" should {

    "return 200 with the invite code and family name" in {
      val result = makeGet("/api/families/mine/invite-code", Some(token))
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "inviteCode") mustBe inviteCode
      field(json, "familyName") must not be empty
    }

    "return 401 with no token" in {
      val result = makeGet("/api/families/mine/invite-code")
      status(result) mustBe UNAUTHORIZED
    }
  }
