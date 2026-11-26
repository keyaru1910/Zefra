package com.paycore.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Chiều hạch toán bút toán kép (Ghi Nợ / Ghi Có)")
public enum EntryDirection {
    DEBIT,
    CREDIT
}
