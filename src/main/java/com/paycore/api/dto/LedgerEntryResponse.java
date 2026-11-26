package com.paycore.api.dto;

import com.paycore.domain.Currency;
import com.paycore.domain.enums.EntryDirection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Chi tiết một dòng bút toán kép trên sổ cái")
public record LedgerEntryResponse(
        @Schema(description = "ID dòng bút toán")
        UUID id,

        @Schema(description = "ID chứng từ hạch toán tổng")
        UUID ledgerTransactionId,

        @Schema(description = "ID tài khoản chịu tác động")
        UUID accountId,

        @Schema(description = "Chiều bút toán (DEBIT / CREDIT)")
        EntryDirection entryDirection,

        @Schema(description = "Số tiền hạch toán")
        BigDecimal amount,

        @Schema(description = "Loại tiền tệ")
        Currency currency,

        @Schema(description = "Số dư trước khi ghi sổ")
        BigDecimal balanceBefore,

        @Schema(description = "Số dư sau khi ghi sổ")
        BigDecimal balanceAfter,

        @Schema(description = "Thời điểm ghi sổ (UTC)")
        Instant createdAt
) {}
