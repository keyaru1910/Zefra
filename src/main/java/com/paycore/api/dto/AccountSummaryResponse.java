package com.paycore.api.dto;

import com.paycore.domain.Currency;
import com.paycore.domain.enums.AccountStatus;
import com.paycore.domain.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Thông tin tổng quan tài khoản / ví tiền")
public record AccountSummaryResponse(
        @Schema(description = "ID tài khoản")
        UUID id,

        @Schema(description = "Số tài khoản duy nhất", example = "ZEF-VND-10000001")
        String accountNumber,

        @Schema(description = "Loại tài khoản")
        AccountType accountType,

        @Schema(description = "Loại tiền tệ")
        Currency currency,

        @Schema(description = "Số dư sổ cái hiện tại")
        BigDecimal balance,

        @Schema(description = "Số dư khả dụng được phép chi tiêu")
        BigDecimal availableBalance,

        @Schema(description = "Trạng thái tài khoản")
        AccountStatus status,

        @Schema(description = "Thời điểm cập nhật gần nhất (UTC)")
        Instant updatedAt
) {}
