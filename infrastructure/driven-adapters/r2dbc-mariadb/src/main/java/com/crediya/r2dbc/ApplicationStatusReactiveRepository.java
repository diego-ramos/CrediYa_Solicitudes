package com.crediya.r2dbc;

import com.crediya.model.applicationstatus.ApplicationStatus;
import com.crediya.r2dbc.data.ApplicationStatusEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplicationStatusReactiveRepository extends ReactiveCrudRepository<ApplicationStatusEntity, String>, ReactiveQueryByExampleExecutor<ApplicationStatusEntity> {
    Mono<ApplicationStatusEntity> findFirstById(Long id);

    @Query("SELECT * FROM application_status WHERE name IN (:statusIds)")
    Flux<ApplicationStatus> findAllByNameIn(List<String> statusNames);
}
