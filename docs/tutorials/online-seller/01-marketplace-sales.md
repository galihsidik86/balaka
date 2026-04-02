# Penjualan Marketplace

Toko Gadget Sejahtera menjual di 3 marketplace, masing-masing dengan potongan admin berbeda. Setiap penjualan menggunakan template DETAILED dengan 2 variabel: `grossSales` (harga jual bruto) dan `adminFee` (potongan marketplace).

## Konsep: Apa yang Terjadi Saat Penjualan

Ketika pembeli membayar di marketplace, uang tidak langsung masuk ke rekening bank Anda. Alurnya:

1. Pembeli bayar → marketplace terima uang
2. Marketplace potong admin fee
3. Sisa masuk ke **Saldo Marketplace** (masih di marketplace)
4. Anda harus **withdraw** untuk memindahkan ke Bank BCA

Maka perlu 2 transaksi terpisah: penjualan (masuk Saldo Marketplace) dan withdraw (Saldo Marketplace → Bank BCA).

## Template per Marketplace

### Penjualan Tokopedia (Admin 5%)

**Template:** Penjualan Tokopedia

| Field | Isi |
|-------|-----|
| Template | Penjualan Tokopedia |
| Tanggal | 2025-01-08 |
| var_grossSales | 25.000.000 |
| var_adminFee | 1.250.000 |
| Deskripsi | Penjualan Tokopedia minggu 1 |
| Referensi | ORD-TOKPED-0101 |

**Catatan:** Amount diisi 0 karena template DETAILED menghitung sendiri dari variabel.

Jurnal yang dihasilkan:

| Akun | Debit | Kredit | Keterangan |
|------|-------|--------|-----------|
| 1.1.03 Saldo Tokopedia | 23.750.000 | | grossSales - adminFee |
| 5.2.01 Biaya Admin Tokopedia | 1.250.000 | | adminFee (5%) |
| 4.1.01 Penjualan Tokopedia | | 25.000.000 | grossSales |

Pendapatan dicatat penuh 25 juta. Potongan admin 1,25 juta dicatat sebagai beban terpisah. Saldo Tokopedia bertambah 23,75 juta (netto setelah potongan).

### Penjualan Shopee (Admin 6%)

**Template:** Penjualan Shopee

| Field | Isi |
|-------|-----|
| Template | Penjualan Shopee |
| Tanggal | 2025-01-15 |
| var_grossSales | 18.000.000 |
| var_adminFee | 1.080.000 |
| Deskripsi | Penjualan Shopee minggu 2 |
| Referensi | ORD-SHOPEE-0102 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit | Keterangan |
|------|-------|--------|-----------|
| 1.1.04 Saldo Shopee | 16.920.000 | | grossSales - adminFee |
| 5.2.02 Biaya Admin Shopee | 1.080.000 | | adminFee (6%) |
| 4.1.02 Penjualan Shopee | | 18.000.000 | grossSales |

Shopee memotong admin fee 6% — lebih tinggi dari Tokopedia. Di penjualan 18 juta, selisihnya 180rb lebih besar dibanding kalau di Tokopedia.

### Penjualan TikTok Shop (Admin 4%)

**Template:** Penjualan TikTok Shop

| Field | Isi |
|-------|-----|
| Template | Penjualan TikTok Shop |
| Tanggal | 2025-01-22 |
| var_grossSales | 12.000.000 |
| var_adminFee | 480.000 |
| Deskripsi | Penjualan TikTok minggu 3 |
| Referensi | ORD-TIKTOK-0103 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit | Keterangan |
|------|-------|--------|-----------|
| 1.1.06 Saldo TikTok Shop | 11.520.000 | | grossSales - adminFee |
| 5.2.05 Biaya Admin TikTok Shop | 480.000 | | adminFee (4%) |
| 4.1.05 Penjualan TikTok Shop | | 12.000.000 | grossSales |

TikTok Shop punya admin fee terendah (4%). Dari 12 juta, hanya 480rb yang dipotong.

## Perbandingan Admin Fee

| Marketplace | Admin Fee | Dari 10 Juta | Yang Anda Terima |
|------------|-----------|-------------|-----------------|
| Tokopedia | 5% | 500.000 | 9.500.000 |
| Shopee | 6% | 600.000 | 9.400.000 |
| TikTok Shop | 4% | 400.000 | 9.600.000 |

## Menghitung adminFee

Anda perlu menghitung sendiri adminFee berdasarkan rate marketplace:

- **Tokopedia:** grossSales x 5% → contoh: 25.000.000 x 5% = 1.250.000
- **Shopee:** grossSales x 6% → contoh: 18.000.000 x 6% = 1.080.000
- **TikTok Shop:** grossSales x 4% → contoh: 12.000.000 x 4% = 480.000

Angka ini bisa dilihat di laporan settlement masing-masing marketplace.

## Jadwal Penjualan 2025

| Bulan | Tokopedia | Shopee | TikTok Shop | Total |
|-------|-----------|--------|------------|-------|
| Jan | 25.000.000 | 18.000.000 | 12.000.000 | 55.000.000 |
| Feb | 22.000.000 | 20.000.000 | 15.000.000 | 57.000.000 |
| Mar | 28.000.000 | 22.000.000 | 18.000.000 | 68.000.000 |
| Apr | 20.000.000 | 16.000.000 | 14.000.000 | 50.000.000 |
| Mei | 24.000.000 | 19.000.000 | 16.000.000 | 59.000.000 |
| Jun | 26.000.000 | 21.000.000 | 17.000.000 | 64.000.000 |
| Jul | 23.000.000 | 17.000.000 | — | 40.000.000 |
| Agu | 27.000.000 | 21.000.000 | 15.000.000 | 63.000.000 |
| Sep | 25.000.000 | 19.000.000 | 14.000.000 | 58.000.000 |
| Okt | 35.000.000 | 28.000.000 | 20.000.000 | 83.000.000 |
| Nov | 40.000.000 | 32.000.000 | 25.000.000 | 97.000.000 |
| Des | 45.000.000 | 35.000.000 | 28.000.000 | 108.000.000 |
| **Total** | **340.000.000** | **268.000.000** | **194.000.000** | **802.000.000** |

Musim promo (10.10, 11.11, 12.12) mendongkrak penjualan signifikan di Q4.

## Total Admin Fee 2025

| Marketplace | Penjualan | Rate | Total Admin Fee |
|------------|-----------|------|----------------|
| Tokopedia | 340.000.000 | 5% | 17.000.000 |
| Shopee | 268.000.000 | 6% | 16.080.000 |
| TikTok Shop | 194.000.000 | 4% | 7.760.000 |
| **Total** | **802.000.000** | | **40.840.000** |

## Tips

- **Catat per batch** — tidak perlu catat per pesanan. Kumpulkan penjualan per minggu atau per periode settlement
- **Cocokkan dengan settlement** — gunakan laporan settlement marketplace sebagai referensi jumlah grossSales dan adminFee
- **Referensi yang konsisten** — gunakan format ORD-TOKPED-BBMM (bulan-minggu) untuk pelacakan

## Langkah Selanjutnya

- [Withdraw Saldo](02-withdrawals.md) — tarik saldo dari marketplace ke rekening bank
