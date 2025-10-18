package dev.matheuslf.desafio.inscritos.controller.dto.task;

import dev.matheuslf.desafio.inscritos.domain.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(
        @NotNull(message = "O novo status é obrigatório.")
        TaskStatus status
) {
}
