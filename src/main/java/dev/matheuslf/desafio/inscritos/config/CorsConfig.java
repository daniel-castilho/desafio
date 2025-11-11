package dev.matheuslf.desafio.inscritos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS) settings.
 * This class defines global CORS rules for the application, allowing
 * requests from any origin for specified HTTP methods and headers.
 */
@Configuration
public class CorsConfig {

    /**
     * Configures global CORS mappings for the application.
     * Allows all origins, specified HTTP methods (GET, POST, PUT, DELETE, OPTIONS),
     * and all headers for all endpoints ("/**").
     *
     * @return A {@link WebMvcConfigurer} bean with the defined CORS rules.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
