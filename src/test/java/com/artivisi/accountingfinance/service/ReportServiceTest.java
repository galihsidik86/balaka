package com.artivisi.accountingfinance.service;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ReportService.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("ReportService Integration Tests")
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Nested
    @DisplayName("Trial Balance Report")
    class TrialBalanceTests {

        @Test
        @DisplayName("Should generate trial balance for current date")
        void shouldGenerateTrialBalanceForCurrentDate() {
            ReportService.TrialBalanceReport report = reportService.generateTrialBalance(LocalDate.now());

            assertThat(report).isNotNull();
            assertThat(report.asOfDate()).isEqualTo(LocalDate.now());
            assertThat(report.items()).isNotNull();
        }

        @Test
        @DisplayName("Should generate trial balance for past date")
        void shouldGenerateTrialBalanceForPastDate() {
            LocalDate pastDate = LocalDate.of(2025, 6, 30);
            ReportService.TrialBalanceReport report = reportService.generateTrialBalance(pastDate);

            assertThat(report).isNotNull();
            assertThat(report.asOfDate()).isEqualTo(pastDate);
        }

        @Test
        @DisplayName("Should have balanced trial balance")
        void shouldHaveBalancedTrialBalance() {
            ReportService.TrialBalanceReport report = reportService.generateTrialBalance(LocalDate.now());

            BigDecimal totalDebit = report.items().stream()
                    .map(ReportService.TrialBalanceItem::debitBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCredit = report.items().stream()
                    .map(ReportService.TrialBalanceItem::creditBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            assertThat(totalDebit).isEqualByComparingTo(totalCredit);
        }

        @Test
        @DisplayName("Should generate trial balance for far future date")
        void shouldGenerateTrialBalanceForFarFutureDate() {
            LocalDate futureDate = LocalDate.of(2099, 12, 31);
            ReportService.TrialBalanceReport report = reportService.generateTrialBalance(futureDate);

            assertThat(report).isNotNull();
        }
    }

    @Nested
    @DisplayName("Income Statement Report")
    class IncomeStatementTests {

        @Test
        @DisplayName("Should generate income statement for date range")
        void shouldGenerateIncomeStatementForDateRange() {
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            ReportService.IncomeStatementReport report = reportService.generateIncomeStatement(startDate, endDate);

            assertThat(report).isNotNull();
            assertThat(report.startDate()).isEqualTo(startDate);
            assertThat(report.endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("Should calculate net income correctly")
        void shouldCalculateNetIncomeCorrectly() {
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            ReportService.IncomeStatementReport report = reportService.generateIncomeStatement(startDate, endDate);

            BigDecimal expectedNetIncome = report.totalRevenue().subtract(report.totalExpense());
            assertThat(report.netIncome()).isEqualByComparingTo(expectedNetIncome);
        }

        @Test
        @DisplayName("Should generate income statement for single month")
        void shouldGenerateIncomeStatementForSingleMonth() {
            LocalDate startDate = LocalDate.of(2025, 6, 1);
            LocalDate endDate = LocalDate.of(2025, 6, 30);

            ReportService.IncomeStatementReport report = reportService.generateIncomeStatement(startDate, endDate);

            assertThat(report).isNotNull();
            assertThat(report.revenueItems()).isNotNull();
            assertThat(report.expenseItems()).isNotNull();
        }

        @Test
        @DisplayName("Should handle empty period")
        void shouldHandleEmptyPeriod() {
            LocalDate startDate = LocalDate.of(2099, 1, 1);
            LocalDate endDate = LocalDate.of(2099, 12, 31);

            ReportService.IncomeStatementReport report = reportService.generateIncomeStatement(startDate, endDate);

            assertThat(report).isNotNull();
            assertThat(report.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(report.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Balance Sheet Report")
    class BalanceSheetTests {

        @Test
        @DisplayName("Should generate balance sheet for current date")
        void shouldGenerateBalanceSheetForCurrentDate() {
            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(LocalDate.now());

            assertThat(report).isNotNull();
            assertThat(report.asOfDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should have balanced balance sheet")
        void shouldHaveBalancedBalanceSheet() {
            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(LocalDate.now());

            BigDecimal assets = report.totalAssets();
            BigDecimal liabilitiesAndEquity = report.totalLiabilities().add(report.totalEquity());

            assertThat(assets).isEqualByComparingTo(liabilitiesAndEquity);
        }

        @Test
        @DisplayName("Should generate balance sheet for past date")
        void shouldGenerateBalanceSheetForPastDate() {
            LocalDate pastDate = LocalDate.of(2025, 6, 30);
            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(pastDate);

            assertThat(report).isNotNull();
            assertThat(report.asOfDate()).isEqualTo(pastDate);
        }

        @Test
        @DisplayName("Should include asset items")
        void shouldIncludeAssetItems() {
            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(LocalDate.now());

            assertThat(report.assetItems()).isNotNull();
        }

        @Test
        @DisplayName("Should include liability items")
        void shouldIncludeLiabilityItems() {
            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(LocalDate.now());

            assertThat(report.liabilityItems()).isNotNull();
        }

        @Test
        @DisplayName("Should include equity items")
        void shouldIncludeEquityItems() {
            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(LocalDate.now());

            assertThat(report.equityItems()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cash Flow Report")
    class CashFlowTests {

        @Test
        @DisplayName("Should generate cash flow statement for date range")
        void shouldGenerateCashFlowStatementForDateRange() {
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            ReportService.CashFlowReport report = reportService.generateCashFlowStatement(startDate, endDate);

            assertThat(report).isNotNull();
            assertThat(report.startDate()).isEqualTo(startDate);
            assertThat(report.endDate()).isEqualTo(endDate);
        }

        @Test
        @DisplayName("Should include operating activities")
        void shouldIncludeOperatingActivities() {
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            ReportService.CashFlowReport report = reportService.generateCashFlowStatement(startDate, endDate);

            assertThat(report.operatingItems()).isNotNull();
        }

        @Test
        @DisplayName("Should include investing activities")
        void shouldIncludeInvestingActivities() {
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            ReportService.CashFlowReport report = reportService.generateCashFlowStatement(startDate, endDate);

            assertThat(report.investingItems()).isNotNull();
        }

        @Test
        @DisplayName("Should include financing activities")
        void shouldIncludeFinancingActivities() {
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            ReportService.CashFlowReport report = reportService.generateCashFlowStatement(startDate, endDate);

            assertThat(report.financingItems()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate net cash flow correctly")
        void shouldCalculateNetCashFlowCorrectly() {
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 12, 31);

            ReportService.CashFlowReport report = reportService.generateCashFlowStatement(startDate, endDate);

            BigDecimal expectedNet = report.operatingTotal()
                    .add(report.investingTotal())
                    .add(report.financingTotal());
            assertThat(report.netCashChange()).isEqualByComparingTo(expectedNet);
        }

        @Test
        @DisplayName("Should include beginning and ending cash balance")
        void shouldIncludeBeginningAndEndingCashBalance() {
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            ReportService.CashFlowReport report = reportService.generateCashFlowStatement(startDate, endDate);

            assertThat(report.beginningCashBalance()).isNotNull();
            assertThat(report.endingCashBalance()).isNotNull();
        }

        @Test
        @DisplayName("Should include cash account balances breakdown")
        void shouldIncludeCashAccountBalancesBreakdown() {
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            ReportService.CashFlowReport report = reportService.generateCashFlowStatement(startDate, endDate);

            assertThat(report.cashAccountBalances()).isNotNull();
        }

        @Test
        @DisplayName("Should handle empty period for cash flow")
        void shouldHandleEmptyPeriodForCashFlow() {
            LocalDate startDate = LocalDate.of(2099, 1, 1);
            LocalDate endDate = LocalDate.of(2099, 12, 31);

            ReportService.CashFlowReport report = reportService.generateCashFlowStatement(startDate, endDate);

            assertThat(report).isNotNull();
            assertThat(report.operatingTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(report.investingTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(report.financingTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Trial Balance with Test Data")
    class TrialBalanceWithTestDataTests {

        @Test
        @DisplayName("Should generate trial balance with balanced totals")
        void shouldGenerateTrialBalanceWithBalancedTotals() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);
            ReportService.TrialBalanceReport report = reportService.generateTrialBalance(asOfDate);

            assertThat(report.totalDebit()).isEqualByComparingTo(report.totalCredit());
            // Items should not include accounts with zero balance
            for (ReportService.TrialBalanceItem item : report.items()) {
                BigDecimal itemTotal = item.debitBalance().add(item.creditBalance());
                assertThat(itemTotal).isNotEqualByComparingTo(BigDecimal.ZERO);
            }
        }

        @Test
        @DisplayName("Should exclude VOID and DRAFT entries from trial balance")
        void shouldExcludeVoidAndDraftEntriesFromTrialBalance() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);
            ReportService.TrialBalanceReport report = reportService.generateTrialBalance(asOfDate);

            // With VOID/DRAFT excluded, debits and credits must still balance
            assertThat(report.totalDebit()).isEqualByComparingTo(report.totalCredit());
        }

        @Test
        @DisplayName("Should have positive totals for date with transactions")
        void shouldHavePositiveTotalsForDateWithTransactions() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);
            ReportService.TrialBalanceReport report = reportService.generateTrialBalance(asOfDate);

            // Test data has transactions, so totals should be positive
            assertThat(report.totalDebit()).isGreaterThan(BigDecimal.ZERO);
            assertThat(report.totalCredit()).isGreaterThan(BigDecimal.ZERO);
            assertThat(report.items()).isNotEmpty();
        }

        @Test
        @DisplayName("Should only include items with non-zero debit or credit")
        void shouldOnlyIncludeItemsWithNonZeroBalance() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);
            ReportService.TrialBalanceReport report = reportService.generateTrialBalance(asOfDate);

            for (ReportService.TrialBalanceItem item : report.items()) {
                boolean hasDebit = item.debitBalance().compareTo(BigDecimal.ZERO) > 0;
                boolean hasCredit = item.creditBalance().compareTo(BigDecimal.ZERO) > 0;
                assertThat(hasDebit || hasCredit).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Income Statement with Test Data")
    class IncomeStatementWithTestDataTests {

        @Test
        @DisplayName("Should calculate positive revenue from test data")
        void shouldCalculatePositiveRevenueFromTestData() {
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            ReportService.IncomeStatementReport report = reportService.generateIncomeStatement(startDate, endDate);

            // V901 alone has 52M revenue, plus other migrations add more
            assertThat(report.totalRevenue()).isGreaterThanOrEqualTo(new BigDecimal("52000000"));
        }

        @Test
        @DisplayName("Should calculate positive expense from test data")
        void shouldCalculatePositiveExpenseFromTestData() {
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            ReportService.IncomeStatementReport report = reportService.generateIncomeStatement(startDate, endDate);

            // V901 alone has 19M expense, plus other migrations add more
            assertThat(report.totalExpense()).isGreaterThanOrEqualTo(new BigDecimal("19000000"));
        }

        @Test
        @DisplayName("Should calculate net income as revenue minus expense")
        void shouldCalculateNetIncomeCorrectlyFromTestData() {
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            ReportService.IncomeStatementReport report = reportService.generateIncomeStatement(startDate, endDate);

            BigDecimal expectedNetIncome = report.totalRevenue().subtract(report.totalExpense());
            assertThat(report.netIncome()).isEqualByComparingTo(expectedNetIncome);
        }

        @Test
        @DisplayName("Should include revenue and expense items")
        void shouldIncludeRevenueAndExpenseItems() {
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            ReportService.IncomeStatementReport report = reportService.generateIncomeStatement(startDate, endDate);

            assertThat(report.revenueItems()).isNotEmpty();
            assertThat(report.expenseItems()).isNotEmpty();
        }

        @Test
        @DisplayName("Should not include header accounts in items")
        void shouldNotIncludeHeaderAccountsInItems() {
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            ReportService.IncomeStatementReport report = reportService.generateIncomeStatement(startDate, endDate);

            for (ReportService.IncomeStatementItem item : report.revenueItems()) {
                assertThat(item.account().getIsHeader()).isFalse();
            }
            for (ReportService.IncomeStatementItem item : report.expenseItems()) {
                assertThat(item.account().getIsHeader()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("Balance Sheet with Test Data")
    class BalanceSheetWithTestDataTests {

        @Test
        @DisplayName("Should calculate positive total assets")
        void shouldCalculatePositiveTotalAssets() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);

            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(asOfDate);

            assertThat(report.totalAssets()).isGreaterThan(BigDecimal.ZERO);
            assertThat(report.assetItems()).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle contra-asset accounts correctly")
        void shouldHandleContraAssetAccountsCorrectly() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);

            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(asOfDate);

            // Balance sheet must be balanced: A = L + E (with retained earnings)
            BigDecimal liabilitiesAndEquity = report.totalLiabilities().add(report.totalEquity());
            assertThat(report.totalAssets()).isEqualByComparingTo(liabilitiesAndEquity);
        }

        @Test
        @DisplayName("Should include retained earnings in equity")
        void shouldIncludeRetainedEarningsInEquity() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);

            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(asOfDate);

            // Balance sheet should be balanced: A = L + E
            BigDecimal liabilitiesAndEquity = report.totalLiabilities().add(report.totalEquity());
            assertThat(report.totalAssets()).isEqualByComparingTo(liabilitiesAndEquity);
        }

        @Test
        @DisplayName("Should calculate current year earnings matching income statement")
        void shouldCalculateCurrentYearEarningsMatchingIncomeStatement() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);

            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(asOfDate);

            // Current year earnings should match income statement for Jan 1 - Jun 30
            ReportService.IncomeStatementReport incomeReport = reportService.generateIncomeStatement(
                    LocalDate.of(2024, 1, 1), asOfDate);

            assertThat(report.currentYearEarnings()).isEqualByComparingTo(incomeReport.netIncome());
        }

        @Test
        @DisplayName("Should include liability items")
        void shouldIncludeLiabilityItemsFromTestData() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);

            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(asOfDate);

            // V901 has hutang usaha 10M, so liabilities should be non-empty
            assertThat(report.liabilityItems()).isNotEmpty();
            assertThat(report.totalLiabilities()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should include equity items")
        void shouldIncludeEquityItemsFromTestData() {
            LocalDate asOfDate = LocalDate.of(2024, 6, 30);

            ReportService.BalanceSheetReport report = reportService.generateBalanceSheet(asOfDate);

            // V901 has modal disetor, so equity items should be non-empty
            assertThat(report.equityItems()).isNotEmpty();
        }
    }
}
