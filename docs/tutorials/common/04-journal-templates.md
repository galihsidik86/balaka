# Template Jurnal

Template jurnal adalah inti dari Balaka. Setiap kali Anda mencatat transaksi, Anda memilih template yang sesuai, mengisi jumlahnya, dan Balaka otomatis membuat jurnal dengan akun yang benar.

Anda **tidak perlu menghafal** akun mana yang di-debit atau di-kredit. Template mengurus semuanya.

## Cara Kerja

1. Pilih template (misal: "Bayar Beban Sewa")
2. Isi tanggal dan jumlah (misal: 15.000.000)
3. Klik **Simpan & Posting**

![Form transaksi Bayar Beban Sewa](screenshots/tutorials/it-service/tx-form-bayar-beban-sewa.png)

4. Balaka otomatis membuat jurnal:

| Akun | Debit | Kredit |
|------|-------|--------|
| 5.1.05 Beban Sewa | 15.000.000 | |
| 1.1.02 Bank BCA | | 15.000.000 |

## Tipe Template

### SIMPLE

Anda hanya mengisi **satu angka** (jumlah). Template menghitung sisanya.

Contoh: "Bayar Beban Sewa" — isi jumlah sewa, otomatis debit beban dan kredit bank.

### DETAILED

Anda mengisi **beberapa angka** (variabel). Untuk transaksi yang melibatkan lebih dari satu komponen nilai.

Contoh: "Penjualan Tokopedia" — isi `grossSales` (harga jual) dan `adminFee` (potongan Tokopedia).

Contoh: "Post Gaji Bulanan" — diisi otomatis oleh sistem payroll dengan 6 variabel: grossSalary, companyBpjsKes, companyBpjsTk, netPay, totalBpjs, pph21.

## Formula

Setiap baris template memiliki **formula** yang menentukan berapa nilai debit atau kreditnya.

| Formula | Arti |
|---------|------|
| `amount` | Sama dengan jumlah yang Anda isi |
| `amount * 1.11` | Jumlah + PPN 11% |
| `amount * 0.11` | PPN 11% dari jumlah |
| `amount * 0.02` | PPh 23 (2% dari jumlah) |
| `grossSales - adminFee` | Harga jual dikurangi potongan admin |
| `cogs` | Variabel terpisah untuk harga pokok |

## Account Hint

Beberapa template menggunakan **hint** alih-alih akun tetap. Saat membuat transaksi, Anda memilih akun yang sesuai dari dropdown.

| Hint | Arti | Pilihan Umum |
|------|------|-------------|
| BANK | Rekening bank | 1.1.02 Bank BCA, 1.1.03 Bank Mandiri |
| PENDAPATAN | Akun pendapatan | 4.1.01, 4.1.02 |
| BEBAN | Akun beban | Sesuai jenis pengeluaran |

Ini memberikan fleksibilitas — satu template bisa dipakai untuk berbagai akun bank atau jenis pendapatan.

## Kategori Template

Template dikelompokkan berdasarkan fungsi:

| Kategori | Contoh |
|----------|--------|
| **INCOME** | Pendapatan jasa, penjualan |
| **EXPENSE** | Sewa, listrik, gaji, cloud |
| **PAYMENT** | Bayar hutang gaji, setor pajak |
| **RECEIPT** | Terima pembayaran, setoran modal |
| **TRANSFER** | Transfer antar bank |

## Template Sistem

Beberapa template ditandai sebagai **sistem** dan dijalankan otomatis oleh fitur tertentu:

| Template | Dijalankan Oleh |
|----------|----------------|
| Post Gaji Bulanan | Fitur Payroll (saat posting) |
| Penyusutan Aset | Fitur Aset Tetap (saat posting penyusutan) |
| Jurnal Penutup Tahun | Fitur Tutup Buku Akhir Tahun |
| Pembelian/Penjualan Persediaan | Fitur Inventori |

Anda tidak perlu menggunakan template ini secara manual — sistem menjalankannya di waktu yang tepat.

## Template per Industri

Setiap industri memiliki template yang sudah disesuaikan:

### IT Service (39 template)
Pendapatan Jasa +PPN, +PPh 23, BUMN FP03, Bayar Beban Cloud/Software/Sewa, Post Gaji, Setor Pajak

### Online Seller (39 template)
Penjualan Tokopedia/Shopee/TikTok, Withdraw Saldo, Pembelian Barang, Ongkir, Packing, Iklan

### Coffee Shop (26 template)
Penjualan Tunai + COGS, Pembelian Bahan Baku, Produksi Barang Jadi, Penjualan Online GrabFood

### Campus (40 template)
Tagihan SPP/Uang Pangkal/Praktikum, Pembayaran SPP, Beasiswa, Gaji Dosen, Hibah

## Membuat Template Sendiri

Jika template bawaan tidak sesuai, Anda bisa membuat template sendiri:

![Daftar template jurnal](screenshots/tutorials/it-service/template-list.png)

1. Buka **Master Data → Template Jurnal**
2. Klik **Buat Template**
3. Isi nama, kategori, dan tipe (SIMPLE/DETAILED)
4. Tambahkan baris jurnal dengan akun dan formula
5. Simpan

Atau gunakan template **Jurnal Manual** — Anda pilih sendiri akun debit dan kredit saat membuat transaksi.

## Langkah Selanjutnya

- [Laporan Keuangan](05-financial-reports.md) — cara membaca hasil dari transaksi yang sudah dicatat
- [Payroll](06-payroll.md) — template otomatis untuk penggajian
