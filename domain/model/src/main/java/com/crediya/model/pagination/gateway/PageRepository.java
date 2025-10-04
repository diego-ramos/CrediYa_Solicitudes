package com.crediya.model.pagination.gateway;

import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import reactor.core.publisher.Mono;

public interface PageRepository {
    <T> Mono<Page<T>> paginate(PageRequest request, Class<T> type, Object criteria);
}
