package dev.matheuslf.desafio.inscritos.controller.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path
) {
}
