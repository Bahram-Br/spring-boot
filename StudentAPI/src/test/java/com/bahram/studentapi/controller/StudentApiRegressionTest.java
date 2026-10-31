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

/** Regression tests for bugs found by AI exploratory testing via student-mcp. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class StudentApiRegressionTest {

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

    private static String body(String name, String email) {
        String e = email == null ? "" : ",\"email\":\"" + email + "\"";
        return "{\"name\":\"" + name + "\",\"age\":20,\"gender\":\"F\",\"major\":\"Math\"" + e + "}";
    }

    private int create(String name, String email) {
        return given().contentType(ContentType.JSON).body(body(name, email))
                .when().post("/students")
                .then().statusCode(201).extract().path("id");
    }

    // Bug 5: POST returned 200
    @Test
    void postValidStudentReturns201WithLocation() {
        int id = given().contentType(ContentType.JSON).body(body("Ada", "ada@school.edu"))
                .when().post("/students")
                .then().statusCode(201)
                .header("Location", matchesPattern(".*/api/v1/students/\\d+$"))
                .body("name", equalTo("Ada"))
                .extract().path("id");
        given().when().get("/students/" + id).then().statusCode(200);
    }

    // Bug 2: duplicate email on POST returned 500
    @Test
    void postDuplicateEmailReturns409() {
        create("Tom", "tom@school.edu");
        given().contentType(ContentType.JSON).body(body("Tom2", "tom@school.edu"))
                .when().post("/students")
                .then().statusCode(409)
                .contentType("application/problem+json")
                .body("status", equalTo(409))
                .body("detail", containsString("tom@school.edu"));
    }

    // Bug 2: duplicate email on PUT returned 500
    @Test
    void putDuplicateEmailReturns409() {
        create("Tom", "tom@school.edu");
        int caseyId = create("Casey", "casey@school.edu");
        given().contentType(ContentType.JSON).body(body("Casey", "tom@school.edu"))
                .when().put("/students/" + caseyId)
                .then().statusCode(409)
                .contentType("application/problem+json")
                .body("status", equalTo(409));
        given().when().get("/students/" + caseyId)
                .then().statusCode(200).body("email", equalTo("casey@school.edu"));
    }

    @Test
    void putSameEmailOnSameStudentIsAllowed() {
        int id = create("Casey", "casey@school.edu");
        given().contentType(ContentType.JSON).body(body("Casey Updated", "casey@school.edu"))
                .when().put("/students/" + id)
                .then().statusCode(200).body("name", equalTo("Casey Updated"));
    }

    // Bug 3 + Bug 1: older-than/14 returned 500 with a stack trace
    @Test
    void olderThanBelowMinimumReturns400WithoutStackTrace() {
        String response = given().when().get("/students/older-than/14")
                .then().statusCode(400)
                .contentType("application/problem+json")
                .body("status", equalTo(400))
                .extract().asString();
        org.junit.jupiter.api.Assertions.assertFalse(response.contains("stackTrace"), response);
        org.junit.jupiter.api.Assertions.assertFalse(response.contains("trace"), response);
        org.junit.jupiter.api.Assertions.assertFalse(response.contains("at com."), response);
    }

    @Test
    void olderThanValidAgeReturns200() {
        given().when().get("/students/older-than/15").then().statusCode(200);
    }

    // Bug 4: invalid email was accepted
    @Test
    void postInvalidEmailReturns400WithFieldError() {
        given().contentType(ContentType.JSON).body(body("Quinn", "not-an-email"))
                .when().post("/students")
                .then().statusCode(400)
                .contentType("application/problem+json")
                .body("status", equalTo(400))
                .body("errors.email", notNullValue());
    }

    @Test
    void postWithoutEmailStillAllowed() {
        create("NoEmail", null);
    }

    // Bug 6: 404 returned plain text
    @Test
    void getMissingStudentReturns404ProblemDetail() {
        given().when().get("/students/99999")
                .then().statusCode(404)
                .contentType("application/problem+json")
                .body("status", equalTo(404))
                .body("detail", equalTo("Student not found with id: 99999"));
    }

    @Test
    void deleteMissingStudentReturns404ProblemDetail() {
        given().when().delete("/students/99999")
                .then().statusCode(404)
                .contentType("application/problem+json")
                .body("status", equalTo(404))
                .body("detail", equalTo("Student not found with id: 99999"));
    }

    // Bug 8: duplicate email check was case-sensitive
    @Test
    void postDuplicateEmailDifferentCaseReturns409() {
        create("Tom", "tom@school.edu");
        given().contentType(ContentType.JSON).body(body("Tom2", "TOM@school.edu"))
                .when().post("/students")
                .then().statusCode(409)
                .contentType("application/problem+json")
                .body("status", equalTo(409))
                .body("detail", containsString("Email already exists"));
    }

    @Test
    void postEmailIsStoredLowercase() {
        int id = given().contentType(ContentType.JSON).body(body("Amy", "Amy@School.EDU"))
                .when().post("/students")
                .then().statusCode(201)
                .body("email", equalTo("amy@school.edu"))
                .extract().path("id");
        given().when().get("/students/" + id)
                .then().statusCode(200).body("email", equalTo("amy@school.edu"));
    }

    @Test
    void putDuplicateEmailDifferentCaseReturns409() {
        create("Tom", "tom@school.edu");
        int caseyId = create("Casey", "casey@school.edu");
        given().contentType(ContentType.JSON).body(body("Casey", "Tom@School.edu"))
                .when().put("/students/" + caseyId)
                .then().statusCode(409)
                .contentType("application/problem+json")
                .body("detail", containsString("Email already exists"));
        given().when().get("/students/" + caseyId)
                .then().statusCode(200).body("email", equalTo("casey@school.edu"));
    }

    @Test
    void searchByEmailIsCaseInsensitive() {
        int id = create("Tom", "tom@school.edu");
        given().when().get("/students/email/TOM@School.edu")
                .then().statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(id))
                .body("[0].email", equalTo("tom@school.edu"));
    }
}
