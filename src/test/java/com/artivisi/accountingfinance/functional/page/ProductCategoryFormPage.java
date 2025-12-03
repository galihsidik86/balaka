package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductCategoryFormPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "#page-title";
    private static final String CODE_INPUT = "#code";
    private static final String NAME_INPUT = "#name";
    private static final String DESCRIPTION_INPUT = "#description";
    private static final String PARENT_SELECT = "#parent";
    private static final String ACTIVE_CHECKBOX = "#active";
    private static final String SUBMIT_BUTTON = "#btn-save";

    public ProductCategoryFormPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public ProductCategoryFormPage navigateToNew() {
        page.navigate(baseUrl + "/products/categories/new");
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

    public void fillDescription(String description) {
        page.fill(DESCRIPTION_INPUT, description);
    }

    public void selectParent(String parentId) {
        page.selectOption(PARENT_SELECT, parentId);
    }

    public void setActive(boolean active) {
        if (active && !page.isChecked(ACTIVE_CHECKBOX)) {
            page.click(ACTIVE_CHECKBOX);
        } else if (!active && page.isChecked(ACTIVE_CHECKBOX)) {
            page.click(ACTIVE_CHECKBOX);
        }
    }

    public void clickSubmit() {
        page.click(SUBMIT_BUTTON);
        page.waitForLoadState();
    }

    public boolean hasValidationError() {
        return page.locator(".text-red-500, .border-red-500").count() > 0;
    }
}
