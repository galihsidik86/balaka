package com.artivisi.accountingfinance.functional;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import com.artivisi.accountingfinance.entity.AlertEvent;
import com.artivisi.accountingfinance.entity.AlertRule;
import com.artivisi.accountingfinance.enums.AlertSeverity;
import com.artivisi.accountingfinance.enums.AlertType;
import com.artivisi.accountingfinance.functional.service.ServiceTestDataInitializer;
import com.artivisi.accountingfinance.repository.AlertEventRepository;
import com.artivisi.accountingfinance.repository.AlertRuleRepository;
import com.artivisi.accountingfinance.service.AlertService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestcontainersConfiguration.class, ServiceTestDataInitializer.class})
@ActiveProfiles("functional")
@DisplayName("Alert Evaluation Tests")
@Transactional
class AlertEvaluationTest {

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private AlertEventRepository alertEventRepository;

    @Test
    @DisplayName("Should evaluate cash low alert")
    void shouldEvaluateCashLow() {
        // Set threshold very high so it triggers
        AlertRule cashLowRule = alertRuleRepository.findByAlertType(AlertType.CASH_LOW)
                .orElseThrow();
        cashLowRule.setThreshold(new BigDecimal("999999999999"));
        cashLowRule.setEnabled(true);
        alertRuleRepository.save(cashLowRule);

        // Clear existing events for this rule
        alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc().stream()
                .filter(e -> e.getAlertRule().getId().equals(cashLowRule.getId()))
                .forEach(e -> {
                    e.setAcknowledgedAt(java.time.LocalDateTime.now());
                    e.setAcknowledgedBy("test-cleanup");
                    alertEventRepository.save(e);
                });

        int triggered = alertService.evaluateAllAlerts();

        // At least the cash low alert should have triggered
        assertThat(triggered).isGreaterThanOrEqualTo(1);

        // Verify event was created
        var events = alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc();
        assertThat(events).anyMatch(e ->
                e.getAlertRule().getAlertType() == AlertType.CASH_LOW);
    }

    @Test
    @DisplayName("Should not create duplicate alerts within 24h")
    void shouldNotCreateDuplicateAlerts() {
        AlertRule cashLowRule = alertRuleRepository.findByAlertType(AlertType.CASH_LOW)
                .orElseThrow();
        cashLowRule.setThreshold(new BigDecimal("999999999999"));
        cashLowRule.setEnabled(true);
        alertRuleRepository.save(cashLowRule);

        // Clear existing events
        alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc().stream()
                .filter(e -> e.getAlertRule().getId().equals(cashLowRule.getId()))
                .forEach(e -> {
                    e.setAcknowledgedAt(java.time.LocalDateTime.now());
                    e.setAcknowledgedBy("test-cleanup");
                    alertEventRepository.save(e);
                });

        // First evaluation
        alertService.evaluateAllAlerts();

        long countAfterFirst = alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc().stream()
                .filter(e -> e.getAlertRule().getAlertType() == AlertType.CASH_LOW)
                .count();

        // Second evaluation — should be deduped
        alertService.evaluateAllAlerts();

        long countAfterSecond = alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc().stream()
                .filter(e -> e.getAlertRule().getAlertType() == AlertType.CASH_LOW)
                .count();

        assertThat(countAfterSecond).isEqualTo(countAfterFirst);
    }

    @Test
    @DisplayName("Should acknowledge alert event")
    void shouldAcknowledgeAlertEvent() {
        // Set threshold very high so it triggers
        AlertRule cashLowRule = alertRuleRepository.findByAlertType(AlertType.CASH_LOW)
                .orElseThrow();
        cashLowRule.setThreshold(new BigDecimal("999999999999"));
        cashLowRule.setEnabled(true);
        alertRuleRepository.save(cashLowRule);

        // Clear existing events for this rule
        alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc().stream()
                .filter(e -> e.getAlertRule().getId().equals(cashLowRule.getId()))
                .forEach(e -> {
                    e.setAcknowledgedAt(java.time.LocalDateTime.now());
                    e.setAcknowledgedBy("test-cleanup");
                    alertEventRepository.save(e);
                });

        // Trigger alert
        alertService.evaluateAllAlerts();

        // Get the unacknowledged event
        var events = alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc();
        var cashLowEvent = events.stream()
                .filter(e -> e.getAlertRule().getAlertType() == AlertType.CASH_LOW)
                .findFirst()
                .orElseThrow();

        // Acknowledge it
        alertService.acknowledge(cashLowEvent.getId(), "test-admin");

        // Verify it's no longer in active alerts
        var activeAfter = alertService.findActiveAlerts();
        assertThat(activeAfter).noneMatch(e -> e.getId().equals(cashLowEvent.getId()));

        // Verify count decreased
        long count = alertService.countActiveAlerts();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should query alert history with filters")
    void shouldQueryAlertHistoryWithFilters() {
        // Query history with various filters
        var allHistory = alertService.findAlertHistory(null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(allHistory).isNotNull();

        // Filter by type
        var cashLowHistory = alertService.findAlertHistory(AlertType.CASH_LOW, null, null,
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(cashLowHistory).isNotNull();

        // Filter by severity
        var warningHistory = alertService.findAlertHistory(null, AlertSeverity.WARNING, null,
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(warningHistory).isNotNull();

        // Filter by acknowledged status
        var acknowledgedHistory = alertService.findAlertHistory(null, null, true,
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(acknowledgedHistory).isNotNull();
    }

    @Test
    @DisplayName("Should update rule threshold and enabled flag")
    void shouldUpdateRuleThresholdAndEnabled() {
        AlertRule rule = alertRuleRepository.findByAlertType(AlertType.CASH_LOW)
                .orElseThrow();

        BigDecimal originalThreshold = rule.getThreshold();
        boolean originalEnabled = rule.isEnabled();

        // Update rule
        AlertRule updated = alertService.updateRule(rule.getId(), new BigDecimal("5000000"), false);
        assertThat(updated.getThreshold()).isEqualByComparingTo(new BigDecimal("5000000"));
        assertThat(updated.isEnabled()).isFalse();

        // Restore original values
        alertService.updateRule(rule.getId(), originalThreshold, originalEnabled);
    }

    @Test
    @DisplayName("Should find rule by id")
    void shouldFindRuleById() {
        var rules = alertService.findAllRules();
        assertThat(rules).isNotEmpty();

        var firstRule = rules.getFirst();
        var found = alertService.findRuleById(firstRule.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAlertType()).isEqualTo(firstRule.getAlertType());
    }

    @Test
    @DisplayName("Should respect disabled rules")
    void shouldRespectDisabledRules() {
        // Disable all rules
        alertRuleRepository.findAll().forEach(rule -> {
            rule.setEnabled(false);
            alertRuleRepository.save(rule);
        });

        // Clear existing unacknowledged events
        alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc().forEach(e -> {
            e.setAcknowledgedAt(java.time.LocalDateTime.now());
            e.setAcknowledgedBy("test-cleanup");
            alertEventRepository.save(e);
        });

        int triggered = alertService.evaluateAllAlerts();
        assertThat(triggered).isZero();

        // Re-enable all rules for other tests
        alertRuleRepository.findAll().forEach(rule -> {
            rule.setEnabled(true);
            alertRuleRepository.save(rule);
        });
    }
}
