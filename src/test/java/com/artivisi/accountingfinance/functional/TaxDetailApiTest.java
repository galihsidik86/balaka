package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.taxdetail.TaxDetailTestDataInitializer;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.RequestOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@DisplayName("Tax Detail & Document API - Functional Tests")
@Import(TaxDetailTestDataInitializer.class)
class TaxDetailApiTest extends PlaywrightTestBase {

    private APIRequestContext apiContext;
    private ObjectMapper objectMapper;
    private String accessToken;

    // Resolved dynamically in setUp from transaction numbers
    private String txnIdPpn;
    private String txnIdPph23;
    private String txnIdNonTax;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        apiContext = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(baseUrl()));

        accessToken = authenticateViaDeviceFlow();

        // Resolve transaction IDs by searching for known transaction numbers
        txnIdPpn = findTransactionId("TAX-TRX-001");
        txnIdPph23 = findTransactionId("TAX-TRX-002");
        txnIdNonTax = findTransactionId("TAX-TRX-003");

        log.info("Resolved transaction IDs: PPN={}, PPH23={}, NonTax={}",
                txnIdPpn, txnIdPph23, txnIdNonTax);
    }

    @AfterEach
    void tearDown() {
        if (apiContext != null) {
            apiContext.dispose();
        }
    }

    @Test
    @DisplayName("POST /api/transactions/{id}/tax-details - creates PPN detail")
    void testCreatePpnDetail() throws Exception {
        Map<String, Object> request = ppnDetailRequest();

        APIResponse response = post("/api/transactions/" + txnIdPpn + "/tax-details", request);
        assertThat(response.status()).isEqualTo(201);

        JsonNode body = parse(response);
        assertThat(body.get("id").asText()).isNotEmpty();
        assertThat(body.get("transactionId").asText()).isEqualTo(txnIdPpn);
        assertThat(body.get("taxType").asText()).isEqualTo("PPN_KELUARAN");
        assertThat(body.get("counterpartyName").asText()).isEqualTo("PT Klien Utama");
        assertThat(body.get("dpp").asDouble()).isEqualTo(10000000.0);
        assertThat(body.get("ppn").asDouble()).isEqualTo(1100000.0);
        assertThat(body.get("transactionCode").asText()).isEqualTo("01");

        log.info("Create PPN detail test passed - id={}", body.get("id").asText());
    }

    @Test
    @DisplayName("GET /api/transactions/{id}/tax-details - lists tax details")
    void testListTaxDetails() throws Exception {
        // Create a detail first
        post("/api/transactions/" + txnIdPpn + "/tax-details", ppnDetailRequest());

        APIResponse response = get("/api/transactions/" + txnIdPpn + "/tax-details");
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        assertThat(body.isArray()).isTrue();
        assertThat(body.size()).isGreaterThanOrEqualTo(1);

        JsonNode first = body.get(0);
        assertThat(first.has("id")).isTrue();
        assertThat(first.has("taxType")).isTrue();
        assertThat(first.has("counterpartyName")).isTrue();

        log.info("List tax details test passed - count={}", body.size());
    }

    @Test
    @DisplayName("GET /api/transactions/{id}/tax-details/{detailId} - gets single detail")
    void testGetTaxDetail() throws Exception {
        // Create a detail
        APIResponse createResponse = post("/api/transactions/" + txnIdPpn + "/tax-details", ppnDetailRequest());
        JsonNode created = parse(createResponse);
        String detailId = created.get("id").asText();

        APIResponse response = get("/api/transactions/" + txnIdPpn + "/tax-details/" + detailId);
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        assertThat(body.get("id").asText()).isEqualTo(detailId);
        assertThat(body.get("taxType").asText()).isEqualTo("PPN_KELUARAN");

        log.info("Get single tax detail test passed - id={}", detailId);
    }

    @Test
    @DisplayName("PUT /api/transactions/{id}/tax-details/{detailId} - updates counterparty name")
    void testUpdateTaxDetail() throws Exception {
        // Create a detail
        APIResponse createResponse = post("/api/transactions/" + txnIdPpn + "/tax-details", ppnDetailRequest());
        JsonNode created = parse(createResponse);
        String detailId = created.get("id").asText();

        // Update counterparty name
        Map<String, Object> updateRequest = ppnDetailRequest();
        updateRequest.put("counterpartyName", "PT Klien Baru Updated");

        APIResponse response = put("/api/transactions/" + txnIdPpn + "/tax-details/" + detailId, updateRequest);
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        assertThat(body.get("id").asText()).isEqualTo(detailId);
        assertThat(body.get("counterpartyName").asText()).isEqualTo("PT Klien Baru Updated");

        log.info("Update tax detail test passed - id={}", detailId);
    }

    @Test
    @DisplayName("DELETE /api/transactions/{id}/tax-details/{detailId} - deletes and verifies empty")
    void testDeleteTaxDetail() throws Exception {
        // Create a detail
        APIResponse createResponse = post("/api/transactions/" + txnIdNonTax + "/tax-details", ppnDetailRequest());
        JsonNode created = parse(createResponse);
        String detailId = created.get("id").asText();

        // Delete it
        APIResponse deleteResponse = delete("/api/transactions/" + txnIdNonTax + "/tax-details/" + detailId);
        assertThat(deleteResponse.status()).isEqualTo(204);

        // Verify empty list for that transaction
        APIResponse listResponse = get("/api/transactions/" + txnIdNonTax + "/tax-details");
        JsonNode list = parse(listResponse);
        assertThat(list).isEmpty();

        log.info("Delete tax detail test passed - id={}", detailId);
    }

    @Test
    @DisplayName("POST /api/tax-details/bulk - bulk creates for 2 transactions")
    void testBulkCreate() throws Exception {
        Map<String, Object> item1 = Map.of(
                "transactionId", txnIdPpn,
                "detail", Map.of(
                        "taxType", "PPN_KELUARAN",
                        "counterpartyName", "PT Bulk Client A",
                        "dpp", 5000000,
                        "ppn", 550000,
                        "transactionCode", "01"
                )
        );

        Map<String, Object> item2 = Map.of(
                "transactionId", txnIdPph23,
                "detail", Map.of(
                        "taxType", "PPH_23",
                        "counterpartyName", "PT Bulk Client B",
                        "grossAmount", 5000000,
                        "taxRate", 2,
                        "taxAmount", 100000
                )
        );

        Map<String, Object> request = Map.of("items", List.of(item1, item2));

        APIResponse response = post("/api/tax-details/bulk", request);
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        assertThat(body.get("successCount").asInt()).isEqualTo(2);
        assertThat(body.get("failureCount").asInt()).isZero();
        assertThat(body.get("results").size()).isEqualTo(2);

        for (int i = 0; i < body.get("results").size(); i++) {
            JsonNode result = body.get("results").get(i);
            assertThat(result.get("success").asBoolean()).isTrue();
            assertThat(result.get("detailId").asText()).isNotEmpty();
        }

        log.info("Bulk create test passed - successCount={}", body.get("successCount").asInt());
    }

    @Test
    @DisplayName("POST /api/transactions/{id}/documents - uploads file")
    void testUploadDocument() throws Exception {
        // Create a minimal valid PNG file (1x1 pixel)
        Path tempFile = Files.createTempFile("test-doc-", ".png");
        Files.write(tempFile, createMinimalPng());

        APIResponse response = apiContext.post("/api/transactions/" + txnIdPpn + "/documents",
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken)
                        .setMultipart(FormData.create()
                                .set("file", tempFile)));

        assertThat(response.status()).isEqualTo(201);

        JsonNode body = parse(response);
        assertThat(body.get("id").asText()).isNotEmpty();
        assertThat(body.get("originalFilename").asText()).contains("test-doc-");
        assertThat(body.get("contentType").asText()).isEqualTo("image/png");
        assertThat(body.get("fileSize").asLong()).isGreaterThan(0);
        assertThat(body.get("fileSizeFormatted").asText()).isNotEmpty();
        assertThat(body.get("checksumSha256").asText()).isNotEmpty();

        // Clean up
        Files.deleteIfExists(tempFile);

        log.info("Upload document test passed - id={}", body.get("id").asText());
    }

    @Test
    @DisplayName("GET /api/transactions/{id}/documents - lists documents")
    void testListDocuments() throws Exception {
        // Upload a document first (must be allowed type)
        Path tempFile = Files.createTempFile("test-list-", ".png");
        Files.write(tempFile, createMinimalPng());

        APIResponse uploadResponse = apiContext.post("/api/transactions/" + txnIdPph23 + "/documents",
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken)
                        .setMultipart(FormData.create()
                                .set("file", tempFile)));
        assertThat(uploadResponse.status()).isEqualTo(201);

        APIResponse response = get("/api/transactions/" + txnIdPph23 + "/documents");
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        assertThat(body.isArray()).isTrue();
        assertThat(body.size()).isGreaterThanOrEqualTo(1);

        JsonNode first = body.get(0);
        assertThat(first.has("id")).isTrue();
        assertThat(first.has("originalFilename")).isTrue();
        assertThat(first.has("contentType")).isTrue();

        // Clean up
        Files.deleteIfExists(tempFile);

        log.info("List documents test passed - count={}", body.size());
    }

    @Test
    @DisplayName("GET /api/documents/{docId} - downloads document content")
    void testDownloadDocument() throws Exception {
        // Upload a document first (must be allowed type)
        Path tempFile = Files.createTempFile("test-download-", ".png");
        byte[] pngBytes = createMinimalPng();
        Files.write(tempFile, pngBytes);

        APIResponse uploadResponse = apiContext.post("/api/transactions/" + txnIdNonTax + "/documents",
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken)
                        .setMultipart(FormData.create()
                                .set("file", tempFile)));
        assertThat(uploadResponse.status()).isEqualTo(201);

        JsonNode uploaded = parse(uploadResponse);
        String docId = uploaded.get("id").asText();

        // Download it
        APIResponse response = get("/api/documents/" + docId);
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().get("content-disposition")).contains("attachment");

        byte[] body = response.body();
        assertThat(body).hasSizeGreaterThan(0);

        // Clean up
        Files.deleteIfExists(tempFile);

        log.info("Download document test passed - docId={}, size={}", docId, body.length);
    }

    @Test
    @DisplayName("API endpoints reject unauthenticated requests with 401")
    void testUnauthenticated() {
        // Use a fixed fake UUID — doesn't matter which, we expect 401 before any lookup
        String fakeId = "00000000-0000-0000-0000-000000000000";

        APIResponse taxDetailsResponse = apiContext.get("/api/transactions/" + fakeId + "/tax-details");
        assertThat(taxDetailsResponse.status()).isEqualTo(401);

        APIResponse documentsResponse = apiContext.get("/api/transactions/" + fakeId + "/documents");
        assertThat(documentsResponse.status()).isEqualTo(401);

        log.info("Unauthenticated test passed");
    }

    // --- Helpers ---

    private String findTransactionId(String transactionNumber) throws Exception {
        APIResponse response = get("/api/analysis/transactions?search=" + transactionNumber + "&size=1");
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        JsonNode data = body.get("data");
        assertThat(data).as("Response data for: " + transactionNumber).isNotNull();
        JsonNode transactions = data.get("transactions");
        assertThat(transactions).as("Transactions array for: " + transactionNumber).isNotNull();
        assertThat(transactions.size()).as("Transaction not found: " + transactionNumber).isGreaterThanOrEqualTo(1);

        return transactions.get(0).get("id").asText();
    }

    private Map<String, Object> ppnDetailRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("taxType", "PPN_KELUARAN");
        request.put("counterpartyName", "PT Klien Utama");
        request.put("transactionCode", "01");
        request.put("dpp", 10000000);
        request.put("ppn", 1100000);
        return request;
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

    private APIResponse put(String path, Object data) {
        return apiContext.put(path,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken)
                        .setHeader("Content-Type", "application/json")
                        .setData(data));
    }

    private APIResponse delete(String path) {
        return apiContext.delete(path,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken));
    }

    private JsonNode parse(APIResponse response) throws Exception {
        return objectMapper.readTree(response.text());
    }

    /**
     * Creates a minimal valid 1x1 pixel PNG file.
     */
    private byte[] createMinimalPng() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        javax.imageio.ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    private String authenticateViaDeviceFlow() throws Exception {
        Map<String, String> codeRequest = new HashMap<>();
        codeRequest.put("clientId", "tax-detail-api-test");

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

        page.locator("input[name='deviceName']").fill("Tax Detail API Test Device");
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
