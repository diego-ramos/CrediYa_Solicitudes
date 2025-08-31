package com.crediya.model.exception.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.crediya.model.exception.Constants.VERIFY_YOUR_DATA;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorMessage {
    INVALID_LOAN_TYPE(
            "BUSS_ERR_S001", "Invalid loan type ", VERIFY_YOUR_DATA
    ),
    INVALID_EMAIL(
            "BUSS_ERR_S002", "Invalid email", VERIFY_YOUR_DATA
    ),
    INVALID_USER_IDENTIFICATION_NUMBER(
            "BUSS_ERR_S003", "Invalid identification number", VERIFY_YOUR_DATA
    ),
    USER_NOT_FOUND(
            "BUSS_ERR_S004", "User not found", VERIFY_YOUR_DATA
    ),
    APPLICATION_STATUS_NOT_FOUND(
            "BUSS_ERR_S005", "Application Status not found", VERIFY_YOUR_DATA
    );

    private final String code;
    private final String description;
    private final String message;

    @Override
    public String toString(){
        return code + ": " + description + ": " + message;
    }
}
