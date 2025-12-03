package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.page.InventoryPurchaseFormPage;
import com.artivisi.accountingfinance.functional.page.InventorySaleFormPage;
import com.artivisi.accountingfinance.functional.page.InventoryStockListPage;
import com.artivisi.accountingfinance.functional.page.InventoryTransactionDetailPage;
import com.artivisi.accountingfinance.functional.page.InventoryTransactionListPage;
import com.artivisi.accountingfinance.functional.page.LoginPage;
import com.artivisi.accountingfinance.functional.page.ProductFormPage;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inventory Transactions (Phase 5.2)")
class InventoryTransactionTest extends PlaywrightTestBase {

    private LoginPage loginPage;
    private InventoryStockListPage stockListPage;
    private InventoryTransactionListPage transactionListPage;
    private InventoryPurchaseFormPage purchaseFormPage;
    private InventorySaleFormPage saleFormPage;
    private InventoryTransactionDetailPage transactionDetailPage;
    private ProductFormPage productFormPage;

    private String testProductCode;
    private String testProductId;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage(page, baseUrl());
        stockListPage = new InventoryStockListPage(page, baseUrl());
        transactionListPage = new InventoryTransactionListPage(page, baseUrl());
        purchaseFormPage = new InventoryPurchaseFormPage(page, baseUrl());
        saleFormPage = new InventorySaleFormPage(page, baseUrl());
        transactionDetailPage = new InventoryTransactionDetailPage(page, baseUrl());
        productFormPage = new ProductFormPage(page, baseUrl());

        loginPage.navigate().loginAsAdmin();

        // Create a test product for inventory transactions
        testProductCode = "INVTEST" + System.currentTimeMillis() % 100000;
        createTestProduct();
    }

    private void createTestProduct() {
        productFormPage.navigateToNew();
        productFormPage.fillCode(testProductCode);
        productFormPage.fillName("Inventory Test Product " + testProductCode);
        productFormPage.fillUnit("pcs");
        productFormPage.selectCostingMethod("WEIGHTED_AVERAGE");
        productFormPage.clickSubmit();

        // Get product ID from URL after creation
        page.navigate(baseUrl() + "/products");
        page.waitForLoadState();
        page.fill("#search-input", testProductCode);
        page.waitForTimeout(500);

        // Click on product to get to detail and extract ID from URL
        page.click("a:has-text('" + testProductCode + "')");
        page.waitForLoadState();
        String url = page.url();
        testProductId = url.substring(url.lastIndexOf("/") + 1);
    }

    @Test
    @DisplayName("Should display stock list page")
    void shouldDisplayStockListPage() {
        stockListPage.navigate();

        stockListPage.assertPageTitleVisible();
        stockListPage.assertPageTitleText("Stok Barang");
    }

    @Test
    @DisplayName("Should display stock table")
    void shouldDisplayStockTable() {
        stockListPage.navigate();

        stockListPage.assertTableVisible();
    }

    @Test
    @DisplayName("Should display transaction list page")
    void shouldDisplayTransactionListPage() {
        transactionListPage.navigate();

        transactionListPage.assertPageTitleVisible();
        transactionListPage.assertPageTitleText("Transaksi Persediaan");
    }

    @Test
    @DisplayName("Should display transaction table")
    void shouldDisplayTransactionTable() {
        transactionListPage.navigate();

        transactionListPage.assertTableVisible();
    }

    @Test
    @DisplayName("Should display purchase form")
    void shouldDisplayPurchaseForm() {
        purchaseFormPage.navigate();

        purchaseFormPage.assertPageTitleText("Pembelian Persediaan");
    }

    @Test
    @DisplayName("Should display sale form")
    void shouldDisplaySaleForm() {
        saleFormPage.navigate();

        saleFormPage.assertPageTitleText("Penjualan Persediaan");
    }

    @Test
    @DisplayName("Should navigate to purchase form from transaction list")
    void shouldNavigateToPurchaseFromTransactionList() {
        transactionListPage.navigate();
        transactionListPage.clickNewTransactionDropdown();
        transactionListPage.clickPurchaseLink();

        purchaseFormPage.assertPageTitleText("Pembelian Persediaan");
    }

    @Test
    @DisplayName("Should navigate to sale form from transaction list")
    void shouldNavigateToSaleFromTransactionList() {
        transactionListPage.navigate();
        transactionListPage.clickNewTransactionDropdown();
        transactionListPage.clickSaleLink();

        saleFormPage.assertPageTitleText("Penjualan Persediaan");
    }

    @Test
    @DisplayName("Should record purchase transaction")
    void shouldRecordPurchaseTransaction() {
        purchaseFormPage.navigate();

        purchaseFormPage.selectProductByValue(testProductId);
        purchaseFormPage.fillDate(java.time.LocalDate.now().toString());
        purchaseFormPage.fillQuantity("100");
        purchaseFormPage.fillUnitCost("10000");
        purchaseFormPage.fillReference("PO-" + System.currentTimeMillis());
        purchaseFormPage.fillNotes("Test purchase");
        purchaseFormPage.clickSubmit();

        // Should redirect to transaction detail
        transactionDetailPage.assertPageTitleText("Detail Transaksi");
        transactionDetailPage.assertTransactionTypeText("Pembelian");
        assertThat(transactionDetailPage.hasSuccessMessage()).isTrue();
    }

    @Test
    @DisplayName("Should update stock after purchase")
    void shouldUpdateStockAfterPurchase() {
        // Record purchase
        purchaseFormPage.navigate();
        purchaseFormPage.selectProductByValue(testProductId);
        purchaseFormPage.fillDate(java.time.LocalDate.now().toString());
        purchaseFormPage.fillQuantity("50");
        purchaseFormPage.fillUnitCost("20000");
        purchaseFormPage.fillReference("PO-TEST");
        purchaseFormPage.clickSubmit();

        // Check stock list
        stockListPage.navigate();
        stockListPage.search(testProductCode);

        assertThat(stockListPage.hasProductWithCode(testProductCode)).isTrue();
    }

    @Test
    @DisplayName("Should record sale transaction after purchase")
    void shouldRecordSaleTransactionAfterPurchase() {
        // First record a purchase
        purchaseFormPage.navigate();
        purchaseFormPage.selectProductByValue(testProductId);
        purchaseFormPage.fillDate(java.time.LocalDate.now().toString());
        purchaseFormPage.fillQuantity("100");
        purchaseFormPage.fillUnitCost("15000");
        purchaseFormPage.fillReference("PO-SALE-TEST");
        purchaseFormPage.clickSubmit();

        // Now record a sale
        saleFormPage.navigate();
        saleFormPage.selectProductByValue(testProductId);
        saleFormPage.fillDate(java.time.LocalDate.now().toString());
        saleFormPage.fillQuantity("30");
        saleFormPage.fillUnitPrice("25000");
        saleFormPage.fillReference("SO-" + System.currentTimeMillis());
        saleFormPage.fillNotes("Test sale");
        saleFormPage.clickSubmit();

        // Should redirect to transaction detail
        transactionDetailPage.assertPageTitleText("Detail Transaksi");
        transactionDetailPage.assertTransactionTypeText("Penjualan");
        assertThat(transactionDetailPage.hasSuccessMessage()).isTrue();
    }

    @Test
    @DisplayName("Should show transaction in list after creation")
    void shouldShowTransactionInListAfterCreation() {
        // Record a purchase
        purchaseFormPage.navigate();
        purchaseFormPage.selectProductByValue(testProductId);
        purchaseFormPage.fillDate(java.time.LocalDate.now().toString());
        purchaseFormPage.fillQuantity("75");
        purchaseFormPage.fillUnitCost("12000");
        purchaseFormPage.fillReference("PO-LIST-TEST");
        purchaseFormPage.clickSubmit();

        // Navigate to transaction list
        transactionListPage.navigate();

        assertThat(transactionListPage.hasTransactionWithType("Pembelian")).isTrue();
    }

    @Test
    @DisplayName("Should view transaction detail from list")
    void shouldViewTransactionDetailFromList() {
        // Record a purchase
        purchaseFormPage.navigate();
        purchaseFormPage.selectProductByValue(testProductId);
        purchaseFormPage.fillDate(java.time.LocalDate.now().toString());
        purchaseFormPage.fillQuantity("25");
        purchaseFormPage.fillUnitCost("8000");
        purchaseFormPage.fillReference("PO-DETAIL-VIEW");
        purchaseFormPage.clickSubmit();

        // Navigate to transaction list and click first item
        transactionListPage.navigate();
        transactionListPage.clickViewTransaction(0);

        transactionDetailPage.assertPageTitleText("Detail Transaksi");
    }
}
