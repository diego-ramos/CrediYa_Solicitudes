package com.crediya.usecase.application;

import com.crediya.model.application.Application;
import com.crediya.model.application.gateways.ApplicationRepository;
import com.crediya.model.applicationstatus.ApplicationStatus;
import com.crediya.model.applicationstatus.gateways.ApplicationStatusRepository;
import com.crediya.model.exception.BusinessException;
import com.crediya.model.exception.message.BusinessErrorMessage;
import com.crediya.model.loantype.LoanType;
import com.crediya.model.loantype.gateways.LoanTypeRepository;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationUseCaseTest {

    private UserRepository userRepository;
    private LoanTypeRepository loanTypeRepository;
    private ApplicationRepository applicationRepository;
    private ApplicationStatusRepository applicationStatusRepository;
    private ApplicationUseCase applicationUseCase;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        loanTypeRepository = Mockito.mock(LoanTypeRepository.class);
        applicationRepository = Mockito.mock(ApplicationRepository.class);
        applicationStatusRepository = Mockito.mock(ApplicationStatusRepository.class);

        applicationUseCase = new ApplicationUseCase(
                userRepository,
                loanTypeRepository,
                applicationRepository,
                applicationStatusRepository
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
        Mono<Application> result = applicationUseCase.newApplication(application);

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
        Mono<Application> result = applicationUseCase.newApplication(application);

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
        Mono<Application> result = applicationUseCase.newApplication(application);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex -> ex instanceof BusinessException &&
                        ((BusinessException) ex).getBusinessErrorMessage() == BusinessErrorMessage.INVALID_LOAN_TYPE)
                .verify();
    }

}
