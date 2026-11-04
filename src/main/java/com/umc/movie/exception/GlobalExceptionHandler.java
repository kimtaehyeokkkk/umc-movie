package com.umc.movie.exception;

import com.umc.movie.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // MySQL 에러 코드 1062 = ER_DUP_ENTRY (UNIQUE/PK 중복)
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    // @Valid @RequestBody DTO 검증에 실패했을 때 (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return toResponse(ErrorCode.INVALID_INPUT, message);
    }

    // @PathVariable, @RequestParam에 붙인 @Size, @Email, @Min 검증에 실패했을 때 (400)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleParameterValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        return toResponse(ErrorCode.INVALID_INPUT, message);
    }

    // /members/abc/ratings처럼 숫자 자리에 문자가 들어왔을 때 (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return toResponse(ErrorCode.INVALID_TYPE, e.getName() + " 값의 형식이 올바르지 않습니다.");
    }

    // JSON 문법이 틀렸거나 "score": "abc"처럼 타입이 맞지 않을 때 (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return toResponse(ErrorCode.INVALID_JSON, ErrorCode.INVALID_JSON.getMessage());
    }

    // Service에서 던진 업무 규칙 위반 (404, 403, 409)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return toResponse(e.getErrorCode(), e.getErrorCode().getMessage());
    }

    // Service의 사전 중복 체크를 통과했는데 DB UNIQUE 제약에서 막혔을 때 (409)
    // 동시에 들어온 요청처럼 사전 체크만으로는 막을 수 없는 경우의 마지막 안전망입니다.
    // 길이 초과·NULL 같은 다른 무결성 오류는 중복이 아니므로 409가 아니라 400으로 응답합니다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();
        if (!(cause instanceof SQLException sqlException) || sqlException.getErrorCode() != MYSQL_DUPLICATE_ENTRY) {
            return toResponse(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getMessage());
        }

        ErrorCode errorCode = resolveConflict(sqlException.getMessage());
        return toResponse(errorCode, errorCode.getMessage());
    }

    // MySQL 메시지 예: Duplicate entry 'a@umc.com' for key 'member.email'
    // 어떤 UNIQUE 키에서 막혔는지 보고, 사전 체크와 같은 code로 응답합니다.
    private ErrorCode resolveConflict(String detail) {
        if (detail == null) {
            return ErrorCode.DATA_CONFLICT;
        }
        int keyIndex = detail.lastIndexOf("for key '");
        if (keyIndex < 0) {
            return ErrorCode.DATA_CONFLICT;
        }
        String key = detail.substring(keyIndex + "for key '".length()).replace("'", "");

        if (key.endsWith("email")) {
            return ErrorCode.EMAIL_DUPLICATED;
        }
        if (key.endsWith("nickname")) {
            return ErrorCode.NICKNAME_DUPLICATED;
        }
        if (key.endsWith("uk_rating_member_movie")) {
            return ErrorCode.RATING_DUPLICATED;
        }
        return ErrorCode.DATA_CONFLICT;
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), message));
    }
}
