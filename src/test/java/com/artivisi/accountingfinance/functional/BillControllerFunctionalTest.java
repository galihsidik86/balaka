package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.service.ServiceTestDataInitializer;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.microsoft.playwright.Locator;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Functional tests for BillController.
 * Tests bill list, create, detail operations.
 */
@DisplayName("Bill Controller Tests")
@Import(ServiceTestDataInitializer.class)
class BillControllerFunctionalTest extends PlaywrightTestBase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    @BeforeEach
    void setupAndLogin() {
        loginAsAdmin();
    }

    /**
     * Creates a vendor and returns its name (used to match dropdown options).
     */
    private String createVendor() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String vendorCode = "BILL-VND-" + suffix;
        String vendorName = "Bill Test Vendor " + suffix;

        navigateTo("/vendors/new");
        waitForPageLoad();
        page.locator("input[name='code']").first().fill(vendorCode);
        page.locator("input[name='name']").first().fill(vendorName);
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        return vendorName;
    }

    /**
     * Selects a vendor from the dropdown by matching option text.
     */
    private void selectVendor(String vendorName) {
        var vendorSelect = page.locator("select#vendor");
        var vendorOptions = vendorSelect.locator("option");
        int optionCount = vendorOptions.count();
        for (int i = 0; i < optionCount; i++) {
            String text = vendorOptions.nth(i).textContent();
            if (text.contains(vendorName)) {
                vendorSelect.selectOption(vendorOptions.nth(i).getAttribute("value"));
                return;
            }
        }
    }

    /**
     * Waits for Alpine.js to render line item inputs inside x-for template.
     */
    private Locator waitForLineItem(String inputName) {
        var locator = page.locator("input[name='" + inputName + "']").first();
        locator.waitFor(new Locator.WaitForOptions().setTimeout(15000));
        return locator;
    }

    @Test
    @DisplayName("Should display bill list page")
    void shouldDisplayBillListPage() {
        navigateTo("/bills");
        waitForPageLoad();

        assertThat(page.locator("[data-testid='bill-list']")).isVisible();
    }

    @Test
    @DisplayName("Should display new bill form")
    void shouldDisplayNewBillForm() {
        navigateTo("/bills/new");
        waitForPageLoad();

        assertThat(page.locator("#btn-simpan")).isVisible();
        assertThat(page.locator("select#vendor")).isVisible();
        assertThat(page.locator("#billDate")).isVisible();
        assertThat(page.locator("#dueDate")).isVisible();
    }

    @Test
    @DisplayName("Should create bill with line items")
    void shouldCreateBillWithLineItems() {
        String vendorName = createVendor();

        navigateTo("/bills/new");
        waitForPageLoad();

        selectVendor(vendorName);

        // Fill dates
        String billDate = LocalDate.now().format(DATE_FORMAT);
        String dueDate = LocalDate.now().plusDays(30).format(DATE_FORMAT);
        page.locator("#billDate").fill(billDate);
        page.locator("#dueDate").fill(dueDate);

        // Fill first line item (Alpine.js auto-adds first line via x-for)
        waitForLineItem("lineDescription").fill("Test line item");
        page.locator("input[name='lineQuantity']").first().fill("2");
        page.locator("input[name='lineUnitPrice']").first().fill("100000");

        // Submit
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Should redirect to bill detail page
        assertThat(page.locator("[data-testid='bill-detail']")).isVisible();
    }

    @Test
    @DisplayName("Should show validation error for empty bill form")
    void shouldShowValidationErrorForEmptyBillForm() {
        navigateTo("/bills/new");
        waitForPageLoad();

        // Submit without filling required fields
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Should stay on form page with validation errors
        assertThat(page.locator("#btn-simpan")).isVisible();
    }

    @Test
    @DisplayName("Should display bill detail page")
    void shouldDisplayBillDetailPage() {
        String vendorName = createVendor();

        navigateTo("/bills/new");
        waitForPageLoad();

        selectVendor(vendorName);

        // Fill dates
        String billDate = LocalDate.now().format(DATE_FORMAT);
        String dueDate = LocalDate.now().plusDays(30).format(DATE_FORMAT);
        page.locator("#billDate").fill(billDate);
        page.locator("#dueDate").fill(dueDate);

        // Fill line item (Alpine.js auto-adds first line via x-for)
        waitForLineItem("lineDescription").fill("Detail test item");
        page.locator("input[name='lineQuantity']").first().fill("1");
        page.locator("input[name='lineUnitPrice']").first().fill("500000");

        // Submit
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Verify detail page shows bill info
        assertThat(page.locator("[data-testid='bill-detail']")).isVisible();
    }
}
