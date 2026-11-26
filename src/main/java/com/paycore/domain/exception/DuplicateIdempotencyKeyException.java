package com.paycore.domain.exception;

import com.paycore.domain.enums.ErrorCode;

public class DuplicateIdempotencyKeyException extends PaycoreException {

    public DuplicateIdempotencyKeyException(String idempotencyKey) {
        super(
                ErrorCode.DUPLICATE_IDEMPOTENCY_KEY,
                "Idempotency-Key '" + idempotencyKey + "' đã được sử dụng cho một payload giao dịch khác"
        );
    }
}
