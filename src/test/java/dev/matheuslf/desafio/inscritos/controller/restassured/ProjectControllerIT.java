package dev.matheuslf.desafio.inscritos.controller.restassured;

import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectRequest;
import dev.matheuslf.desafio.inscritos.domain.entities.Project;
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

    @Test
    @DisplayName("GET /projects/{id}: Deve retornar um projeto quando o ID existe")
    void findById_ShouldReturnProject_WhenProjectExists() {
        // Arrange
        Project project = new Project();
        project.setName("Projeto para Busca");
        project.setStartDate(LocalDate.now());
        projectRepository.save(project);

        given()
            .pathParam("id", project.getId())
        .when()
            .get("/projects/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(project.getId().toString()))
            .body("name", equalTo("Projeto para Busca"));
    }

    @Test
    @DisplayName("DELETE /projects/{id}: Deve deletar um projeto e retornar 204 NO CONTENT")
    void delete_ShouldReturnNoContent_WhenProjectExists() {
        // Arrange
        Project project = new Project();
        project.setName("Projeto para Deletar");
        project.setStartDate(LocalDate.now());
        projectRepository.save(project);

        // Act & Assert para o DELETE
        given()
            .pathParam("id", project.getId())
        .when()
            .delete("/projects/{id}")
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Assert para a verificação pós-deleção
        given()
            .pathParam("id", project.getId())
        .when()
            .get("/projects/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
