package com.paycore.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái hoạt động của tài khoản")
public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}
