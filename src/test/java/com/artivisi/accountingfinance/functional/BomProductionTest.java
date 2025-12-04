package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.page.BomDetailPage;
import com.artivisi.accountingfinance.functional.page.BomFormPage;
import com.artivisi.accountingfinance.functional.page.BomListPage;
import com.artivisi.accountingfinance.functional.page.InventoryPurchaseFormPage;
import com.artivisi.accountingfinance.functional.page.LoginPage;
import com.artivisi.accountingfinance.functional.page.ProductFormPage;
import com.artivisi.accountingfinance.functional.page.ProductionOrderDetailPage;
import com.artivisi.accountingfinance.functional.page.ProductionOrderFormPage;
import com.artivisi.accountingfinance.functional.page.ProductionOrderListPage;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BOM and Production (Phase 5.4)")
class BomProductionTest extends PlaywrightTestBase {

    private LoginPage loginPage;
    private BomListPage bomListPage;
    private BomFormPage bomFormPage;
    private BomDetailPage bomDetailPage;
    private ProductionOrderListPage productionListPage;
    private ProductionOrderFormPage productionFormPage;
    private ProductionOrderDetailPage productionDetailPage;
    private ProductFormPage productFormPage;
    private InventoryPurchaseFormPage purchaseFormPage;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage(page, baseUrl());
        bomListPage = new BomListPage(page, baseUrl());
        bomFormPage = new BomFormPage(page, baseUrl());
        bomDetailPage = new BomDetailPage(page, baseUrl());
        productionListPage = new ProductionOrderListPage(page, baseUrl());
        productionFormPage = new ProductionOrderFormPage(page, baseUrl());
        productionDetailPage = new ProductionOrderDetailPage(page, baseUrl());
        productFormPage = new ProductFormPage(page, baseUrl());
        purchaseFormPage = new InventoryPurchaseFormPage(page, baseUrl());

        loginPage.navigate().loginAsAdmin();
    }

    private String createProduct(String codePrefix) {
        String code = codePrefix + System.currentTimeMillis() % 100000;
        productFormPage.navigateToNew();
        productFormPage.fillCode(code);
        productFormPage.fillName("Product " + code);
        productFormPage.fillUnit("pcs");
        productFormPage.selectCostingMethod("WEIGHTED_AVERAGE");
        productFormPage.clickSubmit();
        return code;
    }

    private String extractProductIdFromList(String code) {
        page.navigate(baseUrl() + "/products");
        page.waitForLoadState();
        page.fill("#search-input", code);
        page.waitForTimeout(500);
        page.click("a:has-text('" + code + "')");
        page.waitForLoadState();
        String url = page.url();
        return url.substring(url.lastIndexOf("/") + 1);
    }

    @Test
    @DisplayName("Should display BOM list page")
    void shouldDisplayBomListPage() {
        bomListPage.navigate();

        bomListPage.assertPageTitleVisible();
        bomListPage.assertPageTitleText("Bill of Materials");
        bomListPage.assertTableVisible();
    }

    @Test
    @DisplayName("Should display BOM form")
    void shouldDisplayBomForm() {
        bomFormPage.navigate();

        bomFormPage.assertPageTitleText("Tambah Bill of Materials");
    }

    @Test
    @DisplayName("Should display Production Order list page")
    void shouldDisplayProductionOrderListPage() {
        productionListPage.navigate();

        productionListPage.assertPageTitleVisible();
        productionListPage.assertPageTitleText("Production Orders");
        productionListPage.assertTableVisible();
    }

    @Test
    @DisplayName("Should display Production Order form")
    void shouldDisplayProductionOrderForm() {
        productionFormPage.navigate();

        productionFormPage.assertPageTitleText("Buat Production Order");
    }

    @Test
    @DisplayName("Should create BOM and Production Order")
    void shouldCreateBomAndProductionOrder() {
        // Create products
        String finishedCode = createProduct("FIN");
        String finishedId = extractProductIdFromList(finishedCode);
        String componentCode = createProduct("CMP");
        String componentId = extractProductIdFromList(componentCode);

        // Create BOM
        String bomCode = "BOM" + System.currentTimeMillis() % 100000;
        bomFormPage.navigate();
        bomFormPage.fillCode(bomCode);
        bomFormPage.fillName("Test BOM " + bomCode);
        bomFormPage.selectProductByValue(finishedId);
        bomFormPage.fillOutputQuantity("1");
        bomFormPage.addComponentLine(componentId, "2", "Test component");
        bomFormPage.clickSubmit();

        // Verify BOM created - check success message
        bomListPage.assertPageTitleText("Bill of Materials");
        assertThat(bomListPage.hasSuccessMessage()).isTrue();

        // Verify BOM appears in list with correct data
        assertThat(page.locator("td:has-text('" + bomCode + "')").count()).isGreaterThan(0);

        // Navigate to BOM detail and verify data
        bomListPage.clickDetailLink(bomCode);
        String bomDetailUrl = page.url();
        String bomId = bomDetailUrl.substring(bomDetailUrl.lastIndexOf("/") + 1);

        // Verify BOM detail shows correct product and component
        assertThat(page.locator("dd:has-text('" + finishedCode + "')").count()).isGreaterThan(0);
        assertThat(page.locator("td:has-text('" + componentCode + "')").count()).isGreaterThan(0);

        // Create Production Order
        productionFormPage.navigate();
        productionFormPage.selectBomByValue(bomId);
        productionFormPage.fillQuantity("10");
        productionFormPage.fillOrderDate(LocalDate.now().toString());
        productionFormPage.fillNotes("Test production order");
        productionFormPage.clickSubmit();

        // Verify Production Order created
        productionListPage.assertPageTitleText("Production Orders");
        assertThat(productionListPage.hasSuccessMessage()).isTrue();

        // Verify Production Order appears in list
        assertThat(page.locator("td:has-text('PO-')").count()).isGreaterThan(0);
        assertThat(page.locator("td:has-text('" + bomCode + "')").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should complete production workflow")
    void shouldCompleteProductionWorkflow() {
        // Create products
        String finishedCode = createProduct("FNW");
        String finishedId = extractProductIdFromList(finishedCode);
        String componentCode = createProduct("CPW");
        String componentId = extractProductIdFromList(componentCode);

        // Create a BOM (output 1 finished good requires 2 components)
        String bomCode = "BOMWF" + System.currentTimeMillis() % 100000;
        bomFormPage.navigate();
        bomFormPage.fillCode(bomCode);
        bomFormPage.fillName("Workflow BOM " + bomCode);
        bomFormPage.selectProductByValue(finishedId);
        bomFormPage.fillOutputQuantity("1");
        bomFormPage.addComponentLine(componentId, "2", null);
        bomFormPage.clickSubmit();

        // Verify BOM created
        assertThat(bomListPage.hasSuccessMessage()).isTrue();

        // Get BOM ID
        bomListPage.navigate();
        bomListPage.clickDetailLink(bomCode);
        String bomDetailUrl = page.url();
        String bomId = bomDetailUrl.substring(bomDetailUrl.lastIndexOf("/") + 1);

        // Purchase component stock (100 units @ 10000 each)
        purchaseFormPage.navigate();
        purchaseFormPage.selectProductByValue(componentId);
        purchaseFormPage.fillDate(LocalDate.now().toString());
        purchaseFormPage.fillQuantity("100");
        purchaseFormPage.fillUnitCost("10000");
        purchaseFormPage.fillReference("PO-PROD-TEST");
        purchaseFormPage.clickSubmit();

        // Verify component stock by checking stock card page
        page.navigate(baseUrl() + "/inventory/stock/" + componentId);
        page.waitForLoadState();
        // Stock displays as "100" or formatted "100,00"
        assertThat(page.locator("text=Stok Saat Ini").count()).isGreaterThan(0);
        assertThat(page.locator(".text-primary-600:has-text('100')").count()).isGreaterThan(0);

        // Create Production Order (produce 5 units, needs 5*2=10 components)
        productionFormPage.navigate();
        productionFormPage.selectBomByValue(bomId);
        productionFormPage.fillQuantity("5");
        productionFormPage.fillOrderDate(LocalDate.now().toString());
        productionFormPage.clickSubmit();

        // Verify order created
        assertThat(productionListPage.hasSuccessMessage()).isTrue();

        // Navigate to detail
        productionListPage.navigate();
        page.locator("a:has-text('Detail')").first().click();
        page.waitForLoadState();

        // Start production
        productionDetailPage.clickStartProduction();
        assertThat(productionDetailPage.hasSuccessMessage()).isTrue();
        productionDetailPage.assertStatusText("Sedang Diproses");

        // Complete production
        productionDetailPage.clickCompleteProduction();
        assertThat(productionDetailPage.hasSuccessMessage()).isTrue();
        productionDetailPage.assertStatusText("Selesai");
        assertThat(productionDetailPage.hasCostSummary()).isTrue();

        // Verify cost calculation: 10 components * 10000 = 100000 total, 100000/5 = 20000 per unit
        assertThat(page.locator("dd:has-text('Rp 100')").count()).isGreaterThan(0); // Total cost 100,000
        assertThat(page.locator("dd:has-text('Rp 20')").count()).isGreaterThan(0);  // Unit cost 20,000

        // Verify component stock reduced (100 - 10 = 90)
        page.navigate(baseUrl() + "/inventory/stock/" + componentId);
        page.waitForLoadState();
        assertThat(page.locator(".text-primary-600:has-text('90')").count()).isGreaterThan(0);

        // Verify finished goods stock increased (0 + 5 = 5)
        page.navigate(baseUrl() + "/inventory/stock/" + finishedId);
        page.waitForLoadState();
        assertThat(page.locator(".text-primary-600:has-text('5')").count()).isGreaterThan(0);
    }
}
