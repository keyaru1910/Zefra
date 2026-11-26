package com.paycore.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Loại giao dịch chuyển tiền")
public enum TransferType {
    P2P,
    DEPOSIT,
    WITHDRAWAL,
    MERCHANT_PAYMENT,
    FEE_COLLECTION,
    REVERSAL
}
