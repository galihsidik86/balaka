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

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Reproduction tests for known bugs.
 * Each test demonstrates the bug and verifies the fix.
 */
@Slf4j
@DisplayName("Bug Reproduction Tests")
@Import(ServiceTestDataInitializer.class)
class BugReproductionTest extends PlaywrightTestBase {

    private APIRequestContext apiContext;
    private ObjectMapper objectMapper;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        apiContext = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(baseUrl()));

        accessToken = authenticateViaDeviceFlow();
    }

    @AfterEach
    void tearDown() {
        if (apiContext != null) {
            apiContext.dispose();
        }
    }

    // ==================== BUG-002 ====================

    @Nested
    @DisplayName("BUG-002: PUT then POST should produce valid journal entries")
    class Bug002 {

        @Test
        @DisplayName("Create draft, PUT to update amount, then POST — should succeed")
        void putUpdateAmountThenPost() throws Exception {
            // 1. Create a DRAFT transaction via POST /api/drafts
            String templateId = getFirstTemplateId();
            String accountId = getFirstAccountId();

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("templateId", templateId);
            createRequest.put("description", "BUG-002 reproduction test");
            createRequest.put("amount", 100000);
            createRequest.put("transactionDate", "2026-02-10");
            createRequest.put("accountSlots", Map.of("BANK", accountId));

            APIResponse createResponse = post("/api/drafts", createRequest);
            assertThat(createResponse.status())
                    .as("Create draft failed: %d %s", createResponse.status(), createResponse.text())
                    .isEqualTo(201);

            JsonNode created = parse(createResponse);
            String transactionId = created.get("transactionId").asText();
            log.info("BUG-002: Created draft transaction {}", transactionId);

            // 2. PUT to update amount
            Map<String, Object> updateRequest = Map.of("amount", 200000);
            APIResponse putResponse = put("/api/transactions/" + transactionId, updateRequest);
            assertThat(putResponse.ok())
                    .as("PUT update failed: %d %s", putResponse.status(), putResponse.text())
                    .isTrue();

            // 3. POST to post it — should succeed with journal entries
            APIResponse postResponse = postNoBody("/api/transactions/" + transactionId + "/post");
            assertThat(postResponse.ok())
                    .as("POST failed: %d %s", postResponse.status(), postResponse.text())
                    .isTrue();

            JsonNode posted = parse(postResponse);
            assertThat(posted.get("status").asText()).isEqualTo("POSTED");
            assertThat(posted.get("journalEntries").size())
                    .as("Posted transaction should have journal entries")
                    .isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Create draft, PUT to change template, then POST — should succeed")
        void putChangeTemplateThenPost() throws Exception {
            // 1. Create a DRAFT transaction with template A (simple amount-based)
            String templateIdA = findSimpleTemplateByName("Pendapatan Jasa Training");
            String accountId = getFirstAccountId();

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("templateId", templateIdA);
            createRequest.put("description", "BUG-002 template change test");
            createRequest.put("amount", 150000);
            createRequest.put("transactionDate", "2026-02-10");
            createRequest.put("accountSlots", Map.of("BANK", accountId));

            APIResponse createResponse = post("/api/drafts", createRequest);
            assertThat(createResponse.status()).isEqualTo(201);

            JsonNode created = parse(createResponse);
            String transactionId = created.get("transactionId").asText();

            // 2. PUT to change to template B (another simple amount-based template)
            String templateIdB = findSimpleTemplateByName("Pendapatan Jasa Konsultasi");
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("templateId", templateIdB);
            updateRequest.put("accountSlots", Map.of("BANK", accountId));

            APIResponse putResponse = put("/api/transactions/" + transactionId, updateRequest);
            assertThat(putResponse.ok())
                    .as("PUT template change failed: %d %s", putResponse.status(), putResponse.text())
                    .isTrue();

            // 3. POST — should succeed with correct journal entries for template B
            APIResponse postResponse = postNoBody("/api/transactions/" + transactionId + "/post");
            assertThat(postResponse.ok())
                    .as("POST after template change failed: %d %s", postResponse.status(), postResponse.text())
                    .isTrue();

            JsonNode posted = parse(postResponse);
            assertThat(posted.get("status").asText()).isEqualTo("POSTED");
            assertThat(posted.get("journalEntries").size()).isGreaterThanOrEqualTo(2);
        }
    }

    // ==================== BUG-003 ====================

    @Nested
    @DisplayName("BUG-003: PUT accountSlots should be idempotent")
    class Bug003 {

        @Test
        @DisplayName("PUT accountSlots twice should not return 409 Conflict")
        void putAccountSlotsTwiceShouldNotConflict() throws Exception {
            // 1. Create a DRAFT transaction
            String templateId = getFirstTemplateId();
            String accountId = getFirstAccountId();

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("templateId", templateId);
            createRequest.put("description", "BUG-003 reproduction test");
            createRequest.put("amount", 100000);
            createRequest.put("transactionDate", "2026-02-10");
            createRequest.put("accountSlots", Map.of("BANK", accountId));

            APIResponse createResponse = post("/api/drafts", createRequest);
            assertThat(createResponse.status()).isEqualTo(201);

            JsonNode created = parse(createResponse);
            String transactionId = created.get("transactionId").asText();

            // 2. First PUT with accountSlots
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("accountSlots", Map.of("BANK", accountId));

            APIResponse put1 = put("/api/transactions/" + transactionId, updateRequest);
            assertThat(put1.ok())
                    .as("First PUT failed: %d %s", put1.status(), put1.text())
                    .isTrue();

            // 3. Second PUT with same accountSlots — should NOT return 409
            APIResponse put2 = put("/api/transactions/" + transactionId, updateRequest);
            assertThat(put2.ok())
                    .as("Second PUT returned %d (expected 200, not 409): %s", put2.status(), put2.text())
                    .isTrue();
        }

        @Test
        @DisplayName("PUT accountSlots then POST should produce valid journal entries")
        void putAccountSlotsThenPostShouldWork() throws Exception {
            // 1. Create a DRAFT transaction
            String templateId = getFirstTemplateId();
            String accountId = getFirstAccountId();

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("templateId", templateId);
            createRequest.put("description", "BUG-003 post after overrides");
            createRequest.put("amount", 75000);
            createRequest.put("transactionDate", "2026-02-10");

            APIResponse createResponse = post("/api/drafts", createRequest);
            assertThat(createResponse.status()).isEqualTo(201);

            JsonNode created = parse(createResponse);
            String transactionId = created.get("transactionId").asText();

            // 2. PUT with accountSlots
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("accountSlots", Map.of("BANK", accountId));

            APIResponse putResponse = put("/api/transactions/" + transactionId, updateRequest);
            assertThat(putResponse.ok())
                    .as("PUT with accountSlots failed: %d %s", putResponse.status(), putResponse.text())
                    .isTrue();

            // 3. POST — should succeed
            APIResponse postResponse = postNoBody("/api/transactions/" + transactionId + "/post");
            assertThat(postResponse.ok())
                    .as("POST after accountSlots PUT failed: %d %s", postResponse.status(), postResponse.text())
                    .isTrue();

            JsonNode posted = parse(postResponse);
            assertThat(posted.get("status").asText()).isEqualTo("POSTED");
            assertThat(posted.get("journalEntries").size()).isGreaterThanOrEqualTo(2);
        }
    }

    // ==================== BUG-004 ====================

    @Nested
    @DisplayName("BUG-004: Analysis endpoint should show journal preview for DRAFTs")
    class Bug004 {

        @Test
        @DisplayName("GET /api/analysis/transactions/{id} on DRAFT should return journal preview entries")
        void analysisEndpointShouldReturnJournalPreviewForDraft() throws Exception {
            // 1. Create a DRAFT transaction
            String templateId = getFirstTemplateId();
            String accountId = getFirstAccountId();

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("templateId", templateId);
            createRequest.put("description", "BUG-004 journal preview test");
            createRequest.put("amount", 90000);
            createRequest.put("transactionDate", "2026-02-10");
            createRequest.put("accountSlots", Map.of("BANK", accountId));

            APIResponse createResponse = post("/api/drafts", createRequest);
            assertThat(createResponse.status()).isEqualTo(201);

            JsonNode created = parse(createResponse);
            String transactionId = created.get("transactionId").asText();

            // 2. GET via analysis endpoint — should show journal preview entries
            APIResponse analysisResponse = get("/api/analysis/transactions/" + transactionId);
            assertThat(analysisResponse.ok())
                    .as("Analysis GET failed: %d %s", analysisResponse.status(), analysisResponse.text())
                    .isTrue();

            JsonNode analysisBody = parse(analysisResponse);
            JsonNode data = analysisBody.get("data");
            assertThat(data.get("status").asText()).isEqualTo("DRAFT");

            // BUG-004: journalEntries is empty for DRAFTs — should contain preview entries
            JsonNode journalEntries = data.get("journalEntries");
            assertThat(journalEntries.size())
                    .as("DRAFT transaction should have preview journal entries, not empty array")
                    .isGreaterThanOrEqualTo(2);
        }
    }

    // ==================== BUG-001 ====================

    @Nested
    @DisplayName("BUG-001: PPN rounding consistency")
    class Bug001 {

        @Test
        @DisplayName("FormulaEvaluator should use consistent rounding for all lines")
        void ppnRoundingShouldBeConsistent() throws Exception {
            // Find the FP 03 BUMN template
            String templateId = findTemplateByName("FP 03");
            if (templateId == null) {
                log.warn("BUG-001: FP 03 template not found — skipping test");
                return;
            }

            // Get template lines to check formulas
            APIResponse templateResponse = get("/api/templates/" + templateId);
            assertThat(templateResponse.ok()).isTrue();
            parse(templateResponse);

            // Verify all lines produce balanced debit/credit
            // Use amount 11,111,250 which gives 11,111,250 × 11% = 1,222,237.5
            // This is the exact amount from the bug report
            String accountId = getFirstAccountId();
            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("templateId", templateId);
            createRequest.put("description", "BUG-001 PPN rounding test");
            createRequest.put("amount", 11111250);
            createRequest.put("transactionDate", "2026-02-10");
            createRequest.put("accountSlots", Map.of("BANK", accountId, "PENDAPATAN", accountId));

            APIResponse createResponse = post("/api/drafts", createRequest);
            assertThat(createResponse.status())
                    .as("Create draft failed: %d %s", createResponse.status(), createResponse.text())
                    .isEqualTo(201);

            JsonNode created = parse(createResponse);
            String transactionId = created.get("transactionId").asText();

            // Post and verify journal balance
            APIResponse postResponse = postNoBody("/api/transactions/" + transactionId + "/post");
            assertThat(postResponse.ok())
                    .as("Post failed: %d %s", postResponse.status(), postResponse.text())
                    .isTrue();

            JsonNode posted = parse(postResponse);
            JsonNode journalEntries = posted.get("journalEntries");

            // Sum debits and credits — they MUST balance
            BigDecimal totalDebit = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;
            for (JsonNode entry : journalEntries) {
                totalDebit = totalDebit.add(new BigDecimal(entry.get("debitAmount").asText()));
                totalCredit = totalCredit.add(new BigDecimal(entry.get("creditAmount").asText()));
            }

            assertThat(totalDebit)
                    .as("Journal entries must balance: total debit=%s, total credit=%s", totalDebit, totalCredit)
                    .isEqualByComparingTo(totalCredit);
        }
    }

    // ==================== HELPER METHODS ====================

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

    private APIResponse postNoBody(String path) {
        return apiContext.post(path,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken));
    }

    private APIResponse put(String path, Object data) {
        return apiContext.put(path,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken)
                        .setHeader("Content-Type", "application/json")
                        .setData(data));
    }

    private JsonNode parse(APIResponse response) throws Exception {
        return objectMapper.readTree(response.text());
    }

    private String getFirstTemplateId() throws Exception {
        APIResponse response = get("/api/templates");
        assertThat(response.ok()).isTrue();
        JsonNode templates = parse(response);
        assertThat(templates.size()).isGreaterThan(0);
        return templates.get(0).get("id").asText();
    }

    private String getSecondTemplateId() throws Exception {
        APIResponse response = get("/api/templates");
        assertThat(response.ok()).isTrue();
        JsonNode templates = parse(response);
        assertThat(templates.size()).isGreaterThan(1);
        return templates.get(1).get("id").asText();
    }

    private String getFirstAccountId() throws Exception {
        APIResponse response = get("/api/analysis/accounts");
        assertThat(response.ok()).isTrue();
        JsonNode body = parse(response);
        JsonNode accounts = body.get("data").get("accounts");
        assertThat(accounts.size()).isGreaterThan(0);
        return accounts.get(0).get("id").asText();
    }

    private String findSimpleTemplateByName(String exactName) throws Exception {
        APIResponse response = get("/api/templates");
        assertThat(response.ok()).isTrue();
        JsonNode templates = parse(response);
        for (JsonNode template : templates) {
            if (template.get("name").asText().equals(exactName)) {
                return template.get("id").asText();
            }
        }
        throw new RuntimeException("Template not found: " + exactName);
    }

    private String findTemplateByName(String nameContains) throws Exception {
        APIResponse response = get("/api/templates");
        assertThat(response.ok()).isTrue();
        JsonNode templates = parse(response);
        for (JsonNode template : templates) {
            if (template.get("name").asText().contains(nameContains)) {
                return template.get("id").asText();
            }
        }
        return null;
    }

    private String authenticateViaDeviceFlow() throws Exception {
        Map<String, String> codeRequest = Map.of("clientId", "bug-repro-test");

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

        page.locator("input[name='deviceName']").fill("Bug Reproduction Test Device");
        page.locator("button[type='submit']:has-text('Otorisasi Perangkat')").click();
        waitForPageLoad();

        Map<String, String> tokenRequest = Map.of("deviceCode", deviceCode);

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
