package com.paycore.api.dto;

import com.paycore.domain.Currency;
import com.paycore.domain.enums.TransferStatus;
import com.paycore.domain.enums.TransferType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Thông tin chi tiết giao dịch chuyển tiền")
public record TransferResponse(
        @Schema(description = "ID giao dịch", example = "30000000-0000-0000-0000-000000000004")
        UUID id,

        @Schema(description = "Mã tham chiếu giao dịch duy nhất", example = "TRF-20261126-0004")
        String referenceCode,

        @Schema(description = "Khóa chống trùng lặp yêu cầu", example = "idem-9f8e7d6c-5b4a")
        String idempotencyKey,

        @Schema(description = "ID tài khoản nguồn")
        UUID sourceAccountId,

        @Schema(description = "ID tài khoản đích")
        UUID destinationAccountId,

        @Schema(description = "Số tiền giao dịch")
        BigDecimal amount,

        @Schema(description = "Phí giao dịch áp dụng")
        BigDecimal feeAmount,

        @Schema(description = "Loại tiền tệ")
        Currency currency,

        @Schema(description = "Phân loại giao dịch")
        TransferType transferType,

        @Schema(description = "Trạng thái hiện tại của giao dịch")
        TransferStatus status,

        @Schema(description = "Điểm rủi ro gian lận (0-100)", example = "18")
        Integer riskScore,

        @Schema(description = "Nội dung chuyển tiền")
        String description,

        @Schema(description = "Thời điểm tạo giao dịch (UTC)")
        Instant createdAt,

        @Schema(description = "Thời điểm hoàn tất hạch toán (UTC)")
        Instant completedAt
) {}
