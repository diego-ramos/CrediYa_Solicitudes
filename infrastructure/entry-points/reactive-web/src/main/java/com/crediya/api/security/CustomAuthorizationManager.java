package com.crediya.api.security;

import com.crediya.api.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String PATH_CLAIM = "path";
    private static final String METHOD_CLAIM = "method";
    private static final String ROLE_CLAIM = "role";
    private static final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication,
                                             AuthorizationContext context) {
        String method = context.getExchange().getRequest().getMethod().name();
        String path = context.getExchange().getRequest().getPath().value();

        return authentication
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    boolean allowed = isAllowed(auth, path, method);
                    log.info(Constants.AUTHORIZATION_CHECK,
                            auth.getName(), path, method, allowed);
                    return new AuthorizationDecision(allowed);
                })
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    private boolean isAllowed(Authentication auth, String path, String method) {
        Jwt jwt = (Jwt) auth.getPrincipal();

        List<Map<String, Object>> permissions = jwt.getClaim(PERMISSIONS_CLAIM);
        if (permissions == null) {
            log.warn(Constants.NO_PERMISSIONS_CLAIM, jwt.getSubject());
            return false;
        }

        return permissions.stream().anyMatch(p ->
                matcher.match((String) p.get(PATH_CLAIM), path) && // Ant-style matching
                        method.equalsIgnoreCase((String) p.get(METHOD_CLAIM)) &&
                        auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(p.get(ROLE_CLAIM)))
        );
    }
}
