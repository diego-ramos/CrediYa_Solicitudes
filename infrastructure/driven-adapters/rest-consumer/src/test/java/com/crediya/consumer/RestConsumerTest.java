package com.crediya.consumer;


import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RestConsumerTest {

    private static AuthenticationRestConsumer restConsumer;
    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        restConsumer = new AuthenticationRestConsumer(webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {

        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Validate the function findByIdentificationNumber.")
    void validateFindByIdentificationNumber() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"state\" : \"ok\"}"));

        var response = restConsumer.findByIdentificationNumber(123);

        StepVerifier.create(response)
                .expectNextMatches(user -> {
                    // adapt check according to your mapping logic
                    return user != null && user.getIdType() == null;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return TechnicalException when backend returns 404")
    void shouldReturnTechnicalExceptionOnNotFound() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Act
        var response = restConsumer.findByIdentificationNumber(999);

        // Assert
        StepVerifier.create(response)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(TechnicalException.class);
                    assertThat(error.getCause())
                            .isInstanceOf(WebClientResponseException.NotFound.class);
                    TechnicalException te = (TechnicalException) error;
                    assertThat(te.getTechnicalErrorMessage())
                            .isEqualTo(TechnicalErrorMessage.USER_IDENTIFICATION_NUMBER_FIND);
                })
                .verify();
    }
}
