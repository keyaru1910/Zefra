package com.paycore.domain;

import com.paycore.domain.exception.CurrencyMismatchException;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value Object bất biến (Immutable) đại diện cho một khoản tiền kèm loại tiền tệ.
 *
 * <p>Quy tắc thiết kế tài chính:
 * <ul>
 *     <li>Luôn dùng {@link BigDecimal}, tuyệt đối không dùng float/double.</li>
 *     <li>Tự động chuẩn hóa {@code scale} theo {@link Currency#getDefaultScale()} (VND=0, USD=2...).</li>
 *     <li>Mặc định sử dụng Banker's Rounding ({@link RoundingMode#HALF_EVEN}) để giảm sai số tích lũy.</li>
 *     <li>Chuẩn hóa -0.00 về 0.00 để đảm bảo nhất quán {@code equals}, {@code hashCode} và hiển thị.</li>
 * </ul>
 */
public final class Money implements Comparable<Money>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Chế độ làm tròn mặc định trong ngân hàng / kế toán: Banker's Rounding.
     */
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency, RoundingMode roundingMode) {
        Objects.requireNonNull(amount, "Số tiền không được null");
        Objects.requireNonNull(currency, "Loại tiền tệ không được null");
        Objects.requireNonNull(roundingMode, "Chế độ làm tròn không được null");

        BigDecimal scaled = amount.setScale(currency.getDefaultScale(), roundingMode);
        // Chuẩn hóa giá trị -0.00 (hoặc -0) sau làm tròn thành 0 dương với đúng scale của tiền tệ
        if (scaled.compareTo(BigDecimal.ZERO) == 0) {
            scaled = BigDecimal.ZERO.setScale(currency.getDefaultScale(), RoundingMode.UNNECESSARY);
        }
        this.amount = scaled;
        this.currency = currency;
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    /**
     * Tạo đối tượng Money từ {@link BigDecimal} và chuẩn hóa scale theo {@link RoundingMode#HALF_EVEN}.
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency, DEFAULT_ROUNDING);
    }

    /**
     * Tạo đối tượng Money với chế độ làm tròn chỉ định tường minh.
     */
    public static Money of(BigDecimal amount, Currency currency, RoundingMode roundingMode) {
        return new Money(amount, currency, roundingMode);
    }

    /**
     * Tạo đối tượng Money yêu cầu chính xác tuyệt đối, ném {@link ArithmeticException}
     * nếu phần thập phân vượt quá scale chuẩn của tiền tệ (khác 0).
     */
    public static Money ofExact(BigDecimal amount, Currency currency) {
        return new Money(amount, currency, RoundingMode.UNNECESSARY);
    }

    /**
     * Tạo đối tượng Money từ chuỗi số (tránh sai số chuyển đổi của double).
     */
    public static Money of(String amount, Currency currency) {
        if (amount == null || amount.isBlank()) {
            throw new IllegalArgumentException("Chuỗi số tiền không được để trống");
        }
        try {
            return of(new BigDecimal(amount.trim()), currency);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Định dạng số tiền không hợp lệ: " + amount, ex);
        }
    }

    /**
     * Tạo đối tượng Money từ đơn vị nhỏ nhất (minor units), ví dụ: 1050 cents = 10.50 USD, 50000 = 50000 VND.
     */
    public static Money ofMinorUnits(long minorUnits, Currency currency) {
        Objects.requireNonNull(currency, "Loại tiền tệ không được null");
        BigDecimal value = BigDecimal.valueOf(minorUnits, currency.getDefaultScale());
        return new Money(value, currency, RoundingMode.UNNECESSARY);
    }

    /**
     * Tạo khoản tiền giá trị 0 với loại tiền tệ chỉ định.
     */
    public static Money zero(Currency currency) {
        Objects.requireNonNull(currency, "Loại tiền tệ không được null");
        return new Money(BigDecimal.ZERO, currency, RoundingMode.UNNECESSARY);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    /**
     * Quy đổi ra đơn vị nhỏ nhất (ví dụ 10.50 USD -> 1050 cents).
     */
    public long toMinorUnits() {
        return amount.movePointRight(currency.getDefaultScale()).longValueExact();
    }

    // -------------------------------------------------------------------------
    // Các phép toán số học bất biến
    // -------------------------------------------------------------------------

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency, RoundingMode.UNNECESSARY);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency, RoundingMode.UNNECESSARY);
    }

    public Money multiply(long factor) {
        return multiply(BigDecimal.valueOf(factor), DEFAULT_ROUNDING);
    }

    public Money multiply(BigDecimal factor) {
        return multiply(factor, DEFAULT_ROUNDING);
    }

    public Money multiply(BigDecimal factor, RoundingMode roundingMode) {
        Objects.requireNonNull(factor, "Hệ số nhân không được null");
        Objects.requireNonNull(roundingMode, "Chế độ làm tròn không được null");
        BigDecimal product = this.amount.multiply(factor);
        return new Money(product, this.currency, roundingMode);
    }

    public Money divide(BigDecimal divisor) {
        return divide(divisor, DEFAULT_ROUNDING);
    }

    public Money divide(BigDecimal divisor, RoundingMode roundingMode) {
        Objects.requireNonNull(divisor, "Số chia không được null");
        Objects.requireNonNull(roundingMode, "Chế độ làm tròn không được null");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Không thể chia số tiền cho 0");
        }
        BigDecimal quotient = this.amount.divide(divisor, currency.getDefaultScale(), roundingMode);
        return new Money(quotient, this.currency, RoundingMode.UNNECESSARY);
    }

    /**
     * Tính phần trăm trên số tiền hiện tại (ví dụ tính phí giao dịch 1.5%: percentage(new BigDecimal("1.5"))).
     * Nhân trước khi chia 100 và làm tròn 1 lần duy nhất để tránh sai số làm tròn kép.
     */
    public Money percentage(BigDecimal percent) {
        return percentage(percent, DEFAULT_ROUNDING);
    }

    public Money percentage(BigDecimal percent, RoundingMode roundingMode) {
        Objects.requireNonNull(percent, "Tỷ lệ phần trăm không được null");
        Objects.requireNonNull(roundingMode, "Chế độ làm tròn không được null");
        BigDecimal calculated = this.amount
                .multiply(percent)
                .divide(ONE_HUNDRED, currency.getDefaultScale(), roundingMode);
        return new Money(calculated, this.currency, RoundingMode.UNNECESSARY);
    }

    /**
     * Chia đều khoản tiền thành {@code parts} phần sao cho tổng các phần khớp tuyệt đối với số tiền gốc.
     * Phần dư (tính theo đơn vị nhỏ nhất) được phân bổ lần lượt cho các phần đầu tiên.
     * Ví dụ: 10.00 USD chia 3 -> [3.34 USD, 3.33 USD, 3.33 USD].
     */
    public List<Money> allocate(int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("Số phần phân bổ phải lớn hơn 0");
        }
        BigInteger totalMinorUnits = this.amount.movePointRight(currency.getDefaultScale()).toBigIntegerExact();
        BigInteger divisor = BigInteger.valueOf(parts);
        BigInteger[] divRem = totalMinorUnits.divideAndRemainder(divisor);
        BigInteger baseShare = divRem[0];
        int remainder = Math.abs(divRem[1].intValueExact());
        BigInteger step = totalMinorUnits.signum() >= 0 ? BigInteger.ONE : BigInteger.valueOf(-1);

        List<Money> allocations = new ArrayList<>(parts);
        for (int i = 0; i < parts; i++) {
            BigInteger shareUnits = i < remainder ? baseShare.add(step) : baseShare;
            BigDecimal shareAmount = new BigDecimal(shareUnits, currency.getDefaultScale());
            allocations.add(new Money(shareAmount, currency, RoundingMode.UNNECESSARY));
        }
        return Collections.unmodifiableList(allocations);
    }

    public Money negate() {
        return new Money(this.amount.negate(), this.currency, RoundingMode.UNNECESSARY);
    }

    public Money abs() {
        return new Money(this.amount.abs(), this.currency, RoundingMode.UNNECESSARY);
    }

    // -------------------------------------------------------------------------
    // Kiểm tra trạng thái & So sánh
    // -------------------------------------------------------------------------

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return compareTo(other) >= 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        return compareTo(other) <= 0;
    }

    @Override
    public int compareTo(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    private void assertSameCurrency(Money other) {
        Objects.requireNonNull(other, "Đối tượng tiền tệ so sánh không được null");
        if (this.currency != other.currency) {
            throw new CurrencyMismatchException(this.currency, other.currency);
        }
    }

    // -------------------------------------------------------------------------
    // Hiển thị & Equality
    // -------------------------------------------------------------------------

    /**
     * Trả về chuỗi số thuần túy không dùng ký hiệu khoa học (ví dụ 10000000 thay vì 1E+7).
     */
    public String toPlainString() {
        return amount.toPlainString();
    }

    public String toFormattedString() {
        return amount.toPlainString() + " " + currency.getIsoCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Money other)) {
            return false;
        }
        return this.currency == other.currency && this.amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return toFormattedString();
    }
}
