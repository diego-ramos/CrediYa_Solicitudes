package com.crediya.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CustomJwtAuthenticationConverterTest {

    private final CustomJwtAuthenticationConverter converter = new CustomJwtAuthenticationConverter();

    @Test
    void shouldConvertJwtWithRoles() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of("USER", "ADMIN"))
                .build();

        Mono<AbstractAuthenticationToken> result = converter.convert(jwt);

        StepVerifier.create(result)
                .assertNext(auth -> {
                    assertThat(auth.getAuthorities())
                            .extracting("authority")
                            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
                    assertThat(auth.getCredentials()).isEqualTo(jwt);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenRolesClaimIsNull() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user") // add at least one claim
                .build();

        Mono<AbstractAuthenticationToken> result = converter.convert(jwt);

        StepVerifier.create(result)
                .assertNext(auth -> assertThat(auth.getAuthorities()).isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenRolesClaimIsEmpty() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of())
                .build();

        Mono<AbstractAuthenticationToken> result = converter.convert(jwt);

        StepVerifier.create(result)
                .assertNext(auth -> assertThat(auth.getAuthorities()).isEmpty())
                .verifyComplete();
    }
}

