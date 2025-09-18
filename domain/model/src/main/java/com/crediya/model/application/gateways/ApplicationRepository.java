package com.crediya.model.application.gateways;

import com.crediya.model.application.Application;
import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface ApplicationRepository {
    Mono<Application> newApplication(Application application);
    Mono<Page<Application>> findAllByApplicationStatusIds(List<Integer> statusIds, int userIdNumber, PageRequest request);
    Mono<Application> updateApplication(Application application);
    Mono<Application> findById(Integer applicationId);
    Mono<Long> countByApplicationStatusId(long statusId);
    Mono<BigDecimal> approvedApplicationsTotalAmount();
}
