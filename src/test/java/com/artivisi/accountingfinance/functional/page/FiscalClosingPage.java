package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class FiscalClosingPage {
    private final Page page;
    private final String baseUrl;

    public FiscalClosingPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public void navigate() {
        page.navigate(baseUrl + "/reports/fiscal-closing");
    }

    public void navigate(int year) {
        page.navigate(baseUrl + "/reports/fiscal-closing?year=" + year);
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
        assertThat(page.locator("#card-revenue")).isVisible();
        assertThat(page.locator("#card-expense")).isVisible();
    }

    // Status badge
    public void assertStatusBadgeVisible() {
        assertThat(page.locator("#status-section")).isVisible();
    }

    // Preview section
    public void assertPreviewSectionVisible() {
        assertThat(page.locator("#preview-section")).isVisible();
    }

    // Explanation
    public void assertExplanationVisible() {
        assertThat(page.locator("#explanation-section")).isVisible();
    }

    // Execute button
    public void assertExecuteButtonVisible() {
        assertThat(page.locator("#btn-execute")).isVisible();
    }

    public void clickExecuteClosing() {
        page.locator("#btn-execute").click();
    }

    // Reverse button
    public void assertReverseButtonVisible() {
        assertThat(page.locator("#btn-reverse")).isVisible();
    }

    public void clickReverseClosing() {
        page.locator("#btn-reverse").click();
    }

    // Modal
    public void fillReverseReason(String reason) {
        page.locator("#reason").fill(reason);
    }

    public void confirmReverse() {
        page.locator("#btn-confirm-reverse").click();
    }

    // Success/Error messages
    public boolean hasSuccessMessage() {
        return page.locator("#alert-success").count() > 0;
    }

    public boolean hasErrorMessage() {
        return page.locator("#alert-error").count() > 0;
    }

    public String getSuccessMessage() {
        return page.locator("#success-message").textContent();
    }

    public String getErrorMessage() {
        return page.locator("#error-message").textContent();
    }
}
