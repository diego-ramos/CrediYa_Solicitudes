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
import com.crediya.model.sqsmessage.SqsCheckDebtCapacityMessage;
import com.crediya.model.sqsmessage.SqsTotalsMessage;
import com.crediya.model.sqsmessage.gateway.SqsMessagePublisher;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApplicationUseCaseTest {

    private UserRepository userRepository;
    private LoanTypeRepository loanTypeRepository;
    private ApplicationRepository applicationRepository;
    private ApplicationStatusRepository applicationStatusRepository;
    private ApplicationUseCase applicationUseCase;
    private SqsMessagePublisher<SqsApplicationUpdateMessage> sqsApplicationUpdateMessagePublisher;
    private SqsMessagePublisher<SqsCheckDebtCapacityMessage> sqsCheckDebtCapacityMessagePublisher;
    private SqsMessagePublisher<SqsTotalsMessage> sqsTotalsMessagePublisher;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        loanTypeRepository = Mockito.mock(LoanTypeRepository.class);
        applicationRepository = Mockito.mock(ApplicationRepository.class);
        applicationStatusRepository = Mockito.mock(ApplicationStatusRepository.class);
        sqsApplicationUpdateMessagePublisher = Mockito.mock(SqsMessagePublisher.class);
        sqsCheckDebtCapacityMessagePublisher = Mockito.mock(SqsMessagePublisher.class);

        applicationUseCase = new ApplicationUseCase(
                userRepository,
                loanTypeRepository,
                applicationRepository,
                applicationStatusRepository,
                sqsApplicationUpdateMessagePublisher,
                sqsCheckDebtCapacityMessagePublisher,
                sqsTotalsMessagePublisher
        );
    }

    @Test
    void shouldCreateNewApplicationSuccessfully() {
        // Arrange
        Application application = new Application();
        application.setIdentificationNumber(123);
        application.setLoanTypeId(10L);

        User user = new User();
        user.setIdentificationNumber(123);
        user.setEmail("test@mail.com");

        LoanType loanType = new LoanType();
        loanType.setId(10L);
        loanType.setName("Personal Loan");

        Application saved = new Application();
        saved.setId(1L);
        saved.setIdentificationNumber(123);

        ApplicationStatus status = new ApplicationStatus();
        status.setId(1);
        status.setName("Revision Pending");

        when(userRepository.findByIdentificationNumber(123)).thenReturn(Mono.just(user));
        when(loanTypeRepository.findById(10L)).thenReturn(Mono.just(loanType));
        when(applicationRepository.newApplication(any(Application.class))).thenReturn(Mono.just(saved));
        when(applicationStatusRepository.findById(1L)).thenReturn(Mono.just(status));

        // Act
        Mono<Application> result = applicationUseCase.newApplication(application, "test@mail.com");

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(app ->
                        app.getLoanType() != null &&
                                "Personal Loan".equals(app.getLoanType().getName()) &&
                                app.getApplicationStatus() != null &&
                                "Revision Pending".equals(app.getApplicationStatus().getName())
                )
                .verifyComplete();

        verify(applicationRepository).newApplication(any(Application.class));
    }

    @Test
    void shouldReturnErrorWhenUserNotFound() {
        // Arrange
        Application application = new Application();
        application.setIdentificationNumber(999);
        application.setLoanTypeId(10L);

        when(userRepository.findByIdentificationNumber(999)).thenReturn(Mono.empty());
        when(loanTypeRepository.findById(10L)).thenReturn(Mono.just(new LoanType())); // 👈 prevent NPE
        when(applicationStatusRepository.findById(anyLong()))
                .thenReturn(Mono.just(new ApplicationStatus(1, "Revision Pending", null)));

        // Act
        Mono<Application> result = applicationUseCase.newApplication(application, "test@mail.com");

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        ((BusinessException) e).getBusinessErrorMessage() == BusinessErrorMessage.USER_NOT_FOUND)
                .verify();
    }

    @Test
    void shouldReturnErrorWhenLoanTypeNotFound() {
        // Arrange
        Application application = new Application();
        application.setIdentificationNumber(123);
        application.setLoanTypeId(99L);

        User user = new User();
        user.setIdentificationNumber(123);

        when(userRepository.findByIdentificationNumber(123)).thenReturn(Mono.just(user));
        when(loanTypeRepository.findById(99L)).thenReturn(Mono.empty());
        when(applicationStatusRepository.findById(1L))
                .thenReturn(Mono.just(new ApplicationStatus(1, "Revision Pending", null)));

        // Act
        Mono<Application> result = applicationUseCase.newApplication(application,"test@mail.com");

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof BusinessException &&
                        ((BusinessException) ex).getBusinessErrorMessage() == BusinessErrorMessage.INVALID_LOAN_TYPE)
                .verify();
    }

    @Test
    void shouldListPendingApplicationsWithPagination() {
        // Arrange
        Application application = new Application();
        application.setIdentificationNumber(123);
        application.setLoanTypeId(10L);
        application.setApplicationStatusId(1L); // make sure this matches the status id

        ApplicationStatus status = new ApplicationStatus();
        status.setId(1);
        status.setName("Revision Pending");

        LoanType loanType = new LoanType();
        loanType.setId(10L);
        loanType.setName("Personal Loan");

        Page<Application> mockPage = new Page<>(
                List.of(application),
                0, // page
                10, // size
                1   // total
        );

        User user =  new User();
        user.setIdentificationNumber(123);

        // Mock repositories
        when(applicationRepository.findAllByApplicationStatusIds(anyList(), anyInt(), any(PageRequest.class)))
                .thenReturn(Mono.just(mockPage));

        when(applicationStatusRepository.findById(1L))
                .thenReturn(Mono.just(status));

        when(loanTypeRepository.findById(10L))
                .thenReturn(Mono.just(loanType));

        when(userRepository.findByIdentificationNumber(123))
                .thenReturn(Mono.just(user));

        // Act
        Mono<Page<Application>> result = applicationUseCase.listApplications(List.of(1), 1, new PageRequest(0, 10));

        // Assert
        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page.content()).hasSize(1);
                    Application app = page.content().get(0);
                    assertThat(app.getLoanType()).isNotNull();
                    assertThat(app.getLoanType().getName()).isEqualTo("Personal Loan");
                    assertThat(app.getApplicationStatus()).isNotNull();
                    assertThat(app.getApplicationStatus().getName()).isEqualTo("Revision Pending");

                    assertThat(page.page()).isZero();
                    assertThat(page.size()).isEqualTo(10);
                    assertThat(page.total()).isEqualTo(1);
                })
                .verifyComplete();

        verify(applicationRepository).findAllByApplicationStatusIds(anyList(), anyInt(), any(PageRequest.class));
        verify(applicationStatusRepository).findById(1L);
        verify(loanTypeRepository).findById(10L);
    }

    @Test
    void shouldUpdateApplicationStatusAndSendSqsMessage() {
        // Arrange
        Application application = new Application();
        application.setId(1L);
        application.setLoanTypeId(10L);
        application.setIdentificationNumber(123);

        ApplicationStatus newStatus = new ApplicationStatus();
        newStatus.setId(2);
        newStatus.setName("APPROVED");

        LoanType loanType = new LoanType();
        loanType.setId(10L);

        User user = new User();
        user.setEmail("user@test.com");
        user.setIdentificationNumber(456);

        when(applicationRepository.findById(1))
                .thenReturn(Mono.just(application));
        when(applicationStatusRepository.findById(2L))
                .thenReturn(Mono.just(newStatus));
        when(applicationRepository.updateApplication(any(Application.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userRepository.findByIdentificationNumber(123))
                .thenReturn(Mono.just(user));
        when(loanTypeRepository.findById(10L))
                .thenReturn(Mono.just(loanType));
        when(sqsApplicationUpdateMessagePublisher.send(any(SqsApplicationUpdateMessage.class)))
                .thenReturn(Mono.empty());

        when(applicationRepository.findAllByApplicationStatusIds(anyList(), anyInt(), any()))
                .thenReturn(Mono.empty());

        // Act
        Mono<Application> result = applicationUseCase.updateApplicationStatus(1, 2L);

        // Assert
        StepVerifier.create(result)
                .assertNext(app -> {
                    assertThat(app.getApplicationStatus()).isEqualTo(newStatus);
                    assertThat(app.getUser()).isEqualTo(user);
                    assertThat(app.getLoanType()).isEqualTo(loanType);
                })
                .verifyComplete();

        verify(sqsApplicationUpdateMessagePublisher)
                .send(any(SqsApplicationUpdateMessage.class));
    }

    @Test
    void shouldThrowWhenApplicationNotFound() {
        // Arrange
        int appId = 1;
        long statusId = 99L;

        when(applicationRepository.findById(appId))
                .thenReturn(Mono.empty()); // simulate application not found

        // ⚠️ Without this, you'll get NPE
        when(applicationStatusRepository.findById(statusId))
                .thenReturn(Mono.just(new ApplicationStatus())); // safe default

        // Act
        Mono<Application> result = applicationUseCase.updateApplicationStatus(appId, statusId);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BusinessException.class);
                    BusinessException ex = (BusinessException) error;
                    assertThat(ex.getBusinessErrorMessage())
                            .isEqualTo(BusinessErrorMessage.APPLICATION_NOT_FOUND);
                })
                .verify();


        verify(applicationRepository).findById(appId);
        verify(applicationStatusRepository).findById(statusId);
    }


    // --- Test 1: USER_EMAIL_MISMATCH ------------------------------------------------
    @Test
    void shouldFailWhenUserEmailDoesNotMatchTokenEmailInNewApplication() {
        // Arrange
        Application input = new Application();
        input.setIdentificationNumber(123);
        input.setLoanTypeId(10L);

        User user = new User();
        user.setIdentificationNumber(123);
        user.setEmail("real@mail.com");

        LoanType loanType = new LoanType();
        loanType.setId(10L);
        loanType.setAutoValidation(false);

        ApplicationStatus status = new ApplicationStatus();
        status.setId(1);

        when(userRepository.findByIdentificationNumber(123)).thenReturn(Mono.just(user));
        when(loanTypeRepository.findById(10L)).thenReturn(Mono.just(loanType));
        when(applicationStatusRepository.findById(anyLong())).thenReturn(Mono.just(status));
        // applicationRepository.newApplication should NOT be called in this scenario

        // Act
        Mono<Application> result = applicationUseCase.newApplication(input, "wrong@mail.com");

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(BusinessException.class);
                    BusinessException ex = (BusinessException) err;
                    assertThat(ex.getBusinessErrorMessage()).isEqualTo(BusinessErrorMessage.USER_EMAIL_MISMATCH);
                })
                .verify();


        verify(userRepository).findByIdentificationNumber(123);
        verify(loanTypeRepository).findById(10L);
        verify(applicationStatusRepository).findById(anyLong());
        verify(applicationRepository, never()).newApplication(any());
        verifyNoInteractions(sqsCheckDebtCapacityMessagePublisher);
    }

    // --- Test 2: loanType.autoValidation == true -> SQS called ---------------------
    @Test
    void shouldSendSqsWhenLoanTypeAutoValidationTrueInNewApplication() {
        // Arrange
        Application input = new Application();
        input.setIdentificationNumber(321);
        input.setLoanTypeId(11L);

        User user = new User();
        user.setIdentificationNumber(321);
        user.setEmail("user@mail.com");

        LoanType loanType = new LoanType();
        loanType.setId(11L);
        loanType.setAutoValidation(true);

        ApplicationStatus status = new ApplicationStatus();
        status.setId(1);
        status.setName("REVISION");

        // This is the application returned by repository.newApplication(...)
        Application savedFromRepo = new Application();
        savedFromRepo.setId(999L);

        when(userRepository.findByIdentificationNumber(321)).thenReturn(Mono.just(user));
        when(loanTypeRepository.findById(11L)).thenReturn(Mono.just(loanType));
        when(applicationStatusRepository.findById(anyLong())).thenReturn(Mono.just(status));
        when(applicationRepository.newApplication(any(Application.class))).thenReturn(Mono.just(savedFromRepo));
        when(sqsCheckDebtCapacityMessagePublisher.send(any(SqsCheckDebtCapacityMessage.class))).thenReturn(Mono.empty());

        // Act
        Mono<Application> result = applicationUseCase.newApplication(input, "user@mail.com");

        // Assert
        StepVerifier.create(result)
                .assertNext(saved -> {
                    // mapping in your code sets user, loanType and status onto `saved`
                    assertThat(saved).isNotNull();
                    assertThat(saved.getUser()).isNotNull();
                    assertThat(saved.getUser().getEmail()).isEqualTo("user@mail.com");
                    assertThat(saved.getLoanType()).isNotNull();
                    assertThat(saved.getLoanType().getAutoValidation()).isTrue();
                    assertThat(saved.getApplicationStatus()).isNotNull();
                    assertThat(saved.getId()).isEqualTo(999L);
                })
                .verifyComplete();

        verify(applicationRepository).newApplication(any(Application.class));
        verify(sqsCheckDebtCapacityMessagePublisher).send(any(SqsCheckDebtCapacityMessage.class));
    }

    // --- Test 3: loanType.autoValidation == false -> NO SQS ------------------------
    @Test
    void shouldNotSendSqsWhenLoanTypeAutoValidationFalseInNewApplication() {
        // Arrange
        Application input = new Application();
        input.setIdentificationNumber(555);
        input.setLoanTypeId(22L);

        User user = new User();
        user.setIdentificationNumber(555);
        user.setEmail("another@mail.com");

        LoanType loanType = new LoanType();
        loanType.setId(22L);
        loanType.setAutoValidation(false);

        ApplicationStatus status = new ApplicationStatus();
        status.setId(1);

        Application savedFromRepo = new Application();
        savedFromRepo.setId(777L);

        when(userRepository.findByIdentificationNumber(555)).thenReturn(Mono.just(user));
        when(loanTypeRepository.findById(22L)).thenReturn(Mono.just(loanType));
        when(applicationStatusRepository.findById(anyLong())).thenReturn(Mono.just(status));
        when(applicationRepository.newApplication(any(Application.class))).thenReturn(Mono.just(savedFromRepo));

        // Act
        Mono<Application> result = applicationUseCase.newApplication(input, "another@mail.com");

        // Assert
        StepVerifier.create(result)
                .assertNext(saved -> {
                    assertThat(saved.getId()).isEqualTo(777L);
                    assertThat(saved.getLoanType()).isNotNull();
                    assertThat(saved.getLoanType().getAutoValidation()).isFalse();
                    assertThat(saved.getUser()).isNotNull();
                    assertThat(saved.getUser().getEmail()).isEqualTo("another@mail.com");
                })
                .verifyComplete();

        verify(applicationRepository).newApplication(any(Application.class));
        verify(sqsCheckDebtCapacityMessagePublisher, never()).send(any());
    }
}
