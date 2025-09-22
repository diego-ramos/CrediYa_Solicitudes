package com.crediya.consumer;

import com.crediya.model.exception.BusinessException;
import com.crediya.model.exception.TechnicalException;
import com.crediya.model.exception.message.BusinessErrorMessage;
import com.crediya.model.exception.message.TechnicalErrorMessage;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationRestConsumer implements UserRepository {
    private static final String SEARCH_USER_BY_IDENTIFICATION_NUMBER_URI = "api/v1/usuarios/identification-number/{x}";
    private static final String GET_ALL_ADMINS_EMAILS = "api/v1/usuarios/get-admins-emails";
    private static final String LOGIN_URL = "api/v1/usuarios/login";
    private final WebClient client;
    private final ObjectMapper mapper;

    @Value("${adapter.restconsumer.username}")
    private String username;

    @Value("${adapter.restconsumer.password}")
    private String password;

    @Override
    @CircuitBreaker(name = "usuarios/identification-number", fallbackMethod = "fallbackUser")
    public Mono<User> findByIdentificationNumber(Integer identificationNumber) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (JwtAuthenticationToken) ctx.getAuthentication())
                .map(auth -> auth.getToken().getTokenValue())
                .flatMap(token ->
                        client.get()
                                .uri(SEARCH_USER_BY_IDENTIFICATION_NUMBER_URI, Map.of("x", identificationNumber))
                                .headers(headers -> headers.setBearerAuth(token))
                                .retrieve()
                                .bodyToMono(User.class)
                                .doOnSubscribe(sub -> log.info(Constants.REQUESTING_USER_WITH_ID_NUMBER, identificationNumber))
                                .doOnNext(user -> log.info(Constants.RECEIVED_USER, user))
                                .switchIfEmpty(Mono.error(new BusinessException(BusinessErrorMessage.USER_NOT_FOUND)))
                                .doOnSuccess(user -> log.info(Constants.FINAL_USER_READY, user))
                );
    }

    public Mono<User> fallbackUser(Integer identificationNumber, Throwable ex) {
        log.error(Constants.CIRCUIT_BREAKER_OPENED_FOR_ID, identificationNumber, ex.getMessage());

        if (ex instanceof WebClientResponseException.NotFound) {
            // For 404, just return empty
            return Mono.empty();
        } else {
            // For other errors, propagate as TechnicalException
            return Mono.error(new TechnicalException(ex, TechnicalErrorMessage.USER_IDENTIFICATION_NUMBER_FIND));
        }
    }

    @Override
    public Flux<String> findAllAdminEmails() {
        AuthRequest authRequest = new AuthRequest(username, password);

        return client.post()
                .uri(LOGIN_URL)
                .bodyValue(authRequest)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .map(AuthResponse::token)
                .flatMapMany(token ->
                        client.get()
                                .uri(GET_ALL_ADMINS_EMAILS)
                                .headers(headers -> headers.setBearerAuth(token))
                                .retrieve()
                                .bodyToMono(String.class)  // 👈 obtenemos el string crudo
                                .flatMapMany(json -> {
                                    try {
                                        List<String> emails = mapper.readValue(
                                                json, new TypeReference<List<String>>() {}
                                        );
                                        return Flux.fromIterable(emails);
                                    } catch (Exception e) {
                                        return Flux.error(new BusinessException(
                                                BusinessErrorMessage.ADMIN_EMAILS_NOT_FOUND
                                        ));
                                    }
                                })
                                .doOnSubscribe(sub -> log.info(Constants.REQUESTING_ADMIN_EMAILS))
                                .doOnNext(email -> log.info(Constants.RECEIVED_EMAILS, email))
                                .switchIfEmpty(Mono.error(new BusinessException(
                                        BusinessErrorMessage.ADMIN_EMAILS_NOT_FOUND)))
                );
    }
}
