# Integrasi AI

Referensi layar fitur integrasi AI: otorisasi perangkat, pencatatan transaksi via API, laporan analisis AI, dan pengelolaan dokumen transaksi.

---

## Otorisasi Perangkat (Device Flow)

AI assistant mengakses aplikasi melalui **OAuth 2.0 Device Authorization Flow** (RFC 8628). Proses ini melibatkan 3 langkah antara AI dan pengguna.

### Langkah 1: AI Meminta Kode

AI mengirim request ke `POST /api/device/code` dengan `clientId` (contoh: `claude-code`). Sistem mengembalikan:
- **deviceCode** -- Kode untuk polling token
- **userCode** -- Kode yang harus dimasukkan pengguna (contoh: `MBJN-KRFJ`)
- **verificationUri** -- URL halaman otorisasi
- **expiresIn** -- Masa berlaku kode (900 detik = 15 menit)

### Langkah 2: Pengguna Otorisasi di Browser

**URL:** `/device` atau URL yang diberikan AI

Buka URL otorisasi di browser. Halaman menampilkan:

1. **Form login** -- Login dengan username dan password
2. **Field kode perangkat** -- Masukkan kode yang ditampilkan AI (contoh: `MBJN-KRFJ`)
3. **Field nama perangkat** (opsional) -- Beri nama deskriptif, contoh: "Claude Code di MacBook"
4. **Tombol Otorisasi Perangkat** -- Klik untuk menyetujui akses

Kode perangkat hanya berlaku 15 menit dan hanya bisa digunakan sekali.

### Langkah 3: AI Mendapat Token

Setelah pengguna otorisasi, AI mendapat access token yang berlaku 30 hari. Token ini digunakan sebagai `Authorization: Bearer {token}` di setiap request API selanjutnya.

### Scope yang Tersedia

| Scope | Hak Akses |
|-------|-----------|
| `drafts:create` | Membuat dan mengedit draft transaksi |
| `drafts:approve` | Approve/reject draft |
| `drafts:read` | Membaca draft |
| `analysis:read` | Membaca laporan keuangan |
| `analysis:write` | Mempublikasikan laporan analisis |
| `transactions:post` | Posting, koreksi, dan hapus transaksi DRAFT |
| `data:import` | Import data dari file ZIP |
| `bills:read` | Membaca vendor bills |
| `bills:create` | Membuat vendor bills |
| `bills:approve` | Approve dan mark-paid vendor bills |
| `bank-recon:read` | Membaca data rekonsiliasi bank |
| `bank-recon:write` | Import statement, matching, complete reconciliation |
| `tax-export:read` | Export data pajak dan membaca data karyawan/payroll |

---

## OpenAPI Spec (API Discovery)

AI assistant membaca OpenAPI spec sebelum berinteraksi dengan API.

**URL:** `GET /v3/api-docs` (tanpa autentikasi)

**Swagger UI:** `/swagger-ui.html`

OpenAPI spec berisi:
- `paths` -- Seluruh endpoint API
- `components.schemas` -- Request/response schema
- `x-authentication` -- Alur Device Flow lengkap, daftar scope
- `x-workflows` -- 13 alur kerja end-to-end
- `x-csv-files` -- 16 spesifikasi CSV untuk data import
- `x-industries` -- Kode industri yang didukung
- `x-error-codes` -- Kode error dan HTTP status

---

## Pencatatan Transaksi via API

AI mencatat transaksi menggunakan template jurnal yang memiliki metadata semantik (keywords, merchant patterns, semantic description).

### Alur Posting Langsung

1. AI membaca daftar template: `GET /api/templates`
2. AI mencocokkan input pengguna dengan template berdasarkan keyword, merchant, amount range
3. AI mengirim transaksi: `POST /api/transactions` dengan `templateId`, `amount`, `description`, `accountSlots`
4. Transaksi langsung berstatus POSTED

### Alur Draft (Review Dulu)

1. AI membuat draft: `POST /api/drafts`
2. AI preview jurnal: `GET /api/transactions/{id}/journal-preview`
3. Pengguna review dan approve: `POST /api/transactions/{id}/post`

### Koreksi dan Pembatalan

- Koreksi draft: `PUT /api/transactions/{id}` (hanya status DRAFT)
- Void transaksi posted: `POST /api/transactions/{id}/void` (membuat reversing entries otomatis)
- Purge transaksi void: `DELETE /api/transactions/purge-voided`

---

## Dokumen Pendukung Transaksi

AI dapat mengupload dan mengelola dokumen pendukung (struk, invoice, bukti potong) per transaksi.

| Endpoint | Keterangan |
|----------|------------|
| `POST /api/transactions/{id}/documents` | Upload dokumen (multipart/form-data) |
| `GET /api/transactions/{id}/documents` | Daftar dokumen |
| `GET /api/transactions/{id}/documents/{docId}` | Download dokumen |
| `DELETE /api/transactions/{id}/documents/{docId}` | Hapus dokumen |

Dokumen dienkripsi at rest dengan unique key per dokumen.

---

## Laporan Analisis AI

### Daftar Laporan

**Navigasi:** Sidebar > **Laporan** > **Analisis AI**

**URL:** `/analysis/reports`

**Permission:** `ANALYSIS_REPORT_VIEW`

Tabel menampilkan laporan yang sudah dipublikasikan oleh AI. Setiap baris:

| Kolom | Keterangan |
|-------|------------|
| Judul | Judul laporan |
| Tipe | Tipe laporan (sesuai industri) |
| Periode | Periode yang dianalisis |
| Tanggal Publikasi | Kapan laporan dipublikasikan |

### Detail Laporan

**URL:** `/analysis/reports/{id}`

Halaman menampilkan 5 bagian terstruktur:

| Bagian | Keterangan |
|--------|------------|
| Ringkasan Eksekutif | Paragraf singkat kondisi keuangan |
| Indikator Utama | Metrik KPI dengan status warna (hijau/kuning/merah) dan perubahan vs periode sebelumnya |
| Temuan | Hal-hal yang perlu diperhatikan, dengan severity badge |
| Rekomendasi | Langkah yang disarankan, dengan prioritas |
| Penilaian Risiko | Risiko yang teridentifikasi beserta mitigasi |

### Industri yang Didukung

| Kode Industri | Contoh Tipe Laporan |
|---------------|---------------------|
| `it-service` | utilization-review, project-profitability, receivables-aging |
| `online-seller` | marketplace-profitability, inventory-health, cash-conversion-cycle |
| `coffee-shop` | food-cost-review, labor-efficiency, menu-engineering |
| `campus` | tuition-collection, budget-realization, scholarship-impact |

Industri perusahaan dikonfigurasi di **Pengaturan** > **Perusahaan** (field `industry`).

---

## Data Import via API

AI dapat menginisialisasi data perusahaan via `POST /api/data-import` (scope: `data:import`).

**Format:** File ZIP berisi file CSV bernomor urut:

| File | Isi |
|------|-----|
| `01_company_config.csv` | Konfigurasi perusahaan |
| `02_chart_of_accounts.csv` | Chart of Accounts |
| `04_journal_templates.csv` | Template jurnal |
| `05_journal_template_lines.csv` | Baris template jurnal |

Import mengganti (truncate) data pada tabel yang memiliki CSV di dalam ZIP. Tabel tanpa CSV tidak terpengaruh.

Contoh seed data per industri tersedia di `industry-seed/{it-service,online-seller,coffee-shop,campus}/seed-data/`.

---

Selanjutnya: [Pengelolaan Data](08-data-management.md)
