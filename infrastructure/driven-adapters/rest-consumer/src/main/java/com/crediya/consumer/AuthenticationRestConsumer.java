package com.crediya.consumer;

import com.crediya.model.exception.BusinessException;
import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.BusinessErrorMessage;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationRestConsumer implements UserRepository {
    private static final String SEARCH_USER_BY_IDENTIFICATION_NUMBER_URI = "api/v1/usuarios/identification-number/{x}";
    private final WebClient client;

    @Override
    @CircuitBreaker(name = "usuarios/identification-number", fallbackMethod = "fallbackUser")
    public Mono<User> findByIdentificationNumber(Integer identificationNumber) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
                .map(auth -> auth.getToken().getTokenValue())
                .flatMap(token ->
                        client.get()
                                .uri(SEARCH_USER_BY_IDENTIFICATION_NUMBER_URI, identificationNumber)
                                .headers(headers -> headers.setBearerAuth(token))
                                .retrieve()
                                .bodyToMono(User.class)
                                .doOnSubscribe(sub -> log.info(Constants.REQUESTINNG_USER_WITH_ID_NUMBER, identificationNumber))
                                .doOnNext(user -> log.info(Constants.RECEIVED_USER, user))
                                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorMessage.USER_NOT_FOUND)))
                                .map(user -> {
                                    if (user == null) {
                                        log.warn(Constants.USER_NOT_FOUND_WITH_ID_NUMBER, identificationNumber);
                                        throw new BusinessException(BusinessErrorMessage.USER_NOT_FOUND);
                                    }
                                    return user;
                                })
                                .onErrorMap(WebClientResponseException.NotFound.class,
                                        ex -> new TechnicalException(ex, TechnicalErrorMessage.USER_IDENTIFICATION_NUMBER_FIND))
                                .doOnSuccess(user -> log.info(Constants.FINAL_USER_READY, user))
                );
    }

    public Mono<User> fallbackUser(Integer identificationNumber, Throwable ex) {
        log.error(Constants.CIRCUIT_BREAKER_OPENED_FOR_ID, identificationNumber, ex.getMessage());
        return Mono.empty();
    }
}
