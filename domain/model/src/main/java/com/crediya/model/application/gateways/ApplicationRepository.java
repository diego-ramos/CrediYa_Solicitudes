package com.crediya.model.application.gateways;

import com.crediya.model.application.Application;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplicationRepository {
    Mono<Application> newApplication(Application application);
    Flux<Application> findAllByApplicationStatusIds(List<Integer> statusIds);
}
