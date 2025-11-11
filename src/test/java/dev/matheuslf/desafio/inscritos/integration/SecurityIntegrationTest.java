package dev.matheuslf.desafio.inscritos.integration;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIntegrationTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private void registerUser(String username, String password, String role) {
        Map<String, Object> body = Map.of(
                "username", username,
                "password", password,
                "role", role
        );

        given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200);
    }

    private String loginAndGetToken(String username, String password) {
        Map<String, Object> body = Map.of(
                "username", username,
                "password", password
        );

        return given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    @DisplayName("MANAGER can create project; DEVELOPER cannot")
    void managerCanCreateProject_developerCannot() {
        String mgr = "mgr_integ" + System.currentTimeMillis();
        String dev = "dev_integ" + System.currentTimeMillis();
        String pwd = "P@ssw0rd123";

        registerUser(mgr, pwd, "MANAGER");
        registerUser(dev, pwd, "DEVELOPER");

        String mgrToken = loginAndGetToken(mgr, pwd);
        String devToken = loginAndGetToken(dev, pwd);

        System.out.println("MGR_TOKEN=" + mgrToken);
        System.out.println("DEV_TOKEN=" + devToken);

        Map<String, Object> projectBody = Map.of(
                "name", "Project Integration",
                "description", "Created by integration test",
                "startDate", LocalDate.now().toString(),
                "endDate", LocalDate.now().plusDays(7).toString()
        );

        // Manager creates project -> 201
        String projectId = given()
                .header("Authorization", "Bearer " + mgrToken)
                .contentType("application/json")
                .body(projectBody)
                .when()
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Developer attempts to create project -> expect 403, but currently failing with 401
        Response devResp = given()
                .header("Authorization", "Bearer " + devToken)
                .contentType("application/json")
                .body(projectBody)
                .when()
                .post("/projects");

        System.out.println("Developer create project response code=" + devResp.getStatusCode());
        System.out.println("Developer create project body=" + devResp.getBody().asString());

        assertEquals(403, devResp.getStatusCode(), "Developer should receive 403 Forbidden when creating project but got " + devResp.getStatusCode());

        // Both can list projects
        given()
                .header("Authorization", "Bearer " + mgrToken)
                .when().get("/projects")
                .then().statusCode(200);

        given()
                .header("Authorization", "Bearer " + devToken)
                .when().get("/projects")
                .then().statusCode(200);

        // Anonymous cannot
        given()
                .when().get("/projects")
                .then().statusCode(401);
    }

    @Test
    @DisplayName("Both roles can create tasks and link to project")
    void bothCanCreateTasks() {
        String mgr = "mgr_task" + System.currentTimeMillis();
        String dev = "dev_task" + System.currentTimeMillis();
        String pwd = "P@ssw0rd123";

        registerUser(mgr, pwd, "MANAGER");
        registerUser(dev, pwd, "DEVELOPER");

        String mgrToken = loginAndGetToken(mgr, pwd);
        String devToken = loginAndGetToken(dev, pwd);

        Map<String, Object> projectBody = Map.of(
                "name", "Project For Tasks",
                "description", "Project for task tests",
                "startDate", LocalDate.now().toString(),
                "endDate", LocalDate.now().plusDays(7).toString()
        );

        String projectId = given()
                .header("Authorization", "Bearer " + mgrToken)
                .contentType("application/json")
                .body(projectBody)
                .when()
                .post("/projects")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        Map<String, Object> taskBody = Map.of(
                "title", "Integration Task",
                "description", "Task created in integration test",
                "status", "TODO",
                "priority", "HIGH",
                "dueDate", LocalDate.now().plusDays(3).toString(),
                "projectId", projectId
        );

        // Developer creates task
        given()
                .header("Authorization", "Bearer " + devToken)
                .contentType("application/json")
                .body(taskBody)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201);

        // Manager creates task
        given()
                .header("Authorization", "Bearer " + mgrToken)
                .contentType("application/json")
                .body(taskBody)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201);
    }
}
