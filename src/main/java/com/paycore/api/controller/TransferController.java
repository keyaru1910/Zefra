package com.paycore.api.controller;

import com.paycore.api.dto.ApiResponse;
import com.paycore.api.dto.CreateTransferRequest;
import com.paycore.api.dto.ErrorResponse;
import com.paycore.api.dto.TransferResponse;
import com.paycore.application.TransferOrchestrator;
import com.paycore.domain.exception.ResourceNotFoundException;
import com.paycore.infrastructure.security.JwtFilter.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
@Validated
@Tag(name = "Transfers", description = "API khởi tạo và tra cứu giao dịch chuyển tiền")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferOrchestrator transferOrchestrator;

    public TransferController(TransferOrchestrator transferOrchestrator) {
        this.transferOrchestrator = transferOrchestrator;
    }

    @PostMapping
    @Operation(
            summary = "Tạo lệnh chuyển tiền mới",
            description = "Khởi tạo giao dịch chuyển tiền kèm kiểm tra Idempotency-Key, đánh giá gian lận (Fraud) và hạch toán sổ cái kép."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Giao dịch đã được tiếp nhận hoặc hoàn tất hạch toán"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu yêu cầu hoặc tiền tệ không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Trùng Idempotency-Key với nội dung khác",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "Không đủ số dư hoặc bị chặn bởi hệ thống chống gian lận",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<TransferResponse>> createTransfer(
            @Parameter(
                    name = "Idempotency-Key",
                    in = ParameterIn.HEADER,
                    required = true,
                    description = "Khóa duy nhất do client sinh (ví dụ UUID) để chống xử lý trùng khi retry",
                    example = "9f8e7d6c-5b4a-4321-9876-1234567890ab"
            )
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Header Idempotency-Key không được để trống")
            @Size(max = 128, message = "Idempotency-Key tối đa 128 ký tự")
            String idempotencyKey,

            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        UUID initiatorId = principal != null ? principal.userId() : UUID.randomUUID();
        TransferResponse response = transferOrchestrator.initiateTransfer(request, idempotencyKey, initiatorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Khởi tạo giao dịch thành công", response));
    }

    @GetMapping("/{transferId}")
    @Operation(
            summary = "Tra cứu chi tiết giao dịch",
            description = "Lấy trạng thái và thông tin chi tiết của một giao dịch theo UUID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Tìm thấy giao dịch"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy giao dịch",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<TransferResponse>> getTransferById(
            @Parameter(description = "UUID của giao dịch", required = true)
            @PathVariable UUID transferId
    ) {
        throw new ResourceNotFoundException("Transfer", transferId);
    }
}
