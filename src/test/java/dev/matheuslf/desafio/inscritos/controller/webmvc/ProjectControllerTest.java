package dev.matheuslf.desafio.inscritos.controller.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectResponse;
import dev.matheuslf.desafio.inscritos.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

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

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

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

    // --- Testes para MANAGER (acesso total) ---

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("POST /projects: Deve criar um projeto e retornar status 201 CREATED com links HATEOAS")
    void create_ShouldReturn201Created_WhenProjectIsValid_AsManager() throws Exception {
        // Arrange
        when(projectService.create(any(ProjectRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value(validRequest.name()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/projects/" + projectId))
                .andExpect(jsonPath("$._links.all-projects.href").exists())
                .andExpect(jsonPath("$._links.all-projects.href").value("http://localhost/projects"));
        verify(projectService, times(1)).create(any(ProjectRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("POST /projects: Deve retornar 400 BAD REQUEST quando a data de término for anterior à de início")
    void create_ShouldReturn400BadRequest_WhenEndDateIsBeforeStartDate_AsManager() throws Exception {
        // Arrange
        String errorMessage = "A data de término não pode ser anterior à data de início do projeto.";
        var invalidRequest = new ProjectRequest(
                "Projeto com Data Inválida",
                "Descrição",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(1)
        );

        when(projectService.create(any(ProjectRequest.class))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        verify(projectService, times(1)).create(any(ProjectRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("POST /projects: Deve retornar 400 BAD REQUEST quando o nome do projeto já existir")
    void create_ShouldReturn400BadRequest_WhenNameIsDuplicate_AsManager() throws Exception {
        // Arrange
        String errorMessage = "Já existe um projeto com o nome: " + validRequest.name();
        when(projectService.create(any(ProjectRequest.class))).thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage));

        verify(projectService, times(1)).create(any(ProjectRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("GET /projects/{id}: Deve retornar projeto e status 200 OK com links HATEOAS quando encontrado")
    void findById_ShouldReturnProjectAnd200Ok_WhenFound_AsManager() throws Exception {
        // Arrange
        when(projectService.findById(projectId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value(validRequest.name()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/projects/" + projectId))
                .andExpect(jsonPath("$._links.all-projects.href").exists())
                .andExpect(jsonPath("$._links.all-projects.href").value("http://localhost/projects"));
        verify(projectService, times(1)).findById(projectId);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("GET /projects/{id}: Deve retornar 404 NOT FOUND e corpo de erro padronizado")
    void findById_ShouldReturn404NotFound_WhenNotFound_AsManager() throws Exception {
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
    @WithMockUser(roles = "MANAGER")
    @DisplayName("GET /projects: Deve retornar lista de projetos e status 200 OK com links HATEOAS")
    void findAll_ShouldReturnListOfProjectsAnd200Ok_AsManager() throws Exception {
        // Arrange
        var projectList = List.of(expectedResponse);
        when(projectService.findAll()).thenReturn(projectList);

        // Act & Assert
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.projectResponseList[0].id").value(projectId.toString()))
                .andExpect(jsonPath("$._embedded.projectResponseList[0].name").value(validRequest.name()))
                .andExpect(jsonPath("$._embedded.projectResponseList[0]._links.self.href").value("http://localhost/projects/" + projectId))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/projects"))
                .andExpect(jsonPath("$._links.all-projects.href").exists())
                .andExpect(jsonPath("$._links.all-projects.href").value("http://localhost/projects"));
        verify(projectService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("PUT /projects/{id}: Deve atualizar e retornar status 200 OK com links HATEOAS quando o projeto é encontrado")
    void update_ShouldReturn200Ok_WhenFound_AsManager() throws Exception {
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
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value(updateRequest.name()))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/projects/" + projectId))
                .andExpect(jsonPath("$._links.all-projects.href").exists())
                .andExpect(jsonPath("$._links.all-projects.href").value("http://localhost/projects"));
        verify(projectService, times(1)).update(eq(projectId), any(ProjectRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("PUT /projects/{id}: Deve retornar 404 NOT FOUND ao tentar atualizar ID inexistente")
    void update_ShouldReturn404NotFound_WhenNotFound_AsManager() throws Exception {
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
    @WithMockUser(roles = "MANAGER")
    @DisplayName("DELETE /projects/{id}: Deve deletar o projeto e retornar status 204 NO CONTENT")
    void delete_ShouldReturn204NoContent_WhenFound_AsManager() throws Exception {
        // Assert
        doNothing().when(projectService).delete(projectId);

        // Act & Assert
        mockMvc.perform(delete("/projects/{id}", projectId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(projectService, times(1)).delete(projectId);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("DELETE /projects/{id}: Deve retornar 404 NOT FOUND ao tentar deletar ID inexistente")
    void delete_ShouldReturn404NotFound_WhenNotFound_AsManager() throws Exception {
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

    // --- Testes para DEVELOPER (apenas leitura em /projects) ---

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("GET /projects: Deve retornar lista de projetos e status 200 OK com links HATEOAS para DEVELOPER")
    void findAll_ShouldReturnListOfProjectsAnd200Ok_AsDeveloper() throws Exception {
        // Arrange
        var projectList = List.of(expectedResponse);
        when(projectService.findAll()).thenReturn(projectList);

        // Act & Assert
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._embedded.projectResponseList[0].id").value(projectId.toString()));
        verify(projectService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("GET /projects/{id}: Deve retornar projeto e status 200 OK com links HATEOAS para DEVELOPER")
    void findById_ShouldReturnProjectAnd200Ok_WhenFound_AsDeveloper() throws Exception {
        // Arrange
        when(projectService.findById(projectId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id").value(projectId.toString()));
        verify(projectService, times(1)).findById(projectId);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("POST /projects: Deve retornar 403 FORBIDDEN para DEVELOPER")
    void create_ShouldReturn403Forbidden_AsDeveloper() throws Exception {
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
        verify(projectService, times(0)).create(any(ProjectRequest.class));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("PUT /projects/{id}: Deve retornar 403 FORBIDDEN para DEVELOPER")
    void update_ShouldReturn403Forbidden_AsDeveloper() throws Exception {
        mockMvc.perform(put("/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
        verify(projectService, times(0)).update(eq(projectId), any(ProjectRequest.class));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER")
    @DisplayName("DELETE /projects/{id}: Deve retornar 403 FORBIDDEN para DEVELOPER")
    void delete_ShouldReturn403Forbidden_AsDeveloper() throws Exception {
        mockMvc.perform(delete("/projects/{id}", projectId))
                .andExpect(status().isForbidden());
        verify(projectService, times(0)).delete(projectId);
    }

    // --- Testes para usuários não autenticados ---

    @Test
    @DisplayName("GET /projects: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void findAll_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(get("/projects").with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(projectService, times(0)).findAll();
    }

    @Test
    @DisplayName("GET /projects/{id}: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void findById_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(get("/projects/{id}", projectId).with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(projectService, times(0)).findById(projectId);
    }

    @Test
    @DisplayName("POST /projects: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void create_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(projectService, times(0)).create(any(ProjectRequest.class));
    }

    @Test
    @DisplayName("PUT /projects/{id}: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void update_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(put("/projects/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(projectService, times(0)).update(eq(projectId), any(ProjectRequest.class));
    }

    @Test
    @DisplayName("DELETE /projects/{id}: Deve retornar 401 UNAUTHORIZED para usuário não autenticado")
    void delete_ShouldReturn401Unauthorized_AsUnauthenticated() throws Exception {
        mockMvc.perform(delete("/projects/{id}", projectId).with(SecurityMockMvcRequestPostProcessors.anonymous())) // Alterado
                .andExpect(status().isUnauthorized());
        verify(projectService, times(0)).delete(projectId);
    }
}
