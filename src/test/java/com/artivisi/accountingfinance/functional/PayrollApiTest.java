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
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@DisplayName("Payroll API - Functional Tests")
@Import(ServiceTestDataInitializer.class)
class PayrollApiTest extends PlaywrightTestBase {

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

    // ==================== SALARY COMPONENT TESTS ====================

    @Test
    @DisplayName("Salary component CRUD lifecycle")
    void salaryComponentCrud() throws Exception {
        // CREATE
        Map<String, Object> request = new HashMap<>();
        request.put("code", "API_TEST_COMP");
        request.put("name", "API Test Component");
        request.put("componentType", "EARNING");
        request.put("isPercentage", false);
        request.put("defaultAmount", 5000000);
        request.put("isTaxable", true);

        APIResponse createResponse = post("/api/salary-components", request);
        assertThat(createResponse.status()).isEqualTo(201);

        JsonNode created = parse(createResponse);
        String componentId = created.get("id").asText();
        assertThat(created.get("code").asText()).isEqualTo("API_TEST_COMP");
        assertThat(created.get("componentType").asText()).isEqualTo("EARNING");
        log.info("Created salary component: id={}", componentId);

        // LIST
        APIResponse listResponse = get("/api/salary-components");
        assertThat(listResponse.status()).isEqualTo(200);
        JsonNode list = parse(listResponse);
        assertThat(list.isArray()).isTrue();
        assertThat(list.size()).isGreaterThanOrEqualTo(1);

        // UPDATE
        request.put("name", "API Test Component Updated");
        APIResponse updateResponse = put("/api/salary-components/" + componentId, request);
        assertThat(updateResponse.status()).isEqualTo(200);
        JsonNode updated = parse(updateResponse);
        assertThat(updated.get("name").asText()).isEqualTo("API Test Component Updated");

        // DELETE (soft delete)
        APIResponse deleteResponse = delete("/api/salary-components/" + componentId);
        assertThat(deleteResponse.status()).isEqualTo(204);
    }

    // ==================== EMPLOYEE TESTS ====================

    @Test
    @DisplayName("Employee CRUD and salary assignment")
    void employeeCrudAndSalaryAssignment() throws Exception {
        // First create a salary component
        Map<String, Object> compRequest = new HashMap<>();
        compRequest.put("code", "GAJI_POKOK_TEST");
        compRequest.put("name", "Gaji Pokok Test");
        compRequest.put("componentType", "EARNING");
        compRequest.put("defaultAmount", 5000000);
        compRequest.put("isTaxable", true);

        APIResponse compResponse = post("/api/salary-components", compRequest);
        assertThat(compResponse.status()).isEqualTo(201);
        String salaryComponentId = parse(compResponse).get("id").asText();

        // CREATE EMPLOYEE
        Map<String, Object> empRequest = new HashMap<>();
        empRequest.put("employeeId", "EMP-API-TEST-001");
        empRequest.put("name", "Test Employee API");
        empRequest.put("npwp", "9999888877776666");
        empRequest.put("nikKtp", "9999888877776666");
        empRequest.put("ptkpStatus", "TK_0");
        empRequest.put("hireDate", "2025-01-01");
        empRequest.put("employmentType", "PERMANENT");
        empRequest.put("employmentStatus", "ACTIVE");
        empRequest.put("jobTitle", "Developer");
        empRequest.put("department", "Engineering");

        APIResponse empCreateResponse = post("/api/employees", empRequest);
        assertThat(empCreateResponse.status()).isEqualTo(201);

        JsonNode empCreated = parse(empCreateResponse);
        String employeeId = empCreated.get("id").asText();
        assertThat(empCreated.get("name").asText()).isEqualTo("Test Employee API");
        assertThat(empCreated.get("ptkpStatus").asText()).isEqualTo("TK_0");
        log.info("Created employee: id={}", employeeId);

        // GET EMPLOYEE
        APIResponse getResponse = get("/api/employees/" + employeeId);
        assertThat(getResponse.status()).isEqualTo(200);

        // LIST EMPLOYEES
        APIResponse listResponse = get("/api/employees?active=true");
        assertThat(listResponse.status()).isEqualTo(200);
        JsonNode empList = parse(listResponse);
        assertThat(empList.size()).isGreaterThanOrEqualTo(1);

        // ASSIGN SALARY COMPONENT
        Map<String, Object> assignRequest = new HashMap<>();
        assignRequest.put("salaryComponentId", salaryComponentId);
        assignRequest.put("amount", 5000000);
        assignRequest.put("effectiveDate", "2025-01-01");

        APIResponse assignResponse = post("/api/employees/" + employeeId + "/salary-components", assignRequest);
        assertThat(assignResponse.status()).isEqualTo(201);

        JsonNode assignment = parse(assignResponse);
        assertThat(assignment.get("componentCode").asText()).isEqualTo("GAJI_POKOK_TEST");
        assertThat(assignment.get("amount").asDouble()).isEqualTo(5000000.0);

        // VERIFY EMPLOYEE HAS SALARY COMPONENT
        APIResponse empDetail = get("/api/employees/" + employeeId);
        JsonNode empWithSalary = parse(empDetail);
        assertThat(empWithSalary.get("salaryComponents").size()).isEqualTo(1);
    }

    // ==================== PAYROLL RUN TESTS ====================

    @Test
    @DisplayName("Payroll run lifecycle: create, calculate, approve, delete")
    void payrollRunLifecycle() throws Exception {
        // CREATE
        Map<String, Object> request = new HashMap<>();
        request.put("payrollPeriod", "2028-01");
        request.put("notes", "API Test Payroll Jan 2028");

        APIResponse createResponse = post("/api/payroll", request);
        assertThat(createResponse.status()).isEqualTo(201);

        JsonNode created = parse(createResponse);
        String payrollId = created.get("id").asText();
        assertThat(created.get("payrollPeriod").asText()).isEqualTo("2028-01");
        assertThat(created.get("status").asText()).isEqualTo("DRAFT");
        log.info("Created payroll run: id={}", payrollId);

        // CALCULATE
        Map<String, Object> calcRequest = new HashMap<>();
        calcRequest.put("baseSalary", 5000000);
        calcRequest.put("jkkRiskClass", 1);

        APIResponse calcResponse = post("/api/payroll/" + payrollId + "/calculate", calcRequest);
        assertThat(calcResponse.status())
                .as("Calculate response: " + calcResponse.text())
                .isEqualTo(200);

        JsonNode calculated = parse(calcResponse);
        assertThat(calculated.get("status").asText()).isEqualTo("CALCULATED");
        assertThat(calculated.get("employeeCount").asInt()).isGreaterThanOrEqualTo(1);
        log.info("Payroll calculated: employeeCount={}", calculated.get("employeeCount").asInt());

        // GET DETAIL
        APIResponse detailResponse = get("/api/payroll/" + payrollId);
        assertThat(detailResponse.status()).isEqualTo(200);
        JsonNode detail = parse(detailResponse);
        assertThat(detail.get("details").size()).isGreaterThanOrEqualTo(1);

        // APPROVE
        APIResponse approveResponse = post("/api/payroll/" + payrollId + "/approve", Map.of());
        assertThat(approveResponse.status()).isEqualTo(200);
        assertThat(parse(approveResponse).get("status").asText()).isEqualTo("APPROVED");

        // DELETE (create a separate draft to test delete)
        Map<String, Object> draftRequest = new HashMap<>();
        draftRequest.put("payrollPeriod", "2028-06");
        APIResponse draftResponse = post("/api/payroll", draftRequest);
        assertThat(draftResponse.status()).isEqualTo(201);
        String draftId = parse(draftResponse).get("id").asText();

        APIResponse deleteResponse = delete("/api/payroll/" + draftId);
        assertThat(deleteResponse.status()).isEqualTo(204);
        log.info("Draft payroll deleted: id={}", draftId);
    }

    @Test
    @DisplayName("Payroll list with filters")
    void payrollListWithFilters() {
        APIResponse listResponse = get("/api/payroll");
        assertThat(listResponse.status()).isEqualTo(200);
    }

    @Test
    @DisplayName("PPh 21 summary endpoint returns valid response")
    void pph21Summary() throws Exception {
        // Query for a year with no posted payroll — should return empty list
        APIResponse summaryResponse = get("/api/payroll/pph21/summary?year=2099");
        assertThat(summaryResponse.status()).isEqualTo(200);

        JsonNode summary = parse(summaryResponse);
        assertThat(summary.get("taxYear").asInt()).isEqualTo(2099);
        assertThat(summary.get("employees").isArray()).isTrue();
        assertThat(summary.get("totalGross").asDouble()).isEqualTo(0.0);
        assertThat(summary.get("totalPph21").asDouble()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("1721-A1 returns 404 for employee with no payroll data")
    void generate1721A1NoData() throws Exception {
        // Get any employee
        APIResponse empResponse = get("/api/employees?active=true");
        JsonNode employees = parse(empResponse);
        assertThat(employees.size()).isGreaterThanOrEqualTo(1);
        String employeeId = employees.get(0).get("id").asText();

        // 1721-A1 for a year with no posted payroll
        APIResponse a1Response = get("/api/payroll/employees/" + employeeId + "/1721-a1?year=2099");
        assertThat(a1Response.status()).isEqualTo(404);
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
        codeRequest.put("clientId", "payroll-api-test");

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

        page.locator("input[name='deviceName']").fill("Payroll API Test Device");
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
