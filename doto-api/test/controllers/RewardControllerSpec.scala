package controllers

import play.api.test.Helpers.*

class RewardControllerSpec extends BaseSpec:

  private val uniq         = System.nanoTime().toString.takeRight(5)
  private var token        = ""
  private var childId      = ""
  private var activeRewId  = ""
  private var pendingRewId = ""
  private var approvedRewId= ""

  override def beforeAll(): Unit =
    super.beforeAll()
    token = registerUser(s"rew_p_$uniq")
    createFamily(token, s"Reward Family $uniq")
    token = loginUser(s"rew_p_$uniq")
    val childResult = makePost("/api/members",
      """{"displayName":"Reward Child","color":"#11AAFF"}""", Some(token))
    childId = field(parseBody(childResult), "id")
    val taskResult = makePost("/api/tasks",
      s"""{"title":"Points task","assignedTo":"$childId","points":200}""", Some(token))
    val taskId = field(parseBody(taskResult), "id")
    status(makePatch(s"/api/tasks/$taskId/complete", token = Some(token))) mustBe OK

  "POST /api/rewards" should {

    "return 201 creating a reward for a child member" in {
      val result = makePost("/api/rewards",
        s"""{"memberId":"$childId","title":"iPad Time","pointsCost":50}""", Some(token))
      status(result) mustBe CREATED
      val json = parseBody(result)
      activeRewId = field(json, "id")
      activeRewId must not be empty
      field(json, "title") mustBe "iPad Time"
      field(json, "status") mustBe "active"
      json.hcursor.downField("pointsCost").as[Int].getOrElse(-1) mustBe 50
    }

    "return 400 when pointsCost is zero" in {
      val result = makePost("/api/rewards",
        s"""{"memberId":"$childId","title":"Free","pointsCost":0}""", Some(token))
      status(result) mustBe BAD_REQUEST
    }

    "return 400 when pointsCost is negative" in {
      val result = makePost("/api/rewards",
        s"""{"memberId":"$childId","title":"Neg","pointsCost":-1}""", Some(token))
      status(result) mustBe BAD_REQUEST
    }

    "return 400 when title is empty" in {
      val result = makePost("/api/rewards",
        s"""{"memberId":"$childId","title":"","pointsCost":10}""", Some(token))
      status(result) mustBe BAD_REQUEST
    }

    "return 401 with no token" in {
      val result = makePost("/api/rewards",
        s"""{"memberId":"$childId","title":"X","pointsCost":10}""")
      status(result) mustBe UNAUTHORIZED
    }
  }

  "GET /api/rewards" should {

    "return 200 with list of family rewards" in {
      val result = makeGet("/api/rewards", Some(token))
      status(result) mustBe OK
      parseBody(result).as[List[io.circe.Json]].getOrElse(Nil) must not be empty
    }

    "return 200 filtered by memberId" in {
      val result  = makeGet(s"/api/rewards?memberId=$childId", Some(token))
      status(result) mustBe OK
      val rewards = parseBody(result).as[List[io.circe.Json]].getOrElse(Nil)
      rewards.foreach(r => r.hcursor.downField("memberId").as[String].getOrElse("") mustBe childId)
    }

    "return 200 filtered by status=active" in {
      val result  = makeGet("/api/rewards?status=active", Some(token))
      status(result) mustBe OK
      val rewards = parseBody(result).as[List[io.circe.Json]].getOrElse(Nil)
      rewards.foreach(r => r.hcursor.downField("status").as[String].getOrElse("") mustBe "active")
    }
  }

  "PATCH /api/rewards/:id/request" should {

    "return 409 when the child has insufficient points for the reward cost" in {
      val bigCostResult = makePost("/api/rewards",
        s"""{"memberId":"$childId","title":"Space Trip","pointsCost":9999}""", Some(token))
      val bigRewId = field(parseBody(bigCostResult), "id")
      val result   = makePatch(s"/api/rewards/$bigRewId/request", token = Some(token))
      status(result) mustBe CONFLICT
      field(parseBody(result), "code") mustBe "conflict"
    }

    "return 200 requesting a reward when the child has enough points" in {
      val result = makePatch(s"/api/rewards/$activeRewId/request", token = Some(token))
      status(result) mustBe OK
      val json = parseBody(result)
      pendingRewId = field(json, "id")
      field(json, "status") mustBe "pending_approval"
      json.hcursor.downField("requestedAt").as[Option[String]].getOrElse(None) must not be empty
    }

    "return 400 if the reward is not in active status" in {
      val result = makePatch(s"/api/rewards/$pendingRewId/request", token = Some(token))
      status(result) mustBe BAD_REQUEST
    }
  }

  "PATCH /api/rewards/:id/approve" should {

    "return 200 approving a pending_approval reward as a parent" in {
      val result = makePatch(s"/api/rewards/$pendingRewId/approve", token = Some(token))
      status(result) mustBe OK
      val json = parseBody(result)
      approvedRewId = field(json, "id")
      field(json, "status") mustBe "approved"
      json.hcursor.downField("approvedBy").as[Option[String]].getOrElse(None) must not be empty
    }

    "return 400 if the reward is not in pending_approval status" in {
      val newActive = makePost("/api/rewards",
        s"""{"memberId":"$childId","title":"Not Pending","pointsCost":1}""", Some(token))
      val nid    = field(parseBody(newActive), "id")
      val result = makePatch(s"/api/rewards/$nid/approve", token = Some(token))
      status(result) mustBe BAD_REQUEST
    }
  }

  "PATCH /api/rewards/:id/redeem" should {

    "return 200 redeeming an approved reward as a parent" in {
      val result = makePatch(s"/api/rewards/$approvedRewId/redeem", token = Some(token))
      status(result) mustBe OK
      field(parseBody(result), "status") mustBe "redeemed"
    }

    "return 400 if the reward is not in approved status" in {
      val result = makePatch(s"/api/rewards/$approvedRewId/redeem", token = Some(token))
      status(result) mustBe BAD_REQUEST
    }
  }

  "DELETE /api/rewards/:id" should {

    "return 204 deleting an active reward as a parent" in {
      val newReward = makePost("/api/rewards",
        s"""{"memberId":"$childId","title":"Deletable","pointsCost":10}""", Some(token))
      val rid    = field(parseBody(newReward), "id")
      val result = makeDelete(s"/api/rewards/$rid", Some(token))
      status(result) mustBe NO_CONTENT
    }

    "return 404 for a non-existent reward id" in {
      val result = makeDelete(s"/api/rewards/00000000-0000-0000-0000-000000000000", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }
