package com.crediya.model.user.gateways;

import com.crediya.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> findByIdentificationNumber(Integer idNumber);

    Flux<String> findAllAdminEmails();
}
