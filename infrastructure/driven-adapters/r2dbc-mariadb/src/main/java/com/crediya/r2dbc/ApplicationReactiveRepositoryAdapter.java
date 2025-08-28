package com.crediya.r2dbc;

import com.crediya.model.application.Application;
import com.crediya.model.application.gateways.ApplicationRepository;
import com.crediya.r2dbc.data.ApplicationEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Application/* change for domain model */,
        ApplicationEntity/* change for adapter model */,
        String,
        ApplicationReactiveRepository
        >

    implements ApplicationRepository
{
    public ApplicationReactiveRepositoryAdapter(ApplicationReactiveRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> mapper.map(d, Application.class/* change for domain model */));
    }

}
