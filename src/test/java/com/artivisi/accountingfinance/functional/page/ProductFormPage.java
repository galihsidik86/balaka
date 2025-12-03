package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductFormPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "#page-title";
    private static final String CODE_INPUT = "#code";
    private static final String NAME_INPUT = "#name";
    private static final String UNIT_INPUT = "#unit";
    private static final String DESCRIPTION_INPUT = "#description";
    private static final String CATEGORY_SELECT = "#category";
    private static final String COSTING_METHOD_SELECT = "#costingMethod";
    private static final String TRACK_INVENTORY_CHECKBOX = "#trackInventory";
    private static final String MINIMUM_STOCK_INPUT = "#minimumStock";
    private static final String SELLING_PRICE_INPUT = "#sellingPrice";
    private static final String INVENTORY_ACCOUNT_SELECT = "#inventoryAccount";
    private static final String COGS_ACCOUNT_SELECT = "#cogsAccount";
    private static final String SALES_ACCOUNT_SELECT = "#salesAccount";
    private static final String ACTIVE_CHECKBOX = "#active";
    private static final String SUBMIT_BUTTON = "#btn-save";

    public ProductFormPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public ProductFormPage navigateToNew() {
        page.navigate(baseUrl + "/products/new");
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

    public void fillUnit(String unit) {
        page.fill(UNIT_INPUT, unit);
    }

    public void fillDescription(String description) {
        page.fill(DESCRIPTION_INPUT, description);
    }

    public void selectCategory(String categoryId) {
        page.selectOption(CATEGORY_SELECT, categoryId);
    }

    public void selectCostingMethod(String method) {
        page.selectOption(COSTING_METHOD_SELECT, method);
    }

    public void setTrackInventory(boolean track) {
        if (track && !page.isChecked(TRACK_INVENTORY_CHECKBOX)) {
            page.click(TRACK_INVENTORY_CHECKBOX);
        } else if (!track && page.isChecked(TRACK_INVENTORY_CHECKBOX)) {
            page.click(TRACK_INVENTORY_CHECKBOX);
        }
    }

    public void fillMinimumStock(String stock) {
        page.fill(MINIMUM_STOCK_INPUT, stock);
    }

    public void fillSellingPrice(String price) {
        page.fill(SELLING_PRICE_INPUT, price);
    }

    public void selectInventoryAccount(String accountId) {
        page.selectOption(INVENTORY_ACCOUNT_SELECT, accountId);
    }

    public void selectCogsAccount(String accountId) {
        page.selectOption(COGS_ACCOUNT_SELECT, accountId);
    }

    public void selectSalesAccount(String accountId) {
        page.selectOption(SALES_ACCOUNT_SELECT, accountId);
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
