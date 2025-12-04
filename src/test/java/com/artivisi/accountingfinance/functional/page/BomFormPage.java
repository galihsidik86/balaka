package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class BomFormPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "h1";
    private static final String CODE_INPUT = "#code";
    private static final String NAME_INPUT = "#name";
    private static final String PRODUCT_SELECT = "#productId";
    private static final String OUTPUT_QUANTITY_INPUT = "#outputQuantity";
    private static final String DESCRIPTION_INPUT = "#description";
    private static final String ACTIVE_CHECKBOX = "#active";
    private static final String ADD_COMPONENT_BUTTON = "button:has-text('Tambah Komponen')";
    private static final String SUBMIT_BUTTON = "button[type='submit']:has-text('Simpan')";

    public BomFormPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public BomFormPage navigate() {
        page.navigate(baseUrl + "/inventory/bom/create");
        page.waitForLoadState();
        return this;
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void fillCode(String code) {
        page.fill(CODE_INPUT, code);
    }

    public void fillName(String name) {
        page.fill(NAME_INPUT, name);
    }

    public void selectProductByValue(String productId) {
        page.selectOption(PRODUCT_SELECT, productId);
    }

    public void fillOutputQuantity(String quantity) {
        page.fill(OUTPUT_QUANTITY_INPUT, quantity);
    }

    public void fillDescription(String description) {
        page.fill(DESCRIPTION_INPUT, description);
    }

    public void setActive(boolean active) {
        boolean isChecked = page.isChecked(ACTIVE_CHECKBOX);
        if (active != isChecked) {
            page.click(ACTIVE_CHECKBOX);
        }
    }

    public void clickAddComponent() {
        page.click(ADD_COMPONENT_BUTTON);
    }

    public void selectComponent(int lineIndex, String productId) {
        page.selectOption("select[name='componentId[]']:nth-of-type(" + (lineIndex + 1) + ")", productId);
    }

    public void fillComponentQuantity(int lineIndex, String quantity) {
        page.locator("input[name='componentQty[]']").nth(lineIndex).fill(quantity);
    }

    public void fillComponentNotes(int lineIndex, String notes) {
        page.locator("input[name='componentNotes[]']").nth(lineIndex).fill(notes);
    }

    public void addComponentLine(String productId, String quantity, String notes) {
        clickAddComponent();
        // Wait for row to be added by JavaScript (select visible rows only, not the hidden template)
        page.waitForSelector(".component-row select[name='componentId[]']");
        int lineIndex = (int) page.locator(".component-row select[name='componentId[]']").count() - 1;
        page.locator(".component-row select[name='componentId[]']").nth(lineIndex).selectOption(productId);
        page.locator(".component-row input[name='componentQty[]']").nth(lineIndex).fill(quantity);
        if (notes != null) {
            page.locator(".component-row input[name='componentNotes[]']").nth(lineIndex).fill(notes);
        }
    }

    public void clickSubmit() {
        page.click(SUBMIT_BUTTON);
        page.waitForLoadState();
    }

    public boolean hasValidationError() {
        return page.locator(".bg-red-100").count() > 0;
    }
}
