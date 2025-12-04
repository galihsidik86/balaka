package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductionOrderFormPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "h1";
    private static final String BOM_SELECT = "#bomId";
    private static final String QUANTITY_INPUT = "#quantity";
    private static final String ORDER_DATE_INPUT = "#orderDate";
    private static final String PLANNED_COMPLETION_DATE_INPUT = "#plannedCompletionDate";
    private static final String NOTES_INPUT = "#notes";
    private static final String SUBMIT_BUTTON = "button[type='submit']:has-text('Simpan')";

    public ProductionOrderFormPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public ProductionOrderFormPage navigate() {
        page.navigate(baseUrl + "/inventory/production/create");
        page.waitForLoadState();
        return this;
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void selectBomByValue(String bomId) {
        page.selectOption(BOM_SELECT, bomId);
    }

    public void fillQuantity(String quantity) {
        page.fill(QUANTITY_INPUT, quantity);
    }

    public void fillOrderDate(String date) {
        page.fill(ORDER_DATE_INPUT, date);
    }

    public void fillPlannedCompletionDate(String date) {
        page.fill(PLANNED_COMPLETION_DATE_INPUT, date);
    }

    public void fillNotes(String notes) {
        page.fill(NOTES_INPUT, notes);
    }

    public void clickSubmit() {
        page.click(SUBMIT_BUTTON);
        page.waitForLoadState();
    }

    public boolean hasValidationError() {
        return page.locator(".bg-red-100").count() > 0;
    }
}
