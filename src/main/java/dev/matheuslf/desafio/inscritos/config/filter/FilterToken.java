package dev.matheuslf.desafio.inscritos.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.matheuslf.desafio.inscritos.controller.exception.ErrorResponse;
import dev.matheuslf.desafio.inscritos.repository.UserRepository;
import dev.matheuslf.desafio.inscritos.service.token.TokenService;
import dev.matheuslf.desafio.inscritos.service.token.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * A security filter that intercepts all incoming requests to validate the JWT token.
 * This filter runs once per request and is responsible for:
 * <ol>
 *     <li>Extracting the token from the Authorization header.</li>
 *     <li>Validating the token using the {@link TokenService}.</li>
 *     <li>Setting the {@link SecurityContextHolder} with the user's authentication details if the token is valid.</li>
 *     <li>Handling invalid token exceptions by returning an HTTP 401 Unauthorized response.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FilterToken extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Processes the incoming request to validate the JWT and set up the security context.
     *
     * @param request  The {@link HttpServletRequest} object that contains the request the client made of the servlet.
     * @param response The {@link HttpServletResponse} object that contains the response the servlet sends to the client.
     * @param filterChain The {@link FilterChain} for invoking the next filter in the chain.
     * @throws ServletException if the request for the POST could not be handled.
     * @throws IOException if an input or output error is detected when the servlet handles the request.
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws ServletException, IOException {

        var token = recoverToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var subject = tokenService.validateToken(token);
            if (subject != null && !subject.isBlank()) {
                var optUser = userRepository.findByUsername(subject);
                if (optUser.isPresent()) {
                    var user = optUser.get();
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Token validated and authentication set for subject={} on path={}", subject, request.getRequestURI());
                } else {
                    // Fallback: if the user is not in the DB (e.g., deleted), but the token is still valid,
                    // try to build a security principal based on the token's claims.
                    var roleFromToken = tokenService.getRoleFromToken(token);
                    if (roleFromToken != null && !roleFromToken.isBlank()) {
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleFromToken));
                        var principal = subject; // Keep username as the principal
                        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Token validated for subject={} but user not found in DB; using role from token={} to set authentication; requestURI={}", subject, roleFromToken, request.getRequestURI());
                    } else {
                        log.warn("Token valid for subject={} but no User entry found in database and no role claim available; requestURI={}", subject, request.getRequestURI());
                    }
                }
            }
        } catch (InvalidTokenException e) {
            log.warn("Invalid token for request {}: {}", request.getRequestURI(), e.getMessage());
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token", request.getRequestURI());
            return; // Stop the filter chain
        } catch (Exception e) {
            log.error("Unexpected error during token processing for request {}: {}", request.getRequestURI(), e.getMessage(), e);
            writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during token processing", request.getRequestURI());
            return; // Stop the filter chain
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Writes a standardized error response to the {@link HttpServletResponse}.
     *
     * @param response The HTTP response object.
     * @param status   The {@link HttpStatus} to be returned.
     * @param message  The specific error message.
     * @param path     The request path that caused the error.
     * @throws IOException if an error occurs while writing to the response.
     */
    private void writeErrorResponse(final HttpServletResponse response,
                                    final HttpStatus status,
                                    final String message,
                                    final String path) throws IOException {
        var err = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), err);
    }

    /**
     * Extracts the JWT from the "Authorization" header of the request.
     *
     * @param request The HTTP request object.
     * @return The JWT as a String, or {@code null} if the header is not found or is invalid.
     */
    private String recoverToken(final HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
