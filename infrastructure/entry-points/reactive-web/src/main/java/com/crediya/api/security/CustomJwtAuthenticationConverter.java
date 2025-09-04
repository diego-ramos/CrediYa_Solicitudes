package com.crediya.api.security;

import com.crediya.api.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CustomJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final static String ROLE_CLAIM = "roles";
    private final static String ROLE_PREFIX = "";

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(ROLE_CLAIM);
        if (roles == null) {
            return List.of();
        }

        Collection<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role)) // must start with "ROLE_"
                .collect(Collectors.toList());

        authorities.forEach(a -> log.debug(Constants.GRANTED_AUTHORITY + " " + a.getAuthority()));

        return authorities;
    }
}
