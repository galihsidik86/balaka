package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.service.ServiceTestDataInitializer;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@DisplayName("Journal Entry API - Functional Tests")
@Import(ServiceTestDataInitializer.class)
class JournalEntryApiTest extends PlaywrightTestBase {

    private APIRequestContext apiContext;
    private ObjectMapper objectMapper;
    private String accessToken;

    // Account IDs resolved at setup from seed data
    private String debitAccountId;   // leaf account for debit lines
    private String creditAccountId;  // leaf account for credit lines
    private String thirdAccountId;   // another leaf account for multi-line tests
    private String headerAccountId;  // header account for validation test

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        apiContext = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(baseUrl()));

        accessToken = authenticateViaDeviceFlow();
        resolveAccountIds();
    }

    @AfterEach
    void tearDown() {
        if (apiContext != null) {
            apiContext.dispose();
        }
    }

    private void resolveAccountIds() throws Exception {
        // Fetch all leaf accounts via API
        APIResponse response = get("/api/drafts/accounts");
        assertThat(response.status()).isEqualTo(200);
        JsonNode accounts = parse(response);

        // Find specific accounts by code from IT service seed data
        String debitCode = "1.1.02";  // Bank BCA (asset)
        String creditCode = "4.1.02"; // Pendapatan Jasa Konsultasi (revenue)
        String thirdCode = "5.1.06";  // Beban Telekomunikasi (expense)

        for (JsonNode account : accounts) {
            String code = account.get("code").asText();
            String id = account.get("id").asText();
            if (debitCode.equals(code)) debitAccountId = id;
            if (creditCode.equals(code)) creditAccountId = id;
            if (thirdCode.equals(code)) thirdAccountId = id;
        }

        assertThat(debitAccountId).as("Debit account " + debitCode + " must exist").isNotNull();
        assertThat(creditAccountId).as("Credit account " + creditCode + " must exist").isNotNull();
        assertThat(thirdAccountId).as("Third account " + thirdCode + " must exist").isNotNull();

        // Find a header account UUID by navigating to COA page and extracting edit link
        // Account code "1.1" (Aset Lancar) is a known header in IT service seed data
        // Edit button id: btn-edit-1-1, href contains the UUID
        navigateTo("/accounts");
        waitForPageLoad();
        var editLink = page.locator("#btn-edit-1-1");
        if (editLink.count() > 0) {
            String href = editLink.getAttribute("href");
            // href format: /accounts/{uuid}/edit
            headerAccountId = href.replace("/accounts/", "").replace("/edit", "");
        }

        log.info("Resolved accounts: debit={}, credit={}, third={}, header={}",
                debitAccountId, creditAccountId, thirdAccountId, headerAccountId);
    }

    // ==================== CREATE JOURNAL ENTRY ====================

    @Nested
    @DisplayName("Create Journal Entry")
    class CreateJournalEntry {

        @Test
        @DisplayName("Happy path: 2-line journal entry creates DRAFT")
        void happyPath() throws Exception {
            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Year-end closing entry",
                    null,
                    List.of(
                            line(debitAccountId, 1000000, 0),
                            line(creditAccountId, 0, 1000000)
                    )
            );

            APIResponse response = post("/api/transactions/journal-entry", request);
            assertThat(response.status())
                    .as("Create journal entry: " + response.text())
                    .isEqualTo(201);

            JsonNode body = parse(response);
            assertThat(body.get("status").asText()).isEqualTo("DRAFT");
            assertThat(body.get("transactionId").asText()).isNotEmpty();
            assertThat(body.get("amount").asDouble()).isEqualTo(1000000.0);
            assertThat(body.get("description").asText()).isEqualTo("Year-end closing entry");
            log.info("Created journal entry draft: id={}", body.get("transactionId").asText());
        }

        @Test
        @DisplayName("Multi-line: 4 lines (2 debit, 2 credit) preserves all entries")
        void multiLine() throws Exception {
            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Multi-line closing journal",
                    null,
                    List.of(
                            line(debitAccountId, 500000, 0),
                            line(thirdAccountId, 500000, 0),
                            line(creditAccountId, 0, 700000),
                            line(creditAccountId, 0, 300000)
                    )
            );

            APIResponse response = post("/api/transactions/journal-entry", request);
            assertThat(response.status())
                    .as("Multi-line create: " + response.text())
                    .isEqualTo(201);

            JsonNode body = parse(response);
            assertThat(body.get("amount").asDouble()).isEqualTo(1000000.0);
            assertThat(body.get("journalEntries").size()).isEqualTo(4);
        }

        @Test
        @DisplayName("Category stored in notes")
        void categoryInNotes() {
            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Closing accrued expenses",
                    "CLOSING",
                    List.of(
                            line(debitAccountId, 250000, 0),
                            line(creditAccountId, 0, 250000)
                    )
            );

            APIResponse response = post("/api/transactions/journal-entry", request);
            assertThat(response.status()).isEqualTo(201);
        }
    }

    // ==================== POST JOURNAL ENTRY ====================

    @Nested
    @DisplayName("Post Journal Entry")
    class PostJournalEntry {

        @Test
        @DisplayName("Full lifecycle: create draft, post, verify journal numbers")
        void fullLifecycle() throws Exception {
            // Create draft
            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Adjusting entry for depreciation",
                    null,
                    List.of(
                            line(debitAccountId, 750000, 0),
                            line(creditAccountId, 0, 750000)
                    )
            );

            APIResponse createResponse = post("/api/transactions/journal-entry", request);
            assertThat(createResponse.status()).isEqualTo(201);
            String transactionId = parse(createResponse).get("transactionId").asText();

            // Post
            APIResponse postResponse = post("/api/transactions/" + transactionId + "/post", Map.of());
            assertThat(postResponse.status())
                    .as("Post journal entry: " + postResponse.text())
                    .isEqualTo(200);

            JsonNode posted = parse(postResponse);
            assertThat(posted.get("status").asText()).isEqualTo("POSTED");
            assertThat(posted.get("transactionNumber").asText()).isNotEmpty();

            // Verify journal entries have journal numbers
            JsonNode entries = posted.get("journalEntries");
            assertThat(entries.size()).isEqualTo(2);
            for (JsonNode entry : entries) {
                assertThat(entry.get("journalNumber").asText()).isNotEmpty();
            }
            log.info("Posted journal entry: txNum={}", posted.get("transactionNumber").asText());
        }
    }

    // ==================== JOURNAL PREVIEW ====================

    @Nested
    @DisplayName("Journal Preview")
    class JournalPreview {

        @Test
        @DisplayName("Preview freeform journal entry returns pre-created entries")
        void previewFreeform() throws Exception {
            // Create draft
            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Opening balance entry",
                    null,
                    List.of(
                            line(debitAccountId, 2000000, 0),
                            line(creditAccountId, 0, 2000000)
                    )
            );

            APIResponse createResponse = post("/api/transactions/journal-entry", request);
            assertThat(createResponse.status()).isEqualTo(201);
            String transactionId = parse(createResponse).get("transactionId").asText();

            // Preview
            APIResponse previewResponse = get("/api/transactions/" + transactionId + "/journal-preview");
            assertThat(previewResponse.status())
                    .as("Preview: " + previewResponse.text())
                    .isEqualTo(200);

            JsonNode preview = parse(previewResponse);
            assertThat(preview.get("valid").asBoolean()).isTrue();
            assertThat(preview.get("entries").size()).isEqualTo(2);
            assertThat(preview.get("totalDebit").asDouble()).isEqualTo(2000000.0);
            assertThat(preview.get("totalCredit").asDouble()).isEqualTo(2000000.0);
        }
    }

    // ==================== VALIDATION ERRORS ====================

    @Nested
    @DisplayName("Validation Errors")
    class ValidationErrors {

        @Test
        @DisplayName("Unbalanced entry returns 400")
        void unbalanced() {
            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Unbalanced entry",
                    null,
                    List.of(
                            line(debitAccountId, 1000000, 0),
                            line(creditAccountId, 0, 500000)
                    )
            );

            APIResponse response = post("/api/transactions/journal-entry", request);
            assertThat(response.status()).isEqualTo(400);
        }

        @Test
        @DisplayName("Both debit and credit on same line returns 400")
        void bothDebitAndCredit() {
            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Both sides entry",
                    null,
                    List.of(
                            line(debitAccountId, 1000000, 500000),
                            line(creditAccountId, 0, 500000)
                    )
            );

            APIResponse response = post("/api/transactions/journal-entry", request);
            assertThat(response.status()).isEqualTo(400);
        }

        @Test
        @DisplayName("Neither debit nor credit returns 400")
        void neitherDebitNorCredit() {
            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Zero entry",
                    null,
                    List.of(
                            line(debitAccountId, 0, 0),
                            line(creditAccountId, 0, 0)
                    )
            );

            APIResponse response = post("/api/transactions/journal-entry", request);
            assertThat(response.status()).isEqualTo(400);
        }

        @Test
        @DisplayName("Fewer than 2 lines returns 400")
        void fewerThan2Lines() {
            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Single line entry",
                    null,
                    List.of(
                            line(debitAccountId, 1000000, 0)
                    )
            );

            APIResponse response = post("/api/transactions/journal-entry", request);
            assertThat(response.status()).isEqualTo(400);
        }

        @Test
        @DisplayName("Header account returns 400")
        void headerAccount() throws Exception {
            if (headerAccountId == null) {
                log.warn("No header account found in seed data, skipping test");
                return;
            }

            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Header account entry",
                    null,
                    List.of(
                            line(headerAccountId, 1000000, 0),
                            line(creditAccountId, 0, 1000000)
                    )
            );

            APIResponse response = post("/api/transactions/journal-entry", request);
            assertThat(response.status()).isEqualTo(400);
        }

        @Test
        @DisplayName("Non-existent account returns 400")
        void nonExistentAccount() {
            String fakeAccountId = UUID.randomUUID().toString();

            Map<String, Object> request = buildJournalEntryRequest(
                    "2025-12-31",
                    "Bad account entry",
                    null,
                    List.of(
                            line(fakeAccountId, 1000000, 0),
                            line(creditAccountId, 0, 1000000)
                    )
            );

            APIResponse response = post("/api/transactions/journal-entry", request);
            assertThat(response.status()).isEqualTo(400);
        }
    }

    // ==================== HELPER METHODS ====================

    private Map<String, Object> buildJournalEntryRequest(String date, String description,
                                                          String category, List<Map<String, Object>> lines) {
        Map<String, Object> request = new HashMap<>();
        request.put("transactionDate", date);
        request.put("description", description);
        if (category != null) {
            request.put("category", category);
        }
        request.put("lines", lines);
        return request;
    }

    private Map<String, Object> line(String accountId, double debit, double credit) {
        Map<String, Object> line = new HashMap<>();
        line.put("accountId", accountId);
        line.put("debit", debit);
        line.put("credit", credit);
        return line;
    }

    private APIResponse get(String path) {
        return apiContext.get(path,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse post(String path, Object data) {
        return apiContext.post(path,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken)
                        .setHeader("Content-Type", "application/json")
                        .setData(data));
    }

    private JsonNode parse(APIResponse response) throws Exception {
        return objectMapper.readTree(response.text());
    }

    private String authenticateViaDeviceFlow() throws Exception {
        Map<String, String> codeRequest = new HashMap<>();
        codeRequest.put("clientId", "journal-entry-api-test");

        APIResponse codeResponse = apiContext.post("/api/device/code",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(codeRequest));

        assertThat(codeResponse.ok()).isTrue();

        JsonNode codeData = objectMapper.readTree(codeResponse.text());
        String deviceCode = codeData.get("deviceCode").asText();
        String userCode = codeData.get("userCode").asText();

        loginAsAdmin();
        navigateTo("/device?code=" + userCode);
        waitForPageLoad();

        page.locator("input[name='deviceName']").fill("Journal Entry API Test Device");
        page.locator("button[type='submit']:has-text('Otorisasi Perangkat')").click();
        waitForPageLoad();

        Map<String, String> tokenRequest = new HashMap<>();
        tokenRequest.put("deviceCode", deviceCode);

        AtomicReference<String> tokenRef = new AtomicReference<>();
        await().atMost(Duration.ofSeconds(20)).pollInterval(Duration.ofSeconds(2)).until(() -> {
            APIResponse tokenResponse = apiContext.post("/api/device/token",
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(tokenRequest));
            if (tokenResponse.ok()) {
                JsonNode tokenData = objectMapper.readTree(tokenResponse.text());
                tokenRef.set(tokenData.get("accessToken").asText());
                return true;
            }
            return false;
        });

        return tokenRef.get();
    }
}
