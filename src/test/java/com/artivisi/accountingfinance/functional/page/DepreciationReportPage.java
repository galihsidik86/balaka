package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class DepreciationReportPage {
    private final Page page;
    private final String baseUrl;

    public DepreciationReportPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public void navigate() {
        page.navigate(baseUrl + "/reports/depreciation");
    }

    public void navigate(int year) {
        page.navigate(baseUrl + "/reports/depreciation?year=" + year);
    }

    // Page title
    public void assertPageTitleVisible() {
        assertThat(page.locator("h1")).isVisible();
    }

    public void assertPageTitleText(String text) {
        assertThat(page.locator("h1")).containsText(text);
    }

    // Year selector
    public void assertYearSelectorVisible() {
        assertThat(page.locator("select#year")).isVisible();
    }

    public int getSelectedYear() {
        String value = page.locator("select#year").inputValue();
        return Integer.parseInt(value);
    }

    public void selectYear(int year) {
        page.selectOption("select#year", String.valueOf(year));
        page.waitForLoadState();
    }

    // Summary cards
    public void assertSummaryCardsVisible() {
        assertThat(page.locator("#summary-cards")).isVisible();
        assertThat(page.locator("#card-purchase-cost")).isVisible();
        assertThat(page.locator("#card-book-value")).isVisible();
    }

    // Table
    public void assertTableVisible() {
        assertThat(page.locator("#depreciation-table")).isVisible();
    }

    public void assertColumnHeaderVisible(String headerText) {
        assertThat(page.locator("th:has-text('" + headerText + "')")).isVisible();
    }

    // Check if asset is in report
    public boolean hasAssetInReport(String assetName) {
        Locator row = page.locator("tbody tr:has-text('" + assetName + "')");
        return row.count() > 0;
    }

    // Check if table or empty message is visible
    public void assertTableOrEmptyMessageVisible() {
        // Table should always be visible
        assertThat(page.locator("table")).isVisible();
    }

    // Print button
    public void assertPrintButtonVisible() {
        assertThat(page.locator("a:has-text('Cetak')")).isVisible();
    }

    public void clickPrint() {
        page.locator("a:has-text('Cetak')").click();
    }
}
