package com.artivisi.accountingfinance.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DataMaskingUtil Tests")
class DataMaskingUtilTest {

    @Nested
    @DisplayName("maskNik() - NIK KTP Masking")
    class MaskNikTests {

        @Test
        @DisplayName("Should mask 16-digit NIK showing first 4 and last 4")
        void shouldMask16DigitNik() {
            String nik = "3201234567890001";

            String masked = DataMaskingUtil.maskNik(nik);

            assertThat(masked).isEqualTo("3201********0001");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNull() {
            assertThat(DataMaskingUtil.maskNik(null)).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"1234567", "12345", ""})
        @DisplayName("Should return original for short strings")
        void shouldReturnOriginalForShortStrings(String input) {
            assertThat(DataMaskingUtil.maskNik(input)).isEqualTo(input);
        }

        @Test
        @DisplayName("Should handle exactly 8 characters")
        void shouldHandleExact8Chars() {
            String nik = "12345678";

            String masked = DataMaskingUtil.maskNik(nik);

            // 8 chars with show 4+4 = no masking needed
            assertThat(masked).isEqualTo("12345678");
        }
    }

    @Nested
    @DisplayName("maskNpwp() - NPWP Masking")
    class MaskNpwpTests {

        @Test
        @DisplayName("Should mask formatted NPWP")
        void shouldMaskFormattedNpwp() {
            String npwp = "12.345.678.9-012.345";

            String masked = DataMaskingUtil.maskNpwp(npwp);

            assertThat(masked).isEqualTo("12.3*************345");
        }

        @Test
        @DisplayName("Should mask unformatted NPWP")
        void shouldMaskUnformattedNpwp() {
            String npwp = "123456789012345";

            String masked = DataMaskingUtil.maskNpwp(npwp);

            assertThat(masked).isEqualTo("1234********345");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNull(String input) {
            assertThat(DataMaskingUtil.maskNpwp(input)).isNull();
        }
    }

    @Nested
    @DisplayName("maskBankAccount() - Bank Account Masking")
    class MaskBankAccountTests {

        @Test
        @DisplayName("Should mask bank account showing first 3 and last 3")
        void shouldMaskBankAccount() {
            String account = "1234567890";

            String masked = DataMaskingUtil.maskBankAccount(account);

            assertThat(masked).isEqualTo("123****890");
        }

        @Test
        @DisplayName("Should handle various account lengths")
        void shouldHandleVariousLengths() {
            assertThat(DataMaskingUtil.maskBankAccount("123456789012345"))
                    .isEqualTo("123*********345");
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345", "1234", ""})
        @DisplayName("Should return original for short accounts")
        void shouldReturnOriginalForShortAccounts(String input) {
            assertThat(DataMaskingUtil.maskBankAccount(input)).isEqualTo(input);
        }
    }

    @Nested
    @DisplayName("maskPhone() - Phone Number Masking")
    class MaskPhoneTests {

        @Test
        @DisplayName("Should mask phone showing first 4 and last 3")
        void shouldMaskPhone() {
            String phone = "081234567890";

            String masked = DataMaskingUtil.maskPhone(phone);

            assertThat(masked).isEqualTo("0812*****890");
        }

        @Test
        @DisplayName("Should handle international format")
        void shouldHandleInternationalFormat() {
            String phone = "+6281234567890";

            String masked = DataMaskingUtil.maskPhone(phone);

            assertThat(masked).isEqualTo("+628*******890");
        }

        @ParameterizedTest
        @ValueSource(strings = {"123456", "12345", ""})
        @DisplayName("Should return original for short numbers")
        void shouldReturnOriginalForShortNumbers(String input) {
            assertThat(DataMaskingUtil.maskPhone(input)).isEqualTo(input);
        }
    }

    @Nested
    @DisplayName("maskBpjsNumber() - BPJS Number Masking")
    class MaskBpjsNumberTests {

        @Test
        @DisplayName("Should mask BPJS number showing first 3 and last 3")
        void shouldMaskBpjsNumber() {
            String bpjs = "0001234567890";

            String masked = DataMaskingUtil.maskBpjsNumber(bpjs);

            assertThat(masked).isEqualTo("000*******890");
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345", "1234", ""})
        @DisplayName("Should return original for short numbers")
        void shouldReturnOriginalForShortNumbers(String input) {
            assertThat(DataMaskingUtil.maskBpjsNumber(input)).isEqualTo(input);
        }
    }

    @Nested
    @DisplayName("maskEmail() - Email Masking")
    class MaskEmailTests {

        @Test
        @DisplayName("Should mask email keeping first 2 chars and domain")
        void shouldMaskEmail() {
            String email = "john.doe@example.com";

            String masked = DataMaskingUtil.maskEmail(email);

            assertThat(masked).isEqualTo("jo******@example.com");
        }

        @Test
        @DisplayName("Should handle short local part")
        void shouldHandleShortLocalPart() {
            String email = "ab@example.com";

            String masked = DataMaskingUtil.maskEmail(email);

            // Local part <= 2 chars, return as-is
            assertThat(masked).isEqualTo("ab@example.com");
        }

        @Test
        @DisplayName("Should return original for email without @")
        void shouldReturnOriginalWithoutAt() {
            String notEmail = "notanemail";

            assertThat(DataMaskingUtil.maskEmail(notEmail)).isEqualTo(notEmail);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNull(String input) {
            assertThat(DataMaskingUtil.maskEmail(input)).isNull();
        }
    }

    @Nested
    @DisplayName("maskMiddle() - Generic Masking")
    class MaskMiddleTests {

        @ParameterizedTest
        @CsvSource({
                "1234567890, 3, 3, 123****890",
                "abcdefghij, 2, 2, ab******ij",
                "test, 1, 1, t**t",
                "ab, 1, 1, ab"
        })
        @DisplayName("Should mask middle portion correctly")
        void shouldMaskMiddleCorrectly(String input, int showFirst, int showLast, String expected) {
            assertThat(DataMaskingUtil.maskMiddle(input, showFirst, showLast)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should return original when length equals showFirst + showLast")
        void shouldReturnOriginalWhenLengthEquals() {
            String input = "abcd";

            String masked = DataMaskingUtil.maskMiddle(input, 2, 2);

            assertThat(masked).isEqualTo("abcd");
        }

        @Test
        @DisplayName("Should return original when length is less than showFirst + showLast")
        void shouldReturnOriginalWhenLengthLess() {
            String input = "abc";

            String masked = DataMaskingUtil.maskMiddle(input, 2, 2);

            assertThat(masked).isEqualTo("abc");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNull(String input) {
            assertThat(DataMaskingUtil.maskMiddle(input, 3, 3)).isNull();
        }

        @Test
        @DisplayName("Should mask single character in middle")
        void shouldMaskSingleCharInMiddle() {
            // 5 chars, show 2+2 = mask 1
            assertThat(DataMaskingUtil.maskMiddle("abcde", 2, 2)).isEqualTo("ab*de");
        }
    }

    @Nested
    @DisplayName("Additional Coverage for Null and Short Inputs")
    class AdditionalNullShortInputTests {

        @Test
        @DisplayName("Should return null for null NPWP short input")
        void shouldReturnNpwpShortInput() {
            assertThat(DataMaskingUtil.maskNpwp("1234567")).isEqualTo("1234567");
            assertThat(DataMaskingUtil.maskNpwp("")).isEqualTo("");
        }

        @Test
        @DisplayName("Should return null for null bank account")
        void shouldReturnNullForNullBankAccount() {
            assertThat(DataMaskingUtil.maskBankAccount(null)).isNull();
        }

        @Test
        @DisplayName("Should return null for null phone")
        void shouldReturnNullForNullPhone() {
            assertThat(DataMaskingUtil.maskPhone(null)).isNull();
        }

        @Test
        @DisplayName("Should return null for null BPJS number")
        void shouldReturnNullForNullBpjs() {
            assertThat(DataMaskingUtil.maskBpjsNumber(null)).isNull();
        }

        @Test
        @DisplayName("Should handle exactly 6 char bank account (showFirst + showLast = 6)")
        void shouldHandleExact6CharBankAccount() {
            assertThat(DataMaskingUtil.maskBankAccount("123456")).isEqualTo("123456");
        }

        @Test
        @DisplayName("Should handle 7 char bank account (one char masked)")
        void shouldHandle7CharBankAccount() {
            assertThat(DataMaskingUtil.maskBankAccount("1234567")).isEqualTo("123*567");
        }

        @Test
        @DisplayName("Should handle exactly 7 char phone (showFirst + showLast = 7)")
        void shouldHandleExact7CharPhone() {
            assertThat(DataMaskingUtil.maskPhone("1234567")).isEqualTo("1234567");
        }

        @Test
        @DisplayName("Should handle 8 char phone (one char masked)")
        void shouldHandle8CharPhone() {
            assertThat(DataMaskingUtil.maskPhone("12345678")).isEqualTo("1234*678");
        }

        @Test
        @DisplayName("Should handle exactly 6 char BPJS (showFirst + showLast = 6)")
        void shouldHandleExact6CharBpjs() {
            assertThat(DataMaskingUtil.maskBpjsNumber("123456")).isEqualTo("123456");
        }

        @Test
        @DisplayName("Should handle email with exactly 1 char local part")
        void shouldHandleEmailWith1CharLocalPart() {
            assertThat(DataMaskingUtil.maskEmail("a@example.com")).isEqualTo("a@example.com");
        }

        @Test
        @DisplayName("Should handle email with 3 char local part")
        void shouldHandleEmailWith3CharLocalPart() {
            assertThat(DataMaskingUtil.maskEmail("abc@example.com")).isEqualTo("ab*@example.com");
        }

        @Test
        @DisplayName("Should handle 9 digit NIK")
        void shouldHandle9DigitNik() {
            assertThat(DataMaskingUtil.maskNik("123456789")).isEqualTo("1234*6789");
        }

        @Test
        @DisplayName("Should handle maskMiddle with showFirst 0 and showLast 0")
        void shouldHandleMaskMiddleZeroShowFirstAndLast() {
            assertThat(DataMaskingUtil.maskMiddle("abcdef", 0, 0)).isEqualTo("******");
        }
    }
}
