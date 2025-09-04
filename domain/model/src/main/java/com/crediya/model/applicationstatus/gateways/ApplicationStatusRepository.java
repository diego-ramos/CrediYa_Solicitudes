package com.crediya.model.applicationstatus.gateways;

import com.crediya.model.applicationstatus.ApplicationStatus;
import reactor.core.publisher.Mono;

public interface ApplicationStatusRepository {
    Mono<ApplicationStatus> findById(Long id);
}
