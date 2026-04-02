# Penjualan Online (GrabFood / GoFood)

Selain walk-in, kedai kopi juga menjual via platform delivery: GrabFood, GoFood, ShopeeFood. Platform ini memotong **admin fee 20%** dari total penjualan sebelum mentransfer ke rekening.

## Konsep Potongan Platform

Jika pelanggan membeli kopi senilai Rp 8.000.000 via GrabFood dalam sebulan:

```
Penjualan kotor (grossSales)   :  8.000.000
Potongan platform 20% (adminFee): (1.600.000)
                                  ──────────
Yang diterima kedai             :  6.400.000
```

Meskipun uang yang masuk hanya 6,4 juta, pendapatan yang dicatat tetap 8 juta. Selisih 1,6 juta dicatat sebagai **Beban Potongan Platform** — ini adalah biaya yang Anda bayar ke GrabFood/GoFood untuk mendapatkan pelanggan.

## Template: Penjualan Online (GrabFood/GoFood)

Template ini bertipe DETAILED dengan 2 variabel:

| Variabel | Keterangan |
|----------|-----------|
| `grossSales` | Total penjualan kotor (sebelum potongan) |
| `adminFee` | Potongan platform (20% dari grossSales) |

**Contoh:** Penjualan GrabFood Februari, gross Rp 8.000.000

| Field | Isi |
|-------|-----|
| Template | Penjualan Online (GrabFood/GoFood) |
| Tanggal | 2025-02-22 |
| Variabel `grossSales` | 8.000.000 |
| Variabel `adminFee` | 1.600.000 |
| Deskripsi | Penjualan GrabFood Februari |
| Referensi | GF-2025-02 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit | Perhitungan |
|------|-------|--------|------------|
| 1.1.02 Bank BCA | 6.400.000 | | grossSales - adminFee |
| 6.2.11 Beban Potongan Platform | 1.600.000 | | adminFee (20%) |
| 4.1.01 Pendapatan Penjualan Minuman | | 8.000.000 | grossSales |

3 akun terlibat:
1. **Bank BCA** bertambah — uang yang benar-benar diterima (setelah dipotong)
2. **Beban Potongan Platform** — biaya yang dipotong GrabFood/GoFood
3. **Pendapatan** — revenue penuh sebelum potongan

Perhatikan: uang online masuk ke **Bank BCA** (transfer dari platform), bukan ke **Kas** seperti penjualan walk-in.

## Penjualan Online Sepanjang 2025

Tidak setiap bulan ada penjualan online (tergantung volume order):

| Bulan | Gross Sales | Admin Fee (20%) | Yang Diterima |
|-------|------------|----------------|--------------|
| Feb | 8.000.000 | 1.600.000 | 6.400.000 |
| Mar | 9.000.000 | 1.800.000 | 7.200.000 |
| Apr | 7.500.000 | 1.500.000 | 6.000.000 |
| Mei | 8.500.000 | 1.700.000 | 6.800.000 |
| Jun | 9.500.000 | 1.900.000 | 7.600.000 |
| Agu | 10.000.000 | 2.000.000 | 8.000.000 |
| Sep | 8.500.000 | 1.700.000 | 6.800.000 |
| Nov | 11.000.000 | 2.200.000 | 8.800.000 |
| Des | 15.000.000 | 3.000.000 | 12.000.000 |
| **Total** | **87.000.000** | **17.400.000** | **69.600.000** |

Desember tertinggi karena banyak pesanan delivery saat liburan.

## Dampak Potongan Platform di Laporan

Di akhir tahun 2025:

| Akun | Saldo |
|------|-------|
| 4.1.01 Pendapatan Penjualan Minuman | 533.000.000 (tunai 446jt + online 87jt) |
| 6.2.11 Beban Potongan Platform | 17.400.000 |

Potongan platform adalah biaya akuisisi pelanggan. Secara efektif, dari setiap Rp 100.000 penjualan online, Anda hanya menerima Rp 80.000.

## Tips

- **Cocokkan dengan laporan platform** — cek statement bulanan dari GrabFood/GoFood untuk memastikan angka grossSales dan adminFee sesuai
- **Catat per platform** — jika Anda menggunakan GrabFood dan GoFood, bisa dicatat terpisah dengan deskripsi yang jelas (GF-2025-02 untuk GrabFood, GO-2025-02 untuk GoFood)
- **Pertimbangkan margin** — jika COGS 38% + platform fee 20% = 58%, margin online hanya 42% vs 62% walk-in. Pertimbangkan apakah volume online cukup untuk membenarkan biaya platform

## Langkah Selanjutnya

- [Produksi & BOM](04-production-bom.md) — membuat croissant dan roti dari bahan baku
