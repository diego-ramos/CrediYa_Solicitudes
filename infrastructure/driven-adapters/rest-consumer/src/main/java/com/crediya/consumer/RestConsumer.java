package com.crediya.consumer;

import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements UserRepository {
    private final String SEARCH_USER_BY_IDENTIFICATION_NUMBER_URI = "api/v1/usuarios/identification-number/{x}"
;    private final WebClient client;


    @Override
    @CircuitBreaker(name = "usuarios/identification-number")
    public Mono<User> findByIdentificationNumber(Integer identificationNumber) {
//        Integer identificationNumber2 = 79948037;
        return client.get()
                .uri(SEARCH_USER_BY_IDENTIFICATION_NUMBER_URI, identificationNumber)
                .retrieve()
                .bodyToMono(User.class)
                .doOnSubscribe(sub -> log.info("➡️ Requesting user with idNumber={}", identificationNumber))
                .doOnNext(user -> log.info("✅ Received User: {}", user))
                .doOnError(error -> log.error("❌ Error while fetching user with idNumber={}: {}", identificationNumber, error.getMessage(), error))
                .doOnSuccess(user -> {
                    if (user == null) {
                        log.warn("⚠️ No user found with idNumber={}", identificationNumber);
                    }
                });

//        Mono<User> monoUser = client.get()
//                .uri("api/v1/usuarios/identification-number/{x}", identificationNumber2)
//                .retrieve()
//                .bodyToMono(User.class)
//                .doOnSubscribe(sub -> System.out.println("➡️ Subscribed for id=" + identificationNumber))
//                .doOnNext(user -> System.out.println("✅ Received User: " + user))
//                .doOnError(err -> System.out.println("❌ Error: " + err.getMessage()));
//
//        // DEBUG ONLY: force execution
//        User user = monoUser.block();
//        System.out.println("👉 Blocked result = " + user);
//
//        return Mono.justOrEmpty(user);
    }

// Possible fallback method
//    public Mono<String> testGetOk(Exception ignored) {
//        return client
//                .get() // TODO: change for another endpoint or destination
//                .retrieve()
//                .bodyToMono(String.class);
//    }

//    @CircuitBreaker(name = "testPost")
//    public Mono<ObjectResponse> testPost() {
//        ObjectRequest request = ObjectRequest.builder()
//            .val1("exampleval1")
//            .val2("exampleval2")
//            .build();
//        return client
//                .post()
//                .body(Mono.just(request), ObjectRequest.class)
//                .retrieve()
//                .bodyToMono(ObjectResponse.class);
//    }
}
