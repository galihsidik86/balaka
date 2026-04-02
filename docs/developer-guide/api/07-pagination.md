# Pagination

List endpoints that return collections use Spring Data's standard pagination. This applies to payroll runs, transactions, and other list APIs.

## Request Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `20` | Items per page |
| `sort` | string | varies | Sort field and direction (e.g., `transactionDate,desc`) |

### Sort Syntax

The `sort` parameter accepts a property name and optional direction:

```
sort=propertyName,asc    (ascending)
sort=propertyName,desc   (descending)
sort=propertyName        (ascending by default)
```

Multiple sort fields can be specified by repeating the parameter:

```
GET /api/payroll?sort=payrollPeriod,desc&sort=status,asc
```

## Response Envelope

Paginated responses use Spring Data's `Page` structure:

```json
{
  "content": [
    { "id": "uuid-1", "payrollPeriod": "2025-12", "status": "POSTED", ... },
    { "id": "uuid-2", "payrollPeriod": "2025-11", "status": "POSTED", ... }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0,
  "numberOfElements": 20,
  "first": true,
  "last": false,
  "empty": false,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  }
}
```

| Field | Description |
|-------|-------------|
| `content` | Array of items for the current page |
| `totalElements` | Total number of items across all pages |
| `totalPages` | Total number of pages |
| `number` | Current page number (0-based) |
| `size` | Requested page size |
| `numberOfElements` | Actual number of items in this page |
| `first` | `true` if this is the first page |
| `last` | `true` if this is the last page |
| `empty` | `true` if the page has no content |

## Example: Iterate All Pages

```bash
PAGE=0
TOTAL_PAGES=1

while [ $PAGE -lt $TOTAL_PAGES ]; do
  RESPONSE=$(curl -s "https://balaka.example.com/api/payroll?page=$PAGE&size=10&sort=payrollPeriod,desc" \
    -H "Authorization: Bearer $TOKEN")

  echo "$RESPONSE" | jq '.content[]'

  TOTAL_PAGES=$(echo "$RESPONSE" | jq '.totalPages')
  PAGE=$((PAGE + 1))
done
```

## Endpoints Using Pagination

| Endpoint | Default Sort |
|----------|-------------|
| `GET /api/payroll` | by payroll period |
| `GET /api/analysis/accounts` | by account code |
| `GET /api/bills` | by bill date |

Non-paginated endpoints (reports, single-entity lookups) return their data directly without the `Page` wrapper.

---

Previous: [Tax Export](06-tax-export.md)
