package com.paycore.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

/**
 * Danh sách các loại tiền tệ được hệ thống PayCore hỗ trợ.
 * Mỗi loại tiền tệ quy định rõ số chữ số thập phân chuẩn (defaultScale) theo ISO 4217.
 */
@Schema(description = "Mã tiền tệ chuẩn ISO 4217 được PayCore hỗ trợ")
public enum Currency {

    VND("VND", 0, "₫", "Vietnamese Dong"),
    USD("USD", 2, "$", "US Dollar"),
    EUR("EUR", 2, "€", "Euro"),
    SGD("SGD", 2, "S$", "Singapore Dollar"),
    JPY("JPY", 0, "¥", "Japanese Yen");

    private final String isoCode;
    private final int defaultScale;
    private final String symbol;
    private final String displayName;

    Currency(String isoCode, int defaultScale, String symbol, String displayName) {
        this.isoCode = isoCode;
        this.defaultScale = defaultScale;
        this.symbol = symbol;
        this.displayName = displayName;
    }

    public String getIsoCode() {
        return isoCode;
    }

    /**
     * Số chữ số sau dấu phẩy theo chuẩn ISO 4217 (ví dụ: VND/JPY = 0, USD/EUR/SGD = 2).
     */
    public int getDefaultScale() {
        return defaultScale;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Chuyển đổi chuỗi mã tiền tệ (không phân biệt hoa/thường) sang enum {@link Currency}.
     */
    public static Currency fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Mã tiền tệ không được để trống");
        }
        String normalized = code.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(c -> c.isoCode.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Loại tiền tệ không được hỗ trợ: " + code));
    }
}
