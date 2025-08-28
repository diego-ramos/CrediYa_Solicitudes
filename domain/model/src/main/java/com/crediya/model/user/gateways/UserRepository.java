package com.crediya.model.user.gateways;

import com.crediya.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {

   // Mono<User> findByEmail(String email);

    Mono<User> findByIdentificationNumber(Integer idNumber);
}
