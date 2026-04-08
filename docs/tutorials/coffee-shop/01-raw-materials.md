# Pembelian Bahan Baku

Bahan baku kedai kopi harus dibeli rutin setiap bulan. Ada 2 cara pembayaran: tunai (langsung bayar) dan kredit (hutang ke supplier).

## Bahan Baku Utama

| Bahan | Rata-rata/Bulan | Supplier |
|-------|----------------|----------|
| Biji kopi arabica | 6,5 - 8 juta | Supplier Kopi |
| Susu segar | 3,5 - 4,5 juta | Distributor Susu |
| Cup dan packaging | 1,2 juta | Toko Packaging |

Total pembelian bahan baku sepanjang 2025: **Rp 145.300.000** (tercatat di akun 1.1.20 Persediaan Bahan Baku).

## Pembelian Tunai

Mayoritas pembelian bahan baku dibayar tunai saat barang diterima.

**Template:** Pembelian Bahan Baku - Tunai

**Contoh:** Pembelian biji kopi arabica Januari, Rp 6.500.000

| Field | Isi |
|-------|-----|
| Template | Pembelian Bahan Baku - Tunai |
| Tanggal | 2025-01-05 |
| Jumlah | 6.500.000 |
| Deskripsi | Pembelian biji kopi arabica Januari |
| Referensi | INV-KNJ-2025-01 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.20 Persediaan Bahan Baku | 6.500.000 | |
| 1.1.01 Kas | | 6.500.000 |

Persediaan bertambah (debit), kas berkurang (kredit). Bahan baku masuk gudang, uang keluar dari kasir.

![Form pembelian bahan baku tunai](screenshots/tutorials/coffee-shop/tx-form-pembelian-bahan-baku-tunai.png)

Untuk memahami konsep debit dan kredit, lihat [Debit dan Kredit](../common/03-debit-credit.md).

## Pembelian Kredit

Untuk pembelian dalam jumlah besar atau supplier yang memberikan tempo pembayaran, gunakan pembelian kredit.

**Template:** Pembelian Bahan Baku - Kredit

**Contoh:** Pembelian biji kopi spesial kredit Q1, Rp 10.000.000

| Field | Isi |
|-------|-----|
| Template | Pembelian Bahan Baku - Kredit |
| Tanggal | 2025-03-05 |
| Jumlah | 10.000.000 |
| Deskripsi | Pembelian biji kopi spesial kredit Q1 |
| Referensi | INV-KNJ-2025-Q1 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.20 Persediaan Bahan Baku | 10.000.000 | |
| 2.1.02 Hutang Supplier | | 10.000.000 |

Persediaan tetap bertambah, tapi yang berkurang bukan kas — melainkan timbul hutang ke supplier. Hutang ini harus dilunasi sesuai kesepakatan.

![Form pembelian bahan baku kredit](screenshots/tutorials/coffee-shop/tx-form-pembelian-bahan-baku-kredit.png)

Di akhir tahun 2025, Kedai Kopi Nusantara masih memiliki **Hutang Supplier Rp 10.000.000** (pembelian kredit Q1 yang belum dilunasi).

## Perbedaan Tunai vs Kredit

| | Tunai | Kredit |
|---|-------|--------|
| Kas berkurang | Ya, langsung | Tidak |
| Hutang timbul | Tidak | Ya |
| Persediaan bertambah | Ya | Ya |
| Cocok untuk | Pembelian rutin kecil | Pembelian besar / supplier tetap |

## Jadwal Pembelian 2025

| Bulan | Biji Kopi | Susu | Lainnya | Total |
|-------|----------|------|---------|-------|
| Jan | 6.500.000 | 3.500.000 | 1.200.000 | 11.200.000 |
| Feb | 7.000.000 | 3.800.000 | — | 10.800.000 |
| Mar | 7.500.000 + 10.000.000 (kredit) | 4.000.000 | — | 21.500.000 |
| Apr | 7.000.000 | 3.800.000 | — | 10.800.000 |
| Mei | 7.200.000 | 3.900.000 | — | 11.100.000 |
| Jun | 7.500.000 | 4.000.000 | — | 11.500.000 |
| Jul | 7.000.000 | 3.800.000 | — | 10.800.000 |
| Agu | 7.500.000 | 4.000.000 | — | 11.500.000 |
| Sep | 7.200.000 | 3.900.000 | — | 11.100.000 |
| Okt | 7.000.000 | 3.800.000 | — | 10.800.000 |
| Nov | 7.500.000 | 4.200.000 | — | 11.700.000 |
| Des | 8.000.000 | 4.500.000 | — | 12.500.000 |
| **Total** | | | | **145.300.000** |

Desember lebih besar karena peak season (liburan dan tahun baru).

## Tips

- **Catat per nota pembelian** — jangan gabungkan pembelian kopi dan susu dalam 1 transaksi, karena supplier berbeda dan referensi nota berbeda
- **Simpan nota** — nomor referensi (INV-KNJ-2025-01, dll) harus cocok dengan nota fisik dari supplier
- **Pantau persediaan** — jika bahan baku cepat habis sebelum akhir bulan, bisa jadi ada waste yang perlu dikontrol

## Langkah Selanjutnya

- [Penjualan Tunai + COGS](02-daily-sales.md) — mencatat penjualan dan HPP
