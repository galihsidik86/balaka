package com.artivisi.accountingfinance.functional.seller;

import com.artivisi.accountingfinance.functional.page.BalanceSheetPage;
import com.artivisi.accountingfinance.functional.page.CashFlowPage;
import com.artivisi.accountingfinance.functional.page.IncomeStatementPage;
import com.artivisi.accountingfinance.functional.page.InventoryTransactionListPage;
import com.artivisi.accountingfinance.functional.page.TrialBalancePage;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

/**
 * Online Seller Sales Tests
 * Tests inventory sales transactions and financial reports.
 * Uses Page Object Pattern with ID-based locators.
 */
@DisplayName("Online Seller - Sales")
@Import(SellerTestDataInitializer.class)
class SellerSalesTest extends PlaywrightTestBase {

    // Page Objects
    private InventoryTransactionListPage transactionListPage;
    private IncomeStatementPage incomeStatementPage;
    private BalanceSheetPage balanceSheetPage;
    private CashFlowPage cashFlowPage;
    private TrialBalancePage trialBalancePage;

    private void initPageObjects() {
        String baseUrl = "http://localhost:" + port;
        transactionListPage = new InventoryTransactionListPage(page, baseUrl);
        incomeStatementPage = new IncomeStatementPage(page, baseUrl);
        balanceSheetPage = new BalanceSheetPage(page, baseUrl);
        cashFlowPage = new CashFlowPage(page, baseUrl);
        trialBalancePage = new TrialBalancePage(page, baseUrl);
    }

    @Test
    @DisplayName("Should display inventory transaction list")
    void shouldDisplayInventoryTransactionList() {
        loginAsAdmin();
        initPageObjects();

        transactionListPage.navigate()
            .verifyPageTitle()
            .verifyTableVisible();
    }

    @Test
    @DisplayName("Should display income statement")
    void shouldDisplayIncomeStatement() {
        loginAsAdmin();
        initPageObjects();

        incomeStatementPage.navigate()
            .verifyPageTitle();
    }

    @Test
    @DisplayName("Should display balance sheet")
    void shouldDisplayBalanceSheet() {
        loginAsAdmin();
        initPageObjects();

        balanceSheetPage.navigate()
            .verifyPageTitle();
    }

    @Test
    @DisplayName("Should display cash flow statement")
    void shouldDisplayCashFlowStatement() {
        loginAsAdmin();
        initPageObjects();

        cashFlowPage.navigate()
            .verifyPageTitle();
    }

    @Test
    @DisplayName("Should display trial balance")
    void shouldDisplayTrialBalance() {
        loginAsAdmin();
        initPageObjects();

        trialBalancePage.navigate()
            .verifyPageTitle();
    }
}
