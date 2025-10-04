package com.crediya.r2dbc.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Table("loan_type")
public class LoanTypeEntity implements Persistable<Long> {
    @Transient
    private boolean isNew = true; // default new

    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("minAmount")
    private BigDecimal minAmount;

    @Column("maxAmount")
    private BigDecimal maxAmount;

    @Column("interestRate")
    private Float interestRate;

    @Column("autoValidation")
    private Boolean autoValidation;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
