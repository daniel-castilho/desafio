package dev.matheuslf.desafio.inscritos.controller;

import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectRequest;
import dev.matheuslf.desafio.inscritos.repository.ProjectRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProjectControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        projectRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /projects: Deve criar um projeto e retornar 201 CREATED")
    void createProject_ShouldCreateAndReturnProject() {
        var request = new ProjectRequest(
                "Projeto de Teste End-to-End",
                "Descrição do projeto de teste.",
                LocalDate.now(),
                LocalDate.now().plusWeeks(2)
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/projects")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("id", notNullValue())
            .body("name", equalTo(request.name()))
            .body("description", equalTo(request.description()));
    }
}
