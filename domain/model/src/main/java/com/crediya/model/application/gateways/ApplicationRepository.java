package com.crediya.model.application.gateways;

import com.crediya.model.application.Application;
import reactor.core.publisher.Mono;

public interface ApplicationRepository {
    Mono<Application> newApplication(Application application);
}
