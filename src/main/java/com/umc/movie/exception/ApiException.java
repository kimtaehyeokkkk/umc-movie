package com.umc.movie.exception;

import lombok.Getter;

// Service나 인터셉터처럼 컨트롤러 안쪽에서 실패를 발견했을 때 던지는 예외입니다.
// 상태 코드, code, message는 ErrorCode가 함께 들고 있어서, 던지는 쪽은 "무엇이 잘못됐는지"만 고르면 됩니다.
@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
