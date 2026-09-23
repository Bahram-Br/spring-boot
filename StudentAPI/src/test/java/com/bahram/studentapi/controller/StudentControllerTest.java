package com.bahram.studentapi.controller;

import com.bahram.studentapi.exception.StudentNotFoundException;
import com.bahram.studentapi.service.StudentService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @BeforeEach
    public void setUp(){
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    public void listStudentsReturnsEmptyArray() throws Exception {

        when(studentService.getStudents()).thenReturn(List.of());

        given()
        .when()
                .get("/api/v1/students")
        .then()
                .statusCode(200)
                        .body("", hasSize(0));

    }

    @Test
    public void getStudentReturns404(){

        when(studentService.getStudents()).thenThrow(new StudentNotFoundException(1));

        given()
                .when()
                .get("/api/v1/students")
                .then()
                .statusCode(404);
    }

    @Test
    public void addStudentRejectsBlankName(){

        given()
                .contentType(ContentType.JSON)
                .body("{\"age\":20,\"name\":\"\",\"gender\":\"F\",\"major\":\"CS\"}")
                .when()
                .post("/api/v1/students")
                .then()
                .statusCode(400);
    }
}


