# Architecture Overview

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 25 (virtual threads) |
| Framework | Spring Boot | 4.0 |
| ORM | Spring Data JPA / Hibernate | |
| Database | PostgreSQL | 18 |
| Migrations | Flyway | |
| Templates | Thymeleaf | |
| Frontend interactivity | HTMX + Alpine.js (CSP build) | |
| CSS | Bootstrap + Tailwind CSS | |
| Testing | JUnit 5, Testcontainers, Playwright | |
| Security analysis | SpotBugs, OWASP ZAP | |
| API docs | springdoc-openapi (Swagger UI) | 3.0.1 |

## Application Layers

```
HTTP Request
    │
    ├── Controller (MVC)          src/.../controller/
    │   ├── Web controllers       Thymeleaf views, HTMX partials
    │   └── API controllers       src/.../controller/api/   REST JSON
    │
    ├── Service                   src/.../service/
    │   └── Business logic, transaction management, calculations
    │
    ├── Repository                src/.../repository/
    │   └── Spring Data JPA interfaces, custom queries
    │
    └── Entity                    src/.../entity/
        └── JPA entities, 85+ tables, UUID primary keys
```

### Controller Layer

Two types of controllers:

1. **Web controllers** (`controller/`) -- render Thymeleaf templates, handle form submissions, return HTML fragments for HTMX. Use `@Controller` annotation and `@PreAuthorize` for method-level security.

2. **API controllers** (`controller/api/`) -- return JSON, use `@RestController`. Secured via Bearer token scopes (`@PreAuthorize("hasAuthority('SCOPE_...')")`). All documented with `@Tag`, `@Operation`, `@ApiResponse` from springdoc.

### Service Layer

Contains all business logic. Key patterns:
- `@Transactional` on write methods.
- Services call repositories, never other controllers.
- Formula evaluation uses SpEL with `SimpleEvaluationContext.forReadOnlyDataBinding()` (sandboxed).
- Tax calculations (PPh 21, PPN, PPh 23) are in dedicated services.

### Repository Layer

Spring Data JPA interfaces extending `JpaRepository<Entity, UUID>`. Custom queries use `@Query` with JPQL. Soft-deleted records are filtered via `@SQLRestriction("deleted_at IS NULL")` on entities.

### Entity Layer

All primary entities extend `BaseEntity` which provides:
- UUID primary key (auto-generated).
- `rowVersion` for optimistic locking (`@Version`).
- Audit fields: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`.
- Soft delete: `deletedAt` field, `softDelete()` / `isDeleted()` methods.

Child tables (e.g., `BankStatementItem`, `ReconciliationItem`) have their own UUID PK but do not extend `BaseEntity` -- no version/audit fields.

## Deployment Model

Single-tenant: each company gets its own application instance and PostgreSQL database. No multi-tenancy logic in the code.

## Frontend Architecture

Server-side rendering with progressive enhancement:

- **Thymeleaf** renders full pages and fragments.
- **HTMX** handles partial page updates (form submissions, dynamic lists) without full page reloads.
- **Alpine.js (CSP build)** provides client-side reactivity (form validation, conditional display, calculations).

Alpine.js CSP requirement: all Alpine components must be registered via `Alpine.data()` in `alpine-components.js`. No inline `x-data` expressions in templates -- the CSP policy blocks `unsafe-eval`.

## Database

- PostgreSQL 18 with SSL connections (`sslmode=require`).
- Connection pooling via HikariCP (10 max connections, 2 minimum idle).
- Flyway manages schema migrations (V001--V004 consolidated).
- `ddl-auto=validate` -- Hibernate validates schema against entities but never modifies it.

## Testing Infrastructure

- **Testcontainers** spins up PostgreSQL for integration and functional tests.
- **Playwright** drives browser-based functional tests against the running application.
- **SpotBugs** performs static security analysis.
- **OWASP ZAP** runs DAST (Dynamic Application Security Testing) against the running app.

---

Next: [Project Structure](02-project-structure.md)
