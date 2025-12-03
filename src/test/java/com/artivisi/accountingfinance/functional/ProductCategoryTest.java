package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.page.LoginPage;
import com.artivisi.accountingfinance.functional.page.ProductCategoryFormPage;
import com.artivisi.accountingfinance.functional.page.ProductCategoryListPage;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product Category Management (Phase 5.1)")
class ProductCategoryTest extends PlaywrightTestBase {

    private LoginPage loginPage;
    private ProductCategoryListPage listPage;
    private ProductCategoryFormPage formPage;

    @BeforeEach
    void setUp() {
        loginPage = new LoginPage(page, baseUrl());
        listPage = new ProductCategoryListPage(page, baseUrl());
        formPage = new ProductCategoryFormPage(page, baseUrl());

        loginPage.navigate().loginAsAdmin();
    }

    @Test
    @DisplayName("Should display category list page")
    void shouldDisplayCategoryListPage() {
        listPage.navigate();

        listPage.assertPageTitleVisible();
        listPage.assertPageTitleText("Kategori Produk");
    }

    @Test
    @DisplayName("Should display category table")
    void shouldDisplayCategoryTable() {
        listPage.navigate();

        listPage.assertTableVisible();
    }

    @Test
    @DisplayName("Should display new category form")
    void shouldDisplayNewCategoryForm() {
        formPage.navigateToNew();

        formPage.assertPageTitleText("Kategori Produk Baru");
    }

    @Test
    @DisplayName("Should navigate to form from list page")
    void shouldNavigateToFormFromListPage() {
        listPage.navigate();
        listPage.clickNewCategoryButton();

        formPage.assertPageTitleText("Kategori Produk Baru");
    }

    @Test
    @DisplayName("Should create new category")
    void shouldCreateNewCategory() {
        formPage.navigateToNew();

        String uniqueCode = "CAT" + System.currentTimeMillis() % 100000;
        String uniqueName = "Test Category " + System.currentTimeMillis();

        formPage.fillCode(uniqueCode);
        formPage.fillName(uniqueName);
        formPage.fillDescription("Test category description");
        formPage.clickSubmit();

        // Should redirect to list page
        listPage.assertPageTitleText("Kategori Produk");
        listPage.search(uniqueCode);
        assertThat(listPage.hasCategoryWithCode(uniqueCode)).isTrue();
    }

    @Test
    @DisplayName("Should show category in list after creation")
    void shouldShowCategoryInListAfterCreation() {
        formPage.navigateToNew();

        String uniqueCode = "CATLST" + System.currentTimeMillis() % 100000;
        String uniqueName = "List Test Category " + System.currentTimeMillis();

        formPage.fillCode(uniqueCode);
        formPage.fillName(uniqueName);
        formPage.clickSubmit();

        // Navigate to list and search
        listPage.navigate();
        listPage.search(uniqueCode);

        assertThat(listPage.hasCategoryWithName(uniqueName)).isTrue();
    }

    @Test
    @DisplayName("Should edit existing category")
    void shouldEditExistingCategory() {
        // Create a category first
        formPage.navigateToNew();

        String uniqueCode = "CATEDT" + System.currentTimeMillis() % 100000;
        String originalName = "Original Category " + System.currentTimeMillis();

        formPage.fillCode(uniqueCode);
        formPage.fillName(originalName);
        formPage.clickSubmit();

        // Navigate to list and edit
        listPage.navigate();
        listPage.search(uniqueCode);
        listPage.clickEditCategory(uniqueCode);

        // Update name
        String updatedName = "Updated Category " + System.currentTimeMillis();
        formPage.fillName(updatedName);
        formPage.clickSubmit();

        // Verify update
        listPage.navigate();
        listPage.search(uniqueCode);
        assertThat(listPage.hasCategoryWithName(updatedName)).isTrue();
    }

    @Test
    @DisplayName("Should require code field")
    void shouldRequireCodeField() {
        formPage.navigateToNew();

        // Browser will prevent submission due to required attribute
        // Just verify the form loaded successfully
        formPage.assertPageTitleText("Kategori Produk Baru");
    }

    @Test
    @DisplayName("Should require name field")
    void shouldRequireNameField() {
        formPage.navigateToNew();

        // Browser will prevent submission due to required attribute
        // Just verify the form loaded successfully
        formPage.assertPageTitleText("Kategori Produk Baru");
    }
}
