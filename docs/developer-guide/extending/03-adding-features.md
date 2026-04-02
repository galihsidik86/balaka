# Adding Features

Step-by-step guide for adding a new feature to Balaka. Follow existing patterns in the codebase.

## 1. Define the Entity

Create a new entity in `src/.../entity/`. Extend `BaseEntity` for primary entities.

```java
@Entity
@Table(name = "widgets")
@Getter @Setter
@SQLRestriction("deleted_at IS NULL")  // Soft delete filter
public class Widget extends BaseEntity {

    @NotBlank
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WidgetStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category")
    private WidgetCategory category;
}
```

Key conventions:
- Table and column names use `snake_case`.
- UUID primary key via `BaseEntity`.
- `@SQLRestriction("deleted_at IS NULL")` for soft delete filtering.
- `FetchType.LAZY` for all `@ManyToOne` relationships.
- Use `@NotNull`, `@NotBlank`, `@Size` for validation.

For child/detail tables that do not need audit or soft delete, use a standalone UUID PK without extending `BaseEntity`:

```java
@Entity
@Table(name = "widget_items")
@Getter @Setter
public class WidgetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_widget", nullable = false)
    private Widget widget;

    @Column(name = "quantity")
    private Integer quantity;
}
```

## 2. Add the Enum (if needed)

Create in `src/.../enums/`:

```java
public enum WidgetStatus {
    ACTIVE,
    INACTIVE,
    ARCHIVED
}
```

## 3. Create the Repository

```java
public interface WidgetRepository extends JpaRepository<Widget, UUID> {

    List<Widget> findByStatus(WidgetStatus status);

    @Query("SELECT w FROM Widget w JOIN FETCH w.category WHERE w.id = :id")
    Optional<Widget> findByIdWithCategory(@Param("id") UUID id);
}
```

Use `JOIN FETCH` in custom queries to avoid N+1 problems with lazy-loaded relationships.

## 4. Implement the Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class WidgetService {

    private final WidgetRepository widgetRepository;

    @Transactional
    public Widget create(Widget widget) {
        log.info("Creating widget: {}", widget.getName());
        return widgetRepository.save(widget);
    }

    @Transactional(readOnly = true)
    public Page<Widget> findAll(Pageable pageable) {
        return widgetRepository.findAll(pageable);
    }

    @Transactional
    public void delete(UUID id) {
        Widget widget = widgetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Widget not found: " + id));
        widget.softDelete();
        widgetRepository.save(widget);
    }
}
```

Conventions:
- `@Transactional` on write methods, `@Transactional(readOnly = true)` on read methods.
- Throw exceptions for invalid state instead of using fallback values.
- Use `softDelete()` instead of `delete()` for entities extending `BaseEntity`.

## 5. Add Web Controller

```java
@Controller
@RequestMapping("/widgets")
@RequiredArgsConstructor
public class WidgetController {

    private final WidgetService widgetService;

    @GetMapping
    @PreAuthorize("hasAuthority('WIDGET_VIEW')")
    public String list(Model model, Pageable pageable) {
        model.addAttribute("widgets", widgetService.findAll(pageable));
        return "widgets/list";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WIDGET_CREATE')")
    public String create(@Valid Widget widget, BindingResult result) {
        if (result.hasErrors()) {
            return "widgets/form";
        }
        widgetService.create(widget);
        return "redirect:/widgets";
    }
}
```

## 6. Add API Controller (if needed)

```java
@RestController
@RequestMapping("/api/widgets")
@Tag(name = "Widgets", description = "Widget management API")
@PreAuthorize("hasAuthority('SCOPE_widgets:read')")
@RequiredArgsConstructor
@Slf4j
public class WidgetApiController {

    private final WidgetService widgetService;

    @GetMapping
    @Operation(summary = "List widgets")
    public ResponseEntity<Page<WidgetResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(
            widgetService.findAll(pageable).map(WidgetResponse::from));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_widgets:write')")
    @Operation(summary = "Create a widget")
    public ResponseEntity<WidgetResponse> create(
            @Valid @RequestBody CreateWidgetRequest request) {
        Widget widget = widgetService.create(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WidgetResponse.from(widget));
    }
}
```

API controller conventions:
- Place in `controller/api/` package.
- Use `@RestController` and `@RequestMapping("/api/...")`.
- Annotate with `@Tag` for Swagger grouping.
- Class-level `@PreAuthorize` for the read scope, method-level for write.
- Use records for request/response DTOs.

## 7. Add Thymeleaf Template

Create templates in `src/main/resources/templates/widgets/`. Use the layout from `fragments/main.html`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      th:replace="~{fragments/main :: layout(~{::title}, ~{::content})}">
<head><title>Widgets</title></head>
<body>
<div th:fragment="content">
    <h1 class="text-2xl font-bold mb-4">Widgets</h1>
    <!-- content here -->
</div>
</body>
</html>
```

For HTMX interactions, return HTML fragments from the controller.

## 8. Add Database Schema

Modify the appropriate migration file:

- `V002__core_schema.sql` for core accounting tables.
- `V003__feature_schema.sql` for feature-specific tables.
- `V004__seed_data.sql` for seed data.

```sql
-- In V003__feature_schema.sql
CREATE TABLE widgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    id_category UUID REFERENCES widget_categories(id),
    row_version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP
);
```

## 9. Add Permissions

Add permission constants in `Permission.java`:

```java
public static final String WIDGET_VIEW = "WIDGET_VIEW";
public static final String WIDGET_CREATE = "WIDGET_CREATE";
public static final String WIDGET_EDIT = "WIDGET_EDIT";
public static final String WIDGET_DELETE = "WIDGET_DELETE";
```

Add to the appropriate role sets (`ADMIN_PERMISSIONS`, `OWNER_PERMISSIONS`, etc.).

Add to `V004__seed_data.sql`:

```sql
INSERT INTO permissions (id, name) VALUES
    (gen_random_uuid(), 'WIDGET_VIEW'),
    (gen_random_uuid(), 'WIDGET_CREATE'),
    (gen_random_uuid(), 'WIDGET_EDIT'),
    (gen_random_uuid(), 'WIDGET_DELETE');
```

If the feature needs API access, add the scope to `DeviceAuthService`:

```java
deviceToken.setScopes("...,widgets:read,widgets:write");
```

## 10. Add Sidebar Entry

Add navigation in `fragments/main.html` (both desktop and mobile sidebars). The sidebar uses `<details>` elements for collapsible groups.

## 11. Write Tests

See [Testing](05-testing.md) for the test pyramid and patterns.

## Checklist

- [ ] Entity with `BaseEntity` (or standalone UUID PK for child tables)
- [ ] Repository with custom queries using `JOIN FETCH`
- [ ] Service with `@Transactional`
- [ ] Web controller with `@PreAuthorize`
- [ ] API controller with `@Tag`, `@Operation`, scope-based auth
- [ ] Thymeleaf template using layout fragment
- [ ] Migration in V002/V003
- [ ] Permissions in `Permission.java` and V004
- [ ] Sidebar entry in `main.html`
- [ ] Functional test (Playwright)
- [ ] SpotBugs passes (`./mvnw spotbugs:check`)

---

Next: [Seed Packs](04-seed-packs.md)
