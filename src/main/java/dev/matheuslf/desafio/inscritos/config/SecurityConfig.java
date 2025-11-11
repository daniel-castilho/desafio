package dev.matheuslf.desafio.inscritos.config;

import dev.matheuslf.desafio.inscritos.config.filter.FilterToken;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Spring Security configuration for the application.
 * This class defines the security filter chain, including CORS, CSRF protection,
 * session management, authorization rules for different endpoints and roles,
 * and custom handlers for authentication and access denied exceptions.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FilterToken filterToken;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /**
     * Configures the security filter chain for HTTP requests.
     *
     * @param http The {@link HttpSecurity} object to configure.
     * @return A {@link SecurityFilterChain} bean that defines the security rules.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless sessions
                .authorizeHttpRequests(authz -> authz
                        // Authentication endpoints are publicly accessible
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                        // Swagger UI and API docs are publicly accessible
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

                        // Allow error endpoint so AccessDenied handling doesn't forward to a secured endpoint
                        .requestMatchers("/error", "/error/**").permitAll()

                        // Task-related endpoints require either DEVELOPER or MANAGER role
                        .requestMatchers("/tasks/**").hasAnyRole("DEVELOPER", "MANAGER")

                        // Project-related GET requests require either DEVELOPER or MANAGER role
                        .requestMatchers(HttpMethod.GET, "/projects/**").hasAnyRole("DEVELOPER", "MANAGER")
                        // Project-related POST, PUT, DELETE requests require MANAGER role
                        .requestMatchers(HttpMethod.POST, "/projects/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/projects/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/projects/**").hasRole("MANAGER")

                        .anyRequest().authenticated() // All other requests require authentication
                )
                .addFilterBefore(filterToken, UsernamePasswordAuthenticationFilter.class) // Add custom JWT filter
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // Handle unauthenticated requests
                        .accessDeniedHandler(customAccessDeniedHandler) // Handle access denied for authenticated users
                );

        return http.build();
    }
}
