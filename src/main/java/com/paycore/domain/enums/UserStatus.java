package com.paycore.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái tài khoản người dùng")
public enum UserStatus {
    ACTIVE,
    FROZEN,
    SUSPENDED,
    CLOSED
}
