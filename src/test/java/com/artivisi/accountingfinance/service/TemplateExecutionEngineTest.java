package com.artivisi.accountingfinance.service;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import com.artivisi.accountingfinance.entity.JournalTemplate;
import com.artivisi.accountingfinance.entity.JournalTemplateLine;
import com.artivisi.accountingfinance.enums.JournalPosition;
import com.artivisi.accountingfinance.enums.TemplateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for TemplateExecutionEngine.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("TemplateExecutionEngine Integration Tests")
class TemplateExecutionEngineTest {

    @Autowired
    private TemplateExecutionEngine executionEngine;

    @Autowired
    private JournalTemplateService journalTemplateService;

    // Template IDs from V800 base test data
    private static final UUID INCOME_TEMPLATE_ID = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    // Template IDs from V903 formula test templates
    private static final UUID PPN_SALE_TEMPLATE_ID = UUID.fromString("f0000000-0000-0000-0000-000000000011");
    private static final UUID PPN_PURCHASE_TEMPLATE_ID = UUID.fromString("f0000000-0000-0000-0000-000000000012");
    private static final UUID PPH23_TEMPLATE_ID = UUID.fromString("f0000000-0000-0000-0000-000000000013");
    private static final UUID SALARY_DEDUCTION_TEMPLATE_ID = UUID.fromString("f0000000-0000-0000-0000-000000000014");

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should return error when template is null")
        void shouldReturnErrorWhenTemplateIsNull() {
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), BigDecimal.TEN, "test");

            List<String> errors = executionEngine.validate(null, context);
            assertThat(errors).contains("Template is required");
        }

        @Test
        @DisplayName("Should return error when context is null")
        void shouldReturnErrorWhenContextIsNull() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            List<String> errors = executionEngine.validate(template, null);
            assertThat(errors).contains("Execution context is required");
        }

        @Test
        @DisplayName("Should return error when template is inactive")
        void shouldReturnErrorWhenTemplateInactive() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);
            template.setActive(false);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), BigDecimal.TEN, "test");

            List<String> errors = executionEngine.validate(template, context);
            assertThat(errors).contains("Template is not active");
        }

        @Test
        @DisplayName("Should return error when template has fewer than 2 lines")
        void shouldReturnErrorWhenTemplateTooFewLines() {
            JournalTemplate template = new JournalTemplate();
            template.setActive(true);
            template.setTemplateType(TemplateType.SIMPLE);
            // Only one line
            JournalTemplateLine line = new JournalTemplateLine();
            template.addLine(line);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), BigDecimal.TEN, "test");

            List<String> errors = executionEngine.validate(template, context);
            assertThat(errors).contains("Template must have at least 2 lines");
        }

        @Test
        @DisplayName("Should return error when transaction date is missing")
        void shouldReturnErrorWhenDateMissing() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(null, BigDecimal.TEN, "test");

            List<String> errors = executionEngine.validate(template, context);
            assertThat(errors).contains("Transaction date is required");
        }

        @Test
        @DisplayName("Should return error when amount is null for SIMPLE template")
        void shouldReturnErrorWhenAmountNull() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), null, "test");

            List<String> errors = executionEngine.validate(template, context);
            assertThat(errors).contains("Amount is required");
        }

        @Test
        @DisplayName("Should return error when amount is zero for SIMPLE template")
        void shouldReturnErrorWhenAmountZero() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), BigDecimal.ZERO, "test");

            List<String> errors = executionEngine.validate(template, context);
            assertThat(errors).contains("Amount must be positive");
        }

        @Test
        @DisplayName("Should return error when amount is negative for SIMPLE template")
        void shouldReturnErrorWhenAmountNegative() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), new BigDecimal("-1"), "test");

            List<String> errors = executionEngine.validate(template, context);
            assertThat(errors).contains("Amount must be positive");
        }

        @Test
        @DisplayName("Should return error when description is blank")
        void shouldReturnErrorWhenDescriptionBlank() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), BigDecimal.TEN, "");

            List<String> errors = executionEngine.validate(template, context);
            assertThat(errors).contains("Description is required");
        }

        @Test
        @DisplayName("Should pass validation for valid SIMPLE template context")
        void shouldPassValidationForValidContext() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), new BigDecimal("1000000"), "Test transaction");

            List<String> errors = executionEngine.validate(template, context);
            assertThat(errors).isEmpty();
        }
    }

    @Nested
    @DisplayName("Preview Tests")
    class PreviewTests {

        @Test
        @DisplayName("Should preview simple template execution")
        void shouldPreviewSimpleTemplate() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), new BigDecimal("5000000"), "Preview test");

            TemplateExecutionEngine.PreviewResult result = executionEngine.preview(template, context);

            assertThat(result.valid()).isTrue();
            assertThat(result.errors()).isEmpty();
            assertThat(result.entries()).hasSize(2);
            assertThat(result.totalDebit()).isEqualByComparingTo(result.totalCredit());
        }

        @Test
        @DisplayName("Should preview PPN sale template with formula")
        void shouldPreviewPpnSaleTemplate() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(PPN_SALE_TEMPLATE_ID);

            BigDecimal amount = new BigDecimal("10000000");
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), amount, "PPN Sale preview");

            TemplateExecutionEngine.PreviewResult result = executionEngine.preview(template, context);

            assertThat(result.valid()).isTrue();
            assertThat(result.entries()).hasSize(3);
            // Bank debit = amount * 1.11 = 11,100,000
            assertThat(result.totalDebit()).isEqualByComparingTo(new BigDecimal("11100000"));
            // Revenue + PPN = 10,000,000 + 1,100,000 = 11,100,000
            assertThat(result.totalCredit()).isEqualByComparingTo(new BigDecimal("11100000"));
        }

        @Test
        @DisplayName("Should return invalid preview for bad context")
        void shouldReturnInvalidPreviewForBadContext() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(null, null, "");

            TemplateExecutionEngine.PreviewResult result = executionEngine.preview(template, context);

            assertThat(result.valid()).isFalse();
            assertThat(result.errors()).isNotEmpty();
        }

        @Test
        @DisplayName("Should preview PPh 23 conditional template")
        void shouldPreviewPph23ConditionalTemplate() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(PPH23_TEMPLATE_ID);

            BigDecimal amount = new BigDecimal("5000000");
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), amount, "PPh 23 preview");

            TemplateExecutionEngine.PreviewResult result = executionEngine.preview(template, context);

            assertThat(result.valid()).isTrue();
            assertThat(result.entries()).hasSize(3);
            assertThat(result.totalDebit()).isEqualByComparingTo(result.totalCredit());
        }

        @Test
        @DisplayName("Should preview salary deduction template with fixed amount")
        void shouldPreviewSalaryDeductionTemplate() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(SALARY_DEDUCTION_TEMPLATE_ID);

            BigDecimal amount = new BigDecimal("8000000");
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), amount, "Salary preview");

            TemplateExecutionEngine.PreviewResult result = executionEngine.preview(template, context);

            assertThat(result.valid()).isTrue();
            assertThat(result.entries()).hasSize(3);
            assertThat(result.totalDebit()).isEqualByComparingTo(result.totalCredit());
        }
    }

    @Nested
    @DisplayName("Execute Tests")
    class ExecuteTests {

        @Test
        @WithMockUser(username = "admin")
        @DisplayName("Should execute simple template and create transaction")
        void shouldExecuteSimpleTemplate() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(
                            LocalDate.now(), new BigDecimal("3000000"), "Execute test", "REF-EXE-001");

            TemplateExecutionEngine.ExecutionResult result = executionEngine.execute(template, context);

            assertThat(result.transactionId()).isNotNull();
            assertThat(result.entries()).hasSize(2);
        }

        @Test
        @WithMockUser(username = "admin")
        @DisplayName("Should throw exception when executing with invalid context")
        void shouldThrowExceptionForInvalidExecution() {
            JournalTemplate template = journalTemplateService.findByIdWithLines(INCOME_TEMPLATE_ID);

            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(null, null, "");

            assertThatThrownBy(() -> executionEngine.execute(template, context))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Template validation failed");
        }
    }

    @Nested
    @DisplayName("Formula Evaluation Tests")
    class FormulaEvaluationTests {

        @Test
        @DisplayName("Should evaluate simple amount formula")
        void shouldEvaluateSimpleAmountFormula() {
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), new BigDecimal("1000000"), "test");

            BigDecimal result = executionEngine.evaluateFormula("amount", context);
            assertThat(result).isEqualByComparingTo(new BigDecimal("1000000"));
        }

        @Test
        @DisplayName("Should evaluate multiplication formula")
        void shouldEvaluateMultiplicationFormula() {
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), new BigDecimal("10000000"), "test");

            BigDecimal result = executionEngine.evaluateFormula("amount * 0.11", context);
            assertThat(result).isEqualByComparingTo(new BigDecimal("1100000"));
        }

        @Test
        @DisplayName("Should evaluate formula with variables")
        void shouldEvaluateFormulaWithVariables() {
            Map<String, BigDecimal> variables = Map.of(
                    "grossSalary", new BigDecimal("8000000"),
                    "companyBpjs", new BigDecimal("320000")
            );
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(
                            LocalDate.now(), null, "test", null, variables);

            BigDecimal result = executionEngine.evaluateFormula("grossSalary", context);
            assertThat(result).isEqualByComparingTo(new BigDecimal("8000000"));
        }

        @Test
        @DisplayName("Should evaluate conditional formula")
        void shouldEvaluateConditionalFormula() {
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), new BigDecimal("5000000"), "test");

            BigDecimal result = executionEngine.evaluateFormula("amount > 2000000 ? amount * 0.02 : 0", context);
            assertThat(result).isEqualByComparingTo(new BigDecimal("100000"));
        }

        @Test
        @DisplayName("Should evaluate conditional formula below threshold")
        void shouldEvaluateConditionalFormulaBelowThreshold() {
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), new BigDecimal("1000000"), "test");

            BigDecimal result = executionEngine.evaluateFormula("amount > 2000000 ? amount * 0.02 : 0", context);
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should evaluate fixed amount formula")
        void shouldEvaluateFixedAmountFormula() {
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), new BigDecimal("8000000"), "test");

            BigDecimal result = executionEngine.evaluateFormula("320000", context);
            assertThat(result).isEqualByComparingTo(new BigDecimal("320000"));
        }
    }

    @Nested
    @DisplayName("ExecutionContext Tests")
    class ExecutionContextTests {

        @Test
        @DisplayName("Should return null for unmapped line order")
        void shouldReturnNullForUnmappedLineOrder() {
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(LocalDate.now(), BigDecimal.TEN, "test");

            assertThat(context.getAccountIdForLine(1)).isNull();
        }

        @Test
        @DisplayName("Should return null for blank account mapping")
        void shouldReturnNullForBlankAccountMapping() {
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(
                            LocalDate.now(), BigDecimal.TEN, "test", null, Map.of(), Map.of("1", ""));

            assertThat(context.getAccountIdForLine(1)).isNull();
        }

        @Test
        @DisplayName("Should resolve UUID from account mapping")
        void shouldResolveUuidFromAccountMapping() {
            UUID accountId = UUID.fromString("10000000-0000-0000-0000-000000000102");
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(
                            LocalDate.now(), BigDecimal.TEN, "test", null, Map.of(),
                            Map.of("1", accountId.toString()));

            assertThat(context.getAccountIdForLine(1)).isEqualTo(accountId);
        }

        @Test
        @DisplayName("Should return null when accountMappings is null")
        void shouldReturnNullWhenAccountMappingsNull() {
            TemplateExecutionEngine.ExecutionContext context =
                    new TemplateExecutionEngine.ExecutionContext(
                            LocalDate.now(), BigDecimal.TEN, "test", null, Map.of(), null);

            assertThat(context.getAccountIdForLine(1)).isNull();
        }
    }
}
