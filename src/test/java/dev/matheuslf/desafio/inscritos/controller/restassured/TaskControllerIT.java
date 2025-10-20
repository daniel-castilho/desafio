package dev.matheuslf.desafio.inscritos.controller.restassured;

import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskRequest;
import dev.matheuslf.desafio.inscritos.domain.entities.Project;
import dev.matheuslf.desafio.inscritos.domain.entities.Task;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskPriority;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;
import dev.matheuslf.desafio.inscritos.repository.ProjectRepository;
import dev.matheuslf.desafio.inscritos.repository.TaskRepository;
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
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        taskRepository.deleteAll();
        projectRepository.deleteAll();

        // Arrange: Create a persistent project to be used by tasks
        testProject = new Project();
        testProject.setName("Projeto Pai para Testes de Tarefa");
        testProject.setStartDate(LocalDate.now());
        projectRepository.save(testProject);
    }

    @Test
    @DisplayName("POST /tasks: Deve criar uma tarefa e retornar 201 CREATED")
    void createTask_ShouldCreateAndReturnTask() {
        var request = new TaskRequest(
                "Tarefa de Teste End-to-End",
                "Descrição da tarefa de teste.",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5),
                testProject.getId()
        );

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/tasks")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("id", notNullValue())
            .body("title", equalTo(request.title()))
            .body("projectId", equalTo(testProject.getId().toString()));
    }

    @Test
    @DisplayName("GET /tasks/{id}: Deve retornar uma tarefa quando o ID existe")
    void findById_ShouldReturnTask_WhenTaskExists() {
        // Arrange
        Task task = Task.builder()
                .title("Tarefa para Busca")
                .status(TaskStatus.DOING)
                .priority(TaskPriority.HIGH)
                .project(testProject)
                .build();
        taskRepository.save(task);

        given()
            .pathParam("id", task.getId())
        .when()
            .get("/tasks/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(task.getId().toString()))
            .body("title", equalTo("Tarefa para Busca"));
    }

    @Test
    @DisplayName("DELETE /tasks/{id}: Deve deletar uma tarefa e retornar 204 NO CONTENT")
    void delete_ShouldReturnNoContent_WhenTaskExists() {
        // Arrange
        Task task = Task.builder()
                .title("Tarefa para Deletar")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .project(testProject)
                .build();
        taskRepository.save(task);

        // Act & Assert para o DELETE
        given()
            .pathParam("id", task.getId())
        .when()
            .delete("/tasks/{id}")
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Assert para a verificação pós-deleção
        given()
            .pathParam("id", task.getId())
        .when()
            .get("/tasks/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
