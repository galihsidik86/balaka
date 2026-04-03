package com.artivisi.accountingfinance.manual;

import com.artivisi.accountingfinance.functional.service.ServiceTestDataInitializer;
import com.artivisi.accountingfinance.ui.PlaywrightTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Manual Screenshot Capture")
@ActiveProfiles("screenshot")
@Import(ServiceTestDataInitializer.class)
class ScreenshotCaptureTest extends PlaywrightTestBase {

    private static final Path SCREENSHOTS_DIR = Paths.get("target", "user-manual", "screenshots");

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(SCREENSHOTS_DIR);
        loginAsAdmin();
    }

    @Test
    @DisplayName("Should capture all page screenshots")
    void shouldCaptureAllPageScreenshots() {
        for (ScreenshotCapture.PageDefinition pageDef : ScreenshotCapture.getPageDefinitions()) {
            capturePageScreenshot(pageDef);
        }
    }

    private void capturePageScreenshot(ScreenshotCapture.PageDefinition pageDef) {
        System.out.printf("Capturing: %s (%s)%n", pageDef.name(), pageDef.url());

        try {
            navigateTo(pageDef.url());
            waitForPageLoad();

            // Wait for HTMX to complete (extra time for dynamic content)
            page.waitForTimeout(500);

            // Take screenshot
            Path screenshotPath = SCREENSHOTS_DIR.resolve(pageDef.id() + ".png");
            page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(false));

            System.out.printf("  Saved: %s%n", screenshotPath);
            assertThat(Files.exists(screenshotPath)).isTrue();
        } catch (Exception e) {
            System.err.printf("  Failed to capture %s: %s%n", pageDef.name(), e.getMessage());
        }
    }

    @Test
    @DisplayName("Should generate user manual HTML")
    void shouldGenerateUserManualHtml() throws Exception {
        Path markdownDir = Paths.get("docs", "user-manual");
        Path outputDir = Paths.get("target", "user-manual");
        Path screenshotsDir = SCREENSHOTS_DIR;

        UserManualGenerator generator = new UserManualGenerator(markdownDir, outputDir, screenshotsDir);
        generator.generate();

        Path indexPath = outputDir.resolve("index.html");
        assertThat(Files.exists(indexPath)).isTrue();

        // Verify all section group pages are generated
        // Each SectionGroup produces a {group-id}.html file (except "beranda" which is index.html)
        for (UserManualGenerator.SectionGroup group : UserManualGenerator.getSectionGroups()) {
            if ("beranda".equals(group.id())) continue;
            Path groupPage = outputDir.resolve(group.id() + ".html");
            assertThat(Files.exists(groupPage))
                    .as("Page should exist: " + group.id() + ".html")
                    .isTrue();

            // Verify all sections within this group page are present
            String html = Files.readString(groupPage);
            for (UserManualGenerator.Section section : group.sections()) {
                assertThat(html).contains("id=\"" + section.id() + "\"");
            }
        }
    }
}
