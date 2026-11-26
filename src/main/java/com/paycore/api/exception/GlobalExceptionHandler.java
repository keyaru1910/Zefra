package com.paycore.api.exception;

import com.paycore.api.dto.ErrorResponse;
import com.paycore.api.dto.ErrorResponse.FieldViolation;
import com.paycore.domain.enums.ErrorCode;
import com.paycore.domain.exception.PaycoreException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

/**
 * Bộ xử lý ngoại lệ tập trung cho toàn bộ REST API của PayCore.
 * Chuẩn hóa mọi lỗi về cấu trúc {@link ErrorResponse} và che giấu stack trace nội bộ khi trả về client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PaycoreException.class)
    public ResponseEntity<ErrorResponse> handlePaycoreException(
            PaycoreException ex,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Lỗi nghiệp vụ [{} - {}] tại {}: {}",
                errorCode.getCode(), ex.getMessage(), request.getRequestURI(), ex.getClass().getSimpleName());

        return buildResponse(errorCode, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldViolation)
                .toList();

        return buildResponse(
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                request,
                violations
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(cv -> new FieldViolation(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();

        return buildResponse(
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                request,
                violations
        );
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception ex,
            HttpServletRequest request
    ) {
        log.debug("Yêu cầu không hợp lệ tại {}: {}", request.getRequestURI(), ex.getMessage());
        String detail = ex instanceof MissingRequestHeaderException || ex instanceof IllegalArgumentException
                ? ex.getMessage()
                : ErrorCode.MALFORMED_REQUEST.getDefaultMessage();

        return buildResponse(ErrorCode.MALFORMED_REQUEST, detail, request, List.of());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            OptimisticLockingFailureException ex,
            HttpServletRequest request
    ) {
        log.warn("Xung đột optimistic lock tại {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                ErrorCode.CONCURRENT_MODIFICATION,
                ErrorCode.CONCURRENT_MODIFICATION.getDefaultMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED.getDefaultMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                ErrorCode.FORBIDDEN,
                ErrorCode.FORBIDDEN.getDefaultMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Lỗi hệ thống không mong muốn tại {}", request.getRequestURI(), ex);
        return buildResponse(
                ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                request,
                List.of()
        );
    }

    private FieldViolation toFieldViolation(FieldError fieldError) {
        String message = fieldError.getDefaultMessage() != null
                ? fieldError.getDefaultMessage()
                : "Giá trị không hợp lệ";
        return new FieldViolation(fieldError.getField(), message);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode,
            String message,
            HttpServletRequest request,
            List<FieldViolation> violations
    ) {
        HttpStatus status = errorCode.getHttpStatus();
        ErrorResponse body = new ErrorResponse(
                errorCode.getCode(),
                message,
                status.value(),
                request.getRequestURI(),
                Instant.now(),
                violations.isEmpty() ? null : violations
        );
        return ResponseEntity.status(status).body(body);
    }
}
