package com.paycore.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin đăng nhập để lấy JWT Access Token")
public record LoginRequest(
        @Schema(description = "Tên đăng nhập hoặc email", example = "nguyen.van.an", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(max = 150, message = "Tên đăng nhập tối đa 150 ký tự")
        String username,

        @Schema(description = "Mật khẩu tài khoản", example = "PayCore@2026", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Mật khẩu không được để trống")
        String password
) {}
