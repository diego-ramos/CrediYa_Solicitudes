package com.crediya.api.config;

import com.crediya.api.security.CustomAccessDeniedHandler;
import com.crediya.api.security.CustomAuthenticationEntryPoint;
import com.crediya.api.security.CustomAuthorizationManager;
import com.crediya.api.security.CustomJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         CustomAuthenticationEntryPoint entryPoint,
                                                         CustomAccessDeniedHandler accessDeniedHandler,
                                                         CustomAuthorizationManager customAuthorizationManager) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/webjars/swagger-ui/*").permitAll()
                        .pathMatchers("/v3/api-docs/*").permitAll()
                        .pathMatchers("/actuator/*").permitAll()
                        .anyExchange().access(customAuthorizationManager) // dynamic DB permissions
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(entryPoint) // triggers for invalid/missing token
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler) // triggers for insufficient roles
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        var decoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();

        // Wrap decoding errors so Spring Security sees them as AuthenticationException
        return jwt -> decoder.decode(jwt)
                .onErrorMap(e -> new AuthenticationException("Invalid or expired token", e) {});
    }
}


