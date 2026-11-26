package com.paycore.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Quyết định từ bộ máy đánh giá rủi ro gian lận")
public enum FraudDecision {
    ALLOW,
    REVIEW,
    BLOCK
}
