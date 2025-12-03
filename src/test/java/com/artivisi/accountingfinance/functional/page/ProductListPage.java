package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductListPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "#page-title";
    private static final String PRODUCT_TABLE = "#product-table";
    private static final String NEW_PRODUCT_BUTTON = "#btn-new-product";
    private static final String SEARCH_INPUT = "#search-input";

    public ProductListPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public ProductListPage navigate() {
        page.navigate(baseUrl + "/products");
        return this;
    }

    public void assertPageTitleVisible() {
        assertThat(page.locator(PAGE_TITLE).isVisible()).isTrue();
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void assertTableVisible() {
        assertThat(page.locator(PRODUCT_TABLE).isVisible()).isTrue();
    }

    public void clickNewProductButton() {
        page.click(NEW_PRODUCT_BUTTON);
        page.waitForLoadState();
    }

    public void search(String query) {
        page.fill(SEARCH_INPUT, query);
        page.waitForTimeout(500);
    }

    public int getProductCount() {
        return page.locator(PRODUCT_TABLE + " tbody tr").count();
    }

    public boolean hasProductWithCode(String code) {
        return page.locator(PRODUCT_TABLE + " tbody tr:has-text('" + code + "')").count() > 0;
    }

    public boolean hasProductWithName(String name) {
        return page.locator(PRODUCT_TABLE + " tbody tr:has-text('" + name + "')").count() > 0;
    }

    public void clickProductCode(String code) {
        page.click(PRODUCT_TABLE + " tbody tr a:has-text('" + code + "')");
        page.waitForLoadState();
    }
}
