package com.crediya.api;

import com.crediya.api.dto.ApplicationRequest;
import com.crediya.api.dto.ApplicationStatusUpdateRequest;
import com.crediya.api.dto.ListApplicationsRequest;
import com.crediya.api.mapper.ApplicationMapper;
import com.crediya.model.exception.BusinessException;
import com.crediya.model.exception.TechnicalException;
import com.crediya.model.pagination.Page;
import com.crediya.model.pagination.PageRequest;
import com.crediya.usecase.application.ApplicationUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Tag(name = "Application", description = "Endpoints related with loan applications")
public class ApplicationHandlerV1 {

    private final ApplicationUseCase applicationUseCase;
    private final ApplicationMapper mapper;
    private final Validator validator;

    public Mono<ServerResponse> newApplication(ServerRequest serverRequest) {

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                var authentication = securityContext.getAuthentication();
                String tokenEmail;

                if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                    Jwt jwt = jwtAuth.getToken();
                    tokenEmail = jwt.getSubject();
                } else {
                    tokenEmail = null;
                }

                return serverRequest.bodyToMono(ApplicationRequest.class)
                    .flatMap(dto -> {
                        var violations = validator.validate(dto);
                        if (!violations.isEmpty()) {
                            String errorMsg = violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .reduce((a, b) -> a + "; " + b)
                                    .orElse(Constants.INVALID_REQUEST);
                            return ServerResponse.badRequest().bodyValue(errorMsg);
                        }

                        return applicationUseCase
                                .newApplication(mapper.toModel(dto), tokenEmail) // ✅ pass email
                                .doOnSuccess(saved -> log.info(Constants.APPLICATION_REGISTER_SUCCESS, saved))
                                .doOnError(e -> log.error(Constants.ERROR_REGISTERING_APPLICATION, e))
                                .flatMap(saved -> ServerResponse.ok().bodyValue(mapper.toResponse(saved)))
                                .onErrorResume(BusinessException.class,
                                        e -> ServerResponse.badRequest().bodyValue(e.getBusinessErrorMessage().toString()))
                                .onErrorResume(TechnicalException.class,
                                        e -> ServerResponse.status(500).bodyValue(e.getTechnicalErrorMessage().toString()));
                    });
            });
    }

    public Mono<ServerResponse> listApplications(ServerRequest serverRequest) {
        ListApplicationsRequest dto = new ListApplicationsRequest();

        // page param
        dto.setPage(Integer.parseInt(serverRequest.queryParam("page").orElse("0")));

        // size param
        dto.setSize(Integer.parseInt(serverRequest.queryParam("size").orElse("10")));

        // statusIds param (comma-separated)
        dto.setStatusIds(
                serverRequest.queryParam("statusIds")
                        .map(ids -> Arrays.stream(ids.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(Integer::parseInt)
                                .toList()
                        )
                        .orElse(List.of())
        );

        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse(Constants.INVALID_REQUEST);
            return ServerResponse.badRequest().bodyValue(errorMsg);
        }

        PageRequest pageRequest = new PageRequest(dto.getPage(), dto.getSize());

        return applicationUseCase.listApplications(dto.getStatusIds(), pageRequest)
                .doOnNext(p -> log.info(Constants.RETURNING_APPLICATIONS_PAGE, p.page(), p.size(), p.total()))
                .doOnError(e -> log.error(Constants.ERROR_GETTING_APPLICATIONS, e))
                .map(appPage -> new Page<>(
                        appPage.content().stream()
                                .map(mapper::toResponse)
                                .toList(),
                        appPage.page(),
                        appPage.size(),
                        appPage.total()
                ))
                .flatMap(appPage -> ServerResponse.ok().bodyValue(appPage));
    }

    public Mono<ServerResponse> updateApplicationStatus(ServerRequest serverRequest) {

        return serverRequest.bodyToMono(ApplicationStatusUpdateRequest.class)
            .doOnNext(user -> log.info(Constants.APPLICATION_STATUS_UPDATE_REQUEST, user))
            .flatMap(dto -> {
                var violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    String errorMsg = violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .reduce((a, b) -> a + "; " + b)
                            .orElse(Constants.INVALID_REQUEST);
                    return ServerResponse.badRequest().bodyValue(errorMsg);
                }

                return applicationUseCase
                        .updateApplicationStatus(dto.id(), dto.applicationNewStatusId())
                        .doOnSuccess(saved -> log.info(Constants.RETURNING_APPLICATION, saved))
                        .doOnError(e -> log.error(Constants.ERROR_UPDATING_APPLICATION_STATUS, e))
                        .flatMap(saved -> ServerResponse.ok().bodyValue(mapper.toResponse(saved)))
                        .onErrorResume(BusinessException.class,
                                e -> ServerResponse.badRequest().bodyValue(e.getBusinessErrorMessage().toString()))
                        .onErrorResume(TechnicalException.class,
                                e -> ServerResponse.status(500).bodyValue(e.getTechnicalErrorMessage().toString()));
            });
    }

}
