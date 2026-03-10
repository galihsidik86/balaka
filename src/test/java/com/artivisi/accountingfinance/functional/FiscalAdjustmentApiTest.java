package com.artivisi.accountingfinance.functional;

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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@DisplayName("Fiscal Adjustments API - Functional Tests")
class FiscalAdjustmentApiTest extends PlaywrightTestBase {

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
    @DisplayName("CRUD lifecycle: create, list, update, delete fiscal adjustment")
    void crudLifecycle() throws Exception {
        // CREATE
        Map<String, Object> request = new HashMap<>();
        request.put("year", 2025);
        request.put("description", "Denda Pajak");
        request.put("adjustmentCategory", "PERMANENT");
        request.put("adjustmentDirection", "POSITIVE");
        request.put("amount", 3226367.00);
        request.put("accountCode", "5.9.90");
        request.put("notes", "Pasal 9(1)(k) UU PPh");

        APIResponse createResponse = post("/api/fiscal-adjustments", request);
        assertThat(createResponse.status()).isEqualTo(201);

        JsonNode created = parse(createResponse);
        String id = created.get("id").asText();
        assertThat(id).isNotBlank();
        assertThat(created.get("year").asInt()).isEqualTo(2025);
        assertThat(created.get("description").asText()).isEqualTo("Denda Pajak");
        assertThat(created.get("adjustmentCategory").asText()).isEqualTo("PERMANENT");
        assertThat(created.get("adjustmentDirection").asText()).isEqualTo("POSITIVE");
        assertThat(created.get("amount").asDouble()).isEqualTo(3226367.00);
        assertThat(created.get("accountCode").asText()).isEqualTo("5.9.90");
        assertThat(created.get("notes").asText()).isEqualTo("Pasal 9(1)(k) UU PPh");
        log.info("Created fiscal adjustment: id={}", id);

        // LIST
        APIResponse listResponse = get("/api/fiscal-adjustments?year=2025");
        assertThat(listResponse.status()).isEqualTo(200);

        JsonNode list = parse(listResponse);
        assertThat(list.isArray()).isTrue();
        assertThat(list.size()).isGreaterThanOrEqualTo(1);

        boolean found = false;
        for (JsonNode item : list) {
            if (item.get("id").asText().equals(id)) {
                found = true;
                break;
            }
        }
        assertThat(found).as("Created adjustment should appear in list").isTrue();

        // UPDATE
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("year", 2025);
        updateRequest.put("description", "Denda Pajak (Updated)");
        updateRequest.put("adjustmentCategory", "PERMANENT");
        updateRequest.put("adjustmentDirection", "POSITIVE");
        updateRequest.put("amount", 5000000.00);
        updateRequest.put("accountCode", "5.9.90");
        updateRequest.put("notes", "Updated notes");

        APIResponse updateResponse = put("/api/fiscal-adjustments/" + id, updateRequest);
        assertThat(updateResponse.status()).isEqualTo(200);

        JsonNode updated = parse(updateResponse);
        assertThat(updated.get("description").asText()).isEqualTo("Denda Pajak (Updated)");
        assertThat(updated.get("amount").asDouble()).isEqualTo(5000000.00);
        assertThat(updated.get("notes").asText()).isEqualTo("Updated notes");
        log.info("Updated fiscal adjustment: id={}", id);

        // DELETE
        APIResponse deleteResponse = delete("/api/fiscal-adjustments/" + id);
        assertThat(deleteResponse.status()).isEqualTo(204);
        log.info("Deleted fiscal adjustment: id={}", id);

        // VERIFY DELETED
        APIResponse listAfterDelete = get("/api/fiscal-adjustments?year=2025");
        JsonNode listAfter = parse(listAfterDelete);
        boolean stillExists = false;
        for (JsonNode item : listAfter) {
            if (item.get("id").asText().equals(id)) {
                stillExists = true;
                break;
            }
        }
        assertThat(stillExists).as("Deleted adjustment should not appear in list").isFalse();
    }

    @Test
    @DisplayName("POST with invalid data returns 400")
    void createWithInvalidData() {
        // Missing required fields
        Map<String, Object> request = new HashMap<>();
        request.put("year", null);
        request.put("description", "");

        APIResponse response = post("/api/fiscal-adjustments", request);
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("PUT on non-existent ID returns 404")
    void updateNonExistent() {
        Map<String, Object> request = new HashMap<>();
        request.put("year", 2025);
        request.put("description", "Test");
        request.put("adjustmentCategory", "PERMANENT");
        request.put("adjustmentDirection", "POSITIVE");
        request.put("amount", 1000.00);

        APIResponse response = put("/api/fiscal-adjustments/00000000-0000-0000-0000-000000000000", request);
        assertThat(response.status()).isEqualTo(404);
    }

    @Test
    @DisplayName("GET with empty year returns empty list")
    void listEmptyYear() throws Exception {
        APIResponse response = get("/api/fiscal-adjustments?year=1900");
        assertThat(response.status()).isEqualTo(200);

        JsonNode list = parse(response);
        assertThat(list.isArray()).isTrue();
        assertThat(list).isEmpty();
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

    private String authenticateViaDeviceFlow() throws Exception {
        Map<String, String> codeRequest = new HashMap<>();
        codeRequest.put("clientId", "fiscal-adj-api-test");

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

        page.locator("input[name='deviceName']").fill("Fiscal Adjustment API Test Device");
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
