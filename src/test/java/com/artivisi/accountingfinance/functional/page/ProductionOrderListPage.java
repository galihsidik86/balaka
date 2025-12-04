package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductionOrderListPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "h1";
    private static final String STATUS_SELECT = "select[name='status']";
    private static final String FILTER_BUTTON = "button[type='submit']:has-text('Filter')";
    private static final String ADD_BUTTON = "a:has-text('Buat Order Produksi')";
    private static final String TABLE = "table";

    public ProductionOrderListPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public ProductionOrderListPage navigate() {
        page.navigate(baseUrl + "/inventory/production");
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
        assertThat(page.locator(TABLE).isVisible()).isTrue();
    }

    public void filterByStatus(String status) {
        page.selectOption(STATUS_SELECT, status);
        page.click(FILTER_BUTTON);
        page.waitForLoadState();
    }

    public void clickAddOrder() {
        page.click(ADD_BUTTON);
        page.waitForLoadState();
    }

    public boolean hasOrderWithNumber(String orderNumber) {
        return page.locator("td:has-text('" + orderNumber + "')").count() > 0;
    }

    public void clickDetailLink(String orderNumber) {
        page.locator("tr:has-text('" + orderNumber + "') a:has-text('Detail')").click();
        page.waitForLoadState();
    }

    public boolean hasSuccessMessage() {
        return page.locator(".bg-green-100").count() > 0;
    }
}
