package com.crediya.usecase.application;

import com.crediya.model.application.Application;
import com.crediya.model.application.gateways.ApplicationRepository;
import com.crediya.model.applicationstatus.gateways.ApplicationStatusRepository;
import com.crediya.model.exception.BusinessException;
import com.crediya.model.exception.message.BusinessErrorMessage;
import com.crediya.model.loantype.gateways.LoanTypeRepository;
import com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ApplicationUseCase {
    private static final long REVISION_PENDING_LOAN_STATUS = 1L;

    private final UserRepository userRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusRepository applicationStatusRepository;

    public Mono<Application> newApplication(Application application) {
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
}
