package com.taskflow.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun taskFlowOpenAPI(): OpenAPI {
        val securitySchemeName = "BearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("TaskFlow REST API")
                    .description("Full-stack team task tracker API built with Spring Boot 3 and Kotlin")
                    .version("1.0.0")
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .`in`(SecurityScheme.In.HEADER)
                        .description("Provide your JWT token obtained from /api/auth/login")
                )
            )
    }
}
