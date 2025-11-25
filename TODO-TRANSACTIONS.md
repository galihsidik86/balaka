# TODO: Transactions (1.5)

User-friendly abstraction over templates. Non-accountants select a template, fill in amounts.

**Reference:** `docs/06-implementation-plan.md` section 1.5

## Dependencies

- COA (1.1) ✅ Complete
- Journal Entries (1.2) ✅ Complete
- Journal Templates (1.4) ✅ Complete

---

## Implementation Status

### Backend ✅ Complete

| Component | Status | Location |
|-----------|--------|----------|
| Database Migration | ✅ | `V004__create_transactions.sql` |
| Transaction Entity | ✅ | `entity/Transaction.java` |
| TransactionSequence Entity | ✅ | `entity/TransactionSequence.java` |
| TransactionAccountMapping Entity | ✅ | `entity/TransactionAccountMapping.java` |
| TransactionStatus Enum | ✅ | `enums/TransactionStatus.java` |
| VoidReason Enum | ✅ | `enums/VoidReason.java` |
| TransactionRepository | ✅ | `repository/TransactionRepository.java` |
| TransactionSequenceRepository | ✅ | `repository/TransactionSequenceRepository.java` |
| TransactionService | ✅ | `service/TransactionService.java` |
| TransactionController | ✅ | `controller/TransactionController.java` |
| TransactionDto | ✅ | `dto/TransactionDto.java` |
| VoidTransactionDto | ✅ | `dto/VoidTransactionDto.java` |

**Service Features Implemented:**
- [x] CRUD operations (create, update, delete)
- [x] Transaction numbering (auto-increment per year)
- [x] Post transaction → generates journal entries
- [x] Void transaction → creates reversal entries
- [x] Status workflow (DRAFT → POSTED → VOID)
- [x] SpEL formula calculation for amounts
- [x] Journal balance validation
- [x] Filters (status, category, date range)
- [x] Search functionality
- [x] Pagination

**Controller Endpoints Implemented:**
- [x] GET `/transactions` - list page
- [x] GET `/transactions/new` - form page
- [x] GET `/transactions/{id}` - detail page
- [x] GET `/transactions/{id}/edit` - edit page
- [x] GET `/transactions/{id}/void` - void form
- [x] REST API: GET/POST/PUT/DELETE `/transactions/api/*`

---

### Frontend ❌ Mockups Only

All HTML templates exist but contain **hardcoded static data**. They need to be converted to dynamic Thymeleaf.

#### list.html - ❌ Mockup
- [ ] Replace static transaction rows with `th:each="${transactions}"`
- [ ] Wire up template dropdown to dynamic `${templates}` from controller
- [ ] Connect filters to actual query parameters
- [ ] Connect search to API
- [ ] Wire pagination to actual page data
- [ ] Summary cards should show real totals

**Current issues:**
- Hardcoded transaction rows (TRX-2025-0023, etc.)
- Hardcoded template links with fake IDs (`templateId='income-consulting'`)
- Static summary cards (Rp 45.500.000, etc.)
- Non-functional filters and pagination

#### form.html - ❌ Mockup
- [ ] Wire template info from `${selectedTemplate}`
- [ ] Populate accounts dropdown from `${accounts}`
- [ ] Bind form fields to `${transaction}` for edit mode
- [ ] Connect form submission to REST API
- [ ] Dynamic journal preview based on template lines
- [ ] Add `id="form-transaksi"` for test compatibility

**Current issues:**
- Hardcoded template name "Pendapatan Jasa Konsultasi"
- Hardcoded account options (1.1.01, 1.1.02, 1.1.03)
- Static journal preview (always shows same 2 lines)
- Form action is `action="#"` (not connected)

#### detail.html - ❌ Mockup
- [ ] Bind all fields to `${transaction}`
- [ ] Dynamic journal entries from `${transaction.journalEntries}`
- [ ] Dynamic status banner based on `${transaction.status}`
- [ ] Wire void button to actual void page
- [ ] Dynamic audit trail

**Current issues:**
- All data hardcoded (TRX-2025-0023, Rp 15.000.000, etc.)
- Static journal entries
- Static audit trail

#### void.html - ❌ Mockup
- [ ] Bind transaction summary to `${transaction}`
- [ ] Dynamic journal entries to be voided
- [ ] Wire form submission to void API
- [ ] Wire void reasons to `VoidReason` enum

**Current issues:**
- Hardcoded transaction (TRX-2025-0023)
- Static journal entries
- Form action is `action="#"`
- Hardcoded back link (`/transactions/txn-001`)

---

### Playwright Tests ❌ Not Started

Page objects exist but reference non-existent element IDs.

#### Page Objects to Fix
- [ ] `TransactionsListPage.java` - fix element IDs to match actual HTML
- [ ] `TransactionFormPage.java` - fix element IDs to match actual HTML

#### Tests to Write
- [ ] Transaction list page loads with data
- [ ] Create draft transaction
- [ ] Post transaction (verify journal entry created)
- [ ] Edit draft transaction
- [ ] Delete draft transaction
- [ ] Void posted transaction (verify reversal entries)
- [ ] Transaction number auto-increments
- [ ] Filter by status
- [ ] Filter by category
- [ ] Filter by date range
- [ ] Search transactions

---

## TODO Checklist

### 1. Convert list.html to Dynamic ⏳
- [ ] Add `th:each` for transaction rows
- [ ] Wire template dropdown to `${templates}`
- [ ] Connect filters (status, category, date range)
- [ ] Connect search functionality
- [ ] Wire pagination
- [ ] Dynamic summary cards

### 2. Convert form.html to Dynamic ⏳
- [ ] Template selection from `${templates}`
- [ ] Account dropdown from `${accounts}`
- [ ] Form binding for edit mode
- [ ] Connect save draft button to API
- [ ] Connect save & post button to API
- [ ] Dynamic journal preview from template lines

### 3. Convert detail.html to Dynamic ⏳
- [ ] All transaction fields from `${transaction}`
- [ ] Journal entries from `${transaction.journalEntries}`
- [ ] Status-based UI (different banners for DRAFT/POSTED/VOID)
- [ ] Audit trail (if implemented)

### 4. Convert void.html to Dynamic ⏳
- [ ] Transaction summary from `${transaction}`
- [ ] Journal entries preview
- [ ] Form submission to void endpoint
- [ ] VoidReason enum mapping

### 5. Fix Page Objects ⏳
- [ ] Update element IDs in TransactionsListPage.java
- [ ] Update element IDs in TransactionFormPage.java

### 6. Write Playwright Tests ⏳
- [ ] TransactionListTest.java
- [ ] TransactionCreateTest.java
- [ ] TransactionPostTest.java
- [ ] TransactionVoidTest.java
- [ ] TransactionFilterTest.java

---

## Current Status

**Backend:** ✅ Complete (entities, service, controller, REST API)

**Frontend:** ❌ Mockups only - all templates have hardcoded data

**Tests:** ❌ Not started - page objects exist but not functional

**Next Steps:**
1. Convert list.html to dynamic (highest priority - entry point)
2. Convert form.html to dynamic
3. Convert detail.html to dynamic
4. Convert void.html to dynamic
5. Fix page objects
6. Write Playwright tests
