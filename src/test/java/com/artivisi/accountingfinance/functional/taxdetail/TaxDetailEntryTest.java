package com.artivisi.accountingfinance.functional.taxdetail;

import com.artivisi.accountingfinance.entity.TaxTransactionDetail;
import com.artivisi.accountingfinance.entity.Transaction;
import com.artivisi.accountingfinance.repository.TaxTransactionDetailRepository;
import com.artivisi.accountingfinance.repository.TransactionRepository;
import com.artivisi.accountingfinance.enums.TransactionStatus;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@DisplayName("Tax Detail Entry Tests")
@Import(TaxDetailTestDataInitializer.class)
class TaxDetailEntryTest extends PlaywrightTestBase {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TaxTransactionDetailRepository taxTransactionDetailRepository;

    private Transaction ppnTransaction;
    private Transaction pph23Transaction;
    private Transaction nonTaxTransaction;

    @BeforeEach
    void setupAndLogin() {
        loginAsAdmin();

        // Find test transactions created by initializer
        List<Transaction> postedTransactions = transactionRepository
                .findByStatusOrderByTransactionDateDesc(TransactionStatus.POSTED);

        for (Transaction t : postedTransactions) {
            if (t.getDescription().contains("Test PPN Keluaran")) {
                ppnTransaction = t;
            } else if (t.getDescription().contains("Test PPh 23")) {
                pph23Transaction = t;
            } else if (t.getDescription().contains("Test Non-Tax")) {
                nonTaxTransaction = t;
            }
        }

        // Clean up tax details from previous tests to ensure test isolation
        cleanupTaxDetails(ppnTransaction.getId());
        cleanupTaxDetails(pph23Transaction.getId());
        cleanupTaxDetails(nonTaxTransaction.getId());
    }

    private void cleanupTaxDetails(UUID transactionId) {
        List<TaxTransactionDetail> details = taxTransactionDetailRepository
                .findAllByTransactionIdOrderByTaxTypeAsc(transactionId);
        taxTransactionDetailRepository.deleteAll(details);
    }

    @Test
    @DisplayName("Should show tax detail section on posted transaction")
    void shouldShowTaxDetailSectionOnPostedTransaction() {
        navigateTo("/transactions/" + ppnTransaction.getId());
        waitForPageLoad();

        // Tax detail section should be visible for POSTED transactions
        Locator taxSection = page.locator("[data-testid='tax-detail-section-card']");
        assertThat(taxSection).isVisible();

        // Wait for HTMX to load the section
        page.waitForTimeout(1000);

        // Should show empty state initially
        Locator emptyState = page.locator("[data-testid='tax-detail-empty']");
        assertThat(emptyState).isVisible();

        // Add button should be visible
        Locator addButton = page.locator("[data-testid='btn-add-tax-detail']");
        assertThat(addButton).isVisible();
    }

    @Test
    @DisplayName("Should add PPN Keluaran detail")
    void shouldAddPpnKeluaranDetail() {
        navigateTo("/transactions/" + ppnTransaction.getId());
        waitForPageLoad();
        page.waitForTimeout(1000);

        // Click add button
        page.locator("[data-testid='btn-add-tax-detail']").click();
        page.waitForTimeout(500);

        // Form should appear
        Locator form = page.locator("[data-testid='tax-detail-form']");
        assertThat(form).isVisible();

        // Select PPN Keluaran
        page.locator("[data-testid='tax-type-select']").selectOption("PPN_KELUARAN");
        page.waitForTimeout(300);

        // Fill e-Faktur fields
        page.locator("[data-testid='faktur-number']").fill("010.000-25.00000001");
        page.locator("[data-testid='faktur-date']").fill("2025-01-15");
        page.locator("[data-testid='transaction-code']").selectOption("01");

        // DPP and PPN should be pre-filled from suggestions
        Locator dppInput = page.locator("[data-testid='dpp']");
        // Fill if not pre-populated
        if (dppInput.inputValue().isEmpty()) {
            dppInput.fill("10000000");
        }
        Locator ppnInput = page.locator("[data-testid='ppn']");
        if (ppnInput.inputValue().isEmpty()) {
            ppnInput.fill("1100000");
        }

        // Fill counterparty
        Locator nameInput = page.locator("[data-testid='counterparty-name']");
        if (nameInput.inputValue().isEmpty()) {
            nameInput.fill("PT Client Test");
        }

        // Submit
        page.locator("[data-testid='btn-save-tax-detail']").click();
        page.waitForTimeout(1000);

        // Should show the card
        Locator detailList = page.locator("[data-testid='tax-detail-list']");
        assertThat(detailList).isVisible();

        // Verify PPN badge shows in the tax detail card
        assertThat(detailList.locator(".bg-green-100").first()).isVisible();
    }

    @Test
    @DisplayName("Should add PPh 23 detail")
    void shouldAddPph23Detail() {
        navigateTo("/transactions/" + pph23Transaction.getId());
        waitForPageLoad();
        page.waitForTimeout(1000);

        // Click add button
        page.locator("[data-testid='btn-add-tax-detail']").click();
        page.waitForTimeout(500);

        // Select PPh 23
        page.locator("[data-testid='tax-type-select']").selectOption("PPH_23");
        page.waitForTimeout(300);

        // Fill e-Bupot fields
        page.locator("[data-testid='bupot-number']").fill("BP-2025-00001");
        page.locator("[data-testid='tax-object-code']").fill("24-104-01");

        // Fill amounts
        Locator grossInput = page.locator("[data-testid='gross-amount']");
        if (grossInput.inputValue().isEmpty()) {
            grossInput.fill("5000000");
        }
        Locator rateInput = page.locator("[data-testid='tax-rate']");
        if (rateInput.inputValue().isEmpty()) {
            rateInput.fill("2.00");
        }
        Locator amountInput = page.locator("[data-testid='tax-amount']");
        if (amountInput.inputValue().isEmpty()) {
            amountInput.fill("100000");
        }

        // Fill counterparty
        Locator nameInput = page.locator("[data-testid='counterparty-name']");
        if (nameInput.inputValue().isEmpty()) {
            nameInput.fill("CV Konsultan Test");
        }

        // Submit
        page.locator("[data-testid='btn-save-tax-detail']").click();
        page.waitForTimeout(1000);

        // Should show the PPh badge in the tax detail card
        Locator detailCard = page.locator("[data-testid^='tax-detail-card-']").first();
        assertThat(detailCard).isVisible();
        assertThat(detailCard.locator(".bg-blue-100")).isVisible();
    }

    @Test
    @DisplayName("Should auto-populate counterparty from client")
    void shouldAutoPopulateFromClient() {
        navigateTo("/transactions/" + ppnTransaction.getId());
        waitForPageLoad();
        page.waitForTimeout(1000);

        // Click add button
        page.locator("[data-testid='btn-add-tax-detail']").click();
        page.waitForTimeout(500);

        // Select PPN type to show counterparty section
        page.locator("[data-testid='tax-type-select']").selectOption("PPN_KELUARAN");
        page.waitForTimeout(300);

        // Counterparty name should be pre-filled from project's client
        Locator nameInput = page.locator("[data-testid='counterparty-name']");

        // If project has client, name should be populated
        // (depends on test data - PRJ-2024-001 belongs to MANDIRI client)
        if (ppnTransaction.getProject() != null) {
            assertThat(nameInput).not().hasValue("");
        }

        // Cancel form
        page.locator("[data-testid='btn-cancel-tax-detail']").click();
        page.waitForTimeout(500);
    }

    @Test
    @DisplayName("Should auto-populate DPP and PPN from journal entries")
    void shouldAutoPopulateDppPpnFromJournalEntries() {
        navigateTo("/transactions/" + ppnTransaction.getId());
        waitForPageLoad();
        page.waitForTimeout(1000);

        // Click add button
        page.locator("[data-testid='btn-add-tax-detail']").click();
        page.waitForTimeout(500);

        // Select PPN type
        page.locator("[data-testid='tax-type-select']").selectOption("PPN_KELUARAN");
        page.waitForTimeout(300);

        // DPP and PPN inputs should have suggested values
        Locator dppInput = page.locator("[data-testid='dpp']");
        // DPP should be the transaction amount (10,000,000)
        assertThat(dppInput).not().hasValue("");

        Locator ppnInput = page.locator("[data-testid='ppn']");
        // PPN should be auto-calculated from journal entry
        assertThat(ppnInput).not().hasValue("");

        // Cancel
        page.locator("[data-testid='btn-cancel-tax-detail']").click();
    }

    @Test
    @DisplayName("Should validate faktur number uniqueness")
    void shouldValidateFakturNumberUniqueness() {
        navigateTo("/transactions/" + ppnTransaction.getId());
        waitForPageLoad();
        page.waitForTimeout(1000);

        // First: add a detail with a faktur number
        page.locator("[data-testid='btn-add-tax-detail']").click();
        page.waitForTimeout(500);

        page.locator("[data-testid='tax-type-select']").selectOption("PPN_KELUARAN");
        page.waitForTimeout(300);

        String uniqueFaktur = "010.000-25.99999999";
        page.locator("[data-testid='faktur-number']").fill(uniqueFaktur);
        page.locator("[data-testid='dpp']").fill("10000000");
        page.locator("[data-testid='ppn']").fill("1100000");
        page.locator("[data-testid='counterparty-name']").fill("PT Duplicate Test");

        page.locator("[data-testid='btn-save-tax-detail']").click();
        page.waitForTimeout(1000);

        // Now try to add another with same faktur number
        page.locator("[data-testid='btn-add-tax-detail']").click();
        page.waitForTimeout(500);

        page.locator("[data-testid='tax-type-select']").selectOption("PPN_KELUARAN");
        page.waitForTimeout(300);

        page.locator("[data-testid='faktur-number']").fill(uniqueFaktur);
        page.locator("[data-testid='dpp']").fill("5000000");
        page.locator("[data-testid='ppn']").fill("550000");
        page.locator("[data-testid='counterparty-name']").fill("PT Duplicate Test 2");

        page.locator("[data-testid='btn-save-tax-detail']").click();
        page.waitForTimeout(1000);

        // Should show error
        Locator error = page.locator("[data-testid='tax-detail-error']");
        assertThat(error).isVisible();
    }

    @Test
    @DisplayName("Should edit tax detail")
    void shouldEditTaxDetail() {
        // First, add a detail
        navigateTo("/transactions/" + pph23Transaction.getId());
        waitForPageLoad();
        page.waitForTimeout(1000);

        page.locator("[data-testid='btn-add-tax-detail']").click();
        page.waitForTimeout(500);

        page.locator("[data-testid='tax-type-select']").selectOption("PPH_23");
        page.waitForTimeout(300);

        page.locator("[data-testid='gross-amount']").fill("5000000");
        page.locator("[data-testid='tax-rate']").fill("2.00");
        page.locator("[data-testid='tax-amount']").fill("100000");
        page.locator("[data-testid='counterparty-name']").fill("PT Edit Test");

        page.locator("[data-testid='btn-save-tax-detail']").click();
        page.waitForTimeout(1500);

        // Verify initial save succeeded - card should be visible
        Locator initialCard = page.locator("[data-testid^='tax-detail-card-']").first();
        assertThat(initialCard).isVisible();

        // Click edit button on the card
        initialCard.locator("button[title='Edit']").click();
        page.waitForTimeout(1500);

        // Edit form should appear
        Locator editForm = page.locator("[data-testid='tax-detail-form']");
        assertThat(editForm).isVisible();

        // Change counterparty name
        Locator nameInput = page.locator("[data-testid='counterparty-name']");
        nameInput.clear();
        nameInput.fill("PT Edit Test Updated");

        page.locator("[data-testid='btn-save-tax-detail']").click();
        page.waitForTimeout(2000);

        // Reload the page to verify the edit was saved
        navigateTo("/transactions/" + pph23Transaction.getId());
        waitForPageLoad();
        page.waitForTimeout(1500);

        // Verify updated name shows in the detail card
        Locator updatedCard = page.locator("[data-testid^='tax-detail-card-']").first();
        assertThat(updatedCard).isVisible();
        assertThat(updatedCard.locator("text=PT Edit Test Updated")).isVisible();
    }

    @Test
    @DisplayName("Should delete tax detail")
    void shouldDeleteTaxDetail() {
        // First, add a detail
        navigateTo("/transactions/" + ppnTransaction.getId());
        waitForPageLoad();
        page.waitForTimeout(1000);

        page.locator("[data-testid='btn-add-tax-detail']").click();
        page.waitForTimeout(500);

        page.locator("[data-testid='tax-type-select']").selectOption("PPN_KELUARAN");
        page.waitForTimeout(300);

        page.locator("[data-testid='dpp']").fill("10000000");
        page.locator("[data-testid='ppn']").fill("1100000");
        page.locator("[data-testid='counterparty-name']").fill("PT Delete Test");

        page.locator("[data-testid='btn-save-tax-detail']").click();
        page.waitForTimeout(1000);

        // Now verify detail exists
        Locator detailCard = page.locator("[data-testid^='tax-detail-card-']").first();
        assertThat(detailCard).isVisible();

        // Accept confirm dialog
        page.onDialog(dialog -> dialog.accept());

        // Click delete
        detailCard.locator("button[title='Hapus']").click();
        page.waitForTimeout(1000);

        // Should show empty state after deleting the only detail
        Locator emptyState = page.locator("[data-testid='tax-detail-empty']");
        assertThat(emptyState).isVisible();
    }

    @Test
    @DisplayName("Should show indicator on transaction list")
    void shouldShowIndicatorOnTransactionList() {
        // First add a tax detail to a transaction
        navigateTo("/transactions/" + ppnTransaction.getId());
        waitForPageLoad();
        page.waitForTimeout(1000);

        page.locator("[data-testid='btn-add-tax-detail']").click();
        page.waitForTimeout(500);

        page.locator("[data-testid='tax-type-select']").selectOption("PPN_KELUARAN");
        page.waitForTimeout(300);

        page.locator("[data-testid='dpp']").fill("10000000");
        page.locator("[data-testid='ppn']").fill("1100000");
        page.locator("[data-testid='counterparty-name']").fill("PT Indicator Test");

        page.locator("[data-testid='btn-save-tax-detail']").click();
        page.waitForTimeout(1000);

        // Go to transaction list
        navigateTo("/transactions");
        waitForPageLoad();

        // Tax indicator should be visible for the transaction with tax detail
        Locator indicator = page.locator("[data-testid='tax-indicator']");
        assertThat(indicator.first()).isVisible();
    }

    @Test
    @DisplayName("Should show bulk entry page")
    void shouldShowBulkEntryPage() {
        navigateTo("/transactions/tax-details/bulk");
        waitForPageLoad();

        // Page title should be visible
        assertThat(page.locator("#page-title")).hasText("Input Detail Pajak");

        // Date filter form should be visible
        assertThat(page.locator("input[name='startDate']")).isVisible();
        assertThat(page.locator("input[name='endDate']")).isVisible();
    }
}
