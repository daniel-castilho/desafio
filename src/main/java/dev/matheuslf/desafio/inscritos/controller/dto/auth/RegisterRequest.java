package dev.matheuslf.desafio.inscritos.controller.dto.auth;

import dev.matheuslf.desafio.inscritos.domain.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank
        String username,
        @NotBlank
        String password,
        @NotNull
        Role role
) {
}
