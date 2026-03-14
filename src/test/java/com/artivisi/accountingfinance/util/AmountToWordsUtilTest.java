package com.artivisi.accountingfinance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AmountToWordsUtil.
 * Tests Indonesian number to words conversion.
 */
@DisplayName("AmountToWordsUtil - Indonesian Number to Words")
class AmountToWordsUtilTest {

    private void assertToWords(BigDecimal amount, String expected) {
        String result = AmountToWordsUtil.toWords(amount);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 'nol rupiah' for null")
    void shouldReturnNolRupiahForNull() {
        assertToWords(null, "nol rupiah");
    }

    @Test
    @DisplayName("Should return 'nol rupiah' for zero")
    void shouldReturnNolRupiahForZero() {
        assertToWords(BigDecimal.ZERO, "nol rupiah");
    }

    @ParameterizedTest
    @MethodSource("allAmounts")
    @DisplayName("Should convert amounts to Indonesian words")
    void shouldConvertAmountsToWords(BigDecimal amount, String expected) {
        assertToWords(amount, expected);
    }

    static Stream<Arguments> allAmounts() {
        return Stream.of(
                // Single digits (1-9)
                Arguments.of(new BigDecimal("1"), "Satu rupiah"),
                Arguments.of(new BigDecimal("5"), "Lima rupiah"),
                Arguments.of(new BigDecimal("9"), "Sembilan rupiah"),
                // Teens (10-19)
                Arguments.of(new BigDecimal("10"), "Sepuluh rupiah"),
                Arguments.of(new BigDecimal("11"), "Sebelas rupiah"),
                Arguments.of(new BigDecimal("12"), "Dua belas rupiah"),
                Arguments.of(new BigDecimal("15"), "Lima belas rupiah"),
                Arguments.of(new BigDecimal("19"), "Sembilan belas rupiah"),
                // Tens (20-99)
                Arguments.of(new BigDecimal("20"), "Dua puluh rupiah"),
                Arguments.of(new BigDecimal("25"), "Dua puluh lima rupiah"),
                Arguments.of(new BigDecimal("50"), "Lima puluh rupiah"),
                Arguments.of(new BigDecimal("99"), "Sembilan puluh sembilan rupiah"),
                // Hundreds (100-999)
                Arguments.of(new BigDecimal("100"), "Seratus rupiah"),
                Arguments.of(new BigDecimal("150"), "Seratus lima puluh rupiah"),
                Arguments.of(new BigDecimal("200"), "Dua ratus rupiah"),
                Arguments.of(new BigDecimal("500"), "Lima ratus rupiah"),
                Arguments.of(new BigDecimal("999"), "Sembilan ratus sembilan puluh sembilan rupiah"),
                // Thousands (1,000-999,999)
                Arguments.of(new BigDecimal("1000"), "Seribu rupiah"),
                Arguments.of(new BigDecimal("1500"), "Seribu lima ratus rupiah"),
                Arguments.of(new BigDecimal("2000"), "Dua ribu rupiah"),
                Arguments.of(new BigDecimal("5000"), "Lima ribu rupiah"),
                Arguments.of(new BigDecimal("10000"), "Sepuluh ribu rupiah"),
                Arguments.of(new BigDecimal("100000"), "Seratus ribu rupiah"),
                Arguments.of(new BigDecimal("500000"), "Lima ratus ribu rupiah"),
                // Millions (1,000,000+)
                Arguments.of(new BigDecimal("1000000"), "Satu juta rupiah"),
                Arguments.of(new BigDecimal("5000000"), "Lima juta rupiah"),
                Arguments.of(new BigDecimal("10000000"), "Sepuluh juta rupiah"),
                Arguments.of(new BigDecimal("100000000"), "Seratus juta rupiah"),
                // Billions (1,000,000,000+)
                Arguments.of(new BigDecimal("1000000000"), "Satu miliar rupiah"),
                Arguments.of(new BigDecimal("5000000000"), "Lima miliar rupiah"),
                Arguments.of(new BigDecimal("10000000000"), "Sepuluh miliar rupiah"),
                // Trillions
                Arguments.of(new BigDecimal("1000000000000"), "Satu triliun rupiah")
        );
    }

    @Test
    @DisplayName("Should handle complex Indonesian amounts")
    void shouldHandleComplexIndonesianAmounts() {
        // Common invoice amounts
        String result1 = AmountToWordsUtil.toWords(new BigDecimal("1234567"));
        assertThat(result1).contains("juta");
        assertThat(result1).endsWith("rupiah");

        // Typical salary amount
        String result2 = AmountToWordsUtil.toWords(new BigDecimal("7500000"));
        assertThat(result2).isEqualTo("Tujuh juta lima ratus ribu rupiah");
    }

    @Test
    @DisplayName("Should round decimal amounts")
    void shouldRoundDecimalAmounts() {
        String result = AmountToWordsUtil.toWords(new BigDecimal("1500.75"));
        assertThat(result).isEqualTo("Seribu lima ratus satu rupiah");
    }

    @Test
    @DisplayName("Should capitalize first letter")
    void shouldCapitalizeFirstLetter() {
        String result = AmountToWordsUtil.toWords(new BigDecimal("1"));
        assertThat(result.charAt(0)).isUpperCase();
    }

    @Test
    @DisplayName("Should handle large trillion with complex remainder")
    void shouldHandleLargeTrillionWithComplexRemainder() {
        String result = AmountToWordsUtil.toWords(new BigDecimal("2500000000000"));
        assertThat(result).isEqualTo("Dua triliun lima ratus miliar rupiah");
    }

    @Test
    @DisplayName("Should handle decimal rounding down")
    void shouldHandleDecimalRoundingDown() {
        // 1500.25 rounds to 1500
        String result = AmountToWordsUtil.toWords(new BigDecimal("1500.25"));
        assertThat(result).isEqualTo("Seribu lima ratus rupiah");
    }

    @Test
    @DisplayName("Should handle teens 13 through 19")
    void shouldHandleTeens() {
        assertToWords(new BigDecimal("13"), "Tiga belas rupiah");
        assertToWords(new BigDecimal("14"), "Empat belas rupiah");
        assertToWords(new BigDecimal("16"), "Enam belas rupiah");
        assertToWords(new BigDecimal("17"), "Tujuh belas rupiah");
        assertToWords(new BigDecimal("18"), "Delapan belas rupiah");
    }

    @Test
    @DisplayName("Should handle tens with zero unit")
    void shouldHandleTensWithZeroUnit() {
        assertToWords(new BigDecimal("30"), "Tiga puluh rupiah");
        assertToWords(new BigDecimal("40"), "Empat puluh rupiah");
        assertToWords(new BigDecimal("60"), "Enam puluh rupiah");
        assertToWords(new BigDecimal("70"), "Tujuh puluh rupiah");
        assertToWords(new BigDecimal("80"), "Delapan puluh rupiah");
        assertToWords(new BigDecimal("90"), "Sembilan puluh rupiah");
    }

    @Test
    @DisplayName("Should handle hundreds 200-999")
    void shouldHandleHundredsWithRemainder() {
        assertToWords(new BigDecimal("123"), "Seratus dua puluh tiga rupiah");
        assertToWords(new BigDecimal("250"), "Dua ratus lima puluh rupiah");
        assertToWords(new BigDecimal("311"), "Tiga ratus sebelas rupiah");
        assertToWords(new BigDecimal("415"), "Empat ratus lima belas rupiah");
    }

    @Test
    @DisplayName("Should handle seribu with remainder")
    void shouldHandleSeribuWithRemainder() {
        assertToWords(new BigDecimal("1001"), "Seribu satu rupiah");
        assertToWords(new BigDecimal("1999"), "Seribu sembilan ratus sembilan puluh sembilan rupiah");
    }

    @Test
    @DisplayName("Should handle thousands with complex remainders")
    void shouldHandleThousandsWithComplexRemainders() {
        assertToWords(new BigDecimal("12345"), "Dua belas ribu tiga ratus empat puluh lima rupiah");
        assertToWords(new BigDecimal("100001"), "Seratus ribu satu rupiah");
    }

    @Test
    @DisplayName("Should handle millions with remainders")
    void shouldHandleMillionsWithRemainders() {
        assertToWords(new BigDecimal("1500000"), "Satu juta lima ratus ribu rupiah");
        assertToWords(new BigDecimal("2100500"), "Dua juta seratus ribu lima ratus rupiah");
    }

    @Test
    @DisplayName("Should handle billions with remainders")
    void shouldHandleBillionsWithRemainders() {
        assertToWords(new BigDecimal("1500000000"), "Satu miliar lima ratus juta rupiah");
        assertToWords(new BigDecimal("2000000001"), "Dua miliar satu rupiah");
    }

    @Test
    @DisplayName("Should handle trillions with remainders")
    void shouldHandleTrillionsWithRemainders() {
        assertToWords(new BigDecimal("1500000000000"), "Satu triliun lima ratus miliar rupiah");
        assertToWords(new BigDecimal("2000000000001"), "Dua triliun satu rupiah");
    }

    @Test
    @DisplayName("Should clean up multiple spaces in result")
    void shouldCleanUpMultipleSpaces() {
        // 20 -> "dua puluh " + convertToWords(0) where convertToWords(0) returns ""
        // This tests the space cleanup in capitalize
        String result = AmountToWordsUtil.toWords(new BigDecimal("20"));
        assertThat(result).doesNotContain("  ");
    }
}
