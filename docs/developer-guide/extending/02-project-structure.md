# Project Structure

## Source Layout

```
src/main/java/com/artivisi/accountingfinance/
├── AccountingFinanceApplication.java   Main class
├── config/                             Spring configuration
│   ├── SecurityConfig.java             Spring Security setup
│   ├── OpenApiConfig.java              Swagger/OpenAPI config
│   ├── ThemeConfig.java                Per-client theming
│   ├── WebMvcConfig.java               MVC interceptors
│   └── ...
├── controller/                         Web controllers (Thymeleaf)
│   ├── DashboardController.java
│   ├── TransactionController.java
│   ├── DeviceAuthorizationController.java
│   └── api/                            REST API controllers
│       ├── TransactionApiController.java
│       ├── DeviceAuthApiController.java
│       ├── FinancialAnalysisApiController.java
│       ├── PayrollApiController.java
│       ├── TaxExportApiController.java
│       └── ...  (20+ API controllers)
├── dto/                                Request/response records
│   ├── CreateTransactionRequest.java
│   ├── TransactionResponse.java
│   └── ...
├── entity/                             JPA entities (85+)
│   ├── BaseEntity.java                 UUID PK, audit, soft delete
│   ├── Transaction.java
│   ├── JournalEntry.java
│   ├── ChartOfAccount.java
│   └── ...
├── enums/                              Enum types
│   ├── AccountType.java
│   ├── VoidReason.java
│   └── ...
├── exception/                          Custom exceptions
├── repository/                         Spring Data JPA repositories
├── scheduler/                          Scheduled tasks (payroll, alerts)
├── security/                           Security components
│   ├── Permission.java                 Authority string constants
│   ├── BearerTokenAuthenticationFilter.java
│   └── ...
├── service/                            Business logic
│   ├── TransactionService.java
│   ├── JournalService.java
│   ├── PayrollService.java
│   ├── DeviceAuthService.java
│   └── ...
└── util/                               Utility classes
```

## Resources

```
src/main/resources/
├── application.properties              Main config
├── db/migration/                       Flyway production migrations
│   ├── V001__security.sql              Users, roles, permissions
│   ├── V002__core_schema.sql           COA, templates, transactions, journals
│   ├── V003__feature_schema.sql        Payroll, tax, assets, alerts, etc.
│   └── V004__seed_data.sql             Default seed data
├── static/                             CSS, JS, images
│   └── js/alpine-components.js         All Alpine.js components (CSP)
└── templates/                          Thymeleaf templates
    ├── fragments/                      Shared fragments (main.html layout)
    ├── dashboard.html
    ├── accounts/
    ├── transactions/
    └── ...
```

## Test Layout

```
src/test/
├── java/.../
│   ├── functional/                     Playwright browser tests
│   │   ├── ChartOfAccountsTest.java
│   │   ├── TransactionTest.java
│   │   ├── seller/                     Online seller industry tests
│   │   ├── manufacturing/              Coffee shop industry tests
│   │   └── campus/                     Campus industry tests
│   ├── integration/                    Testcontainers tests
│   ├── unit/                           Unit tests
│   └── controller/                     Controller unit tests
└── resources/
    └── db/test/integration/            Test-only migrations
        ├── V800__base_test_data.sql
        ├── V900-V912                   Domain-specific test data
        └── cleanup-for-clear-test.sql
```

## Industry Seed Packs

```
industry-seed/
├── it-service/seed-data/               IT services company (PKP)
│   ├── 01_company_config.csv
│   ├── 02_chart_of_accounts.csv
│   ├── 03_salary_components.csv
│   ├── 04_journal_templates.csv
│   ├── 05_journal_template_lines.csv
│   ├── ...
│   └── MANIFEST.md
├── online-seller/seed-data/            E-commerce seller
├── coffee-shop/seed-data/              Coffee shop / F&B
└── campus/seed-data/                   Campus / educational institution
```

Seed packs are loaded via `DataImportService` and contain numbered CSV files for deterministic import order.

## Key Files

### BaseEntity

`src/.../entity/BaseEntity.java` -- all primary entities extend this.

```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long rowVersion;          // Optimistic locking

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime deletedAt;  // Soft delete

    public void softDelete() { ... }
    public boolean isDeleted() { ... }
}
```

### Permission.java

`src/.../security/Permission.java` -- utility class with `public static final String` constants for all authorities. Not an enum.

```java
public final class Permission {
    public static final String TRANSACTION_VIEW = "TRANSACTION_VIEW";
    public static final String TRANSACTION_CREATE = "TRANSACTION_CREATE";
    public static final String TRANSACTION_POST = "TRANSACTION_POST";
    // ... 60+ permission constants

    // Role sets
    public static final Set<String> ADMIN_PERMISSIONS = Set.of(...);
    public static final Set<String> OWNER_PERMISSIONS = Set.of(...);
}
```

Used in `@PreAuthorize("hasAuthority('TRANSACTION_CREATE')")` annotations.

### ViewConstants.java

Contains page name constants and redirect prefix strings used by web controllers to avoid string duplication.

### Migration Files

| File | Content |
|------|---------|
| `V001__security.sql` | Users, roles, permissions, device codes/tokens |
| `V002__core_schema.sql` | COA, templates, transactions, journal entries |
| `V003__feature_schema.sql` | Payroll, tax, assets, bank reconciliation, alerts, bills, inventory |
| `V004__seed_data.sql` | Default roles, permissions, system templates |

Pre-production strategy: modify existing V001--V004 files instead of creating new migration files. See [Contributing](06-contributing.md) for details.

---

Next: [Adding Features](03-adding-features.md)
