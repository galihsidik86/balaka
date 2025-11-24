package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.page.AccountFormPage;
import com.artivisi.accountingfinance.functional.page.ChartOfAccountsPage;
import com.artivisi.accountingfinance.functional.page.LoginPage;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Chart of Accounts - Activate/Deactivate Account")
class ChartOfAccountStatusTest extends PlaywrightTestBase {

    private LoginPage loginPage;
    private ChartOfAccountsPage accountsPage;
    private AccountFormPage formPage;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage(page, baseUrl());
        accountsPage = new ChartOfAccountsPage(page, baseUrl());
        formPage = new AccountFormPage(page, baseUrl());

        loginPage.navigate().loginAsAdmin();
    }

    @Test
    @DisplayName("Should show deactivate button on active accounts")
    void shouldShowDeactivateButtonOnActiveAccounts() {
        accountsPage.navigate();

        // Root accounts are active by default - should have deactivate button
        accountsPage.assertDeactivateButtonVisible("1");
        accountsPage.assertDeactivateButtonVisible("2");
        accountsPage.assertDeactivateButtonVisible("3");
        accountsPage.assertDeactivateButtonVisible("4");
        accountsPage.assertDeactivateButtonVisible("5");

        // Active accounts should NOT have activate button
        accountsPage.assertActivateButtonNotVisible("1");
    }

    @Test
    @DisplayName("Should deactivate account and show success message")
    void shouldDeactivateAccountAndShowSuccessMessage() {
        // Create a test account to deactivate
        formPage.navigateToNew();
        formPage.fillAccountCode("9.8.01");
        formPage.fillAccountName("Account To Deactivate");
        formPage.selectAccountType("EXPENSE");
        formPage.selectNormalBalanceDebit();
        formPage.clickSave();

        accountsPage.assertSuccessMessageVisible();
        accountsPage.assertAccountRowVisible("9.8.01");

        // Verify account is active (has deactivate button, no activate button)
        accountsPage.assertDeactivateButtonVisible("9.8.01");
        accountsPage.assertActivateButtonNotVisible("9.8.01");

        // Deactivate the account
        accountsPage.clickDeactivateAccount("9.8.01");

        // Verify success message
        accountsPage.assertSuccessMessageVisible();
        accountsPage.assertSuccessMessageText("Akun berhasil dinonaktifkan");

        // Verify account is now inactive (has activate button, no deactivate button)
        accountsPage.assertActivateButtonVisible("9.8.01");
        accountsPage.assertDeactivateButtonNotVisible("9.8.01");
    }

    @Test
    @DisplayName("Should show activate button on inactive accounts")
    void shouldShowActivateButtonOnInactiveAccounts() {
        // Create and deactivate an account
        formPage.navigateToNew();
        formPage.fillAccountCode("9.8.02");
        formPage.fillAccountName("Inactive Account Test");
        formPage.selectAccountType("EXPENSE");
        formPage.selectNormalBalanceDebit();
        formPage.clickSave();

        accountsPage.clickDeactivateAccount("9.8.02");

        // Inactive account should have activate button
        accountsPage.assertActivateButtonVisible("9.8.02");

        // Inactive account should NOT have deactivate button
        accountsPage.assertDeactivateButtonNotVisible("9.8.02");
    }

    @Test
    @DisplayName("Should activate account and show success message")
    void shouldActivateAccountAndShowSuccessMessage() {
        // Create and deactivate an account
        formPage.navigateToNew();
        formPage.fillAccountCode("9.8.03");
        formPage.fillAccountName("Account To Activate");
        formPage.selectAccountType("EXPENSE");
        formPage.selectNormalBalanceDebit();
        formPage.clickSave();

        accountsPage.clickDeactivateAccount("9.8.03");
        accountsPage.assertActivateButtonVisible("9.8.03");

        // Now activate the account
        accountsPage.clickActivateAccount("9.8.03");

        // Verify success message
        accountsPage.assertSuccessMessageVisible();
        accountsPage.assertSuccessMessageText("Akun berhasil diaktifkan");

        // Verify account is now active again
        accountsPage.assertDeactivateButtonVisible("9.8.03");
        accountsPage.assertActivateButtonNotVisible("9.8.03");
    }

    @Test
    @DisplayName("Inactive accounts should be visually distinguished (grayed out)")
    void inactiveAccountsShouldBeGrayedOut() {
        // Create an account
        formPage.navigateToNew();
        formPage.fillAccountCode("9.8.04");
        formPage.fillAccountName("Visual Test Account");
        formPage.selectAccountType("EXPENSE");
        formPage.selectNormalBalanceDebit();
        formPage.clickSave();

        // Active account should not have inactive styling
        accountsPage.assertAccountIsActive("9.8.04");
        accountsPage.assertInactiveBadgeNotVisible("9.8.04");

        // Deactivate the account
        accountsPage.clickDeactivateAccount("9.8.04");

        // Inactive account should have grayed out styling
        accountsPage.assertAccountIsInactive("9.8.04");
        accountsPage.assertInactiveBadgeVisible("9.8.04");
    }

    @Test
    @DisplayName("Status change should persist after page refresh")
    void statusChangeShouldPersistAfterRefresh() {
        // Create an account
        formPage.navigateToNew();
        formPage.fillAccountCode("9.8.05");
        formPage.fillAccountName("Persist Test Account");
        formPage.selectAccountType("EXPENSE");
        formPage.selectNormalBalanceDebit();
        formPage.clickSave();

        // Deactivate the account
        accountsPage.clickDeactivateAccount("9.8.05");
        accountsPage.assertActivateButtonVisible("9.8.05");

        // Refresh the page
        accountsPage.navigate();

        // Account should still be inactive
        accountsPage.assertActivateButtonVisible("9.8.05");
        accountsPage.assertDeactivateButtonNotVisible("9.8.05");
        accountsPage.assertAccountIsInactive("9.8.05");
    }
}
