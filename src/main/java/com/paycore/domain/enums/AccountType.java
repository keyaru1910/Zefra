package com.paycore.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Phân loại tài khoản trong hệ thống sổ cái PayCore")
public enum AccountType {
    WALLET,
    MERCHANT_SETTLEMENT,
    SYSTEM_FEE,
    SYSTEM_SUSPENSE,
    CLEARING
}
