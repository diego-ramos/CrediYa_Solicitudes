package com.crediya.sqs.sender;

import com.crediya.model.sqsmessage.SqsTotalsSummaryMessage;
import com.crediya.model.sqsmessage.gateway.SqsMessagePublisher;
import com.crediya.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class SqsTotalsSummarySender implements SqsMessagePublisher<SqsTotalsSummaryMessage> {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> send(SqsTotalsSummaryMessage sqsMessage) {
        return Mono.fromCallable(() -> buildRequest(sqsMessage))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(SqsTotalsSummaryMessage message) throws JsonProcessingException {
        log.info("Sending SqsTotalsSummaryMessage {}", objectMapper.writeValueAsString(message));
        return SendMessageRequest.builder()
                .queueUrl(properties.totalsSummaryQueueUrl())
                .messageBody(objectMapper.writeValueAsString(message))
                .build();
    }
}