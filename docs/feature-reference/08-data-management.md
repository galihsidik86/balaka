# Pengelolaan Data

Referensi layar fitur pengelolaan data: ekspor data, impor data, data subjects (GDPR/UU PDP), dan audit log.

---

## Ekspor Data

### Halaman Ekspor

**Navigasi:** Sidebar > **Pengaturan** > **Ekspor Data**

**URL:** `/settings/export`

Halaman menampilkan statistik data yang akan diekspor:
- Jumlah record per tabel (akun, template, transaksi, karyawan, dll)
- Total keseluruhan record

**Tombol:**
- **Download Ekspor** -- Mengunduh seluruh data perusahaan dalam format ZIP

### Format Ekspor

File ZIP berisi file-file CSV dengan nama bernomor urut (dependency order):

| File | Isi |
|------|-----|
| `01_company_config.csv` | Konfigurasi perusahaan |
| `02_chart_of_accounts.csv` | Chart of Accounts |
| `04_journal_templates.csv` | Template jurnal |
| `05_journal_template_lines.csv` | Baris template jurnal |
| `18_transactions.csv` | Transaksi |
| ... | (file CSV lainnya sesuai data yang ada) |

Nama file ZIP: `export-YYYYMMDD.zip` (berdasarkan tanggal download).

Data sensitif (NPWP, nomor rekening) tetap ditampilkan dalam bentuk asli untuk pengguna yang berwenang.

---

## Impor Data

### Halaman Impor

**Navigasi:** Sidebar > **Pengaturan** > **Import Data**

**URL:** `/settings/import`

**Permission:** `DATA_IMPORT`

### Field

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| File | Ya | File upload | File ZIP hasil ekspor |

### Validasi

- File tidak boleh kosong
- Format harus `.zip`
- File harus berisi CSV dengan nama yang dikenali

### Perilaku

- Import mengganti (truncate) data pada tabel yang memiliki CSV di dalam ZIP
- Tabel yang tidak memiliki CSV di dalam ZIP tidak terpengaruh
- File CSV yang hanya berisi header (tanpa data) dilewati

### Hasil

Setelah berhasil, halaman menampilkan pesan:
```
Import berhasil: X record data, Y dokumen dalam Z ms
```

Jika terjadi error, pesan error ditampilkan.

---

## Audit Log

### Halaman Audit Log

**Navigasi:** Sidebar > **Pengaturan** > **Audit Log**

**URL:** `/settings/audit-log`

**Permission:** Hanya `ADMIN`

### Filter

| Field | Keterangan |
|-------|------------|
| Tanggal (dari-sampai) | Rentang waktu log |
| User | Filter berdasarkan pengguna |
| Tipe Aktivitas | Filter berdasarkan jenis event |
| Entity | Filter berdasarkan entitas yang diakses |

### Tabel Log

| Kolom | Keterangan |
|-------|------------|
| Waktu | Timestamp event |
| User | Username yang melakukan aksi |
| Tipe | Jenis aktivitas (LOGIN, DATA_ACCESS, DATA_MODIFICATION, EXPORT, SECURITY_EVENT, API_CALL, SETTINGS_CHANGE) |
| Detail | Informasi detail event |

### Aktivitas yang Dicatat

| Aktivitas | Contoh |
|-----------|--------|
| Login | Login/logout, failed attempts |
| Data Access | View data sensitif |
| Data Modification | Create, Update, Delete |
| Export | Download report, export data |
| Security Events | Password change, role change |
| API Call | Request dari AI assistant (source, merchant, amount) |
| Settings Change | Perubahan konfigurasi, device token revoked |

### Retention

- Audit log disimpan minimal 2 tahun
- Log tidak bisa dihapus oleh pengguna biasa

---

## Data Subjects (GDPR / UU PDP)

### Halaman Data Subjects

**Navigasi:** Sidebar > **Pengaturan** > **Data Subjects**

**URL:** `/settings/data-subjects`

Halaman untuk mengelola data subject sesuai regulasi perlindungan data pribadi.

**Fitur:**
- Register data subject (karyawan, klien)
- Track consent
- Manage data processing activities

### Ekspor Data Subject (DSAR)

**Data Subject Access Request** -- Hak subjek data untuk mendapatkan salinan data pribadinya.

Langkah:
1. Buka **Pengaturan** > **Data Subjects**
2. Cari data subject
3. Klik **Export Data**
4. Sistem generate file (format JSON/PDF)
5. Download dan deliver ke subject

Batas waktu pemenuhan:
- GDPR: 30 hari
- UU PDP: 3x24 jam

### Halaman Privacy

**Navigasi:** Sidebar > **Pengaturan** > **Privacy**

**URL:** `/settings/privacy`

**Konfigurasi:**
- Data retention period
- Consent requirements
- Data masking rules

---

## Enkripsi Data

### Field-Level Encryption

Data sensitif (PII) dienkripsi menggunakan AES-256-GCM:
- NPWP karyawan
- Nomor rekening bank
- Alamat
- Nomor BPJS
- Nomor telepon

### Document Encryption

Dokumen yang diupload (struk, invoice, bukti potong) dienkripsi:
- Encryption at rest
- Unique key per document
- Backward compatible dengan dokumen lama

Data terenkripsi tidak bisa di-query langsung di database. Export data tetap menampilkan data asli untuk pengguna yang berwenang.

---

## Dokumen Transaksi

Setiap transaksi dapat memiliki dokumen pendukung (struk, invoice, bukti potong).

### Upload Dokumen

Di halaman detail transaksi, bagian **Dokumen** menampilkan daftar dokumen yang sudah diupload.

**Tombol:**
- **Upload** -- Membuka dialog file picker (mendukung PDF dan gambar)

### Daftar Dokumen

| Kolom | Keterangan |
|-------|------------|
| Nama File | Nama file asli |
| Ukuran | Ukuran file |
| Tanggal Upload | Kapan file diupload |

**Aksi per dokumen:**
- **Download** -- Mengunduh file
- **Hapus** -- Menghapus dokumen

---

Selanjutnya: [Otorisasi Perangkat API](09-api-device-auth.md)
