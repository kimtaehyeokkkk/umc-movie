package com.umc.movie.exception;

import com.umc.movie.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @PathVariable에 붙인 @Size, @Email 검증에 실패했을 때 (400)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return toResponse(HttpStatus.BAD_REQUEST, message);
    }

    // /members/abc/ratings처럼 숫자 자리에 문자가 들어왔을 때 (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return toResponse(HttpStatus.BAD_REQUEST, e.getName() + " 값의 형식이 올바르지 않습니다.");
    }

    // Service에서 던진 ResponseStatusException (예: 존재하지 않는 유저 404)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
        return toResponse(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
    }

    private ResponseEntity<ErrorResponse> toResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message));
    }
}
