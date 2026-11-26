package com.paycore.application;

import com.paycore.api.dto.CreateTransferRequest;
import com.paycore.api.dto.FraudAssessmentResponse;
import com.paycore.domain.Money;
import com.paycore.domain.enums.FraudDecision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Dịch vụ đánh giá rủi ro gian lận (Fraud & AML Rule Engine) trước khi hạch toán giao dịch.
 */
@Service
public class FraudService {

    private final int reviewThreshold;
    private final int blockThreshold;

    public FraudService(
            @Value("${paycore.fraud.review-threshold:60}") int reviewThreshold,
            @Value("${paycore.fraud.block-threshold:85}") int blockThreshold
    ) {
        this.reviewThreshold = reviewThreshold;
        this.blockThreshold = blockThreshold;
    }

    /**
     * Đánh giá điểm rủi ro (0-100) cho một yêu cầu chuyển tiền.
     */
    public FraudAssessmentResponse evaluateTransfer(UUID transferId, CreateTransferRequest request, UUID initiatorId) {
        Money amount = Money.of(request.amount(), request.currency());
        List<String> triggeredRules = new ArrayList<>();
        int score = 10;

        // Ví dụ quy tắc kiểm tra ngưỡng giá trị cao (High-Value Transaction Rule)
        if (amount.getCurrency().name().equals("VND") && amount.toMinorUnits() >= 100_000_000L) {
            score += 35;
            triggeredRules.add("HIGH_VALUE_VND_OVER_100M");
        } else if (!amount.getCurrency().name().equals("VND") && amount.toMinorUnits() >= 1_000_000L) {
            score += 35;
            triggeredRules.add("HIGH_VALUE_FX_OVER_10K");
        }

        if (triggeredRules.isEmpty()) {
            triggeredRules.add("BASELINE_PASS");
        }

        FraudDecision decision = resolveDecision(score);
        return new FraudAssessmentResponse(
                UUID.randomUUID(),
                transferId,
                score,
                decision,
                triggeredRules,
                Instant.now()
        );
    }

    private FraudDecision resolveDecision(int riskScore) {
        if (riskScore >= blockThreshold) {
            return FraudDecision.BLOCK;
        }
        if (riskScore >= reviewThreshold) {
            return FraudDecision.REVIEW;
        }
        return FraudDecision.ALLOW;
    }
}
