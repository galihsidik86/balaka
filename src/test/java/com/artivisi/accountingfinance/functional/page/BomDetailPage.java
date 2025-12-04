package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class BomDetailPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "h1";
    private static final String EDIT_BUTTON = "a:has-text('Edit')";
    private static final String DEACTIVATE_BUTTON = "button:has-text('Nonaktifkan')";

    public BomDetailPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void assertBomCodeVisible(String code) {
        assertThat(page.locator("dd:has-text('" + code + "')").count()).isGreaterThan(0);
    }

    public void assertProductVisible(String productName) {
        assertThat(page.locator("dd:has-text('" + productName + "')").count()).isGreaterThan(0);
    }

    public void assertComponentVisible(String componentCode) {
        assertThat(page.locator("td:has-text('" + componentCode + "')").count()).isGreaterThan(0);
    }

    public void clickEdit() {
        page.click(EDIT_BUTTON);
        page.waitForLoadState();
    }

    public void clickDeactivate() {
        page.click(DEACTIVATE_BUTTON);
        page.waitForLoadState();
    }

    public boolean hasSuccessMessage() {
        return page.locator(".bg-green-100").count() > 0;
    }

    public boolean isActive() {
        return page.locator("span:has-text('Aktif')").count() > 0;
    }
}
