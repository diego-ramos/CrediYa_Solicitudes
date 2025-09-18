package com.crediya.r2dbc;

import com.crediya.r2dbc.data.ApplicationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collection;

public interface ApplicationReactiveRepository extends ReactiveCrudRepository<ApplicationEntity, String>, ReactiveQueryByExampleExecutor<ApplicationEntity> {
    Mono<ApplicationEntity> findById (Integer applicationId);

    Flux<ApplicationEntity> findAllByApplicationStatusIdIsInAndIdentificationNumber(Collection<Integer> applicationStatusIds, Integer identificationNumber);

    Flux<ApplicationEntity>  findAllByApplicationStatusIdIn(Collection<Integer> applicationStatusIds);

    Mono<Long> countAllByApplicationStatusId(long applicationStatusId);

    @Query("SELECT SUM(a.amount)" +
            "FROM application a " +
            "WHERE a.id_status = :applicationStatusId")
    Mono<BigDecimal>  applicationsTotalAmount(long applicationStatusId);
}
