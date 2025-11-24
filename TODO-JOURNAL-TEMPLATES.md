# TODO: Journal Templates (1.4)

Predefined recipes for common transactions. Generates journal entries automatically.

## Purpose

- Standardize common transactions (revenue, expense, payment, receipt)
- Reduce data entry errors
- Enable non-accountants to create proper journal entries
- Reused by Transactions (1.5)

## Dependencies

- COA (1.1) ✅ Complete
- Journal Entries (1.2) ✅ Complete
- Basic Reports (1.3) ✅ Complete

---

## TODO List

### 1. Database Schema

- [ ] Create V007__journal_templates.sql migration
- [ ] journal_templates table (id, name, code, description, category, cash_flow_category, version, is_system, active, audit fields)
- [ ] journal_template_lines table (id, template_id, line_order, account_id, debit_formula, credit_formula, description)

**Categories:**
- INCOME - Revenue transactions
- EXPENSE - Expense transactions
- PAYMENT - Cash/bank outflow
- RECEIPT - Cash/bank inflow
- TRANSFER - Between accounts
- ADJUSTMENT - Period-end adjustments

**Cash Flow Categories:**
- OPERATING - Day-to-day business
- INVESTING - Asset purchases/sales
- FINANCING - Loans, capital

### 2. Entity Classes

- [ ] JournalTemplate entity
- [ ] JournalTemplateLine entity
- [ ] TemplateCategory enum
- [ ] CashFlowCategory enum
- [ ] JournalTemplateRepository
- [ ] JournalTemplateLineRepository

### 3. Service Layer

- [ ] JournalTemplateService (CRUD operations)
- [ ] TemplateExecutionEngine.execute(template, context) → JournalEntry
- [ ] TemplateExecutionEngine.validate(template) → List<ValidationError>
- [ ] Template versioning support

### 4. System Templates (IT Services)

Pre-seeded templates via V008__it_services_templates.sql:

| Code | Name | Category | Debit | Credit |
|------|------|----------|-------|--------|
| INC-CONSULT | Pendapatan Konsultasi | INCOME | Bank/Kas | Pendapatan Konsultasi |
| INC-DEV | Pendapatan Development | INCOME | Bank/Kas | Pendapatan Development |
| EXP-SALARY | Beban Gaji | EXPENSE | Beban Gaji | Bank/Kas |
| EXP-SERVER | Beban Server & Cloud | EXPENSE | Beban Server | Bank/Kas |
| EXP-OFFICE | Beban Perlengkapan Kantor | EXPENSE | Beban Perlengkapan | Bank/Kas |
| PAY-VENDOR | Pembayaran Hutang | PAYMENT | Hutang Usaha | Bank/Kas |
| RCV-CLIENT | Penerimaan Piutang | RECEIPT | Bank/Kas | Piutang Usaha |
| TRF-BANK | Transfer Antar Bank | TRANSFER | Bank Tujuan | Bank Asal |
| ADJ-DEPREC | Penyusutan Aset | ADJUSTMENT | Beban Penyusutan | Akum. Penyusutan |

### 5. Template UI

- [ ] Template list page (`/templates`)
- [ ] Category filter tabs
- [ ] Search by name/code
- [ ] Template detail view (`/templates/{id}`)
- [ ] Template form (`/templates/new`, `/templates/{id}/edit`)
- [ ] Template line management (add/edit/remove lines)
- [ ] System template indicator (non-editable)

### 6. Template Execution UI

- [ ] Execute template button on detail page
- [ ] Execution form (date, amount, description)
- [ ] Preview generated journal entry
- [ ] Confirm and create journal entry
- [ ] Link to created journal entry

### 7. Playwright Tests

- [ ] TemplateListTest.java - List and filter templates
- [ ] TemplateDetailTest.java - View template details
- [ ] TemplateFormTest.java - Create/edit templates
- [ ] TemplateExecutionTest.java - Execute templates

---

## Service Methods

```java
JournalTemplateService {
    findAll() → List<JournalTemplate>
    findByCategory(TemplateCategory) → List<JournalTemplate>
    findById(UUID) → JournalTemplate
    create(JournalTemplate) → JournalTemplate
    update(UUID, JournalTemplate) → JournalTemplate
    delete(UUID) → void
    activate(UUID) → void
    deactivate(UUID) → void
}

TemplateExecutionEngine {
    execute(JournalTemplate, ExecutionContext) → JournalEntry
    validate(JournalTemplate) → List<ValidationError>
    preview(JournalTemplate, ExecutionContext) → JournalEntryPreview
}

ExecutionContext {
    LocalDate transactionDate
    BigDecimal amount
    String description
    Map<String, Object> additionalFields
}
```

---

## Database Schema

```sql
CREATE TABLE journal_templates (
    id UUID PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(20) NOT NULL,
    cash_flow_category VARCHAR(20),
    version INTEGER DEFAULT 1,
    is_system BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP
);

CREATE TABLE journal_template_lines (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES journal_templates(id),
    line_order INTEGER NOT NULL,
    account_id UUID NOT NULL REFERENCES chart_of_accounts(id),
    debit_formula VARCHAR(100),  -- e.g., "amount", "amount * 0.11", null
    credit_formula VARCHAR(100), -- e.g., "amount", null
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    UNIQUE(template_id, line_order)
);
```

---

## Notes

- System templates (is_system=true) cannot be edited or deleted
- Template versioning: increment version on each update
- Formula supports: "amount", "amount * factor", constants
- Tags, favorites, usage tracking deferred to 1.8
