package controllers

import play.api.test.Helpers.*

class ShoppingControllerSpec extends BaseSpec:

  private val uniq   = System.nanoTime().toString.takeRight(5)
  private var token  = ""
  private var listId = ""
  private var itemId = ""

  override def beforeAll(): Unit =
    super.beforeAll()
    token = registerUser(s"shp_p_$uniq")
    createFamily(token, s"Shop Family $uniq")
    token = loginUser(s"shp_p_$uniq")

  "GET /api/shopping/lists" should {

    "return 200 with an empty list initially" in {
      val result = makeGet("/api/shopping/lists", Some(token))
      status(result) mustBe OK
      parseBody(result).as[List[io.circe.Json]].getOrElse(List("x")) mustBe Nil
    }

    "return 401 with no token" in {
      val result = makeGet("/api/shopping/lists")
      status(result) mustBe UNAUTHORIZED
    }
  }

  "POST /api/shopping/lists" should {

    "return 201 creating a shopping list" in {
      val result = makePost("/api/shopping/lists", """{"name":"Weekly Groceries"}""", Some(token))
      status(result) mustBe CREATED
      val json = parseBody(result)
      listId = field(json, "id")
      listId must not be empty
      field(json, "name") mustBe "Weekly Groceries"
      json.hcursor.downField("itemCount").as[Int].getOrElse(-1) mustBe 0
    }

    "return 400 when name is blank" in {
      val result = makePost("/api/shopping/lists", """{"name":""}""", Some(token))
      status(result) mustBe BAD_REQUEST
    }
  }

  "GET /api/shopping/lists/:id/items" should {

    "return 200 with empty items for a new list" in {
      val result = makeGet(s"/api/shopping/lists/$listId/items", Some(token))
      status(result) mustBe OK
      parseBody(result).as[List[io.circe.Json]].getOrElse(List("x")) mustBe Nil
    }

    "return 404 for a non-existent list" in {
      val result = makeGet("/api/shopping/lists/00000000-0000-0000-0000-000000000000/items", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }

  "POST /api/shopping/lists/:id/items" should {

    "return 201 adding an item to the list" in {
      val result = makePost(s"/api/shopping/lists/$listId/items",
        """{"name":"Organic Milk","category":"dairy","quantity":"2L"}""",
        Some(token))
      status(result) mustBe CREATED
      val json = parseBody(result)
      itemId = field(json, "id")
      itemId must not be empty
      field(json, "name") mustBe "Organic Milk"
      field(json, "category") mustBe "dairy"
      json.hcursor.downField("isChecked").as[Boolean].getOrElse(true) mustBe false
    }

    "return 201 adding an item with default category" in {
      val result = makePost(s"/api/shopping/lists/$listId/items",
        """{"name":"Mystery Item"}""", Some(token))
      status(result) mustBe CREATED
      field(parseBody(result), "category") mustBe "other"
    }

    "return 400 when item name is empty" in {
      val result = makePost(s"/api/shopping/lists/$listId/items",
        """{"name":""}""", Some(token))
      status(result) mustBe BAD_REQUEST
    }

    "return 404 for adding item to non-existent list" in {
      val result = makePost("/api/shopping/lists/00000000-0000-0000-0000-000000000000/items",
        """{"name":"Nowhere"}""", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }

  "PATCH /api/shopping/lists/:listId/items/:itemId/check" should {

    "return 200 checking an item" in {
      val result = makePatch(s"/api/shopping/lists/$listId/items/$itemId/check",
        """{"isChecked":true}""", Some(token))
      status(result) mustBe OK
      val json = parseBody(result)
      json.hcursor.downField("isChecked").as[Boolean].getOrElse(false) mustBe true
      json.hcursor.downField("checkedBy").as[Option[String]].getOrElse(None) must not be empty
    }

    "return 200 unchecking an item" in {
      val result = makePatch(s"/api/shopping/lists/$listId/items/$itemId/check",
        """{"isChecked":false}""", Some(token))
      status(result) mustBe OK
      parseBody(result).hcursor.downField("isChecked").as[Boolean].getOrElse(true) mustBe false
    }

    "return 404 for a non-existent item" in {
      val result = makePatch(s"/api/shopping/lists/$listId/items/00000000-0000-0000-0000-000000000000/check",
        """{"isChecked":true}""", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }

  "DELETE /api/shopping/lists/:id/items/checked" should {

    "return 200 clearing all checked items from the list" in {
      status(makePatch(s"/api/shopping/lists/$listId/items/$itemId/check",
        """{"isChecked":true}""", Some(token)))
      val result = makeDelete(s"/api/shopping/lists/$listId/items/checked", Some(token))
      status(result) mustBe OK
      parseBody(result).hcursor.downField("deletedCount").as[Int].getOrElse(-1) must be >= 1
    }
  }

  "DELETE /api/shopping/lists/:listId/items/:itemId" should {

    "return 204 deleting a specific item" in {
      val newItem = makePost(s"/api/shopping/lists/$listId/items",
        """{"name":"To Be Deleted"}""", Some(token))
      val iid     = field(parseBody(newItem), "id")
      val result  = makeDelete(s"/api/shopping/lists/$listId/items/$iid", Some(token))
      status(result) mustBe NO_CONTENT
    }
  }

  "DELETE /api/shopping/lists/:id" should {

    "return 204 deleting the whole list" in {
      val result = makeDelete(s"/api/shopping/lists/$listId", Some(token))
      status(result) mustBe NO_CONTENT
    }

    "return 404 for an already-deleted list" in {
      val result = makeDelete(s"/api/shopping/lists/$listId", Some(token))
      status(result) mustBe NOT_FOUND
    }
  }

