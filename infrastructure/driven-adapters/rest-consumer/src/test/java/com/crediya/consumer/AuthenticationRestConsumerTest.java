package com.crediya.consumer;


import com.crediya.model.exception.BusinessException;
import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.BusinessErrorMessage;
import com.crediya.model.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class AuthenticationRestConsumerTest {

    @Mock
    private ObjectMapper mapper;

    private MockWebServer mockBackEnd;
    private AuthenticationRestConsumer restConsumer;

    @BeforeEach
    void setUp() throws IOException {

        mockBackEnd = new MockWebServer();
        mockBackEnd.start();

        WebClient client = WebClient.builder()
                .baseUrl(mockBackEnd.url("/").toString())
                .build();

        restConsumer = new AuthenticationRestConsumer(client, mapper);

        // Setup security context with fake JWT
        JwtAuthenticationToken auth = new JwtAuthenticationToken(
                Jwt.withTokenValue("fake-token").header("alg", "none").claim("sub", "test").build()
        );
        SecurityContext context = new SecurityContextImpl(auth);
        ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Should return user when backend returns 200")
    void shouldReturnUserWhenOk() {
        User expectedUser = new User();
        expectedUser.setIdentificationNumber(123);
        expectedUser.setEmail("darp@tes.com");

        mockBackEnd.enqueue(new MockResponse()
                .setBody("{\"identificationNumber\":123,\"email\":\"darp@tes.com\"}")
                .addHeader("Content-Type", "application/json"));

        SecurityContextImpl context = new SecurityContextImpl(
                new JwtAuthenticationToken(
                        Jwt.withTokenValue("fake-token")
                                .header("alg", "none")
                                .claim("sub", "test")
                                .build()
                )
        );

        Mono<User> response = restConsumer.findByIdentificationNumber(123)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));

        StepVerifier.create(response)
                .expectNextMatches(user ->
                        user.getIdentificationNumber().equals(expectedUser.getIdentificationNumber()) &&
                                user.getEmail().equals(expectedUser.getEmail()))
                .verifyComplete();
    }


    @Test
    @DisplayName("Should return BusinessException when backend returns empty body")
    void shouldReturnBusinessExceptionWhenEmpty() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        // Setup fake JWT in security context
        SecurityContextImpl context = new SecurityContextImpl(
                new JwtAuthenticationToken(
                        Jwt.withTokenValue("fake-token")
                                .header("alg", "none")
                                .claim("sub", "test")
                                .build()
                )
        );

        Mono<User> response = restConsumer.findByIdentificationNumber(999)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));

        StepVerifier.create(response)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BusinessException.class);
                    BusinessException be = (BusinessException) error;
                    assertThat(be.getBusinessErrorMessage())
                            .isEqualTo(BusinessErrorMessage.USER_NOT_FOUND);
                })
                .verify();
    }

    @Test
    @DisplayName("Fallback should return empty Mono on 404")
    void shouldReturnEmptyOnNotFound() {
        WebClientResponseException notFound = WebClientResponseException.create(
                404,
                "Not Found",
                null,
                null,
                null
        );

        Mono<User> response = restConsumer.fallbackUser(999, notFound);

        StepVerifier.create(response)
                .verifyComplete(); // completes empty
    }

    @Test
    @DisplayName("Fallback should propagate TechnicalException on other errors")
    void shouldPropagateTechnicalExceptionOnOtherError() {
        RuntimeException runtimeEx = new RuntimeException("boom");

        Mono<User> response = restConsumer.fallbackUser(999, runtimeEx);

        StepVerifier.create(response)
                .expectErrorMatches(ex -> ex instanceof TechnicalException &&
                        ex.getCause() == runtimeEx)
                .verify();
    }
}

