package com.paycore.domain.enums;

import org.springframework.http.HttpStatus;

/**
 * Bảng mã lỗi chuẩn hóa của hệ thống PayCore dùng chung cho tầng Domain và API.
 */
public enum ErrorCode {

    // Lỗi dữ liệu đầu vào & xác thực (4xx)
    VALIDATION_ERROR("PAYCORE_400_VALIDATION", HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    MALFORMED_REQUEST("PAYCORE_400_MALFORMED", HttpStatus.BAD_REQUEST, "Định dạng JSON hoặc tham số không hợp lệ"),
    CURRENCY_MISMATCH("PAYCORE_400_CURRENCY_MISMATCH", HttpStatus.BAD_REQUEST, "Loại tiền tệ không khớp"),
    UNAUTHORIZED("PAYCORE_401_UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Chưa xác thực hoặc token không hợp lệ"),
    FORBIDDEN("PAYCORE_403_FORBIDDEN", HttpStatus.FORBIDDEN, "Không có quyền truy cập tài nguyên này"),
    RESOURCE_NOT_FOUND("PAYCORE_404_NOT_FOUND", HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên yêu cầu"),
    DUPLICATE_IDEMPOTENCY_KEY("PAYCORE_409_IDEMPOTENCY", HttpStatus.CONFLICT, "Khóa idempotency đã được sử dụng với nội dung khác"),
    CONCURRENT_MODIFICATION("PAYCORE_409_CONCURRENCY", HttpStatus.CONFLICT, "Xung đột cập nhật dữ liệu đồng thời, vui lòng thử lại"),

    // Lỗi nghiệp vụ tài chính (422)
    INSUFFICIENT_BALANCE("PAYCORE_422_INSUFFICIENT_BALANCE", HttpStatus.UNPROCESSABLE_ENTITY, "Số dư khả dụng không đủ"),
    ACCOUNT_INACTIVE("PAYCORE_422_ACCOUNT_INACTIVE", HttpStatus.UNPROCESSABLE_ENTITY, "Tài khoản đang bị đóng băng hoặc đã đóng"),
    FRAUD_BLOCKED("PAYCORE_422_FRAUD_BLOCKED", HttpStatus.UNPROCESSABLE_ENTITY, "Giao dịch bị từ chối bởi hệ thống phòng chống gian lận"),
    LEDGER_UNBALANCED("PAYCORE_500_LEDGER_UNBALANCED", HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi toàn vẹn sổ cái: tổng Nợ không bằng tổng Có"),

    // Lỗi hệ thống (5xx)
    INTERNAL_ERROR("PAYCORE_500_INTERNAL", HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống nội bộ");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
