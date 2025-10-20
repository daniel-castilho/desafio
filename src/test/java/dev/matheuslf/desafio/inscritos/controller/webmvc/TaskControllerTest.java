package dev.matheuslf.desafio.inscritos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    @DisplayName("POST /tasks: Deve criar uma tarefa e retornar status 201 CREATED com links HATEOAS")
    void create_ShouldReturn201Created_WhenTaskIsValid() throws Exception {
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
    @DisplayName("POST /tasks: Deve retornar 400 BAD REQUEST e corpo de erro padronizado")
    void create_ShouldReturn400BadRequest_WhenServiceThrowsException() throws Exception {
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
    @DisplayName("GET /tasks/{id}: Deve retornar tarefa e status 200 OK com links HATEOAS quando encontrada")
    void findById_ShouldReturnTaskAnd200Ok_WhenFound() throws Exception {
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
    @DisplayName("GET /tasks/{id}: Deve retornar 404 NOT FOUND e corpo de erro padronizado")
    void findById_ShouldReturn404NotFound_WhenNotFound() throws Exception {
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
    @DisplayName("GET /tasks: Deve retornar lista de tarefas e status 200 OK com links HATEOAS")
    void findAllWithoutFilters_ShouldReturnTasksAnd200Ok() throws Exception {
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
    @DisplayName("PUT /tasks/{id}: Deve atualizar a tarefa e retornar status 200 OK com links HATEOAS")
    void update_ShouldReturn200Ok_WhenFound() throws Exception {
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
    @DisplayName("PUT /tasks/{id}: Deve retornar 404 NOT FOUND e corpo de erro padronizado ao tentar atualizar ID inexistente")
    void update_ShouldReturn404NotFound_WhenNotFound() throws Exception {
        // Arrange
        String errorMessage = "Tarefa não encontrada";
        doThrow(new NoSuchElementException(errorMessage))
                .when(taskService).update(eq(taskId), any(TaskRequest.class));

        // Act & Assert
        mockMvc.perform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/tasks/" + taskId));
        verify(taskService, times(1)).update(eq(taskId), any(TaskRequest.class));
    }

    @Test
    @DisplayName("PATCH /tasks/{id}/status: Deve atualizar apenas o status e retornar status 200 OK com links HATEOAS")
    void updateStatus_ShouldReturn200Ok_WhenFound() throws Exception {
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
    @DisplayName("PATCH /tasks/{id}/status: Deve retornar 400 BAD REQUEST ao tentar alterar status de tarefa concluída")
    void updateStatus_ShouldReturn400BadRequest_WhenTaskIsAlreadyDone() throws Exception {
        // Arrange
        String errorMessage = "Não é possível alterar o status de uma tarefa já concluída.";
        var statusUpdateRequest = new TaskStatusUpdateRequest(TaskStatus.DOING);

        when(taskService.updateStatus(eq(taskId), any(TaskStatusUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(patch("/tasks/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/tasks/" + taskId + "/status"));

        verify(taskService, times(1)).updateStatus(eq(taskId), any(TaskStatusUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /tasks/{id}: Deve deletar a tarefa e retornar status 204 NO CONTENT")
    void delete_ShouldReturn204NoContent_WhenFound() throws Exception {
        // Arrange
        doNothing().when(taskService).delete(taskId);

        // Act & Assert
        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(taskService, times(1)).delete(taskId);
    }

    @Test
    @DisplayName("DELETE /tasks/{id}: Deve retornar 404 NOT FOUND e corpo de erro padronizado ao tentar deletar ID inexistente")
    void delete_ShouldReturn404NotFound_WhenNotFound() throws Exception {
        // Arrange
        String errorMessage = "Tarefa não encontrada";
        doThrow(new NoSuchElementException(errorMessage))
                .when(taskService).delete(taskId);

        // Act & Assert
        mockMvc.perform(delete("/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/tasks/" + taskId));
        verify(taskService, times(1)).delete(taskId);
    }
}
