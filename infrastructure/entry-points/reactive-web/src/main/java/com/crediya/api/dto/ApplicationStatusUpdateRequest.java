package com.crediya.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusUpdateRequest(

    @Schema(description = "Application Id", example = "1")
    @NotNull(message = "Application Id is required")
    Integer id,

    @Schema(description = "Application Status Id", example = "1")
    @NotNull(message = "Application Status Id is required")
    Long applicationNewStatusId
){}
