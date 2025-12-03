package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryPurchaseFormPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "#page-title";
    private static final String PRODUCT_SELECT = "#productId";
    private static final String DATE_INPUT = "#transactionDate";
    private static final String QUANTITY_INPUT = "#quantity";
    private static final String UNIT_COST_INPUT = "#unitCost";
    private static final String REFERENCE_INPUT = "#referenceNumber";
    private static final String NOTES_INPUT = "#notes";
    private static final String SUBMIT_BUTTON = "#btn-submit";

    public InventoryPurchaseFormPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public InventoryPurchaseFormPage navigate() {
        page.navigate(baseUrl + "/inventory/purchase");
        page.waitForLoadState();
        return this;
    }

    public void fillDate(String date) {
        page.fill(DATE_INPUT, date);
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void selectProduct(String productCode) {
        page.selectOption(PRODUCT_SELECT, new com.microsoft.playwright.options.SelectOption().setLabel(productCode));
    }

    public void selectProductByValue(String productId) {
        page.selectOption(PRODUCT_SELECT, productId);
    }

    public void fillQuantity(String quantity) {
        page.fill(QUANTITY_INPUT, quantity);
    }

    public void fillUnitCost(String unitCost) {
        page.fill(UNIT_COST_INPUT, unitCost);
    }

    public void fillReference(String reference) {
        page.fill(REFERENCE_INPUT, reference);
    }

    public void fillNotes(String notes) {
        page.fill(NOTES_INPUT, notes);
    }

    public void clickSubmit() {
        page.click(SUBMIT_BUTTON);
        page.waitForLoadState();
    }
}
