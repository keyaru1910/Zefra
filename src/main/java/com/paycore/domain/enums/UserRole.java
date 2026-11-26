package com.paycore.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vai trò người dùng trong hệ thống")
public enum UserRole {
    USER,
    MERCHANT,
    COMPLIANCE_OFFICER,
    ADMIN
}
