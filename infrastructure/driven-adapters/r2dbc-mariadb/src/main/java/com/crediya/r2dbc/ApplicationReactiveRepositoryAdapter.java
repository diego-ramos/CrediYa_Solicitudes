package com.crediya.r2dbc;

import com.crediya.model.application.Application;
import com.crediya.model.application.gateways.ApplicationRepository;
import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import com.crediya.r2dbc.data.ApplicationEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import com.crediya.r2dbc.pagination.Paginator;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class ApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Application/* change for domain model */,
        ApplicationEntity/* change for adapter model */,
        String,
        ApplicationReactiveRepository
        >

    implements ApplicationRepository
{
    private final Paginator paginator;

    public ApplicationReactiveRepositoryAdapter(ApplicationReactiveRepository repository,
                                                ObjectMapper mapper,
                                                Paginator paginator) {
        super(repository, mapper, d -> mapper.map(d, Application.class));
        this.paginator = paginator;
    }

    @Override
    public Mono<Application> newApplication(Application application) {
        return repository.save(toData(application))
                .map(this::toEntity)
                .onErrorMap(e -> new TechnicalException(e, TechnicalErrorMessage.APPLICATION_SAVE));
    }

    @Override
    public Mono<Page<Application>> findAllByApplicationStatusIds(List<Integer> statusIds, PageRequest pageRequest) {

        Criteria criteria = Criteria.where("id_status").in(statusIds);
        return paginator.paginate(criteria, ApplicationEntity.class, pageRequest)
                .map(pageEntity -> {
                    List<Application> content = pageEntity.content()
                            .stream()
                            .map(this::toEntity) // tu mapper: ApplicationEntity -> Application
                            .toList();
                    return new Page<>(
                            content,
                            pageEntity.page(),
                            pageEntity.size(),
                            pageEntity.total()
                    );
                });
    }

}
