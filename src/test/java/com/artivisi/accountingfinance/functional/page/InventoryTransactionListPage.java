package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryTransactionListPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "#page-title";
    private static final String TRANSACTION_TABLE = "#transaction-table";
    private static final String NEW_TRANSACTION_DROPDOWN = "button:has-text('Transaksi Baru')";
    private static final String PURCHASE_LINK = "a:has-text('Pembelian')";
    private static final String SALE_LINK = "a:has-text('Penjualan')";
    private static final String ADJUSTMENT_LINK = "a:has-text('Penyesuaian')";

    public InventoryTransactionListPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public InventoryTransactionListPage navigate() {
        page.navigate(baseUrl + "/inventory/transactions");
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
        assertThat(page.locator(TRANSACTION_TABLE).isVisible()).isTrue();
    }

    public void clickNewTransactionDropdown() {
        page.click(NEW_TRANSACTION_DROPDOWN);
        page.waitForTimeout(200);
    }

    public void clickPurchaseLink() {
        page.click(PURCHASE_LINK);
        page.waitForLoadState();
    }

    public void clickSaleLink() {
        page.click(SALE_LINK);
        page.waitForLoadState();
    }

    public void clickAdjustmentLink() {
        page.click(ADJUSTMENT_LINK);
        page.waitForLoadState();
    }

    public boolean hasTransactionWithType(String type) {
        return page.locator("span:has-text('" + type + "')").first().isVisible();
    }

    public void clickViewTransaction(int index) {
        page.locator(TRANSACTION_TABLE + " tbody tr").nth(index).locator("a").last().click();
        page.waitForLoadState();
    }
}
