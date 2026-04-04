# Payroll Bulanan

Setiap bulan, jalankan payroll untuk 5 karyawan. Sistem menghitung BPJS dan PPh 21 otomatis.

Untuk konsep payroll, lihat [Panduan Payroll](../common/06-payroll.md).

## Konfigurasi PT Solusi Digital Nusantara

| Parameter | Nilai |
|-----------|-------|
| Base Salary | 15.000.000 |
| JKK Risk Class | 1 (jasa/IT) |
| Jumlah Karyawan | 5 |

## Langkah Bulanan

### 1. Buat Payroll Run

Buka **Payroll → Buat Baru**:
- Periode: `2025-01`
- Base Salary: `15000000`
- JKK Risk Class: `1`

Klik **Buat & Kalkulasi**.

![Form pembuatan payroll baru](screenshots/tutorials/it-service/payroll-form.png)

### 2. Review Hasil

Sistem menampilkan:

| Item | Jan-Nov (per bulan) | Desember |
|------|-------------------|----------|
| Total Gross | 75.000.000 | 75.000.000 |
| Total Employee BPJS | 2.602.115 | 2.602.115 |
| Total PPh 21 | 3.600.000 | 16.350.000 |
| Total Deductions | 6.202.115 | 18.952.115 |
| Total Net Pay | 68.797.885 | 56.047.885 |
| Total Company BPJS | 6.584.230 | 6.584.230 |

Desember memiliki PPh 21 lebih besar karena **rekonsiliasi tahunan** — PPh 21 dihitung ulang dengan tarif progresif dan selisih dari TER bulanan disesuaikan.

![Hasil kalkulasi payroll](screenshots/tutorials/it-service/payroll-calculated.png)

### 3. Approve & Post

- Klik **Approve** → review final
- Klik **Post ke Jurnal** → jurnal "Post Gaji Bulanan" otomatis dibuat

### 4. Bayar Gaji

Setelah payroll diposting, buat transaksi:
- **Bayar Hutang Gaji** — jumlah = Total Net Pay
- **Bayar Hutang BPJS** — jumlah = Total BPJS (karyawan + perusahaan)

### 5. Setor PPh 21

Bulan berikutnya (sebelum tanggal 10):
- **Setor PPh 21** — jumlah = Total PPh 21 dari payroll bulan lalu

## Contoh Januari 2025

```
Payroll Jan:
  Gross: 75.000.000 (5 × 15jt)
  PPh 21: 3.600.000
  Net Pay: 68.797.885

Setelah post payroll:
  ✅ Beban Gaji       (D) 75.000.000
  ✅ Beban BPJS Kes   (D) 2.400.000
  ✅ Beban BPJS TK    (D) 4.184.230
  ✅ Hutang Gaji      (C) 68.797.885
  ✅ Hutang BPJS      (C) 9.186.345
  ✅ Hutang PPh 21    (C) 3.600.000

Bayar Hutang Gaji:
  ✅ Hutang Gaji      (D) 68.797.885
  ✅ Bank BCA         (C) 68.797.885

Bayar Hutang BPJS:
  ✅ Hutang BPJS      (D) 9.186.345
  ✅ Bank BCA         (C) 9.186.345

Setor PPh 21 (Feb, tgl 10):
  ✅ Hutang PPh 21    (D) 3.600.000
  ✅ Bank BCA         (C) 3.600.000
```

Setelah semua: Hutang Gaji = 0, Hutang BPJS = 0. Hutang PPh 21 = 3.600.000 (akan disetor bulan depan).

## PPh 21 per Karyawan

| Karyawan | PTKP | Kategori TER | PPh 21/bulan (Jan-Nov) |
|----------|------|-------------|----------------------|
| Ahmad Fauzi | K/2 | B | 675.000 |
| Sari Wulandari | TK/0 | A | 750.000 |
| Riko Pratama | K/1 | B | 675.000 |
| Maya Anggraini | TK/0 | A | 750.000 |
| Dian Kusuma | K/0 | A | 750.000 |
| **Total** | | | **3.600.000** |

## Langkah Selanjutnya

- [Aset Tetap](05-fixed-assets.md) — beli laptop dan server
