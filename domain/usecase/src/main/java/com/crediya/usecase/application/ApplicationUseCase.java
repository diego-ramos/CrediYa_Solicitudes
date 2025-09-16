package com.crediya.usecase.application;

import com.crediya.model.application.Application;
import com.crediya.model.application.FirstInstallment;
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
import com.crediya.model.sqsmessage.SqsCheckDebtCapacityMessage;
import com.crediya.model.sqsmessage.gateway.SqsMessagePublisher;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ApplicationUseCase {
    private static final long REVISION_PENDING_LOAN_STATUS = 1L;
    private static final int APPROVED_LOAN_STATUS = 3;


    private final UserRepository userRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final SqsMessagePublisher<SqsApplicationUpdateMessage> sqsApplicationUpdateMessagePublisher;
    private final SqsMessagePublisher<SqsCheckDebtCapacityMessage> sqsCheckDebtCapacityMessagePublisher;

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
                                saved.setUser(user);
                                return saved;
                            });
                })
                .flatMap(saved -> {
                    // 🔎 Check the loanType attribute here
                    if (Boolean.TRUE.equals(saved.getLoanType().getAutoValidation())) {
                        // If true → call AWS SQS (assuming sqsService.send returns Mono<Void>)
                        SqsCheckDebtCapacityMessage message = new SqsCheckDebtCapacityMessage();
                        message.setApplication(saved);
                        return sqsCheckDebtCapacityMessagePublisher.send(message)
                                .thenReturn(saved); // return the application after SQS send
                    }
                    // If false → just continue without SQS

                    return Mono.just(saved);
                });
    }

    public Mono<Page<Application>> listApplications(List<Integer> statusIds,int userIdNumber, PageRequest pageRequest) {
        return applicationRepository.findAllByApplicationStatusIds(statusIds, userIdNumber, pageRequest)
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
                    SqsApplicationUpdateMessage.SqsApplicationUpdateMessageBuilder messageBuilder =
                            SqsApplicationUpdateMessage.builder()
                                    .applicationId(enriched.getId())
                                    .email(enriched.getUser() != null ? enriched.getUser().getEmail() : null)
                                    .newApplicationStatus(enriched.getApplicationStatus() != null
                                            ? enriched.getApplicationStatus().getName()
                                            : null);

                    return applicationRepository.findAllByApplicationStatusIds(
                                    List.of(APPROVED_LOAN_STATUS),
                                    enriched.getUser().getIdentificationNumber(),
                                    null
                            )
                            .flatMapMany(page -> Flux.fromIterable(page.content()))
                            .flatMap(app ->
                                    loanTypeRepository.findById(app.getLoanTypeId())
                                            .map(loanType -> {
                                                app.setLoanType(loanType);
                                                return calculateFirstInstallment(app);
                                            })
                            )
                            .collectList()
                            .defaultIfEmpty(List.of())
                            .flatMap(otherInstallments ->
                                    loanTypeRepository.findById(enriched.getLoanTypeId())
                                            .map(loanType -> {
                                                enriched.setLoanType(loanType);

                                                List<FirstInstallment> installments = new ArrayList<>();
                                                // If you want enriched always included:
                                                // installments.add(calculateFirstInstallment(enriched));
                                                installments.addAll(otherInstallments);

                                                return messageBuilder.installments(installments).build();
                                            })
                            )
                            .flatMap(message ->
                                    sqsApplicationUpdateMessagePublisher.send(message)
                                            .onErrorResume(e -> Mono.empty())
                                            .thenReturn(enriched)
                            );
                });

    }

    private FirstInstallment calculateFirstInstallment(Application application) {
        BigDecimal P = application.getAmount(); // Capital
        BigDecimal annualRate = BigDecimal.valueOf(application.getLoanType().getInterestRate());
        int n = application.getTerm(); // número de meses

        // i = tasa mensual en decimal
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP) // pasar % a decimal
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP); // mensual

        BigDecimal onePlusRatePow = (BigDecimal.ONE.add(monthlyRate)).pow(n, MathContext.DECIMAL64);

        // fórmula de amortización: C = P * [ i * (1+i)^n ] / [ (1+i)^n - 1 ]
        BigDecimal numerator = P.multiply(monthlyRate).multiply(onePlusRatePow);
        BigDecimal denominator = onePlusRatePow.subtract(BigDecimal.ONE);

        BigDecimal monthlyPayment = numerator.divide(denominator, 10, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);

        // intereses primera cuota
        BigDecimal interest = P.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);

        // abono a capital
        BigDecimal principal = monthlyPayment.subtract(interest).setScale(2, RoundingMode.HALF_UP);

        return new FirstInstallment(application.getId(), monthlyPayment, interest, principal);
    }

}
