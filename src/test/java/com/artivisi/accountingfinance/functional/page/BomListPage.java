package com.artivisi.accountingfinance.functional.page;

import com.microsoft.playwright.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class BomListPage {
    private final Page page;
    private final String baseUrl;

    private static final String PAGE_TITLE = "h1";
    private static final String SEARCH_INPUT = "input[name='search']";
    private static final String ADD_BUTTON = "a:has-text('Tambah BOM')";
    private static final String TABLE = "table";

    public BomListPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public BomListPage navigate() {
        page.navigate(baseUrl + "/inventory/bom");
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
        assertThat(page.locator(TABLE).isVisible()).isTrue();
    }

    public void search(String query) {
        page.fill(SEARCH_INPUT, query);
        page.locator(SEARCH_INPUT).press("Enter");
        page.waitForLoadState();
    }

    public void clickAddBom() {
        page.click(ADD_BUTTON);
        page.waitForLoadState();
    }

    public boolean hasBomWithCode(String code) {
        return page.locator("td:has-text('" + code + "')").count() > 0;
    }

    public void clickDetailLink(String code) {
        page.locator("tr:has-text('" + code + "') a:has-text('Detail')").click();
        page.waitForLoadState();
    }

    public void clickEditLink(String code) {
        page.locator("tr:has-text('" + code + "') a:has-text('Edit')").click();
        page.waitForLoadState();
    }

    public boolean hasSuccessMessage() {
        return page.locator(".bg-green-100").count() > 0;
    }
}
