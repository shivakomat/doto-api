package controllers

import play.api.test.Helpers.*

class AuthControllerSpec extends BaseSpec:

  private val uniq     = System.nanoTime().toString.takeRight(5)
  private val username = s"auth_$uniq"
  private val password = "password123"

  "POST /api/auth/register" should {

    "return 201 with token and profile on valid registration" in {
      val result = makePost("/api/auth/register",
        s"""{"username":"$username","password":"$password","displayName":"Auth User","role":"parent"}""")
      status(result) mustBe CREATED
      val json = parseBody(result)
      val token = field(json, "token")
      token must not be empty
      val profile = json.hcursor.downField("profile")
      profile.downField("username").as[String].getOrElse("") mustBe username
      profile.downField("role").as[String].getOrElse("") mustBe "parent"
      profile.downField("points").as[Int].getOrElse(-1) mustBe 0
      profile.downField("familyId").as[Option[String]].getOrElse(Some("x")) mustBe None
      profile.downField("isAuthAccount").as[Boolean].getOrElse(false) mustBe true
    }

    "return 409 when username is already taken" in {
      val result = makePost("/api/auth/register",
        s"""{"username":"$username","password":"$password","displayName":"Dup","role":"parent"}""")
      status(result) mustBe CONFLICT
      field(parseBody(result), "code") mustBe "conflict"
    }

    "return 400 when username is too short" in {
      val result = makePost("/api/auth/register",
        s"""{"username":"ab","password":"$password","displayName":"X","role":"parent"}""")
      status(result) mustBe BAD_REQUEST
      field(parseBody(result), "code") mustBe "validation_error"
    }

    "return 400 when username contains invalid characters" in {
      val result = makePost("/api/auth/register",
        s"""{"username":"bad user!","password":"$password","displayName":"X","role":"parent"}""")
      status(result) mustBe BAD_REQUEST
    }

    "return 400 when password is too short" in {
      val result = makePost("/api/auth/register",
        s"""{"username":"new_$uniq","password":"short","displayName":"X","role":"parent"}""")
      status(result) mustBe BAD_REQUEST
    }

    "return 400 when displayName is empty" in {
      val result = makePost("/api/auth/register",
        s"""{"username":"new2_$uniq","password":"$password","displayName":"","role":"parent"}""")
      status(result) mustBe BAD_REQUEST
    }

    "return 400 for malformed JSON" in {
      val result = makePost("/api/auth/register", "not-json")
      status(result) mustBe BAD_REQUEST
    }
  }

  "POST /api/auth/login" should {

    "return 200 with token on valid credentials" in {
      val result = makePost("/api/auth/login",
        s"""{"username":"$username","password":"$password"}""")
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "token") must not be empty
      json.hcursor.downField("profile").downField("username").as[String].getOrElse("") mustBe username
    }

    "return 401 for wrong password" in {
      val result = makePost("/api/auth/login",
        s"""{"username":"$username","password":"wrongpassword"}""")
      status(result) mustBe UNAUTHORIZED
      field(parseBody(result), "code") mustBe "unauthorized"
    }

    "return 401 for non-existent username" in {
      val result = makePost("/api/auth/login",
        s"""{"username":"nobody_$uniq","password":"$password"}""")
      status(result) mustBe UNAUTHORIZED
    }
  }

  "GET /api/auth/me" should {

    "return 200 with the caller's profile when authenticated" in {
      val meUser  = s"me_$uniq"
      registerUser(meUser)
      val meToken = loginUser(meUser)
      meToken must not be empty
      val result  = makeGet("/api/auth/me", Some(meToken))
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "username") mustBe meUser
      field(json, "role") mustBe "parent"
    }

    "return 401 when no token is provided" in {
      val result = makeGet("/api/auth/me")
      status(result) mustBe UNAUTHORIZED
    }

    "return 401 for an invalid token" in {
      val result = makeGet("/api/auth/me", Some("Bearer invalid.token.here"))
      status(result) mustBe UNAUTHORIZED
    }
  }
