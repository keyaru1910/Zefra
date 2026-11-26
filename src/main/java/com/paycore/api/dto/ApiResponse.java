package com.paycore.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Wrapper chuẩn cho mọi response thành công từ PayCore API.
 */
@Schema(description = "Cấu trúc phản hồi chuẩn của API PayCore")
public record ApiResponse<T>(
        @Schema(description = "Trạng thái thành công", example = "true")
        boolean success,

        @Schema(description = "Thông điệp mô tả kết quả", example = "Xử lý thành công")
        String message,

        @Schema(description = "Dữ liệu trả về")
        T data,

        @Schema(description = "Thời điểm phản hồi theo chuẩn UTC", example = "2026-11-26T00:30:00Z")
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok("Thành công", data);
    }
}
