package com.crediya.model.application;

import com.crediya.model.applicationstatus.ApplicationStatus;
import com.crediya.model.loantype.LoanType;
import com.crediya.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Application {
    private Long id;
    private Integer identificationNumber;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private Long applicationStatusId;
    private ApplicationStatus applicationStatus;
    private Long loanTypeId;
    private LoanType loanType;
    private User user;
}
