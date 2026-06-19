package com.example.bookstore.config;

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
                        .title("Bookstore API")
                        .version("1.0.0")
                        .description("API REST para la gestion de una libreria digital.")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("dev@bookstore.com")));
    }
}
