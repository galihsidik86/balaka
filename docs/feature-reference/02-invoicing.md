# Faktur (Invoice)

Referensi layar fitur faktur: pembuatan faktur, alur kerja status, pencatatan pembayaran, laporan umur piutang, dan laporan klien.

---

## Alur Kerja Faktur

```
DRAFT --> SENT --> PARTIAL --> PAID
                     ^           ^
                 (sebagian)   (lunas)
```

| Status | Keterangan |
|--------|------------|
| DRAFT | Baru dibuat, bisa diedit |
| SENT | Sudah dikirim ke klien, menunggu pembayaran |
| PARTIAL | Sebagian dibayar |
| PAID | Lunas |
| OVERDUE | Melewati tanggal jatuh tempo (status otomatis) |

---

## Daftar Faktur

**Navigasi:** Sidebar > **Proyek** > **Faktur**

**URL:** `/invoices`

Tabel menampilkan semua faktur. Setiap baris menampilkan:
- Nomor faktur
- Klien
- Tanggal faktur
- Tanggal jatuh tempo
- Total
- Status (badge berwarna)

---

## Membuat Faktur

**Navigasi:** Faktur > **Faktur Baru**

**URL:** `/invoices/new`

### Field Faktur

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Klien | Ya | Dropdown | Pilih dari daftar klien yang terdaftar |
| Tanggal Faktur | Ya | Date | Tanggal penerbitan faktur |
| Tanggal Jatuh Tempo | Ya | Date | Batas waktu pembayaran |
| Proyek | Tidak | Dropdown | Kaitkan ke proyek tertentu (opsional) |

### Line Item

Tambahkan satu atau lebih line item:

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Deskripsi | Ya | Text | Keterangan jasa atau barang |
| Quantity | Ya | Number | Jumlah unit |
| Harga Satuan | Ya | Number (Rp) | Harga per unit |
| Total | -- | Otomatis | Dihitung: quantity x harga satuan |

**Tombol:**
- **Tambah Item** -- Menambah baris line item baru
- **Hapus** -- Menghapus baris line item (ikon X di samping baris)
- **Simpan** -- Menyimpan faktur dengan status DRAFT

---

## Detail Faktur

**URL:** `/invoices/{id}`

Menampilkan informasi faktur lengkap:
- Nomor faktur, klien, tanggal, jatuh tempo
- Status (badge berwarna)
- Tabel line item (deskripsi, qty, harga satuan, total)
- Total faktur
- Sisa tagihan (balance due) -- jika ada pembayaran sebagian

### Tombol Aksi (tergantung status)

| Tombol | Tersedia di Status | Hasil |
|--------|--------------------|-------|
| Edit | DRAFT | Membuka formulir edit |
| Kirim | DRAFT | Mengubah status ke SENT |
| Catat Pembayaran | SENT, PARTIAL, OVERDUE | Membuka formulir pembayaran |

Faktur yang sudah berstatus SENT tidak bisa diedit.

---

## Mencatat Pembayaran

**Navigasi:** Detail faktur > tombol **Catat Pembayaran**

### Field Pembayaran

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Tanggal Pembayaran | Ya | Date | Tanggal dana diterima |
| Jumlah | Ya | Number (Rp) | Nominal pembayaran (bisa kurang dari sisa tagihan) |
| Metode Pembayaran | Ya | Dropdown | Transfer, Cash, Cek, Kartu Kredit, E-Wallet, Lainnya |
| Nomor Referensi | Ya | Text | Nomor bukti transfer atau kuitansi |
| Catatan | Tidak | Text | Keterangan tambahan |

**Tombol:**
- **Simpan Pembayaran** -- Mencatat pembayaran

### Perilaku Status

- Jika jumlah pembayaran < sisa tagihan: status berubah ke **PARTIAL**
- Jika total semua pembayaran = total faktur: status berubah ke **PAID**

### Validasi

- Jumlah pembayaran tidak boleh melebihi sisa tagihan
- Total pembayaran + pembayaran baru <= total faktur
- Pembayaran hanya bisa dicatat pada faktur berstatus SENT, PARTIAL, atau OVERDUE

### Riwayat Pembayaran

Ditampilkan di halaman detail faktur sebagai tabel:

| Kolom | Keterangan |
|-------|------------|
| Tanggal | Tanggal pembayaran |
| Jumlah | Nominal pembayaran |
| Metode | Metode pembayaran |
| Referensi | Nomor bukti transfer/kuitansi |

---

## Laporan Umur Piutang (Aging Receivables)

**Navigasi:** Sidebar > **Laporan** > **Umur Piutang**

**URL:** `/reports/aging-receivables`

### Filter

| Field | Keterangan |
|-------|------------|
| Tanggal Acuan (As-of Date) | Tanggal referensi untuk menghitung umur piutang |

### Bucket Aging

| Bucket | Keterangan |
|--------|------------|
| Belum Jatuh Tempo | Faktur yang belum melewati due date |
| 1-30 hari | Terlambat 1-30 hari |
| 31-60 hari | Terlambat 31-60 hari |
| 61-90 hari | Terlambat 61-90 hari |
| > 90 hari | Terlambat lebih dari 90 hari |

### Isi Laporan

- **Ringkasan per bucket:** Total piutang di setiap kategori umur
- **Tabel per klien:** Rincian saldo per klien di setiap bucket
- Setelah pembayaran sebagian, saldo berkurang sesuai jumlah yang sudah dibayar
- Setelah faktur lunas, klien hilang dari laporan aging

---

## Laporan Klien (Client Statement)

**Navigasi:** Detail klien > **Lihat Laporan**, atau Sidebar > **Laporan** > **Laporan Klien**

**URL:** `/reports/client-statement`

### Filter

| Field | Keterangan |
|-------|------------|
| Klien | Pilih klien |
| Tanggal Mulai | Awal periode |
| Tanggal Akhir | Akhir periode |

### Isi Laporan

| Kolom | Keterangan |
|-------|------------|
| Tanggal | Tanggal transaksi |
| Tipe | Invoice atau Pembayaran |
| No. Referensi | Nomor faktur atau nomor pembayaran |
| Keterangan | Deskripsi |
| Invoice | Jumlah faktur |
| Pembayaran | Jumlah pembayaran |
| Saldo Berjalan | Running balance piutang |

Informasi tambahan:
- **Saldo Awal:** Total piutang sebelum periode yang dipilih
- **Saldo Akhir:** Posisi piutang di akhir periode

**Tombol:**
- **Cetak** -- Membuka versi cetak (format A4 landscape)

---

Selanjutnya: [Tagihan Vendor (Bills)](03-vendor-bills.md)
