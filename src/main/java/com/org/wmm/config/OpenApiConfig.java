package com.org.wmm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WilliamMacMiron API")
                        .version("0.1.0")
                        .description("""
                                Backend API for WilliamMacMiron — content management system \
                                for cataloging and reviewing spirits with multi-language support, \
                                tasting notes and flavor profiles.
                                
                                ## Authentication
                                
                                Most endpoints require a JWT Bearer token. \
                                Obtain one via `POST /auth/login`, then click **Authorize** \
                                and paste the `accessToken` value.
                                
                                ## Roles
                                
                                | Role | Access |
                                |------|--------|
                                | `ROLE_ADMIN` | Full access |
                                | `ROLE_EDITOR` | Content management |
                                | `ROLE_VIEWER` | Read-only admin panel |
                                """)
                        .contact(new Contact()
                                .name("WilliamMacMiron")
                                .url("https://github.com/jkamin61/wmm")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local dev")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the accessToken from /auth/login response")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

