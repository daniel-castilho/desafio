package dev.matheuslf.desafio.inscritos.controller.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.matheuslf.desafio.inscritos.controller.TaskController;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskResponse;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskStatusUpdateRequest;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskPriority;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;
import dev.matheuslf.desafio.inscritos.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors; // Importação da classe

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    private final UUID taskId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();
    private TaskRequest validRequest;
    private TaskResponse expectedResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Configura MockMvc para aplicar os filtros de segurança
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        validRequest = new TaskRequest(
                "Implementar Controller",
                "Escrever testes de integração para a TaskController",
                TaskStatus.DOING,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(2),
                projectId
        );

        expectedResponse = new TaskResponse(
                taskId,
                validRequest.title(),
                validRequest.description(),
                validRequest.status().name(),
                validRequest.priority().name(),
                validRequest.dueDate(),
                validRequest.projectId()
        );
    }

    // --- Testes para DEVELOPER (acesso total em /tasks) ---

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("POST /tasks: Deve criar uma tarefa e retornar status 201 CREATED com links HATEOAS para DEVELOPER")
    void create_ShouldReturn201Created_WhenTaskIsValid_AsDeveloper() throws Exception {
        // Arrange
        when(taskService.create(any(TaskRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value(validRequest.title()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.all-tasks.href").exists());
        verify(taskService, times(1)).create(any(TaskRequest.class));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("POST /tasks: Deve retornar 400 BAD REQUEST e corpo de erro padronizado para DEVELOPER")
    void create_ShouldReturn400BadRequest_WhenServiceThrowsException_AsDeveloper() throws Exception {
        // Arrange
        String errorMessage = "Dados inválidos";
        doThrow(new IllegalArgumentException(errorMessage))
                .when(taskService).create(any(TaskRequest.class));

        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/tasks"));
        verify(taskService, times(1)).create(any(TaskRequest.class));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("GET /tasks/{id}: Deve retornar tarefa e status 200 OK com links HATEOAS quando encontrada para DEVELOPER")
    void findById_ShouldReturnTaskAnd200Ok_WhenFound_AsDeveloper() throws Exception {
        // Arrange
        when(taskService.findById(taskId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value(validRequest.title()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.all-tasks.href").exists());
        verify(taskService, times(1)).findById(taskId);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("GET /tasks/{id}: Deve retornar 404 NOT FOUND e corpo de erro padronizado para DEVELOPER")
    void findById_ShouldReturn404NotFound_WhenNotFound_AsDeveloper() throws Exception {
        // Arrange
        String errorMessage = "Tarefa não encontrada";
        when(taskService.findById(taskId)).thenThrow(new NoSuchElementException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/tasks/" + taskId));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("GET /tasks: Deve retornar lista de tarefas e status 200 OK com links HATEOAS para DEVELOPER")
    void findAllWithoutFilters_ShouldReturnTasksAnd200Ok_AsDeveloper() throws Exception {
        // Arrange
        var taskList = Collections.singletonList(expectedResponse);
        when(taskService.findAllWithFilters(any(), any(), any())).thenReturn(taskList);

        // Act & Assert
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.taskResponseList[0].id").value(taskId.toString()))
                .andExpect(jsonPath("$._embedded.taskResponseList[0].title").value(validRequest.title()))
                .andExpect(jsonPath("$._embedded.taskResponseList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
        verify(taskService, times(1)).findAllWithFilters(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("PUT /tasks/{id}: Deve atualizar a tarefa e retornar status 200 OK com links HATEOAS para DEVELOPER")
    void update_ShouldReturn200Ok_WhenFound_AsDeveloper() throws Exception {
        // Arrange
        var updateRequest = new TaskRequest(
                "Tarefa Atualizada",
                "Descrição da Tarefa Atualizada",
                TaskStatus.DOING,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(2),
                projectId);
        var updateResponse = new TaskResponse(
                taskId,
                updateRequest.title(),
                updateRequest.description(),
                updateRequest.status().name(),
                updateRequest.priority().name(),
                updateRequest.dueDate(),
                updateRequest.projectId());
        when(taskService.update(eq(taskId), any(TaskRequest.class))).thenReturn(updateResponse);

        // Act & Assert
        mockMvc.perform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value(updateRequest.title()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.all-tasks.href").exists());
        verify(taskService, times(1)).update(eq(taskId), any(TaskRequest.class));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("PATCH /tasks/{id}/status: Deve atualizar apenas o status e retornar status 200 OK com links HATEOAS para DEVELOPER")
    void updateStatus_ShouldReturn200Ok_WhenFound_AsDeveloper() throws Exception {
        // Arrange
        var newStatus = TaskStatus.DONE;
        var statusUpdateRequest = new TaskStatusUpdateRequest(newStatus);

        var responseAfterUpdate = new TaskResponse(
                taskId, validRequest.title(), validRequest.description(),
                newStatus.name(), validRequest.priority().name(), validRequest.dueDate(), projectId);

        when(taskService.updateStatus(eq(taskId), any(TaskStatusUpdateRequest.class))).thenReturn(responseAfterUpdate);

        // Act & Assert
        mockMvc.perform(patch("/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.status").value(newStatus.name()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.all-tasks.href").exists());

        verify(taskService, times(1)).updateStatus(eq(taskId), any(TaskStatusUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("DELETE /tasks/{id}: Deve deletar a tarefa e retornar status 204 NO CONTENT para DEVELOPER")
    void delete_ShouldReturn204NoContent_WhenFound_AsDeveloper() throws Exception {
        // Arrange
        doNothing().when(taskService).delete(taskId);

        // Act & Assert
        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(taskService, times(1)).delete(taskId);
    }

    // --- Testes para usuários não autenticados ---

    @Test
    @DisplayName("POST /tasks: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void create_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(taskService, times(0)).create(any(TaskRequest.class));
    }

    @Test
    @DisplayName("GET /tasks/{id}: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void findById_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(get("/tasks/{id}", taskId).with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(taskService, times(0)).findById(taskId);
    }

    @Test
    @DisplayName("GET /tasks: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void findAllWithoutFilters_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(get("/tasks").with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(taskService, times(0)).findAllWithFilters(any(), any(), any());
    }

    @Test
    @DisplayName("PUT /tasks/{id}: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void update_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(taskService, times(0)).update(eq(taskId), any(TaskRequest.class));
    }

    @Test
    @DisplayName("PATCH /tasks/{id}/status: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void updateStatus_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(patch("/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskStatusUpdateRequest(TaskStatus.DONE)))
                        .with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(taskService, times(0)).updateStatus(eq(taskId), any(TaskStatusUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /tasks/{id}: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void delete_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", taskId).with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(taskService, times(0)).delete(taskId);
    }
}
