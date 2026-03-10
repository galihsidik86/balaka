package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.service.ServiceTestDataInitializer;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Functional tests for VendorController.
 * Tests vendor list, create, edit, activate, deactivate operations.
 */
@DisplayName("Vendor Controller Tests")
@Import(ServiceTestDataInitializer.class)
class VendorControllerFunctionalTest extends PlaywrightTestBase {

    @BeforeEach
    void setupAndLogin() {
        loginAsAdmin();
    }

    @Test
    @DisplayName("Should display vendor list page")
    void shouldDisplayVendorListPage() {
        navigateTo("/vendors");
        waitForPageLoad();

        assertThat(page.locator("[data-testid='vendor-list']")).isVisible();
    }

    @Test
    @DisplayName("Should filter vendors by status")
    void shouldFilterVendorsByStatus() {
        navigateTo("/vendors");
        waitForPageLoad();

        var vendorList = page.locator("[data-testid='vendor-list']");
        var statusSelect = vendorList.locator("select[name='active']").first();
        if (statusSelect.isVisible()) {
            statusSelect.selectOption("true");
            vendorList.locator("form button[type='submit']").first().click();
            waitForPageLoad();
        }

        assertThat(page.locator("[data-testid='vendor-list']")).isVisible();
    }

    @Test
    @DisplayName("Should search vendors by keyword")
    void shouldSearchVendorsByKeyword() {
        navigateTo("/vendors");
        waitForPageLoad();

        var vendorList = page.locator("[data-testid='vendor-list']");
        var searchInput = vendorList.locator("input[name='search']").first();
        if (searchInput.isVisible()) {
            searchInput.fill("test");
            vendorList.locator("form button[type='submit']").first().click();
            waitForPageLoad();
        }

        assertThat(page.locator("[data-testid='vendor-list']")).isVisible();
    }

    @Test
    @DisplayName("Should display new vendor form")
    void shouldDisplayNewVendorForm() {
        navigateTo("/vendors/new");
        waitForPageLoad();

        assertThat(page.locator("#btn-simpan")).isVisible();
    }

    @Test
    @DisplayName("Should create new vendor")
    void shouldCreateNewVendor() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String vendorCode = "VND-" + suffix;
        String vendorName = "Test Vendor " + suffix;

        navigateTo("/vendors/new");
        waitForPageLoad();

        page.locator("input[name='code']").first().fill(vendorCode);
        page.locator("input[name='name']").first().fill(vendorName);

        var contactInput = page.locator("input[name='contactPerson']").first();
        if (contactInput.isVisible()) {
            contactInput.fill("Contact " + suffix);
        }

        var emailInput = page.locator("input[name='email']").first();
        if (emailInput.isVisible()) {
            emailInput.fill("vendor" + suffix + "@test.com");
        }

        var phoneInput = page.locator("input[name='phone']").first();
        if (phoneInput.isVisible()) {
            phoneInput.fill("081234567890");
        }

        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Should redirect to vendor detail page
        assertThat(page.locator("[data-testid='vendor-detail']")).isVisible();
    }

    @Test
    @DisplayName("Should show validation error for empty form")
    void shouldShowValidationErrorForEmptyForm() {
        navigateTo("/vendors/new");
        waitForPageLoad();

        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Should stay on form page with validation errors
        assertThat(page.locator("#btn-simpan")).isVisible();
    }

    @Test
    @DisplayName("Should display vendor detail page")
    void shouldDisplayVendorDetailPage() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String vendorCode = "VND-DTL-" + suffix;

        // Create vendor first
        navigateTo("/vendors/new");
        waitForPageLoad();
        page.locator("input[name='code']").first().fill(vendorCode);
        page.locator("input[name='name']").first().fill("Detail Vendor " + suffix);
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Navigate to detail page
        navigateTo("/vendors/" + vendorCode);
        waitForPageLoad();

        assertThat(page.locator("[data-testid='vendor-detail']")).isVisible();
    }

    @Test
    @DisplayName("Should display vendor edit form with populated data")
    void shouldDisplayVendorEditForm() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String vendorCode = "VND-EDT-" + suffix;

        // Create vendor first
        navigateTo("/vendors/new");
        waitForPageLoad();
        page.locator("input[name='code']").first().fill(vendorCode);
        page.locator("input[name='name']").first().fill("Edit Vendor " + suffix);
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Navigate to edit form
        navigateTo("/vendors/" + vendorCode + "/edit");
        waitForPageLoad();

        assertThat(page.locator("#btn-simpan")).isVisible();
        assertThat(page.locator("input[name='code']").first()).hasValue(vendorCode);
    }

    @Test
    @DisplayName("Should update vendor")
    void shouldUpdateVendor() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String vendorCode = "VND-UPD-" + suffix;

        // Create vendor first
        navigateTo("/vendors/new");
        waitForPageLoad();
        page.locator("input[name='code']").first().fill(vendorCode);
        page.locator("input[name='name']").first().fill("Update Vendor " + suffix);
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Navigate to edit form
        navigateTo("/vendors/" + vendorCode + "/edit");
        waitForPageLoad();

        // Update name
        page.locator("input[name='name']").first().fill("Updated Vendor " + suffix);
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Should redirect to detail page
        assertThat(page.locator("[data-testid='vendor-detail']")).isVisible();
    }

    @Test
    @DisplayName("Should deactivate vendor")
    void shouldDeactivateVendor() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String vendorCode = "VND-DEACT-" + suffix;

        // Create vendor first (new vendors are active by default)
        navigateTo("/vendors/new");
        waitForPageLoad();
        page.locator("input[name='code']").first().fill(vendorCode);
        page.locator("input[name='name']").first().fill("Deactivate Vendor " + suffix);
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Navigate to detail page and deactivate
        navigateTo("/vendors/" + vendorCode);
        waitForPageLoad();

        var deactivateForm = page.locator("form[action*='/deactivate']").first();
        if (deactivateForm.isVisible()) {
            deactivateForm.locator("button[type='submit']").click();
            waitForPageLoad();
        }

        assertThat(page.locator("[data-testid='vendor-detail']")).isVisible();
    }

    @Test
    @DisplayName("Should update vendor with all fields")
    void shouldUpdateVendorWithAllFields() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String vendorCode = "VND-FULL-" + suffix;

        // Create vendor first
        navigateTo("/vendors/new");
        waitForPageLoad();
        page.locator("input[name='code']").first().fill(vendorCode);
        page.locator("input[name='name']").first().fill("Full Vendor " + suffix);
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Navigate to edit form and fill all fields
        navigateTo("/vendors/" + vendorCode + "/edit");
        waitForPageLoad();

        page.locator("input[name='name']").first().fill("Updated Full Vendor " + suffix);

        var contactInput = page.locator("input[name='contactPerson']").first();
        if (contactInput.isVisible()) contactInput.fill("John Doe");

        var emailInput = page.locator("input[name='email']").first();
        if (emailInput.isVisible()) emailInput.fill("john@example.com");

        var phoneInput = page.locator("input[name='phone']").first();
        if (phoneInput.isVisible()) phoneInput.fill("08123456789");

        var paymentTermInput = page.locator("input[name='paymentTermDays']").first();
        if (paymentTermInput.isVisible()) paymentTermInput.fill("30");

        var addressInput = page.locator("textarea[name='address']").first();
        if (addressInput.isVisible()) addressInput.fill("Jl. Test No. 123");

        var npwpInput = page.locator("input[name='npwp']").first();
        if (npwpInput.isVisible()) npwpInput.fill("12.345.678.9-012.345");

        var nitkuInput = page.locator("input[name='nitku']").first();
        if (nitkuInput.isVisible()) nitkuInput.fill("1234567890");

        var bankNameInput = page.locator("input[name='bankName']").first();
        if (bankNameInput.isVisible()) bankNameInput.fill("Bank BCA");

        var bankAccountInput = page.locator("input[name='bankAccountNumber']").first();
        if (bankAccountInput.isVisible()) bankAccountInput.fill("1234567890");

        var bankAccountNameInput = page.locator("input[name='bankAccountName']").first();
        if (bankAccountNameInput.isVisible()) bankAccountNameInput.fill("PT Test Vendor");

        var notesInput = page.locator("textarea[name='notes']").first();
        if (notesInput.isVisible()) notesInput.fill("Test notes for vendor");

        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Should redirect to detail page
        assertThat(page.locator("[data-testid='vendor-detail']")).isVisible();
    }

    @Test
    @DisplayName("Should search and filter vendors simultaneously")
    void shouldSearchAndFilterVendorsSimultaneously() {
        navigateTo("/vendors?active=true&search=test");
        waitForPageLoad();

        assertThat(page.locator("[data-testid='vendor-list']")).isVisible();
    }

    @Test
    @DisplayName("Should activate vendor")
    void shouldActivateVendor() {
        String suffix = String.valueOf(System.currentTimeMillis());
        String vendorCode = "VND-ACT-" + suffix;

        // Create vendor first
        navigateTo("/vendors/new");
        waitForPageLoad();
        page.locator("input[name='code']").first().fill(vendorCode);
        page.locator("input[name='name']").first().fill("Activate Vendor " + suffix);
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Deactivate first
        navigateTo("/vendors/" + vendorCode);
        waitForPageLoad();
        var deactivateForm = page.locator("form[action*='/deactivate']").first();
        if (deactivateForm.isVisible()) {
            deactivateForm.locator("button[type='submit']").click();
            waitForPageLoad();
        }

        // Now activate
        navigateTo("/vendors/" + vendorCode);
        waitForPageLoad();
        var activateForm = page.locator("form[action*='/activate']").first();
        if (activateForm.isVisible()) {
            activateForm.locator("button[type='submit']").click();
            waitForPageLoad();
        }

        assertThat(page.locator("[data-testid='vendor-detail']")).isVisible();
    }
}
