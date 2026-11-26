package com.paycore.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Loại chứng từ hạch toán trên sổ cái")
public enum LedgerTransactionType {
    TRANSFER,
    FEE,
    REVERSAL,
    ADJUSTMENT
}
