# Rekonsiliasi Bank

Referensi layar fitur rekonsiliasi bank: konfigurasi parser, import mutasi, pencocokan transaksi, dan laporan rekonsiliasi.

---

## Konfigurasi Parser

### Halaman Daftar Parser

**Navigasi:** Sidebar > **Rekonsiliasi Bank** > **Konfigurasi Parser**

**URL:** `/bank-recon/configs`

Halaman menampilkan tabel parser yang tersedia. Setiap baris menampilkan nama bank, tipe, dan format tanggal.

**Parser bawaan:**

| Bank | Tipe | Format Tanggal | Kolom |
|------|------|---------------|-------|
| BCA | BCA | dd/MM/yyyy | Tanggal, Keterangan, Cabang, Debit, Credit, Saldo |
| Mandiri | MANDIRI | dd/MM/yyyy | Tanggal, Keterangan, Debit, Kredit, Saldo |
| BNI | BNI | dd-MMM-yyyy | Tanggal, Keterangan, Cabang, Debit, Kredit, Saldo |
| BSI | BSI | dd/MM/yyyy | Tanggal, Keterangan, Debit, Kredit, Saldo |
| CIMB | CIMB | dd/MM/yyyy | Tanggal, Keterangan, Debit, Kredit, Saldo |

**Tombol:**
- **Parser Baru** -- Membuka formulir pembuatan parser kustom

### Formulir Parser Kustom

**Navigasi:** Konfigurasi Parser > **Parser Baru**

**URL:** `/bank-recon/configs/new`

**Field:**

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Tipe Bank | Ya | Dropdown | Pilih `CUSTOM` untuk parser kustom |
| Nama Config | Ya | Text | Nama deskriptif, contoh: "Bank Jatim CSV" |
| Kolom Tanggal | Ya | Number | Index kolom tanggal dalam CSV (dimulai dari 0) |
| Kolom Keterangan | Ya | Number | Index kolom deskripsi transaksi |
| Format Tanggal | Ya | Text | Pattern Java DateTimeFormatter, contoh: `dd/MM/yyyy` |

**Tombol:**
- **Simpan** -- Menyimpan konfigurasi parser baru

---

## Import Mutasi Bank

### Halaman Import

**Navigasi:** Sidebar > **Rekonsiliasi Bank** > **Import Mutasi**

**URL:** `/bank-recon/import`

**Field:**

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Rekening Bank | Ya | Dropdown | Pilih rekening bank yang terdaftar (contoh: BCA - 1234567890) |
| Parser Config | Ya | Dropdown | Pilih parser sesuai bank |
| Periode Mulai | Ya | Date | Tanggal awal mutasi, format dd/MM/yyyy |
| Periode Akhir | Ya | Date | Tanggal akhir mutasi, format dd/MM/yyyy |
| File CSV | Ya | File upload | File CSV yang didownload dari internet banking |

**Tombol:**
- **Import** -- Memproses file CSV dan membuat record mutasi bank

**Persyaratan file CSV:**
- Format CSV dengan header
- Encoding UTF-8
- Kolom sesuai parser yang dipilih
- Pastikan format tanggal sesuai konfigurasi parser

### Daftar Mutasi

**Navigasi:** Sidebar > **Rekonsiliasi Bank** > **Daftar Mutasi**

**URL:** `/bank-recon/statements`

Tabel menampilkan semua mutasi yang sudah di-import. Setiap baris menampilkan:
- Bank
- Periode (tanggal mulai -- akhir)
- Jumlah item transaksi
- Tanggal import

Klik baris untuk melihat detail.

### Detail Mutasi

**URL:** `/bank-recon/statements/{id}`

Menampilkan:
- **Bank:** Nama bank
- **Periode:** Tanggal mulai -- akhir
- **Total Item:** Jumlah transaksi dalam mutasi

**Tabel item mutasi:**

| Kolom | Keterangan |
|-------|------------|
| Tanggal | Tanggal transaksi |
| Keterangan | Deskripsi transaksi dari bank |
| Debit | Nominal keluar |
| Kredit | Nominal masuk |
| Saldo | Saldo setelah transaksi |

---

## Proses Rekonsiliasi

### Membuat Rekonsiliasi Baru

**Navigasi:** Sidebar > **Rekonsiliasi Bank** > **Rekonsiliasi Baru**

**URL:** `/bank-recon/reconciliations/new`

**Field:**

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Mutasi Bank | Ya | Dropdown | Pilih mutasi yang akan direkonsiliasi |

**Tombol:**
- **Buat Rekonsiliasi** -- Membuat rekonsiliasi baru dengan status IN_PROGRESS

### Halaman Detail Rekonsiliasi

**URL:** `/bank-recon/reconciliations/{id}`

**Ringkasan:**

| Informasi | Keterangan |
|-----------|------------|
| Status | `IN_PROGRESS` atau `COMPLETED` |
| Saldo Bank | Saldo akhir menurut rekening koran bank |
| Saldo Buku | Saldo akhir menurut pembukuan (general ledger) |
| Item Cocok | Jumlah transaksi yang sudah berhasil dicocokkan |
| Selisih | Perbedaan antara saldo bank dan saldo buku |

**Tombol:**
- **Auto-Match** -- Menjalankan pencocokan otomatis 3 tahap
- **Laporan** -- Membuka halaman laporan rekonsiliasi
- **Complete** -- Menandai rekonsiliasi selesai (hanya jika semua item sudah diproses)

### Auto-Match (Pencocokan Otomatis)

Klik tombol **Auto-Match** di halaman detail rekonsiliasi. Sistem menjalankan pencocokan 3 tahap:

| Tahap | Metode | Kriteria Pencocokan |
|-------|--------|---------------------|
| 1 | Exact Match | Tanggal identik DAN nominal identik |
| 2 | Fuzzy Date | Nominal identik, selisih tanggal maksimal 3 hari |
| 3 | Keyword Match | Kata kunci keterangan bank cocok dengan keterangan buku |

Setelah auto-match, setiap item bank menampilkan status:
- **Matched** -- Sudah dicocokkan dengan transaksi buku
- **Bank Only** -- Hanya ada di bank, tidak ada di buku
- **Unmatched** -- Belum diproses

### Manual Match

Untuk item yang tidak ter-match otomatis:

1. Klik item bank yang belum cocok
2. Sistem menampilkan daftar transaksi buku yang tersedia
3. Pilih transaksi buku yang sesuai
4. Klik **Konfirmasi** untuk mencocokkan

### Mark Bank-Only / Book-Only

Untuk item yang memang hanya ada di satu sisi:

- **Bank Only:** Klik tombol **Bank Only** pada item bank yang tidak ada padanannya di buku. Contoh: biaya administrasi bank, bunga deposito.
- **Book Only:** Item buku yang tidak ada padanannya di bank. Contoh: cek beredar yang belum dicairkan.

Item yang ditandai Bank Only biasanya perlu jurnal penyesuaian di buku besar.

---

## Laporan Rekonsiliasi

**Navigasi:** Halaman detail rekonsiliasi > tombol **Laporan**

**URL:** `/bank-recon/reconciliations/{id}/report`

**Isi laporan:**

| Bagian | Keterangan |
|--------|------------|
| Header | Periode, rekening bank, tanggal rekonsiliasi |
| Ringkasan Saldo | Saldo bank vs saldo buku |
| Item Cocok | Daftar transaksi yang berhasil dicocokkan (bank + buku) |
| Item Bank Only | Transaksi bank tanpa pasangan di buku |
| Item Book Only | Transaksi buku tanpa pasangan di bank |
| Selisih | Total selisih yang perlu ditindaklanjuti |

**Tombol:**
- **Print** -- Membuka versi cetak laporan

---

## Hak Akses

| Permission | Keterangan |
|-----------|------------|
| `BANK_RECON_VIEW` | Melihat daftar mutasi dan rekonsiliasi |
| `BANK_RECON_IMPORT` | Import file CSV mutasi bank |
| `BANK_RECON_MATCH` | Melakukan pencocokan (auto-match dan manual match) |
| `BANK_RECON_COMPLETE` | Menandai rekonsiliasi selesai |
| `BANK_RECON_CONFIG` | Mengelola konfigurasi parser |

---

Selanjutnya: [Faktur & Tagihan (Invoicing)](02-invoicing.md)
