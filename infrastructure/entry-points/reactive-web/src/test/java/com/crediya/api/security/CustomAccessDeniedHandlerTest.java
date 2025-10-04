package com.crediya.api.security;

import com.crediya.api.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CustomAccessDeniedHandlerTest {

    private CustomAccessDeniedHandler handler;

    private ServerWebExchange exchange;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        handler = new CustomAccessDeniedHandler();
        exchange = mock(ServerWebExchange.class);
        response = mock(ServerHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        when(response.getHeaders()).thenReturn(headers);

        when(exchange.getResponse()).thenReturn(response);
        when(response.bufferFactory()).thenReturn(new org.springframework.core.io.buffer.DefaultDataBufferFactory());
    }

    @Test
    void handle_shouldSetStatusCodeAndContentTypeAndWriteBody() {

        HttpHeaders headers = mock(HttpHeaders.class);
        when(response.getHeaders()).thenReturn(headers);
        // Arrange
        when(response.writeWith(any())).thenAnswer(invocation -> Mono.empty());

        // Act
        Mono<Void> result = handler.handle(exchange, new AccessDeniedException("access denied"));

        // Assert
        StepVerifier.create(result).verifyComplete();

        // Verify status code and content type
        verify(response).setStatusCode(HttpStatus.FORBIDDEN);
        verify(response.getHeaders()).setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        // Capture written bytes and verify content
        ArgumentCaptor<org.springframework.core.io.buffer.DataBuffer> captor = ArgumentCaptor.forClass(org.springframework.core.io.buffer.DataBuffer.class);
        verify(response).writeWith(any());
    }

    @Test
    void handle_shouldReturnJsonBodyWithCorrectFields() throws Exception {
        final byte[][] capturedBytes = new byte[1][];
        when(response.writeWith(any())).thenAnswer(invocation -> {
            Mono<DataBuffer> publisher = invocation.getArgument(0, Mono.class);
            publisher.subscribe(dataBuffer -> {
                capturedBytes[0] = dataBuffer.asByteBuffer().array();
            });
            return Mono.empty();
        });

        // Pass AccessDeniedException
        handler.handle(exchange, new AccessDeniedException("access denied")).block();

        String json = new String(capturedBytes[0], StandardCharsets.UTF_8);
        assertThat(json).contains("\"status\":403");
        assertThat(json).contains("\"error\":\"" + Constants.FORBIDDEN + "\"");
        assertThat(json).contains("\"message\":\"" + Constants.YOU_DONT_HAVE_PERMISSION_TO_ACCESS + "\"");
    }
}
