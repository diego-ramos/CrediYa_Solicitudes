package com.crediya.model.exception.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.crediya.model.exception.Constants.A_SYSTEM_FAILURE_OCCURRED;

@Getter
@RequiredArgsConstructor
public enum TechnicalErrorMessage {

    APPLICATION_SAVE(
            "SOL_ERR_001", "Error registering application", A_SYSTEM_FAILURE_OCCURRED
    ),
    USER_EMPTY(
            "SOL_ERR_002", "Repository returned empty", A_SYSTEM_FAILURE_OCCURRED
    ),
    USER_EMAIL_FIND(
            "SOL_ERR_003", "Error finding user by email", A_SYSTEM_FAILURE_OCCURRED
    ),
    USER_IDENTIFICATION_NUMBER_FIND(
            "SOL_ERR_004", "Error finding user by identification number", A_SYSTEM_FAILURE_OCCURRED
    ),
    LOAN_TYPE_ID_FIND(
            "SOL_ERR_005", "Error finding loan type by id", A_SYSTEM_FAILURE_OCCURRED
    ),
    STATUS_ID_FIND(
            "SOL_ERR_006", "Error finding loan status by id", A_SYSTEM_FAILURE_OCCURRED
    );

    private final String code;
    private final String description;
    private final String message;

    @Override
    public String toString(){
        return code + ": " + description + ": " + message;
    }
}

