package com.crediya.r2dbc;

import com.crediya.model.applicationstatus.ApplicationStatus;
import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import com.crediya.r2dbc.data.ApplicationStatusEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ApplicationStatusReactiveRepositoryAdapterTest {

    @Mock
    private ApplicationStatusReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private ApplicationStatusReactiveRepositoryAdapter adapter;

    private ApplicationStatus status;
    private ApplicationStatusEntity entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        status = new ApplicationStatus();
        // populate some fields if needed

        entity = new ApplicationStatusEntity();
        // populate entity fields if needed

        // mock mapper: entity -> domain
        when(mapper.map(any(ApplicationStatusEntity.class), any())).thenReturn(status);
    }

    @Test
    void shouldFindByIdSuccessfully() {
        // Arrange
        when(repository.findFirstById(1L)).thenReturn(Mono.just(entity));

        // Act
        Mono<ApplicationStatus> result = adapter.findById(1L);

        // Assert
        StepVerifier.create(result)
                .expectNext(status)
                .verifyComplete();
    }

    @Test
    void shouldReturnTechnicalExceptionWhenFindByIdFails() {
        // Arrange
        when(repository.findFirstById(1L))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act
        Mono<ApplicationStatus> result = adapter.findById(1L);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof TechnicalException;
                    TechnicalException te = (TechnicalException) error;
                    assert te.getTechnicalErrorMessage() == TechnicalErrorMessage.STATUS_ID_FIND;
                })
                .verify();
    }
}
