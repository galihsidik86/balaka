package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.entity.Project;
import com.artivisi.accountingfinance.functional.taxdetail.TaxDetailTestDataInitializer;
import com.artivisi.accountingfinance.repository.ChartOfAccountRepository;
import com.artivisi.accountingfinance.repository.ProjectRepository;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("Tax Detail Auto-Populate - Functional Tests")
@Import(TaxDetailTestDataInitializer.class)
class TaxDetailAutoPopulateTest extends PlaywrightTestBase {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ChartOfAccountRepository chartOfAccountRepository;

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

    @Test
    @DisplayName("PPN template auto-populates PPN_KELUARAN tax detail on post")
    void testPpnAutoPopulates() throws Exception {
        String templateId = findTemplateIdByName("Pendapatan Jasa + PPN");
        String projectId = findProjectIdByCode("PRJ-2024-001");
        String transactionId = createAndPostDraft(templateId, 10000000, "Auto PPN Test", projectId);

        JsonNode taxDetails = getTaxDetails(transactionId);
        assertThat(taxDetails.size()).as("Should have 1 auto-populated PPN detail").isEqualTo(1);

        JsonNode detail = taxDetails.get(0);
        assertThat(detail.get("taxType").asText()).isEqualTo("PPN_KELUARAN");
        assertThat(detail.get("dpp").asDouble()).isEqualTo(10000000.0);
        assertThat(detail.get("ppn").asDouble()).isEqualTo(1100000.0);
        assertThat(detail.get("transactionCode").asText()).isEqualTo("01");

        log.info("PPN auto-populate test passed");
    }

    @Test
    @DisplayName("PPh 23 template auto-populates PPH_23 tax detail on post")
    void testPph23AutoPopulates() throws Exception {
        String templateId = findTemplateIdByName("Pembayaran Jasa dengan PPh 23");
        String projectId = findProjectIdByCode("PRJ-2024-003");
        String transactionId = createAndPostDraft(templateId, 5000000, "Auto PPh 23 Test", projectId);

        JsonNode taxDetails = getTaxDetails(transactionId);
        assertThat(taxDetails.size()).as("Should have 1 auto-populated PPh 23 detail").isEqualTo(1);

        JsonNode detail = taxDetails.get(0);
        assertThat(detail.get("taxType").asText()).isEqualTo("PPH_23");
        assertThat(detail.get("grossAmount").asDouble()).isEqualTo(5000000.0);
        assertThat(detail.get("taxRate").asDouble()).isEqualTo(2.0);
        assertThat(detail.get("taxAmount").asDouble()).isEqualTo(100000.0);

        log.info("PPh 23 auto-populate test passed");
    }

    @Test
    @DisplayName("Non-tax template creates no tax details on post")
    void testNonTaxTemplateNoDetails() throws Exception {
        String templateId = findTemplateIdByName("Pendapatan Jasa Training");
        String projectId = findProjectIdByCode("PRJ-2024-001");
        String transactionId = createAndPostDraft(templateId, 15000000, "Non-tax Template Test", projectId);

        JsonNode taxDetails = getTaxDetails(transactionId);
        assertThat(taxDetails.size()).as("Non-tax template should have no tax details").isEqualTo(0);

        log.info("Non-tax template test passed");
    }

    @Test
    @DisplayName("Counterparty info populated from project client NPWP")
    void testCounterpartyFromProjectClient() throws Exception {
        String templateId = findTemplateIdByName("Pendapatan Jasa + PPN");
        String projectId = findProjectIdByCode("PRJ-2024-001");
        String transactionId = createAndPostDraft(templateId, 8000000, "Client NPWP Test", projectId);

        JsonNode taxDetails = getTaxDetails(transactionId);
        assertThat(taxDetails.size()).isEqualTo(1);

        JsonNode detail = taxDetails.get(0);
        assertThat(detail.get("counterpartyNpwp").asText()).isEqualTo("01.310.523.4-091.000");
        assertThat(detail.get("counterpartyName").asText()).isEqualTo("PT Bank Mandiri Tbk");

        log.info("Counterparty from project client test passed");
    }

    @Test
    @DisplayName("Already-posted transactions with existing tax details are not duplicated")
    void testNoDuplicates() throws Exception {
        // TAX-TRX-001 is already POSTED with PPN template and has no tax details yet
        // Manually add a tax detail first, then verify auto-populate doesn't duplicate
        String txnId = findTransactionId("TAX-TRX-001");

        // Manually add a PPN detail
        Map<String, Object> manualDetail = new HashMap<>();
        manualDetail.put("taxType", "PPN_KELUARAN");
        manualDetail.put("counterpartyName", "PT Manual Entry");
        manualDetail.put("dpp", 10000000);
        manualDetail.put("ppn", 1100000);
        manualDetail.put("transactionCode", "01");

        APIResponse createResponse = post("/api/transactions/" + txnId + "/tax-details", manualDetail);
        assertThat(createResponse.status()).isEqualTo(201);

        // Now create a new PPN transaction, post it, and verify exactly 1 detail
        String templateId = findTemplateIdByName("Pendapatan Jasa + PPN");
        String projectId = findProjectIdByCode("PRJ-2024-001");
        String newTxnId = createAndPostDraft(templateId, 20000000, "No Duplicate Test", projectId);

        JsonNode taxDetails = getTaxDetails(newTxnId);
        assertThat(taxDetails.size()).as("Should have exactly 1 auto-populated detail, not duplicated").isEqualTo(1);

        // Verify original transaction still has the manual detail (other tests may add details too)
        JsonNode originalDetails = getTaxDetails(txnId);
        assertThat(originalDetails.size()).as("Original transaction should have at least the manual detail").isGreaterThanOrEqualTo(1);
        boolean hasManualEntry = false;
        for (int i = 0; i < originalDetails.size(); i++) {
            if ("PT Manual Entry".equals(originalDetails.get(i).get("counterpartyName").asText())) {
                hasManualEntry = true;
                break;
            }
        }
        assertThat(hasManualEntry).as("Manual detail 'PT Manual Entry' should still exist").isTrue();

        log.info("No duplicates test passed");
    }

    // --- Helper methods ---

    private String createAndPostDraft(String templateId, int amount, String description, String projectId) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("templateId", templateId);
        request.put("description", description);
        request.put("amount", amount);
        request.put("transactionDate", "2026-02-20");
        if (projectId != null) {
            request.put("projectId", projectId);
        }

        // Resolve account slots from template hints
        Map<String, String> accountSlots = resolveAccountSlots(templateId);
        if (!accountSlots.isEmpty()) {
            request.put("accountSlots", accountSlots);
        }

        APIResponse createResponse = post("/api/drafts", request);
        assertThat(createResponse.status())
                .as("Create draft failed: %d %s", createResponse.status(), createResponse.text())
                .isEqualTo(201);

        JsonNode createBody = objectMapper.readTree(createResponse.text());
        String transactionId = createBody.get("transactionId").asText();

        // Post the draft
        APIResponse postResponse = apiContext.post("/api/transactions/" + transactionId + "/post",
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken));

        assertThat(postResponse.ok())
                .as("Post failed: %d %s", postResponse.status(), postResponse.text())
                .isTrue();

        return transactionId;
    }

    private Map<String, String> resolveAccountSlots(String templateId) throws Exception {
        APIResponse templateResponse = get("/api/templates/" + templateId);
        assertThat(templateResponse.ok()).isTrue();

        JsonNode template = objectMapper.readTree(templateResponse.text());
        JsonNode lines = template.get("lines");

        Map<String, String> slots = new HashMap<>();
        for (JsonNode line : lines) {
            if (line.has("accountHint") && !line.get("accountHint").isNull()
                    && !line.get("accountHint").asText().isBlank()) {
                String hint = line.get("accountHint").asText();
                if (!slots.containsKey(hint)) {
                    String accountId = findAccountForHint(hint);
                    slots.put(hint, accountId);
                }
            }
        }
        return slots;
    }

    private String findAccountForHint(String hint) {
        String hintUpper = hint.toUpperCase();
        return switch (hintUpper) {
            case "BANK" -> chartOfAccountRepository.findByAccountCode("1.1.02")
                    .orElseThrow(() -> new RuntimeException("Bank account 1.1.02 not found"))
                    .getId().toString();
            case "PENDAPATAN" -> chartOfAccountRepository.findByAccountCode("4.1.01")
                    .orElseThrow(() -> new RuntimeException("Revenue account 4.1.01 not found"))
                    .getId().toString();
            case "BEBAN" -> chartOfAccountRepository.findByAccountCode("5.1.01")
                    .orElseThrow(() -> new RuntimeException("Expense account 5.1.01 not found"))
                    .getId().toString();
            default -> throw new RuntimeException("Unknown account hint: " + hint);
        };
    }

    private JsonNode getTaxDetails(String transactionId) throws Exception {
        APIResponse response = get("/api/transactions/" + transactionId + "/tax-details");
        assertThat(response.status()).isEqualTo(200);
        return objectMapper.readTree(response.text());
    }

    private String findTemplateIdByName(String name) throws Exception {
        APIResponse response = get("/api/templates");
        assertThat(response.ok()).isTrue();

        JsonNode templates = objectMapper.readTree(response.text());
        for (JsonNode template : templates) {
            if (name.equals(template.get("name").asText())) {
                return template.get("id").asText();
            }
        }
        throw new RuntimeException("Template not found: " + name);
    }

    private String findProjectIdByCode(String projectCode) {
        Project project = projectRepository.findByCode(projectCode)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectCode));
        return project.getId().toString();
    }

    private String findTransactionId(String transactionNumber) throws Exception {
        APIResponse response = get("/api/analysis/transactions?search=" + transactionNumber + "&size=1");
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = objectMapper.readTree(response.text());
        JsonNode transactions = body.get("data").get("transactions");
        assertThat(transactions.size()).as("Transaction not found: " + transactionNumber).isGreaterThanOrEqualTo(1);

        return transactions.get(0).get("id").asText();
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

    private String authenticateViaDeviceFlow() throws Exception {
        Map<String, String> codeRequest = Map.of("clientId", "tax-auto-populate-test");

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

        page.locator("input[name='deviceName']").fill("Tax Auto-Populate Test Device");
        page.locator("button[type='submit']:has-text('Otorisasi Perangkat')").click();
        waitForPageLoad();

        Map<String, String> tokenRequest = Map.of("deviceCode", deviceCode);

        for (int i = 0; i < 10; i++) {
            Thread.sleep(2000);

            APIResponse tokenResponse = apiContext.post("/api/device/token",
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(tokenRequest));

            if (tokenResponse.ok()) {
                JsonNode tokenData = objectMapper.readTree(tokenResponse.text());
                return tokenData.get("accessToken").asText();
            }
        }

        throw new RuntimeException("Failed to get access token");
    }
}
