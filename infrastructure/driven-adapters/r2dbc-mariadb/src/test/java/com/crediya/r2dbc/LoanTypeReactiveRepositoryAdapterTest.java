package com.crediya.r2dbc;

import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import com.crediya.model.loantype.LoanType;
import com.crediya.r2dbc.data.LoanTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoanTypeReactiveRepositoryAdapterTest {

    @Mock
    private LoanTypeReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private LoanTypeReactiveRepositoryAdapter adapter;

    private LoanType loanType;
    private LoanTypeEntity entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        loanType = new LoanType();
        // set sample fields if your LoanType has them

        entity = new LoanTypeEntity();
        // set entity fields if needed

        // mapping mock: LoanTypeEntity -> LoanType
        when(mapper.map(any(LoanTypeEntity.class), any())).thenReturn(loanType);
    }

    @Test
    void shouldFindByIdSuccessfully() {
        // Arrange
        when(repository.findFirstById(1L)).thenReturn(Mono.just(entity));

        // Act
        Mono<LoanType> result = adapter.findById(1L);

        // Assert
        StepVerifier.create(result)
                .expectNext(loanType)
                .verifyComplete();

        verify(repository).findFirstById(1L);
        verify(mapper).map(entity, LoanType.class);
    }

    @Test
    void shouldReturnTechnicalExceptionWhenFindByIdFails() {
        // Arrange
        when(repository.findFirstById(1L))
                .thenReturn(Mono.error(new RuntimeException("DB failure")));

        // Act
        Mono<LoanType> result = adapter.findById(1L);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof TechnicalException;
                    TechnicalException te = (TechnicalException) error;
                    assert te.getTechnicalErrorMessage() == TechnicalErrorMessage.LOAN_TYPE_ID_FIND;
                })
                .verify();

        verify(repository).findFirstById(1L);
    }
}

