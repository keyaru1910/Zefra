package com.paycore.domain.exception;

import com.paycore.domain.Money;
import com.paycore.domain.enums.ErrorCode;

public class InsufficientBalanceException extends PaycoreException {

    public InsufficientBalanceException(String accountNumber, Money available, Money required) {
        super(
                ErrorCode.INSUFFICIENT_BALANCE,
                String.format("Tài khoản %s không đủ số dư khả dụng (hiện có: %s, cần: %s)",
                        accountNumber, available.toFormattedString(), required.toFormattedString())
        );
    }
}
