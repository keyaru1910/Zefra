package com.paycore.api.dto;

import com.paycore.domain.Currency;
import com.paycore.domain.enums.TransferType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Yêu cầu khởi tạo giao dịch chuyển tiền mới")
public record CreateTransferRequest(
        @Schema(description = "ID tài khoản nguồn (bị ghi Nợ)", example = "20000000-0000-0000-0000-000000000005", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Tài khoản nguồn không được để trống")
        UUID sourceAccountId,

        @Schema(description = "ID tài khoản đích (được ghi Có)", example = "20000000-0000-0000-0000-000000000007", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Tài khoản đích không được để trống")
        UUID destinationAccountId,

        @Schema(description = "Số tiền cần chuyển (tối đa 4 chữ số thập phân)", example = "500000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Số tiền chuyển không được để trống")
        @DecimalMin(value = "0.0001", message = "Số tiền giao dịch phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4, message = "Số tiền vượt quá độ chính xác cho phép (15 chữ số nguyên, 4 chữ số thập phân)")
        BigDecimal amount,

        @Schema(description = "Loại tiền tệ", example = "VND", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Loại tiền tệ không được để trống")
        Currency currency,

        @Schema(description = "Loại giao dịch", example = "P2P", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Loại giao dịch không được để trống")
        TransferType transferType,

        @Schema(description = "Nội dung chuyển tiền", example = "Chuyen tien an trua")
        @Size(max = 255, message = "Nội dung chuyển tiền tối đa 255 ký tự")
        String description
) {}
