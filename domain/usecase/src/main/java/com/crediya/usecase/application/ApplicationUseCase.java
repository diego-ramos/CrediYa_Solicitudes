package com.crediya.usecase.application;

import com.crediya.model.application.Application;
import com.crediya.model.exception.BusinessException;
import com.crediya.model.exception.message.BusinessErrorMessage;
import com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ApplicationUseCase {

    private final UserRepository repository;

    public Mono<Application> newApplication(Application application) {
        return repository.findByIdentificationNumber(application.getIdentificationNumber())
                .map(user -> {
                    return application;
                })
                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorMessage.USER_NOT_FOUND)));
    }

}
