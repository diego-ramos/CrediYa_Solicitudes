package com.crediya.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomAuthorizationManagerTest {

    private CustomAuthorizationManager manager;

    @BeforeEach
    void setUp() {
        manager = new CustomAuthorizationManager();
    }

    private AuthorizationContext buildContext(String path, String method) {
        MockServerHttpRequest request = MockServerHttpRequest
                .method(method, path)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        return new AuthorizationContext(exchange);
    }

    private Authentication buildAuthenticationWithPermissions(List<Map<String, Object>> permissions, String role) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("permissions")).thenReturn(permissions);
        when(jwt.getSubject()).thenReturn("test-user");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(jwt);
        when(auth.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority(role)));
        when(auth.getName()).thenReturn("test-user");

        return auth;
    }

    @Test
    void shouldAuthorizeWhenPermissionMatches() {
        List<Map<String, Object>> permissions = List.of(
                Map.<String, Object>of(
                        "path", "/api/data/**",
                        "method", "GET",
                        "role", "ROLE_USER"
                )
        );

        Authentication auth = buildAuthenticationWithPermissions(permissions, "ROLE_USER");
        AuthorizationContext ctx = buildContext("/api/data/123", "GET");

        StepVerifier.create(manager.check(Mono.just(auth), ctx))
                .expectNextMatches(AuthorizationDecision::isGranted)
                .verifyComplete();
    }


    @Test
    void shouldDenyWhenMethodDoesNotMatch() {
        List<Map<String, Object>> permissions = List.of(
                Map.<String, Object>of(
                        "path", "/api/data/**",
                        "method", "POST",
                        "role", "ROLE_USER"
                )
        );

        Authentication auth = buildAuthenticationWithPermissions(permissions, "ROLE_USER");
        AuthorizationContext ctx = buildContext("/api/data/123", "GET");

        StepVerifier.create(manager.check(Mono.just(auth), ctx))
                .expectNextMatches(decision -> !decision.isGranted())
                .verifyComplete();
    }

    @Test
    void shouldDenyWhenUnauthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        AuthorizationContext ctx = buildContext("/api/data/123", "GET");

        StepVerifier.create(manager.check(Mono.just(auth), ctx))
                .expectNextMatches(decision -> !decision.isGranted())
                .verifyComplete();
    }
}
