package com.crediya.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest (
    @Schema(description = "User Identification Number", example = "79948037")
    @NotNull(message = "Identification number is required")
    Integer identificationNumber

){}
