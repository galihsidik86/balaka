package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.service.ServiceTestDataInitializer;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Functional test for OAuth 2.0 Device Authorization Flow.
 * Tests the complete flow from device code request to API usage with access token.
 */
@DisplayName("Device Authentication Flow - Functional Tests")
@Import(ServiceTestDataInitializer.class)
class DeviceAuthFlowTest extends PlaywrightTestBase {

    private APIRequestContext apiContext;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Create API request context
        apiContext = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(baseUrl()));
    }

    @AfterEach
    void tearDown() {
        if (apiContext != null) {
            apiContext.dispose();
        }
    }

    @Test
    @DisplayName("Device flow: request code, authorize via browser, and obtain access token")
    void testDeviceCodeAuthorizationAndTokenExchange() throws Exception {
        // Step 1: Request device code
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("clientId", "playwright-test");

        APIResponse codeResponse = apiContext.post("/api/device/code",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        assertThat(codeResponse.ok()).isTrue();

        JsonNode codeData = objectMapper.readTree(codeResponse.text());
        String deviceCode = codeData.get("deviceCode").asText();
        String userCode = codeData.get("userCode").asText();
        String verificationUri = codeData.get("verificationUri").asText();
        int interval = codeData.get("interval").asInt();

        assertThat(deviceCode).isNotEmpty();
        assertThat(userCode).matches("[A-Z]{4}-[A-Z]{4}");
        assertThat(verificationUri).isEqualTo(baseUrl() + "/device");

        // Step 2: User authorizes via browser
        loginAsAdmin();
        navigateTo("/device?code=" + userCode);
        waitForPageLoad();

        assertThat(page.content()).contains("Otorisasi Perangkat");
        assertThat(page.content()).contains(userCode);
        assertThat(page.content()).contains("playwright-test");

        page.locator("input[name='deviceName']").fill("Playwright Test Device");
        page.locator("button[type='submit']:has-text('Otorisasi Perangkat')").click();
        waitForPageLoad();

        assertThat(page.url()).contains("/device/success");
        assertThat(page.content()).contains("Otorisasi Berhasil");

        // Step 3: Poll for access token
        String accessToken = pollForAccessToken(deviceCode, interval);
        assertThat(accessToken).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Use access token for API operations and verify unauthorized access is blocked")
    void testApiUsageWithDeviceToken() throws Exception {
        // Obtain token via full device flow
        String accessToken = completeDeviceFlowAndGetToken();

        // Test 1: Create draft from text
        Map<String, Object> draftRequest = new HashMap<>();
        draftRequest.put("merchant", "Test Merchant via Device Token");
        draftRequest.put("amount", 150000);
        draftRequest.put("transactionDate", "2026-02-11");
        draftRequest.put("currency", "IDR");
        draftRequest.put("category", "Testing");
        draftRequest.put("description", "Testing device authentication");
        draftRequest.put("confidence", 0.95);
        draftRequest.put("source", "playwright-test");

        APIResponse createDraftResponse = apiContext.post("/api/drafts/from-text",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Authorization", "Bearer " + accessToken)
                        .setData(draftRequest));

        assertThat(createDraftResponse.status()).isIn(200, 201);

        JsonNode draftData = objectMapper.readTree(createDraftResponse.text());
        String draftId = draftData.get("draftId").asText();

        assertThat(draftId).isNotEmpty();
        assertThat(draftData.get("status").asText()).isEqualTo("PENDING");
        assertThat(draftData.get("merchant").asText()).isEqualTo("Test Merchant via Device Token");
        assertThat(draftData.get("amount").asInt()).isEqualTo(150000);

        // Test 2: Get draft by ID
        APIResponse getDraftResponse = apiContext.get("/api/drafts/" + draftId,
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken));

        assertThat(getDraftResponse.ok()).isTrue();

        JsonNode retrievedDraft = objectMapper.readTree(getDraftResponse.text());
        assertThat(retrievedDraft.get("draftId").asText()).isEqualTo(draftId);
        assertThat(retrievedDraft.get("merchant").asText()).isEqualTo("Test Merchant via Device Token");

        // Test 3: List templates
        APIResponse templatesResponse = apiContext.get("/api/drafts/templates",
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken));

        assertThat(templatesResponse.ok()).isTrue();
        JsonNode templates = objectMapper.readTree(templatesResponse.text());
        assertThat(templates.isArray()).isTrue();
        assertThat(templates.size()).isGreaterThan(0);

        // Test 4: List accounts
        APIResponse accountsResponse = apiContext.get("/api/drafts/accounts",
                RequestOptions.create()
                        .setHeader("Authorization", "Bearer " + accessToken));

        assertThat(accountsResponse.ok()).isTrue();
        JsonNode accounts = objectMapper.readTree(accountsResponse.text());
        assertThat(accounts.isArray()).isTrue();
        assertThat(accounts.size()).isGreaterThan(0);

        // Test 5: Create draft from receipt
        Map<String, Object> receiptRequest = new HashMap<>();
        receiptRequest.put("merchant", "Starbucks via Device Token");
        receiptRequest.put("amount", 85000);
        receiptRequest.put("transactionDate", "2026-02-11");
        receiptRequest.put("currency", "IDR");
        receiptRequest.put("items", new String[]{"Latte", "Sandwich"});
        receiptRequest.put("category", "Food & Beverage");
        receiptRequest.put("confidence", 0.92);
        receiptRequest.put("source", "playwright-test");

        APIResponse createReceiptResponse = apiContext.post("/api/drafts/from-receipt",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Authorization", "Bearer " + accessToken)
                        .setData(receiptRequest));

        assertThat(createReceiptResponse.status()).isEqualTo(201);
        JsonNode receiptDraft = objectMapper.readTree(createReceiptResponse.text());
        assertThat(receiptDraft.get("merchant").asText()).isEqualTo("Starbucks via Device Token");

        // Test 6: Unauthorized access (without token) should be blocked
        APIResponse unauthorizedResponse = apiContext.get("/api/drafts/" + draftId);
        assertThat(unauthorizedResponse.status()).isIn(401, 302, 403);
    }

    @Test
    @DisplayName("Should reject invalid device code")
    void testInvalidDeviceCode() throws Exception {
        Map<String, String> tokenRequestBody = new HashMap<>();
        tokenRequestBody.put("deviceCode", "invalid-device-code-123");

        APIResponse response = apiContext.post("/api/device/token",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(tokenRequestBody));

        assertThat(response.status()).isEqualTo(400);

        JsonNode errorData = objectMapper.readTree(response.text());
        assertThat(errorData.get("error").asText()).isEqualTo("invalid_request");
    }

    @Test
    @DisplayName("Should reject invalid user code in browser")
    void testInvalidUserCode() {
        // Login as admin
        loginAsAdmin();

        // Try invalid user code
        navigateTo("/device?code=INVALID-CODE");
        waitForPageLoad();

        assertThat(page.content()).contains("Kode perangkat tidak valid");
    }

    @Test
    @DisplayName("Should require authentication for device authorization page")
    void testAuthRequiredForDeviceAuth() {
        // Try to access device page without login
        navigateTo("/device?code=TEST-CODE");

        // Should redirect to login
        page.waitForURL("**/login**");
        assertThat(page.url()).contains("/login");
    }

    // --- Helpers ---

    private String completeDeviceFlowAndGetToken() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("clientId", "playwright-test-api");

        APIResponse codeResponse = apiContext.post("/api/device/code",
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        JsonNode codeData = objectMapper.readTree(codeResponse.text());
        String deviceCode = codeData.get("deviceCode").asText();
        String userCode = codeData.get("userCode").asText();
        int interval = codeData.get("interval").asInt();

        loginAsAdmin();
        navigateTo("/device?code=" + userCode);
        waitForPageLoad();

        page.locator("input[name='deviceName']").fill("Playwright API Test Device");
        page.locator("button[type='submit']:has-text('Otorisasi Perangkat')").click();
        waitForPageLoad();

        return pollForAccessToken(deviceCode, interval);
    }

    private String pollForAccessToken(String deviceCode, int interval) throws Exception {
        Map<String, String> tokenRequestBody = new HashMap<>();
        tokenRequestBody.put("deviceCode", deviceCode);

        AtomicReference<String> tokenRef = new AtomicReference<>();
        await().atMost(Duration.ofSeconds(interval * 10L)).pollInterval(Duration.ofSeconds(interval)).until(() -> {
            APIResponse tokenResponse = apiContext.post("/api/device/token",
                    RequestOptions.create()
                            .setHeader("Content-Type", "application/json")
                            .setData(tokenRequestBody));

            if (tokenResponse.ok()) {
                JsonNode tokenData = objectMapper.readTree(tokenResponse.text());
                String accessToken = tokenData.get("accessToken").asText();
                String tokenType = tokenData.get("tokenType").asText();
                int expiresIn = tokenData.get("expiresIn").asInt();

                assertThat(accessToken).isNotEmpty();
                assertThat(tokenType).isEqualTo("Bearer");
                assertThat(expiresIn).isGreaterThan(0);
                tokenRef.set(accessToken);
                return true;
            }

            JsonNode errorData = objectMapper.readTree(tokenResponse.text());
            assertThat(errorData.get("error").asText()).isEqualTo("authorization_pending");
            return false;
        });

        return tokenRef.get();
    }
}
