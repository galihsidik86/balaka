# API Authentication

Balaka uses OAuth 2.0 Device Authorization Flow ([RFC 8628](https://tools.ietf.org/html/rfc8628)) for API authentication. This flow is designed for devices and CLI tools that cannot open a browser-based login form directly.

## Authentication Flow

```
CLI/Device                    Balaka API                     Browser
    |                             |                             |
    |-- POST /api/device/code --->|                             |
    |<-- deviceCode, userCode ----|                             |
    |                             |                             |
    |  (display userCode to user)                               |
    |                             |                             |
    |                             |<-- user visits /device ----->|
    |                             |    enters userCode           |
    |                             |    clicks Authorize          |
    |                             |                             |
    |-- POST /api/device/token -->|                             |
    |<-- accessToken, Bearer -----|                             |
    |                             |                             |
    |-- GET /api/analysis/...  -->|                             |
    |   Authorization: Bearer XXX |                             |
```

### Step 1: Request Device Code

```http
POST /api/device/code
Content-Type: application/json

{
  "clientId": "my-integration"
}
```

Response:

```json
{
  "deviceCode": "abc123...",
  "userCode": "ABCD-EFGH",
  "verificationUri": "https://balaka.example.com/device",
  "verificationUriComplete": "https://balaka.example.com/device?code=ABCD-EFGH",
  "expiresIn": 900,
  "interval": 5
}
```

- `deviceCode` -- opaque string used to poll for the token (do not show to user).
- `userCode` -- short code the user enters in their browser.
- `verificationUri` -- URL where the user authorizes the device.
- `expiresIn` -- device code validity in seconds (15 minutes).
- `interval` -- minimum polling interval in seconds.

### Step 2: User Authorization

The user opens `verificationUriComplete` in their browser (or navigates to `verificationUri` and enters the `userCode` manually). They must be logged in to Balaka. After entering the code, they click "Authorize" to grant access.

### Step 3: Poll for Token

While the user authorizes, the client polls for an access token:

```http
POST /api/device/token
Content-Type: application/json

{
  "deviceCode": "abc123..."
}
```

**Pending response** (HTTP 400):

```json
{
  "error": "authorization_pending",
  "errorDescription": "The authorization request is still pending"
}
```

**Success response** (HTTP 200):

```json
{
  "accessToken": "bk_xxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "tokenType": "Bearer",
  "expiresIn": 2592000,
  "scope": "drafts:create,drafts:approve,drafts:read,analysis:read,analysis:write,transactions:post,data:import,bills:read,bills:create,bills:approve,tax-export:read,accounts:read,accounts:write"
}
```

**Error responses** (HTTP 400):

| error | meaning |
|-------|---------|
| `authorization_pending` | User has not yet authorized. Keep polling. |
| `expired_token` | Device code expired (15 min). Restart from Step 1. |
| `access_denied` | User denied authorization. |
| `invalid_request` | Invalid or missing device code. |

### Step 4: Use the Token

Include the token in the `Authorization` header for all API requests:

```http
GET /api/analysis/trial-balance?asOfDate=2025-12-31
Authorization: Bearer bk_xxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

## Scopes

Tokens are issued with all available scopes. Each scope maps to a `SCOPE_` authority in Spring Security:

| Scope | Grants access to |
|-------|-----------------|
| `drafts:create` | Create draft transactions |
| `drafts:approve` | Approve/reject drafts |
| `drafts:read` | Read draft transactions |
| `analysis:read` | Financial reports, snapshots, trial balance, balance sheet, income statement, cash flow |
| `analysis:write` | Publish AI analysis reports |
| `transactions:post` | Create, update, post, void, delete transactions; free-form journal entries |
| `data:import` | Import industry seed data |
| `bills:read` | Read vendor bills |
| `bills:create` | Create vendor bills |
| `bills:approve` | Approve vendor bills |
| `tax-export:read` | Tax reports, SPT data, payroll, employees, fiscal adjustments, salary components |
| `accounts:read` | Read chart of accounts |
| `accounts:write` | Create/update/delete chart of accounts |

## Token Properties

- Validity: 30 days from issuance.
- Storage: tokens are hashed (SHA-256) before storage. The plaintext token is returned only once during issuance.
- Revocation: tokens can be revoked from the web UI at `/settings/devices`.

## Token Management UI

Administrators can manage device tokens at **Settings > Perangkat Terhubung** (`/settings/devices`):

- View all active tokens (device name, client ID, last used, scopes).
- Revoke individual tokens.
- See when tokens were last used and from which IP.

## No Authentication Required

The device code and token endpoints (`/api/device/code` and `/api/device/token`) do not require a Bearer token. They are the entry point for obtaining one.

---

Next: [Quickstart](02-quickstart.md)
