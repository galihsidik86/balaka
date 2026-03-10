package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.taxdetail.TaxDetailTestDataInitializer;
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
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@DisplayName("Tax Export API - Functional Tests")
@Import(TaxDetailTestDataInitializer.class)
class TaxExportApiTest extends PlaywrightTestBase {

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

        // Create tax details on test transactions so export endpoints have data
        String txnIdPpn = findTransactionId("TAX-TRX-001");
        String txnIdPph23 = findTransactionId("TAX-TRX-002");

        // Create PPN Keluaran detail
        Map<String, Object> ppnDetail = new HashMap<>();
        ppnDetail.put("taxType", "PPN_KELUARAN");
        ppnDetail.put("counterpartyName", "PT Export Test Client");
        ppnDetail.put("counterpartyNpwp", "0123456789012345");
        ppnDetail.put("transactionCode", "01");
        ppnDetail.put("dpp", 10000000);
        ppnDetail.put("ppn", 1100000);
        post("/api/transactions/" + txnIdPpn + "/tax-details", ppnDetail);

        // Create PPh 23 detail
        Map<String, Object> pph23Detail = new HashMap<>();
        pph23Detail.put("taxType", "PPH_23");
        pph23Detail.put("counterpartyName", "PT Export Test Vendor");
        pph23Detail.put("counterpartyNpwp", "9876543210123456");
        pph23Detail.put("grossAmount", 5000000);
        pph23Detail.put("taxRate", 2);
        pph23Detail.put("taxAmount", 100000);
        post("/api/transactions/" + txnIdPph23 + "/tax-details", pph23Detail);

        log.info("Tax details created for export tests");
    }

    @AfterEach
    void tearDown() {
        if (apiContext != null) {
            apiContext.dispose();
        }
    }

    // ==================== EXCEL EXPORT TESTS ====================

    @Test
    @DisplayName("GET /api/tax-export/efaktur-keluaran - returns XLSX")
    void testExportEfakturKeluaran() {
        APIResponse response = get("/api/tax-export/efaktur-keluaran?startMonth=2025-01&endMonth=2025-12");
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().get("content-type")).contains("spreadsheetml.sheet");
        assertThat(response.headers().get("content-disposition")).contains("attachment");
        assertThat(response.body()).hasSizeGreaterThan(0);

        log.info("e-Faktur Keluaran export test passed - size={}", response.body().length);
    }

    @Test
    @DisplayName("GET /api/tax-export/efaktur-masukan - returns XLSX")
    void testExportEfakturMasukan() {
        APIResponse response = get("/api/tax-export/efaktur-masukan?startMonth=2025-01&endMonth=2025-12");
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().get("content-type")).contains("spreadsheetml.sheet");
        assertThat(response.headers().get("content-disposition")).contains("attachment");
        assertThat(response.body()).hasSizeGreaterThan(0);

        log.info("e-Faktur Masukan export test passed - size={}", response.body().length);
    }

    @Test
    @DisplayName("GET /api/tax-export/bupot-unifikasi - returns XLSX")
    void testExportBupotUnifikasi() {
        APIResponse response = get("/api/tax-export/bupot-unifikasi?startMonth=2025-01&endMonth=2025-12");
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().get("content-type")).contains("spreadsheetml.sheet");
        assertThat(response.headers().get("content-disposition")).contains("attachment");
        assertThat(response.body()).hasSizeGreaterThan(0);

        log.info("Bupot Unifikasi export test passed - size={}", response.body().length);
    }

    // ==================== JSON ENDPOINT TESTS ====================

    @Test
    @DisplayName("GET /api/tax-export/ppn-detail - returns JSON with PPN data")
    void testPpnDetailJson() throws Exception {
        APIResponse response = get("/api/tax-export/ppn-detail?startDate=2025-01-01&endDate=2025-12-31");
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        assertThat(body.get("reportType").asText()).isEqualTo("ppn-detail");
        assertThat(body.has("generatedAt")).isTrue();
        assertThat(body.has("data")).isTrue();

        JsonNode data = body.get("data");
        assertThat(data.has("keluaranItems")).isTrue();
        assertThat(data.has("masukanItems")).isTrue();
        assertThat(data.has("totals")).isTrue();

        JsonNode totals = data.get("totals");
        assertThat(totals.has("totalDppKeluaran")).isTrue();
        assertThat(totals.has("totalPpnKeluaran")).isTrue();

        // Verify at least 1 keluaran item from setUp
        assertThat(data.get("keluaranItems").size()).isGreaterThanOrEqualTo(1);

        log.info("PPN detail JSON test passed - keluaran={}, masukan={}",
                data.get("keluaranItems").size(), data.get("masukanItems").size());
    }

    @Test
    @DisplayName("GET /api/tax-export/pph23-detail - returns JSON with PPh 23 data")
    void testPph23DetailJson() throws Exception {
        APIResponse response = get("/api/tax-export/pph23-detail?startDate=2025-01-01&endDate=2025-12-31");
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        assertThat(body.get("reportType").asText()).isEqualTo("pph23-detail");

        JsonNode data = body.get("data");
        assertThat(data.has("items")).isTrue();
        assertThat(data.has("totals")).isTrue();
        assertThat(data.get("items").size()).isGreaterThanOrEqualTo(1);

        JsonNode totals = data.get("totals");
        assertThat(totals.has("totalGross")).isTrue();
        assertThat(totals.has("totalTax")).isTrue();

        log.info("PPh 23 detail JSON test passed - items={}", data.get("items").size());
    }

    @Test
    @DisplayName("GET /api/tax-export/rekonsiliasi-fiskal - returns fiscal reconciliation")
    void testRekonsiliasiFiskal() throws Exception {
        APIResponse response = get("/api/tax-export/rekonsiliasi-fiskal?year=2025");
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        assertThat(body.get("reportType").asText()).isEqualTo("rekonsiliasi-fiskal");

        JsonNode data = body.get("data");
        assertThat(data.has("year")).isTrue();
        assertThat(data.get("year").asInt()).isEqualTo(2025);
        assertThat(data.has("commercialNetIncome")).isTrue();
        assertThat(data.has("pkp")).isTrue();
        assertThat(data.has("pphBadan")).isTrue();

        JsonNode pphBadan = data.get("pphBadan");
        assertThat(pphBadan.has("pphTerutang")).isTrue();
        assertThat(pphBadan.has("calculationMethod")).isTrue();

        log.info("Rekonsiliasi Fiskal test passed - year={}, pkp={}",
                data.get("year").asInt(), data.get("pkp").asText());
    }

    @Test
    @DisplayName("GET /api/tax-export/pph-badan - returns PPh Badan calculation")
    void testPphBadan() throws Exception {
        APIResponse response = get("/api/tax-export/pph-badan?year=2025");
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        assertThat(body.get("reportType").asText()).isEqualTo("pph-badan");

        JsonNode data = body.get("data");
        assertThat(data.has("pkp")).isTrue();
        assertThat(data.has("totalRevenue")).isTrue();
        assertThat(data.has("pphTerutang")).isTrue();
        assertThat(data.has("calculationMethod")).isTrue();
        assertThat(data.has("kreditPajakPPh23")).isTrue();
        assertThat(data.has("kreditPajakPPh25")).isTrue();
        assertThat(data.has("totalKreditPajak")).isTrue();
        assertThat(data.has("pph29")).isTrue();

        log.info("PPh Badan test passed - pphTerutang={}, method={}",
                data.get("pphTerutang").asText(), data.get("calculationMethod").asText());
    }

    // ==================== FORMAT=EXCEL TESTS ====================

    @Test
    @DisplayName("GET /api/tax-export/ppn-detail?format=excel - returns XLSX")
    void testPpnDetailExcel() {
        APIResponse response = get("/api/tax-export/ppn-detail?startDate=2025-01-01&endDate=2025-12-31&format=excel");
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().get("content-type")).contains("spreadsheetml.sheet");
        assertThat(response.headers().get("content-disposition")).contains("attachment");
        assertThat(response.body()).hasSizeGreaterThan(0);

        log.info("PPN detail Excel export test passed - size={}", response.body().length);
    }

    @Test
    @DisplayName("GET /api/tax-export/pph23-detail?format=excel - returns XLSX")
    void testPph23DetailExcel() {
        APIResponse response = get("/api/tax-export/pph23-detail?startDate=2025-01-01&endDate=2025-12-31&format=excel");
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().get("content-type")).contains("spreadsheetml.sheet");
        assertThat(response.headers().get("content-disposition")).contains("attachment");
        assertThat(response.body()).hasSizeGreaterThan(0);

        log.info("PPh 23 detail Excel export test passed - size={}", response.body().length);
    }

    @Test
    @DisplayName("GET /api/tax-export/rekonsiliasi-fiskal?format=excel - returns XLSX")
    void testRekonsiliasiFiskalExcel() {
        APIResponse response = get("/api/tax-export/rekonsiliasi-fiskal?year=2025&format=excel");
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.headers().get("content-type")).contains("spreadsheetml.sheet");
        assertThat(response.headers().get("content-disposition")).contains("attachment");
        assertThat(response.body()).hasSizeGreaterThan(0);

        log.info("Rekonsiliasi Fiskal Excel export test passed - size={}", response.body().length);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("API endpoints reject unauthenticated requests with 401")
    void testUnauthenticated() {
        APIResponse response = apiContext.get("/api/tax-export/ppn-detail?startDate=2025-01-01&endDate=2025-12-31");
        assertThat(response.status()).isEqualTo(401);

        APIResponse excelResponse = apiContext.get("/api/tax-export/efaktur-keluaran?startMonth=2025-01&endMonth=2025-12");
        assertThat(excelResponse.status()).isEqualTo(401);

        log.info("Unauthenticated test passed");
    }

    @Test
    @DisplayName("Invalid date parameters return 400")
    void testInvalidDateParams() {
        APIResponse response = get("/api/tax-export/ppn-detail?startDate=invalid&endDate=2025-12-31");
        assertThat(response.status()).isEqualTo(400);

        APIResponse monthResponse = get("/api/tax-export/efaktur-keluaran?startMonth=bad&endMonth=2025-12");
        assertThat(monthResponse.status()).isEqualTo(400);

        log.info("Invalid date params test passed");
    }

    // ==================== HELPERS ====================

    private String findTransactionId(String transactionNumber) throws Exception {
        APIResponse response = get("/api/analysis/transactions?search=" + transactionNumber + "&size=1");
        assertThat(response.status()).isEqualTo(200);

        JsonNode body = parse(response);
        JsonNode transactions = body.get("data").get("transactions");
        assertThat(transactions).as("Transactions for: " + transactionNumber).isNotNull();
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

    private JsonNode parse(APIResponse response) throws Exception {
        return objectMapper.readTree(response.text());
    }

    private String authenticateViaDeviceFlow() throws Exception {
        Map<String, String> codeRequest = new HashMap<>();
        codeRequest.put("clientId", "tax-export-api-test");

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

        page.locator("input[name='deviceName']").fill("Tax Export API Test Device");
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
