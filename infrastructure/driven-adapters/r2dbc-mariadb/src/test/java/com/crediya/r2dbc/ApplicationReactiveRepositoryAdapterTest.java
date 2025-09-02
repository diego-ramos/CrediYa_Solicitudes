package com.crediya.r2dbc;

import com.crediya.model.application.Application;
import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import com.crediya.r2dbc.data.ApplicationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class ApplicationReactiveRepositoryAdapterTest {

    @Mock
    private ApplicationReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private ApplicationReactiveRepositoryAdapter adapter;

    private Application application;
    private ApplicationEntity entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        application = new Application();
        // populate some fields if needed

        entity = new ApplicationEntity();
        // populate entity fields if needed

        // Mock ObjectMapper conversion
        when(mapper.map(any(ApplicationEntity.class), any())).thenReturn(application);
        when(mapper.map(any(Application.class), any())).thenReturn(entity);
    }

    @Test
    void shouldSaveApplicationSuccessfully() {
        // Arrange
        when(repository.save(any(ApplicationEntity.class))).thenReturn(Mono.just(entity));

        // Act
        Mono<Application> result = adapter.newApplication(application);

        // Assert
        StepVerifier.create(result)
                .expectNext(application)
                .verifyComplete();
    }

    @Test
    void shouldReturnTechnicalExceptionWhenSaveFails() {
        // Arrange
        when(repository.save(any(ApplicationEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act
        Mono<Application> result = adapter.newApplication(application);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof TechnicalException;
                    TechnicalException te = (TechnicalException) error;
                    assert te.getTechnicalErrorMessage() == TechnicalErrorMessage.APPLICATION_SAVE;
                })
                .verify();
    }

    @Test
    void shouldFindAllByApplicationStatusIds() {
        // Arrange
        List<Integer> statusIds =  new ArrayList<>();
        statusIds.add(1);

        when(repository.findAllByApplicationStatusIds(anyList())).thenReturn(Flux.just(application));

        // Act
        Flux<Application> result = adapter.findAllByApplicationStatusIds(statusIds);

        // Assert
        StepVerifier.create(result)
                .expectNext(application)
                .verifyComplete();
    }
}
