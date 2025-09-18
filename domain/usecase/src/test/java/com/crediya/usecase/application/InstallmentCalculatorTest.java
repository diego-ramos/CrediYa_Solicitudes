package com.crediya.usecase.application;

import com.crediya.model.application.Application;
import com.crediya.model.application.FirstInstallment;
import com.crediya.model.loantype.LoanType;
import com.crediya.usecase.helper.InstallmentCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class InstallmentCalculatorTest {

    @Test
    void shouldCalculateFirstInstallmentCorrectly() {
        // Arrange
        Application app = new Application();
        app.setId(1L);
        app.setAmount(BigDecimal.valueOf(10000)); // capital
        app.setTerm(12); // 12 months

        LoanType loanType = new LoanType();
        loanType.setInterestRate(12.0F); // 12% annual
        app.setLoanType(loanType);

        // Act
        FirstInstallment installment = InstallmentCalculator.calculateFirstInstallment(app);

        // Assert
        assertThat(installment).isNotNull();
        assertThat(installment.loanId()).isEqualTo(1L);

        // Approximate values (depends on formula correctness)
        assertThat(installment.monthlyPayment())
                .isEqualByComparingTo(BigDecimal.valueOf(888.49)); // expected ~888.49
        assertThat(installment.interest())
                .isEqualByComparingTo(BigDecimal.valueOf(100.00)); // first month interest = 10,000 * 1% = 100
        assertThat(installment.principal())
                .isEqualByComparingTo(BigDecimal.valueOf(788.49)); // rest goes to principal
    }
}
