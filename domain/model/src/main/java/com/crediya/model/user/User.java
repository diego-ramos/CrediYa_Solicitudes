package com.crediya.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {

    private Integer idType;
    private Integer identificationNumber;
    private String firstNames;
    private String lastNames;
    private String email ;
    private String phone;
    private BigDecimal baseSalary;
    private LocalDate birthDate;
    private String address;
}
