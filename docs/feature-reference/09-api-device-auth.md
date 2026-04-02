# Otorisasi Perangkat API

Referensi layar fitur otorisasi perangkat: alur Device Flow, pengelolaan token, dan pencabutan akses.

---

## Alur Device Authorization Flow

OAuth 2.0 Device Authorization Flow (RFC 8628) memungkinkan AI assistant atau perangkat lain mengakses API tanpa memasukkan password secara langsung.

```
AI Assistant                    Aplikasi                     Pengguna
     |                              |                            |
     |-- POST /api/device/code ---->|                            |
     |<-- deviceCode + userCode ----|                            |
     |                              |                            |
     |   "Buka URL ini,             |                            |
     |    masukkan kode: MBJN-KRFJ" |                            |
     |                              |<-- Buka /device -----------|
     |                              |<-- Login + masukkan kode --|
     |                              |<-- Klik Otorisasi ---------|
     |                              |                            |
     |-- POST /api/device/token --->|                            |
     |<-- accessToken --------------|                            |
     |                              |                            |
     |-- GET /api/... (Bearer) ---->|                            |
```

---

## Halaman Otorisasi (Pengguna)

**URL:** `/device`

Halaman ini dibuka oleh pengguna di browser untuk mengotorisasi perangkat.

### Field

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Username | Ya | Text | Username login |
| Password | Ya | Password | Password login |
| Kode Perangkat | Ya | Text | Kode yang ditampilkan AI (contoh: `MBJN-KRFJ`) |
| Nama Perangkat | Tidak | Text | Nama deskriptif, contoh: "Claude Code di MacBook" |

**Tombol:**
- **Otorisasi Perangkat** -- Menyetujui akses dan menerbitkan token

### Perilaku

- Kode perangkat berlaku 15 menit sejak diterbitkan
- Kode hanya bisa digunakan sekali
- Token yang diterbitkan berlaku 30 hari
- Token memiliki seluruh scope yang tersedia

---

## Pengelolaan Perangkat (Token Management)

### Halaman Perangkat API

**Navigasi:** Sidebar > **Master Data** > **Perangkat API**

**URL:** `/settings/devices`

Halaman menampilkan daftar device token yang aktif milik pengguna yang sedang login.

### Tabel Token

| Kolom | Keterangan |
|-------|------------|
| Perangkat | Nama device (yang diisi saat otorisasi). Kosong jika tidak diisi |
| Client ID | Identifier aplikasi, contoh: `claude-code` |
| Scope | Hak akses token, contoh: `drafts:create,analysis:read,...` |
| Status | **Aktif** (badge hijau) atau **Kedaluwarsa** (badge abu-abu) |
| Dibuat | Waktu token diterbitkan |
| Terakhir Digunakan | Waktu terakhir token dipakai untuk request API |
| IP Terakhir | IP address terakhir yang menggunakan token |

### Tombol Aksi

| Tombol | Keterangan |
|--------|------------|
| Cabut | Mencabut satu token tertentu. Token yang dicabut tidak bisa digunakan lagi |
| Cabut Semua | Mencabut seluruh token aktif milik pengguna sekaligus |

Setelah token dicabut:
- AI assistant yang menggunakan token tersebut mendapat HTTP 401 Unauthorized
- Pengguna perlu melakukan Device Flow ulang untuk mendapatkan token baru
- Event pencabutan dicatat di audit log

### Admin View

Administrator dapat melihat dan mencabut token dari halaman detail pengguna:

**Navigasi:** Sidebar > **Pengaturan** > **Pengguna** > klik user > bagian **Sesi Perangkat Aktif**

---

## API Endpoints

### Device Code (Tanpa Autentikasi)

**`POST /api/device/code`**

Request:
```json
{
  "clientId": "claude-code"
}
```

Response:
```json
{
  "deviceCode": "a79d766e4972e61d...",
  "userCode": "MBJN-KRFJ",
  "verificationUri": "https://balaka.example.com/device",
  "verificationUriComplete": "https://balaka.example.com/device?code=MBJN-KRFJ",
  "expiresIn": 900,
  "interval": 5
}
```

| Field | Keterangan |
|-------|------------|
| deviceCode | Kode internal untuk polling token |
| userCode | Kode yang harus dimasukkan pengguna |
| verificationUri | URL halaman otorisasi |
| verificationUriComplete | URL dengan kode sudah terisi |
| expiresIn | Masa berlaku kode dalam detik (900 = 15 menit) |
| interval | Interval polling dalam detik |

### Token Polling (Tanpa Autentikasi)

**`POST /api/device/token`**

Request:
```json
{
  "deviceCode": "a79d766e4972e61d..."
}
```

Response (berhasil):
```json
{
  "accessToken": "15d07ef9030cba7b...",
  "tokenType": "Bearer",
  "expiresIn": 2592000,
  "scope": "drafts:create,drafts:approve,..."
}
```

| Field | Keterangan |
|-------|------------|
| accessToken | Token untuk autentikasi API |
| tokenType | Selalu `Bearer` |
| expiresIn | Masa berlaku token dalam detik (2592000 = 30 hari) |
| scope | Daftar scope yang diberikan |

Response (belum diotorisasi):
```json
{
  "error": "authorization_pending",
  "errorDescription": "The authorization request is still pending"
}
```

Response (kode kedaluwarsa):
```json
{
  "error": "expired_token",
  "errorDescription": "Device code has expired"
}
```

---

## Scope Reference

| Scope | Keterangan |
|-------|------------|
| `drafts:create` | Membuat dan mengedit draft transaksi |
| `drafts:approve` | Approve/reject draft |
| `drafts:read` | Membaca draft |
| `analysis:read` | Membaca laporan keuangan (snapshot, trial balance, neraca, dll) |
| `analysis:write` | Mempublikasikan laporan analisis AI |
| `transactions:post` | Posting, koreksi, void, purge transaksi |
| `data:import` | Import data dari file ZIP |
| `bills:read` | Membaca tagihan vendor |
| `bills:create` | Membuat tagihan vendor baru |
| `bills:approve` | Approve dan mark-paid tagihan vendor |
| `bank-recon:read` | Membaca data rekonsiliasi bank |
| `bank-recon:write` | Import statement, matching, complete reconciliation |
| `tax-export:read` | Export data pajak, membaca data karyawan/payroll |

Request tanpa scope yang sesuai mendapat HTTP 403 Forbidden.

---

Kembali ke: [Rekonsiliasi Bank](01-bank-reconciliation.md)
