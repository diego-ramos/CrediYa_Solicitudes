package com.crediya.sqs.sender;

import com.crediya.model.sqsmessage.SqsCheckDebtCapacityMessage;
import com.crediya.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SqsCheckDebtCapacitySenderTest {

    private SQSSenderProperties properties;
    private SqsAsyncClient client;
    private ObjectMapper objectMapper;
    private SqsCheckDebtCapacitySender sender;

    @BeforeEach
    void setUp() {
        properties = mock(SQSSenderProperties.class);
        client = mock(SqsAsyncClient.class);
        objectMapper = mock(ObjectMapper.class);

        sender = new SqsCheckDebtCapacitySender(properties, client, objectMapper);
    }

    @Test
    void send_shouldPublishMessageAndReturnMessageId() throws Exception {
        // Arrange
        SqsCheckDebtCapacityMessage message = new SqsCheckDebtCapacityMessage();
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/123456789012/debt-capacity";
        String jsonBody = "{\"foo\":\"bar\"}";

        when(properties.debtCapacityQueueUrl()).thenReturn(queueUrl);
        when(objectMapper.writeValueAsString(message)).thenReturn(jsonBody);

        SendMessageResponse response = SendMessageResponse.builder()
                .messageId("abc-123")
                .build();

        CompletableFuture<SendMessageResponse> future = CompletableFuture.completedFuture(response);
        when(client.sendMessage(any(SendMessageRequest.class))).thenReturn(future);

        // Act
        Mono<String> result = sender.send(message);

        // Assert
        StepVerifier.create(result)
                .expectNext("abc-123")
                .verifyComplete();

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(client).sendMessage(captor.capture());

        SendMessageRequest requestSent = captor.getValue();
        assertThat(requestSent.queueUrl()).isEqualTo(queueUrl);
        assertThat(requestSent.messageBody()).isEqualTo(jsonBody);

        verify(objectMapper).writeValueAsString(message);
    }

    @Test
    void send_shouldErrorWhenSerializationFails() throws Exception {
        // Arrange
        SqsCheckDebtCapacityMessage message = new SqsCheckDebtCapacityMessage();

        when(objectMapper.writeValueAsString(message)).thenThrow(new RuntimeException("Serialization error"));

        // Act
        Mono<String> result = sender.send(message);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Serialization error"))
                .verify();
    }
}
