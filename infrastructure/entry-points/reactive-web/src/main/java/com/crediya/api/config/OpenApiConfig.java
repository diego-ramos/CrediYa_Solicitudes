package com.crediya.api.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi solicitudApi() {
        return GroupedOpenApi.builder()
                .group("solicitud")
                .pathsToMatch("/api/v1/solicitud/**")
                .build();
    }
}
