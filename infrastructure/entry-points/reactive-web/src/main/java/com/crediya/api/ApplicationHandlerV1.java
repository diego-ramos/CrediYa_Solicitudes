package com.crediya.api;

import com.crediya.api.dto.ApplicationRequest;
import com.crediya.api.mapper.ApplicationMapper;
import com.crediya.model.exception.BusinessException;
import com.crediya.model.exception.TechnicalException;
import com.crediya.usecase.application.ApplicationUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@Tag(name = "Application", description = "Endpoints related with loan applications")
public class ApplicationHandlerV1 {

    private final ApplicationUseCase applicationUseCasenUseCase;
    private final ApplicationMapper mapper;
    private final Validator validator;

    public Mono<ServerResponse> newApplication(ServerRequest serverRequest) {

        return  serverRequest.bodyToMono(ApplicationRequest.class)
                .flatMap(dto -> {
                    var violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        String errorMsg = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .reduce((a, b) -> a + "; " + b)
                                .orElse(Constants.INVALID_REQUEST);
                        return ServerResponse.badRequest().bodyValue(errorMsg);
                    }
                    return applicationUseCasenUseCase.newApplication(mapper.toModel(dto))
                            .doOnSuccess(saved -> log.info(Constants.APPLICATION_REGISTER_SUCCESS, saved))
                            .doOnError(e -> log.error(Constants.ERROR_REGISTERING_APPLICATION, e))
                            .flatMap(saved -> ServerResponse.ok().bodyValue(saved))
                            .onErrorResume(BusinessException.class, e ->ServerResponse.badRequest().bodyValue(e.getBusinessErrorMessage().toString()))
                            .onErrorResume(TechnicalException.class,
                                    e -> ServerResponse.status(500).bodyValue(e.getTechnicalErrorMessage().toString()));
                });
    }
}
