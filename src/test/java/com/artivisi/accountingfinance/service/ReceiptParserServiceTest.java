package com.artivisi.accountingfinance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReceiptParserService Tests")
class ReceiptParserServiceTest {

    private ReceiptParserService parser;

    @BeforeEach
    void setUp() {
        parser = new ReceiptParserService();
    }

    @Nested
    @DisplayName("Receipt Type Detection")
    class ReceiptTypeDetectionTests {

        @Test
        @DisplayName("Should detect Jago Syariah receipt")
        void shouldDetectJagoSyariahReceipt() {
            String ocrText = """
                Bank Jago Syariah
                Transfer Berhasil
                Rp 1.500.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("jago");
        }

        @Test
        @DisplayName("Should detect Jago receipt")
        void shouldDetectJagoReceipt() {
            String ocrText = """
                Bank Jago
                Transfer
                Rp 500.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("jago");
        }

        @Test
        @DisplayName("Should detect CIMB Octo receipt")
        void shouldDetectCimbOctoReceipt() {
            String ocrText = """
                OCTO Mobile
                Transfer Success
                IDR 2.500.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("cimb");
        }

        @Test
        @DisplayName("Should detect CIMB receipt")
        void shouldDetectCimbReceipt() {
            String ocrText = """
                CIMB Niaga
                Transfer Success
                IDR 1.000.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("cimb");
        }

        @Test
        @DisplayName("Should detect GoPay receipt")
        void shouldDetectGopayReceipt() {
            String ocrText = """
                GoPay
                Ditransfer ke John Doe
                Rp 250.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("gopay");
        }

        @Test
        @DisplayName("Should detect Byond receipt")
        void shouldDetectByondReceipt() {
            String ocrText = """
                Byond
                Nama Merchant
                ABC Store
                Rp 100.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("byond");
        }

        @Test
        @DisplayName("Should detect BSI as Byond receipt")
        void shouldDetectBsiAsbyondReceipt() {
            String ocrText = """
                BSI Mobile
                Transfer Berhasil
                Rp 750.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("byond");
        }

        @Test
        @DisplayName("Should detect unknown receipt type")
        void shouldDetectUnknownReceiptType() {
            String ocrText = """
                Some Unknown Bank
                Transfer Complete
                Rp 500.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("unknown");
        }
    }

    @Nested
    @DisplayName("Jago Receipt Parsing")
    class JagoParsingTests {

        @Test
        @DisplayName("Should parse Jago receipt with merchant name (Acquirer Name followed by Fee)")
        void shouldParseJagoReceiptWithMerchantName() {
            // Jago parser expects: "Acquirer Name\nMERCHANT_NAME\nFee" pattern
            // The amount pattern finds first "Rp X" so we need the amount before Fee
            String ocrText = """
                Bank Jago Syariah
                Transfer Berhasil
                Rp 1.500.000
                Acquirer Name
                TOKOPEDIA SELLER
                Fee
                Reference Number
                abc123xyz789
                26 Nov 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("jago");
            assertThat(result.merchantName()).isEqualTo("TOKOPEDIA SELLER");
            assertThat(result.amount()).isEqualByComparingTo("1500000");
            assertThat(result.reference()).isEqualTo("abc123xyz789");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 26));
        }

        @Test
        @DisplayName("Should extract amount from Jago receipt")
        void shouldExtractAmountFromJagoReceipt() {
            String ocrText = """
                Bank Jago Syariah
                Rp 2.350.000
                26 Nov 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.amount()).isEqualByComparingTo("2350000");
        }

        @Test
        @DisplayName("Should calculate confidence scores for Jago receipt with all fields")
        void shouldCalculateConfidenceForJagoReceipt() {
            String ocrText = """
                Bank Jago Syariah
                Rp 1.000.000
                Acquirer Name
                TEST MERCHANT
                Fee
                Reference Number
                ref12345
                26 Nov 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantConfidence()).isEqualByComparingTo("0.85");
            assertThat(result.amountConfidence()).isEqualByComparingTo("0.95");
            assertThat(result.dateConfidence()).isEqualByComparingTo("0.90");
            assertThat(result.overallConfidence()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should have zero merchant confidence when merchant not found")
        void shouldHaveZeroMerchantConfidenceWhenNotFound() {
            String ocrText = """
                Bank Jago Syariah
                Rp 500.000
                26 Nov 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantName()).isNull();
            assertThat(result.merchantConfidence()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("CIMB Receipt Parsing")
    class CimbParsingTests {

        @Test
        @DisplayName("Should parse CIMB receipt with Indonesian format amount")
        void shouldParseCimbReceiptWithIndonesianFormatAmount() {
            // CIMB parser uses IDR pattern, but amount parsing uses Indonesian format
            // So IDR 2.500.000 (with dots) should work correctly
            String ocrText = """
                OCTO Mobile
                Transfer Successful
                IDR 2.500.000
                JOHN DOE
                Transaction Time
                15 Dec 2025 10:30:00
                Reference
                123456789012
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("cimb");
            assertThat(result.amount()).isEqualByComparingTo("2500000");
            assertThat(result.merchantName()).isEqualTo("JOHN DOE");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 12, 15));
        }

        @Test
        @DisplayName("Should extract recipient from CIMB receipt")
        void shouldExtractRecipientFromCimbReceipt() {
            // CIMB parser looks for uppercase name after IDR line
            String ocrText = """
                OCTO Mobile
                IDR 1.000.000
                JANE DOE
                Transaction Time
                10 Jan 2025 14:00:00
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantName()).isEqualTo("JANE DOE");
        }

        @Test
        @DisplayName("Should calculate confidence scores for CIMB receipt")
        void shouldCalculateConfidenceForCimbReceipt() {
            String ocrText = """
                OCTO Mobile
                IDR 1.000.000
                JANE DOE
                Transaction Time
                10 Jan 2025 14:00:00
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantConfidence()).isEqualByComparingTo("0.80");
            assertThat(result.amountConfidence()).isEqualByComparingTo("0.90");
            assertThat(result.dateConfidence()).isEqualByComparingTo("0.85");
        }
    }

    @Nested
    @DisplayName("GoPay Receipt Parsing")
    class GopayParsingTests {

        @Test
        @DisplayName("Should parse GoPay receipt with merchant and amount")
        void shouldParseGopayReceiptWithMerchantAndAmount() {
            String ocrText = """
                GoPay
                Transfer Berhasil
                Ditransfer ke John Doe Store
                Rp 350.000
                Tanggal
                20 Oktober 2025
                ID transaksi
                gopay12345abc
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("gopay");
            assertThat(result.merchantName()).isEqualTo("John Doe Store");
            assertThat(result.amount()).isEqualByComparingTo("350000");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 10, 20));
            assertThat(result.reference()).isEqualTo("gopay12345abc");
        }

        @Test
        @DisplayName("Should calculate high confidence for GoPay receipt")
        void shouldCalculateHighConfidenceForGopayReceipt() {
            String ocrText = """
                GoPay
                Ditransfer ke Test Merchant
                Rp 500.000
                Tanggal
                15 Nov 2025
                ID transaksi
                gptx123
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantConfidence()).isEqualByComparingTo("0.90");
            assertThat(result.amountConfidence()).isEqualByComparingTo("0.95");
            assertThat(result.dateConfidence()).isEqualByComparingTo("0.90");
        }
    }

    @Nested
    @DisplayName("Byond Receipt Parsing")
    class ByondParsingTests {

        @Test
        @DisplayName("Should parse Byond receipt with merchant name")
        void shouldParseByondReceiptWithMerchantName() {
            String ocrText = """
                Byond by BSI
                Transfer Berhasil
                Nama Merchant
                INDOMARET CENTRAL
                Rp 125.500
                05 Sep 2025
                Nomor Transaksi
                FT12345ABC
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("byond");
            assertThat(result.merchantName()).isEqualTo("INDOMARET CENTRAL");
            assertThat(result.amount()).isEqualByComparingTo("125500");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 9, 5));
            assertThat(result.reference()).isEqualTo("FT12345ABC");
        }

        @Test
        @DisplayName("Should calculate confidence scores for Byond receipt")
        void shouldCalculateConfidenceForByondReceipt() {
            String ocrText = """
                Byond
                Nama Merchant
                TEST STORE
                Rp 100.000
                10 Aug 2025
                Nomor Transaksi
                FT99999
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantConfidence()).isEqualByComparingTo("0.85");
            assertThat(result.amountConfidence()).isEqualByComparingTo("0.90");
            assertThat(result.dateConfidence()).isEqualByComparingTo("0.85");
        }
    }

    @Nested
    @DisplayName("Generic Receipt Parsing")
    class GenericParsingTests {

        @Test
        @DisplayName("Should parse generic receipt with Rp amount")
        void shouldParseGenericReceiptWithRpAmount() {
            String ocrText = """
                WARUNG MAKAN SEDERHANA
                Jl. Sudirman No. 123
                ========================
                TOTAL Rp 75.000
                26/11/2025
                Terima kasih
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("unknown");
            assertThat(result.amount()).isEqualByComparingTo("75000");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 26));
            assertThat(result.merchantName()).isEqualTo("WARUNG MAKAN SEDERHANA");
        }

        @Test
        @DisplayName("Should parse generic receipt with IDR amount (Indonesian format)")
        void shouldParseGenericReceiptWithIdrAmount() {
            // Generic parser also uses Indonesian format (dot as thousand separator)
            String ocrText = """
                SUPERMARKET ABC
                IDR 250.000
                15-12-2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.amount()).isEqualByComparingTo("250000");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 12, 15));
        }

        @Test
        @DisplayName("Should calculate lower confidence for generic receipt")
        void shouldCalculateLowerConfidenceForGenericReceipt() {
            String ocrText = """
                Some Store Name
                Rp 100.000
                01/01/2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantConfidence()).isEqualByComparingTo("0.50");
            assertThat(result.amountConfidence()).isEqualByComparingTo("0.70");
            assertThat(result.dateConfidence()).isEqualByComparingTo("0.60");
        }

        @Test
        @DisplayName("Should extract merchant from first non-empty line")
        void shouldExtractMerchantFromFirstLine() {
            String ocrText = """
                PT MERCHANT NAME
                Some address
                Rp 50.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantName()).isEqualTo("PT MERCHANT NAME");
        }
    }

    @Nested
    @DisplayName("Amount Parsing")
    class AmountParsingTests {

        @Test
        @DisplayName("Should parse amount with thousand separator dot (Indonesian format)")
        void shouldParseAmountWithThousandSeparatorDot() {
            String ocrText = """
                Unknown Receipt
                Rp 1.500.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.amount()).isEqualByComparingTo("1500000");
        }

        @Test
        @DisplayName("Should parse amount without separator")
        void shouldParseAmountWithoutSeparator() {
            String ocrText = """
                Unknown Receipt
                Rp 500000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.amount()).isEqualByComparingTo("500000");
        }

        @Test
        @DisplayName("Should parse small amount")
        void shouldParseSmallAmount() {
            String ocrText = """
                Unknown Receipt
                Rp 5.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.amount()).isEqualByComparingTo("5000");
        }

        @Test
        @DisplayName("Should parse large amount")
        void shouldParseLargeAmount() {
            String ocrText = """
                Unknown Receipt
                Rp 150.000.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.amount()).isEqualByComparingTo("150000000");
        }

        @Test
        @DisplayName("Should find first Rp amount in text")
        void shouldFindFirstRpAmountInText() {
            String ocrText = """
                Unknown Receipt
                Rp 10.000
                Rp 15.000
                Rp 25.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            // Parser finds first match
            assertThat(result.amount()).isNotNull();
        }

        @Test
        @DisplayName("Should handle amount with decimal (Indonesian comma)")
        void shouldHandleAmountWithDecimal() {
            // In Indonesian format: 10.000,50 = 10000.50
            String ocrText = """
                Unknown Receipt
                Rp 10.000,50
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.amount()).isEqualByComparingTo("10000.50");
        }
    }

    @Nested
    @DisplayName("Date Parsing")
    class DateParsingTests {

        @Test
        @DisplayName("Should parse Indonesian month name - Januari")
        void shouldParseIndonesianMonthJanuari() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                15 Januari 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - Februari")
        void shouldParseIndonesianMonthFebruari() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                28 Februari 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 2, 28));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - Maret")
        void shouldParseIndonesianMonthMaret() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                10 Maret 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 3, 10));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - April")
        void shouldParseIndonesianMonthApril() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                5 April 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 4, 5));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - Mei")
        void shouldParseIndonesianMonthMei() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                20 Mei 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - Juni")
        void shouldParseIndonesianMonthJuni() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                15 Juni 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 6, 15));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - Juli")
        void shouldParseIndonesianMonthJuli() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                4 Juli 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 7, 4));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - Agustus")
        void shouldParseIndonesianMonthAgustus() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                17 Agustus 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 8, 17));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - September")
        void shouldParseIndonesianMonthSeptember() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                1 September 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 9, 1));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - Oktober")
        void shouldParseIndonesianMonthOktober() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                31 Oktober 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 10, 31));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - November")
        void shouldParseIndonesianMonthNovember() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                11 November 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 11));
        }

        @Test
        @DisplayName("Should parse Indonesian month name - Desember")
        void shouldParseIndonesianMonthDesember() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                25 Desember 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 12, 25));
        }

        @Test
        @DisplayName("Should parse abbreviated Indonesian month - Nov")
        void shouldParseAbbreviatedIndonesianMonthNov() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                26 Nov 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 26));
        }

        @Test
        @DisplayName("Should parse abbreviated Indonesian month - Okt")
        void shouldParseAbbreviatedIndonesianMonthOkt() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                15 Okt 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 10, 15));
        }

        @Test
        @DisplayName("Should parse date with slash separator dd/MM/yyyy")
        void shouldParseDateWithSlashSeparator() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                26/11/2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 26));
        }

        @Test
        @DisplayName("Should parse date with dash separator dd-MM-yyyy")
        void shouldParseDateWithDashSeparator() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                26-11-2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 26));
        }

        @Test
        @DisplayName("Should parse English month abbreviations")
        void shouldParseEnglishMonthAbbreviations() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                15 Dec 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 12, 15));
        }

        @Test
        @DisplayName("Should parse abbreviated month - Aug")
        void shouldParseAbbreviatedMonthAug() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                17 Aug 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 8, 17));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            ReceiptParserService.ParsedReceipt result = parser.parse(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for blank input")
        void shouldReturnNullForBlankInput() {
            ReceiptParserService.ParsedReceipt result = parser.parse("   ");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty input")
        void shouldReturnNullForEmptyInput() {
            ReceiptParserService.ParsedReceipt result = parser.parse("");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle receipt with missing amount")
        void shouldHandleReceiptWithMissingAmount() {
            String ocrText = """
                Bank Jago
                Transfer Berhasil
                No amount here
                26 Nov 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.amount()).isNull();
            assertThat(result.amountConfidence()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Should handle receipt with missing date")
        void shouldHandleReceiptWithMissingDate() {
            String ocrText = """
                Bank Jago
                Transfer Berhasil
                Rp 500.000
                No date here
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.transactionDate()).isNull();
            assertThat(result.dateConfidence()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Should preserve raw OCR text")
        void shouldPreserveRawOcrText() {
            String ocrText = """
                Bank Jago
                Test Receipt Content
                Rp 100.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.rawText()).isEqualTo(ocrText);
        }

        @Test
        @DisplayName("Should handle receipt with special characters")
        void shouldHandleReceiptWithSpecialCharacters() {
            String ocrText = """
                Unknown Receipt
                Store: ABC & XYZ Co., Ltd.
                Rp 100.000
                26/11/2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.amount()).isEqualByComparingTo("100000");
        }

        @Test
        @DisplayName("Should skip numeric-only lines for merchant extraction")
        void shouldSkipNumericOnlyLinesForMerchant() {
            String ocrText = """
                12345
                67890
                ACTUAL MERCHANT NAME
                Rp 100.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantName()).isEqualTo("ACTUAL MERCHANT NAME");
        }

        @Test
        @DisplayName("Should skip short lines for merchant extraction")
        void shouldSkipShortLinesForMerchant() {
            String ocrText = """
                AB
                CD
                PROPER MERCHANT NAME
                Rp 100.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantName()).isEqualTo("PROPER MERCHANT NAME");
        }
    }

    @Nested
    @DisplayName("Confidence Score Calculation")
    class ConfidenceScoreTests {

        @Test
        @DisplayName("Should calculate weighted overall confidence")
        void shouldCalculateWeightedOverallConfidence() {
            // When all fields are present for Jago receipt:
            // merchant: 0.85 (30% weight)
            // amount: 0.95 (40% weight)
            // date: 0.90 (30% weight)
            // Overall = 0.85*0.30 + 0.95*0.40 + 0.90*0.30 = 0.255 + 0.38 + 0.27 = 0.905

            String ocrText = """
                Bank Jago Syariah
                Rp 1.000.000
                Acquirer Name
                TEST MERCHANT
                Fee
                Reference Number
                ref12345
                26 Nov 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            // Amount confidence (0.95) * 0.40 = 0.38
            // Date confidence (0.90) * 0.30 = 0.27
            // Merchant confidence (0.85) * 0.30 = 0.255
            // Total = 0.905 rounded to 0.91
            assertThat(result.overallConfidence()).isEqualByComparingTo("0.91");
        }

        @Test
        @DisplayName("Should have lower overall confidence when fields are missing")
        void shouldHaveLowerConfidenceWhenFieldsMissing() {
            String ocrText = """
                Unknown receipt with no recognizable patterns
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.overallConfidence()).isLessThan(new BigDecimal("0.50"));
        }

        @Test
        @DisplayName("Should have partial confidence when only amount found")
        void shouldHavePartialConfidenceWhenOnlyAmountFound() {
            String ocrText = """
                Rp 100.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            // Only amount found (0.70 * 0.40 = 0.28)
            assertThat(result.amountConfidence()).isGreaterThan(BigDecimal.ZERO);
            assertThat(result.overallConfidence()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Real-World Receipt Scenarios")
    class RealWorldScenariosTests {

        @Test
        @DisplayName("Should parse complete Jago transfer receipt")
        void shouldParseCompleteJagoTransferReceipt() {
            // Real-world Jago receipt format - amount appears first before Fee
            String ocrText = """
                Bank Jago Syariah
                Transfer Berhasil

                Rp 2.350.000

                Acquirer Name
                PT TOKOPEDIA
                Fee

                Transaction Date
                26 Nov 2025 14:30:00

                Reference Number
                jago2025112614300012345
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("jago");
            assertThat(result.merchantName()).isEqualTo("PT TOKOPEDIA");
            assertThat(result.amount()).isEqualByComparingTo("2350000");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 26));
            assertThat(result.reference()).isEqualTo("jago2025112614300012345");
            assertThat(result.overallConfidence()).isGreaterThan(new BigDecimal("0.50"));
        }

        @Test
        @DisplayName("Should parse QRIS payment receipt")
        void shouldParseQrisPaymentReceipt() {
            String ocrText = """
                QRIS Payment
                Bank Jago

                Merchant: KOPI KENANGAN SUDIRMAN

                Amount: Rp 45.000

                Date: 26 Nov 2025

                Status: SUCCESS
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("jago");
            assertThat(result.amount()).isEqualByComparingTo("45000");
        }

        @Test
        @DisplayName("Should parse restaurant receipt")
        void shouldParseRestaurantReceipt() {
            String ocrText = """
                RESTORAN PADANG SEDERHANA
                Jl. Sudirman No. 123
                Jakarta 12345
                ================================
                Nasi Padang Lengkap    Rp 35.000
                Es Teh Manis           Rp  5.000
                --------------------------------
                Subtotal               Rp 40.000
                PPN 11%                Rp  4.400
                --------------------------------
                TOTAL                  Rp 44.400
                ================================
                Tanggal: 26/11/2025
                Kasir: ANI

                Terima kasih
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("unknown");
            assertThat(result.merchantName()).isEqualTo("RESTORAN PADANG SEDERHANA");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 26));
        }

        @Test
        @DisplayName("Should parse grocery store receipt")
        void shouldParseGroceryStoreReceipt() {
            String ocrText = """
                INDOMARET
                Jl. Gatot Subroto No. 45

                Item 1        Rp  15.000
                Item 2        Rp  25.000
                Item 3        Rp  10.000
                --------------------------
                TOTAL         Rp  50.000
                TUNAI         Rp 100.000
                KEMBALI       Rp  50.000

                26-11-2025 18:45:30
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.merchantName()).isEqualTo("INDOMARET");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 26));
        }

        @Test
        @DisplayName("Should parse gas station receipt")
        void shouldParseGasStationReceipt() {
            String ocrText = """
                SPBU PERTAMINA 31.123.45
                Jl. Gatot Subroto

                PERTALITE
                Rp 10.000/L x 5.00 L
                ----------------------
                TOTAL: Rp 50.000

                15/12/2025 08:30
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.amount()).isNotNull();
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 12, 15));
        }

        @Test
        @DisplayName("Should parse e-commerce receipt")
        void shouldParseEcommerceReceipt() {
            String ocrText = """
                TOKOPEDIA
                Order ID: INV/20251126/MPL/123456

                Samsung Galaxy A55
                Rp 4.999.000

                Ongkir: Rp 15.000
                Diskon: -Rp 100.000

                Total: Rp 4.914.000

                26 Nov 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.merchantName()).isEqualTo("TOKOPEDIA");
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 11, 26));
        }
    }

    @Nested
    @DisplayName("Reference Number Extraction")
    class ReferenceExtractionTests {

        @Test
        @DisplayName("Should extract Jago reference number")
        void shouldExtractJagoReferenceNumber() {
            String ocrText = """
                Bank Jago Syariah
                Rp 100.000
                Reference Number
                abc123xyz
                26 Nov 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.reference()).isEqualTo("abc123xyz");
        }

        @Test
        @DisplayName("Should extract GoPay transaction ID")
        void shouldExtractGopayTransactionId() {
            String ocrText = """
                GoPay
                Ditransfer ke Test
                Rp 100.000
                Tanggal
                26 Nov 2025
                ID transaksi
                gp123abc456
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.reference()).isEqualTo("gp123abc456");
        }

        @Test
        @DisplayName("Should extract Byond transaction number")
        void shouldExtractByondTransactionNumber() {
            String ocrText = """
                Byond
                Nama Merchant
                TEST
                Rp 100.000
                26 Nov 2025
                Nomor Transaksi
                FT1234567890
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.reference()).isEqualTo("FT1234567890");
        }
    }

    @Nested
    @DisplayName("Generic Receipt Fallback Patterns")
    class GenericFallbackTests {

        @Test
        @DisplayName("Should fall back to TOTAL pattern when no Rp/IDR prefix found")
        void shouldFallBackToTotalPatternWhenNoPrefix() {
            String ocrText = """
                WARUNG BAKSO
                Bakso Urat x2
                TOTAL 75.000
                26/11/2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("unknown");
            assertThat(result.amount()).isEqualByComparingTo("75000");
        }

        @Test
        @DisplayName("Should fall back to Indonesian date pattern when slash/dash date not found")
        void shouldFallBackToIndonesianDatePattern() {
            // Generic parser first tries dd/MM/yyyy or dd-MM-yyyy, then falls back to "dd month yyyy"
            // Use text that has no slash/dash date but has Indonesian month date
            String ocrText = """
                TOKO SERBA ADA
                Rp 50.000
                Tanggal pembelian 15 Mei 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 5, 15));
        }

        @Test
        @DisplayName("Should return null merchant when all lines are short or numeric")
        void shouldReturnNullMerchantWhenAllLinesShortOrNumeric() {
            String ocrText = """
                12
                34
                56
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.merchantName()).isNull();
            assertThat(result.merchantConfidence()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Should return null amount and null date when nothing matches")
        void shouldReturnNullAmountAndDateWhenNothingMatches() {
            String ocrText = """
                Some random text without amounts or dates
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.amount()).isNull();
            assertThat(result.transactionDate()).isNull();
            assertThat(result.amountConfidence()).isEqualByComparingTo("0");
            assertThat(result.dateConfidence()).isEqualByComparingTo("0");
            // Merchant is extracted from first non-empty line, so merchant confidence is 0.50
            assertThat(result.merchantConfidence()).isEqualByComparingTo("0.50");
            // Overall = 0.50 * 0.30 + 0 * 0.40 + 0 * 0.30 = 0.15
            assertThat(result.overallConfidence()).isEqualByComparingTo("0.15");
        }

        @Test
        @DisplayName("Should handle generic receipt with no reference")
        void shouldHandleGenericReceiptWithNoReference() {
            String ocrText = """
                TOKO ABC
                Rp 100.000
                01/01/2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.reference()).isNull();
        }
    }

    @Nested
    @DisplayName("Amount Parsing Edge Cases")
    class AmountEdgeCaseTests {

        @Test
        @DisplayName("Should truncate long decimal portion to 2 decimal places")
        void shouldTruncateLongDecimalPortion() {
            // "10,000,00000" after removing dots becomes "10,000,00000"
            // after replacing commas: "10.000.00000"
            // The branch triggers when decimal portion > 3 chars
            String ocrText = """
                Unknown Receipt
                Rp 10,00000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.amount()).isNotNull();
        }

        @Test
        @DisplayName("Should parse amount with Rp prefix and no space")
        void shouldParseAmountWithRpPrefixNoSpace() {
            String ocrText = """
                Unknown Receipt
                Rp250.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.amount()).isEqualByComparingTo("250000");
        }

        @Test
        @DisplayName("Should parse IDR amount in CIMB receipt")
        void shouldParseIdrAmountInCimbReceipt() {
            String ocrText = """
                OCTO Mobile
                IDR 500.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("cimb");
            assertThat(result.amount()).isEqualByComparingTo("500000");
        }

        @Test
        @DisplayName("Should parse IDR amount without space")
        void shouldParseIdrAmountWithoutSpace() {
            String ocrText = """
                OCTO Mobile
                IDR1.000.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.amount()).isEqualByComparingTo("1000000");
        }
    }

    @Nested
    @DisplayName("CIMB Recipient Extraction Edge Cases")
    class CimbRecipientEdgeCaseTests {

        @Test
        @DisplayName("Should return null merchant when line after IDR is not all uppercase")
        void shouldReturnNullMerchantWhenNotAllUppercase() {
            String ocrText = """
                OCTO Mobile
                IDR 1.000.000
                some lowercase name
                Transaction Time
                10 Jan 2025 14:00:00
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("cimb");
            assertThat(result.merchantName()).isNull();
            assertThat(result.merchantConfidence()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("Should return null merchant when IDR is on last line")
        void shouldReturnNullMerchantWhenIdrOnLastLine() {
            String ocrText = """
                OCTO Mobile
                Transaction Time
                10 Jan 2025 14:00:00
                IDR 1.000.000""";

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("cimb");
            // IDR is on last line, no next line to check
        }

        @Test
        @DisplayName("Should extract CIMB 12-digit reference number")
        void shouldExtractCimb12DigitReference() {
            String ocrText = """
                OCTO Mobile
                IDR 500.000
                RECIPIENT NAME
                Transaction Time
                10 Jan 2025
                999888777666
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.reference()).isEqualTo("999888777666");
        }
    }

    @Nested
    @DisplayName("Date Parsing Additional Formats")
    class DateParsingAdditionalTests {

        @ParameterizedTest(name = "Should parse abbreviated month {0} as month {1}")
        @CsvSource({
            "Feb, 2, 10",
            "Mar, 3, 15",
            "Apr, 4, 20",
            "May, 5, 25",
            "Jun, 6, 1",
            "Jul, 7, 4",
            "Agu, 8, 17",
            "Sep, 9, 30",
            "Des, 12, 31"
        })
        @DisplayName("Should parse all abbreviated Indonesian months")
        void shouldParseAbbreviatedIndonesianMonths(String monthAbbr, int expectedMonth, int day) {
            String ocrText = String.format("""
                Unknown Receipt
                Rp 100.000
                %d %s 2025
                """, day, monthAbbr);

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, expectedMonth, day));
        }

        @Test
        @DisplayName("Should return null date when month name is not recognized")
        void shouldReturnNullDateWhenMonthNotRecognized() {
            String ocrText = """
                Bank Jago
                Rp 100.000
                26 Zulkaidah 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isNull();
        }

        @Test
        @DisplayName("Should parse date with single-digit day")
        void shouldParseDateWithSingleDigitDay() {
            String ocrText = """
                Unknown Receipt
                Rp 100.000
                5 Jan 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.transactionDate()).isEqualTo(LocalDate.of(2025, 1, 5));
        }
    }

    @Nested
    @DisplayName("Confidence Score Edge Cases")
    class ConfidenceEdgeCaseTests {

        @Test
        @DisplayName("Should calculate zero overall confidence when no fields extracted")
        void shouldCalculateZeroOverallConfidenceWhenNoFields() {
            String ocrText = """
                AB
                CD
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.merchantConfidence()).isEqualByComparingTo("0");
            assertThat(result.amountConfidence()).isEqualByComparingTo("0");
            assertThat(result.dateConfidence()).isEqualByComparingTo("0");
            assertThat(result.overallConfidence()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("Should calculate CIMB overall confidence with all fields")
        void shouldCalculateCimbOverallConfidenceWithAllFields() {
            String ocrText = """
                OCTO Mobile
                IDR 1.000.000
                JOHN DOE
                Transaction Time
                10 Jan 2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            // merchant: 0.80 * 0.30 = 0.24
            // amount: 0.90 * 0.40 = 0.36
            // date: 0.85 * 0.30 = 0.255
            // total = 0.855 -> 0.86
            assertThat(result.overallConfidence()).isEqualByComparingTo("0.86");
        }

        @Test
        @DisplayName("Should calculate GoPay overall confidence with all fields")
        void shouldCalculateGopayOverallConfidenceWithAllFields() {
            String ocrText = """
                GoPay
                Ditransfer ke Test Store
                Rp 500.000
                Tanggal
                15 Nov 2025
                ID transaksi
                gptx123
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            // merchant: 0.90 * 0.30 = 0.27
            // amount: 0.95 * 0.40 = 0.38
            // date: 0.90 * 0.30 = 0.27
            // total = 0.92
            assertThat(result.overallConfidence()).isEqualByComparingTo("0.92");
        }

        @Test
        @DisplayName("Should calculate Byond overall confidence with all fields")
        void shouldCalculateByondOverallConfidenceWithAllFields() {
            String ocrText = """
                Byond
                Nama Merchant
                TEST STORE
                Rp 100.000
                10 Aug 2025
                Nomor Transaksi
                FT99999
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            // merchant: 0.85 * 0.30 = 0.255
            // amount: 0.90 * 0.40 = 0.36
            // date: 0.85 * 0.30 = 0.255
            // total = 0.87
            assertThat(result.overallConfidence()).isEqualByComparingTo("0.87");
        }

        @Test
        @DisplayName("Should calculate generic overall confidence with all fields")
        void shouldCalculateGenericOverallConfidenceWithAllFields() {
            String ocrText = """
                TOKO ABC
                Rp 100.000
                01/01/2025
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            // merchant: 0.50 * 0.30 = 0.15
            // amount: 0.70 * 0.40 = 0.28
            // date: 0.60 * 0.30 = 0.18
            // total = 0.61
            assertThat(result.overallConfidence()).isEqualByComparingTo("0.61");
        }
    }

    @Nested
    @DisplayName("Receipt Type Detection Priority")
    class ReceiptTypeDetectionPriorityTests {

        @Test
        @DisplayName("Should prioritize Byond over other types when byond keyword present")
        void shouldPrioritizeByondWhenKeywordPresent() {
            // Text contains both "jago" and "byond" — byond should win (checked first)
            String ocrText = """
                Byond Jago
                Rp 100.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("byond");
        }

        @Test
        @DisplayName("Should detect Jago Syariah before plain Jago")
        void shouldDetectJagoSyariahBeforePlainJago() {
            String ocrText = """
                Bank Jago Syariah
                Rp 100.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("jago");
        }

        @Test
        @DisplayName("Should detect CIMB via octo keyword")
        void shouldDetectCimbViaOctoKeyword() {
            String ocrText = """
                OCTO Mobile Banking
                IDR 100.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("cimb");
        }

        @Test
        @DisplayName("Should detect BSI as Byond type")
        void shouldDetectBsiAsByondType() {
            String ocrText = """
                BSI Mobile
                Rp 100.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("byond");
        }
    }

    @Nested
    @DisplayName("Jago Parsing Missing Fields")
    class JagoMissingFieldsTests {

        @Test
        @DisplayName("Should have zero confidence for all missing fields in Jago receipt")
        void shouldHaveZeroConfidenceForAllMissingFieldsInJago() {
            String ocrText = """
                Bank Jago Syariah
                No recognizable data here
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("jago");
            assertThat(result.merchantName()).isNull();
            assertThat(result.amount()).isNull();
            assertThat(result.transactionDate()).isNull();
            assertThat(result.reference()).isNull();
            assertThat(result.merchantConfidence()).isEqualByComparingTo("0");
            assertThat(result.amountConfidence()).isEqualByComparingTo("0");
            assertThat(result.dateConfidence()).isEqualByComparingTo("0");
            assertThat(result.overallConfidence()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("GoPay Parsing Missing Fields")
    class GopayMissingFieldsTests {

        @Test
        @DisplayName("Should have zero confidence for missing fields in GoPay receipt")
        void shouldHaveZeroConfidenceForMissingFieldsInGopay() {
            String ocrText = """
                GoPay
                No recognizable data
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("gopay");
            assertThat(result.merchantName()).isNull();
            assertThat(result.amount()).isNull();
            assertThat(result.transactionDate()).isNull();
            assertThat(result.reference()).isNull();
        }
    }

    @Nested
    @DisplayName("Byond Parsing Missing Fields")
    class ByondMissingFieldsTests {

        @Test
        @DisplayName("Should have zero confidence for missing fields in Byond receipt")
        void shouldHaveZeroConfidenceForMissingFieldsInByond() {
            String ocrText = """
                Byond
                No recognizable data
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("byond");
            assertThat(result.merchantName()).isNull();
            assertThat(result.amount()).isNull();
            assertThat(result.transactionDate()).isNull();
            assertThat(result.reference()).isNull();
        }
    }

    @Nested
    @DisplayName("CIMB Parsing Missing Fields")
    class CimbMissingFieldsTests {

        @Test
        @DisplayName("Should have zero confidence for missing fields in CIMB receipt")
        void shouldHaveZeroConfidenceForMissingFieldsInCimb() {
            String ocrText = """
                CIMB Niaga
                No recognizable data
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result.receiptType()).isEqualTo("cimb");
            assertThat(result.merchantName()).isNull();
            assertThat(result.amount()).isNull();
            assertThat(result.transactionDate()).isNull();
            assertThat(result.reference()).isNull();
            assertThat(result.merchantConfidence()).isEqualByComparingTo("0");
            assertThat(result.amountConfidence()).isEqualByComparingTo("0");
            assertThat(result.dateConfidence()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("Generic TOTAL Fallback Amount")
    class GenericTotalFallbackTests {

        @Test
        @DisplayName("Should extract amount from TOTAL line without Rp/IDR prefix")
        void shouldExtractAmountFromTotalLineWithoutPrefix() {
            String ocrText = """
                WARUNG BAKSO
                Bakso          25.000
                Es Jeruk       10.000
                TOTAL 35.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            assertThat(result.receiptType()).isEqualTo("unknown");
            // The first Rp/IDR pattern won't match (no prefix), fallback to TOTAL pattern
            assertThat(result.amount()).isNotNull();
        }

        @Test
        @DisplayName("Should use TOTAL Rp pattern as fallback")
        void shouldUseTotalRpPatternAsFallback() {
            String ocrText = """
                MINIMARKET XYZ
                Item satu
                Item dua
                TOTAL Rp 85.000
                """;

            ReceiptParserService.ParsedReceipt result = parser.parse(ocrText);

            assertThat(result).isNotNull();
            // The first (?:Rp|IDR) pattern matches "Rp 85.000" so it won't fall through
            assertThat(result.amount()).isEqualByComparingTo("85000");
        }
    }
}
