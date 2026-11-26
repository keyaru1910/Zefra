package com.paycore.domain;

import com.paycore.domain.exception.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money Value Object - Kiểm thử đơn vị (Scale & Rounding Edge Cases)")
class MoneyTest {

    @Nested
    @DisplayName("1. Chuẩn hóa Scale theo từng loại tiền tệ")
    class ScaleNormalizationTests {

        @Test
        @DisplayName("Tiền tệ 2 chữ số thập phân (USD, EUR, SGD) luôn giữ scale = 2")
        void shouldNormalizeTwoDecimalCurrenciesToScaleTwo() {
            Money fromInteger = Money.of(new BigDecimal("100"), Currency.USD);
            Money fromOneDecimal = Money.of(new BigDecimal("100.5"), Currency.EUR);
            Money fromFourDecimals = Money.of(new BigDecimal("100.5000"), Currency.SGD);

            assertThat(fromInteger.getAmount()).hasScaleOf(2);
            assertThat(fromInteger.toPlainString()).isEqualTo("100.00");

            assertThat(fromOneDecimal.getAmount()).hasScaleOf(2);
            assertThat(fromOneDecimal.toPlainString()).isEqualTo("100.50");

            assertThat(fromFourDecimals.getAmount()).hasScaleOf(2);
            assertThat(fromFourDecimals.toPlainString()).isEqualTo("100.50");
        }

        @Test
        @DisplayName("Tiền tệ không có phần thập phân (VND, JPY) luôn giữ scale = 0")
        void shouldNormalizeZeroDecimalCurrenciesToScaleZero() {
            Money vnd = Money.of(new BigDecimal("50000.00"), Currency.VND);
            Money jpy = Money.of("1500.0", Currency.JPY);

            assertThat(vnd.getAmount()).hasScaleOf(0);
            assertThat(vnd.toPlainString()).isEqualTo("50000");

            assertThat(jpy.getAmount()).hasScaleOf(0);
            assertThat(jpy.toPlainString()).isEqualTo("1500");
        }

        @Test
        @DisplayName("Số tiền lớn không bị chuyển sang ký hiệu khoa học (scientific notation)")
        void shouldFormatLargeVndWithoutScientificNotation() {
            Money tenBillionVnd = Money.of("10000000000", Currency.VND);

            assertThat(tenBillionVnd.toPlainString()).isEqualTo("10000000000");
            assertThat(tenBillionVnd.toFormattedString()).isEqualTo("10000000000 VND");
        }
    }

    @Nested
    @DisplayName("2. Edge cases làm tròn Banker's Rounding (HALF_EVEN)")
    class BankersRoundingTests {

        @ParameterizedTest(name = "{0} USD -> {1} USD (HALF_EVEN)")
        @CsvSource({
                "10.125, 10.12",
                "10.135, 10.14",
                "10.105, 10.10",
                "10.115, 10.12",
                "0.004,  0.00",
                "0.005,  0.00",
                "0.0051, 0.01",
                "0.015,  0.02",
                "-10.125, -10.12",
                "-10.135, -10.14"
        })
        @DisplayName("Làm tròn ranh giới .xx5 cho USD theo quy tắc số chẵn gần nhất")
        void shouldApplyHalfEvenRoundingForUsd(String rawInput, String expected) {
            Money money = Money.of(rawInput, Currency.USD);
            assertThat(money.toPlainString()).isEqualTo(expected);
        }

        @ParameterizedTest(name = "{0} VND -> {1} VND (HALF_EVEN)")
        @CsvSource({
                "100.5, 100",
                "101.5, 102",
                "0.5,   0",
                "1.5,   2",
                "-100.5, -100",
                "-101.5, -102"
        })
        @DisplayName("Làm tròn ranh giới .5 cho VND (scale = 0) theo quy tắc số chẵn gần nhất")
        void shouldApplyHalfEvenRoundingForVnd(String rawInput, String expected) {
            Money money = Money.of(rawInput, Currency.VND);
            assertThat(money.toPlainString()).isEqualTo(expected);
        }

        @Test
        @DisplayName("Giá trị âm rất nhỏ làm tròn về 0 phải chuẩn hóa thành 0 dương (không hiển thị -0.00)")
        void shouldCanonicalizeNegativeZeroAfterRounding() {
            Money tinyNegativeUsd = Money.of("-0.004", Currency.USD);
            Money tinyNegativeVnd = Money.of("-0.4", Currency.VND);

            assertThat(tinyNegativeUsd.isZero()).isTrue();
            assertThat(tinyNegativeUsd.toPlainString()).isEqualTo("0.00");
            assertThat(tinyNegativeUsd).isEqualTo(Money.zero(Currency.USD));
            assertThat(tinyNegativeUsd.hashCode()).isEqualTo(Money.zero(Currency.USD).hashCode());

            assertThat(tinyNegativeVnd.isZero()).isTrue();
            assertThat(tinyNegativeVnd.toPlainString()).isEqualTo("0");
            assertThat(tinyNegativeVnd).isEqualTo(Money.zero(Currency.VND));
        }
    }

    @Nested
    @DisplayName("3. Chế độ làm tròn tường minh & ofExact")
    class ExplicitRoundingAndExactTests {

        @Test
        @DisplayName("Hỗ trợ chỉ định RoundingMode tường minh (HALF_UP, CEILING, FLOOR)")
        void shouldRespectExplicitRoundingModes() {
            BigDecimal input = new BigDecimal("10.125");

            assertThat(Money.of(input, Currency.USD, RoundingMode.HALF_UP).toPlainString()).isEqualTo("10.13");
            assertThat(Money.of(new BigDecimal("10.121"), Currency.USD, RoundingMode.CEILING).toPlainString()).isEqualTo("10.13");
            assertThat(Money.of(new BigDecimal("10.129"), Currency.USD, RoundingMode.FLOOR).toPlainString()).isEqualTo("10.12");
        }

        @Test
        @DisplayName("ofExact chấp nhận trailing zeros vô nghĩa nhưng từ chối phần dư cần làm tròn")
        void shouldValidatePrecisionInOfExact() {
            Money exactUsd = Money.ofExact(new BigDecimal("25.5000"), Currency.USD);
            assertThat(exactUsd.toPlainString()).isEqualTo("25.50");

            assertThatThrownBy(() -> Money.ofExact(new BigDecimal("25.505"), Currency.USD))
                    .isInstanceOf(ArithmeticException.class);

            assertThatThrownBy(() -> Money.ofExact(new BigDecimal("1000.5"), Currency.VND))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    @DisplayName("4. Phép toán nhân, chia, tính phần trăm & phân bổ (Allocation)")
    class ArithmeticRoundingTests {

        @Test
        @DisplayName("Nhân với tỷ lệ thập phân: giữ đủ độ chính xác trung gian và làm tròn 1 lần cuối")
        void shouldMultiplyWithoutPrematureRounding() {
            Money base = Money.of("100.00", Currency.USD);

            // 100.00 * 0.01255 = 1.255 -> HALF_EVEN = 1.26
            assertThat(base.multiply(new BigDecimal("0.01255")).toPlainString()).isEqualTo("1.26");
            // 100.00 * 0.01245 = 1.245 -> HALF_EVEN = 1.24
            assertThat(base.multiply(new BigDecimal("0.01245")).toPlainString()).isEqualTo("1.24");
        }

        @Test
        @DisplayName("Chia số thập phân vô hạn tuần hoàn và chặn chia cho 0")
        void shouldDivideWithRoundingAndRejectZeroDivisor() {
            Money hundredUsd = Money.of("100.00", Currency.USD);
            Money tenUsd = Money.of("10.00", Currency.USD);

            assertThat(hundredUsd.divide(new BigDecimal("3")).toPlainString()).isEqualTo("33.33");
            assertThat(tenUsd.divide(new BigDecimal("3"), RoundingMode.CEILING).toPlainString()).isEqualTo("3.34");

            assertThatThrownBy(() -> hundredUsd.divide(BigDecimal.ZERO))
                    .isInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("Tính phí theo phần trăm cho VND và USD")
        void shouldCalculatePercentageAccurately() {
            Money vndAmount = Money.of("1050", Currency.VND);
            // 1050 * 1.5 / 100 = 15.75 -> HALF_EVEN = 16 VND
            assertThat(vndAmount.percentage(new BigDecimal("1.5")).toPlainString()).isEqualTo("16");

            Money usdAmount = Money.of("33.33", Currency.USD);
            // 33.33 * 15 / 100 = 4.9995 -> HALF_EVEN = 5.00 USD
            assertThat(usdAmount.percentage(new BigDecimal("15")).toPlainString()).isEqualTo("5.00");
        }

        @Test
        @DisplayName("Phân bổ số tiền (allocate) không làm thất thoát xu/đồng lẻ")
        void shouldAllocateWithoutLosingMinorUnits() {
            Money tenUsd = Money.of("10.00", Currency.USD);
            List<Money> usdParts = tenUsd.allocate(3);

            assertThat(usdParts).containsExactly(
                    Money.of("3.34", Currency.USD),
                    Money.of("3.33", Currency.USD),
                    Money.of("3.33", Currency.USD)
            );
            Money usdSum = usdParts.stream().reduce(Money.zero(Currency.USD), Money::add);
            assertThat(usdSum).isEqualTo(tenUsd);

            Money hundredVnd = Money.of("100", Currency.VND);
            List<Money> vndParts = hundredVnd.allocate(3);
            assertThat(vndParts).containsExactly(
                    Money.of("34", Currency.VND),
                    Money.of("33", Currency.VND),
                    Money.of("33", Currency.VND)
            );

            Money negativeTenUsd = tenUsd.negate();
            List<Money> negativeParts = negativeTenUsd.allocate(3);
            assertThat(negativeParts).containsExactly(
                    Money.of("-3.34", Currency.USD),
                    Money.of("-3.33", Currency.USD),
                    Money.of("-3.33", Currency.USD)
            );
        }
    }

    @Nested
    @DisplayName("5. Minor Units, Equality, HashCode & Guard tiền tệ")
    class EqualityAndGuardTests {

        @Test
        @DisplayName("Chuyển đổi qua lại đơn vị nhỏ nhất (minor units)")
        void shouldConvertMinorUnitsCorrectly() {
            Money usd = Money.ofMinorUnits(1050L, Currency.USD);
            Money vnd = Money.ofMinorUnits(50000L, Currency.VND);

            assertThat(usd.toPlainString()).isEqualTo("10.50");
            assertThat(usd.toMinorUnits()).isEqualTo(1050L);

            assertThat(vnd.toPlainString()).isEqualTo("50000");
            assertThat(vnd.toMinorUnits()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("equals và hashCode nhất quán dù scale đầu vào khác nhau")
        void shouldHaveConsistentEqualsAndHashCode() {
            Money a = Money.of(new BigDecimal("10"), Currency.USD);
            Money b = Money.of(new BigDecimal("10.0"), Currency.USD);
            Money c = Money.of(new BigDecimal("10.0000"), Currency.USD);
            Money jpy = Money.of("10", Currency.JPY);
            Money vnd = Money.of("10", Currency.VND);

            assertThat(a).isEqualTo(b).isEqualTo(c);
            assertThat(a.hashCode()).isEqualTo(b.hashCode()).isEqualTo(c.hashCode());
            assertThat(vnd).isNotEqualTo(jpy);
        }

        @Test
        @DisplayName("Cộng/trừ/so sánh khác loại tiền tệ phải ném CurrencyMismatchException")
        void shouldThrowCurrencyMismatchException() {
            Money usd = Money.of("10.00", Currency.USD);
            Money vnd = Money.of("250000", Currency.VND);

            assertThatThrownBy(() -> usd.add(vnd)).isInstanceOf(CurrencyMismatchException.class);
            assertThatThrownBy(() -> usd.subtract(vnd)).isInstanceOf(CurrencyMismatchException.class);
            assertThatThrownBy(() -> usd.compareTo(vnd)).isInstanceOf(CurrencyMismatchException.class);
        }

        @Test
        @DisplayName("Từ chối input null hoặc chuỗi không hợp lệ")
        void shouldRejectInvalidInputs() {
            assertThatThrownBy(() -> Money.of((BigDecimal) null, Currency.USD))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Money.of("   ", Currency.USD))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Money.of("12.34abc", Currency.USD))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Money.of("10.00", Currency.USD).allocate(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
