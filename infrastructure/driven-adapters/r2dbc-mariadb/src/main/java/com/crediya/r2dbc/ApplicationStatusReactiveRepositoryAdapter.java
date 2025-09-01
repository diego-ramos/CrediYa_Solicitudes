package com.crediya.r2dbc;

import com.crediya.model.applicationstatus.ApplicationStatus;
import com.crediya.model.applicationstatus.gateways.ApplicationStatusRepository;
import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import com.crediya.r2dbc.data.ApplicationStatusEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class ApplicationStatusReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        ApplicationStatus/* change for domain model */,
        ApplicationStatusEntity/* change for adapter model */,
        String,
        ApplicationStatusReactiveRepository
        >

    implements ApplicationStatusRepository
{
    public ApplicationStatusReactiveRepositoryAdapter(ApplicationStatusReactiveRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> mapper.map(d, ApplicationStatus.class));
    }

    @Override
    public Mono<ApplicationStatus> findById(Long id) {
        return repository
                .findFirstById(id)
                .map(this::toEntity)
                .onErrorMap(e -> new TechnicalException(e, TechnicalErrorMessage.STATUS_ID_FIND));
    }

    @Override
    public Flux<ApplicationStatus> findAllByNameIn(List<String> statusNames) {
        return repository.findAllByNameIn(statusNames);
    }
}
