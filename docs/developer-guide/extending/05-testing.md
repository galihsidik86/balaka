# Testing

## Test Pyramid

| Level | Tool | Data Source | Location | Count |
|-------|------|-------------|----------|-------|
| Unit | JUnit 5 + Mockito | Mocks | `src/test/java/.../unit/` | Fast |
| Integration | Testcontainers (PostgreSQL) | V800-V912 migrations | `src/test/java/.../integration/` | ~30s startup |
| Functional (E2E) | Playwright | Industry seed initializers | `src/test/java/.../functional/` | ~60-90 min total |
| Security (SAST) | SpotBugs | Source code | N/A | ~1 min |
| Security (DAST) | OWASP ZAP | Running app | `src/test/java/.../functional/ZapDastTest.java` | ~5-15 min |

## Running Tests

### Full Test Suite

The full test suite takes 60-90 minutes. Always run in background with log capture. Never run multiple instances simultaneously.

```bash
# Background execution with log capture
nohup ./mvnw test > target/test-output.log 2>&1 &

# Or foreground with tee
./mvnw test 2>&1 | tee target/test-output.log
```

### Specific Test

```bash
./mvnw test -Dtest=TransactionTest
```

### Visible Browser (Debugging)

```bash
./mvnw test -Dtest=TransactionTest -Dplaywright.headless=false -Dplaywright.slowmo=100
```

### SpotBugs Only

```bash
./mvnw spotbugs:check
# Results: target/spotbugsXml.xml
```

### DAST Only

```bash
# Full scan (active + passive, ~15 min)
./mvnw test -Dtest=ZapDastTest

# Quick scan (passive only, ~1 min)
./mvnw test -Dtest=ZapDastTest -Ddast.quick=true

# Results: target/security-reports/zap-*.html
```

## Test Data Strategy

### Integration Tests

Use Flyway test migrations in `src/test/resources/db/test/integration/`:

| Migration | Content |
|-----------|---------|
| `V800__base_test_data.sql` | Base test data (users, roles) |
| `V900-V912` | Domain-specific data (templates, transactions, payroll, drafts, etc.) |

These migrations run automatically when Testcontainers spins up the PostgreSQL instance.

### Functional Tests

Functional tests do NOT use migration-based test data. Instead, they use `@TestConfiguration` initializers that load industry seed packs programmatically:

```java
@TestConfiguration
@Profile("functional")
public class ItServiceTestDataInitializer {

    @Autowired
    private DataImportService dataImportService;

    @PostConstruct
    public void init() {
        dataImportService.importFromSeedPack("it-service");
    }
}
```

Test classes import the relevant initializer:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
@Import(ItServiceTestDataInitializer.class)
class TransactionTest {
    // ...
}
```

Industry-specific tests have their own subdirectories:
- `functional/seller/` -- online seller tests
- `functional/manufacturing/` -- coffee shop tests
- `functional/campus/` -- campus tests

## Writing Functional Tests (Playwright)

### Basic Structure

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("functional")
class WidgetTest {

    @LocalServerPort
    private int port;

    private Playwright playwright;
    private Browser browser;
    private Page page;

    @BeforeEach
    void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
    }

    @AfterEach
    void teardown() {
        browser.close();
        playwright.close();
    }

    @Test
    void shouldCreateWidget() {
        // Login
        page.navigate("http://localhost:" + port + "/login");
        page.fill("input[name=username]", "admin");
        page.fill("input[name=password]", "password");
        page.click("button[type=submit]");

        // Navigate and interact
        page.navigate("http://localhost:" + port + "/widgets");
        // ... assertions
    }
}
```

### Playwright Gotchas

- **`<option>` elements in `<select>` are NOT "visible"** to Playwright. Do not use `assertThat(option).isVisible()`. Use `evaluate("el => el.value")` to get the selected value.

- **`text=X` is a substring match**. Use `.font-medium:text-is('Exact Text')` or `getByText(text, new Page.GetByTextOptions().setExact(true))` when precision is needed.

- **`<form>` inside `<tr>` is invalid HTML**. The browser strips it. Use card layout with forms instead of table rows wrapping forms.

- **Lazy fetch in tests**: entity relationships marked `LAZY` will throw `LazyInitializationException` outside a transaction. Use `JOIN FETCH` in repository queries or `@Transactional` on the test class.

- **Locator specificity**: `a[href*='/report']` can match sidebar links. Use more specific selectors like `a[href*='reconciliations'][href*='report']`.

## Writing Integration Tests

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class WidgetRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    private WidgetRepository widgetRepository;

    @Test
    void shouldFindByStatus() {
        // Test repository methods against real PostgreSQL
    }
}
```

## SpotBugs

The project maintains a zero-issue SpotBugs policy. Any new exclusions in `spotbugs-exclude.xml` must include comprehensive justifications with mitigation details.

```bash
./mvnw spotbugs:check
```

If SpotBugs reports a false positive:
1. Add an exclusion to `spotbugs-exclude.xml`.
2. Include a detailed comment explaining why it is a false positive.
3. Document what mitigation is in place.

---

Next: [Contributing](06-contributing.md)
