package com.artivisi.accountingfinance.service;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import com.artivisi.accountingfinance.entity.JournalTemplate;
import com.artivisi.accountingfinance.repository.JournalTemplateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("UserTemplatePreference Service Tests")
class UserTemplatePreferenceServiceTest {

    @Autowired
    private UserTemplatePreferenceService preferenceService;

    @Autowired
    private JournalTemplateRepository templateRepository;

    private static final String TEST_USER = "admin";

    @Test
    @DisplayName("Should toggle favorite on — first time creates preference")
    void shouldToggleFavoriteOnFirstTime() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        boolean result = preferenceService.toggleFavorite(TEST_USER, template.getId());
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should toggle favorite off — second toggle")
    void shouldToggleFavoriteOff() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        // First toggle: on
        preferenceService.toggleFavorite(TEST_USER, template.getId());
        // Second toggle: off
        boolean result = preferenceService.toggleFavorite(TEST_USER, template.getId());
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should record usage — first time creates preference")
    void shouldRecordUsageFirstTime() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        preferenceService.recordUsage(TEST_USER, template.getId());

        var pref = preferenceService.getPreference(TEST_USER, template.getId());
        assertThat(pref).isPresent();
        assertThat(pref.get().getUseCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should record usage — increment existing preference")
    void shouldRecordUsageIncrement() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        preferenceService.recordUsage(TEST_USER, template.getId());
        preferenceService.recordUsage(TEST_USER, template.getId());

        var pref = preferenceService.getPreference(TEST_USER, template.getId());
        assertThat(pref).isPresent();
        assertThat(pref.get().getUseCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get favorites list")
    void shouldGetFavorites() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        preferenceService.toggleFavorite(TEST_USER, template.getId());

        List<JournalTemplate> favorites = preferenceService.getFavorites(TEST_USER);
        assertThat(favorites).extracting(JournalTemplate::getId).contains(template.getId());
    }

    @Test
    @DisplayName("Should get favorite template IDs")
    void shouldGetFavoriteTemplateIds() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        preferenceService.toggleFavorite(TEST_USER, template.getId());

        Set<UUID> ids = preferenceService.getFavoriteTemplateIds(TEST_USER);
        assertThat(ids).contains(template.getId());
    }

    @Test
    @DisplayName("Should check isFavorite")
    void shouldCheckIsFavorite() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        assertThat(preferenceService.isFavorite(TEST_USER, template.getId())).isFalse();

        preferenceService.toggleFavorite(TEST_USER, template.getId());
        assertThat(preferenceService.isFavorite(TEST_USER, template.getId())).isTrue();
    }

    @Test
    @DisplayName("Should get recently used templates")
    void shouldGetRecentlyUsed() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        preferenceService.recordUsage(TEST_USER, template.getId());

        List<JournalTemplate> recent = preferenceService.getRecentlyUsed(TEST_USER, 5);
        assertThat(recent).extracting(JournalTemplate::getId).contains(template.getId());
    }

    @Test
    @DisplayName("Should get most used templates")
    void shouldGetMostUsed() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        preferenceService.recordUsage(TEST_USER, template.getId());

        List<JournalTemplate> mostUsed = preferenceService.getMostUsed(TEST_USER, 5);
        assertThat(mostUsed).extracting(JournalTemplate::getId).contains(template.getId());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        assertThatThrownBy(() -> preferenceService.toggleFavorite("nonexistent-user", template.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw when template not found")
    void shouldThrowWhenTemplateNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> preferenceService.toggleFavorite(TEST_USER, nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Template not found");
    }

    @Test
    @DisplayName("Should return empty optional for non-existent preference")
    void shouldReturnEmptyOptionalForNonExistentPreference() {
        JournalTemplate template = templateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No templates in test data"));

        var pref = preferenceService.getPreference(TEST_USER, template.getId());
        assertThat(pref).isEmpty();
    }
}
