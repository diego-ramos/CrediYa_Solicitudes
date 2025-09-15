package com.crediya.sqs.sender;

import com.crediya.model.sqsmessage.SqsApplicationUpdateMessage;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SqsApplicationUpdateSenderTest {

    private SQSSenderProperties properties;
    private SqsAsyncClient client;
    private ObjectMapper objectMapper;
    private SqsApplicationUpdateSender sender;

    @BeforeEach
    void setUp() {
        properties = mock(SQSSenderProperties.class);
        client = mock(SqsAsyncClient.class);
        objectMapper = new ObjectMapper();
        sender = new SqsApplicationUpdateSender(properties, client, objectMapper);

        when(properties.applicationUpdateQueueUrl()).thenReturn("http://sqs.test-queue");
    }

    @Test
    void shouldSendMessageSuccessfully() {
        // Arrange
        SqsApplicationUpdateMessage message = new SqsApplicationUpdateMessage();
        message.setEmail("test@test.com");
        message.setApplicationId(123L);
        message.setNewApplicationStatus("APPROVED");

        SendMessageResponse fakeResponse = SendMessageResponse.builder()
                .messageId("msg-001")
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(fakeResponse));

        // Act
        Mono<String> result = sender.send(message);

        // Assert
        StepVerifier.create(result)
                .expectNext("msg-001")
                .verifyComplete();

        // Capture the request sent to SQS
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(client).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.queueUrl()).isEqualTo("http://sqs.test-queue");
        assertThat(request.messageBody()).contains("123"); // JSON body includes the id
        assertThat(request.messageBody()).contains("APPROVED");
    }

    @Test
    void shouldFailWhenJsonSerializationFails() {
        // Arrange
        SqsApplicationUpdateMessage badMessage = mock(SqsApplicationUpdateMessage.class);
        // Force objectMapper.writeValueAsString to fail by throwing exception
        SqsApplicationUpdateSender badSender =
                new SqsApplicationUpdateSender(properties, client, spy(new ObjectMapper() {
                    @Override
                    public String writeValueAsString(Object value) {
                        throw new RuntimeException("Serialization failed");
                    }
                }));

        // Act
        Mono<String> result = badSender.send(badMessage);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Serialization failed"))
                .verify();
    }
}
