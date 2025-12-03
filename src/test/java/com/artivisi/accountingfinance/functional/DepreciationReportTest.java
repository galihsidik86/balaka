package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.page.*;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Depreciation Report (Phase 4.4)")
class DepreciationReportTest extends PlaywrightTestBase {

    private LoginPage loginPage;
    private DepreciationReportPage reportPage;
    private AssetFormPage assetFormPage;
    private AssetDetailPage assetDetailPage;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage(page, baseUrl());
        reportPage = new DepreciationReportPage(page, baseUrl());
        assetFormPage = new AssetFormPage(page, baseUrl());
        assetDetailPage = new AssetDetailPage(page, baseUrl());

        loginPage.navigate().loginAsAdmin();
    }

    @Nested
    @DisplayName("Report Display")
    class ReportDisplayTests {

        @Test
        @DisplayName("Should display depreciation report page")
        void shouldDisplayDepreciationReportPage() {
            reportPage.navigate();

            reportPage.assertPageTitleVisible();
            reportPage.assertPageTitleText("Laporan Penyusutan");
            reportPage.assertYearSelectorVisible();
        }

        @Test
        @DisplayName("Should have year selector with default current year")
        void shouldHaveYearSelector() {
            reportPage.navigate();

            int currentYear = java.time.LocalDate.now().getYear();
            assertThat(reportPage.getSelectedYear()).isEqualTo(currentYear);
        }

        @Test
        @DisplayName("Should change year when selecting different year")
        void shouldChangeYearOnSelection() {
            reportPage.navigate();

            reportPage.selectYear(2024);

            assertThat(reportPage.getSelectedYear()).isEqualTo(2024);
        }

        @Test
        @DisplayName("Should display summary cards")
        void shouldDisplaySummaryCards() {
            reportPage.navigate();

            reportPage.assertSummaryCardsVisible();
        }

        @Test
        @DisplayName("Should display table with correct columns")
        void shouldDisplayTableWithCorrectColumns() {
            reportPage.navigate();

            reportPage.assertTableVisible();
            reportPage.assertColumnHeaderVisible("Nama Aset");
            reportPage.assertColumnHeaderVisible("Kategori");
            reportPage.assertColumnHeaderVisible("Tgl Perolehan");
            reportPage.assertColumnHeaderVisible("Harga Perolehan");
            reportPage.assertColumnHeaderVisible("Masa Manfaat");
            reportPage.assertColumnHeaderVisible("Metode");
            reportPage.assertColumnHeaderVisible("Penyusutan Tahun Ini");
            reportPage.assertColumnHeaderVisible("Akum. Penyusutan");
            reportPage.assertColumnHeaderVisible("Nilai Buku");
        }
    }

    @Nested
    @DisplayName("Report with Asset Data")
    class ReportWithDataTests {

        @Test
        @DisplayName("Should show table or empty message for report")
        void shouldShowTableOrEmptyMessage() {
            int currentYear = java.time.LocalDate.now().getYear();

            // Navigate to depreciation report
            reportPage.navigate();
            reportPage.selectYear(currentYear);

            // Either table with data or empty message should be visible
            reportPage.assertTableOrEmptyMessageVisible();
        }
    }

    @Nested
    @DisplayName("Print View")
    class PrintViewTests {

        @Test
        @DisplayName("Should have print button")
        void shouldHavePrintButton() {
            reportPage.navigate();

            reportPage.assertPrintButtonVisible();
        }
    }
}
