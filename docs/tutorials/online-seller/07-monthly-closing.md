# Tutup Bulan & Tahun

Setiap bulan, lakukan langkah-langkah berikut untuk memastikan keuangan Toko Gadget Sejahtera tercatat dengan benar.

Untuk panduan umum, lihat [Panduan Tutup Bulanan](../common/11-monthly-closing.md) dan [Panduan Tutup Tahun](../common/12-year-end-closing.md).

## Checklist Bulanan

### Minggu 1 Bulan Berikutnya

- [ ] Input semua penjualan marketplace bulan lalu (Tokopedia, Shopee, TikTok Shop)
- [ ] Input semua withdraw saldo marketplace bulan lalu
- [ ] Input pembelian barang dagangan (jika ada)

### Tanggal 10

- [ ] **Setor PPh 21** — jumlah dari payroll bulan lalu (626.250/bulan)

### Tanggal 15

- [ ] **Bayar PPh Final UMKM** — 0,5% dari omzet bruto bulan lalu

### Akhir Bulan

- [ ] Jalankan payroll bulan ini
- [ ] Bayar gaji (Bayar Hutang Gaji)
- [ ] Bayar BPJS (Bayar Hutang BPJS)
- [ ] Post penyusutan bulan ini (rak gudang: 166.667/bulan, mulai Maret)
- [ ] Catat ongkir, packing, iklan, sewa, listrik, admin bank
- [ ] **Tutup periode** bulan lalu

## Contoh: Closing Januari 2025

### Penjualan Januari (sudah dicatat)

| Marketplace | grossSales | adminFee |
|------------|-----------|---------|
| Tokopedia | 25.000.000 | 1.250.000 |
| Shopee | 18.000.000 | 1.080.000 |
| TikTok Shop | 12.000.000 | 480.000 |
| **Total** | **55.000.000** | **2.810.000** |

### Withdraw Januari (sudah dicatat)
- Tokopedia: 23.750.000
- Shopee: 16.920.000
- TikTok Shop: 11.520.000

### Pengeluaran Januari (sudah dicatat)
- Sewa Gudang: 5.000.000
- Listrik: 800.000
- Ongkir: 2.000.000
- Packing: 500.000
- Iklan: 3.000.000
- Admin Bank: 15.000

### Payroll Januari
- Post payroll → Bayar gaji 20.611.450 → Bayar BPJS 3.015.100

### Setor Pajak (Februari)
- PPh 21: 626.250 (tanggal 10 Feb)
- PPh Final UMKM: 275.000 (tanggal 15 Feb) — 0,5% dari omzet 55 juta

### Tutup Periode Januari
- Buka **Periode Fiskal** → Januari 2025 → Tutup Bulan

## Perbedaan dengan IT Service

Toko Gadget Sejahtera sebagai UMKM non-PKP punya closing yang lebih sederhana:

| Langkah | IT Service (PKP) | Online Seller (UMKM) |
|---------|-----------------|---------------------|
| Setor PPN | Ya (akhir bulan berikutnya) | **Tidak ada** — non-PKP |
| PPh Final UMKM | Tidak ada | Ya (tgl 15) |
| Rekonsiliasi PPN | Hitung PPN Keluaran - Masukan | Tidak perlu |
| PPh 23 | Klien memotong 2% | Tidak ada |

## Verifikasi Akhir Bulan

Setelah semua langkah selesai, cek:

| Akun | Seharusnya |
|------|-----------|
| Hutang Gaji (2.1.10) | 0 (sudah dibayar) |
| Hutang BPJS (2.1.13) | 0 (sudah disetor) |
| Hutang PPh 21 (2.1.20) | Hanya bulan ini (belum jatuh tempo) |
| Bank BCA (1.1.02) | Positif |
| Saldo Marketplace | Hanya sisa yang belum di-withdraw |

## Aset Tetap: Rak Gudang

Toko Gadget membeli 1 aset tetap:

| Aset | Tanggal | Harga | Masa Manfaat | Penyusutan/Bulan |
|------|---------|-------|-------------|-----------------|
| Rak Gudang Heavy Duty | 10 Mar 2025 | 8.000.000 | 48 bulan | 166.667 |

Penyusutan dimulai bulan berikutnya (April 2025) dan di-post setiap akhir bulan.

Total penyusutan 2025: 166.667 x 10 bulan (Mar–Des) = **1.666.667**

Untuk konsep penyusutan, lihat [Panduan Aset Tetap](../common/10-fixed-assets.md).

## Tutup Tahun 2025

### Langkah 1: Pastikan 12 Bulan Ditutup

Buka **Periode Fiskal** dan pastikan Januari–Desember 2025 berstatus **MONTH_CLOSED**.

### Langkah 2: Review Laba Rugi

Buka **Laporan → Laba Rugi** (periode: 1 Jan – 31 Des 2025):

```
PENDAPATAN
  Penjualan Tokopedia              340.000.000
  Penjualan Shopee                 268.000.000
  Penjualan TikTok Shop            194.000.000
  Cashback Marketplace               2.000.000
                                   ───────────
  Total Pendapatan                 804.000.000

BEBAN USAHA
  Biaya Admin Tokopedia             17.000.000
  Biaya Admin Shopee                16.080.000
  Biaya Admin TikTok Shop           7.760.000
  Biaya Iklan Marketplace           44.800.000
  Ongkir Ditanggung Penjual         29.800.000
  Biaya Packing                      7.160.000
  Beban Gaji                       264.000.000
  Beban BPJS Perusahaan             27.033.600
  Beban Sewa Gudang                 60.000.000
  Beban Listrik                     10.100.000
  Beban Penyusutan                   1.666.667
                                   ───────────
  Total Beban Usaha                485.400.267

BEBAN LUAR USAHA
  Beban Admin Bank                     180.000
  Beban PPh Final                    3.470.000
                                   ───────────

LABA BERSIH                        314.949.733
```

### Langkah 3: Jurnal Penutup

Buka **Laporan → Tutup Buku Tahun** dan klik **Eksekusi**.

Jurnal penutup menutup semua akun pendapatan dan beban:
- Semua pendapatan (4.x) menjadi saldo 0
- Semua beban (5.x) menjadi saldo 0
- Selisih masuk ke **Laba Ditahan (3.2.01)**

### Langkah 4: PPh Final UMKM — Tidak Perlu SPT Badan

Karena Toko Gadget Sejahtera menggunakan PPh Final 0,5%, tidak perlu membuat rekonsiliasi fiskal atau menghitung PPh Badan. PPh sudah dibayar setiap bulan (final).

Yang perlu dilaporkan di SPT Tahunan:
- Total omzet bruto: 802.000.000
- Total PPh Final dibayar: 4.010.000 (termasuk Desember yang dibayar Jan 2026)
- Lampiran neraca dan laba rugi

### Neraca Setelah Closing (31 Desember 2025)

```
ASET
  Bank BCA                         188.222.650
  Saldo Shopee                      31.020.000
  Persediaan Barang Dagangan       390.000.000
  Peralatan Gudang                   8.000.000
  Akum. Penyusutan                  (1.666.667)
                                   ───────────
  Total Aset                       615.575.983

KEWAJIBAN
  Hutang PPh 21                        626.250
                                   ───────────
  Total Kewajiban                      626.250

EKUITAS
  Modal Disetor                    300.000.000
  Laba Ditahan                     314.949.733
                                   ───────────
  Total Ekuitas                    614.949.733

  Total Kewajiban + Ekuitas        615.575.983
```

## Kesalahan Umum

### 1. Lupa Withdraw Saldo Marketplace

Penjualan sudah dicatat tapi saldo masih di marketplace. Uang tidak di bank — tidak bisa bayar gaji atau supplier. Lakukan withdraw rutin.

### 2. Menghitung PPh Final dari Netto

PPh Final dihitung dari **omzet bruto** (grossSales), bukan dari netto setelah dikurangi admin fee. Jika penjualan Tokopedia 25 juta, PPh Final dihitung dari 25 juta — bukan dari 23,75 juta.

### 3. Tidak Mencatat Admin Fee Marketplace

Admin fee harus dicatat sebagai beban terpisah. Jika hanya mencatat netto yang diterima sebagai pendapatan, laporan laba rugi tidak akurat — pendapatan terlihat lebih rendah dari sebenarnya.

### 4. Restock Terlambat untuk Musim Promo

Stok harus sudah siap **sebelum** promo dimulai. Lihat pola pembelian di demo data — restock besar dilakukan awal bulan (1–3 Oktober, 1 November, 1 Desember).

### 5. Hutang Gaji atau BPJS Tidak Nol

Jika Trial Balance menunjukkan Hutang Gaji masih ada saldo, Anda belum mencatat "Bayar Hutang Gaji" setelah posting payroll. Pastikan setiap bulan ada 3 transaksi: Bayar Gaji, Bayar BPJS, Setor PPh 21.

## Referensi

- [Panduan Payroll](../common/06-payroll.md)
- [Panduan PPh](../common/08-pph.md)
- [Panduan BPJS](../common/09-bpjs.md)
- [Panduan Aset Tetap](../common/10-fixed-assets.md)
- [Panduan Tutup Bulanan](../common/11-monthly-closing.md)
- [Panduan Tutup Tahun](../common/12-year-end-closing.md)
