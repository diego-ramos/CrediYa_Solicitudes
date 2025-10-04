package com.crediya.r2dbc;

import com.crediya.model.application.Application;
import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import com.crediya.r2dbc.data.ApplicationEntity;
import com.crediya.r2dbc.pagination.Paginator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.relational.core.query.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationReactiveRepositoryAdapterTest {

    @Mock
    private ApplicationReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    Paginator paginator;

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
        List<Integer> statusIds = List.of(1);
        PageRequest pageRequest = new PageRequest(0, 10);

        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(12L);
        applicationEntity.setApplicationStatusId(1);
        applicationEntity.setLoanTypeId(10);

        application.setApplicationStatusId(1L);
        application.setLoanTypeId(10L);

        Page<ApplicationEntity> entityPage = new Page<>(
                List.of(applicationEntity),
                0, // page
                10, // size
                1   // total
        );

        when(paginator.paginate(any(Criteria.class), eq(ApplicationEntity.class), eq(pageRequest)))
                .thenReturn(Mono.just(entityPage));

        // Act
        Mono<Page<Application>> result = adapter.findAllByApplicationStatusIds(statusIds, 1, pageRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page.content()).hasSize(1);
                    Application app = page.content().get(0);
                    assertThat(app.getApplicationStatusId()).isEqualTo(1L);
                    assertThat(app.getLoanTypeId()).isEqualTo(10);

                    assertThat(page.page()).isZero();
                    assertThat(page.size()).isEqualTo(10);
                    assertThat(page.total()).isEqualTo(1);
                })
                .verifyComplete();

        verify(paginator).paginate(any(Criteria.class), eq(ApplicationEntity.class), eq(pageRequest));
    }

    @Test
    void shouldFindAllByApplicationStatusIdsWithUserIdNumberAndNoPageRequest() {
        // Arrange
        List<Integer> statusIds = List.of(1);
        int userIdNumber = 567;

        application.setApplicationStatusId(1L);
        application.setLoanTypeId(10L);

        entity.setId(1L);
        entity.setApplicationStatusId(1);
        entity.setLoanTypeId(10);

        when(repository.findAllByApplicationStatusIdIsInAndIdentificationNumber(statusIds, userIdNumber))
                .thenReturn(Flux.just(entity));

        // Act
        Mono<Page<Application>> result = adapter.findAllByApplicationStatusIds(statusIds, userIdNumber, null);

        // Assert
        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page.content()).hasSize(1);
                    Application app = page.content().get(0);
                    assertThat(app.getApplicationStatusId()).isEqualTo(1);
                    assertThat(app.getLoanTypeId()).isEqualTo(10);

                    assertThat(page.page()).isZero();
                    assertThat(page.size()).isEqualTo(1);
                    assertThat(page.total()).isEqualTo(1);
                })
                .verifyComplete();

        verify(repository).findAllByApplicationStatusIdIsInAndIdentificationNumber(statusIds, userIdNumber);
    }

    @Test
    void shouldFindAllByApplicationStatusIdsWithNoUserIdNumberAndNoPageRequest() {
        // Arrange
        List<Integer> statusIds = List.of(1);
        int userIdNumber = 0; // <= 0

        application.setApplicationStatusId(1L);
        application.setLoanTypeId(20L);

        entity.setId(2L);
        entity.setApplicationStatusId(1);
        entity.setLoanTypeId(20);

        when(repository.findAllByApplicationStatusIdIn(statusIds))
                .thenReturn(Flux.just(entity));

        // Act
        Mono<Page<Application>> result = adapter.findAllByApplicationStatusIds(statusIds, userIdNumber, null);

        // Assert
        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page.content()).hasSize(1);
                    Application app = page.content().get(0);
                    assertThat(app.getApplicationStatusId()).isEqualTo(1L);
                    assertThat(app.getLoanTypeId()).isEqualTo(20L);

                    assertThat(page.page()).isZero();
                    assertThat(page.size()).isEqualTo(1);
                    assertThat(page.total()).isEqualTo(1);
                })
                .verifyComplete();

        verify(repository).findAllByApplicationStatusIdIn(statusIds);
    }

    @Test
    void shouldUpdateApplicationSuccessfully() {
        // Arrange
        application.setId(1L);
        application.setLoanTypeId(10L);

        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(1L);
        entity.setLoanTypeId(10);

        when(repository.save(any(ApplicationEntity.class)))
                .thenReturn(Mono.just(entity));

        // Act
        Mono<Application> result = adapter.updateApplication(application);

        // Assert
        StepVerifier.create(result)
                .assertNext(app -> {
                    assertThat(app.getId()).isEqualTo(1L);
                    assertThat(app.getLoanTypeId()).isEqualTo(10L);
                })
                .verifyComplete();

        verify(repository).save(any(ApplicationEntity.class));
    }

    @Test
    void shouldWrapExceptionWhenUpdateApplicationFails() {
        // Arrange
        Application application = new Application();
        application.setId(1L);
        when(repository.save(any(ApplicationEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act
        Mono<Application> result = adapter.updateApplication(application);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(TechnicalException.class);
                    TechnicalException te = (TechnicalException) error;
                    assertThat(te.getTechnicalErrorMessage())
                            .isEqualTo(TechnicalErrorMessage.APPLICATION_SAVE);
                })
                .verify();

        verify(repository).save(any(ApplicationEntity.class));
    }


    @Test
    void shouldFindByIdSuccessfully() {
        // Arrange
        int appId = 123;
        entity.setId((long) appId);
        entity.setLoanTypeId(5);

        application.setId(123L);
        application.setApplicationStatusId(123L);
        application.setLoanTypeId(5L);

        when(repository.findById(appId))
                .thenReturn(Mono.just(entity));

        // Act
        Mono<Application> result = adapter.findById(appId);

        // Assert
        StepVerifier.create(result)
                .assertNext(app -> {
                    assertThat(app.getId()).isEqualTo(123L);
                    assertThat(app.getLoanTypeId()).isEqualTo(5L);
                })
                .verifyComplete();

        verify(repository).findById(appId);
    }

    @Test
    void shouldWrapExceptionWhenFindByIdFails() {
        // Arrange
        int appId = 123;
        when(repository.findById(appId))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act
        Mono<Application> result = adapter.findById(appId);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(TechnicalException.class);
                    TechnicalException te = (TechnicalException) error;
                    assertThat(te.getTechnicalErrorMessage())
                            .isEqualTo(TechnicalErrorMessage.APPLICATION_FIND_BY_ID);
                })
                .verify();

        verify(repository).findById(appId);
    }


}
