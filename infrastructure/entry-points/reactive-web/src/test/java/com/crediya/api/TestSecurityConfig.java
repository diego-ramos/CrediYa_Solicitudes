package com.crediya.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@TestConfiguration
public class TestSecurityConfig {
    private static final String CUSTOMER_ROLE = "CLIENTE";
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v*/solicitud/new").hasAnyRole(CUSTOMER_ROLE) // ✅ role-based restriction
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // Fake decoder to allow mockJwt() without secretKey
        return token -> Mono.just(Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("scope", "ADMINISTRADOR")
                .build());
    }
}

