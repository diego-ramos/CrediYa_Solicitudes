package com.crediya.model.application;
import com.crediya.model.applicationstatus.ApplicationStatus;
import com.crediya.model.loantype.LoanType;
import lombok.*;

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
    private ApplicationStatus idStatus;
    private Integer loanTypeId;
    private LoanType loanType;
}
