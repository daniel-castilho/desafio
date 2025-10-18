package dev.matheuslf.desafio.inscritos.controller.dto.project;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}
