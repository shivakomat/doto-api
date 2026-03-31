package controllers

import io.circe.Json
import io.circe.parser.parse
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import scala.concurrent.Future

trait BaseSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll:

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().build()

  protected def makePost(path: String, body: String, token: Option[String] = None) =
    val headers = buildHeaders(token)
    route(app, FakeRequest(POST, path).withTextBody(body).withHeaders(headers*)).get

  protected def makeGet(path: String, token: Option[String] = None) =
    val headers = buildHeaders(token)
    route(app, FakeRequest(GET, path).withHeaders(headers*)).get

  protected def makePut(path: String, body: String, token: Option[String] = None) =
    val headers = buildHeaders(token)
    route(app, FakeRequest(PUT, path).withTextBody(body).withHeaders(headers*)).get

  protected def makePatch(path: String, body: String = "{}", token: Option[String] = None) =
    val headers = buildHeaders(token)
    route(app, FakeRequest(PATCH, path).withTextBody(body).withHeaders(headers*)).get

  protected def makeDelete(path: String, token: Option[String] = None) =
    val headers = buildHeaders(token)
    route(app, FakeRequest(DELETE, path).withHeaders(headers*)).get

  private def buildHeaders(token: Option[String]): Seq[(String, String)] =
    Seq("Content-Type" -> "text/plain") ++
      token.map(t => "Authorization" -> s"Bearer $t").toSeq

  protected def parseBody(result: Future[Result]): Json =
    parse(contentAsString(result)).getOrElse(Json.Null)

  protected def field(json: Json, key: String): String =
    json.hcursor.downField(key).as[String].getOrElse("")

  protected def fieldInt(json: Json, key: String): Int =
    json.hcursor.downField(key).as[Int].getOrElse(-1)

  protected def registerUser(username: String, password: String = "password123", displayName: String = "Test User", role: String = "parent"): String =
    val result = makePost("/api/auth/register",
      s"""{"username":"$username","password":"$password","displayName":"$displayName","role":"$role"}"""
    )
    field(parseBody(result), "token")

  protected def loginUser(username: String, password: String = "password123"): String =
    val result = makePost("/api/auth/login",
      s"""{"username":"$username","password":"$password"}"""
    )
    field(parseBody(result), "token")

  protected def createFamily(token: String, name: String = "Test Family"): Json =
    val result = makePost("/api/families", s"""{"name":"$name"}""", Some(token))
    parseBody(result)
