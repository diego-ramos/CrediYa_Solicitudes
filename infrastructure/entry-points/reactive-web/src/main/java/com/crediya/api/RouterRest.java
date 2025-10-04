package com.crediya.api;

import com.crediya.api.dto.ApplicationRequest;
import com.crediya.api.dto.ApplicationResponse;
import com.crediya.api.dto.ApplicationStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;


@Configuration
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = ApplicationHandlerV1.class,
                    beanMethod = "newApplication",
                    operation = @Operation(
                            operationId = "newApplication",
                            summary = "Registrar una nueva solicitud de préstramo",
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    required = true,
                                    description = "Datos de la solicitud de préstamo a registrar",
                                    content = @Content(schema = @Schema(implementation = ApplicationRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Solicitud de préstamo registrado exitosamente", content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Error de validación"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = ApplicationHandlerV1.class,
                    beanMethod = "listApplications",
                    operation = @Operation(
                            operationId = "listApplications",
                            summary = "List loan applications",
                            parameters = {
                                    @Parameter(name = "page", description = "Page", required = false),
                                    @Parameter(name = "size", description = "records per page", required = false),
                                    @Parameter(name = "statusIds", description = "application status list to be query", required = true, example = "1,3,4")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Return applications list successfully", content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Error de validación"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.PUT,
                    beanClass = ApplicationHandlerV1.class,
                    beanMethod = "updateApplicationStatus",
                    operation = @Operation(
                            operationId = "updateApplicationStatus",
                            summary = "Updates application status",
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    required = true,
                                    description = "Datos de la solicitud de préstamo a actualizar",
                                    content = @Content(schema = @Schema(implementation = ApplicationStatusUpdateRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Return application updated", content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Error de validación"),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(ApplicationHandlerV1 applicationHandlerV1) {
        return RouterFunctions
            .route()
                .path("/api/v1", builder -> builder
                    .POST("/solicitud", applicationHandlerV1::newApplication)
                    .GET("/solicitud", applicationHandlerV1::listApplications)
                    .PUT("/solicitud", applicationHandlerV1::updateApplicationStatus))
            .build();
    }
}
