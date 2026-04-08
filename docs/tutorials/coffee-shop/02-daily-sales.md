# Penjualan Tunai + COGS

Kedai kopi mencatat penjualan tunai secara agregat per minggu. Setiap transaksi penjualan sekaligus mencatat HPP (Harga Pokok Penjualan / COGS) — biaya bahan baku yang terpakai untuk menghasilkan minuman dan makanan yang terjual.

## Konsep COGS untuk Kedai Kopi

COGS (Cost of Goods Sold) adalah biaya bahan baku yang digunakan untuk membuat produk yang dijual. Di kedai kopi, ini mencakup biji kopi, susu, gula, es batu, cup, dan bahan lainnya.

**Rasio COGS di Kedai Kopi Nusantara: 38% dari revenue.**

Artinya dari setiap Rp 100.000 penjualan, Rp 38.000 adalah biaya bahan baku. Sisanya Rp 62.000 adalah **laba kotor** (gross margin 62%) — yang masih harus dikurangi biaya operasional (gaji, sewa, listrik, dll).

## Template: Penjualan Tunai + COGS

Template ini bertipe DETAILED — Anda mengisi 2 variabel:

| Variabel | Keterangan |
|----------|-----------|
| `revenueAmount` | Total penjualan dari POS (kasir) |
| `cogs` | HPP = 38% dari revenue |

**Contoh:** Penjualan minggu 1 Januari, revenue Rp 18.000.000

| Field | Isi |
|-------|-----|
| Template | Penjualan Tunai + COGS |
| Tanggal | 2025-01-11 |
| Variabel `revenueAmount` | 18.000.000 |
| Variabel `cogs` | 6.840.000 |
| Deskripsi | Penjualan minggu 1 Januari |
| Referensi | POS-2025-01-W1 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit | Keterangan |
|------|-------|--------|-----------|
| 1.1.01 Kas | 18.000.000 | | Uang masuk dari penjualan |
| 4.1.01 Pendapatan Penjualan Minuman | | 18.000.000 | Revenue tercatat |
| 5.1.01 HPP Kopi | 6.840.000 | | Biaya bahan baku terpakai |
| 1.1.21 Persediaan Barang Jadi | | 6.840.000 | Persediaan berkurang |

4 akun terlibat dalam 1 transaksi:
1. **Kas** bertambah — uang diterima dari pelanggan
2. **Pendapatan** bertambah — omzet tercatat
3. **HPP** bertambah — biaya bahan baku diakui sebagai beban
4. **Persediaan Barang Jadi** berkurang — stok keluar

![Form penjualan tunai dengan COGS](screenshots/tutorials/coffee-shop/tx-form-penjualan-tunai-cogs.png)

## Menghitung COGS

Angka COGS bisa dihitung dari data POS:

```
COGS = Revenue × 38%

Contoh minggu 1 Januari:
  Revenue = 18.000.000
  COGS    = 18.000.000 × 0,38 = 6.840.000
```

Rasio 38% ini dihitung dari harga pokok bahan baku per cup/porsi dibagi harga jual. Setiap kedai kopi punya rasio berbeda tergantung kualitas bahan dan harga jual.

## Agregasi Mingguan

Penjualan di kasir terjadi per transaksi (per cup/porsi), tapi pencatatan di Balaka dilakukan **per minggu**. Ini menyederhanakan pencatatan tanpa kehilangan akurasi.

Contoh Januari 2025:

| Minggu | Tanggal | Revenue | COGS (38%) |
|--------|---------|---------|-----------|
| W1 | 11 Jan | 18.000.000 | 6.840.000 |
| W2 | 18 Jan | 17.500.000 | 6.650.000 |
| W3-4 | 25 Jan | 16.000.000 | 6.080.000 |
| **Total Jan** | | **51.500.000** | **19.570.000** |

## Penjualan Tunai Sepanjang 2025

| Bulan | Revenue | COGS | Laba Kotor |
|-------|---------|------|-----------|
| Jan | 51.500.000 | 19.570.000 | 31.930.000 |
| Feb | 33.500.000 | 12.730.000 | 20.770.000 |
| Mar | 37.500.000 | 14.250.000 | 23.250.000 |
| Apr | 33.500.000 | 12.730.000 | 20.770.000 |
| Mei | 35.500.000 | 13.490.000 | 22.010.000 |
| Jun | 37.000.000 | 14.060.000 | 22.940.000 |
| Jul | 33.500.000 | 12.730.000 | 20.770.000 |
| Agu | 36.000.000 | 13.680.000 | 22.320.000 |
| Sep | 35.000.000 | 13.300.000 | 21.700.000 |
| Okt | 34.500.000 | 13.110.000 | 21.390.000 |
| Nov | 36.500.000 | 13.870.000 | 22.630.000 |
| Des | 42.000.000 | 15.960.000 | 26.040.000 |
| **Total** | **446.000.000** | **169.480.000** | **276.520.000** |

Desember tertinggi karena peak season (liburan sekolah, Natal, tahun baru).

## Laba Kotor vs Laba Bersih

Laba kotor 62% terlihat sehat, tapi ini belum memperhitungkan beban operasional:

```
Revenue                    446.000.000   (tunai saja)
- COGS                    (169.480.000)
                          ─────────────
Laba Kotor                 276.520.000   (gross margin 62%)

- Gaji + BPJS             (397.944.000)  ← ini yang besar
- Sewa                     (96.000.000)
- Listrik                  (18.250.000)
- Dll
```

Inilah mengapa kedai kopi dengan gross margin 62% masih bisa rugi — beban gaji dan sewa yang tinggi. Lihat [Pajak UMKM](07-tax-umkm.md) untuk analisis lengkapnya.

## Tips

- **Cocokkan dengan POS** — total revenue di Balaka harus sama dengan laporan Z-read mesin kasir
- **Konsistensi rasio COGS** — jika rasio berubah signifikan, periksa apakah harga bahan baku naik atau ada waste
- **Referensi mingguan** — gunakan format POS-2025-01-W1 agar mudah dilacak

## Langkah Selanjutnya

- [Penjualan Online](03-online-delivery.md) — penjualan via GrabFood/GoFood
