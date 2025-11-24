# TODO: Journal Templates (1.4 Basic)

Predefined recipes for common transactions. Generates journal entries automatically.

**Reference:** `docs/06-implementation-plan.md` section 1.4, `docs/99-decisions-and-questions.md`

## Dependencies

- COA (1.1) ✅ Complete
- Journal Entries (1.2) ✅ Complete

## Scope (from implementation plan)

**In scope for 1.4:**
- Template entity with versioning
- Template lines entity (account mappings, debit/credit rules)
- Category field (income, expense, payment, receipt, transfer)
- Cash flow category field (operating, investing, financing)
- System templates for IT Services (preloaded via migration)
- Template CRUD UI
- Template list with category filter
- Template detail view
- Template execution (generates journal entry)

**NOT in scope (deferred):**
- Tags, favorites, usage tracking → 1.8 Template Enhancements
- SpEL formula support → 1.6 Formula Support (optional for MVP)
- Search functionality → 1.8

---

## TODO Checklist

### 1. Database Schema ✅

- [x] V003__create_journal_templates.sql migration
- [x] journal_templates table with versioning (version field)
- [x] journal_template_lines table

### 2. Entity Classes ✅

- [x] JournalTemplate entity with version field
- [x] JournalTemplateLine entity
- [x] TemplateCategory enum (INCOME, EXPENSE, PAYMENT, RECEIPT, TRANSFER, ADJUSTMENT)
- [x] CashFlowCategory enum (OPERATING, INVESTING, FINANCING)
- [x] JournalTemplateRepository
- [x] JournalTemplateLineRepository

### 3. Service Layer

- [x] JournalTemplateService with CRUD operations
- [ ] Version increment on update (Decision #10) - verify implementation
- [x] System template protection (cannot edit/delete is_system=true)
- [x] TemplateExecutionEngine.execute() → creates JournalEntry
- [x] TemplateExecutionEngine.preview() → shows what will be created (Decision #14)
- [x] TemplateExecutionEngine.validate()

**Versioning (Decision #10):**
- [ ] Verify version field increments on each update
- [ ] Journal entries store template_version used at execution time
- [ ] Display version number in detail view

### 4. System Templates (IT Services) ✅

- [x] Pre-seeded via V003 migration (12 templates)
- [x] Categories: INCOME, EXPENSE, PAYMENT, RECEIPT, TRANSFER, ADJUSTMENT

### 5. Template List UI

- [x] Route GET /templates
- [ ] Dynamic list from database (currently static HTML mockup)
- [ ] Category filter tabs (functional)
- [ ] Link to detail page for each template

### 6. Template Detail View ✅

- [x] Route GET /templates/{id}
- [x] Display template info (name, category, cash flow category)
- [x] Display template lines (account, position, formula)
- [x] Execute button → links to execution page
- [x] Edit button (hidden for system templates)

### 7. Template CRUD UI

- [x] Routes: GET /templates/new, GET /templates/{id}/edit
- [ ] Form POST to /templates (create) and /templates/{id} (update)
- [ ] Use @ModelAttribute binding (same pattern as Journal Entry form)
- [ ] Account dropdown uses ${accounts} with th:each (currently hardcoded)
- [ ] Add/remove template lines (Alpine.js for dynamic rows)
- [ ] Load existing data for edit mode via th:object="${template}"
- [ ] System template warning (non-editable)
- [ ] Delete via POST /templates/{id}/delete

### 8. Template Execution UI ✅

- [x] Route GET /templates/{id}/execute
- [x] Execution form (date, amount, description)
- [x] Preview button → shows generated journal lines
- [x] Execute button → creates journal entry
- [x] Success message with link to created journal

### 9. Playwright Tests

- [x] Template list page loads
- [x] Template detail page loads
- [x] Template execution flow (preview → execute)
- [x] Validation errors displayed
- [ ] Template create flow (save, verify in list)
- [ ] Template edit flow (version increments after save)
- [ ] Template delete flow (non-system only)
- [ ] System template protection (edit/delete buttons hidden or disabled)
- [ ] Version displayed in detail view

---

## Key Decisions Applied

| Decision | Description | Applied |
|----------|-------------|---------|
| #10 | Version templates, increment on edit | ✅ Service increments version |
| #14 | No conditional logic, use preview | ✅ Preview before execute |
| #2 | Configurable templates with preloaded defaults | ✅ System templates seeded |

---

## Current Status

**Working:**
- Backend complete (entities, service, execution engine)
- System templates seeded
- Template detail view (dynamic)
- Template execution flow (preview → execute → journal created)

**Not Working:**
- list.html is static mockup
- form.html doesn't submit (action="#")
- form.html account dropdown is hardcoded
- Cannot create/edit/delete templates through UI

---

## Implementation Tasks

1. **Make list.html dynamic**
   - Replace static HTML with Thymeleaf: `th:each="template : ${templates}"`
   - Category filter tabs using query parameter `?category=INCOME`
   - Link cards to /templates/{id}

2. **Fix form.html (Thymeleaf + Spring form pattern)**
   - Change form action to `th:action="@{/templates}"` (create) or `th:action="@{/templates/{id}(id=${template.id})}"` (edit)
   - Use `th:object="${template}"` for form binding
   - Use `th:each` for account dropdown from ${accounts}
   - Alpine.js only for dynamic line add/remove (not form submission)
   - Add controller POST endpoints for create/update/delete

3. **Add CRUD Playwright tests**
   - Test create new template
   - Test edit existing template (version increments)
   - Test delete non-system template
   - Test system template is protected
