package com.paycore.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Payload lỗi chuẩn trả về từ {@code GlobalExceptionHandler}.
 */
@Schema(description = "Cấu trúc phản hồi lỗi chuẩn hóa của hệ thống")
public record ErrorResponse(
        @Schema(description = "Mã lỗi nghiệp vụ nội bộ", example = "PAYCORE_422_INSUFFICIENT_BALANCE")
        String code,

        @Schema(description = "Mô tả lỗi chi tiết", example = "Số dư khả dụng không đủ")
        String message,

        @Schema(description = "HTTP Status Code", example = "422")
        int status,

        @Schema(description = "Đường dẫn API phát sinh lỗi", example = "/api/v1/transfers")
        String path,

        @Schema(description = "Thời điểm xảy ra lỗi (UTC)", example = "2026-11-26T00:30:00Z")
        Instant timestamp,

        @Schema(description = "Danh sách lỗi chi tiết theo từng trường dữ liệu (nếu có)")
        List<FieldViolation> fieldErrors
) {
    @Schema(description = "Chi tiết lỗi validation trên một trường cụ thể")
    public record FieldViolation(
            @Schema(description = "Tên trường bị lỗi", example = "amount")
            String field,

            @Schema(description = "Thông báo lỗi của trường", example = "Số tiền giao dịch phải lớn hơn 0")
            String message
    ) {}
}
