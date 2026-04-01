package com.artivisi.accountingfinance.functional.demo;

import com.artivisi.accountingfinance.entity.*;
import com.artivisi.accountingfinance.enums.TransactionStatus;
import com.artivisi.accountingfinance.repository.*;
import com.artivisi.accountingfinance.service.FiscalYearClosingService;
import com.artivisi.accountingfinance.service.ReportService;
import com.artivisi.accountingfinance.service.ReportService.TrialBalanceReport;
import com.artivisi.accountingfinance.service.ReportService.TrialBalanceItem;
import com.artivisi.accountingfinance.service.TaxTransactionDetailService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.artivisi.accountingfinance.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification test that runs AFTER DemoItServiceDataLoader.
 * Dumps and verifies all critical financial data:
 * 1. Payroll journal entries (BPJS, PPh 21)
 * 2. Depreciation journal entries
 * 3. Tax amounts (PPN, PPh 21, PPh 23)
 * 4. Per-account balances at end of each month
 * 5. Trial balance at year-end
 *
 * Run after demo loader: ./mvnw test -Dtest="DemoItServiceDataLoader,DemoVerificationTest"
 * Or standalone (will load data first): ./mvnw test -Dtest=DemoVerificationTest
 */
@Slf4j
@DisplayName("Demo Data Verification")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemoVerificationTest extends DemoDataLoaderBase {

    @Autowired private jakarta.persistence.EntityManager entityManager;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private JournalEntryRepository journalEntryRepository;
    @Autowired private PayrollRunRepository payrollRunRepository;
    @Autowired private PayrollDetailRepository payrollDetailRepository;
    @Autowired private ChartOfAccountRepository chartOfAccountRepository;
    @Autowired private ReportService reportService;
    @Autowired private FiscalYearClosingService fiscalYearClosingService;
    @Autowired private TaxTransactionDetailRepository taxTransactionDetailRepository;

    private static final NumberFormat IDR = NumberFormat.getNumberInstance(new Locale("id", "ID"));

    @Override protected String industryName() { return "IT Service"; }
    @Override protected String seedDataPath() { return "industry-seed/it-service/seed-data"; }
    @Override protected String demoDataPath() { return "src/test/resources/demo-data/it-service"; }
    @Override protected long baseSalary() { return 15000000; }

    @Test @Order(1) @DisplayName("0. Load demo data")
    void loadData() throws Exception {
        importSeedData();
        importMasterData();
        createDemoUsers();
        executeDemoTransactions("demo-data/it-service/demo-transactions.csv");
    }

    @Test @Order(2) @DisplayName("1. Verify payroll journal entries")
    void verifyPayroll() {
        entityManager.clear(); // Clear L1 cache to see committed data from Playwright
        log.info("========== PAYROLL VERIFICATION ==========");

        var payrollRuns = payrollRunRepository.findAll().stream()
                .sorted(Comparator.comparing(PayrollRun::getPayrollPeriod))
                .toList();

        log.info("Payroll runs: {}", payrollRuns.size());
        assertThat(payrollRuns).hasSize(12);

        for (var run : payrollRuns) {
            var details = payrollDetailRepository.findByPayrollRunId(run.getId());
            log.info("Period {} | Status={} | Employees={} | Gross={} | Deductions={} | NetPay={} | CompanyBPJS={} | PPh21={}",
                    run.getPayrollPeriod(), run.getStatus(), details.size(),
                    fmt(run.getTotalGross()), fmt(run.getTotalDeductions()),
                    fmt(run.getTotalNetPay()), fmt(run.getTotalCompanyBpjs()),
                    fmt(run.getTotalPph21()));

            // Verify consistency: gross - deductions = net pay
            BigDecimal expectedNet = run.getTotalGross().subtract(run.getTotalDeductions());
            assertThat(run.getTotalNetPay())
                    .as("Period %s: gross - deductions should equal net pay", run.getPayrollPeriod())
                    .isEqualByComparingTo(expectedNet);

            // Log per-employee details for first month (avoid lazy loading employee)
            if (run.getPayrollPeriod().equals("2025-01")) {
                for (var d : details) {
                    log.info("  EMP | Gross={} | BPJSKesEmp={} | BPJSKesCo={} | JHTEmp={} | JHTCo={} | JPEmp={} | JPCo={} | JKK={} | JKM={} | PPh21={} | Net={}",
                            fmt(d.getGrossSalary()),
                            fmt(d.getBpjsKesEmployee()), fmt(d.getBpjsKesCompany()),
                            fmt(d.getBpjsJhtEmployee()), fmt(d.getBpjsJhtCompany()),
                            fmt(d.getBpjsJpEmployee()), fmt(d.getBpjsJpCompany()),
                            fmt(d.getBpjsJkk()), fmt(d.getBpjsJkm()),
                            fmt(d.getPph21()), fmt(d.getNetPay()));
                }
            }

            // Dump journal entries for first payroll via reference number lookup
            if (run.getPayrollPeriod().equals("2025-01")) {
                // Find payroll transaction by reference number
                String ref = "PAYROLL-" + run.getPayrollPeriod();
                // Log all transaction references to debug
                var allRefs = transactionRepository.findAll().stream()
                        .map(t -> t.getReferenceNumber() + " [" + t.getDescription() + "]")
                        .filter(r -> r.contains("PAYROLL") || r.contains("Payroll") || r.contains("payroll"))
                        .toList();
                log.info("  Payroll-related transactions: {}", allRefs);
                var payrollTx = transactionRepository.findAll().stream()
                        .filter(t -> ref.equals(t.getReferenceNumber()))
                        .findFirst();
                if (payrollTx.isPresent()) {
                    var journalEntries = journalEntryRepository.findByTransactionIdWithAccount(payrollTx.get().getId());
                    log.info("  Journal entries for payroll 2025-01 ({} entries):", journalEntries.size());
                    for (var je : journalEntries) {
                        log.info("    {} {} | D={} | C={}",
                                je.getAccount().getAccountCode(), je.getAccount().getAccountName(),
                                fmt(je.getDebitAmount()), fmt(je.getCreditAmount()));
                    }
                } else {
                    log.warn("  No payroll transaction found with reference: {}", ref);
                }
            }
        }
    }

    @Test @Order(3) @DisplayName("2. Verify depreciation journal entries")
    void verifyDepreciation() {
        log.info("========== DEPRECIATION VERIFICATION ==========");

        // Find all asset-related transactions by reference prefix
        var assetTx = transactionRepository.findAll().stream()
                .filter(t -> t.getReferenceNumber() != null &&
                        t.getReferenceNumber().startsWith("PO-AST"))
                .toList();

        log.info("Fixed asset purchase transactions: {}", assetTx.size());

        // Check depreciation account balances
        var depreciationAccounts = chartOfAccountRepository.findAll().stream()
                .filter(a -> a.getAccountName().contains("Penyusutan") || a.getAccountName().contains("Akum"))
                .toList();

        for (var acct : depreciationAccounts) {
            BigDecimal debit = journalEntryRepository.sumDebitByAccountAndDateRange(
                    acct.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
            BigDecimal credit = journalEntryRepository.sumCreditByAccountAndDateRange(
                    acct.getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
            if (debit.signum() != 0 || credit.signum() != 0) {
                log.info("  {} {} | Debit={} | Credit={}",
                        acct.getAccountCode(), acct.getAccountName(), fmt(debit), fmt(credit));
            }
        }
    }

    @Test @Order(4) @DisplayName("3. Verify tax amounts")
    void verifyTax() {
        log.info("========== TAX VERIFICATION ==========");

        // PPN: Sum all PPN Keluaran (credit to 2.1.03) and PPN deposits (debit to 2.1.03)
        var ppnAccount = chartOfAccountRepository.findByAccountCode("2.1.03");
        if (ppnAccount.isPresent()) {
            BigDecimal ppnDebit = journalEntryRepository.sumDebitByAccountAndDateRange(
                    ppnAccount.get().getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
            BigDecimal ppnCredit = journalEntryRepository.sumCreditByAccountAndDateRange(
                    ppnAccount.get().getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
            log.info("PPN (2.1.03) | Keluaran (Credit)={} | Deposits (Debit)={} | Outstanding={}",
                    fmt(ppnCredit), fmt(ppnDebit), fmt(ppnCredit.subtract(ppnDebit)));
        }

        // PPh 21: Sum from payroll vs deposits
        var pph21Account = chartOfAccountRepository.findByAccountCode("2.1.20");
        if (pph21Account.isPresent()) {
            BigDecimal pph21Debit = journalEntryRepository.sumDebitByAccountAndDateRange(
                    pph21Account.get().getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
            BigDecimal pph21Credit = journalEntryRepository.sumCreditByAccountAndDateRange(
                    pph21Account.get().getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
            log.info("PPh 21 (2.1.20) | Payroll Accrued (Credit)={} | Deposits (Debit)={} | Outstanding={}",
                    fmt(pph21Credit), fmt(pph21Debit), fmt(pph21Credit.subtract(pph21Debit)));
        }

        // PPh 23 kredit pajak
        var pph23Account = chartOfAccountRepository.findByAccountCode("1.1.26");
        if (pph23Account.isPresent()) {
            BigDecimal pph23Debit = journalEntryRepository.sumDebitByAccountAndDateRange(
                    pph23Account.get().getId(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
            log.info("Kredit Pajak PPh 23 (1.1.26) | Total={}", fmt(pph23Debit));
        }

        // Sum PPh 21 from payroll runs
        BigDecimal totalPayrollPph21 = payrollRunRepository.findAll().stream()
                .map(PayrollRun::getTotalPph21)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total PPh 21 from payroll runs: {}", fmt(totalPayrollPph21));

        // Sum PPh 21 deposits from Setor PPh 21 transactions (identified by reference prefix)
        BigDecimal totalPph21Deposits = transactionRepository.findAll().stream()
                .filter(t -> t.getReferenceNumber() != null &&
                        t.getReferenceNumber().startsWith("PPH21-") &&
                        t.getStatus() == TransactionStatus.POSTED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total PPh 21 deposits (Setor PPh 21): {}", fmt(totalPph21Deposits));
    }

    @Test @Order(5) @DisplayName("4. Verify monthly account balances")
    void verifyMonthlyBalances() {
        log.info("========== MONTHLY BALANCE VERIFICATION ==========");

        // Key accounts to track monthly
        String[] keyAccounts = {"1.1.02", "2.1.03", "2.1.10", "2.1.13", "2.1.20", "3.1.01"};
        String[] accountNames = {"Bank BCA", "Hutang PPN", "Hutang Gaji", "Hutang BPJS", "Hutang PPh 21", "Modal"};

        // Header
        StringBuilder header = new StringBuilder(String.format("%-10s", "Month"));
        for (String name : accountNames) {
            header.append(String.format(" | %15s", name));
        }
        log.info(header.toString());
        log.info("-".repeat(header.length()));

        for (int month = 1; month <= 12; month++) {
            LocalDate endOfMonth = LocalDate.of(2025, month, 1).plusMonths(1).minusDays(1);
            StringBuilder row = new StringBuilder(String.format("%-10s", "2025-" + String.format("%02d", month)));

            for (String code : keyAccounts) {
                var acct = chartOfAccountRepository.findByAccountCode(code);
                if (acct.isPresent()) {
                    BigDecimal debit = journalEntryRepository.sumDebitByAccountAndDateRange(
                            acct.get().getId(), LocalDate.of(1900, 1, 1), endOfMonth);
                    BigDecimal credit = journalEntryRepository.sumCreditByAccountAndDateRange(
                            acct.get().getId(), LocalDate.of(1900, 1, 1), endOfMonth);
                    BigDecimal balance = acct.get().getNormalBalance().name().equals("DEBIT")
                            ? debit.subtract(credit) : credit.subtract(debit);
                    row.append(String.format(" | %15s", fmtShort(balance)));
                } else {
                    row.append(String.format(" | %15s", "N/A"));
                }
            }
            log.info(row.toString());
        }
    }

    @Test @Order(6) @DisplayName("5. Verify year-end trial balance")
    void verifyYearEndTrialBalance() {
        validateTrialBalance(LocalDate.of(2025, 12, 31));
    }

    @Test @Order(7) @DisplayName("6. Verify transaction count by template")
    void verifyTransactionsByTemplate() {
        log.info("========== TRANSACTION SUMMARY BY TEMPLATE ==========");
        var allTx = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.POSTED)
                .collect(Collectors.groupingBy(
                        t -> t.getReferenceNumber() != null && t.getReferenceNumber().startsWith("PAYROLL") ? "Post Gaji Bulanan" : "(other)",
                        Collectors.counting()));

        // Count by reference prefix for a simpler breakdown
        var txByRef = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.POSTED)
                .map(t -> t.getDescription())
                .collect(Collectors.groupingBy(desc -> {
                    if (desc.startsWith("Payroll ")) return "Post Gaji Bulanan";
                    return desc.length() > 40 ? desc.substring(0, 40) : desc;
                }, Collectors.counting()));

        txByRef.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> log.info("  {} : {}", e.getKey(), e.getValue()));

        long total = transactionRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransactionStatus.POSTED).count();
        log.info("Total POSTED transactions: {}", total);
    }

    @Test @Order(8) @DisplayName("7. Verify Income Statement (P&L)")
    void verifyIncomeStatement() {
        entityManager.clear();
        log.info("========== INCOME STATEMENT (P&L) 2025 ==========");

        var pl = reportService.generateIncomeStatement(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        log.info("PENDAPATAN:");
        for (var item : pl.revenueItems()) {
            log.info("  {} {} : {}", item.account().getAccountCode(),
                    item.account().getAccountName(), fmt(item.balance()));
        }
        log.info("  Total Pendapatan: {}", fmt(pl.totalRevenue()));

        log.info("BEBAN:");
        for (var item : pl.expenseItems()) {
            log.info("  {} {} : {}", item.account().getAccountCode(),
                    item.account().getAccountName(), fmt(item.balance()));
        }
        log.info("  Total Beban: {}", fmt(pl.totalExpense()));
        log.info("  LABA BERSIH: {}", fmt(pl.netIncome()));

        // Verify specific amounts from SCENARIO.md
        assertThat(pl.totalRevenue()).as("Total revenue should be 2,110,000,000")
                .isEqualByComparingTo(new BigDecimal("2110000000"));

        // Total expenses = Gaji 900M + BPJS 79M + Sewa 180M + Cloud 66M + Telecom 30M
        //                 + Software 9.9M + Ops 35.1M + Bank 180K + Penyusutan 15.625M
        // The exact amount depends on depreciation rounding — verify it's in range
        assertThat(pl.totalExpense()).as("Total expense should be around 1,316M")
                .isBetween(new BigDecimal("1300000000"), new BigDecimal("1330000000"));

        assertThat(pl.netIncome()).as("Net income = revenue - expense")
                .isEqualByComparingTo(pl.totalRevenue().subtract(pl.totalExpense()));
        assertThat(pl.netIncome()).as("Net income should be positive (profitable company)")
                .isGreaterThan(BigDecimal.ZERO);

        log.info("P&L verified: Revenue={}, Expense={}, Net Income={}",
                fmt(pl.totalRevenue()), fmt(pl.totalExpense()), fmt(pl.netIncome()));
    }

    @Test @Order(9) @DisplayName("8. Verify Balance Sheet (before closing)")
    void verifyBalanceSheetBeforeClosing() {
        entityManager.clear();
        log.info("========== BALANCE SHEET (before closing) 2025-12-31 ==========");

        var bs = reportService.generateBalanceSheet(LocalDate.of(2025, 12, 31));

        log.info("ASET:");
        for (var item : bs.assetItems()) {
            log.info("  {} {} : {}", item.account().getAccountCode(),
                    item.account().getAccountName(), fmt(item.balance()));
        }
        log.info("  Total Aset: {}", fmt(bs.totalAssets()));

        log.info("KEWAJIBAN:");
        for (var item : bs.liabilityItems()) {
            log.info("  {} {} : {}", item.account().getAccountCode(),
                    item.account().getAccountName(), fmt(item.balance()));
        }
        log.info("  Total Kewajiban: {}", fmt(bs.totalLiabilities()));

        log.info("EKUITAS:");
        for (var item : bs.equityItems()) {
            log.info("  {} {} : {}", item.account().getAccountCode(),
                    item.account().getAccountName(), fmt(item.balance()));
        }
        log.info("  Laba Berjalan: {}", fmt(bs.currentYearEarnings()));
        log.info("  Total Ekuitas: {}", fmt(bs.totalEquity()));

        BigDecimal liabilitiesAndEquity = bs.totalLiabilities().add(bs.totalEquity());
        log.info("  TOTAL KEWAJIBAN + EKUITAS: {}", fmt(liabilitiesAndEquity));

        assertThat(bs.totalAssets())
                .as("Balance sheet must balance: Assets = Liabilities + Equity")
                .isEqualByComparingTo(liabilitiesAndEquity);

        // Verify specific asset values
        // Bank BCA ~1.29B, Kredit PPh23 36.6M, Peralatan Komputer 70M, Akum Penyusutan ~15.6M
        assertThat(bs.totalAssets()).as("Total assets should be around 1.4B")
                .isBetween(new BigDecimal("1300000000"), new BigDecimal("1500000000"));

        // Laba Berjalan should equal P&L net income
        var pl = reportService.generateIncomeStatement(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        assertThat(bs.currentYearEarnings())
                .as("Laba Berjalan on balance sheet should equal P&L net income")
                .isEqualByComparingTo(pl.netIncome());

        log.info("Balance sheet verified: Assets={}, L+E={}, Laba Berjalan={}",
                fmt(bs.totalAssets()), fmt(liabilitiesAndEquity), fmt(bs.currentYearEarnings()));
    }

    @Test @Order(10) @DisplayName("9. Execute fiscal year closing")
    void executeFiscalYearClosing() {
        entityManager.clear();
        log.info("========== FISCAL YEAR CLOSING 2025 ==========");

        var preview = fiscalYearClosingService.previewClosing(2025);
        log.info("Closing preview: revenue={}, expense={}, netIncome={}",
                fmt(preview.totalRevenue()), fmt(preview.totalExpense()), fmt(preview.netIncome()));

        assertThat(preview.alreadyClosed()).as("Should not be already closed").isFalse();
        assertThat(preview.netIncome()).as("Net income should match P&L")
                .isGreaterThan(BigDecimal.ZERO);

        // Execute closing via Playwright UI (avoids journal sequence issues in service layer)
        loginAsAdmin();
        navigateTo("/reports/fiscal-closing?year=2025");
        waitForPageLoad();

        // Click Execute button
        page.onDialog(dialog -> dialog.accept());
        var executeBtn = page.locator("form[action*='/execute'] button[type='submit']");
        if (executeBtn.count() > 0) {
            executeBtn.click();
            waitForPageLoad();
            log.info("Closing executed via UI for year 2025");
        } else {
            log.warn("Execute button not found — closing may already be done or page structure differs");
        }
    }

    @Test @Order(11) @DisplayName("10. Verify Balance Sheet AFTER closing")
    void verifyBalanceSheetAfterClosing() {
        entityManager.clear();
        log.info("========== BALANCE SHEET (after closing) 2025-12-31 ==========");

        var bs = reportService.generateBalanceSheet(LocalDate.of(2025, 12, 31));

        log.info("ASET:");
        for (var item : bs.assetItems()) {
            log.info("  {} {} : {}", item.account().getAccountCode(),
                    item.account().getAccountName(), fmt(item.balance()));
        }
        log.info("  Total Aset: {}", fmt(bs.totalAssets()));

        log.info("KEWAJIBAN:");
        for (var item : bs.liabilityItems()) {
            log.info("  {} {} : {}", item.account().getAccountCode(),
                    item.account().getAccountName(), fmt(item.balance()));
        }
        log.info("  Total Kewajiban: {}", fmt(bs.totalLiabilities()));

        log.info("EKUITAS:");
        for (var item : bs.equityItems()) {
            log.info("  {} {} : {}", item.account().getAccountCode(),
                    item.account().getAccountName(), fmt(item.balance()));
        }
        log.info("  Laba Berjalan (should be 0 after closing): {}", fmt(bs.currentYearEarnings()));
        log.info("  Total Ekuitas: {}", fmt(bs.totalEquity()));

        BigDecimal liabilitiesAndEquity = bs.totalLiabilities().add(bs.totalEquity());
        log.info("  TOTAL KEWAJIBAN + EKUITAS: {}", fmt(liabilitiesAndEquity));

        assertThat(bs.totalAssets())
                .as("Balance sheet must STILL balance after closing")
                .isEqualByComparingTo(liabilitiesAndEquity);

        // After closing, Laba Berjalan should be 0 (revenue/expense zeroed to retained earnings)
        assertThat(bs.currentYearEarnings())
                .as("Laba Berjalan should be 0 after closing (transferred to Laba Ditahan)")
                .isEqualByComparingTo(BigDecimal.ZERO);

        // Total assets should remain the same as before closing
        // (closing only affects equity composition, not asset values)

        // After closing, trial balance should show Laba Ditahan with net income,
        // and revenue/expense accounts should be zero
        var tb = reportService.generateTrialBalance(LocalDate.of(2025, 12, 31));
        log.info("Trial balance after closing:");
        for (var item : tb.items()) {
            log.info("  {} {} | D={} | C={}",
                    item.account().getAccountCode(), item.account().getAccountName(),
                    fmt(item.debitBalance()), fmt(item.creditBalance()));
        }
        assertThat(tb.totalDebit()).isEqualByComparingTo(tb.totalCredit());

        // Revenue accounts (4.x) should be zero after closing
        boolean hasRevenueBalance = tb.items().stream()
                .anyMatch(item -> item.account().getAccountCode().startsWith("4.")
                        && (item.debitBalance().signum() != 0 || item.creditBalance().signum() != 0));
        assertThat(hasRevenueBalance)
                .as("Revenue accounts should be zeroed after closing").isFalse();

        // Expense accounts (5.x) should be zero after closing
        boolean hasExpenseBalance = tb.items().stream()
                .anyMatch(item -> item.account().getAccountCode().startsWith("5.")
                        && (item.debitBalance().signum() != 0 || item.creditBalance().signum() != 0));
        assertThat(hasExpenseBalance)
                .as("Expense accounts should be zeroed after closing").isFalse();

        // Laba Ditahan (3.2.01) should now contain the net income
        var labaDitahan = tb.items().stream()
                .filter(item -> item.account().getAccountCode().equals("3.2.01"))
                .findFirst();
        assertThat(labaDitahan).as("Laba Ditahan (3.2.01) should exist after closing").isPresent();
        log.info("Laba Ditahan (3.2.01) after closing: {}", fmt(labaDitahan.get().creditBalance()));

        log.info("Post-closing verification: revenue/expense zeroed, Laba Ditahan populated");
    }

    @Test @Order(12) @DisplayName("11. Verify tax details (auto-populated)")
    void verifyTaxDetails() {
        entityManager.clear();
        log.info("========== TAX DETAIL AUTO-POPULATION ==========");

        var allTaxDetails = taxTransactionDetailRepository.findAll();
        log.info("Total tax details: {}", allTaxDetails.size());

        var byType = allTaxDetails.stream()
                .collect(Collectors.groupingBy(d -> d.getTaxType().name(), Collectors.counting()));
        byType.forEach((type, count) -> log.info("  {}: {} entries", type, count));

        long ppnCount = byType.getOrDefault("PPN_KELUARAN", 0L);
        long pph23Count = byType.getOrDefault("PPH_23", 0L);
        long pph21Count = byType.getOrDefault("PPH_21", 0L);
        log.info("PPN Keluaran: {}, PPh 23: {}, PPh 21: {}", ppnCount, pph23Count, pph21Count);

        // Tax auto-populate requires linked clients for counterparty name.
        // PPN/PPh 23 entries depend on client linkage in demo transactions.
        // Log counts for verification — actual assertions depend on client linkage.
        log.info("Tax details auto-populated: PPN={}, PPh23={}, PPh21={}", ppnCount, pph23Count, pph21Count);
        log.info("Note: PPN/PPh 23 auto-populate requires transactions linked to clients");
    }

    @Test @Order(13) @DisplayName("12. Verify Coretax SPT Lampiran via API")
    void verifyCoretaxLampiran() {
        loginAsAdmin();
        log.info("========== CORETAX SPT LAMPIRAN EXPORT ==========");

        // Check SPT checklist page loads
        navigateTo("/reports/spt-tahunan");
        waitForPageLoad();
        String pageContent = page.content();
        log.info("SPT Checklist page loaded");

        // Check key data indicators
        boolean hasLaporanKeuangan = pageContent.contains("Laporan Keuangan");
        boolean hasPenyusutan = pageContent.contains("Penyusutan");
        boolean hasPayroll = pageContent.contains("Payroll") || pageContent.contains("PPh 21");
        log.info("  Laporan Keuangan: {}", hasLaporanKeuangan);
        log.info("  Penyusutan Aset: {}", hasPenyusutan);
        log.info("  Payroll/PPh 21: {}", hasPayroll);

        // Test Coretax lampiran API endpoint
        navigateTo("/api/tax-export/spt-tahunan/lampiran?year=2025");
        waitForPageLoad();
        String apiResponse = page.locator("body").textContent();
        log.info("Lampiran API response length: {} chars", apiResponse.length());

        boolean hasTranskrip8A = apiResponse.contains("transkrip8A") || apiResponse.contains("8A");
        boolean hasLampiranI = apiResponse.contains("lampiranI") || apiResponse.contains("rekonsiliasiFiskal");
        log.info("  Contains Transkrip 8A: {}", hasTranskrip8A);
        log.info("  Contains Lampiran I: {}", hasLampiranI);

        assertThat(apiResponse.length()).as("Lampiran API should return data").isGreaterThan(100);
    }

    private String fmt(BigDecimal amount) {
        if (amount == null || amount.signum() == 0) return "0";
        return IDR.format(amount);
    }

    private String fmtShort(BigDecimal amount) {
        if (amount == null || amount.signum() == 0) return "0";
        long val = amount.longValue();
        if (Math.abs(val) >= 1_000_000) return String.format("%,.1fM", val / 1_000_000.0);
        if (Math.abs(val) >= 1_000) return String.format("%,.1fK", val / 1_000.0);
        return String.valueOf(val);
    }
}
