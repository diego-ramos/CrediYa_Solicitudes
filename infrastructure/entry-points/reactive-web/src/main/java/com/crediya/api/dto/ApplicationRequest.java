package com.crediya.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApplicationRequest (
    @Schema(description = "User Identification Number", example = "79948037")
    @NotNull(message = "Identification number is required")
    Integer identificationNumber,

    @Schema(description = "Loan Amount", example = "10000000")
    @NotNull(message = "amount is required")
    BigDecimal amount,

    @Schema(description = "Term of loan in months", example = "12")
    @NotNull(message = "term is required")
    Integer term,

    @Schema(description = "Loan Type id", example = "1")
    @NotNull(message = "Loan Type is required")
    Integer loanTypeId

){}
