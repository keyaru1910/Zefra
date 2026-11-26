package com.paycore.domain.exception;

import com.paycore.domain.Currency;
import com.paycore.domain.enums.ErrorCode;

/**
 * Ném ra khi thực hiện phép toán hoặc chuyển tiền giữa hai loại tiền tệ khác nhau.
 */
public class CurrencyMismatchException extends PaycoreException {

    public CurrencyMismatchException(Currency expected, Currency actual) {
        super(
                ErrorCode.CURRENCY_MISMATCH,
                String.format("Không thể xử lý giữa hai loại tiền tệ khác nhau: %s và %s",
                        expected.getIsoCode(), actual.getIsoCode())
        );
    }
}
