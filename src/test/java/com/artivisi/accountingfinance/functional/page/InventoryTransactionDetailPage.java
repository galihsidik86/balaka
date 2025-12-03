package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryTransactionDetailPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "#page-title";
    private static final String SUCCESS_MESSAGE = ".bg-green-50";
    private static final String TRANSACTION_TYPE = "span.rounded-full";
    private static final String PRODUCT_LINK = "a.text-primary-600";
    private static final String QUANTITY = ".text-green-600, .text-red-600";

    public InventoryTransactionDetailPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void assertSuccessMessage(String expected) {
        assertThat(page.locator(SUCCESS_MESSAGE).textContent()).contains(expected);
    }

    public void assertTransactionTypeText(String expected) {
        assertThat(page.locator(TRANSACTION_TYPE).first().textContent()).contains(expected);
    }

    public void assertProductVisible(String productCode) {
        assertThat(page.locator(PRODUCT_LINK).first().textContent()).contains(productCode);
    }

    public void assertQuantityVisible() {
        assertThat(page.locator(QUANTITY).first().isVisible()).isTrue();
    }

    public boolean hasSuccessMessage() {
        return page.locator(SUCCESS_MESSAGE).isVisible();
    }
}
