package com.crediya.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(ApplicationHandlerV1 applicationHandlerV1) {
        return RouterFunctions
            .route()
                .path("/api/v1", builder -> builder.POST("/solicitud/new", applicationHandlerV1::newApplication))
            .build();
        }
}
