package com.paycore.api.dto;

import com.paycore.domain.enums.FraudDecision;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Kết quả đánh giá rủi ro gian lận của giao dịch")
public record FraudAssessmentResponse(
        @Schema(description = "ID bản ghi đánh giá")
        UUID id,

        @Schema(description = "ID giao dịch được đánh giá")
        UUID transferId,

        @Schema(description = "Điểm rủi ro (0-100)", example = "18")
        int riskScore,

        @Schema(description = "Quyết định xử lý")
        FraudDecision decision,

        @Schema(description = "Danh sách các rule đã kích hoạt")
        List<String> triggeredRules,

        @Schema(description = "Thời điểm đánh giá (UTC)")
        Instant evaluatedAt
) {}
