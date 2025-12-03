package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryStockListPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "#page-title";
    private static final String STOCK_TABLE = "#stock-table";
    private static final String SEARCH_INPUT = "#search-input";
    private static final String TOTAL_VALUE = ".text-primary-600.text-2xl";
    private static final String LOW_STOCK_ALERT = ".bg-red-50";

    public InventoryStockListPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public InventoryStockListPage navigate() {
        page.navigate(baseUrl + "/inventory/stock");
        page.waitForLoadState();
        return this;
    }

    public void assertPageTitleVisible() {
        assertThat(page.locator(PAGE_TITLE).isVisible()).isTrue();
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void assertTableVisible() {
        assertThat(page.locator(STOCK_TABLE).isVisible()).isTrue();
    }

    public void search(String query) {
        page.fill(SEARCH_INPUT, query);
        page.waitForTimeout(500); // Wait for HTMX
    }

    public boolean hasProductWithCode(String code) {
        return page.locator("a:has-text('" + code + "')").first().isVisible();
    }

    public void clickProductCode(String code) {
        page.click("a:has-text('" + code + "')");
        page.waitForLoadState();
    }

    public boolean hasLowStockAlert() {
        return page.locator(LOW_STOCK_ALERT).isVisible();
    }

    public String getTotalInventoryValue() {
        return page.locator(TOTAL_VALUE).first().textContent();
    }
}
