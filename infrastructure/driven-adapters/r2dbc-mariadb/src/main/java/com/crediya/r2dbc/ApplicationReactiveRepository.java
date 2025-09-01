package com.crediya.r2dbc;

import com.crediya.model.application.Application;
import com.crediya.r2dbc.data.ApplicationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ApplicationReactiveRepository extends ReactiveCrudRepository<ApplicationEntity, String>, ReactiveQueryByExampleExecutor<ApplicationEntity> {

    @Query("SELECT * FROM application WHERE id_status IN (:statusIds)")
    Flux<Application> findAllByApplicationStatusIds(List<Integer> statusIds);
}
