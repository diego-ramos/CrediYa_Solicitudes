package com.crediya.model.exception;

import com.crediya.model.exception.message.BusinessErrorMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BusinessException extends RuntimeException {

    private final BusinessErrorMessage businessErrorMessage;
}
