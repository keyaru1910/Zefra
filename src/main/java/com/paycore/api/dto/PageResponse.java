package com.paycore.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Cấu trúc dữ liệu phân trang chuẩn")
public record PageResponse<T>(
        @Schema(description = "Danh sách phần tử của trang hiện tại")
        List<T> items,

        @Schema(description = "Số trang hiện tại (bắt đầu từ 0)", example = "0")
        int page,

        @Schema(description = "Kích thước trang", example = "20")
        int size,

        @Schema(description = "Tổng số phần tử", example = "4")
        long totalElements,

        @Schema(description = "Tổng số trang", example = "1")
        int totalPages
) {}
