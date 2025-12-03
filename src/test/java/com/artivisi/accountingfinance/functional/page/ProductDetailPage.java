package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductDetailPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "#page-title";
    private static final String PRODUCT_NAME = "h2.text-2xl";
    private static final String PRODUCT_CODE = "h2.text-2xl + p";
    private static final String STATUS_BADGE = "span.rounded-full";
    private static final String EDIT_BUTTON = "a:has-text('Edit')";
    private static final String ACTIVATE_BUTTON = "button:has-text('Aktifkan')";
    private static final String DEACTIVATE_BUTTON = "button:has-text('Nonaktifkan')";
    private static final String DELETE_BUTTON = "button:has-text('Hapus')";

    public ProductDetailPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void assertProductNameText(String expected) {
        assertThat(page.locator(PRODUCT_NAME).textContent()).contains(expected);
    }

    public void assertProductCodeText(String expected) {
        assertThat(page.locator(PRODUCT_CODE).textContent()).contains(expected);
    }

    public void assertStatusText(String expected) {
        assertThat(page.locator(STATUS_BADGE).first().textContent()).contains(expected);
    }

    public boolean hasActivateButton() {
        return page.locator(ACTIVATE_BUTTON).isVisible();
    }

    public boolean hasDeactivateButton() {
        return page.locator(DEACTIVATE_BUTTON).isVisible();
    }

    public void clickActivateButton() {
        page.click(ACTIVATE_BUTTON);
        page.waitForLoadState();
    }

    public void clickDeactivateButton() {
        page.onDialog(dialog -> dialog.accept());
        page.click(DEACTIVATE_BUTTON);
        page.waitForLoadState();
    }

    public void clickEditButton() {
        page.click(EDIT_BUTTON);
        page.waitForLoadState();
    }

    public void clickDeleteButton() {
        page.onDialog(dialog -> dialog.accept());
        page.click(DELETE_BUTTON);
        page.waitForLoadState();
    }
}
