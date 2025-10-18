package dev.matheuslf.desafio.inscritos.services;

import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskRequest;
import dev.matheuslf.desafio.inscritos.controller.dto.task.TaskResponse;
import dev.matheuslf.desafio.inscritos.domain.entities.Project;
import dev.matheuslf.desafio.inscritos.domain.entities.Task;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskPriority;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;
import dev.matheuslf.desafio.inscritos.mapper.TaskMapper;
import dev.matheuslf.desafio.inscritos.repository.ProjectRepository;
import dev.matheuslf.desafio.inscritos.repository.TaskRepository;
import dev.matheuslf.desafio.inscritos.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskMapper taskMapper;

    private UUID taskId;
    private UUID projectId;
    private TaskRequest validRequest;
    private Project mockProject;
    private Task mockTaskEntity;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        // 1. Mock do Projeto Pai
        mockProject = new Project();
        mockProject.setId(projectId);
        mockProject.setName("Parent Project");

        // 2. Mock da Entidade Tarefa
        mockTaskEntity = new Task();
        mockTaskEntity.setId(taskId);
        mockTaskEntity.setTitle("Test Task");
        mockTaskEntity.setPriority(TaskPriority.MEDIUM);
        mockTaskEntity.setStatus(TaskStatus.TODO);
        mockTaskEntity.setProject(mockProject);

        // 3. Mock do DTO de Requisição
        validRequest = new TaskRequest(
                "New Task Title",
                "Detailed description",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(5),
                projectId
        );
    }

    @Test
    @DisplayName("Deve criar uma tarefa com sucesso e vincular ao projeto existente")
    void create_ShouldSaveTaskAndReturnResponse_WhenProjectExists() {
        // Arrange
        Task taskToSave = new Task();
        TaskResponse expectedResponse = new TaskResponse(
                taskId, validRequest.title(), validRequest.description(),
                validRequest.status().toString(), validRequest.priority().toString(),
                validRequest.dueDate(),
                projectId
        );

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(taskMapper.toEntity(validRequest)).thenReturn(taskToSave);
        when(taskRepository.save(taskToSave)).thenReturn(mockTaskEntity);
        when(taskMapper.toResponse(mockTaskEntity)).thenReturn(expectedResponse);

        // Act
        TaskResponse actualResponse = taskService.create(validRequest);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(taskId, actualResponse.id());
        assertEquals(mockProject, taskToSave.getProject());
        verify(projectRepository, times(1)).findById(projectId);
        verify(taskRepository, times(1)).save(taskToSave);
    }

    @Test
    @DisplayName("Deve lançar NoSuchElementException se o projeto pai não for encontrado")
    void create_ShouldThrowNoSuchElementException_WhenProjectNotFound() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> taskService.create(validRequest),
                "Deve lançar exceção se o projeto pai não existir.");
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).toEntity(any(TaskRequest.class));
    }

    @Test
    @DisplayName("Deve retornar TaskResponse quando a tarefa é encontrada")
    void findById_ShouldReturnTaskResponse_WhenFound() {
        // Arrange
        TaskResponse expectedResponse = new TaskResponse(
                taskId, "Test Task", null, TaskStatus.TODO.toString(),
                TaskPriority.MEDIUM.toString(), null, projectId
        );

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(mockTaskEntity));
        when(taskMapper.toResponse(mockTaskEntity)).thenReturn(expectedResponse);

        // Act
        TaskResponse actualResponse = taskService.findById(taskId);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(taskId, actualResponse.id());
    }

    @Test
    @DisplayName("Deve lançar NoSuchElementException quando a tarefa não é encontrada")
    void findById_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> taskService.findById(taskId),
                "Esperava que NoSuchElementException fosse lançada para ID inexistente.");
        verify(taskMapper, never()).toResponse(any(Task.class));
    }

    @Test
    @DisplayName("Deve atualizar uma tarefa existente e retornar TaskResponse")
    void update_ShouldUpdateTaskAndReturnResponse_WhenFound() {
        // Arrange
        UUID taskIdToUpdate = mockTaskEntity.getId();

        TaskRequest updateRequest = new TaskRequest(
                "Updated Title",
                "New description",
                TaskStatus.DOING,
                TaskPriority.LOW,
                LocalDate.now().plusDays(20),
                projectId
        );

        TaskResponse expectedResponse = new TaskResponse(
                taskIdToUpdate, updateRequest.title(), updateRequest.description(),
                updateRequest.status().toString(), updateRequest.priority().toString(),
                updateRequest.dueDate(),
                projectId
        );

        when(taskRepository.findById(taskIdToUpdate)).thenReturn(Optional.of(mockTaskEntity));

        doNothing().when(taskMapper).updateFromDto(eq(updateRequest), any(Task.class));
        when(taskRepository.save(mockTaskEntity)).thenReturn(mockTaskEntity);
        when(taskMapper.toResponse(mockTaskEntity)).thenReturn(expectedResponse);

        // Act
        TaskResponse actualResponse = taskService.update(taskIdToUpdate, updateRequest);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(updateRequest.title(), actualResponse.title());
        assertEquals(updateRequest.status().toString(), actualResponse.status());
        verify(taskRepository, times(1)).findById(taskIdToUpdate);
        verify(taskMapper, times(1)).updateFromDto(eq(updateRequest), eq(mockTaskEntity)); // Verifica o mapeamento de atualização
        verify(taskRepository, times(1)).save(mockTaskEntity);
    }

    @Test
    @DisplayName("Deve lançar NoSuchElementException ao tentar atualizar uma tarefa inexistente")
    void update_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(taskRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> taskService.update(nonExistentId, validRequest),
                "Deve lançar exceção se a tarefa não for encontrada.");
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).updateFromDto(any(TaskRequest.class), any(Task.class));
    }

    @Test
    @DisplayName("Deve deletar uma tarefa existente com sucesso")
    void delete_ShouldDeleteTask_WhenFound() {
        // Arrange
        when(taskRepository.existsById(taskId)).thenReturn(true);

        // Act
        taskService.delete(taskId);

        // Assert
        verify(taskRepository, times(1)).existsById(taskId);
        verify(taskRepository, times(1)).deleteById(taskId);
        verify(taskRepository, never()).findById(any(UUID.class));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("Deve lançar NoSuchElementException ao tentar deletar uma tarefa inexistente")
    void delete_ShouldThrowNoSuchElementException_WhenNotFound() {
        // Arrange
        Mockito.lenient().when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> taskService.delete(taskId),
                "Esperava que NoSuchElementException fosse lançada para ID inexistente.");
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
