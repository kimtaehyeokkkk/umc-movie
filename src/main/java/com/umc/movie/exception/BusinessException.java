package com.umc.movie.exception;

import lombok.Getter;

// Service에서 업무 규칙을 어겼을 때 던지는 예외입니다. 어떤 실패인지는 ErrorCode가 알려줍니다.
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
