package com.artivisi.accountingfinance.service;

import com.artivisi.accountingfinance.entity.ChartOfAccount;
import com.artivisi.accountingfinance.entity.JournalEntry;
import com.artivisi.accountingfinance.enums.JournalEntryStatus;
import com.artivisi.accountingfinance.repository.ChartOfAccountRepository;
import com.artivisi.accountingfinance.repository.JournalEntryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for fiscal year closing operations.
 * Creates closing entries to transfer income/expense balances to retained earnings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FiscalYearClosingService {

    // Account codes - should match COA
    private static final String LABA_BERJALAN_CODE = "3.2.02";
    private static final String LABA_DITAHAN_CODE = "3.2.01";

    private final JournalEntryRepository journalEntryRepository;
    private final ChartOfAccountRepository chartOfAccountRepository;
    private final ReportService reportService;

    /**
     * Check if closing entries already exist for a year.
     */
    public boolean hasClosingEntries(int year) {
        String referencePattern = "CLOSING-" + year + "-%";
        return journalEntryRepository.countByReferenceNumberLike(referencePattern) > 0;
    }

    /**
     * Get existing closing entries for a year.
     */
    public List<JournalEntry> getClosingEntries(int year) {
        String referencePattern = "CLOSING-" + year + "-%";
        return journalEntryRepository.findByReferenceNumberLike(referencePattern);
    }

    /**
     * Preview closing entries without saving.
     * Returns what would be created if executeClosing() is called.
     */
    public ClosingPreview previewClosing(int year) {
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        // Get income statement for the year
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        ReportService.IncomeStatementReport incomeStatement =
                reportService.generateIncomeStatement(yearStart, yearEnd);

        List<ClosingEntryPreview> entries = new ArrayList<>();

        // Entry 1: Close Revenue to Laba Berjalan
        if (incomeStatement.totalRevenue().compareTo(BigDecimal.ZERO) != 0) {
            entries.add(new ClosingEntryPreview(
                    "CLOSING-" + year + "-01",
                    "Tutup Pendapatan ke Laba Berjalan",
                    yearEnd,
                    createRevenueClosingLines(incomeStatement, year)
            ));
        }

        // Entry 2: Close Expenses to Laba Berjalan
        if (incomeStatement.totalExpense().compareTo(BigDecimal.ZERO) != 0) {
            entries.add(new ClosingEntryPreview(
                    "CLOSING-" + year + "-02",
                    "Tutup Beban ke Laba Berjalan",
                    yearEnd,
                    createExpenseClosingLines(incomeStatement, year)
            ));
        }

        // Entry 3: Transfer Laba Berjalan to Laba Ditahan
        BigDecimal netIncome = incomeStatement.netIncome();
        if (netIncome.compareTo(BigDecimal.ZERO) != 0) {
            entries.add(new ClosingEntryPreview(
                    "CLOSING-" + year + "-03",
                    "Transfer Laba Berjalan ke Laba Ditahan",
                    yearEnd,
                    createRetainedEarningsLines(netIncome, year)
            ));
        }

        return new ClosingPreview(
                year,
                incomeStatement.totalRevenue(),
                incomeStatement.totalExpense(),
                incomeStatement.netIncome(),
                entries,
                hasClosingEntries(year)
        );
    }

    /**
     * Execute year-end closing.
     * Creates closing journal entries to transfer income/expense to retained earnings.
     */
    @Transactional
    public List<JournalEntry> executeClosing(int year) {
        // Check for duplicate closing
        if (hasClosingEntries(year)) {
            throw new IllegalStateException(
                    "Jurnal penutup untuk tahun " + year + " sudah ada. " +
                    "Hapus jurnal penutup yang ada terlebih dahulu jika ingin membuat ulang.");
        }

        LocalDate yearEnd = LocalDate.of(year, 12, 31);
        LocalDate yearStart = LocalDate.of(year, 1, 1);

        // Get income statement for the year
        ReportService.IncomeStatementReport incomeStatement =
                reportService.generateIncomeStatement(yearStart, yearEnd);

        List<JournalEntry> closingEntries = new ArrayList<>();
        String username = getCurrentUsername();

        // Get required accounts
        ChartOfAccount labaBerjalan = chartOfAccountRepository.findByAccountCode(LABA_BERJALAN_CODE)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Akun Laba Berjalan (" + LABA_BERJALAN_CODE + ") tidak ditemukan"));
        ChartOfAccount labaDitahan = chartOfAccountRepository.findByAccountCode(LABA_DITAHAN_CODE)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Akun Laba Ditahan (" + LABA_DITAHAN_CODE + ") tidak ditemukan"));

        // Entry 1: Close Revenue accounts to Laba Berjalan
        if (incomeStatement.totalRevenue().compareTo(BigDecimal.ZERO) != 0) {
            String journalNumber = generateJournalNumber(yearEnd, "01");

            // Debit each revenue account (to zero it out)
            for (ReportService.IncomeStatementItem item : incomeStatement.revenueItems()) {
                if (item.balance().compareTo(BigDecimal.ZERO) != 0) {
                    JournalEntry entry = createEntry(
                            journalNumber,
                            yearEnd,
                            item.account(),
                            item.balance(),
                            BigDecimal.ZERO,
                            "Tutup Pendapatan ke Laba Berjalan - Tahun " + year,
                            "CLOSING-" + year + "-01",
                            username
                    );
                    closingEntries.add(journalEntryRepository.save(entry));
                }
            }

            // Credit Laba Berjalan
            JournalEntry creditEntry = createEntry(
                    journalNumber,
                    yearEnd,
                    labaBerjalan,
                    BigDecimal.ZERO,
                    incomeStatement.totalRevenue(),
                    "Tutup Pendapatan ke Laba Berjalan - Tahun " + year,
                    "CLOSING-" + year + "-01",
                    username
            );
            closingEntries.add(journalEntryRepository.save(creditEntry));

            log.info("Created revenue closing entry for year {}: {}", year, incomeStatement.totalRevenue());
        }

        // Entry 2: Close Expense accounts to Laba Berjalan
        if (incomeStatement.totalExpense().compareTo(BigDecimal.ZERO) != 0) {
            String journalNumber = generateJournalNumber(yearEnd, "02");

            // Credit each expense account (to zero it out)
            for (ReportService.IncomeStatementItem item : incomeStatement.expenseItems()) {
                if (item.balance().compareTo(BigDecimal.ZERO) != 0) {
                    JournalEntry entry = createEntry(
                            journalNumber,
                            yearEnd,
                            item.account(),
                            BigDecimal.ZERO,
                            item.balance(),
                            "Tutup Beban ke Laba Berjalan - Tahun " + year,
                            "CLOSING-" + year + "-02",
                            username
                    );
                    closingEntries.add(journalEntryRepository.save(entry));
                }
            }

            // Debit Laba Berjalan
            JournalEntry debitEntry = createEntry(
                    journalNumber,
                    yearEnd,
                    labaBerjalan,
                    incomeStatement.totalExpense(),
                    BigDecimal.ZERO,
                    "Tutup Beban ke Laba Berjalan - Tahun " + year,
                    "CLOSING-" + year + "-02",
                    username
            );
            closingEntries.add(journalEntryRepository.save(debitEntry));

            log.info("Created expense closing entry for year {}: {}", year, incomeStatement.totalExpense());
        }

        // Entry 3: Transfer Laba Berjalan to Laba Ditahan
        BigDecimal netIncome = incomeStatement.netIncome();
        if (netIncome.compareTo(BigDecimal.ZERO) != 0) {
            String journalNumber = generateJournalNumber(yearEnd, "03");

            if (netIncome.compareTo(BigDecimal.ZERO) > 0) {
                // Profit: Debit Laba Berjalan, Credit Laba Ditahan
                JournalEntry debitEntry = createEntry(
                        journalNumber,
                        yearEnd,
                        labaBerjalan,
                        netIncome,
                        BigDecimal.ZERO,
                        "Transfer Laba Berjalan ke Laba Ditahan - Tahun " + year,
                        "CLOSING-" + year + "-03",
                        username
                );
                closingEntries.add(journalEntryRepository.save(debitEntry));

                JournalEntry creditEntry = createEntry(
                        journalNumber,
                        yearEnd,
                        labaDitahan,
                        BigDecimal.ZERO,
                        netIncome,
                        "Transfer Laba Berjalan ke Laba Ditahan - Tahun " + year,
                        "CLOSING-" + year + "-03",
                        username
                );
                closingEntries.add(journalEntryRepository.save(creditEntry));
            } else {
                // Loss: Credit Laba Berjalan, Debit Laba Ditahan
                BigDecimal loss = netIncome.abs();
                JournalEntry creditEntry = createEntry(
                        journalNumber,
                        yearEnd,
                        labaBerjalan,
                        BigDecimal.ZERO,
                        loss,
                        "Transfer Rugi Berjalan ke Laba Ditahan - Tahun " + year,
                        "CLOSING-" + year + "-03",
                        username
                );
                closingEntries.add(journalEntryRepository.save(creditEntry));

                JournalEntry debitEntry = createEntry(
                        journalNumber,
                        yearEnd,
                        labaDitahan,
                        loss,
                        BigDecimal.ZERO,
                        "Transfer Rugi Berjalan ke Laba Ditahan - Tahun " + year,
                        "CLOSING-" + year + "-03",
                        username
                );
                closingEntries.add(journalEntryRepository.save(debitEntry));
            }

            log.info("Created retained earnings transfer for year {}: {}", year, netIncome);
        }

        log.info("Completed fiscal year closing for {}: {} entries created", year, closingEntries.size());
        return closingEntries;
    }

    /**
     * Reverse (void) closing entries for a year.
     */
    @Transactional
    public int reverseClosing(int year, String reason) {
        List<JournalEntry> closingEntries = getClosingEntries(year);

        if (closingEntries.isEmpty()) {
            throw new IllegalStateException("Tidak ada jurnal penutup untuk tahun " + year);
        }

        String username = getCurrentUsername();
        int count = 0;

        for (JournalEntry entry : closingEntries) {
            if (entry.getStatus() == JournalEntryStatus.POSTED) {
                entry.setStatus(JournalEntryStatus.VOID);
                entry.setVoidReason("Pembatalan Jurnal Penutup: " + reason);
                entry.setVoidedAt(LocalDateTime.now());
                journalEntryRepository.save(entry);
                count++;
            }
        }

        log.info("Reversed {} closing entries for year {}", count, year);
        return count;
    }

    // Helper methods

    private String generateJournalNumber(LocalDate date, String suffix) {
        // Format: JV-YYYYMMDD-XXXX where XXXX is sequence
        String prefix = "JV-" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        Integer maxSeq = journalEntryRepository.findMaxSequenceByPrefix(prefix + "%");
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        return prefix + String.format("%04d", nextSeq);
    }

    private JournalEntry createEntry(String journalNumber, LocalDate date,
            ChartOfAccount account, BigDecimal debit, BigDecimal credit,
            String description, String referenceNumber, String username) {
        JournalEntry entry = new JournalEntry();
        entry.setJournalNumber(journalNumber);
        entry.setJournalDate(date);
        entry.setAccount(account);
        entry.setDebitAmount(debit);
        entry.setCreditAmount(credit);
        entry.setDescription(description);
        entry.setReferenceNumber(referenceNumber);
        entry.setStatus(JournalEntryStatus.POSTED);
        entry.setPostedAt(LocalDateTime.now());
        entry.setCreatedBy(username);
        return entry;
    }

    private List<ClosingLinePreview> createRevenueClosingLines(
            ReportService.IncomeStatementReport incomeStatement, int year) {
        List<ClosingLinePreview> lines = new ArrayList<>();

        for (ReportService.IncomeStatementItem item : incomeStatement.revenueItems()) {
            if (item.balance().compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ClosingLinePreview(
                        item.account().getAccountCode(),
                        item.account().getAccountName(),
                        item.balance(),
                        BigDecimal.ZERO,
                        "Tutup " + item.account().getAccountName()
                ));
            }
        }

        lines.add(new ClosingLinePreview(
                LABA_BERJALAN_CODE,
                "Laba Berjalan",
                BigDecimal.ZERO,
                incomeStatement.totalRevenue(),
                "Total Pendapatan Tahun " + year
        ));

        return lines;
    }

    private List<ClosingLinePreview> createExpenseClosingLines(
            ReportService.IncomeStatementReport incomeStatement, int year) {
        List<ClosingLinePreview> lines = new ArrayList<>();

        for (ReportService.IncomeStatementItem item : incomeStatement.expenseItems()) {
            if (item.balance().compareTo(BigDecimal.ZERO) != 0) {
                lines.add(new ClosingLinePreview(
                        item.account().getAccountCode(),
                        item.account().getAccountName(),
                        BigDecimal.ZERO,
                        item.balance(),
                        "Tutup " + item.account().getAccountName()
                ));
            }
        }

        lines.add(new ClosingLinePreview(
                LABA_BERJALAN_CODE,
                "Laba Berjalan",
                incomeStatement.totalExpense(),
                BigDecimal.ZERO,
                "Total Beban Tahun " + year
        ));

        return lines;
    }

    private List<ClosingLinePreview> createRetainedEarningsLines(BigDecimal netIncome, int year) {
        List<ClosingLinePreview> lines = new ArrayList<>();

        if (netIncome.compareTo(BigDecimal.ZERO) > 0) {
            lines.add(new ClosingLinePreview(
                    LABA_BERJALAN_CODE,
                    "Laba Berjalan",
                    netIncome,
                    BigDecimal.ZERO,
                    "Laba Bersih Tahun " + year
            ));
            lines.add(new ClosingLinePreview(
                    LABA_DITAHAN_CODE,
                    "Laba Ditahan",
                    BigDecimal.ZERO,
                    netIncome,
                    "Transfer ke Laba Ditahan"
            ));
        } else {
            BigDecimal loss = netIncome.abs();
            lines.add(new ClosingLinePreview(
                    LABA_BERJALAN_CODE,
                    "Laba Berjalan",
                    BigDecimal.ZERO,
                    loss,
                    "Rugi Bersih Tahun " + year
            ));
            lines.add(new ClosingLinePreview(
                    LABA_DITAHAN_CODE,
                    "Laba Ditahan",
                    loss,
                    BigDecimal.ZERO,
                    "Transfer Rugi ke Laba Ditahan"
            ));
        }

        return lines;
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // Record classes for preview data

    public record ClosingPreview(
            int year,
            BigDecimal totalRevenue,
            BigDecimal totalExpense,
            BigDecimal netIncome,
            List<ClosingEntryPreview> entries,
            boolean alreadyClosed
    ) {}

    public record ClosingEntryPreview(
            String referenceNumber,
            String description,
            LocalDate date,
            List<ClosingLinePreview> lines
    ) {}

    public record ClosingLinePreview(
            String accountCode,
            String accountName,
            BigDecimal debit,
            BigDecimal credit,
            String memo
    ) {}
}
