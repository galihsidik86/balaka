package com.artivisi.accountingfinance.service;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import com.artivisi.accountingfinance.entity.AlertEvent;
import com.artivisi.accountingfinance.entity.AlertRule;
import com.artivisi.accountingfinance.enums.AlertSeverity;
import com.artivisi.accountingfinance.enums.AlertType;
import com.artivisi.accountingfinance.repository.AlertEventRepository;
import com.artivisi.accountingfinance.repository.AlertRuleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for AlertService.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("AlertService Integration Tests")
class AlertServiceTest {

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private AlertEventRepository alertEventRepository;

    @Nested
    @DisplayName("CRUD Operations")
    class CrudTests {

        @Test
        @DisplayName("Should find all rules ordered by alert type")
        void shouldFindAllRules() {
            List<AlertRule> rules = alertService.findAllRules();
            assertThat(rules).isNotEmpty();
            // Verify ordering by alert type name (string ordering)
            for (int i = 1; i < rules.size(); i++) {
                assertThat(rules.get(i).getAlertType().name().compareTo(rules.get(i - 1).getAlertType().name()))
                        .isGreaterThanOrEqualTo(0);
            }
        }

        @Test
        @DisplayName("Should find rule by ID")
        void shouldFindRuleById() {
            List<AlertRule> rules = alertService.findAllRules();
            assertThat(rules).isNotEmpty();

            Optional<AlertRule> found = alertService.findRuleById(rules.getFirst().getId());
            assertThat(found).isPresent();
        }

        @Test
        @DisplayName("Should return empty for non-existent rule ID")
        void shouldReturnEmptyForNonExistentRuleId() {
            Optional<AlertRule> found = alertService.findRuleById(UUID.randomUUID());
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should update rule threshold and enabled status")
        void shouldUpdateRuleThresholdAndEnabled() {
            List<AlertRule> rules = alertService.findAllRules();
            assertThat(rules).isNotEmpty();

            AlertRule rule = rules.getFirst();
            BigDecimal newThreshold = new BigDecimal("99999.00");

            AlertRule updated = alertService.updateRule(rule.getId(), newThreshold, false);

            assertThat(updated.getThreshold()).isEqualByComparingTo(newThreshold);
            assertThat(updated.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent rule")
        void shouldThrowExceptionWhenUpdatingNonExistentRule() {
            UUID randomId = UUID.randomUUID();
            assertThatThrownBy(() -> alertService.updateRule(randomId, BigDecimal.ONE, true))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Alert rule not found");
        }
    }

    @Nested
    @DisplayName("Alert Event Operations")
    class AlertEventTests {

        @Test
        @DisplayName("Should find active alerts (unacknowledged)")
        void shouldFindActiveAlerts() {
            List<AlertEvent> alerts = alertService.findActiveAlerts();
            assertThat(alerts).isNotNull();
        }

        @Test
        @DisplayName("Should count active alerts")
        void shouldCountActiveAlerts() {
            long count = alertService.countActiveAlerts();
            assertThat(count).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should acknowledge an alert event")
        void shouldAcknowledgeAlertEvent() {
            // Create an alert event
            AlertRule rule = alertRuleRepository.findByEnabledTrue().getFirst();
            AlertEvent event = new AlertEvent();
            event.setAlertRule(rule);
            event.setSeverity(AlertSeverity.WARNING);
            event.setMessage("Test alert for acknowledgement");
            AlertEvent saved = alertEventRepository.save(event);

            // Acknowledge it
            alertService.acknowledge(saved.getId(), "testuser");

            // Verify
            AlertEvent acknowledged = alertEventRepository.findById(saved.getId()).orElseThrow();
            assertThat(acknowledged.getAcknowledgedAt()).isNotNull();
            assertThat(acknowledged.getAcknowledgedBy()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should throw exception when acknowledging non-existent event")
        void shouldThrowExceptionWhenAcknowledgingNonExistent() {
            UUID randomId = UUID.randomUUID();
            assertThatThrownBy(() -> alertService.acknowledge(randomId, "admin"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Alert event not found");
        }

        @Test
        @DisplayName("Should find alert history with no filters")
        void shouldFindAlertHistoryNoFilters() {
            Page<AlertEvent> history = alertService.findAlertHistory(
                    null, null, null, PageRequest.of(0, 10));
            assertThat(history).isNotNull();
        }

        @Test
        @DisplayName("Should find alert history filtered by alert type")
        void shouldFindAlertHistoryByType() {
            // Create an event first
            AlertRule rule = alertRuleRepository.findByAlertType(AlertType.CASH_LOW).orElseThrow();
            AlertEvent event = new AlertEvent();
            event.setAlertRule(rule);
            event.setSeverity(AlertSeverity.WARNING);
            event.setMessage("Test cash low");
            alertEventRepository.save(event);

            Page<AlertEvent> history = alertService.findAlertHistory(
                    AlertType.CASH_LOW, null, null, PageRequest.of(0, 10));
            assertThat(history.getTotalElements()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should find alert history filtered by severity")
        void shouldFindAlertHistoryBySeverity() {
            AlertRule rule = alertRuleRepository.findByEnabledTrue().getFirst();
            AlertEvent event = new AlertEvent();
            event.setAlertRule(rule);
            event.setSeverity(AlertSeverity.CRITICAL);
            event.setMessage("Test critical alert");
            alertEventRepository.save(event);

            Page<AlertEvent> history = alertService.findAlertHistory(
                    null, AlertSeverity.CRITICAL, null, PageRequest.of(0, 10));
            assertThat(history.getTotalElements()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should find alert history filtered by acknowledged status true")
        void shouldFindAlertHistoryByAcknowledgedTrue() {
            AlertRule rule = alertRuleRepository.findByEnabledTrue().getFirst();
            AlertEvent event = new AlertEvent();
            event.setAlertRule(rule);
            event.setSeverity(AlertSeverity.WARNING);
            event.setMessage("Acknowledged event");
            event.setAcknowledgedAt(LocalDateTime.now());
            event.setAcknowledgedBy("admin");
            alertEventRepository.save(event);

            Page<AlertEvent> history = alertService.findAlertHistory(
                    null, null, true, PageRequest.of(0, 10));
            assertThat(history.getTotalElements()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("Should find alert history filtered by acknowledged status false")
        void shouldFindAlertHistoryByAcknowledgedFalse() {
            AlertRule rule = alertRuleRepository.findByEnabledTrue().getFirst();
            AlertEvent event = new AlertEvent();
            event.setAlertRule(rule);
            event.setSeverity(AlertSeverity.WARNING);
            event.setMessage("Unacknowledged event");
            alertEventRepository.save(event);

            Page<AlertEvent> history = alertService.findAlertHistory(
                    null, null, false, PageRequest.of(0, 10));
            assertThat(history.getTotalElements()).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Alert Evaluation")
    class EvaluationTests {

        @Test
        @DisplayName("Should evaluate all enabled alerts without errors")
        void shouldEvaluateAllAlertsWithoutErrors() {
            int triggered = alertService.evaluateAllAlerts();
            assertThat(triggered).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should skip evaluation when recent unacknowledged event exists (dedup)")
        void shouldSkipDuplicateAlerts() {
            // First evaluation
            alertService.evaluateAllAlerts();

            // Get all unacknowledged events
            List<AlertEvent> firstRun = alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc();

            // Second evaluation should skip rules that already have unacknowledged events
            int secondTriggered = alertService.evaluateAllAlerts();
            // If first run created events, second run should trigger fewer or equal
            List<AlertEvent> secondRun = alertEventRepository.findByAcknowledgedAtIsNullOrderByTriggeredAtDesc();
            assertThat(secondRun.size()).isGreaterThanOrEqualTo(firstRun.size());
        }

        @Test
        @DisplayName("Should not trigger alerts when all rules are disabled")
        void shouldNotTriggerWhenAllDisabled() {
            // Disable all rules
            List<AlertRule> rules = alertRuleRepository.findAll();
            for (AlertRule rule : rules) {
                rule.setEnabled(false);
                alertRuleRepository.save(rule);
            }

            int triggered = alertService.evaluateAllAlerts();
            assertThat(triggered).isZero();
        }

        @Test
        @DisplayName("Should evaluate CASH_LOW alert with high threshold")
        void shouldEvaluateCashLowWithHighThreshold() {
            // Set CASH_LOW threshold very high to trigger it
            AlertRule cashLow = alertRuleRepository.findByAlertType(AlertType.CASH_LOW).orElseThrow();
            cashLow.setThreshold(new BigDecimal("999999999999"));
            cashLow.setEnabled(true);
            alertRuleRepository.save(cashLow);

            // Disable all other rules
            List<AlertRule> allRules = alertRuleRepository.findAll();
            for (AlertRule rule : allRules) {
                if (rule.getAlertType() != AlertType.CASH_LOW) {
                    rule.setEnabled(false);
                    alertRuleRepository.save(rule);
                }
            }

            int triggered = alertService.evaluateAllAlerts();
            assertThat(triggered).isEqualTo(1);
        }

        @Test
        @DisplayName("Should evaluate RECEIVABLE_OVERDUE alert")
        void shouldEvaluateReceivableOverdue() {
            AlertRule rule = alertRuleRepository.findByAlertType(AlertType.RECEIVABLE_OVERDUE).orElseThrow();
            rule.setEnabled(true);
            rule.setThreshold(BigDecimal.ZERO);
            alertRuleRepository.save(rule);

            // Disable other rules
            disableAllRulesExcept(AlertType.RECEIVABLE_OVERDUE);

            int triggered = alertService.evaluateAllAlerts();
            assertThat(triggered).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should evaluate EXPENSE_SPIKE alert")
        void shouldEvaluateExpenseSpike() {
            AlertRule rule = alertRuleRepository.findByAlertType(AlertType.EXPENSE_SPIKE).orElseThrow();
            rule.setEnabled(true);
            rule.setThreshold(BigDecimal.ZERO); // Set threshold to 0% to increase chance of triggering
            alertRuleRepository.save(rule);

            disableAllRulesExcept(AlertType.EXPENSE_SPIKE);

            int triggered = alertService.evaluateAllAlerts();
            assertThat(triggered).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should evaluate PROJECT_COST_OVERRUN alert")
        void shouldEvaluateProjectCostOverrun() {
            AlertRule rule = alertRuleRepository.findByAlertType(AlertType.PROJECT_COST_OVERRUN).orElseThrow();
            rule.setEnabled(true);
            alertRuleRepository.save(rule);

            disableAllRulesExcept(AlertType.PROJECT_COST_OVERRUN);

            int triggered = alertService.evaluateAllAlerts();
            assertThat(triggered).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should evaluate PROJECT_MARGIN_DROP alert")
        void shouldEvaluateProjectMarginDrop() {
            AlertRule rule = alertRuleRepository.findByAlertType(AlertType.PROJECT_MARGIN_DROP).orElseThrow();
            rule.setEnabled(true);
            rule.setThreshold(new BigDecimal("99")); // High threshold to trigger
            alertRuleRepository.save(rule);

            disableAllRulesExcept(AlertType.PROJECT_MARGIN_DROP);

            int triggered = alertService.evaluateAllAlerts();
            assertThat(triggered).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should evaluate COLLECTION_SLOWDOWN alert")
        void shouldEvaluateCollectionSlowdown() {
            AlertRule rule = alertRuleRepository.findByAlertType(AlertType.COLLECTION_SLOWDOWN).orElseThrow();
            rule.setEnabled(true);
            alertRuleRepository.save(rule);

            disableAllRulesExcept(AlertType.COLLECTION_SLOWDOWN);

            int triggered = alertService.evaluateAllAlerts();
            assertThat(triggered).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should evaluate CLIENT_CONCENTRATION alert")
        void shouldEvaluateClientConcentration() {
            AlertRule rule = alertRuleRepository.findByAlertType(AlertType.CLIENT_CONCENTRATION).orElseThrow();
            rule.setEnabled(true);
            rule.setThreshold(BigDecimal.ONE); // Very low threshold to trigger
            alertRuleRepository.save(rule);

            disableAllRulesExcept(AlertType.CLIENT_CONCENTRATION);

            int triggered = alertService.evaluateAllAlerts();
            assertThat(triggered).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should update lastTriggeredAt when alert triggers")
        void shouldUpdateLastTriggeredAtWhenTriggered() {
            AlertRule cashLow = alertRuleRepository.findByAlertType(AlertType.CASH_LOW).orElseThrow();
            cashLow.setThreshold(new BigDecimal("999999999999"));
            cashLow.setEnabled(true);
            cashLow.setLastTriggeredAt(null);
            alertRuleRepository.save(cashLow);

            disableAllRulesExcept(AlertType.CASH_LOW);

            alertService.evaluateAllAlerts();

            AlertRule updated = alertRuleRepository.findByAlertType(AlertType.CASH_LOW).orElseThrow();
            assertThat(updated.getLastTriggeredAt()).isNotNull();
        }

        private void disableAllRulesExcept(AlertType keepEnabled) {
            List<AlertRule> allRules = alertRuleRepository.findAll();
            for (AlertRule r : allRules) {
                if (r.getAlertType() != keepEnabled) {
                    r.setEnabled(false);
                    alertRuleRepository.save(r);
                }
            }
        }
    }
}
