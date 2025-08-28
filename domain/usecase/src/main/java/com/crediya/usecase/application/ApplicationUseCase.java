package com.crediya.usecase.application;

import com.crediya.model.application.Application;
import com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ApplicationUseCase {

    private final UserRepository repository;

    public Mono<Application> newApplication(Application application){

        //System.out.println(repository.findByIdentificationNumber(application.getUserIdentificationNumber()));
        repository.findByIdentificationNumber(application.getIdentificationNumber())
                .doOnNext(user -> System.out.println("Got user: " + user)) // peek at value
                .map(user -> user.getEmail())  // transform inside stream
                .subscribe(email -> System.out.println("User email: " + email));
        return Mono.empty();
    }

}
