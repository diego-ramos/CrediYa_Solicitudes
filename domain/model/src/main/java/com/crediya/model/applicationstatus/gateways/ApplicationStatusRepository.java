package com.crediya.model.applicationstatus.gateways;

import com.crediya.model.applicationstatus.ApplicationStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplicationStatusRepository {
    Mono<ApplicationStatus> findById(Long id);
    Flux<ApplicationStatus> findAllByNameIn(List<String> statusNames);
}
