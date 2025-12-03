package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductCategoryListPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "#page-title";
    private static final String CATEGORY_TABLE = "#category-table";
    private static final String NEW_CATEGORY_BUTTON = "#btn-new-category";
    private static final String SEARCH_INPUT = "#search-input";

    public ProductCategoryListPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public ProductCategoryListPage navigate() {
        page.navigate(baseUrl + "/products/categories");
        return this;
    }

    public void assertPageTitleVisible() {
        assertThat(page.locator(PAGE_TITLE).isVisible()).isTrue();
    }

    public void assertPageTitleText(String expected) {
        assertThat(page.locator(PAGE_TITLE).textContent()).contains(expected);
    }

    public void assertTableVisible() {
        assertThat(page.locator(CATEGORY_TABLE).isVisible()).isTrue();
    }

    public void clickNewCategoryButton() {
        page.click(NEW_CATEGORY_BUTTON);
        page.waitForLoadState();
    }

    public void search(String query) {
        page.fill(SEARCH_INPUT, query);
        page.waitForTimeout(500);
    }

    public int getCategoryCount() {
        return page.locator(CATEGORY_TABLE + " tbody tr").count();
    }

    public boolean hasCategoryWithCode(String code) {
        return page.locator(CATEGORY_TABLE + " tbody tr:has-text('" + code + "')").count() > 0;
    }

    public boolean hasCategoryWithName(String name) {
        return page.locator(CATEGORY_TABLE + " tbody tr:has-text('" + name + "')").count() > 0;
    }

    public void clickEditCategory(String code) {
        page.click(CATEGORY_TABLE + " tbody tr:has-text('" + code + "') a:has-text('Edit')");
        page.waitForLoadState();
    }
}
