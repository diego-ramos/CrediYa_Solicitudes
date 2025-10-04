package com.crediya.model.sqsmessage.gateway;

import reactor.core.publisher.Mono;

public interface SqsMessagePublisher<T> {
     Mono<String> send(T sqsMessage);
}
