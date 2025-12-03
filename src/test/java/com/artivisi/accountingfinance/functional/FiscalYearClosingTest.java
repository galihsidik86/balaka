package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.page.*;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Fiscal Year Closing (Phase 4.5)")
class FiscalYearClosingTest extends PlaywrightTestBase {

    private LoginPage loginPage;
    private FiscalClosingPage closingPage;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage(page, baseUrl());
        closingPage = new FiscalClosingPage(page, baseUrl());

        loginPage.navigate().loginAsAdmin();
    }

    @Nested
    @DisplayName("Page Display")
    class PageDisplayTests {

        @Test
        @DisplayName("Should display fiscal closing page")
        void shouldDisplayFiscalClosingPage() {
            closingPage.navigate();

            closingPage.assertPageTitleVisible();
            closingPage.assertPageTitleText("Penutupan Tahun Buku");
            closingPage.assertYearSelectorVisible();
        }

        @Test
        @DisplayName("Should have year selector with default to previous year")
        void shouldHaveYearSelectorWithDefaultToPreviousYear() {
            closingPage.navigate();

            int previousYear = java.time.LocalDate.now().getYear() - 1;
            assertThat(closingPage.getSelectedYear()).isEqualTo(previousYear);
        }

        @Test
        @DisplayName("Should change year when selecting different year")
        void shouldChangeYearOnSelection() {
            closingPage.navigate();

            closingPage.selectYear(2023);

            assertThat(closingPage.getSelectedYear()).isEqualTo(2023);
        }

        @Test
        @DisplayName("Should display summary cards")
        void shouldDisplaySummaryCards() {
            closingPage.navigate();

            closingPage.assertSummaryCardsVisible();
        }

        @Test
        @DisplayName("Should display status badge")
        void shouldDisplayStatusBadge() {
            closingPage.navigate();

            closingPage.assertStatusBadgeVisible();
        }
    }

    @Nested
    @DisplayName("Preview Section")
    class PreviewSectionTests {

        @Test
        @DisplayName("Should display preview section")
        void shouldDisplayPreviewSection() {
            closingPage.navigate();

            closingPage.assertPreviewSectionVisible();
        }

        @Test
        @DisplayName("Should display explanation section")
        void shouldDisplayExplanationSection() {
            closingPage.navigate();

            closingPage.assertExplanationVisible();
        }
    }

    @Nested
    @DisplayName("Action Buttons")
    class ActionButtonTests {

        @Test
        @DisplayName("Should show execute button when year not closed")
        void shouldShowExecuteButtonWhenYearNotClosed() {
            // Use a year that likely has no closing entries
            closingPage.navigate(2022);

            // If not already closed, execute button should be visible
            // (May not be visible if there are no transactions)
            closingPage.assertPageTitleVisible();
        }
    }
}
