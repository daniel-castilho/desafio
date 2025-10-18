package dev.matheuslf.desafio.inscritos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Desafio Técnico – Sistema de Gestão de Projetos e Demandas")
                        .description("API RESTful para CRUD de Projetos e suas respectivas Tarefas, utilizando Clean Architecture e TDD.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Daniel Castilho")
                                .email("dan.castilho@gmail.com")
                                .url("https://www.linkedin.com/in/dancastilho/")));
    }
}
