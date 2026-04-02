# Bagan Akun (Chart of Accounts)

Bagan akun adalah daftar semua akun keuangan yang digunakan perusahaan Anda untuk mencatat transaksi. Setiap transaksi pasti melibatkan minimal dua akun.

## Jenis Akun

Ada 5 jenis akun utama:

| Jenis | Kode | Contoh | Saldo Normal |
|-------|------|--------|-------------|
| **ASET** (Asset) | 1.x | Kas, Bank, Piutang, Persediaan, Peralatan | Debit |
| **LIABILITAS** (Liability) | 2.x | Hutang Usaha, Hutang Pajak, Hutang Gaji | Kredit |
| **EKUITAS** (Equity) | 3.x | Modal, Laba Ditahan | Kredit |
| **PENDAPATAN** (Revenue) | 4.x | Pendapatan Jasa, Penjualan | Kredit |
| **BEBAN** (Expense) | 5.x | Gaji, Sewa, Listrik, Penyusutan | Debit |

**Saldo Normal** menunjukkan di sisi mana saldo akun biasanya berada:
- Aset dan Beban = **Debit** (bertambah di sisi debit)
- Liabilitas, Ekuitas, dan Pendapatan = **Kredit** (bertambah di sisi kredit)

## Hierarki Akun

Akun disusun bertingkat:

```
1         ASET (header)
├─ 1.1       Aset Lancar (header)
│  ├─ 1.1.01    Kas (detail — bisa diisi transaksi)
│  ├─ 1.1.02    Bank BCA (detail)
│  └─ 1.1.10    Piutang Usaha (detail)
├─ 1.2       Aset Tetap (header)
│  ├─ 1.2.01    Peralatan Komputer (detail)
│  └─ 1.2.02    Akum. Penyusutan (detail)
```

Hanya akun **detail** (level terbawah) yang bisa digunakan dalam transaksi. Akun **header** hanya untuk pengelompokan di laporan.

## Membaca Daftar Akun

Buka **Master Data → Bagan Akun** untuk melihat seluruh daftar akun.

Setiap akun menampilkan:
- **Kode** — nomor unik (misal 1.1.02)
- **Nama** — deskripsi akun (misal Bank BCA)
- **Jenis** — Asset/Liability/Equity/Revenue/Expense
- **Saldo Normal** — Debit atau Kredit
- **Status** — Aktif atau Nonaktif

## Akun yang Sering Digunakan

### Untuk IT Service / Jasa Konsultasi

| Kode | Nama | Kegunaan |
|------|------|---------|
| 1.1.02 | Bank BCA | Rekening utama perusahaan |
| 1.1.10 | Piutang Usaha | Tagihan yang belum dibayar klien |
| 1.1.26 | Kredit Pajak PPh 23 | PPh 23 yang dipotong oleh klien |
| 2.1.03 | Hutang PPN | PPN yang belum disetor ke negara |
| 2.1.10 | Hutang Gaji | Gaji yang belum ditransfer ke karyawan |
| 3.1.01 | Modal Disetor | Modal dari pemilik |
| 4.1.01 | Pendapatan Jasa | Pendapatan dari layanan IT |
| 5.1.01 | Beban Gaji | Biaya gaji karyawan |

### Untuk Online Seller

| Kode | Nama | Kegunaan |
|------|------|---------|
| 1.1.04 | Saldo Tokopedia | Saldo yang belum ditarik dari Tokopedia |
| 1.1.05 | Saldo Shopee | Saldo yang belum ditarik dari Shopee |
| 1.1.20 | Persediaan | Stok barang dagangan |
| 4.1.01 | Penjualan Tokopedia | Pendapatan dari Tokopedia |
| 5.2.01 | Biaya Admin Tokopedia | Potongan admin marketplace |

### Untuk Coffee Shop

| Kode | Nama | Kegunaan |
|------|------|---------|
| 1.1.01 | Kas | Uang tunai dari penjualan di kedai |
| 1.1.20 | Persediaan Bahan Baku | Kopi, susu, gula yang belum dipakai |
| 1.1.21 | Persediaan Barang Jadi | Croissant, roti yang sudah diproduksi |
| 5.1.01 | HPP Kopi | Harga pokok penjualan kopi |

## Langkah Selanjutnya

- [Debit dan Kredit](03-debit-credit.md) — memahami cara pencatatan
- [Template Jurnal](04-journal-templates.md) — template otomatis untuk setiap jenis transaksi
