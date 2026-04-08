# PPh Final UMKM

Toko Gadget Sejahtera adalah UMKM non-PKP, sehingga pajak penghasilannya mengikuti skema **PPh Final 0,5%** sesuai PP 55/2022.

Untuk konsep perpajakan umum, lihat [Panduan PPh](../common/08-pph.md).

## Apa Itu PPh Final UMKM?

UMKM dengan omzet di bawah Rp 4,8 miliar per tahun dikenai pajak penghasilan final sebesar **0,5% dari omzet bruto bulanan**. "Final" artinya pajak ini tidak bisa dikreditkan atau dikurangkan lagi — sekali bayar, selesai.

### Syarat Berlaku

| Kriteria | Toko Gadget Sejahtera |
|----------|----------------------|
| Omzet tahunan | < 4,8 miliar (802 juta di 2025) |
| Status PKP | Tidak (non-PKP) |
| Dasar hukum | PP 55/2022 (menggantikan PP 23/2018) |
| Tarif | 0,5% dari omzet bruto |
| Sifat | Final |

### Batas Waktu

PP 55/2022 memberikan fasilitas ini maksimal 7 tahun untuk WP Badan. Toko Gadget Sejahtera berdiri Maret 2019 — masih dalam masa fasilitas sampai 2026.

## Cara Menghitung

Rumusnya:

```
PPh Final = Omzet Bruto Bulan Lalu x 0,5%
```

Omzet bruto = total penjualan sebelum dikurangi admin fee marketplace. Ambil dari total grossSales semua marketplace bulan tersebut.

### Contoh: Januari 2025

| Marketplace | grossSales |
|------------|-----------|
| Tokopedia | 25.000.000 |
| Shopee | 18.000.000 |
| TikTok Shop | 12.000.000 |
| **Total Omzet** | **55.000.000** |

PPh Final = 55.000.000 x 0,5% = **275.000**

## Cara Mencatat

Gunakan template **Bayar PPh Final UMKM**.

PPh Final Januari dibayar di bulan Februari (sebelum tanggal 15).

| Field | Isi |
|-------|-----|
| Template | Bayar PPh Final UMKM |
| Tanggal | 2025-02-15 |
| Jumlah | 275.000 |
| Deskripsi | PPh Final 0,5% revenue Januari |
| Referensi | PPHFINAL-2025-01 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 5.9.01 Beban PPh Final | 275.000 | |
| 1.1.02 Bank BCA | | 275.000 |

PPh Final dicatat sebagai **beban** (bukan hutang yang ditunda) karena langsung dibayar.

![Form pembayaran PPh Final UMKM](screenshots/tutorials/online-seller/tx-form-bayar-pph-final-umkm.png)

## Jadwal PPh Final 2025

| Bulan Omzet | Omzet Bruto | PPh Final (0,5%) | Bulan Bayar |
|-------------|------------|-----------------|------------|
| Januari | 55.000.000 | 275.000 | Feb 15 |
| Februari | 57.000.000 | 285.000 | Mar 15 |
| Maret | 68.000.000 | 340.000 | Apr 15 |
| April | 50.000.000 | 250.000 | Mei 15 |
| Mei | 59.000.000 | 295.000 | Jun 15 |
| Juni | 64.000.000 | 320.000 | Jul 15 |
| Juli | 40.000.000 | 200.000 | Agu 15 |
| Agustus | 63.000.000 | 315.000 | Sep 15 |
| September | 58.000.000 | 290.000 | Okt 15 |
| Oktober | 83.000.000 | 415.000 | Nov 15 |
| November | 97.000.000 | 485.000 | Des 15 |
| Desember | 108.000.000 | 540.000 | Jan 2026 |
| **Total 2025** | **802.000.000** | **4.010.000** | |

**Catatan:** PPh Final yang dibayar selama tahun 2025 = Rp 3.470.000 (Januari–November). PPh Final Desember (540.000) dibayar Januari 2026.

## Perbedaan PPh Final vs PPh Badan

| | PPh Final UMKM | PPh Badan (IT Service) |
|---|---------------|----------------------|
| Tarif | 0,5% dari omzet bruto | 11%/22% dari laba kena pajak |
| Dasar perhitungan | Omzet (sebelum biaya) | Laba (setelah biaya) |
| PPN | Tidak ada (non-PKP) | Memungut PPN 11% |
| Kredit pajak | Tidak ada | PPh 23 bisa dikreditkan |
| Rekonsiliasi fiskal | Tidak perlu | Wajib |
| Pelaporan | Cukup setor bulanan | SPT Tahunan Badan |

PPh Final UMKM jauh lebih sederhana — tidak perlu menghitung laba bersih atau rekonsiliasi fiskal. Cukup hitung omzet bruto x 0,5%.

## Tips

- **Hitung dari grossSales, bukan netto** — admin fee marketplace tidak dikurangkan. Omzet = total harga jual bruto
- **Bayar sebelum tanggal 15** — telat bayar kena denda 2% per bulan
- **Pantau akumulasi omzet** — jika omzet tahunan mendekati 4,8 miliar, tahun berikutnya mungkin harus pindah ke PPh Badan biasa dan daftar PKP. Toko Gadget Sejahtera dengan omzet 802 juta masih jauh dari batas

## Langkah Selanjutnya

- [Payroll](06-payroll.md) — gaji 4 karyawan gudang dan admin
