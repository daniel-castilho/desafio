package dev.matheuslf.desafio.inscritos.services;

import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.project.ProjectResponse;
import dev.matheuslf.desafio.inscritos.domain.entities.Project;
import dev.matheuslf.desafio.inscritos.mapper.ProjectMapper;
import dev.matheuslf.desafio.inscritos.repository.ProjectRepository;
import dev.matheuslf.desafio.inscritos.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    private UUID projectId;
    private Project projectEntity;
    private ProjectRequest validRequest;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        projectEntity = new Project();
        projectEntity.setId(projectId);
        projectEntity.setName("Project Test");
        projectEntity.setStartDate(LocalDate.now());

        validRequest = new ProjectRequest(
                "New Project",
                "Description",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10)
        );
    }

    @Test
    @DisplayName("Deve criar um projeto com sucesso quando os dados são válidos")
    void create_ShouldReturnProjectResponse_WhenDataIsValid() {
        // Arrange
        Project projectToSave = new Project();
        projectToSave.setName(validRequest.name());
        Project savedProject = projectEntity;
        ProjectResponse expectedResponse = new ProjectResponse(
                projectId,
                savedProject.getName(),
                savedProject.getDescription(),
                savedProject.getStartDate(),
                savedProject.getEndDate()
        );

        when(projectRepository.existsByName(validRequest.name())).thenReturn(false);
        when(projectMapper.toEntity(validRequest)).thenReturn(projectToSave);
        when(projectRepository.save(projectToSave)).thenReturn(savedProject);
        when(projectMapper.toResponse(savedProject)).thenReturn(expectedResponse);

        // Act
        ProjectResponse actualResponse = projectService.create(validRequest);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(projectId, actualResponse.id());
        verify(projectRepository, times(1)).save(projectToSave);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se a data final for anterior à inicial")
    void create_ShouldThrowIllegalArgumentException_WhenEndDateIsBeforeStartDate() {
        // Arrange
        ProjectRequest invalidRequest = new ProjectRequest(
                "Invalid Project",
                "Description",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(1)
        );
        when(projectRepository.existsByName(invalidRequest.name())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> projectService.create(invalidRequest),
                "A data de término não pode ser anterior à data de início do projeto.");
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se o nome do projeto já existir")
    void create_ShouldThrowIllegalArgumentException_WhenNameIsDuplicate() {
        // Arrange
        when(projectRepository.existsByName(validRequest.name())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> projectService.create(validRequest),
                "Já existe um projeto com o nome: " + validRequest.name());
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve retornar ProjectResponse quando o projeto é encontrado")
    void findById_ShouldReturnProjectResponse_WhenFound() {
        // Arrange
        ProjectResponse expectedResponse = new ProjectResponse(
                projectId, "Project Test", null, LocalDate.now(), null
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(projectMapper.toResponse(projectEntity)).thenReturn(expectedResponse);

        // Act
        ProjectResponse actualResponse = projectService.findById(projectId);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(projectId, actualResponse.id());
    }

    @Test
    @DisplayName("Deve lançar NoSuchElementException quando o projeto não é encontrado")
    void findById_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> projectService.findById(projectId),
                "Esperava que NoSuchElementException fosse lançada para ID inexistente.");
        verify(projectMapper, never()).toResponse(any(Project.class));
    }

    @Test
    @DisplayName("Deve retornar uma lista de ProjectResponse em findAll")
    void findAll_ShouldReturnListOfProjectResponse() {
        // Arrange
        List<Project> projectList = List.of(projectEntity);
        ProjectResponse expectedResponseForList = new ProjectResponse(
                projectId, "Project Test", null, LocalDate.now(), null
        );
        List<ProjectResponse> expectedList = List.of(expectedResponseForList);
        when(projectRepository.findAll()).thenReturn(projectList);
        when(projectMapper.toResponse(projectEntity)).thenReturn(expectedResponseForList);

        // Act
        List<ProjectResponse> actualList = projectService.findAll();

        // Assert
        assertNotNull(actualList);
        assertEquals(expectedList.size(), actualList.size());
        verify(projectRepository, times(1)).findAll();
        verify(projectMapper, times(1)).toResponse(projectEntity);
    }

    @Test
    @DisplayName("Deve atualizar um projeto existente e retornar ProjectResponse")
    void update_ShouldUpdateProjectAndReturnResponse_WhenFound() {
        // Arrange
        ProjectRequest updateRequest = new ProjectRequest(
                "Updated Project Name",
                "New Description",
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(15)
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.existsByName(updateRequest.name())).thenReturn(false);
        doNothing().when(projectMapper).updateFromDto(eq(updateRequest), eq(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        ProjectResponse updatedResponse = new ProjectResponse(
                projectId, updateRequest.name(), updateRequest.description(), updateRequest.startDate(), updateRequest.endDate()
        );
        when(projectMapper.toResponse(projectEntity)).thenReturn(updatedResponse);

        // Act
        ProjectResponse actualResponse = projectService.update(projectId, updateRequest);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(updateRequest.name(), actualResponse.name());
        verify(projectRepository, times(1)).findById(projectId);
        verify(projectMapper, times(1)).updateFromDto(eq(updateRequest), eq(projectEntity));
        verify(projectRepository, times(1)).save(projectEntity);
    }

    @Test
    @DisplayName("Deve lançar NoSuchElementException ao tentar atualizar um projeto inexistente")
    void update_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(projectRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> projectService.update(nonExistentId, validRequest),
                "Deve lançar exceção se o projeto não for encontrado para atualização.");
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectMapper, never()).updateFromDto(any(ProjectRequest.class), any(Project.class));
    }

    @Test
    @DisplayName("Deve deletar um projeto existente com sucesso (usando existsById)")
    void delete_ShouldDeleteProject_WhenFound() {
        // Arrange
        when(projectRepository.existsById(projectId)).thenReturn(true);

        // Act
        projectService.delete(projectId);

        // Assert
        verify(projectRepository, times(1)).existsById(projectId);
        verify(projectRepository, times(1)).deleteById(projectId);
    }

    @Test
    @DisplayName("Deve lançar NoSuchElementException ao tentar deletar um projeto inexistente")
    void delete_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        Mockito.lenient().when(projectRepository.existsById(projectId)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> projectService.delete(projectId),
                "Deve lançar exceção se o projeto a ser deletado não existir.");

        // Assert
        verify(projectRepository, times(1)).existsById(projectId);
        verify(projectRepository, never()).deleteById(any(UUID.class));
    }
}
