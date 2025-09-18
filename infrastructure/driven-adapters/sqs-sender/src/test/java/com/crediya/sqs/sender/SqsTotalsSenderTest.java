package com.crediya.sqs.sender;

import com.crediya.model.sqsmessage.SqsTotalsMessage;
import com.crediya.model.sqsmessage.Total;
import com.crediya.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SqsTotalsSenderTest {

    private SQSSenderProperties properties;
    private SqsAsyncClient sqsAsyncClient;
    private ObjectMapper objectMapper;
    private SqsTotalsSender sender;

    @BeforeEach
    void setUp() {
        properties = mock(SQSSenderProperties.class);
        sqsAsyncClient = mock(SqsAsyncClient.class);
        objectMapper = new ObjectMapper();

        when(properties.totalsQueueUrl()).thenReturn("https://sqs.aws/123/totals-queue");

        sender = new SqsTotalsSender(properties, sqsAsyncClient, objectMapper);
    }

    @Test
    void shouldSendMessageWithTotalsList() throws Exception {
        // given
        Total total1 = new Total("APPROVED_APPLICATION_KEY", "100");
        Total total2 = new Total("REJECTED_APPLICATION_KEY", "50");
        SqsTotalsMessage message = new SqsTotalsMessage(List.of(total1, total2));

        SendMessageResponse mockResponse =
                SendMessageResponse.builder().messageId("msg-123").build();

        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // when
        StepVerifier.create(sender.send(message))
                .expectNext("msg-123")
                .verifyComplete();

        // then: capture the request
        ArgumentCaptor<SendMessageRequest> captor =
                ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsAsyncClient, times(1)).sendMessage(captor.capture());

        SendMessageRequest sentRequest = captor.getValue();
        assertThat(sentRequest.queueUrl()).isEqualTo("https://sqs.aws/123/totals-queue");

        String body = sentRequest.messageBody();
        assertThat(body).contains("APPROVED_APPLICATION_KEY");
        assertThat(body).contains("100");
        assertThat(body).contains("REJECTED_APPLICATION_KEY");
        assertThat(body).contains("50");
    }

    @Test
    void shouldPropagateErrorWhenSqsFails() {
        // given
        SqsTotalsMessage message = new SqsTotalsMessage(List.of(new Total("X", "1")));

        when(sqsAsyncClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        // when / then
        StepVerifier.create(sender.send(message))
                .expectErrorMatches(ex -> ex.getMessage().contains("boom"))
                .verify();
    }

}
