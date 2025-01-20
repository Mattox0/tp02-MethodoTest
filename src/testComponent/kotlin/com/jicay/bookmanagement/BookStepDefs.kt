package com.jicay.bookmanagement

import io.cucumber.java.Before
import io.cucumber.java.Scenario
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.kotest.matchers.shouldBe
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import org.springframework.boot.test.web.server.LocalServerPort

class BookStepDefs {
    @LocalServerPort
    private var port: Int? = 0

    @Before
    fun setup(scenario: Scenario) {
        RestAssured.baseURI = "http://localhost:$port"
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @When("the user creates the book {string} written by {string}")
    fun createBook(title: String, author: String) {
        given()
            .contentType(ContentType.JSON)
            .and()
            .body(
                """
                    {
                      "name": "$title",
                      "author": "$author"
                    }
                """.trimIndent()
            )
            .`when`()
            .post("/books")
            .then()
            .statusCode(201)
    }

    @When("the user get all books")
    fun getAllBooks() {
        lastResponse = given()
            .`when`()
            .get("/books")
    }

    @When("the user gets the book with id {int}")
    fun getBookById(id: Int) {
        lastResponse = given()
            .`when`()
            .get("/books/$id")
    }

    @When("the user reserves the book with id {int}")
    fun reserveBook(id: Int) {
        lastResponse = given()
            .contentType(ContentType.JSON)
            .`when`()
            .post("/books/reserved/$id")
    }

    @Then("the list should contains the following books in the same order")
    fun shouldHaveListOfBooks(payload: List<Map<String, Any>>) {
        lastResponse.then().statusCode(200)
        val expectedResponse = JsonPath(payload.joinToString(separator = ",", prefix = "[", postfix = "]") { line ->
            """
                {
                    "name": "${line["name"]}",
                    "author": "${line["author"]}",
                    "reserved": ${line["reserved"]}
                }
            """.trimIndent()
        }).prettify()

        lastResponse.then().extract().body().jsonPath().prettify() shouldBe expectedResponse
    }

    @Then("the book should have the following properties")
    fun shouldHaveBookProperties(payload: List<Map<String, Any>>) {
        lastResponse.then().statusCode(200)
        val expectedResponse = JsonPath("""
            {
                "name": "${payload[0]["name"]}",
                "author": "${payload[0]["author"]}",
                "reserved": ${payload[0]["reserved"]}
            }
        """.trimIndent()).prettify()

        lastResponse.then().extract().body().jsonPath().prettify() shouldBe expectedResponse
    }

    @Then("the reservation should fail with conflict status")
    fun reservationShouldFailWithConflict() {
        lastResponse.then().statusCode(409)
    }

    @Then("the book should not be found")
    fun bookShouldNotBeFound() {
        lastResponse.then().statusCode(404)
    }

    companion object {
        lateinit var lastResponse: Response
    }
}