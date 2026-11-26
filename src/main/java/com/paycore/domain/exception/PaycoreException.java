package com.paycore.domain.exception;

import com.paycore.domain.enums.ErrorCode;

/**
 * Ngoại lệ gốc (Base Exception) cho toàn bộ lỗi nghiệp vụ trong PayCore.
 */
public class PaycoreException extends RuntimeException {

    private final ErrorCode errorCode;

    public PaycoreException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public PaycoreException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }

    public PaycoreException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(detailMessage, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
