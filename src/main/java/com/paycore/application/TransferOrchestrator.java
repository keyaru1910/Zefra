package com.paycore.application;

import com.paycore.api.dto.CreateTransferRequest;
import com.paycore.api.dto.FraudAssessmentResponse;
import com.paycore.api.dto.TransferResponse;
import com.paycore.domain.Money;
import com.paycore.domain.enums.FraudDecision;
import com.paycore.domain.enums.TransferStatus;
import com.paycore.domain.exception.FraudBlockedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Bộ điều phối quy trình chuyển tiền (Transfer Saga / Orchestrator):
 * Idempotency Check -> Fraud Evaluation -> Double-Entry Ledger Posting.
 */
@Service
public class TransferOrchestrator {

    private static final BigDecimal DEFAULT_FEE_PERCENT = new BigDecimal("0.5");

    private final FraudService fraudService;
    private final LedgerService ledgerService;

    public TransferOrchestrator(FraudService fraudService, LedgerService ledgerService) {
        this.fraudService = fraudService;
        this.ledgerService = ledgerService;
    }

    /**
     * Khởi tạo và điều phối giao dịch chuyển tiền (boilerplate sẵn sàng kết nối Repository/DB).
     */
    public TransferResponse initiateTransfer(
            CreateTransferRequest request,
            String idempotencyKey,
            UUID initiatorId
    ) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new IllegalArgumentException("Tài khoản nguồn và tài khoản đích không được trùng nhau");
        }

        UUID transferId = UUID.randomUUID();
        Money transferAmount = Money.of(request.amount(), request.currency());
        Money fee = transferAmount.percentage(DEFAULT_FEE_PERCENT);

        FraudAssessmentResponse assessment = fraudService.evaluateTransfer(transferId, request, initiatorId);
        if (assessment.decision() == FraudDecision.BLOCK) {
            throw new FraudBlockedException(assessment.riskScore(), "Vượt ngưỡng rủi ro cho phép");
        }

        TransferStatus status = assessment.decision() == FraudDecision.REVIEW
                ? TransferStatus.FRAUD_CHECK_PENDING
                : TransferStatus.COMPLETED;

        Instant now = Instant.now();
        return new TransferResponse(
                transferId,
                "TRF-" + transferId.toString().substring(0, 8).toUpperCase(),
                idempotencyKey,
                request.sourceAccountId(),
                request.destinationAccountId(),
                transferAmount.getAmount(),
                fee.getAmount(),
                request.currency(),
                request.transferType(),
                status,
                assessment.riskScore(),
                request.description(),
                now,
                status == TransferStatus.COMPLETED ? now : null
        );
    }
}
