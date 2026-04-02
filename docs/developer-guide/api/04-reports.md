# Reports API

Financial report endpoints for reading accounting data. All endpoints require `Authorization: Bearer <token>` with the `analysis:read` scope.

Base URL: `/api/analysis`

## Response Envelope

All report endpoints return an `AnalysisResponse<T>` wrapper:

```json
{
  "reportType": "trial-balance",
  "generatedAt": "2025-12-31T10:00:00",
  "parameters": {
    "asOfDate": "2025-12-31"
  },
  "data": { ... },
  "metadata": {
    "currency": "IDR",
    "accountingBasis": "accrual",
    "description": "..."
  }
}
```

| Field | Description |
|-------|-------------|
| `reportType` | Report identifier string |
| `generatedAt` | ISO 8601 timestamp when the report was generated |
| `parameters` | Echo of the request parameters |
| `data` | Report-specific payload (varies per endpoint) |
| `metadata` | Additional context (currency, descriptions) |

## Company Info

```http
GET /api/analysis/company
```

Returns company configuration: name, industry, currency, fiscal year start month, PKP status, NPWP.

## Monthly Snapshot

```http
GET /api/analysis/snapshot?month=2025-12&year=2025
```

Returns KPI dashboard data: revenue, expenses, net profit, profit margin, cash balance, receivables, payables, transaction count, and month-over-month change percentages.

## Trial Balance

```http
GET /api/analysis/trial-balance?asOfDate=2025-12-31
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `asOfDate` | date | yes | Balance date (`yyyy-MM-dd`) |

Response `data`:

```json
{
  "items": [
    {
      "accountCode": "1-1100",
      "accountName": "Kas",
      "debit": 50000000.00,
      "credit": 0.00
    }
  ],
  "totalDebit": 150000000.00,
  "totalCredit": 150000000.00
}
```

## Income Statement

```http
GET /api/analysis/income-statement?startDate=2025-01-01&endDate=2025-12-31&excludeClosing=true
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `startDate` | date | yes | Period start (`yyyy-MM-dd`) |
| `endDate` | date | yes | Period end (`yyyy-MM-dd`) |
| `excludeClosing` | boolean | no | Exclude closing journal entries (default: `false`) |

Response `data`:

```json
{
  "revenueItems": [
    { "accountCode": "4-1100", "accountName": "Pendapatan Jasa", "amount": 120000000.00 }
  ],
  "expenseItems": [
    { "accountCode": "5-1100", "accountName": "Beban Gaji", "amount": 60000000.00 }
  ],
  "totalRevenue": 120000000.00,
  "totalExpense": 80000000.00,
  "netIncome": 40000000.00
}
```

## Balance Sheet

```http
GET /api/analysis/balance-sheet?asOfDate=2025-12-31
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `asOfDate` | date | yes | Balance date (`yyyy-MM-dd`) |

Response `data`:

```json
{
  "assetItems": [
    { "accountCode": "1-1100", "accountName": "Kas", "amount": 50000000.00 }
  ],
  "liabilityItems": [
    { "accountCode": "2-1100", "accountName": "Hutang Usaha", "amount": 10000000.00 }
  ],
  "equityItems": [
    { "accountCode": "3-1100", "accountName": "Modal Disetor", "amount": 100000000.00 }
  ],
  "totalAssets": 150000000.00,
  "totalLiabilities": 10000000.00,
  "totalEquity": 100000000.00,
  "currentYearEarnings": 40000000.00
}
```

The accounting equation holds: `totalAssets = totalLiabilities + totalEquity + currentYearEarnings`.

## Cash Flow Statement

```http
GET /api/analysis/cash-flow?startDate=2025-01-01&endDate=2025-12-31
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `startDate` | date | yes | Period start (`yyyy-MM-dd`) |
| `endDate` | date | yes | Period end (`yyyy-MM-dd`) |

Response `data`:

```json
{
  "operatingItems": [
    { "description": "Pendapatan Jasa", "amount": 120000000.00 }
  ],
  "investingItems": [
    { "description": "Pembelian Aset Tetap", "amount": -15000000.00 }
  ],
  "financingItems": [],
  "operatingTotal": 40000000.00,
  "investingTotal": -15000000.00,
  "financingTotal": 0.00,
  "netCashChange": 25000000.00,
  "beginningCashBalance": 25000000.00,
  "endingCashBalance": 50000000.00,
  "cashAccountBalances": [
    { "accountName": "Bank BCA", "balance": 45000000.00 },
    { "accountName": "Kas", "balance": 5000000.00 }
  ]
}
```

Positive amounts represent cash inflows, negative amounts represent cash outflows. Cash flow categories are determined by the `cashFlowCategory` field on journal templates (`OPERATING`, `INVESTING`, `FINANCING`, `NON_CASH`).

## Tax Summary

```http
GET /api/analysis/tax-summary?startDate=2025-01-01&endDate=2025-12-31
```

Returns aggregated tax data for the period: PPN, PPh 21, PPh 23, PPh 4(2), PPh 25.

## Additional Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /api/analysis/accounts` | Chart of accounts with balances |
| `GET /api/analysis/transactions/{id}` | Single transaction with full journal entries |
| `GET /api/analysis/ledger` | General ledger entries for a date range |

For the full list of available endpoints and their schemas, see the Swagger UI at `/swagger-ui.html`.

---

Next: [Payroll](05-payroll.md)
