package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.page.LoginPage;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Document Storage and Attachment")
class DocumentStorageTest extends PlaywrightTestBase {

    // Use POSTED test transaction from V904 test migration
    // (avoiding DRAFT transaction 001 which is used by DocumentAttachmentTest for empty state check)
    private static final String TEST_TRANSACTION_ID = "a0000000-0000-0000-0000-000000000002";
    // Use test invoice from V906 test migration (uses invoice number, not UUID)
    private static final String TEST_INVOICE_NUMBER = "INV-2024-001";

    private LoginPage loginPage;
    private String transactionDetailUrl;
    private String invoiceDetailUrl;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage(page, baseUrl());
        loginPage.navigate().loginAsAdmin();

        transactionDetailUrl = baseUrl() + "/transactions/" + TEST_TRANSACTION_ID;
        invoiceDetailUrl = baseUrl() + "/invoices/" + TEST_INVOICE_NUMBER;
    }

    // ==================== DOCUMENT UPLOAD TESTS ====================

    @Test
    @DisplayName("Should upload JPG image to transaction")
    void shouldUploadJpgImageToTransaction() throws IOException {
        // Create a test JPG file
        Path testFile = createTestJpgFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();

        // Upload the file
        uploadFileToDocumentSection(testFile);

        // Verify document appears in list
        Locator documentList = page.locator("#document-list-container");
        assertThat(documentList.locator("text=test-image.jpg").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should upload PNG image to transaction")
    void shouldUploadPngImageToTransaction() throws IOException {
        Path testFile = createTestPngFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();

        uploadFileToDocumentSection(testFile);

        Locator documentList = page.locator("#document-list-container");
        assertThat(documentList.locator("text=test-image.png").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should upload PDF document to transaction")
    void shouldUploadPdfDocumentToTransaction() throws IOException {
        Path testFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();

        uploadFileToDocumentSection(testFile);

        Locator documentList = page.locator("#document-list-container");
        assertThat(documentList.locator("text=test-document.pdf").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should upload multiple documents to same transaction")
    void shouldUploadMultipleDocumentsToSameTransaction() throws IOException {
        Path jpgFile = createTestJpgFile();
        Path pdfFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();

        // Upload first file
        uploadFileToDocumentSection(jpgFile);
        page.waitForTimeout(500);

        // Upload second file
        uploadFileToDocumentSection(pdfFile);
        page.waitForTimeout(500);

        // Verify both documents appear
        Locator documentList = page.locator("#document-list-container");
        assertThat(documentList.locator("text=test-image.jpg").count()).isGreaterThan(0);
        assertThat(documentList.locator("text=test-document.pdf").count()).isGreaterThan(0);
    }

    // ==================== DOCUMENT VIEW/DOWNLOAD TESTS ====================

    @Test
    @DisplayName("Should view uploaded document")
    void shouldViewUploadedDocument() throws IOException {
        Path testFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();
        uploadFileToDocumentSection(testFile);

        // Wait for document to appear
        page.waitForTimeout(500);

        // Click view button
        Locator viewButton = page.locator("#document-list-container a[title='Lihat']").first();
        assertThat(viewButton.isVisible()).isTrue();

        // View should open in new tab (we just verify the link exists and is correct)
        String viewHref = viewButton.getAttribute("href");
        assertThat(viewHref).contains("/documents/");
        assertThat(viewHref).contains("/view");
    }

    @Test
    @DisplayName("Should download uploaded document")
    void shouldDownloadUploadedDocument() throws IOException {
        Path testFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();
        uploadFileToDocumentSection(testFile);
        page.waitForTimeout(500);

        // Get download link
        Locator downloadButton = page.locator("#document-list-container a[title='Unduh']").first();
        assertThat(downloadButton.isVisible()).isTrue();

        String downloadHref = downloadButton.getAttribute("href");

        // Download the file
        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", downloadHref);
        });

        assertThat(download).isNotNull();
        assertThat(download.suggestedFilename()).isEqualTo("test-document.pdf");

        // Verify downloaded file has content
        assertThat(Files.size(download.path())).isGreaterThan(0);
    }

    @Test
    @DisplayName("Downloaded file should match original file size")
    void downloadedFileShouldMatchOriginalFileSize() throws IOException {
        Path testFile = createTestPdfFile();
        long originalSize = Files.size(testFile);

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();
        uploadFileToDocumentSection(testFile);
        page.waitForTimeout(500);

        Locator downloadButton = page.locator("#document-list-container a[title='Unduh']").first();
        String downloadHref = downloadButton.getAttribute("href");

        Download download = page.waitForDownload(() -> {
            page.evaluate("url => { window.location.href = url; }", downloadHref);
        });

        assertThat(Files.size(download.path())).isEqualTo(originalSize);
    }

    // ==================== DOCUMENT DELETE TESTS ====================

    @Test
    @DisplayName("Should delete uploaded document")
    void shouldDeleteUploadedDocument() throws IOException {
        Path testFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();
        uploadFileToDocumentSection(testFile);
        page.waitForTimeout(500);

        // Verify document exists
        Locator documentList = page.locator("#document-list-container");
        assertThat(documentList.locator("text=test-document.pdf").count()).isGreaterThan(0);

        // Click delete button (need to hover first to make it visible)
        Locator documentRow = documentList.locator("div.group").first();
        documentRow.hover();

        // Handle confirm dialog
        page.onDialog(dialog -> dialog.accept());

        Locator deleteButton = documentRow.locator("button[title='Hapus']");
        deleteButton.click();

        // Wait for HTMX to update
        page.waitForTimeout(500);

        // Verify document is removed or success message shown
        assertThat(page.locator("text=Dokumen berhasil dihapus").count() > 0 ||
                  page.locator("#document-list-container").locator("text=test-document.pdf").count() == 0).isTrue();
    }

    // ==================== INVOICE DOCUMENT TESTS ====================

    @Test
    @DisplayName("Should display invoice detail page correctly")
    void shouldDisplayInvoiceDetailPageCorrectly() {
        // Invoice detail page currently doesn't have document upload section
        // This test verifies the page loads and can be extended when document upload is added
        page.navigate(invoiceDetailUrl);
        waitForPageLoad();

        // Verify invoice detail page loads correctly
        Locator invoiceDetail = page.locator("[data-testid='invoice-detail']");
        assertThat(invoiceDetail.isVisible()).isTrue();

        // Verify invoice number is displayed
        String pageContent = page.content();
        assertThat(pageContent).contains(TEST_INVOICE_NUMBER);
    }

    // ==================== DOCUMENT METADATA TESTS ====================

    @Test
    @DisplayName("Document list should show file size")
    void documentListShouldShowFileSize() throws IOException {
        Path testFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();
        uploadFileToDocumentSection(testFile);
        page.waitForTimeout(500);

        // Verify file size is displayed
        Locator documentList = page.locator("#document-list-container");
        String documentInfo = documentList.textContent();

        // Should show KB or B for file size
        assertThat(documentInfo).containsAnyOf("KB", "MB", "B");
    }

    @Test
    @DisplayName("Document list should show upload timestamp")
    void documentListShouldShowUploadTimestamp() throws IOException {
        Path testFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();
        uploadFileToDocumentSection(testFile);
        page.waitForTimeout(500);

        // Verify timestamp is displayed (format: d MMM yyyy HH:mm)
        Locator documentList = page.locator("#document-list-container");
        String documentInfo = documentList.textContent();

        // Should show year (2024 or 2025)
        assertThat(documentInfo).containsAnyOf("2024", "2025");
    }

    @Test
    @DisplayName("Document list should show uploader name")
    void documentListShouldShowUploaderName() throws IOException {
        Path testFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();
        uploadFileToDocumentSection(testFile);
        page.waitForTimeout(500);

        // Verify uploader name is displayed
        Locator documentList = page.locator("#document-list-container");
        String documentInfo = documentList.textContent();

        // Should show admin (logged in user)
        assertThat(documentInfo).contains("admin");
    }

    @Test
    @DisplayName("Document list should show different icons for different file types")
    void documentListShouldShowDifferentIconsForDifferentFileTypes() throws IOException {
        Path jpgFile = createTestJpgFile();
        Path pdfFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();

        uploadFileToDocumentSection(jpgFile);
        page.waitForTimeout(500);
        uploadFileToDocumentSection(pdfFile);
        page.waitForTimeout(500);

        Locator documentList = page.locator("#document-list-container");

        // Verify both image and PDF icons are present
        // Image: blue icon (bg-blue-100)
        // PDF: red icon (bg-red-100)
        assertThat(documentList.locator(".bg-blue-100").count()).isGreaterThan(0);
        assertThat(documentList.locator(".bg-red-100").count()).isGreaterThan(0);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("Should show error for unsupported file type")
    void shouldShowErrorForUnsupportedFileType() throws IOException {
        Path testFile = createTestTxtFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();

        // Try to upload unsupported file type
        Locator fileInput = page.locator("#document-list-container input[type='file']");
        fileInput.setInputFiles(testFile);

        // Wait for HTMX response
        page.waitForTimeout(500);

        // Should show error message
        Locator documentContainer = page.locator("#document-list-container");
        String containerText = documentContainer.textContent();

        // Either shows error or the file is not accepted (input accept attribute filters)
        assertThat(containerText.contains("not allowed") ||
                  documentContainer.locator("text=test-file.txt").count() == 0).isTrue();
    }

    // Empty state test is covered by DocumentAttachmentTest

    @Test
    @DisplayName("Transaction detail page should load document section via HTMX")
    void transactionDetailPageShouldLoadDocumentSectionViaHtmx() {
        page.navigate(transactionDetailUrl);

        // Wait for HTMX to load document section
        page.waitForTimeout(1000);

        Locator documentSection = page.locator("#document-section");
        assertThat(documentSection.isVisible()).isTrue();

        // Document list container should be loaded
        Locator documentList = page.locator("#document-list-container");
        assertThat(documentList.count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should preserve document list after page refresh")
    void shouldPreserveDocumentListAfterPageRefresh() throws IOException {
        Path testFile = createTestPdfFile();

        page.navigate(transactionDetailUrl);
        waitForDocumentSection();
        uploadFileToDocumentSection(testFile);
        page.waitForTimeout(500);

        // Verify document exists
        assertThat(page.locator("#document-list-container").locator("text=test-document.pdf").count()).isGreaterThan(0);

        // Refresh page
        page.reload();
        waitForDocumentSection();

        // Document should still be visible
        assertThat(page.locator("#document-list-container").locator("text=test-document.pdf").count()).isGreaterThan(0);
    }

    // ==================== HELPER METHODS ====================

    private void waitForDocumentSection() {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        // Wait for HTMX to load document list
        page.waitForTimeout(1000);
        page.waitForSelector("#document-list-container",
            new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(5000));
    }

    private void uploadFileToDocumentSection(Path filePath) {
        Locator fileInput = page.locator("#document-list-container input[type='file']");
        fileInput.setInputFiles(filePath);
        // Wait for HTMX upload and swap
        page.waitForTimeout(1000);
    }

    private Path createTestJpgFile() throws IOException {
        Path file = tempDir.resolve("test-image.jpg");
        // Create a minimal valid JPEG file
        // JPEG magic bytes: FF D8 FF E0 00 10 4A 46 49 46 00 01
        byte[] jpegHeader = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
            0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xDB, 0x00, 0x43, 0x00,
            // Quantization table (simplified)
            0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07,
            0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14,
            0x0D, 0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13,
            0x0F, 0x14, 0x1D, 0x1A, 0x1F, 0x1E, 0x1D, 0x1A,
            0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22,
            0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C,
            0x30, 0x31, 0x34, 0x34, 0x34, 0x1F, 0x27, 0x39,
            0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32,
            (byte) 0xFF, (byte) 0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x11, 0x00,
            (byte) 0xFF, (byte) 0xC4, 0x00, 0x1F, 0x00, 0x00, 0x01, 0x05, 0x01, 0x01, 0x01, 0x01,
            0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x07, 0x08, 0x09, 0x0A, 0x0B,
            (byte) 0xFF, (byte) 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00, 0x7F,
            (byte) 0xFF, (byte) 0xD9
        };
        Files.write(file, jpegHeader);
        return file;
    }

    private Path createTestPngFile() throws IOException {
        Path file = tempDir.resolve("test-image.png");
        // Create a minimal valid PNG file (1x1 pixel)
        byte[] pngData = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
            0x00, 0x00, 0x00, 0x0D, // IHDR length
            0x49, 0x48, 0x44, 0x52, // IHDR
            0x00, 0x00, 0x00, 0x01, // width = 1
            0x00, 0x00, 0x00, 0x01, // height = 1
            0x08, 0x02, // bit depth = 8, color type = 2 (RGB)
            0x00, 0x00, 0x00, // compression, filter, interlace
            (byte) 0x90, 0x77, 0x53, (byte) 0xDE, // IHDR CRC
            0x00, 0x00, 0x00, 0x0C, // IDAT length
            0x49, 0x44, 0x41, 0x54, // IDAT
            0x08, (byte) 0xD7, 0x63, (byte) 0xF8, 0x0F, 0x00, 0x00, 0x01, 0x01, 0x00, 0x05, (byte) 0xFE, // compressed data
            (byte) 0xB3, (byte) 0xF0, (byte) 0x4E, // IDAT CRC (partial)
            0x00, 0x00, 0x00, 0x00, // IEND length
            0x49, 0x45, 0x4E, 0x44, // IEND
            (byte) 0xAE, 0x42, 0x60, (byte) 0x82 // IEND CRC
        };
        Files.write(file, pngData);
        return file;
    }

    private Path createTestPdfFile() throws IOException {
        Path file = tempDir.resolve("test-document.pdf");
        // Create a minimal valid PDF file
        String pdfContent =
            "%PDF-1.4\n" +
            "1 0 obj\n" +
            "<< /Type /Catalog /Pages 2 0 R >>\n" +
            "endobj\n" +
            "2 0 obj\n" +
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +
            "endobj\n" +
            "3 0 obj\n" +
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\n" +
            "endobj\n" +
            "xref\n" +
            "0 4\n" +
            "0000000000 65535 f \n" +
            "0000000009 00000 n \n" +
            "0000000058 00000 n \n" +
            "0000000115 00000 n \n" +
            "trailer\n" +
            "<< /Size 4 /Root 1 0 R >>\n" +
            "startxref\n" +
            "190\n" +
            "%%EOF";
        Files.writeString(file, pdfContent);
        return file;
    }

    private Path createTestTxtFile() throws IOException {
        Path file = tempDir.resolve("test-file.txt");
        Files.writeString(file, "This is a test text file that should not be accepted.");
        return file;
    }
}
