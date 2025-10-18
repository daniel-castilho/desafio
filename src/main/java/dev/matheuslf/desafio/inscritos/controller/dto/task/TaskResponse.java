package dev.matheuslf.desafio.inscritos.controller.dto.task;

import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        String status,
        String priority,
        LocalDate dueDate,
        UUID projectId
) {
}
