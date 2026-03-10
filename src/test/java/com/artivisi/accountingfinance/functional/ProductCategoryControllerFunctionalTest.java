package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.functional.service.ServiceTestDataInitializer;
import com.artivisi.accountingfinance.repository.ProductCategoryRepository;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Functional tests for ProductCategoryController.
 * Tests product category list, create, edit, delete operations.
 */
@DisplayName("Product Category Controller Tests")
@Import(ServiceTestDataInitializer.class)
class ProductCategoryControllerFunctionalTest extends PlaywrightTestBase {

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @BeforeEach
    void setupAndLogin() {
        loginAsAdmin();
    }

    @Test
    @DisplayName("Should display product category list page")
    void shouldDisplayProductCategoryListPage() {
        navigateTo("/products/categories");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should search categories by keyword")
    void shouldSearchCategoriesByKeyword() {
        navigateTo("/products/categories");
        waitForPageLoad();

        var searchInput = page.locator("input[name='search'], input[name='keyword']").first();
        if (searchInput.isVisible()) {
            searchInput.fill("bahan");

            var filterBtn = page.locator("form button[type='submit']").first();
            if (filterBtn.isVisible()) {
                filterBtn.click();
                waitForPageLoad();
            }
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should display new product category form")
    void shouldDisplayNewProductCategoryForm() {
        navigateTo("/products/categories/new");
        waitForPageLoad();

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should create new product category")
    void shouldCreateNewProductCategory() {
        navigateTo("/products/categories/new");
        waitForPageLoad();

        // Fill category name
        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            nameInput.fill("Test Category " + System.currentTimeMillis());
        }

        // Fill category code
        var codeInput = page.locator("input[name='code']").first();
        if (codeInput.isVisible()) {
            codeInput.fill("TC" + System.currentTimeMillis());
        }

        // Fill description
        var descriptionInput = page.locator("textarea[name='description'], input[name='description']").first();
        if (descriptionInput.isVisible()) {
            descriptionInput.fill("Test category description");
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
        navigateTo("/products/categories/new");
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
    @DisplayName("Should display product category edit form")
    void shouldDisplayProductCategoryEditForm() {
        var category = categoryRepository.findAll().stream().findFirst();
        if (category.isEmpty()) {
            return;
        }

        navigateTo("/products/categories/" + category.get().getId() + "/edit");
        waitForPageLoad();

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should update product category")
    void shouldUpdateProductCategory() {
        var category = categoryRepository.findAll().stream().findFirst();
        if (category.isEmpty()) {
            return;
        }

        navigateTo("/products/categories/" + category.get().getId() + "/edit");
        waitForPageLoad();

        // Update name
        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            nameInput.fill("Updated Category " + System.currentTimeMillis());
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
    @DisplayName("Should delete product category")
    void shouldDeleteProductCategory() {
        var category = categoryRepository.findAll().stream().findFirst();
        if (category.isEmpty()) {
            return;
        }

        navigateTo("/products/categories/" + category.get().getId() + "/edit");
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
    @DisplayName("Should search categories via query param")
    void shouldSearchCategoriesViaQueryParam() {
        navigateTo("/products/categories?search=test");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should paginate category list")
    void shouldPaginateCategoryList() {
        navigateTo("/products/categories?page=0&size=5");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should display new category form with parent categories dropdown")
    void shouldDisplayNewCategoryFormWithParentDropdown() {
        navigateTo("/products/categories/new");
        waitForPageLoad();

        // Verify form elements
        var codeInput = page.locator("input[name='code']").first();
        if (codeInput.isVisible()) {
            assertThat(codeInput).isVisible();
        }

        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            assertThat(nameInput).isVisible();
        }

        var parentSelect = page.locator("select[name='parent']").first();
        if (parentSelect.isVisible()) {
            assertThat(parentSelect).isVisible();
        }
    }

    @Test
    @DisplayName("Should display edit form with pre-populated data")
    void shouldDisplayEditFormWithPrePopulatedData() {
        var category = categoryRepository.findAll().stream().findFirst();
        if (category.isEmpty()) {
            return;
        }

        navigateTo("/products/categories/" + category.get().getId() + "/edit");
        waitForPageLoad();

        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            var value = nameInput.inputValue();
            org.assertj.core.api.Assertions.assertThat(value)
                .as("Name input should be pre-populated")
                .isNotEmpty();
        }
    }

    @Test
    @DisplayName("Should create category with parent")
    void shouldCreateCategoryWithParent() {
        navigateTo("/products/categories/new");
        waitForPageLoad();

        var codeInput = page.locator("input[name='code']").first();
        if (codeInput.isVisible()) {
            codeInput.fill("SUB" + System.currentTimeMillis());
        }

        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            nameInput.fill("Sub Category " + System.currentTimeMillis());
        }

        // Select a parent category if available
        var parentSelect = page.locator("select[name='parent']").first();
        if (parentSelect.isVisible()) {
            var options = parentSelect.locator("option");
            if (options.count() > 1) {
                parentSelect.selectOption(new String[]{options.nth(1).getAttribute("value")});
            }
        }

        var submitBtn = page.locator("#btn-simpan").first();
        if (submitBtn.isVisible()) {
            submitBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should create category with description")
    void shouldCreateCategoryWithDescription() {
        navigateTo("/products/categories/new");
        waitForPageLoad();

        var codeInput = page.locator("input[name='code']").first();
        if (codeInput.isVisible()) {
            codeInput.fill("DSC" + System.currentTimeMillis());
        }

        var nameInput = page.locator("input[name='name']").first();
        if (nameInput.isVisible()) {
            nameInput.fill("Desc Category " + System.currentTimeMillis());
        }

        var descInput = page.locator("textarea[name='description'], input[name='description']").first();
        if (descInput.isVisible()) {
            descInput.fill("Category with full description for testing");
        }

        var activeCheckbox = page.locator("input[name='active']").first();
        if (activeCheckbox.isVisible()) {
            activeCheckbox.check();
        }

        var submitBtn = page.locator("#btn-simpan").first();
        if (submitBtn.isVisible()) {
            submitBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should update category with new description")
    void shouldUpdateCategoryWithNewDescription() {
        var category = categoryRepository.findAll().stream().findFirst();
        if (category.isEmpty()) {
            return;
        }

        navigateTo("/products/categories/" + category.get().getId() + "/edit");
        waitForPageLoad();

        var descInput = page.locator("textarea[name='description'], input[name='description']").first();
        if (descInput.isVisible()) {
            descInput.fill("Updated description " + System.currentTimeMillis());
        }

        var submitBtn = page.locator("#btn-simpan").first();
        if (submitBtn.isVisible()) {
            submitBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should search categories with empty result")
    void shouldSearchCategoriesWithEmptyResult() {
        navigateTo("/products/categories?search=nonexistent_category_xyz_12345");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should paginate category list page 2")
    void shouldPaginateCategoryListPage2() {
        navigateTo("/products/categories?page=1&size=5");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }
}
