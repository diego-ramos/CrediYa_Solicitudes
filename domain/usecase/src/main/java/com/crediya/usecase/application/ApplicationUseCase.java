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
import com.crediya.model.sqsmessage.SqsApplicationUpdateMessage;
import com.crediya.model.sqsmessage.gateway.SqsMessagePublisher;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;

@RequiredArgsConstructor
public class ApplicationUseCase {
    private static final long REVISION_PENDING_LOAN_STATUS = 1L;


    private final UserRepository userRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final SqsMessagePublisher sqsMessagePublisher;

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

                                Mono<User> user =
                                        userRepository.findByIdentificationNumber(application.getIdentificationNumber());

                                return Mono.zip(statusMono, loanTypeMono, user)
                                        .map(tuple -> {
                                            application.setApplicationStatus(tuple.getT1());
                                            application.setLoanType(tuple.getT2());
                                            application.setUser(tuple.getT3());
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

    public Mono<Application> updateApplicationStatus(int applicationId, long newApplicationStatusId) {
        return Mono.zip(
                        applicationRepository.findById(applicationId)
                                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorMessage.APPLICATION_NOT_FOUND))),
                        applicationStatusRepository.findById(newApplicationStatusId)
                                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorMessage.APPLICATION_STATUS_NOT_FOUND)))
                )
                .flatMap(tuple -> {
                    var application = tuple.getT1();
                    var newStatus = tuple.getT2();

                    application.setApplicationStatus(newStatus);
                    application.setApplicationStatusId(newApplicationStatusId);

                    // Return both saved application and status together
                    return applicationRepository.updateApplication(application)
                            .map(saved -> Tuples.of(saved, newStatus));
                })
                .flatMap(tuple -> {
                    Application saved = tuple.getT1();
                    ApplicationStatus status = tuple.getT2();

                    Mono<User> userMono =
                            userRepository.findByIdentificationNumber(saved.getIdentificationNumber());

                    Mono<LoanType> loanTypeMono =
                            loanTypeRepository.findById(saved.getLoanTypeId());

                    return Mono.zip(userMono, loanTypeMono)
                            .map(tuple2 -> {
                                saved.setUser(tuple2.getT1());
                                saved.setLoanType(tuple2.getT2());
                                saved.setApplicationStatus(status); // reuse existing status
                                return saved;
                            });
                })
                .flatMap(enriched -> {
                    SqsApplicationUpdateMessage message = SqsApplicationUpdateMessage.builder()
                            .applicationId(enriched.getId())
                            .email(enriched.getUser() != null ? enriched.getUser().getEmail() : null)
                            .newApplicationStatus(enriched.getApplicationStatus() != null ? enriched.getApplicationStatus().getName(): null)
                            .build();

                    return sqsMessagePublisher.send(message)
                            .onErrorResume(e -> {
                                System.out.println("Send Message failed: " + e.getMessage());
                                return Mono.empty(); // ignore errors
                            })
                            .thenReturn(enriched);
                });
    }

}
