package com.crediya.model.sqsmessage.gateway;

import com.crediya.model.sqsmessage.SqsApplicationUpdateMessage;
import reactor.core.publisher.Mono;

public interface SqsMessagePublisher {
     Mono<String> send(SqsApplicationUpdateMessage sqsApplicationUpdateMessage);
}
