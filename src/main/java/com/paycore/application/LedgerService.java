package com.paycore.application;

import com.paycore.api.dto.LedgerEntryResponse;
import com.paycore.domain.Money;
import com.paycore.domain.enums.EntryDirection;
import com.paycore.domain.enums.ErrorCode;
import com.paycore.domain.exception.PaycoreException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Dịch vụ Sổ cái kế toán kép (Double-Entry Ledger Service).
 * Đảm bảo bất biến kế toán: mọi chứng từ hạch toán đều có SUM(DEBIT) == SUM(CREDIT).
 */
@Service
public class LedgerService {

    /**
     * Kiểm tra tính cân bằng kế toán kép của danh sách bút toán trước khi ghi xuống DB.
     */
    public void verifyDoubleEntryBalance(List<LedgerEntryResponse> entries) {
        if (entries == null || entries.size() < 2) {
            throw new PaycoreException(ErrorCode.LEDGER_UNBALANCED,
                    "Một chứng từ sổ cái phải có tối thiểu 2 bút toán (DEBIT và CREDIT)");
        }

        var currency = entries.get(0).currency();
        Money totalDebit = Money.zero(currency);
        Money totalCredit = Money.zero(currency);

        for (LedgerEntryResponse entry : entries) {
            Money entryMoney = Money.ofExact(entry.amount(), entry.currency());
            if (entry.entryDirection() == EntryDirection.DEBIT) {
                totalDebit = totalDebit.add(entryMoney);
            } else {
                totalCredit = totalCredit.add(entryMoney);
            }
        }

        if (!totalDebit.equals(totalCredit)) {
            throw new PaycoreException(
                    ErrorCode.LEDGER_UNBALANCED,
                    String.format("Tổng DEBIT (%s) không bằng tổng CREDIT (%s)",
                            totalDebit.toFormattedString(), totalCredit.toFormattedString())
            );
        }
    }

    /**
     * Truy vấn lịch sử bút toán theo tài khoản (boilerplate chờ nối JPA Repository).
     */
    public List<LedgerEntryResponse> getAccountEntries(UUID accountId, int page, int size) {
        return List.of();
    }
}
