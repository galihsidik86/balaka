# Tutup Bulan

Setiap bulan, lakukan langkah-langkah berikut untuk memastikan keuangan PT Solusi Digital tercatat dengan benar.

Untuk panduan umum tutup bulan, lihat [Panduan Tutup Bulanan](../common/11-monthly-closing.md).

## Checklist Bulanan

### Minggu 1 Bulan Berikutnya

- [ ] Input semua transaksi pendapatan bulan lalu
- [ ] Input semua pengeluaran bulan lalu (cloud, sewa, telekom, dll)

### Tanggal 10

- [ ] **Setor PPh 21** — jumlah dari payroll bulan lalu
- [ ] **Setor PPh 23** — jika ada pembayaran jasa ke vendor bulan lalu

### Tanggal 15

- [ ] **Setor PPN** — PPN Keluaran bulan lalu dikurangi PPN Masukan

### Akhir Bulan

- [ ] Jalankan payroll bulan ini
- [ ] Bayar gaji (Bayar Hutang Gaji)
- [ ] Bayar BPJS (Bayar Hutang BPJS)
- [ ] Post penyusutan bulan ini
- [ ] Catat beban admin bank
- [ ] **Tutup periode** bulan lalu

## Contoh: Closing Januari 2025

### Pendapatan Januari (sudah dicatat)
- Mandiri: 150jt (+PPN+PPh23)
- PLN: 200jt (BUMN FP03)

### Pengeluaran Januari (sudah dicatat)
- Cloud: 5,5jt
- Software: 3,3jt
- Sewa: 15jt
- Telekom: 2,5jt
- Operasional: 3jt
- Admin Bank: 15rb

### Payroll Januari
- Post payroll → Bayar gaji 68,8jt → Bayar BPJS 9,2jt

### Penyusutan Januari
- Laptop: 520.833

### Setor Pajak (Februari)
- PPh 21: 3.600.000 (tanggal 10 Feb)
- PPN: 16.500.000 (akhir Feb) — hanya dari invoice Mandiri, PLN PPN dipungut pembeli

### Tutup Periode Januari
- Buka Periode Fiskal → Januari 2025 → Tutup Bulan

![Daftar periode fiskal](screenshots/tutorials/it-service/fiscal-period-list.png)

## PPN: Mana yang Disetor?

Tidak semua bulan ada PPN yang disetor. Bulan dimana semua pendapatan dari BUMN (FP 03), PPN nihil:

| Bulan | Pendapatan | PPN Keluaran | Setor PPN |
|-------|-----------|-------------|-----------|
| Jan | Mandiri 150jt | 16.500.000 | Ya |
| Feb | Grab 80jt + Telkom 120jt | 22.000.000 | Ya |
| Mar | Kominfo 180jt (BUMN) | 0 | Nihil |
| Apr | Mandiri 100jt | 11.000.000 | Ya |
| Jun | PLN 250jt (BUMN) | 0 | Nihil |
| Sep | Kominfo 220jt (BUMN) | 0 | Nihil |

## Verifikasi Akhir Bulan

Setelah semua langkah selesai, cek:

| Akun | Seharusnya |
|------|-----------|
| Hutang Gaji (2.1.10) | 0 (sudah dibayar) |
| Hutang BPJS (2.1.13) | 0 (sudah disetor) |
| Hutang PPh 21 (2.1.20) | Hanya bulan ini (belum jatuh tempo) |
| Hutang PPN (2.1.03) | Akumulasi yang belum disetor |
| Bank BCA (1.1.02) | Positif dan masuk akal |

## Langkah Selanjutnya

- [Tutup Tahun & SPT](07-year-end.md) — closing akhir tahun
