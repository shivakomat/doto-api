package controllers

import play.api.test.Helpers.*

class HealthControllerSpec extends BaseSpec:

  "GET /api/health" should {

    "return 200 with status ok" in {
      val result = makeGet("/api/health")
      status(result) mustBe OK
      val json = parseBody(result)
      field(json, "status") mustBe "ok"
    }
  }
