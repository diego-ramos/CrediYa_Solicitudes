package com.crediya.r2dbc;

import com.crediya.r2dbc.data.ApplicationStatusEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ApplicationStatusReactiveRepository extends ReactiveCrudRepository<ApplicationStatusEntity, String>, ReactiveQueryByExampleExecutor<ApplicationStatusEntity> {
    Mono<ApplicationStatusEntity> findFirstById(Long id);
}
