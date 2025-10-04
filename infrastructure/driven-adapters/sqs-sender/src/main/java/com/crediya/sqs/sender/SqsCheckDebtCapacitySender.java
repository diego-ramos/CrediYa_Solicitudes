package com.crediya.sqs.sender;

import com.crediya.model.sqsmessage.SqsCheckDebtCapacityMessage;
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
public class SqsCheckDebtCapacitySender implements SqsMessagePublisher<SqsCheckDebtCapacityMessage> {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;

    public Mono<String> send(SqsCheckDebtCapacityMessage message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(SqsCheckDebtCapacityMessage message) throws JsonProcessingException {
        return SendMessageRequest.builder()
                .queueUrl(properties.debtCapacityQueueUrl())
                .messageBody(objectMapper.writeValueAsString(message))
                .build();
    }
}