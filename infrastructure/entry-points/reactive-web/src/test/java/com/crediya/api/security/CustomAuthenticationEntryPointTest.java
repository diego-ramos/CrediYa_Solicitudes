package com.crediya.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
    }

    @Test
    void shouldReturnUnauthorizedResponse() {
        // Mock exchange and response
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();

        when(exchange.getResponse()).thenReturn(response);
        when(response.getHeaders()).thenReturn(headers);
        when(response.bufferFactory()).thenReturn(new DefaultDataBufferFactory());

        // Mock writeWith to return a completed Mono
        when(response.writeWith(any(Mono.class))).thenAnswer(invocation -> Mono.empty());

        AuthenticationException authException = mock(AuthenticationException.class);

        // Act
        Mono<Void> result = entryPoint.commence(exchange, authException);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verify status and content type
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        assert headers.getContentType().equals(MediaType.APPLICATION_JSON);
    }
}
