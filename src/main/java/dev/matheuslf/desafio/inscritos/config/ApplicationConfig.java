package dev.matheuslf.desafio.inscritos.config;

import dev.matheuslf.desafio.inscritos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Centralized configuration for application-level beans, primarily focusing on
 * security and data access components. This class defines how the application
 * loads user details, authenticates users, and encodes passwords.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * Provides a {@link UserDetailsService} bean that retrieves user details from the database.
     * This is a core component of Spring Security for loading user-specific data.
     *
     * @return An implementation of {@link UserDetailsService} that fetches a user by username.
     * @throws UsernameNotFoundException if the user cannot be found in the repository.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Configures and provides the primary {@link AuthenticationProvider} for the application.
     * This implementation uses a {@link DaoAuthenticationProvider}, which integrates the
     * {@link UserDetailsService} and {@link PasswordEncoder} to validate user credentials.
     *
     * @return A configured {@link DaoAuthenticationProvider} bean.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the {@link AuthenticationManager} as a bean.
     * This manager is the main entry point for Spring Security's authentication mechanism
     * and is used by the login endpoint to process authentication requests.
     *
     * @param config The {@link AuthenticationConfiguration} provided by Spring Security.
     * @return The configured {@link AuthenticationManager}.
     * @throws Exception if an error occurs while retrieving the manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Provides a {@link PasswordEncoder} bean that uses the BCrypt strong hashing function.
     * This is used by the {@link AuthenticationProvider} to compare submitted passwords
     * with the securely stored hashes in the database.
     *
     * @return A {@link BCryptPasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
