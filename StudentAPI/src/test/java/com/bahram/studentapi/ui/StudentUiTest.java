package com.bahram.studentapi.ui;

import com.bahram.studentapi.repository.StudentRepository;
import com.bahram.studentapi.ui.pages.StudentPage;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class StudentUiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @LocalServerPort
    int port;

    @Autowired
    StudentRepository studentRepository;

    WebDriver driver;
    StudentPage page;

    @BeforeEach
    public void setUp() {
        studentRepository.deleteAll();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        page = new StudentPage(driver, "http://localhost:" + port);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void pageLoadsWithEmptyTable() {
        // Act
        page.open();

        // Assert
        assertThat(page.rows()).isEmpty();
    }

    @Test
    public void studentAddedInUiIsSavedInApi() {
        // Arrange
        page.open();

        // Act
        page.addStudent("Amy", "20", "Female", "Math", "amy@school.edu");
        page.waitForRowContaining("amy@school.edu");

        // Assert: UI -> API
        given()
                .when()
                .get("/students")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Amy"))
                .body("[0].email", equalTo("amy@school.edu"));
    }

    @Test
    public void studentCreatedInApiIsShownInUi() {
        // Arrange: API -> UI
        createStudentViaApi("Ben", "ben@school.edu");

        // Act
        page.open();

        // Assert
        assertThat(page.rows()).hasSize(1);
        assertThat(page.rows().get(0).getText()).contains("Ben", "ben@school.edu");
    }

    @Test
    public void duplicateEmailWithDifferentCaseShows409Error() {
        // Arrange
        createStudentViaApi("Tom", "tom@school.edu");
        page.open();

        // Act
        page.addStudent("Tommy", "22", "Male", "Physics", "TOM@school.edu");

        // Assert
        assertThat(page.errorMessage()).startsWith("409").contains("Email already exists");
        assertThat(page.rows()).hasSize(1);
    }

    @Test
    public void blankNameShowsValidationError(){
        // Arrange
        page.open();

        // Act: blank name, valid email
        page.addStudent("","20","Female","Accounting","amy@school.edu");

        // Assert
        assertThat(page.errorMessage()).startsWith("400").contains("name");
        assertThat(page.rows()).isEmpty();
    }

    @Test
    public void invalidEmailShowsValidationError(){
        // Arrange
        page.open();

        // Act: blank name, valid email
        page.addStudent("Amy Wolf","20","Female","Accounting","not-an-email");

        // Assert
        assertThat(page.errorMessage()).startsWith("400").contains("email");
        assertThat(page.rows()).isEmpty();
    }

    private void createStudentViaApi(String name, String email) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"%s","age":21,"gender":"Male","major":"CS","email":"%s"}
                        """.formatted(name, email))
                .when()
                .post("/students")
                .then()
                .statusCode(201);
    }
}
