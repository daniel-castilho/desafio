package dev.matheuslf.desafio.inscritos.repository;

import dev.matheuslf.desafio.inscritos.domain.entities.Project;
import dev.matheuslf.desafio.inscritos.domain.entities.Task;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskPriority;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static dev.matheuslf.desafio.inscritos.service.impl.TaskServiceImpl.buildTaskSpecification;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Project projectA;
    private Project projectB;
    private Task taskTodoHighA;
    private Task taskDoneMediumA;
    private Task taskDoingLowB;

    private final LocalDate tomorrow = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();

        projectA = Project.builder()
                .name("Project Alpha")
                .description("Project A description")
                .startDate(LocalDate.now())
                .build();
        projectRepository.save(projectA);

        projectB = Project.builder()
                .name("Project Beta")
                .description("Project B description")
                .startDate(LocalDate.now())
                .build();
        projectRepository.save(projectB);

        taskTodoHighA = Task.builder()
                .title("Implementar Feature X")
                .description("Detalhamento da Feature X")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(tomorrow)
                .project(projectA)
                .build();

        taskDoneMediumA = Task.builder()
                .title("Testes de Integração")
                .description("Finalizar cobertura de testes")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.MEDIUM)
                .dueDate(tomorrow)
                .project(projectA)
                .build();

        taskDoingLowB = Task.builder()
                .title("Documentação Básica")
                .status(TaskStatus.DOING)
                .priority(TaskPriority.LOW)
                .dueDate(tomorrow)
                .project(projectB)
                .build();

        taskRepository.saveAll(List.of(taskTodoHighA, taskDoneMediumA, taskDoingLowB));
    }

    @Test
    @DisplayName("FIND: Deve salvar e recuperar uma Task com seu Project relacionado")
    void findById_ShouldReturnTaskWithProject() {
        // Act
        var foundTask = taskRepository.findById(taskTodoHighA.getId()).orElse(null);

        // Assert
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getTitle()).isEqualTo("Implementar Feature X");

        // Verifica o relacionamento
        assertThat(foundTask.getProject()).isNotNull();
        assertThat(foundTask.getProject().getId()).isEqualTo(projectA.getId());
        assertThat(foundTask.getProject().getName()).isEqualTo("Project Alpha");
    }

    @Test
    @DisplayName("FILTER: Deve retornar tarefas filtradas por Status (DONE)")
    void findAllWithFilters_ShouldFilterByStatus() {
        // Arrange
        var spec = buildTaskSpecification(TaskStatus.DONE, null, null);

        // Act
        var result = taskRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(taskDoneMediumA.getTitle());
    }

    @Test
    @DisplayName("FILTER: Deve retornar tarefas filtradas por Prioridade (HIGH)")
    void findAllWithFilters_ShouldFilterByPriority() {
        // Arrange
        var spec = buildTaskSpecification(null, TaskPriority.HIGH, null);

        // Act
        var result = taskRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(taskTodoHighA.getTitle());
    }

    @Test
    @DisplayName("FILTER: Deve retornar tarefas filtradas por ID do Projeto (Project A)")
    void findAllWithFilters_ShouldFilterByProjectId() {
        // Arrange
        var spec = buildTaskSpecification(null, null, projectA.getId());

        // Act
        var result = taskRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(2); // taskTodoHighA e taskDoneMediumA
        assertThat(result).extracting(Task::getProject).extracting(Project::getName)
                .containsOnly(projectA.getName());
    }

    @Test
    @DisplayName("FILTER: Deve retornar tarefas filtradas com múltiplos critérios (Status DONE e Project A)")
    void findAllWithFilters_ShouldFilterByMultipleCriteria() {
        // Arrange
        var spec = buildTaskSpecification(TaskStatus.DONE, null, projectA.getId());

        // Act
        var result = taskRepository.findAll(spec);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(taskDoneMediumA.getTitle());
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(result.get(0).getProject().getId()).isEqualTo(projectA.getId());
    }

    @Test
    @DisplayName("FILTER: Deve retornar lista vazia se não houver correspondência")
    void findAllWithFilters_ShouldReturnEmptyList_WhenNoMatch() {
        // Arrange
        var spec = buildTaskSpecification(TaskStatus.TODO, TaskPriority.LOW, null);

        // Act
        var result = taskRepository.findAll(spec);

        // Assert
        assertThat(result).isEmpty();
    }
}
