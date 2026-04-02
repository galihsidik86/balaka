# Debit dan Kredit

Setiap transaksi keuangan dicatat dengan minimal satu **debit** dan satu **kredit**. Total debit harus selalu sama dengan total kredit — ini disebut **double-entry bookkeeping**.

## Aturan Dasar

| Jenis Akun | Bertambah di | Berkurang di |
|-----------|-------------|-------------|
| Aset | **Debit** | Kredit |
| Beban | **Debit** | Kredit |
| Liabilitas | Debit | **Kredit** |
| Ekuitas | Debit | **Kredit** |
| Pendapatan | Debit | **Kredit** |

## Contoh Transaksi

### 1. Terima Pendapatan Jasa Rp 100.000.000

Uang masuk ke bank (aset bertambah) dan pendapatan bertambah:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.02 Bank BCA | 100.000.000 | |
| 4.1.01 Pendapatan Jasa | | 100.000.000 |

Bank bertambah (debit), pendapatan bertambah (kredit). **Balance**: 100.000.000 = 100.000.000 ✓

### 2. Bayar Sewa Kantor Rp 15.000.000

Uang keluar dari bank (aset berkurang) dan beban bertambah:

| Akun | Debit | Kredit |
|------|-------|--------|
| 5.1.05 Beban Sewa | 15.000.000 | |
| 1.1.02 Bank BCA | | 15.000.000 |

Beban bertambah (debit), bank berkurang (kredit). **Balance**: 15.000.000 = 15.000.000 ✓

### 3. Pendapatan dengan PPN 11%

Klien bayar Rp 111.000.000 (harga jual 100jt + PPN 11jt):

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.02 Bank BCA | 111.000.000 | |
| 4.1.01 Pendapatan Jasa | | 100.000.000 |
| 2.1.03 Hutang PPN | | 11.000.000 |

Bank menerima total termasuk PPN. Pendapatan dicatat tanpa PPN. PPN menjadi hutang ke negara.

### 4. Posting Payroll

Gaji karyawan Rp 75.000.000, BPJS perusahaan Rp 6.584.230, PPh 21 Rp 3.600.000, net pay Rp 68.797.885:

| Akun | Debit | Kredit |
|------|-------|--------|
| 5.1.01 Beban Gaji | 75.000.000 | |
| 5.1.02 Beban BPJS Kes | 2.400.000 | |
| 5.1.03 Beban BPJS TK | 4.184.230 | |
| 2.1.10 Hutang Gaji | | 68.797.885 |
| 2.1.13 Hutang BPJS | | 9.186.345 |
| 2.1.20 Hutang PPh 21 | | 3.600.000 |

Beban diakui (debit), hutang terbentuk (kredit). Pembayaran gaji dan BPJS dilakukan terpisah nanti.

## Yang Perlu Diingat

- **Debit bukan berarti pengeluaran.** Debit berarti sisi kiri jurnal.
- **Kredit bukan berarti pemasukan.** Kredit berarti sisi kanan jurnal.
- **Total debit selalu = total kredit.** Jika tidak seimbang, ada kesalahan.
- **Anda tidak perlu hafal aturan ini.** Template jurnal di Balaka otomatis menentukan akun mana yang di-debit dan di-kredit.

## Langkah Selanjutnya

- [Template Jurnal](04-journal-templates.md) — cara Balaka mengotomasi pencatatan debit/kredit
- [Laporan Keuangan](05-financial-reports.md) — cara membaca hasil pencatatan
