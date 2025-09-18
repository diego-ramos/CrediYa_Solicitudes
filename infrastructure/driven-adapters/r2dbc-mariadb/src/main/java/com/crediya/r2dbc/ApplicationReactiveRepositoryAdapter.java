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

import java.math.BigDecimal;
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
    private static final long APPROVED_STATUS_ID = 3L;
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
    public Mono<Page<Application>> findAllByApplicationStatusIds(List<Integer> statusIds, int userIdNumber, PageRequest pageRequest) {
        Criteria criteria = Criteria.where("id_status").in(statusIds);

        if(userIdNumber > 0) {
            criteria = criteria.and("user_identification_number").is(userIdNumber);
        }

        if (pageRequest == null) {
            if (userIdNumber > 0) {
                return repository.findAllByApplicationStatusIdIsInAndIdentificationNumber(statusIds, userIdNumber)
                        .map(this::toEntity)
                        .collectList()
                        .map(applications -> new Page<>(
                                applications,
                                0,                        // page index = 0
                                applications.size(),      // size = total size
                                applications.size()       // total = same as size
                        ));
            }
            else {
                return repository.findAllByApplicationStatusIdIn(statusIds)
                        .map(this::toEntity)
                        .collectList()
                        .map(applications -> new Page<>(
                                applications,
                                0,                        // page index = 0
                                applications.size(),      // size = total size
                                applications.size()       // total = same as size
                        ));
            }
        }

        return paginator.paginate(criteria, ApplicationEntity.class, pageRequest)
                .map(pageEntity -> {
                    List<Application> content = pageEntity.content()
                            .stream()
                            .map(this::toEntity) // mapper
                            .toList();

                    return new Page<>(
                            content,
                            pageEntity.page(),
                            pageEntity.size(),
                            pageEntity.total()
                    );
                });
    }


    @Override
    public Mono<Application> updateApplication(Application application) {
        return repository.save(toData(application))
            .map(this::toEntity)
            .onErrorMap(e -> new TechnicalException(e, TechnicalErrorMessage.APPLICATION_SAVE));
    }

    @Override
    public Mono<Application> findById(Integer applicationId) {
        return repository.findById(applicationId)
            .map(this::toEntity)
            .onErrorMap(e -> new TechnicalException(e, TechnicalErrorMessage.APPLICATION_FIND_BY_ID));

    }

    @Override
    public Mono<Long> countByApplicationStatusId(long statusId) {
        return repository.countAllByApplicationStatusId(statusId);
    }

    @Override
    public Mono<BigDecimal> approvedApplicationsTotalAmount() {
        return repository.applicationsTotalAmount(APPROVED_STATUS_ID);
    }

}
