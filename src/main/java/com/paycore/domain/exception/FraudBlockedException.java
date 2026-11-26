package com.paycore.domain.exception;

import com.paycore.domain.enums.ErrorCode;

public class FraudBlockedException extends PaycoreException {

    private final int riskScore;

    public FraudBlockedException(int riskScore, String reason) {
        super(
                ErrorCode.FRAUD_BLOCKED,
                String.format("Giao dịch bị chặn do rủi ro gian lận (score=%d): %s", riskScore, reason)
        );
        this.riskScore = riskScore;
    }

    public int getRiskScore() {
        return riskScore;
    }
}
