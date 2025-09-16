package com.crediya.model.application;

import java.math.BigDecimal;

public record FirstInstallment(
        Long loanId,
        BigDecimal monthlyPayment,
        BigDecimal interest,
        BigDecimal principal
) {}
