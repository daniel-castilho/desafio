package dev.matheuslf.desafio.inscritos.domain.entities;

import dev.matheuslf.desafio.inscritos.domain.enums.TaskPriority;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "task")
public class Task {

    @Id
    @UuidGenerator
    private UUID id;

    @NotBlank(message = "O título é obrigatório")
    @Size(min = 5, max = 150, message = "O título deve ter entre 5 e 150 caracteres")
    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = true)
    private String description;

    @NotNull(message = "O status da tarefa é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "task_status")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TaskStatus status;

    @NotNull(message = "A prioridade da tarefa é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "task_priority")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TaskPriority priority;

    @Column(name = "due_date", nullable = true)
    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false) // Mapeia para a coluna 'project_id' no DB
    @NotNull(message = "A tarefa deve estar vinculada a um projeto")
    private Project project;

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", project=" + project +
                '}';
    }
}
