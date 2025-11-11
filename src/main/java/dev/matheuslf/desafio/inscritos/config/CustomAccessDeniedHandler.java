package dev.matheuslf.desafio.inscritos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.matheuslf.desafio.inscritos.controller.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom implementation of Spring Security's {@link AccessDeniedHandler}.
 * This handler is invoked when an authenticated user attempts to access a resource
 * for which they do not have sufficient permissions (i.e., a 403 Forbidden error).
 * It serializes a standardized {@link ErrorResponse} to the client.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code CustomAccessDeniedHandler} with the provided {@link ObjectMapper}.
     * The {@link ObjectMapper} is used to serialize the error response to JSON.
     *
     * @param objectMapper The Spring-configured {@link ObjectMapper} instance.
     */
    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Handles an {@link AccessDeniedException} by sending a 403 Forbidden HTTP response
     * with a standardized JSON error body.
     *
     * @param request               The {@link HttpServletRequest} that resulted in an {@link AccessDeniedException}.
     * @param response              The {@link HttpServletResponse} to send the error response to.
     * @param accessDeniedException The {@link AccessDeniedException} that was thrown.
     * @throws IOException      If an input or output error occurs.
     * @throws ServletException If a servlet-specific error occurs.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        var err = new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                accessDeniedException.getMessage(),
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), err);
        // Do NOT forward to /error to avoid additional security filter processing
    }
}
