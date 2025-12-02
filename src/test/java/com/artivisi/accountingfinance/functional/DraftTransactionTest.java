package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.page.LoginPage;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Draft Transaction Management")
class DraftTransactionTest extends PlaywrightTestBase {

    // Test data IDs from V910 migration
    private static final String PENDING_DRAFT_ID_1 = "d0000000-0000-0000-0000-000000000001";
    private static final String PENDING_DRAFT_ID_2 = "d0000000-0000-0000-0000-000000000002";
    private static final String PENDING_DRAFT_ID_3 = "d0000000-0000-0000-0000-000000000003";
    private static final String APPROVED_DRAFT_ID = "d0000000-0000-0000-0000-000000000004";
    private static final String REJECTED_DRAFT_ID = "d0000000-0000-0000-0000-000000000005";
    private static final String LOW_CONFIDENCE_DRAFT_ID = "d0000000-0000-0000-0000-000000000006";

    // Template from seed data
    private static final String TEST_TEMPLATE_NAME = "Pembayaran Cash/Tunai";

    private LoginPage loginPage;
    private String draftsListUrl;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage(page, baseUrl());
        loginPage.navigate().loginAsAdmin();
        draftsListUrl = baseUrl() + "/drafts";
    }

    @Nested
    @DisplayName("Draft List Page")
    class DraftListTests {

        @Test
        @DisplayName("Should display draft list page")
        void shouldDisplayDraftListPage() {
            page.navigate(draftsListUrl);
            waitForPageLoad();

            assertThat(page.locator("#page-title").textContent()).contains("Draft Transaksi");
            assertThat(page.locator("#drafts-list-content").isVisible()).isTrue();
        }

        @Test
        @DisplayName("Should show pending drafts in list")
        void shouldShowPendingDraftsInList() {
            page.navigate(draftsListUrl);
            waitForPageLoad();

            // Should show at least one pending draft
            String pageContent = page.content();
            assertThat(pageContent).containsAnyOf("Toko Bangunan Jaya", "Warung Makan Sederhana", "SPBU Pertamina");
        }

        @Test
        @DisplayName("Should show pending count badge")
        void shouldShowPendingCountBadge() {
            page.navigate(draftsListUrl);
            waitForPageLoad();

            // Should show pending count badge (at least 3 pending drafts from test data)
            String pageContent = page.content();
            assertThat(pageContent).contains("menunggu review");
        }

        @Test
        @DisplayName("Should filter by status PENDING")
        void shouldFilterByStatusPending() {
            page.navigate(draftsListUrl + "?status=PENDING");
            waitForPageLoad();

            // Verify filter is applied
            Locator statusSelect = page.locator("#filter-status");
            assertThat(statusSelect.inputValue()).isEqualTo("PENDING");

            // Should show pending drafts (SPBU Pertamina or low confidence draft remain pending after approval tests)
            String pageContent = page.content();
            assertThat(pageContent).containsAnyOf("SPBU Pertamina", "Warung ???", "Toko Bangunan Jaya", "Warung Makan Sederhana");
        }

        @Test
        @DisplayName("Should filter by status APPROVED")
        void shouldFilterByStatusApproved() {
            page.navigate(draftsListUrl + "?status=APPROVED");
            waitForPageLoad();

            // Verify filter is applied
            Locator statusSelect = page.locator("#filter-status");
            assertThat(statusSelect.inputValue()).isEqualTo("APPROVED");

            // Should show approved draft (Indomaret)
            String pageContent = page.content();
            assertThat(pageContent).contains("Indomaret");
        }

        @Test
        @DisplayName("Should filter by status REJECTED")
        void shouldFilterByStatusRejected() {
            page.navigate(draftsListUrl + "?status=REJECTED");
            waitForPageLoad();

            // Verify filter is applied
            Locator statusSelect = page.locator("#filter-status");
            assertThat(statusSelect.inputValue()).isEqualTo("REJECTED");

            // Should show rejected draft (Unknown Merchant)
            String pageContent = page.content();
            assertThat(pageContent).contains("Unknown Merchant");
        }

        @Test
        @DisplayName("Should navigate to draft detail from list")
        void shouldNavigateToDraftDetailFromList() {
            page.navigate(draftsListUrl + "?status=PENDING");
            waitForPageLoad();

            // Click on first draft link
            Locator firstDraftLink = page.locator("table tbody tr a").first();
            firstDraftLink.click();
            waitForPageLoad();

            // Should be on detail page
            assertThat(page.url()).contains("/drafts/");
            assertThat(page.locator("#page-title").textContent()).contains("Detail Draft Transaksi");
        }
    }

    @Nested
    @DisplayName("Draft Detail Page")
    class DraftDetailTests {

        @Test
        @DisplayName("Should display pending draft detail")
        void shouldDisplayPendingDraftDetail() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_1);
            waitForPageLoad();

            // Verify page loads
            assertThat(page.locator("#draft-detail-content").isVisible()).isTrue();

            // Verify draft info
            String pageContent = page.content();
            assertThat(pageContent).contains("Toko Bangunan Jaya");
            assertThat(pageContent).contains("250");
            assertThat(pageContent).contains("Menunggu Review");
        }

        @Test
        @DisplayName("Should show approve form for pending draft")
        void shouldShowApproveFormForPendingDraft() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_1);
            waitForPageLoad();

            // Verify approve form exists
            assertThat(page.locator("#templateId").isVisible()).isTrue();
            assertThat(page.locator("#description").isVisible()).isTrue();
            assertThat(page.locator("#amount").isVisible()).isTrue();
            assertThat(page.locator("button:has-text('Approve')").isVisible()).isTrue();
        }

        @Test
        @DisplayName("Should show reject form for pending draft")
        void shouldShowRejectFormForPendingDraft() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_1);
            waitForPageLoad();

            // Verify reject form exists
            assertThat(page.locator("#reason").isVisible()).isTrue();
            assertThat(page.locator("button:has-text('Tolak')").isVisible()).isTrue();
        }

        @Test
        @DisplayName("Should display confidence scores")
        void shouldDisplayConfidenceScores() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_1);
            waitForPageLoad();

            // Verify confidence section
            String pageContent = page.content();
            assertThat(pageContent).contains("Confidence Score");
            assertThat(pageContent).contains("Overall");
            assertThat(pageContent).contains("85%");
        }

        @Test
        @DisplayName("Should display low confidence warning")
        void shouldDisplayLowConfidenceWarning() {
            page.navigate(baseUrl() + "/drafts/" + LOW_CONFIDENCE_DRAFT_ID);
            waitForPageLoad();

            // Verify low confidence is displayed
            String pageContent = page.content();
            assertThat(pageContent).contains("10%");
        }

        @Test
        @DisplayName("Should display OCR text")
        void shouldDisplayOcrText() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_1);
            waitForPageLoad();

            // Verify OCR text section
            String pageContent = page.content();
            assertThat(pageContent).contains("Teks OCR");
            assertThat(pageContent).contains("Jl. Raya No. 123");
        }

        @Test
        @DisplayName("Should display rejection reason for rejected draft")
        void shouldDisplayRejectionReasonForRejectedDraft() {
            page.navigate(baseUrl() + "/drafts/" + REJECTED_DRAFT_ID);
            waitForPageLoad();

            // Verify rejection info
            String pageContent = page.content();
            assertThat(pageContent).contains("Ditolak");
            assertThat(pageContent).contains("Alasan Penolakan");
            assertThat(pageContent).contains("Struk tidak bisa dibaca dengan jelas");
        }

        @Test
        @DisplayName("Should show approved status for approved draft")
        void shouldShowApprovedStatusForApprovedDraft() {
            page.navigate(baseUrl() + "/drafts/" + APPROVED_DRAFT_ID);
            waitForPageLoad();

            // Verify approved status
            String pageContent = page.content();
            assertThat(pageContent).contains("Disetujui");
        }

        @Test
        @DisplayName("Should have back link to list")
        void shouldHaveBackLinkToList() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_1);
            waitForPageLoad();

            // Verify back link
            Locator backLink = page.locator("a:has-text('Kembali ke Daftar Draft')");
            assertThat(backLink.isVisible()).isTrue();

            backLink.click();
            waitForPageLoad();
            assertThat(page.url()).contains("/drafts");
        }
    }

    @Nested
    @DisplayName("Draft Approval Flow")
    class DraftApprovalTests {

        @Test
        @DisplayName("Should approve draft with template selection")
        void shouldApproveDraftWithTemplateSelection() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_1);
            waitForPageLoad();

            // Select template - get first non-empty option
            Locator templateSelect = page.locator("#templateId");
            Locator firstOption = templateSelect.locator("option[value]:not([value=''])").first();
            String firstTemplateId = firstOption.getAttribute("value");
            templateSelect.selectOption(firstTemplateId);

            // Fill description
            Locator descriptionInput = page.locator("#description");
            descriptionInput.fill("Test approval from Playwright");

            // Submit approve form
            page.locator("button:has-text('Approve')").click();
            waitForPageLoad();

            // Should redirect to transaction edit page
            assertThat(page.url()).contains("/transactions/");
            assertThat(page.url()).contains("/edit");
        }

        @Test
        @DisplayName("Should reject draft with reason")
        void shouldRejectDraftWithReason() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_2);
            waitForPageLoad();

            // Fill rejection reason
            Locator reasonInput = page.locator("#reason");
            reasonInput.fill("Test rejection from Playwright - struk tidak valid");

            // Submit reject form
            page.locator("button:has-text('Tolak')").click();
            waitForPageLoad();

            // Should redirect to drafts list
            assertThat(page.url()).contains("/drafts");
        }
    }

    @Nested
    @DisplayName("Draft Delete")
    class DraftDeleteTests {

        @Test
        @DisplayName("Should display pending draft that can be deleted")
        void shouldDisplayPendingDraftForDeletion() {
            // Navigate to pending draft
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_3);
            waitForPageLoad();

            // Verify draft exists and is pending (can be deleted)
            String pageContent = page.content();
            assertThat(pageContent).contains("SPBU Pertamina");
            assertThat(pageContent).contains("Menunggu Review");
            // Draft is pending status, which means it can be deleted
        }
    }

    @Nested
    @DisplayName("Draft API Endpoints")
    class DraftApiTests {

        @Test
        @DisplayName("Should get draft list via API")
        void shouldGetDraftListViaApi() {
            var response = page.request().get(baseUrl() + "/drafts/api");

            assertThat(response.status()).isEqualTo(200);
            String body = response.text();
            assertThat(body).contains("content");
        }

        @Test
        @DisplayName("Should get draft by ID via API")
        void shouldGetDraftByIdViaApi() {
            var response = page.request().get(baseUrl() + "/drafts/api/" + APPROVED_DRAFT_ID);

            assertThat(response.status()).isEqualTo(200);
            String body = response.text();
            assertThat(body).contains("Indomaret");
        }

        @Test
        @DisplayName("Should filter drafts by status via API")
        void shouldFilterDraftsByStatusViaApi() {
            var response = page.request().get(baseUrl() + "/drafts/api?status=REJECTED");

            assertThat(response.status()).isEqualTo(200);
            String body = response.text();
            assertThat(body).contains("Unknown Merchant");
        }
    }

    @Nested
    @DisplayName("Draft Status Display")
    class DraftStatusDisplayTests {

        @Test
        @DisplayName("Should display correct status badge for PENDING")
        void shouldDisplayCorrectStatusBadgeForPending() {
            // Use draft ID 6 which is not modified by approval tests
            page.navigate(baseUrl() + "/drafts/" + LOW_CONFIDENCE_DRAFT_ID);
            waitForPageLoad();

            // Verify PENDING status badge
            Locator statusBadge = page.locator("span:has-text('Menunggu Review')");
            assertThat(statusBadge.isVisible()).isTrue();
        }

        @Test
        @DisplayName("Should display correct status badge for APPROVED")
        void shouldDisplayCorrectStatusBadgeForApproved() {
            page.navigate(baseUrl() + "/drafts/" + APPROVED_DRAFT_ID);
            waitForPageLoad();

            // Verify APPROVED status badge
            Locator statusBadge = page.locator("span:has-text('Disetujui')");
            assertThat(statusBadge.isVisible()).isTrue();
        }

        @Test
        @DisplayName("Should display correct status badge for REJECTED")
        void shouldDisplayCorrectStatusBadgeForRejected() {
            page.navigate(baseUrl() + "/drafts/" + REJECTED_DRAFT_ID);
            waitForPageLoad();

            // Verify REJECTED status badge
            Locator statusBadge = page.locator("span:has-text('Ditolak')");
            assertThat(statusBadge.isVisible()).isTrue();
        }
    }

    @Nested
    @DisplayName("Draft Source Display")
    class DraftSourceDisplayTests {

        @Test
        @DisplayName("Should display MANUAL source")
        void shouldDisplayManualSource() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_1);
            waitForPageLoad();

            String pageContent = page.content();
            assertThat(pageContent).contains("Manual");
        }

        @Test
        @DisplayName("Should display TELEGRAM source with icon")
        void shouldDisplayTelegramSourceWithIcon() {
            page.navigate(baseUrl() + "/drafts/" + PENDING_DRAFT_ID_2);
            waitForPageLoad();

            String pageContent = page.content();
            assertThat(pageContent).contains("Telegram");
        }
    }

}
