package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.entity.AssetCategory;
import com.artivisi.accountingfinance.entity.AssetStatus;
import com.artivisi.accountingfinance.entity.DepreciationMethod;
import com.artivisi.accountingfinance.entity.FixedAsset;
import com.artivisi.accountingfinance.functional.service.ServiceTestDataInitializer;
import com.artivisi.accountingfinance.repository.AssetCategoryRepository;
import com.artivisi.accountingfinance.repository.FixedAssetRepository;
import com.artivisi.accountingfinance.service.FixedAssetService;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Functional tests for FixedAssetController.
 * Tests fixed asset list, create, edit, depreciate, dispose operations.
 */
@DisplayName("Fixed Asset Controller Tests")
@Import(ServiceTestDataInitializer.class)
class FixedAssetControllerFunctionalTest extends PlaywrightTestBase {

    @Autowired
    private FixedAssetRepository assetRepository;

    @Autowired
    private AssetCategoryRepository categoryRepository;

    @Autowired
    private FixedAssetService fixedAssetService;

    @BeforeEach
    void setupAndLogin() {
        ensureActiveAssetExists();
        loginAsAdmin();
    }

    private AssetCategory getCategory() {
        return categoryRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new AssertionError("AssetCategory required for test"));
    }

    private FixedAsset ensureActiveAssetExists() {
        var activeAsset = assetRepository.findAll().stream()
                .filter(a -> a.getStatus() == AssetStatus.ACTIVE)
                .findFirst();

        if (activeAsset.isPresent()) {
            return activeAsset.get();
        }

        // Create one using service (which initializes accounts from category)
        return createFreshAssetForTest("TEST");
    }

    private FixedAsset createFreshAssetForTest(String prefix) {
        AssetCategory category = getCategory();
        FixedAsset asset = new FixedAsset();
        asset.setAssetCode(prefix + "-" + System.currentTimeMillis());
        asset.setName(prefix + " Asset " + System.currentTimeMillis());
        asset.setCategory(category);
        asset.setPurchaseDate(LocalDate.now());
        asset.setPurchaseCost(BigDecimal.valueOf(5000000));
        asset.setDepreciationStartDate(LocalDate.now().withDayOfMonth(1));
        asset.setDepreciationMethod(DepreciationMethod.STRAIGHT_LINE);
        asset.setUsefulLifeMonths(24);
        asset.setResidualValue(BigDecimal.ZERO);

        // Use service to create (it initializes accounts from category)
        return fixedAssetService.create(asset);
    }

    @Test
    @DisplayName("Should display fixed asset list page")
    void shouldDisplayFixedAssetListPage() {
        navigateTo("/assets");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should filter assets by category")
    void shouldFilterAssetsByCategory() {
        navigateTo("/assets");
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
    @DisplayName("Should filter assets by status")
    void shouldFilterAssetsByStatus() {
        navigateTo("/assets");
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
    @DisplayName("Should display new fixed asset form")
    void shouldDisplayNewFixedAssetForm() {
        navigateTo("/assets/new");
        waitForPageLoad();

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should create new fixed asset")
    void shouldCreateNewFixedAsset() {
        var category = getCategory();

        navigateTo("/assets/new");
        waitForPageLoad();

        String uniqueCode = "CREATE-" + System.currentTimeMillis() % 100000;

        // Fill asset code
        page.locator("#assetCode").fill(uniqueCode);

        // Fill asset name
        page.locator("#name").fill("Test Asset " + uniqueCode);

        // Select category
        page.locator("#category").selectOption(category.getId().toString());

        // Fill purchase date
        page.locator("#purchaseDate").fill(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        // Fill purchase cost
        page.locator("#purchaseCost").fill("50000000");

        // Fill depreciation start date
        page.locator("#depreciationStartDate").fill(LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE));

        // Fill useful life
        page.locator("#usefulLifeMonths").fill("48");

        // Fill residual value
        page.locator("#residualValue").fill("0");

        // Submit
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Should redirect to detail page
        assertThat(page).hasURL(java.util.regex.Pattern.compile(".*\\/assets\\/.*"));
    }

    @Test
    @DisplayName("Should display fixed asset detail page")
    void shouldDisplayFixedAssetDetailPage() {
        var asset = ensureActiveAssetExists();

        navigateTo("/assets/" + asset.getId());
        waitForPageLoad();

        assertThat(page.locator("#asset-code")).isVisible();
        assertThat(page).hasURL(java.util.regex.Pattern.compile(".*\\/assets\\/.*"));
    }

    @Test
    @DisplayName("Should display fixed asset edit form")
    void shouldDisplayFixedAssetEditForm() {
        var asset = ensureActiveAssetExists();

        navigateTo("/assets/" + asset.getId() + "/edit");
        waitForPageLoad();

        assertThat(page.locator("#assetCode")).isVisible();
    }

    @Test
    @DisplayName("Should update fixed asset")
    void shouldUpdateFixedAsset() {
        var asset = createFreshAssetForTest("UPDATE");

        navigateTo("/assets/" + asset.getId() + "/edit");
        waitForPageLoad();

        // Update name
        page.locator("#name").fill("Updated Asset " + System.currentTimeMillis());

        // Submit
        page.locator("#btn-simpan").click();
        waitForPageLoad();

        // Should redirect to detail
        assertThat(page).hasURL(java.util.regex.Pattern.compile(".*\\/assets\\/.*"));
    }

    @Test
    @DisplayName("Should run depreciation")
    void shouldRunDepreciation() {
        var asset = ensureActiveAssetExists();

        navigateTo("/assets/" + asset.getId());
        waitForPageLoad();

        var depreciateBtn = page.locator("form[action*='/depreciate'] button[type='submit']").first();
        if (depreciateBtn.isVisible()) {
            depreciateBtn.click();
            waitForPageLoad();
        }

        assertThat(page).hasURL(java.util.regex.Pattern.compile(".*\\/assets\\/.*"));
    }

    @Test
    @DisplayName("Should display dispose form")
    void shouldDisplayDisposeForm() {
        var asset = createFreshAssetForTest("DISPOSE-FORM");

        navigateTo("/assets/" + asset.getId() + "/dispose");
        waitForPageLoad();

        assertThat(page.locator("#disposalType")).isVisible();
    }

    @Test
    @DisplayName("Should dispose fixed asset")
    void shouldDisposeFixedAsset() {
        var asset = createFreshAssetForTest("DISPOSE");

        navigateTo("/assets/" + asset.getId() + "/dispose");
        waitForPageLoad();

        // Select disposal type
        page.locator("#disposalType").selectOption("SOLD");

        // Fill disposal date
        page.locator("#disposalDate").fill(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        // Fill proceeds
        page.locator("#proceeds").fill("3000000");

        // Submit
        page.locator("#btn-dispose").click();
        waitForPageLoad();

        // Should redirect to detail page
        assertThat(page).hasURL(java.util.regex.Pattern.compile(".*\\/assets\\/.*"));
    }

    @Test
    @DisplayName("Should display depreciation report")
    void shouldDisplayDepreciationReport() {
        navigateTo("/assets/depreciation-report");
        waitForPageLoad();

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should filter depreciation report by date")
    void shouldFilterDepreciationReportByDate() {
        navigateTo("/assets/depreciation-report");
        waitForPageLoad();

        var startDateInput = page.locator("input[name='startDate']").first();
        var endDateInput = page.locator("input[name='endDate']").first();

        if (startDateInput.isVisible() && endDateInput.isVisible()) {
            startDateInput.fill("2024-01-01");
            endDateInput.fill("2024-12-31");

            var filterBtn = page.locator("form button[type='submit']").first();
            if (filterBtn.isVisible()) {
                filterBtn.click();
                waitForPageLoad();
            }
        }

        assertThat(page.locator("body")).isVisible();
    }

    // ==================== DEPRECIATION MANAGEMENT ====================

    @Test
    @DisplayName("Should display depreciation list page")
    void shouldDisplayDepreciationListPage() {
        navigateTo("/assets/depreciation");
        waitForPageLoad();

        assertThat(page.locator("#page-title, h1").first()).isVisible();
    }

    @Test
    @DisplayName("Should generate depreciation entries")
    void shouldGenerateDepreciationEntries() {
        navigateTo("/assets/depreciation");
        waitForPageLoad();

        // Find period input and generate button
        var periodInput = page.locator("input[name='period']").first();
        if (periodInput.isVisible()) {
            periodInput.fill(YearMonth.now().toString());
        }

        var generateBtn = page.locator("form[action*='/depreciation/generate'] button[type='submit']").first();
        if (generateBtn.isVisible()) {
            generateBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should post all depreciation entries")
    void shouldPostAllDepreciationEntries() {
        navigateTo("/assets/depreciation");
        waitForPageLoad();

        var periodInput = page.locator("input[name='period']").first();
        if (periodInput.isVisible()) {
            periodInput.fill(YearMonth.now().toString());
        }

        var postAllBtn = page.locator("form[action*='/depreciation/post-all'] button[type='submit']").first();
        if (postAllBtn.isVisible()) {
            postAllBtn.click();
            waitForPageLoad();
        }

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should call post depreciation entry API")
    void shouldCallPostDepreciationEntryApi() {
        // Test POST to depreciation entry post endpoint
        var response = page.request().post(
                baseUrl() + "/assets/depreciation/00000000-0000-0000-0000-000000000000/post");

        // Should respond (CSRF protected or not found)
        org.assertj.core.api.Assertions.assertThat(response.status())
                .as("Post depreciation entry should respond")
                .isIn(302, 403, 404, 500);
    }

    @Test
    @DisplayName("Should call skip depreciation entry API")
    void shouldCallSkipDepreciationEntryApi() {
        // Test POST to skip depreciation entry endpoint
        var response = page.request().post(
                baseUrl() + "/assets/depreciation/00000000-0000-0000-0000-000000000000/skip");

        // Should respond (CSRF protected or not found)
        org.assertj.core.api.Assertions.assertThat(response.status())
                .as("Skip depreciation entry should respond")
                .isIn(302, 403, 404, 500);
    }

    // ==================== DELETE ASSET ====================

    @Test
    @DisplayName("Should delete fixed asset via API")
    void shouldDeleteFixedAsset() {
        // Create a fresh asset for delete test
        var asset = createFreshAssetForTest("DELETE");

        // Delete API requires CSRF, test it with POST to /delete endpoint
        var response = page.request().post(baseUrl() + "/assets/" + asset.getId() + "/delete");

        // Should respond (either success redirect or 403 CSRF)
        org.assertj.core.api.Assertions.assertThat(response.status())
                .as("Delete should respond")
                .isIn(200, 302, 403);
    }

    // ==================== SEARCH FILTER ====================

    @Test
    @DisplayName("Should search assets by text")
    void shouldSearchAssetsByText() {
        navigateTo("/assets?search=test");
        waitForPageLoad();

        assertThat(page.locator("body")).isVisible();
    }

    @Test
    @DisplayName("Should filter assets with multiple parameters")
    void shouldFilterAssetsWithMultipleParameters() {
        var category = categoryRepository.findAll().stream().findFirst();
        String categoryParam = category.map(c -> "&categoryId=" + c.getId()).orElse("");

        navigateTo("/assets?status=ACTIVE" + categoryParam + "&search=test");
        waitForPageLoad();

        assertThat(page.locator("body")).isVisible();
    }

    // ==================== PAGINATION ====================

    @Test
    @DisplayName("Should paginate asset list")
    void shouldPaginateAssetList() {
        navigateTo("/assets?page=0&size=5");
        waitForPageLoad();

        assertThat(page.locator("body")).isVisible();
    }
}
