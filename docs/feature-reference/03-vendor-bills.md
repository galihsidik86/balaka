# Tagihan Vendor (Bills)

Referensi layar fitur tagihan vendor: pembuatan tagihan, alur kerja status, pencatatan pembayaran, laporan umur hutang, dan laporan vendor.

---

## Alur Kerja Tagihan

```
DRAFT --> APPROVED --> PARTIAL --> PAID
                        ^           ^
                    (sebagian)   (lunas)
```

| Status | Keterangan |
|--------|------------|
| DRAFT | Baru dibuat, menunggu approval |
| APPROVED | Disetujui, menunggu pembayaran |
| PARTIAL | Sebagian dibayar |
| PAID | Lunas |
| OVERDUE | Melewati tanggal jatuh tempo (status otomatis) |

---

## Daftar Tagihan

**Navigasi:** Sidebar > **Pembelian** > **Tagihan**

**URL:** `/bills`

Tabel menampilkan semua tagihan vendor. Setiap baris menampilkan:
- Nomor tagihan
- Vendor
- Tanggal tagihan
- Tanggal jatuh tempo
- Total
- Status (badge berwarna)

---

## Membuat Tagihan

**Navigasi:** Tagihan > **Tagihan Baru**

**URL:** `/bills/new`

### Field Tagihan

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Vendor | Ya | Dropdown | Pilih dari daftar vendor yang terdaftar |
| Tanggal Tagihan | Ya | Date | Tanggal penerbitan oleh vendor |
| Tanggal Jatuh Tempo | Ya | Date | Batas waktu pembayaran |
| Nomor Referensi Vendor | Tidak | Text | Nomor tagihan dari sisi vendor |

### Line Item

Tambahkan satu atau lebih line item:

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Deskripsi | Ya | Text | Keterangan barang atau jasa |
| Quantity | Ya | Number | Jumlah unit |
| Harga Satuan | Ya | Number (Rp) | Harga per unit |
| Total | -- | Otomatis | Dihitung: quantity x harga satuan |

**Tombol:**
- **Tambah Item** -- Menambah baris line item baru
- **Hapus** -- Menghapus baris line item
- **Simpan** -- Menyimpan tagihan dengan status DRAFT

---

## Detail Tagihan

**URL:** `/bills/{id}`

Menampilkan informasi tagihan lengkap:
- Nomor tagihan, vendor, tanggal, jatuh tempo, referensi vendor
- Status (badge berwarna)
- Tabel line item
- Total tagihan
- Sisa hutang (balance due)

### Tombol Aksi (tergantung status)

| Tombol | Tersedia di Status | Hasil |
|--------|--------------------|-------|
| Edit | DRAFT | Membuka formulir edit |
| Setujui | DRAFT | Mengubah status ke APPROVED |
| Catat Pembayaran | APPROVED, PARTIAL, OVERDUE | Membuka formulir pembayaran |

Tagihan yang sudah berstatus APPROVED tidak bisa diedit.

---

## Mencatat Pembayaran

**Navigasi:** Detail tagihan > tombol **Catat Pembayaran**

### Field Pembayaran

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Tanggal Pembayaran | Ya | Date | Tanggal pembayaran dilakukan |
| Jumlah | Ya | Number (Rp) | Nominal pembayaran (bisa kurang dari sisa hutang) |
| Metode Pembayaran | Ya | Dropdown | Transfer, Cash, Cek, Kartu Kredit, E-Wallet, Lainnya |
| Nomor Referensi | Ya | Text | Nomor bukti transfer atau kuitansi |
| Catatan | Tidak | Text | Keterangan tambahan |

**Tombol:**
- **Simpan Pembayaran** -- Mencatat pembayaran

### Perilaku Status

- Jika jumlah pembayaran < sisa hutang: status berubah ke **PARTIAL**
- Jika total semua pembayaran = total tagihan: status berubah ke **PAID**

### Validasi

- Jumlah pembayaran tidak boleh melebihi sisa hutang
- Total pembayaran + pembayaran baru <= total tagihan
- Pembayaran hanya bisa dicatat pada tagihan berstatus APPROVED, PARTIAL, atau OVERDUE

### Riwayat Pembayaran

Ditampilkan di halaman detail tagihan sebagai tabel:

| Kolom | Keterangan |
|-------|------------|
| Tanggal | Tanggal pembayaran |
| Jumlah | Nominal pembayaran |
| Metode | Metode pembayaran |
| Referensi | Nomor bukti transfer/kuitansi |

---

## Laporan Umur Hutang (Aging Payables)

**Navigasi:** Sidebar > **Laporan** > **Umur Hutang**

**URL:** `/reports/aging-payables`

### Filter

| Field | Keterangan |
|-------|------------|
| Tanggal Acuan (As-of Date) | Tanggal referensi untuk menghitung umur hutang |

### Bucket Aging

| Bucket | Keterangan |
|--------|------------|
| Belum Jatuh Tempo | Tagihan yang belum melewati due date |
| 1-30 hari | Terlambat 1-30 hari |
| 31-60 hari | Terlambat 31-60 hari |
| 61-90 hari | Terlambat 61-90 hari |
| > 90 hari | Terlambat lebih dari 90 hari |

### Isi Laporan

- **Ringkasan per bucket:** Total hutang di setiap kategori umur
- **Tabel per vendor:** Rincian saldo per vendor di setiap bucket
- Setelah pembayaran sebagian, saldo berkurang
- Setelah tagihan lunas, vendor hilang dari laporan aging

---

## Laporan Vendor (Vendor Statement)

**Navigasi:** Detail vendor > **Lihat Laporan**

**URL:** `/reports/vendor-statement`

### Filter

| Field | Keterangan |
|-------|------------|
| Vendor | Pilih vendor |
| Tanggal Mulai | Awal periode |
| Tanggal Akhir | Akhir periode |

### Isi Laporan

| Kolom | Keterangan |
|-------|------------|
| Tanggal | Tanggal transaksi |
| Tipe | Bill atau Pembayaran |
| No. Referensi | Nomor tagihan atau nomor pembayaran |
| Keterangan | Deskripsi |
| Tagihan | Jumlah tagihan |
| Pembayaran | Jumlah pembayaran |
| Saldo Berjalan | Running balance hutang |

Informasi tambahan:
- **Saldo Awal:** Total hutang sebelum periode yang dipilih
- **Saldo Akhir:** Posisi hutang di akhir periode

**Tombol:**
- **Cetak** -- Membuka versi cetak (format A4 landscape)

---

## API Tagihan Vendor

Tagihan vendor juga bisa dikelola via API (untuk integrasi AI):

| Endpoint | Scope | Keterangan |
|----------|-------|------------|
| `GET /api/bills` | `bills:read` | Daftar tagihan |
| `GET /api/bills/{id}` | `bills:read` | Detail tagihan |
| `POST /api/bills` | `bills:create` | Buat tagihan baru |
| `POST /api/bills/{id}/approve` | `bills:approve` | Approve tagihan |
| `POST /api/bills/{id}/payments` | `bills:approve` | Catat pembayaran |

---

Selanjutnya: [Transaksi Berulang](04-recurring-transactions.md)
