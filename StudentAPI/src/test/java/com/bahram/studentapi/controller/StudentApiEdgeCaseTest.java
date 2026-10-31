package com.bahram.studentapi.controller;

import com.bahram.studentapi.repository.StudentRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/** Edge-case / boundary tests: validation limits, not-found paths and the error format. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class StudentApiEdgeCaseTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @LocalServerPort
    int port;

    @Autowired
    StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        studentRepository.deleteAll();
    }

    /** Valid body; age is raw JSON so tests can send 0, -1, "abc", null, ... */
    private static String body(String name, String age, String email) {
        String e = "";
        if (email != null) {
            e = ",\"email\":\"" + email + "\"";
        }
        return "{\"name\":" + name + ",\"age\":" + age + ",\"gender\":\"F\",\"major\":\"Math\"" + e + "}";
    }

    private int create(String name, String email) {
        return given().contentType(ContentType.JSON).body(body("\"" + name + "\"", "20", email))
                .when().post("/students")
                .then().statusCode(201).extract().path("id");
    }

    private void expect400(String json, String field) {
        given().contentType(ContentType.JSON).body(json)
                .when().post("/students")
                .then().statusCode(400)
                .contentType("application/problem+json")
                .body("status", equalTo(400))
                .body("errors." + field, notNullValue());
    }

    // ---- age boundaries (@NotNull @Min(1)) ----
    @Test
    void ageOneIsAccepted() {
        given().contentType(ContentType.JSON).body(body("\"Min\"", "1", null))
                .when().post("/students").then().statusCode(201).body("age", equalTo(1));
    }

    @Test
    void ageZeroReturns400() { expect400(body("\"Zero\"", "0", null), "age"); }

    @Test
    void negativeAgeReturns400() { expect400(body("\"Neg\"", "-1", null), "age"); }

    @Test
    void missingAgeReturns400() { expect400(body("\"NoAge\"", "null", null), "age"); }

    // ---- name (@NotBlank) ----
    @Test
    void blankNameReturns400() { expect400(body("\"\"", "20", null), "name"); }

    @Test
    void whitespaceOnlyNameReturns400() { expect400(body("\"   \"", "20", null), "name"); }

    @Test
    void nullNameReturns400() { expect400(body("null", "20", null), "name"); }

    @Test
    void nameAtDbLimit255IsAccepted() {
        create("a".repeat(255), null);
    }

    @Test
    void nameOverDbLimitReturns400() {
        // 256 chars: must be rejected by @Size(max = 255) as 400, not reach the DB and become a misleading 409
        expect400(body("\"" + "a".repeat(256) + "\"", "20", null), "name");
    }

    // ---- malformed requests ----
    @Test
    void malformedJsonReturns400ProblemDetail() {
        given().contentType(ContentType.JSON).body("{\"name\": \"Bad\", ")
                .when().post("/students")
                .then().statusCode(400).contentType("application/problem+json")
                .body("status", equalTo(400)).body("$", not(hasKey("trace")));
    }

    @Test
    void ageAsStringReturns400() {
        given().contentType(ContentType.JSON).body(body("\"Str\"", "\"abc\"", null))
                .when().post("/students")
                .then().statusCode(400).contentType("application/problem+json");
    }

    @Test
    void nonNumericIdReturns400() {
        given().when().get("/students/abc")
                .then().statusCode(400).contentType("application/problem+json")
                .body("status", equalTo(400));
    }

    // ---- not found ----
    @Test
    void putOnMissingStudentReturns404() {
        given().contentType(ContentType.JSON).body(body("\"Ghost\"", "20", null))
                .when().put("/students/99999")
                .then().statusCode(404).contentType("application/problem+json")
                .body("detail", equalTo("Student not found with id: 99999"));
    }

    @Test
    void getAfterDeleteReturns404() {
        int id = create("Temp", "temp@school.edu");
        given().when().delete("/students/" + id).then().statusCode(200).body("id", equalTo(id));
        given().when().get("/students/" + id).then().statusCode(404);
    }

    // ---- search ----
    @Test
    void searchesWithNoMatchReturnEmptyList() {
        create("Ada", "ada@school.edu");
        given().get("/students/name/Nobody").then().statusCode(200).body("$", empty());
        given().get("/students/major/Art").then().statusCode(200).body("$", empty());
        given().get("/students/email/none@school.edu").then().statusCode(200).body("$", empty());
    }

    @Test
    void olderThanIsStrictlyGreater() {
        // age 20 is not "older than 20", but is older than 19
        create("Ada", null);
        given().get("/students/older-than/20").then().statusCode(200).body("$", empty());
        given().get("/students/older-than/19").then().statusCode(200).body("$", hasSize(1));
    }
}
