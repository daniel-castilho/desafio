package dev.matheuslf.desafio.inscritos.controller.dto.task;

import dev.matheuslf.desafio.inscritos.domain.enums.TaskPriority;
import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record TaskRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 5, max = 150, message = "O título deve ter entre 5 e 150 caracteres")
        String title,

        @Size(max = 500, message = "A descrição não pode exceder 500 caracteres")
        String description,

        @NotNull(message = "O status da tarefa é obrigatório")
        TaskStatus status,

        @NotNull(message = "A prioridade da tarefa é obrigatória")
        TaskPriority priority,

        @NotNull(message = "A data de vencimento é obrigatória") // Se a data for opcional, remova este @NotNull
        @FutureOrPresent(message = "A data de vencimento deve ser hoje ou no futuro")
        LocalDate dueDate,

        @NotNull(message = "A tarefa deve estar vinculada a um projeto")
        UUID projectId
) {
}
