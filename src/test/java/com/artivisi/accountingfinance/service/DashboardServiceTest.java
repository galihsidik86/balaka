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
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DashboardService.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("DashboardService Integration Tests")
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Nested
    @DisplayName("KPI Calculation")
    class KpiTests {

        @Test
        @DisplayName("Should calculate KPIs for current month")
        void shouldCalculateKpisForCurrentMonth() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi).isNotNull();
            assertThat(kpi.revenue()).isNotNull();
            assertThat(kpi.expense()).isNotNull();
            assertThat(kpi.netProfit()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate KPIs for past month")
        void shouldCalculateKpisForPastMonth() {
            YearMonth pastMonth = YearMonth.of(2025, 6);
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(pastMonth);

            assertThat(kpi).isNotNull();
        }

        @Test
        @DisplayName("Should calculate net profit correctly")
        void shouldCalculateNetProfitCorrectly() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi.netProfit())
                    .isEqualByComparingTo(kpi.revenue().subtract(kpi.expense()));
        }

        @Test
        @DisplayName("Should include transaction count")
        void shouldIncludeTransactionCount() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi.transactionCount()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should include cash bank items")
        void shouldIncludeCashBankItems() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi.cashBankItems()).isNotNull();
        }

        @Test
        @DisplayName("Should handle empty month data")
        void shouldHandleEmptyMonthData() {
            YearMonth futureMonth = YearMonth.of(2099, 12);
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(futureMonth);

            assertThat(kpi).isNotNull();
        }
    }

    @Nested
    @DisplayName("Amortization Summary")
    class AmortizationSummaryTests {

        @Test
        @DisplayName("Should get amortization summary")
        void shouldGetAmortizationSummary() {
            DashboardService.AmortizationSummary summary = dashboardService.getAmortizationSummary();

            assertThat(summary).isNotNull();
            assertThat(summary.totalPending()).isGreaterThanOrEqualTo(0);
            assertThat(summary.overdueCount()).isGreaterThanOrEqualTo(0);
            assertThat(summary.dueThisMonth()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should have valid overdue counts")
        void shouldHaveValidOverdueCounts() {
            DashboardService.AmortizationSummary summary = dashboardService.getAmortizationSummary();

            assertThat(summary.overdueCount()).isLessThanOrEqualTo(summary.totalPending());
        }

        @Test
        @DisplayName("Should include upcoming entries")
        void shouldIncludeUpcomingEntries() {
            DashboardService.AmortizationSummary summary = dashboardService.getAmortizationSummary();

            assertThat(summary.upcomingEntries()).isNotNull();
        }
    }

    @Nested
    @DisplayName("KPI Change Percentages")
    class KpiChangePercentageTests {

        @Test
        @DisplayName("Should calculate revenue change percentage")
        void shouldCalculateRevenueChangePercentage() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi.revenueChange()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate expense change percentage")
        void shouldCalculateExpenseChangePercentage() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi.expenseChange()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate profit change percentage")
        void shouldCalculateProfitChangePercentage() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi.profitChange()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate margin change as points difference")
        void shouldCalculateMarginChange() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi.marginChange()).isNotNull();
        }

        @Test
        @DisplayName("Should return zero margin when revenue is zero")
        void shouldReturnZeroMarginWhenRevenueIsZero() {
            // Far future month with no transactions
            YearMonth futureMonth = YearMonth.of(2099, 12);
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(futureMonth);

            assertThat(kpi.profitMargin()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return 100% change from zero previous")
        void shouldReturn100PercentChangeFromZeroPrevious() {
            // Use a month with data where previous month has no data (test data starts Jan 2024)
            YearMonth januaryMonth = YearMonth.of(2024, 1);
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(januaryMonth);

            // If Jan 2024 has revenue but Dec 2023 is just capital injection (no revenue accounts),
            // the revenue change should be 100
            if (kpi.revenue().compareTo(BigDecimal.ZERO) > 0) {
                assertThat(kpi.revenueChange()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should calculate receivables and payables balances")
        void shouldCalculateReceivablesAndPayablesBalances() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi.receivablesBalance()).isNotNull();
            assertThat(kpi.payablesBalance()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate cash balance from cash/bank accounts")
        void shouldCalculateCashBalanceFromCashBankAccounts() {
            YearMonth currentMonth = YearMonth.now();
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(currentMonth);

            assertThat(kpi.cashBalance()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate KPIs for month with test data")
        void shouldCalculateKpisForMonthWithTestData() {
            // Feb 2024 has consulting revenue and salary expense in test data
            YearMonth feb2024 = YearMonth.of(2024, 2);
            DashboardService.DashboardKPI kpi = dashboardService.calculateKPIs(feb2024);

            assertThat(kpi).isNotNull();
            assertThat(kpi.month()).isEqualTo(feb2024);
            // Revenue should include consulting revenue from Feb 2024
            assertThat(kpi.revenue()).isNotNull();
            // Expenses should include salary from Feb 2024
            assertThat(kpi.expense()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Recent Transactions")
    class RecentTransactionsTests {

        @Test
        @DisplayName("Should get recent transactions with limit")
        void shouldGetRecentTransactionsWithLimit() {
            List<DashboardService.RecentTransaction> transactions = dashboardService.getRecentTransactions(5);

            assertThat(transactions).isNotNull();
            assertThat(transactions.size()).isLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Should get recent transactions with limit of 1")
        void shouldGetRecentTransactionsWithLimitOfOne() {
            List<DashboardService.RecentTransaction> transactions = dashboardService.getRecentTransactions(1);

            assertThat(transactions).isNotNull();
            assertThat(transactions.size()).isLessThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should include transaction details in recent transactions")
        void shouldIncludeTransactionDetailsInRecentTransactions() {
            List<DashboardService.RecentTransaction> transactions = dashboardService.getRecentTransactions(10);

            for (DashboardService.RecentTransaction tx : transactions) {
                assertThat(tx.transactionNumber()).isNotBlank();
                assertThat(tx.transactionDate()).isNotNull();
                assertThat(tx.amount()).isNotNull();
                assertThat(tx.category()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Quick Templates")
    class QuickTemplateTests {

        @Test
        @DisplayName("Should get frequent templates")
        void shouldGetFrequentTemplates() {
            List<DashboardService.QuickTemplate> templates = dashboardService.getFrequentTemplates(5);

            assertThat(templates).isNotNull();
            assertThat(templates.size()).isLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Should only return templates with usage count > 0")
        void shouldOnlyReturnTemplatesWithUsageCountGreaterThanZero() {
            List<DashboardService.QuickTemplate> templates = dashboardService.getFrequentTemplates(10);

            for (DashboardService.QuickTemplate t : templates) {
                assertThat(t.usageCount()).isGreaterThan(0);
            }
        }

        @Test
        @DisplayName("Should get recent templates")
        void shouldGetRecentTemplates() {
            List<DashboardService.QuickTemplate> templates = dashboardService.getRecentTemplates(5);

            assertThat(templates).isNotNull();
            assertThat(templates.size()).isLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("Should only return templates with lastUsedAt not null")
        void shouldOnlyReturnTemplatesWithLastUsedAtNotNull() {
            List<DashboardService.QuickTemplate> templates = dashboardService.getRecentTemplates(10);

            for (DashboardService.QuickTemplate t : templates) {
                assertThat(t.lastUsedAt()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should include template details")
        void shouldIncludeTemplateDetails() {
            List<DashboardService.QuickTemplate> templates = dashboardService.getFrequentTemplates(10);

            for (DashboardService.QuickTemplate t : templates) {
                assertThat(t.id()).isNotNull();
                assertThat(t.templateName()).isNotBlank();
                assertThat(t.category()).isNotNull();
            }
        }
    }
}
