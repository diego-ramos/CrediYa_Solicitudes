package com.crediya.usecase.application;

import com.crediya.model.application.Application;
import com.crediya.model.application.gateways.ApplicationRepository;
import com.crediya.model.applicationstatus.ApplicationStatus;
import com.crediya.model.applicationstatus.gateways.ApplicationStatusRepository;
import com.crediya.model.exception.BusinessException;
import com.crediya.model.exception.message.BusinessErrorMessage;
import com.crediya.model.loantype.LoanType;
import com.crediya.model.loantype.gateways.LoanTypeRepository;
import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class ApplicationUseCase {
    private static final long REVISION_PENDING_LOAN_STATUS = 1L;
    private static final List<String> PENDING_STATUS_NAMES = List.of("Pendiente de revisión", "Rechazada", "Revision manual");


    private final UserRepository userRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusRepository applicationStatusRepository;

    public Mono<Application> newApplication(Application application, String tokenEmail) {
        return Mono.zip(
                        userRepository.findByIdentificationNumber(application.getIdentificationNumber())
                                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorMessage.USER_NOT_FOUND))),
                        loanTypeRepository.findById(application.getLoanTypeId())
                                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorMessage.INVALID_LOAN_TYPE))),
                        applicationStatusRepository.findById(REVISION_PENDING_LOAN_STATUS)
                                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorMessage.APPLICATION_STATUS_NOT_FOUND)))
                )
                .flatMap(tuple -> {
                    var user = tuple.getT1();
                    var loanType = tuple.getT2();
                    var status = tuple.getT3();

                    if (!user.getEmail().equalsIgnoreCase(tokenEmail)) {
                        return Mono.error(new BusinessException(BusinessErrorMessage.USER_EMAIL_MISMATCH));
                    }

                    application.setEmail(user.getEmail());
                    application.setApplicationStatusId(REVISION_PENDING_LOAN_STATUS);

                    return applicationRepository.newApplication(application)
                            .map(saved -> {
                                saved.setLoanType(loanType);
                                saved.setApplicationStatus(status);
                                return saved;
                            });
                });
    }

    public Mono<Page<Application>> listApplications(List<Integer> statusIds,PageRequest pageRequest) {
        return applicationRepository.findAllByApplicationStatusIds(statusIds, pageRequest)
            .flatMap(page ->
                    Flux.fromIterable(page.content()) // `content()` since it's a record
                            .flatMap(application -> {
                                Mono<ApplicationStatus> statusMono =
                                        applicationStatusRepository.findById(application.getApplicationStatusId());

                                Mono<LoanType> loanTypeMono =
                                        loanTypeRepository.findById(application.getLoanTypeId());

                                return Mono.zip(statusMono, loanTypeMono)
                                        .map(tuple -> {
                                            application.setApplicationStatus(tuple.getT1());
                                            application.setLoanType(tuple.getT2());
                                            return application;
                                        });
                            })
                            .collectList()
                            .map(enrichedApplications -> new Page<>(
                                    enrichedApplications,
                                    page.page(),   // current page number
                                    page.size(),   // current page size
                                    page.total()   // total count
                            ))
            );
    }


}
