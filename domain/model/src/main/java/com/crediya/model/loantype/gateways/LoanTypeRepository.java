package com.crediya.model.loantype.gateways;

import com.crediya.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<LoanType> findById(Long id);
}
