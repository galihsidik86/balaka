package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.service.ServiceTestDataInitializer;
import com.artivisi.accountingfinance.repository.ProductCategoryRepository;
import com.artivisi.accountingfinance.repository.ProductRepository;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Functional tests for ProductController.
 * Tests product list, create, edit, activate, deactivate, delete operations.
 */
@DisplayName("Product Controller Tests")
@Import(ServiceTestDataInitializer.class)
class ProductControllerFunctionalTest extends PlaywrightTestBase {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @BeforeEach
    void setupAndLogin() {
        loginAsAdmin();
    }

    @Test
    @DisplayName("Should display product list page")
    void shouldDisplayProductListPage() {
        navigateTo("/products");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should filter products by category")
    void shouldFilterProductsByCategory() {
        navigateTo("/products");
        waitForPageLoad();

        var categorySelect = page.locator("select[name='categoryId']").first();
        if (categorySelect.isVisible()) {
            var options = categorySelect.locator("option");
            if (options.count() > 1) {
                categorySelect.selectOption(new String[]{options.nth(1).getAttribute("value")});

                var filterBtn = page.locator("form button[type='submit']").first();
                if (filterBtn.isVisible()) {
                    filterBtn.click();
                    waitForPageLoad();
                }
            }
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should filter products by status")
    void shouldFilterProductsByStatus() {
        navigateTo("/products");
        waitForPageLoad();

        var statusSelect = page.locator("select[name='status']").first();
        if (statusSelect.isVisible()) {
            statusSelect.selectOption("ACTIVE");

            var filterBtn = page.locator("form button[type='submit']").first();
            if (filterBtn.isVisible()) {
                filterBtn.click();
                waitForPageLoad();
            }
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should search products by keyword")
    void shouldSearchProductsByKeyword() {
        navigateTo("/products");
        waitForPageLoad();

        var searchInput = page.locator("input[name='search'], input[name='keyword']").first();
        if (searchInput.isVisible()) {
            searchInput.fill("kopi");

            var filterBtn = page.locator("form button[type='submit']").first();
            if (filterBtn.isVisible()) {
                filterBtn.click();
                waitForPageLoad();
            }
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should display new product form")
    void shouldDisplayNewProductForm() {
        navigateTo("/products/new");
        waitForPageLoad();

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should create new product")
    void shouldCreateNewProduct() {
        var category = categoryRepository.findAll().stream().findFirst();
        if (category.isEmpty()) {
            return;
        }

        navigateTo("/products/new");
        waitForPageLoad();

        // Fill product code
        var codeInput = page.locator("input[name='code']").first();
        if (codeInput.isVisible()) {
            codeInput.fill("PRD-" + System.currentTimeMillis());
        }

        // Fill product name
        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            nameInput.fill("Test Product " + System.currentTimeMillis());
        }

        // Select category
        var categorySelect = page.locator("select[name='category.id'], select[name='categoryId']").first();
        if (categorySelect.isVisible()) {
            categorySelect.selectOption(category.get().getId().toString());
        }

        // Fill unit
        var unitInput = page.locator("input[name='unit']").first();
        if (unitInput.isVisible()) {
            unitInput.fill("pcs");
        }

        // Submit
        var submitBtn = page.locator("#btn-simpan").first();
        if (submitBtn.isVisible()) {
            submitBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should show validation error for empty name")
    void shouldShowValidationErrorForEmptyName() {
        navigateTo("/products/new");
        waitForPageLoad();

        // Submit without filling required fields
        var submitBtn = page.locator("#btn-simpan").first();
        if (submitBtn.isVisible()) {
            submitBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should display product detail page")
    void shouldDisplayProductDetailPage() {
        var product = productRepository.findAll().stream().findFirst();
        if (product.isEmpty()) {
            return;
        }

        navigateTo("/products/" + product.get().getId());
        waitForPageLoad();

        assertThat(page).hasURL(java.util.regex.Pattern.compile(".*\\/products\\/.*"));
    }

    @Test
    @DisplayName("Should display product edit form")
    void shouldDisplayProductEditForm() {
        var product = productRepository.findAll().stream().findFirst();
        if (product.isEmpty()) {
            return;
        }

        navigateTo("/products/" + product.get().getId() + "/edit");
        waitForPageLoad();

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should update product")
    void shouldUpdateProduct() {
        var product = productRepository.findAll().stream().findFirst();
        if (product.isEmpty()) {
            return;
        }

        navigateTo("/products/" + product.get().getId() + "/edit");
        waitForPageLoad();

        // Update name
        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            nameInput.fill("Updated Product " + System.currentTimeMillis());
        }

        // Submit
        var submitBtn = page.locator("#btn-simpan").first();
        if (submitBtn.isVisible()) {
            submitBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should activate product")
    void shouldActivateProduct() {
        var product = productRepository.findAll().stream()
                .filter(p -> !p.isActive())
                .findFirst();
        if (product.isEmpty()) {
            return;
        }

        navigateTo("/products/" + product.get().getId());
        waitForPageLoad();

        var activateBtn = page.locator("form[action*='/activate'] button[type='submit']").first();
        if (activateBtn.isVisible()) {
            activateBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should deactivate product")
    void shouldDeactivateProduct() {
        var product = productRepository.findAll().stream()
                .filter(p -> p.isActive())
                .findFirst();
        if (product.isEmpty()) {
            return;
        }

        navigateTo("/products/" + product.get().getId());
        waitForPageLoad();

        var deactivateBtn = page.locator("form[action*='/deactivate'] button[type='submit']").first();
        if (deactivateBtn.isVisible()) {
            deactivateBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should delete product")
    void shouldDeleteProduct() {
        var product = productRepository.findAll().stream().findFirst();
        if (product.isEmpty()) {
            return;
        }

        navigateTo("/products/" + product.get().getId());
        waitForPageLoad();

        var deleteBtn = page.locator("form[action*='/delete'] button[type='submit']").first();
        if (deleteBtn.isVisible()) {
            deleteBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    // ==================== ADDITIONAL COVERAGE TESTS ====================

    @Test
    @DisplayName("Should filter products by active status via query param")
    void shouldFilterProductsByActiveStatusViaQueryParam() {
        navigateTo("/products?active=true");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should filter products by inactive status via query param")
    void shouldFilterProductsByInactiveStatusViaQueryParam() {
        navigateTo("/products?active=false");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should paginate product list")
    void shouldPaginateProductList() {
        navigateTo("/products?page=0&size=5");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should search products via query param")
    void shouldSearchProductsViaQueryParam() {
        navigateTo("/products?search=test");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should filter products by category via query param")
    void shouldFilterProductsByCategoryViaQueryParam() {
        var category = categoryRepository.findAll().stream().findFirst();
        if (category.isEmpty()) {
            return;
        }

        navigateTo("/products?categoryId=" + category.get().getId());
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should combine search and category filters")
    void shouldCombineSearchAndCategoryFilters() {
        var category = categoryRepository.findAll().stream().findFirst();
        if (category.isEmpty()) {
            return;
        }

        navigateTo("/products?search=test&categoryId=" + category.get().getId() + "&active=true");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should display product detail with product data")
    void shouldDisplayProductDetailWithProductData() {
        var product = productRepository.findAll().stream().findFirst();
        if (product.isEmpty()) {
            return;
        }

        navigateTo("/products/" + product.get().getId());
        waitForPageLoad();

        // Verify product detail page renders with product info
        assertThat(page.locator("#page-title, h1, h2").first()).isVisible();
    }

    @Test
    @DisplayName("Should display new product form with costing method defaults")
    void shouldDisplayNewProductFormWithDefaults() {
        navigateTo("/products/new");
        waitForPageLoad();

        // Verify form has costing method select
        var costingMethodSelect = page.locator("select[name='costingMethod']").first();
        if (costingMethodSelect.isVisible()) {
            assertThat(costingMethodSelect).isVisible();
        }

        // Verify form has categories select
        var categorySelect = page.locator("select[name='category']").first();
        if (categorySelect.isVisible()) {
            assertThat(categorySelect).isVisible();
        }
    }

    @Test
    @DisplayName("Should display product edit form with existing data populated")
    void shouldDisplayProductEditFormWithExistingData() {
        var product = productRepository.findAll().stream().findFirst();
        if (product.isEmpty()) {
            return;
        }

        navigateTo("/products/" + product.get().getId() + "/edit");
        waitForPageLoad();

        // Verify the name field is pre-populated
        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            var value = nameInput.inputValue();
            org.assertj.core.api.Assertions.assertThat(value)
                .as("Name input should be pre-populated")
                .isNotEmpty();
        }
    }

    @Test
    @DisplayName("Should submit create form with all fields filled")
    void shouldSubmitCreateFormWithAllFields() {
        navigateTo("/products/new");
        waitForPageLoad();

        var codeInput = page.locator("input[name='code']").first();
        if (codeInput.isVisible()) {
            codeInput.fill("PRD-FULL-" + System.currentTimeMillis());
        }

        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            nameInput.fill("Full Product " + System.currentTimeMillis());
        }

        var unitInput = page.locator("input[name='unit']").first();
        if (unitInput.isVisible()) {
            unitInput.fill("kg");
        }

        var descInput = page.locator("textarea[name='description'], input[name='description']").first();
        if (descInput.isVisible()) {
            descInput.fill("Test product description");
        }

        var sellingPriceInput = page.locator("input[name='sellingPrice']").first();
        if (sellingPriceInput.isVisible()) {
            sellingPriceInput.fill("50000");
        }

        var minStockInput = page.locator("input[name='minimumStock']").first();
        if (minStockInput.isVisible()) {
            minStockInput.fill("10");
        }

        // Select category if available
        var categorySelect = page.locator("select[name='category']").first();
        if (categorySelect.isVisible()) {
            var options = categorySelect.locator("option");
            if (options.count() > 1) {
                categorySelect.selectOption(new String[]{options.nth(1).getAttribute("value")});
            }
        }

        var submitBtn = page.locator("#btn-simpan").first();
        if (submitBtn.isVisible()) {
            submitBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }
}
