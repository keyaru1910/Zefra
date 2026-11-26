package com.paycore.api.controller;

import com.paycore.api.dto.AccountSummaryResponse;
import com.paycore.api.dto.ApiResponse;
import com.paycore.api.dto.ErrorResponse;
import com.paycore.api.dto.LedgerEntryResponse;
import com.paycore.api.dto.PageResponse;
import com.paycore.application.LedgerService;
import com.paycore.domain.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Validated
@Tag(name = "Accounts & Ledger", description = "API tra cứu số dư tài khoản và sao kê bút toán sổ cái")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final LedgerService ledgerService;

    public AccountController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/{accountId}")
    @Operation(
            summary = "Xem thông tin số dư tài khoản",
            description = "Trả về số dư sổ cái (balance) và số dư khả dụng (availableBalance) của tài khoản."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy tài khoản",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<AccountSummaryResponse>> getAccountSummary(
            @Parameter(description = "UUID của tài khoản", required = true)
            @PathVariable UUID accountId
    ) {
        throw new ResourceNotFoundException("Account", accountId);
    }

    @GetMapping("/{accountId}/ledger-entries")
    @Operation(
            summary = "Tra cứu sao kê bút toán sổ cái của tài khoản",
            description = "Liệt kê lịch sử các dòng ghi Nợ / ghi Có (DEBIT / CREDIT) theo thứ tự thời gian giảm dần."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Tham số phân trang không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<LedgerEntryResponse>>> getLedgerEntries(
            @Parameter(description = "UUID của tài khoản", required = true)
            @PathVariable UUID accountId,

            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Số trang phải >= 0")
            int page,

            @Parameter(description = "Số bản ghi mỗi trang (1 - 100)", example = "20")
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Kích thước trang tối thiểu là 1")
            @Max(value = 100, message = "Kích thước trang tối đa là 100")
            int size
    ) {
        List<LedgerEntryResponse> entries = ledgerService.getAccountEntries(accountId, page, size);
        PageResponse<LedgerEntryResponse> result = new PageResponse<>(entries, page, size, entries.size(), 0);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
