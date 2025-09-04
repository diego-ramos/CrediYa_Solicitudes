package com.crediya.r2dbc.pagination;

import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaginatorTest {

    @Mock
    private R2dbcEntityTemplate template;

    @InjectMocks
    private Paginator paginator;

    @Test
    void shouldPaginateResults() {
        // Arrange
        Criteria criteria = Criteria.where("id_status").is(1);
        PageRequest pageRequest = new PageRequest(0, 2);

        List<String> entities = List.of("A", "B");
        long totalCount = 5L;

        when(template.select(any(Query.class), eq(String.class)))
                .thenReturn(Flux.fromIterable(entities));
        when(template.count(any(Query.class), eq(String.class)))
                .thenReturn(Mono.just(totalCount));

        // Act
        Mono<Page<String>> result = paginator.paginate(criteria, String.class, pageRequest);

        // Assert
        StepVerifier.create(result)
                .assertNext(page -> {
                    assertThat(page.content()).containsExactly("A", "B");
                    assertThat(page.page()).isEqualTo(0);
                    assertThat(page.size()).isEqualTo(2);
                    assertThat(page.total()).isEqualTo(5);
                })
                .verifyComplete();

        verify(template).select(any(Query.class), eq(String.class));
        verify(template).count(any(Query.class), eq(String.class));
    }
}


