# Operasional Kampus

Kampus memiliki pengeluaran rutin bulanan untuk utilitas, keamanan, kebersihan, serta pengeluaran berkala untuk pemeliharaan dan bahan praktikum.

## Pengeluaran Bulanan Tetap

| Template | Akun Beban | Rata-rata/Bulan | Keterangan |
|----------|-----------|----------------|-----------|
| Bayar Listrik Kampus | 5.2.11 | 8.000.000 - 9.000.000 | Fluktuasi: rendah saat liburan (7M), tinggi saat kuliah penuh (9M) |
| Bayar Air Kampus | 5.2.12 | 2.000.000 - 2.300.000 | PDAM |
| Bayar Internet & Telepon | 5.2.13 | 5.000.000 | Internet dedicated, tetap setiap bulan |
| Beban Keamanan | 5.2.15 | 3.000.000 | Jasa keamanan/satpam |
| Beban Kebersihan | 5.2.14 | 2.500.000 | Jasa cleaning service |
| Beban Admin Bank | 5.4.01 | 25.000 | Potongan otomatis bank |

### Pola Listrik Kampus

Listrik kampus bervariasi mengikuti kalender akademik:

| Bulan | Jumlah | Keterangan |
|-------|--------|-----------|
| Jan | 8.000.000 | Awal semester |
| Feb | 8.200.000 | Kuliah penuh |
| Mar | 8.500.000 | Kuliah + lab aktif |
| Apr | 7.800.000 | Menjelang UTS |
| May | 7.500.000 | Setelah UTS |
| Jun | 7.000.000 | Liburan semester |
| Jul | 8.500.000 | Registrasi maba |
| Aug | 9.000.000 | Kuliah penuh + maba |
| Sep | 8.800.000 | Kuliah penuh |
| Oct | 8.500.000 | Menjelang UTS |
| Nov | 8.200.000 | Setelah UTS |
| Dec | 7.500.000 | Liburan akhir tahun |
| **Total** | **97.500.000** | |

## Pengeluaran Berkala

| Template | Akun Beban | Frekuensi | Keterangan |
|----------|-----------|-----------|-----------|
| Bayar ATK | 5.2.10 | 5x/tahun | ATK dan supplies kantor |
| Bayar Bahan Praktikum | 5.1.10 | 4x/tahun | Bahan habis pakai lab |
| Pemeliharaan Lab | 5.1.11 | 2x/tahun | Service dan upgrade peralatan lab |
| Pemeliharaan Gedung | 5.2.21 | 3x/tahun | Pengecatan, perbaikan atap, dll |

### ATK

| Tanggal | Jumlah | Referensi |
|---------|--------|-----------|
| 2025-02-20 | 1.500.000 | ATK-2025-02 |
| 2025-04-05 | 1.200.000 | ATK-2025-04 |
| 2025-06-15 | 1.300.000 | ATK-2025-06 |
| 2025-08-20 | 1.500.000 | ATK-2025-08 |
| 2025-11-15 | 1.500.000 | ATK-2025-11 |
| 2025-12-15 | 1.000.000 | ATK-2025-12 |
| **Total** | **8.000.000** | |

### Bahan Praktikum

| Tanggal | Jumlah | Deskripsi |
|---------|--------|-----------|
| 2025-02-25 | 2.000.000 | Bahan praktikum lab komputer |
| 2025-05-15 | 2.500.000 | Bahan praktikum lab jaringan |
| 2025-08-25 | 3.000.000 | Bahan praktikum semester baru |
| 2025-12-10 | 2.000.000 | Bahan praktikum UAS |
| **Total** | **9.500.000** | |

![Form pembelian bahan praktikum](screenshots/tutorials/campus/tx-form-bayar-bahan-praktikum.png)

### Pemeliharaan Lab

| Tanggal | Jumlah | Deskripsi |
|---------|--------|-----------|
| 2025-05-20 | 5.000.000 | Service peralatan lab |
| 2025-10-20 | 8.000.000 | Upgrade peralatan lab |
| **Total** | **13.000.000** | |

### Pemeliharaan Gedung

| Tanggal | Jumlah | Deskripsi |
|---------|--------|-----------|
| 2025-03-31 | 10.000.000 | Pengecatan kampus Q1 |
| 2025-09-25 | 15.000.000 | Perbaikan atap kampus |
| 2025-12-31 | 8.000.000 | Pemeliharaan akhir tahun |
| **Total** | **33.000.000** | |

## Cara Mencatat

Semua template pengeluaran bekerja sama:

1. Pilih template (misal: Bayar Listrik Kampus)
2. Isi tanggal dan jumlah
3. Isi deskripsi dan referensi
4. Klik **Simpan & Posting**

Jurnal yang dihasilkan (contoh listrik):

| Akun | Debit | Kredit |
|------|-------|--------|
| 5.2.11 Beban Listrik | 8.000.000 | |
| 1.1.02 Bank BCA | | 8.000.000 |

Beban bertambah (debit), bank berkurang (kredit).

## Aset Tetap: Server Lab Komputer

Kampus membeli server lab komputer di bulan Maret:

| Field | Isi |
|-------|-----|
| Kode | AST-LAB-001 |
| Nama | Server Lab Komputer |
| Kategori | LAB |
| Tanggal Beli | 2025-03-25 |
| Harga Beli | 35.000.000 |
| Metode | Garis Lurus |
| Masa Manfaat | 48 bulan |
| Nilai Residu | 0 |

Penyusutan per bulan: 35.000.000 / 48 = **729.167/bulan**

Penyusutan dimulai April 2025, total 10 bulan di tahun 2025 (Apr-Dec, tapi dihitung mulai bulan berikutnya setelah pembelian):

| Akun | Saldo Akhir 2025 |
|------|-----------------|
| Aset Tetap | 35.000.000 |
| Akum. Penyusutan | (7.291.667) |
| Beban Penyusutan | 7.291.667 |
| **Nilai Buku** | **27.708.333** |

Untuk konsep penyusutan, lihat [Panduan Aset Tetap](../common/10-fixed-assets.md).

## Total Beban Operasional Kampus 2025

| Kategori | Jumlah/Tahun |
|----------|-------------|
| Beban Listrik | 97.500.000 |
| Beban Telepon & Internet | 60.000.000 |
| Beban Keamanan | 36.000.000 |
| Beban Pemeliharaan Gedung | 33.000.000 |
| Beban Kebersihan | 30.000.000 |
| Beban Air | 24.800.000 |
| Beban Pemeliharaan Lab | 13.000.000 |
| Beban Bahan Praktikum | 9.500.000 |
| Beban ATK | 8.000.000 |
| Beban Penyusutan | 7.291.667 |
| Beban Admin Bank | 300.000 |
| **Total Operasional** | **320.391.667** |

Ditambah beban gaji+BPJS (846,6M) dan beasiswa (40M), total beban kampus: **1.178.034.867**.

## Langkah Selanjutnya

- [Pelaporan](07-reporting.md) — laporan ke yayasan dan tutup tahun
