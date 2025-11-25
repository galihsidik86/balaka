# Claude Instructions

## Project Overview

Indonesian accounting application for small businesses. Spring Boot 4.0 + Thymeleaf + PostgreSQL.

## Current Status

- **Phase 0:** ✅ Complete (project setup, auth, CI/CD)
- **Phase 1:** 🔄 In Progress (Core Accounting MVP)
  - 1.1 COA: ✅ Complete
  - 1.2 Journal Entries: ✅ Complete
  - 1.3 Basic Reports: ✅ Complete
  - 1.4 Journal Templates: ✅ Complete
  - 1.5 Transactions: ⏳ In Progress - See `TODO-TRANSACTIONS.md`
  - See `docs/06-implementation-plan.md` for full plan

## Key Files

| Purpose | Location |
|---------|----------|
| Implementation Plan | `docs/06-implementation-plan.md` |
| Transactions TODO | `TODO-TRANSACTIONS.md` |
| Entities | `src/main/java/.../entity/` |
| Services | `src/main/java/.../service/` |
| Controllers | `src/main/java/.../controller/` |
| Templates | `src/main/resources/templates/` |
| Migrations | `src/main/resources/db/migration/` |
| Functional Tests | `src/test/java/.../functional/` |

## Development Guidelines

1. **Feature completion criteria:** Item is only checked when verified by Playwright functional test
2. **No fallback/default values:** Throw errors instead of silently handling missing data
3. **Technical language:** No marketing speak, strictly technical documentation
4. **Test-driven:** Write functional tests for new features

## Running the App

```bash
# Run tests
./mvnw test

# Run specific functional test
./mvnw test -Dtest=ChartOfAccountSeedDataTest

# Run with visible browser (debugging)
./mvnw test -Dtest=ChartOfAccountSeedDataTest -Dplaywright.headless=false -Dplaywright.slowmo=100
```

## Database

- PostgreSQL via Testcontainers (tests)
- Flyway migrations: V001-V006
- Seed data: IT Services COA, admin user (admin/admin)

## Architecture

```
User → Controller (MVC) → Service → Repository → PostgreSQL
         ↓
    Thymeleaf Templates (HTMX + Alpine.js)
```

## Current Focus

Next: Transactions (1.5) per implementation plan:
1. Convert UI templates from mockup to dynamic Thymeleaf
2. Wire form submissions to REST API
3. Fix page objects to match actual HTML
4. Write Playwright functional tests

**Note:** Backend is complete (entities, service, controller). Frontend templates are mockups with hardcoded data.
