package com.paycore.domain.exception;

import com.paycore.domain.enums.ErrorCode;

public class ResourceNotFoundException extends PaycoreException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                String.format("Không tìm thấy %s với mã định danh: %s", resourceName, identifier)
        );
    }
}
