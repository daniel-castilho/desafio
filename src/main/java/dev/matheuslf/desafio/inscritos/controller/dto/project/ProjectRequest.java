package dev.matheuslf.desafio.inscritos.controller.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}
