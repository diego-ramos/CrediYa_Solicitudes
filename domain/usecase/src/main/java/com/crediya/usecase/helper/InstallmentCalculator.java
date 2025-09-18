package com.crediya.usecase.helper;

import com.crediya.model.application.Application;
import com.crediya.model.application.FirstInstallment;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class InstallmentCalculator {

    public static FirstInstallment calculateFirstInstallment(Application application) {
        BigDecimal amount = application.getAmount(); // Capital
        BigDecimal annualRate = BigDecimal.valueOf(application.getLoanType().getInterestRate());
        int n = application.getTerm(); // número de meses

        // i = tasa mensual en decimal
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP) // pasar % a decimal
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP); // mensual

        BigDecimal onePlusRatePow = (BigDecimal.ONE.add(monthlyRate)).pow(n, MathContext.DECIMAL64);

        // fórmula de amortización: C = P * [ i * (1+i)^n ] / [ (1+i)^n - 1 ]
        BigDecimal numerator = amount.multiply(monthlyRate).multiply(onePlusRatePow);
        BigDecimal denominator = onePlusRatePow.subtract(BigDecimal.ONE);

        BigDecimal monthlyPayment = numerator.divide(denominator, 10, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);

        // intereses primera cuota
        BigDecimal interest = amount.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);

        // abono a capital
        BigDecimal principal = monthlyPayment.subtract(interest).setScale(2, RoundingMode.HALF_UP);

        return new FirstInstallment(application.getId(), monthlyPayment, interest, principal);
    }
}
