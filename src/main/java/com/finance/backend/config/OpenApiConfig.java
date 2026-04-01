package com.finance.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration.
 * Adds JWT bearer token support to the Swagger UI so secured endpoints
 * can be tested directly from the docs page.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI financeOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("Finance Data Processing API")
                        .description("Backend API for finance dashboard with role-based access control. "
                                + "Built for Zorvyn internship assessment.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Varun Kumar Thakur")
                                .email("varunkumarthakur021@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")));
    }
}
