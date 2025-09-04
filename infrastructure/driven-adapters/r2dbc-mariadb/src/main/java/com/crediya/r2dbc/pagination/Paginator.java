package com.crediya.r2dbc.pagination;

import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Paginator {

    private final R2dbcEntityTemplate template;

    public <T> Mono<Page<T>> paginate(
            Criteria criteria,   // dynamic filters
            Class<T> type,
            PageRequest request
    ) {
        Query query = Query.query(criteria);

        Query pagedQuery = query
                .limit(request.size())
                .offset(request.offset());

        Mono<Long> total = template.count(query, type);

        return template.select(pagedQuery, type)
                .collectList()
                .zipWith(total)
                .map(tuple -> new Page<>(
                        tuple.getT1(),
                        request.page(),
                        request.size(),
                        tuple.getT2()
                ));
    }
}
