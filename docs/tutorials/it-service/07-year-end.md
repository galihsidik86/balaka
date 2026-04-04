# Tutup Tahun & SPT

Setelah 12 bulan, waktunya menutup tahun buku 2025, membuat rekonsiliasi fiskal, dan menyiapkan data SPT Tahunan.

Untuk konsep umum, lihat [Panduan Tutup Tahun](../common/12-year-end-closing.md) dan [Coretax Export](../common/13-coretax-export.md).

## Langkah 1: Pastikan Semua Bulan Sudah Ditutup

Buka **Periode Fiskal** dan pastikan 12 bulan 2025 berstatus **MONTH_CLOSED**.

## Langkah 2: Review Laba Rugi

Buka **Laporan → Laba Rugi** (periode: 1 Jan – 31 Des 2025):

```
PENDAPATAN
  Pendapatan Jasa                 2.110.000.000
                                  ─────────────
  Total Pendapatan                2.110.000.000

BEBAN USAHA
  Beban Gaji                       900.000.000
  Beban BPJS Kesehatan               28.800.000
  Beban BPJS Ketenagakerjaan          50.210.760
  Beban Sewa                        180.000.000
  Beban Cloud & Server                66.000.000
  Beban Telekomunikasi                30.000.000
  Beban Penyusutan                    15.625.000
  Beban Software & Lisensi             9.900.000
  Beban Operasional Lainnya           35.100.000
                                  ─────────────
  Total Beban Usaha               1.315.635.760

BEBAN LUAR USAHA
  Beban Admin Bank                       180.000
                                  ─────────────

LABA BERSIH                         794.184.240
```

## Langkah 3: Rekonsiliasi Fiskal

Buka **Laporan → Rekonsiliasi Fiskal** (tahun: 2025).

![Laporan rekonsiliasi fiskal](screenshots/tutorials/it-service/report-rekonsiliasi-fiskal.png)

Tambahkan koreksi fiskal:

| Uraian | Kategori | Arah | Jumlah |
|--------|----------|------|--------|
| Beban entertainment tanpa daftar nominatif | Beda Tetap | Positif | 5.000.000 |
| Sumbangan non-deductible | Beda Tetap | Positif | 2.000.000 |
| Selisih penyusutan komersial vs fiskal | Beda Waktu | Positif | 3.000.000 |

Hasil rekonsiliasi:

```
Laba Komersial                      794.184.240
+ Koreksi Positif                    10.000.000
− Koreksi Negatif                             0
                                  ─────────────
Penghasilan Kena Pajak (PKP)        804.184.240
```

## Langkah 4: PPh Badan

Dari PKP 804.184.240:

| Komponen | Nilai |
|----------|-------|
| PKP (dibulatkan ribuan) | 804.184.000 |
| PPh Badan Terutang | 88.460.240 |
| Kredit Pajak PPh 23 | (36.600.000) |
| Angsuran PPh 25 | — |
| **PPh 29 Kurang Bayar** | **51.860.240** |

Bayar PPh 29 sebelum penyampaian SPT (batas: 30 April 2026).

## Langkah 5: Jurnal Penutup

Buka **Laporan → Tutup Buku Tahun** dan klik **Eksekusi**.

Jurnal penutup menutup semua akun pendapatan dan beban:
- Semua pendapatan (4.x) menjadi saldo 0
- Semua beban (5.x) menjadi saldo 0
- Selisih masuk ke **Laba Ditahan (3.2.01)**: 794.184.240

![Checklist SPT tahunan](screenshots/tutorials/it-service/report-spt-checklist.png)

## Langkah 6: Export Coretax

Buka **Laporan → SPT Tahunan** untuk melihat data yang siap diinput ke Coretax:

### Transkrip 8A
Neraca dan Laba Rugi dengan nomor field Coretax.

### Lampiran I — Rekonsiliasi Fiskal
- Pendapatan Neto: 794.184.240
- Koreksi Positif: 10.000.000 (3 item)
- PKP: 804.184.240

### Lampiran III — Kredit Pajak PPh 23
4 bukti potong dari klien (Mandiri, Telkom), total kredit: 36.600.000

### PPh Badan
- PPh Terutang: 88.460.240
- Kredit Pajak: 36.600.000
- PPh 29: 51.860.240

## Neraca Setelah Closing

```
ASET
  Bank BCA                        1.288.859.240
  Kredit Pajak PPh 23                36.600.000
  Peralatan Komputer                 70.000.000
  Akum. Penyusutan                  (15.625.000)
                                  ─────────────
  Total Aset                      1.379.834.240

KEWAJIBAN
  Hutang PPN                         69.300.000
  Hutang PPh 21                      16.350.000
                                  ─────────────
  Total Kewajiban                    85.650.000

EKUITAS
  Modal Disetor                     500.000.000
  Laba Ditahan                      794.184.240
                                  ─────────────
  Total Ekuitas                   1.294.184.240

  Total Kewajiban + Ekuitas       1.379.834.240  ✓
```

## Langkah Selanjutnya

- [Kesalahan Umum](08-common-mistakes.md) — yang perlu dihindari
