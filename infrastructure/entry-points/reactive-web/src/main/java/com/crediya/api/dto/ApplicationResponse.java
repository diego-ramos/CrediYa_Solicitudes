package com.crediya.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ApplicationResponse (
    @Schema(description = "User Identification Number", example = "79948037")
    Integer identificationNumber,

    @Schema(description = "User email", example = "darp@test.com")
    String email,

    @Schema(description = "Loan Amount", example = "10000000")
    BigDecimal amount,

    @Schema(description = "Term of loan in months", example = "12")
    Integer term,

    @Schema(description = "Interest rate", example = "9.5")
    Float interestRate,


    @Schema(description = "Application Status", example = "Approved")
    String applicationStatus,

    @Schema(description = "Loan Type", example = "Personal")
    String loanType

){}
