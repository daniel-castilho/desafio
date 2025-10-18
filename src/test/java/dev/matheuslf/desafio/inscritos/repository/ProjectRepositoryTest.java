package dev.matheuslf.desafio.inscritos.repository;

import dev.matheuslf.desafio.inscritos.domain.entities.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    private Project projectAlpha;
    private Project projectBeta;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();

        projectAlpha = Project.builder()
                .name("Alpha Project")
                .description("Descrição do Projeto Alpha")
                .startDate(today)
                .endDate(today.plusDays(30))
                .build();

        projectBeta = Project.builder()
                .name("Beta Project")
                .description("Descrição do Projeto Beta")
                .startDate(today.plusDays(10))
                .endDate(today.plusDays(60))
                .build();

        projectRepository.saveAll(List.of(projectAlpha, projectBeta));
    }

    @Test
    @DisplayName("CREATE: Deve salvar e retornar o Project com ID gerado")
    void save_ShouldPersistProjectAndGenerateId() {
        // Arrange
        var newProject = Project.builder()
                .name("Novo Project")
                .startDate(today)
                .build();

        // Act
        var savedProject = projectRepository.save(newProject);

        // Assert
        assertThat(savedProject).isNotNull();
        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getName()).isEqualTo("Novo Project");
        assertThat(projectRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("READ: Deve encontrar um Project pelo ID")
    void findById_ShouldReturnProject() {
        // Act
        Optional<Project> foundProject = projectRepository.findById(projectAlpha.getId());

        // Assert
        assertThat(foundProject).isPresent();
        var project = foundProject.get();
        assertThat(project.getName()).isEqualTo("Alpha Project");
        assertThat(project.getStartDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("READ: Deve retornar vazio ao buscar por ID inexistente")
    void findById_ShouldReturnEmpty_WhenNotFound() {
        // Act
        Optional<Project> foundProject = projectRepository.findById(java.util.UUID.randomUUID());

        // Assert
        assertThat(foundProject).isNotPresent();
    }

    @Test
    @DisplayName("READ ALL: Deve retornar todos os Projects persistidos")
    void findAll_ShouldReturnAllProjects() {
        // Act
        var projects = projectRepository.findAll();

        // Assert
        assertThat(projects).hasSize(2);
        assertThat(projects).extracting(Project::getName)
                .containsExactlyInAnyOrder("Alpha Project", "Beta Project");
    }

    @Test
    @DisplayName("DELETE: Deve deletar um Project com sucesso")
    void deleteById_ShouldRemoveProject() {
        // Arrange
        var initialCount = projectRepository.count();

        // Act
        projectRepository.deleteById(projectAlpha.getId());

        // Assert
        assertThat(projectRepository.count()).isEqualTo(initialCount - 1);
        Optional<Project> deletedProject = projectRepository.findById(projectAlpha.getId());
        assertThat(deletedProject).isNotPresent();
    }

    @Test
    @DisplayName("EXISTS: Deve retornar true se o Project existir")
    void existsById_ShouldReturnTrue_WhenExists() {
        // Act & Assert
        assertThat(projectRepository.existsById(projectAlpha.getId())).isTrue();
    }

    @Test
    @DisplayName("EXISTS: Deve retornar false se o Project não existir")
    void existsById_ShouldReturnFalse_WhenNotExists() {
        // Act & Assert
        assertThat(projectRepository.existsById(java.util.UUID.randomUUID())).isFalse();
    }
}