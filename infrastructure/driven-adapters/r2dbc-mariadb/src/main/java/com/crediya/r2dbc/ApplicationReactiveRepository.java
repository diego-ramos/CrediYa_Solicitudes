package com.crediya.r2dbc;

import com.crediya.r2dbc.data.ApplicationEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ApplicationReactiveRepository extends ReactiveCrudRepository<ApplicationEntity, String>, ReactiveQueryByExampleExecutor<ApplicationEntity> {
}
