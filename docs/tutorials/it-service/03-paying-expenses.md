# Mencatat Pengeluaran

Pengeluaran rutin IT service dicatat menggunakan template SIMPLE — isi jumlah, klik posting, selesai.

## Pengeluaran Bulanan

| Template | Akun Beban | Rata-rata/Bulan | Keterangan |
|----------|-----------|----------------|-----------|
| Bayar Beban Cloud & Server | 5.1.20 | 5.500.000 | AWS/GCP/Azure |
| Bayar Beban Sewa | 5.1.05 | 15.000.000 | Sewa kantor |
| Bayar Beban Telekomunikasi | 5.1.06 | 2.500.000 | Internet + telepon |
| Bayar Beban Operasional | 5.1.99 | 2.500-3.500 | ATK, transport, konsumsi |
| Beban Admin Bank | 5.2.01 | 15.000 | Potongan otomatis bank |

## Pengeluaran Triwulanan

| Template | Akun Beban | Per Kuartal | Keterangan |
|----------|-----------|------------|-----------|
| Bayar Beban Software & Lisensi | 5.1.21 | 3.300.000 | JetBrains, GitHub |

## Cara Mencatat

Semua template pengeluaran bekerja sama:

1. Pilih template (misal: Bayar Beban Cloud & Server)
2. Isi tanggal dan jumlah
3. Isi deskripsi dan referensi
4. Klik **Simpan & Posting**

![Form transaksi pembayaran beban](screenshots/tutorials/it-service/tx-form-bayar-beban-sewa.png)

Jurnal yang dihasilkan (contoh cloud):

| Akun | Debit | Kredit |
|------|-------|--------|
| 5.1.20 Beban Cloud & Server | 5.500.000 | |
| 1.1.02 Bank BCA | | 5.500.000 |

Beban bertambah (debit), bank berkurang (kredit). Setiap pengeluaran mengurangi saldo bank.

## Total Pengeluaran Operasional 2025

| Kategori | Jumlah/Tahun |
|----------|-------------|
| Beban Gaji (via payroll) | 900.000.000 |
| Beban BPJS (via payroll) | 79.010.760 |
| Beban Sewa | 180.000.000 |
| Beban Cloud & Server | 66.000.000 |
| Beban Telekomunikasi | 30.000.000 |
| Beban Software & Lisensi | 9.900.000 |
| Beban Operasional | 35.100.000 |
| Beban Penyusutan | 15.625.000 |
| Beban Admin Bank | 180.000 |
| **Total** | **1.315.815.760** |

## Tips

- **Catat segera** — jangan tunda pencatatan, karena saldo bank di Balaka harus cocok dengan mutasi bank
- **Gunakan referensi** — isi nomor invoice/kwitansi vendor sebagai referensi untuk pelacakan
- **Bukti dokumen** — upload foto kwitansi/invoice sebagai lampiran transaksi

## Langkah Selanjutnya

- [Payroll Bulanan](04-payroll.md) — mencatat gaji dan BPJS karyawan
