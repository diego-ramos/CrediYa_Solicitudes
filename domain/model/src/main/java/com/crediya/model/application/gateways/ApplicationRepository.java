package com.crediya.model.application.gateways;

import com.crediya.model.application.Application;
import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplicationRepository {
    Mono<Application> newApplication(Application application);
    Mono<Page<Application>> findAllByApplicationStatusIds(List<Integer> statusIds, PageRequest request);
}
