package dev.matheuslf.desafio.inscritos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectResponse;
import dev.matheuslf.desafio.inscritos.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.MediaTypes; // Import adicionado
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    private final UUID projectId = UUID.randomUUID();
    private ProjectRequest validRequest;
    private ProjectResponse expectedResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        validRequest = new ProjectRequest(
                "Novo Projeto API",
                "Descrição do Projeto",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10)
        );

        expectedResponse = new ProjectResponse(
                projectId,
                validRequest.name(),
                validRequest.description(),
                validRequest.startDate(),
                validRequest.endDate()
        );
    }

    @Test
    @DisplayName("POST /projects: Deve criar um projeto e retornar status 201 CREATED com links HATEOAS")
    void create_ShouldReturn201Created_WhenProjectIsValid() throws Exception {
        // Arrange
        when(projectService.create(any(ProjectRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON)) // Alterado para HAL+JSON
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value(validRequest.name()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/projects/" + projectId))
                .andExpect(jsonPath("$._links.all-projects.href").exists())
                .andExpect(jsonPath("$._links.all-projects.href").value("http://localhost/projects"));
        verify(projectService, times(1)).create(any(ProjectRequest.class));
    }

    @Test
    @DisplayName("POST /projects: Deve retornar 400 BAD REQUEST e corpo de erro padronizado")
    void create_ShouldReturn400BadRequest_WhenServiceThrowsIllegalArgumentException() throws Exception {
        // Arrange
        String errorMessage = "A data final não pode ser anterior à inicial.";
        doThrow(new IllegalArgumentException(errorMessage))
                .when(projectService).create(any(ProjectRequest.class));

        // Act & Assert
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/projects"));
        verify(projectService, times(1)).create(any(ProjectRequest.class));
    }

    @Test
    @DisplayName("GET /projects/{id}: Deve retornar projeto e status 200 OK com links HATEOAS quando encontrado")
    void findById_ShouldReturnProjectAnd200Ok_WhenFound() throws Exception {
        // Arrange
        when(projectService.findById(projectId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON)) // Alterado para HAL+JSON
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value(validRequest.name()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/projects/" + projectId))
                .andExpect(jsonPath("$._links.all-projects.href").exists())
                .andExpect(jsonPath("$._links.all-projects.href").value("http://localhost/projects"));
        verify(projectService, times(1)).findById(projectId);
    }

    @Test
    @DisplayName("GET /projects/{id}: Deve retornar 404 NOT FOUND e corpo de erro padronizado")
    void findById_ShouldReturn404NotFound_WhenNotFound() throws Exception {
        // Arrange
        String errorMessage = "Projeto não encontrado";
        when(projectService.findById(projectId)).thenThrow(new NoSuchElementException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/projects/" + projectId));
    }

    @Test
    @DisplayName("GET /projects: Deve retornar lista de projetos e status 200 OK com links HATEOAS")
    void findAll_ShouldReturnListOfProjectsAnd200Ok() throws Exception {
        // Arrange
        var projectList = List.of(expectedResponse);
        when(projectService.findAll()).thenReturn(projectList);

        // Act & Assert
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON)) // Alterado para HAL+JSON
                .andExpect(jsonPath("$._embedded.projectResponseList[0].id").value(projectId.toString()))
                .andExpect(jsonPath("$._embedded.projectResponseList[0].name").value(validRequest.name()))
                .andExpect(jsonPath("$._embedded.projectResponseList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._embedded.projectResponseList[0]._links.self.href").value("http://localhost/projects/" + projectId))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/projects"));
        verify(projectService, times(1)).findAll();
    }

    @Test
    @DisplayName("PUT /projects/{id}: Deve atualizar e retornar status 200 OK com links HATEOAS quando o projeto é encontrado")
    void update_ShouldReturn200Ok_WhenFound() throws Exception {
        // Arrange
        var updateRequest = new ProjectRequest(
                "Projeto Atualizado",
                "Descrição Atualizada",
                LocalDate.now(),
                LocalDate.now().plusDays(5));
        var updateResponse = new ProjectResponse(
                projectId,
                updateRequest.name(),
                updateRequest.description(),
                updateRequest.startDate(),
                updateRequest.endDate());
        when(projectService.update(eq(projectId), any(ProjectRequest.class))).thenReturn(updateResponse);

        // Act & Assert
        mockMvc.perform(put("/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON)) // Alterado para HAL+JSON
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value(updateRequest.name()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/projects/" + projectId))
                .andExpect(jsonPath("$._links.all-projects.href").exists())
                .andExpect(jsonPath("$._links.all-projects.href").value("http://localhost/projects"));
        verify(projectService, times(1)).update(eq(projectId), any(ProjectRequest.class));
    }

    @Test
    @DisplayName("PUT /projects/{id}: Deve retornar 404 NOT FOUND ao tentar atualizar ID inexistente")
    void update_ShouldReturn404NotFound_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Projeto não encontrado para o ID de atualização: " + projectId))
                .when(projectService).update(eq(projectId), any(ProjectRequest.class));

        // Act & Assert
        mockMvc.perform(put("/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Projeto não encontrado para o ID de atualização: " + projectId))
                .andExpect(jsonPath("$.path").value("/projects/" + projectId));
        verify(projectService, times(1)).update(eq(projectId), any(ProjectRequest.class));
    }

    @Test
    @DisplayName("DELETE /projects/{id}: Deve deletar o projeto e retornar status 204 NO CONTENT")
    void delete_ShouldReturn204NoContent_WhenFound() throws Exception {
        // Assert
        doNothing().when(projectService).delete(projectId);

        // Act & Assert
        mockMvc.perform(delete("/projects/{id}", projectId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(projectService, times(1)).delete(projectId);
    }

    @Test
    @DisplayName("DELETE /projects/{id}: Deve retornar 404 NOT FOUND ao tentar deletar ID inexistente")
    void delete_ShouldReturn404NotFound_WhenNotFound() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Projeto não encontrado para exclusão."))
                .when(projectService).delete(projectId);

        // Act & Assert
        mockMvc.perform(delete("/projects/{id}", projectId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Projeto não encontrado para exclusão."))
                .andExpect(jsonPath("$.path").value("/projects/" + projectId));
        verify(projectService, times(1)).delete(projectId);
    }
}
