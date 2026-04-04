# Aset Tetap

PT Solusi Digital membeli 2 aset tetap selama 2025:

| Aset | Tanggal | Harga | Masa Manfaat | Penyusutan/Bulan |
|------|---------|-------|-------------|-----------------|
| Laptop HP EliteBook 860 | Jan 15 | 25.000.000 | 48 bulan | 520.833 |
| Server Dell PowerEdge R750 | Mar 20 | 45.000.000 | 48 bulan | 937.500 |

Untuk konsep penyusutan, lihat [Panduan Aset Tetap](../common/10-fixed-assets.md).

## Pembelian Laptop (Januari)

### Langkah 1: Daftarkan Aset

Buka **Aset Tetap → Tambah Baru**:

| Field | Isi |
|-------|-----|
| Kode | AST-LPT-001 |
| Nama | Laptop HP EliteBook 860 |
| Kategori | KOMPUTER |
| Tanggal Beli | 2025-01-15 |
| Harga Beli | 25.000.000 |
| Metode | Garis Lurus |
| Masa Manfaat | 48 bulan |
| Nilai Residu | 0 |

![Form pendaftaran aset tetap](screenshots/tutorials/it-service/asset-form.png)

### Langkah 2: Catat Pembayaran

Template **Pembelian Aset Tetap** otomatis membuat jurnal:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.2.01 Peralatan Komputer | 25.000.000 | |
| 1.1.02 Bank BCA | | 25.000.000 |

![Detail aset tetap yang sudah terdaftar](screenshots/tutorials/it-service/asset-detail.png)

## Penyusutan Bulanan

Setiap akhir bulan, buka **Aset Tetap → Penyusutan**:

1. Pilih periode
2. Klik **Generate**
3. Klik **Post** untuk setiap entry

Bulan Januari–Februari (hanya laptop):
- Penyusutan: 520.833/bulan

Mulai Maret (laptop + server):
- Laptop: 520.833
- Server: 937.500
- **Total: 1.458.333/bulan**

## Dampak di Laporan

### Trial Balance (31 Desember 2025)

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.2.01 Peralatan Komputer | 70.000.000 | |
| 1.2.02 Akum. Penyusutan | | 15.625.000 |
| 5.1.12 Beban Penyusutan | 15.625.000 | |

Nilai buku aset: 70.000.000 − 15.625.000 = **54.375.000**

### Rincian Penyusutan

| Aset | Bulan Mulai | Jumlah Bulan | Total Penyusutan |
|------|------------|-------------|-----------------|
| Laptop | Feb 2025 | 12 | 6.250.000 |
| Server | Apr 2025 | 10 | 9.375.000 |
| **Total** | | | **15.625.000** |

## Langkah Selanjutnya

- [Tutup Bulan](06-monthly-closing.md) — penyusutan sebagai bagian siklus bulanan
