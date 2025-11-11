package dev.matheuslf.desafio.inscritos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * This class customizes the OpenAPI specification generated for the RESTful API,
 * providing metadata such as title, description, version, and contact information.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures a custom {@link OpenAPI} bean with application-specific information.
     * This includes the API title, description, version, and contact details.
     *
     * @return A customized {@link OpenAPI} instance for Swagger UI.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Technical Challenge – Project and Demand Management System")
                        .description("RESTful API for CRUD operations on Projects and their respective Tasks, implemented using Clean Architecture and TDD principles.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Daniel Castilho")
                                .email("dan.castilho@gmail.com")
                                .url("https://www.linkedin.com/in/dancastilho/")));
    }
}
