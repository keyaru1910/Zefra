package com.paycore.api.dto;

import com.paycore.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Phản hồi xác thực chứa JWT token")
public record AuthResponse(
        @Schema(description = "JWT Bearer Access Token")
        String accessToken,

        @Schema(description = "Loại token", example = "Bearer")
        String tokenType,

        @Schema(description = "Thời gian hết hạn tính bằng mili-giây", example = "3600000")
        long expiresInMs,

        @Schema(description = "Tên người dùng đã xác thực", example = "nguyen.van.an")
        String username,

        @Schema(description = "Vai trò người dùng", example = "USER")
        UserRole role
) {}
