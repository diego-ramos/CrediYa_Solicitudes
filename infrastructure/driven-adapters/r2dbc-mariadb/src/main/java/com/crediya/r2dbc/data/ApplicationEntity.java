package com.crediya.r2dbc.data;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Table("application")
public class ApplicationEntity implements Persistable<Long> {
    @Transient
    private boolean isNew = true; // default new

    @Id
    @Column("id")
    private Long id;

    @Column("user_identification_number")
    private Integer userIdentificationNumber;

    @Column("amount")
    private BigDecimal amount;

    @Column("term")
    private Integer term;

    @Column("email")
    private String email;

    @Column("id_status")
    private Integer statusId;

    @Column("id_loan_type")
    private Integer loanTypeId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
