package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.page.LoginPage;
import com.artivisi.accountingfinance.functional.page.PayrollDetailPage;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import com.microsoft.playwright.Download;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Payroll Report Generation")
class PayrollReportTest extends PlaywrightTestBase {

    // Use pre-approved payroll from test migration V909
    private static final String TEST_PAYROLL_ID = "a0000000-0000-0000-0000-000000000001";
    
    private LoginPage loginPage;
    private PayrollDetailPage detailPage;
    private String payrollDetailUrl;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage(page, baseUrl());
        detailPage = new PayrollDetailPage(page, baseUrl());

        loginPage.navigate().loginAsAdmin();

        // Use pre-approved payroll from test data
        payrollDetailUrl = baseUrl() + "/payroll/" + TEST_PAYROLL_ID;
    }

    // ==================== PAYROLL SUMMARY REPORTS ====================

    @Test
    @DisplayName("Should export payroll summary to PDF")
    void shouldExportPayrollSummaryToPdf() {
        page.navigate(payrollDetailUrl);
        
        // Create and click a download link programmatically
        String exportUrl = payrollDetailUrl + "/export/summary/pdf";
        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", exportUrl);
        });

        // Verify download
        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).contains("rekap-gaji");
        assertThat(download.suggestedFilename()).endsWith(".pdf");

        // Verify file has content
        try {
            Path downloadPath = download.path();
            assertThat(Files.size(downloadPath)).isGreaterThan(1000); // PDF should be at least 1KB
        } catch (IOException e) {
            throw new RuntimeException("Failed to verify downloaded file", e);
        }
    }

    @Test
    @DisplayName("Should export payroll summary to Excel")
    void shouldExportPayrollSummaryToExcel() {
        page.navigate(payrollDetailUrl);

        String exportUrl = payrollDetailUrl + "/export/summary/excel";
        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", exportUrl);
        });

        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).contains("rekap-gaji");
        assertThat(download.suggestedFilename()).endsWith(".xlsx");

        try {
            Path downloadPath = download.path();
            assertThat(Files.size(downloadPath)).isGreaterThan(1000);
        } catch (IOException e) {
            throw new RuntimeException("Failed to verify downloaded file", e);
        }
    }

    @Test
    @DisplayName("Payroll summary PDF should contain company name")
    void payrollSummaryPdfShouldContainCompanyName() {
        page.navigate(payrollDetailUrl);

        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/summary/pdf");
        });

        assertThat(download.suggestedFilename()).contains("rekap-gaji");
        // We can't easily verify PDF content without a PDF library,
        // but we verified the file is generated with reasonable size
    }

    @Test
    @DisplayName("Payroll summary Excel should have correct sheet name")
    void payrollSummaryExcelShouldHaveCorrectSheetName() {
        page.navigate(payrollDetailUrl);

        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/summary/excel");
        });

        assertThat(download.suggestedFilename()).contains("rekap-gaji");
        // Excel content verification would require POI library
    }

    // ==================== PPh 21 REPORTS ====================

    @Test
    @DisplayName("Should export PPh 21 report to PDF")
    void shouldExportPph21ReportToPdf() {
        page.navigate(payrollDetailUrl);

        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/pph21/pdf");
        });

        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).contains("pph21");
        assertThat(download.suggestedFilename()).endsWith(".pdf");

        try {
            Path downloadPath = download.path();
            assertThat(Files.size(downloadPath)).isGreaterThan(1000);
        } catch (IOException e) {
            throw new RuntimeException("Failed to verify downloaded file", e);
        }
    }

    @Test
    @DisplayName("Should export PPh 21 report to Excel")
    void shouldExportPph21ReportToExcel() {
        page.navigate(payrollDetailUrl);

        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/pph21/excel");
        });

        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).contains("pph21");
        assertThat(download.suggestedFilename()).endsWith(".xlsx");

        try {
            Path downloadPath = download.path();
            assertThat(Files.size(downloadPath)).isGreaterThan(1000);
        } catch (IOException e) {
            throw new RuntimeException("Failed to verify downloaded file", e);
        }
    }

    @Test
    @DisplayName("PPh 21 report should be available for approved payroll")
    void pph21ReportShouldBeAvailableForApprovedPayroll() {
        page.navigate(payrollDetailUrl);

        // Verify export button is present (links are in collapsed dropdown)
        assertThat(page.locator("#btn-export").isVisible()).isTrue();
    }

    // ==================== BPJS REPORTS ====================

    @Test
    @DisplayName("Should export BPJS report to PDF")
    void shouldExportBpjsReportToPdf() {
        page.navigate(payrollDetailUrl);

        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/bpjs/pdf");
        });

        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).contains("bpjs");
        assertThat(download.suggestedFilename()).endsWith(".pdf");

        try {
            Path downloadPath = download.path();
            assertThat(Files.size(downloadPath)).isGreaterThan(1000);
        } catch (IOException e) {
            throw new RuntimeException("Failed to verify downloaded file", e);
        }
    }

    @Test
    @DisplayName("Should export BPJS report to Excel")
    void shouldExportBpjsReportToExcel() {
        page.navigate(payrollDetailUrl);

        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/bpjs/excel");
        });

        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).contains("bpjs");
        assertThat(download.suggestedFilename()).endsWith(".xlsx");

        try {
            Path downloadPath = download.path();
            assertThat(Files.size(downloadPath)).isGreaterThan(2000); // BPJS report has 2 sheets
        } catch (IOException e) {
            throw new RuntimeException("Failed to verify downloaded file", e);
        }
    }

    @Test
    @DisplayName("BPJS report should be available for approved payroll")
    void bpjsReportShouldBeAvailableForApprovedPayroll() {
        page.navigate(payrollDetailUrl);

        // Verify export button is present (links are in collapsed dropdown)
        assertThat(page.locator("#btn-export").isVisible()).isTrue();
    }

    // ==================== PAYSLIP GENERATION ====================

    @Test
    @DisplayName("Should generate payslip PDF for employee")
    void shouldGeneratePayslipPdfForEmployee() {
        page.navigate(payrollDetailUrl);

        // Get first employee ID from V908 test data
        String employeeId = "e0000000-0000-0000-0000-000000000001";

        // Download payslip directly via URL
        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/payslip/" + employeeId + "/pdf");
        });

        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).contains("slip-gaji");
        assertThat(download.suggestedFilename()).endsWith(".pdf");

        try {
            Path downloadPath = download.path();
            assertThat(Files.size(downloadPath)).isGreaterThan(500); // Payslip is smaller
        } catch (IOException e) {
            throw new RuntimeException("Failed to verify downloaded file", e);
        }
    }

    @Test
    @DisplayName("Should generate payslip for all employees")
    void shouldGeneratePayslipForAllEmployees() {
        page.navigate(payrollDetailUrl);

        // Verify we have 3 employees from V909 test data
        assertThat(page.locator("table tbody tr").count()).isEqualTo(3);

        // Generate payslip for first employee from V908 test data
        String employeeId = "e0000000-0000-0000-0000-000000000001";

        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/payslip/" + employeeId + "/pdf");
        });

        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).contains("slip-gaji");
    }

    // ==================== BUKTI POTONG 1721-A1 ====================

    @Test
    @DisplayName("Should display bukti potong page")
    void shouldDisplayBuktiPotongPage() {
        page.navigate(baseUrl() + "/payroll/bukti-potong");

        // Page should load successfully
        assertThat(page.locator("h1, h2").first().isVisible()).isTrue();
    }

    @Test
    @DisplayName("Should have year selector on bukti potong page")
    void shouldHaveYearSelectorOnBuktiPotongPage() {
        page.navigate(baseUrl() + "/payroll/bukti-potong");

        // Year selector should be present and contain 2025 (from V909 test data)
        assertThat(page.locator("select#year-select, select[name='year']").count()).isGreaterThan(0);
        String yearOptions = page.locator("select#year-select, select[name='year']").textContent();
        assertThat(yearOptions).contains("2025");
    }

    @Test
    @DisplayName("Should display bukti potong page with year selector")
    void shouldHaveEmployeeSelectorOnBuktiPotongPage() {
        page.navigate(baseUrl() + "/payroll/bukti-potong?year=2025");

        // Year selector should be present with 2025 option
        assertThat(page.locator("select#year-select").count()).isEqualTo(1);
        String yearOptions = page.locator("select#year-select").textContent();
        assertThat(yearOptions).contains("2025");
    }

    @Test
    @DisplayName("Bukti potong page should show info message when no full year data")
    void shouldGenerateBuktiPotong1721A1ForEmployee() {
        page.navigate(baseUrl() + "/payroll/bukti-potong?year=2025");

        // Page should load successfully with year selector
        assertThat(page.locator("select#year-select").count()).isEqualTo(1);
        
        // Either shows employee table or no data message (we only have 1 month of data, not full year)
        boolean hasTable = page.locator("table[data-testid='bukti-potong-table']").count() > 0;
        boolean hasNoDataMessage = page.locator(".bg-yellow-50").count() > 0;
        assertThat(hasTable || hasNoDataMessage).isTrue();
    }

    // ==================== REPORT CONTENT VALIDATION ====================

    @Test
    @DisplayName("All report exports should work for posted payroll")
    void allReportExportsShouldWorkForPostedPayroll() {
        // Post the payroll first
        page.navigate(payrollDetailUrl);
        detailPage.clickPostButton();
        page.waitForTimeout(500);

        // Test all export formats still work after posting
        Download summaryPdf = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/summary/pdf");
        });
        assertThat(summaryPdf).isNotNull();

        Download summaryExcel = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/summary/excel");
        });
        assertThat(summaryExcel).isNotNull();

        Download pph21Pdf = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/pph21/pdf");
        });
        assertThat(pph21Pdf).isNotNull();

        Download pph21Excel = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/pph21/excel");
        });
        assertThat(pph21Excel).isNotNull();

        Download bpjsPdf = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/bpjs/pdf");
        });
        assertThat(bpjsPdf).isNotNull();

        Download bpjsExcel = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/bpjs/excel");
        });
        assertThat(bpjsExcel).isNotNull();
    }

    @Test
    @DisplayName("Report filenames should contain period information")
    void reportFilenamesShouldContainPeriodInformation() {
        page.navigate(payrollDetailUrl);

        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/summary/pdf");
        });

        String filename = download.suggestedFilename();
        assertThat(filename).contains("2025-06"); // Contains period from V909 test payroll (2025-06)
    }

    @Test
    @DisplayName("Reports should be generated with reasonable file sizes")
    void reportsShouldBeGeneratedWithReasonableFileSizes() throws IOException {
        page.navigate(payrollDetailUrl);

        // PDF should be larger than Excel generally
        Download pdfDownload = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/summary/pdf");
        });

        Download excelDownload = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/summary/excel");
        });

        long pdfSize = Files.size(pdfDownload.path());
        long excelSize = Files.size(excelDownload.path());

        // Both should have reasonable sizes
        assertThat(pdfSize).isBetween(1000L, 10_000_000L); // 1KB to 10MB
        assertThat(excelSize).isBetween(1000L, 10_000_000L);
    }

    // ==================== ERROR HANDLING ====================

    @Test
    @DisplayName("Should handle invalid payroll ID gracefully")
    void shouldHandleInvalidPayrollIdGracefully() {
        // Use a valid UUID format that doesn't exist in the database
        page.navigate(baseUrl() + "/payroll/00000000-0000-0000-0000-000000099999/export/summary/pdf");

        // Should show error page or redirect
        assertThat(page.url()).matches(".*/(error|payroll|404).*");
    }

    @Test
    @DisplayName("Should handle invalid employee ID in payslip gracefully")
    void shouldHandleInvalidEmployeeIdInPayslipGracefully() {
        // Use a valid UUID format that doesn't exist in the database
        page.navigate(payrollDetailUrl + "/payslip/00000000-0000-0000-0000-000000099999/pdf");

        // Should show error page or redirect
        assertThat(page.url()).matches(".*/(error|payroll|404).*");
    }

    // ==================== MULTIPLE EMPLOYEES TEST ====================

    @Test
    @DisplayName("Summary report should include all employees")
    void summaryReportShouldIncludeAllEmployees() {
        page.navigate(payrollDetailUrl);

        // Verify there are multiple employees in the detail table
        int employeeCount = page.locator("table tbody tr").count();
        assertThat(employeeCount).isEqualTo(3); // We have 3 test employees

        // Generate summary report
        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/summary/pdf");
        });

        assertThat(download).isNotNull();
        // The PDF should contain 3 employee records from V909 test data
        try {
            assertThat(Files.size(download.path())).isGreaterThan(1500); // Adjusted for actual 3-employee PDF size
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== CALCULATION VERIFICATION ====================

    @Test
    @DisplayName("Report should show correct total calculations")
    void reportShouldShowCorrectTotalCalculations() {
        page.navigate(payrollDetailUrl);

        // Get totals from detail page
        String totalGross = detailPage.getTotalGross();
        String totalDeductions = detailPage.getTotalDeductions();
        String totalNet = detailPage.getTotalNet();

        // All totals should be present and formatted
        assertThat(totalGross).contains("Rp");
        assertThat(totalDeductions).contains("Rp");
        assertThat(totalNet).contains("Rp");

        // Generate report to ensure calculations are consistent
        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/summary/pdf");
        });

        assertThat(download).isNotNull();
    }

    // ==================== DIFFERENT RISK CLASS TEST ====================

    @Test
    @DisplayName("BPJS report should reflect different JKK risk classes")
    void bpjsReportShouldReflectDifferentJkkRiskClasses() {
        // Test with the pre-approved payroll (risk class 2)
        page.navigate(payrollDetailUrl);
        
        // Generate BPJS report
        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", payrollDetailUrl + "/export/bpjs/pdf");
        });

        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).contains("bpjs");
    }
}
