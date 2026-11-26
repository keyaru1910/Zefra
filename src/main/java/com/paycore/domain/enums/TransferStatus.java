package com.paycore.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Trạng thái vòng đời của giao dịch chuyển tiền")
public enum TransferStatus {
    INITIATED,
    FRAUD_CHECK_PENDING,
    APPROVED,
    PROCESSING,
    COMPLETED,
    FAILED,
    REVERSED,
    BLOCKED
}
