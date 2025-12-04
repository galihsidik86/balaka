package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductionOrderDetailPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "h1";
    private static final String EDIT_BUTTON = "a:has-text('Edit')";
    private static final String START_BUTTON = "button:has-text('Mulai Produksi')";
    private static final String COMPLETE_BUTTON = "button:has-text('Selesaikan Produksi')";
    private static final String CANCEL_BUTTON = "button:has-text('Batalkan')";
    private static final String DELETE_BUTTON = "button:has-text('Hapus')";

    public ProductionOrderDetailPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void assertOrderNumberVisible(String orderNumber) {
        assertThat(page.locator("dd:has-text('" + orderNumber + "')").count()).isGreaterThan(0);
    }

    public void assertBomVisible(String bomCode) {
        assertThat(page.locator("dd:has-text('" + bomCode + "')").count()).isGreaterThan(0);
    }

    public void assertStatusText(String status) {
        assertThat(page.locator("span:has-text('" + status + "')").count()).isGreaterThan(0);
    }

    public void clickEdit() {
        page.click(EDIT_BUTTON);
        page.waitForLoadState();
    }

    public void clickStartProduction() {
        page.onceDialog(dialog -> dialog.accept());
        page.click(START_BUTTON);
        page.waitForLoadState();
    }

    public void clickCompleteProduction() {
        page.onceDialog(dialog -> dialog.accept());
        page.click(COMPLETE_BUTTON);
        page.waitForLoadState();
    }

    public void clickCancel() {
        page.onceDialog(dialog -> dialog.accept());
        page.click(CANCEL_BUTTON);
        page.waitForLoadState();
    }

    public void clickDelete() {
        page.onceDialog(dialog -> dialog.accept());
        page.click(DELETE_BUTTON);
        page.waitForLoadState();
    }

    public boolean hasSuccessMessage() {
        return page.locator(".bg-green-100").count() > 0;
    }

    public boolean hasErrorMessage() {
        return page.locator(".bg-red-100").count() > 0;
    }

    public boolean hasCostSummary() {
        return page.locator("dt:has-text('Total Biaya Komponen')").count() > 0;
    }

    public String getTotalCost() {
        return page.locator("dd:has-text('Rp')").first().textContent();
    }
}
