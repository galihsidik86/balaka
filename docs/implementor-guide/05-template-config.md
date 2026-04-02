# Konfigurasi Template Jurnal

Panduan mereview, menyesuaikan, dan membuat template jurnal untuk klien.

## Konsep Template Jurnal

Template jurnal adalah fitur inti Balaka yang memungkinkan pengguna mencatat transaksi tanpa harus mengetahui akun mana yang di-debit atau di-kredit. Pengguna cukup:

1. Pilih template (misal: "Bayar Beban Sewa")
2. Isi jumlah
3. Klik Simpan & Posting

Balaka otomatis membuat jurnal akuntansi yang benar.

## Review Template Bawaan

Setelah import seed pack, buka **Master Data > Template Jurnal** untuk melihat daftar template.

### Template per Industri

| Industri | Jumlah Template | Fokus |
|----------|----------------|-------|
| IT Service | 40 | Pendapatan jasa + PPN/PPh, beban operasional IT |
| Online Seller | 39 | Penjualan marketplace, withdraw saldo, inventory |
| Coffee Shop | 8 | Penjualan tunai, pembelian bahan, produksi |
| Campus | 40 | Tagihan SPP, pembayaran, beasiswa, hibah |

### Template Sistem

Beberapa template ditandai sebagai **sistem** dan dijalankan otomatis oleh fitur tertentu:

| Template | Dijalankan Oleh | Kapan |
|----------|----------------|-------|
| Post Gaji Bulanan | Fitur Payroll | Saat posting penggajian |
| Penyusutan Aset | Fitur Aset Tetap | Saat posting penyusutan bulanan |
| Jurnal Penutup Tahun | Tutup Buku | Saat tutup buku akhir tahun |

Template sistem tidak perlu diubah kecuali ada kebutuhan khusus.

## Tipe Template

### SIMPLE

Pengguna hanya mengisi **satu angka** (jumlah). Semua baris jurnal dihitung dari angka tersebut.

Contoh: **Bayar Beban Sewa**

| Baris | Akun | Debit | Kredit |
|-------|------|-------|--------|
| 1 | 5.1.05 Beban Sewa | `amount` | |
| 2 | [BANK] | | `amount` |

Pengguna isi jumlah `15.000.000` → debit Beban Sewa 15 juta, kredit Bank 15 juta.

### DETAILED

Pengguna mengisi **beberapa variabel**. Untuk transaksi yang melibatkan lebih dari satu komponen nilai.

Contoh: **Penjualan Tokopedia**

Variabel: `grossSales` (harga jual) dan `adminFee` (potongan admin Tokopedia)

| Baris | Akun | Debit | Kredit |
|-------|------|-------|--------|
| 1 | 1.1.04 Saldo Tokopedia | `grossSales - adminFee` | |
| 2 | 5.1.31 Beban Admin Tokopedia | `adminFee` | |
| 3 | 4.1.01 Pendapatan Penjualan | | `grossSales` |

Pengguna isi `grossSales = 25.000.000` dan `adminFee = 1.250.000` → debit Saldo Tokopedia 23.750.000, debit Beban Admin 1.250.000, kredit Pendapatan 25.000.000.

## Formula

Setiap baris template memiliki formula yang menentukan nilai debit atau kreditnya.

### Formula untuk Template SIMPLE

| Formula | Arti | Contoh |
|---------|------|--------|
| `amount` | Sama dengan jumlah yang diisi | Rp 10.000.000 |
| `amount * 1.11` | Jumlah + PPN 11% | Rp 11.100.000 |
| `amount * 0.11` | PPN 11% dari jumlah | Rp 1.100.000 |
| `amount * 0.02` | PPh 23 sebesar 2% | Rp 200.000 |
| `amount * 0.98` | Jumlah dikurangi PPh 23 | Rp 9.800.000 |

### Formula untuk Template DETAILED

| Formula | Arti |
|---------|------|
| `grossSales` | Variabel harga jual kotor |
| `adminFee` | Variabel potongan admin |
| `grossSales - adminFee` | Selisih harga jual dan potongan |
| `grossSalary` | Variabel gaji kotor (payroll) |
| `netPay` | Variabel gaji bersih (payroll) |

Variabel bisa menggunakan operasi aritmatika: `+`, `-`, `*` (kali), `/` (bagi).

## Account Hint

Beberapa baris template menggunakan **hint** alih-alih akun tetap. Saat pengguna membuat transaksi, mereka memilih akun yang sesuai dari dropdown.

| Hint | Arti | Contoh Akun |
|------|------|-------------|
| BANK | Rekening bank | 1.1.02 Bank BCA, 1.1.03 Bank Mandiri |
| PENDAPATAN | Akun pendapatan | 4.1.01 Pendapatan Jasa Training |
| BEBAN | Akun beban | 5.1.05 Beban Sewa, 5.1.07 Beban Listrik |
| ASET_TETAP | Akun aset tetap | 1.2.01 Peralatan Komputer |
| BEBAN_PENYUSUTAN | Akun beban penyusutan | 5.1.12 Beban Penyusutan |
| AKUM_PENYUSUTAN | Akun akumulasi penyusutan | 1.2.02 Akum. Penyusutan |

Keuntungan hint: satu template bisa dipakai untuk berbagai akun bank atau jenis pendapatan. Tidak perlu membuat template terpisah untuk setiap akun bank.

## Kategori Template

Template dikelompokkan berdasarkan kategori:

| Kategori | Fungsi | Contoh |
|----------|--------|--------|
| INCOME | Pencatatan pendapatan | Pendapatan jasa, penjualan |
| EXPENSE | Pencatatan beban | Sewa, listrik, gaji |
| PAYMENT | Pembayaran hutang | Bayar hutang gaji, setor pajak |
| RECEIPT | Penerimaan kas | Terima pembayaran, setoran modal |
| TRANSFER | Transfer antar akun | Transfer antar bank |

## Template Tags

Template bisa diberi tag untuk memudahkan pencarian dan filterisasi. Tag yang umum digunakan:

- `pajak` — template yang berhubungan dengan pajak
- `payroll` — template penggajian
- `rutin` — transaksi yang terjadi setiap bulan

## Membuat Template Baru

Jika template bawaan tidak mencukupi, buat template baru:

1. Buka **Master Data > Template Jurnal**
2. Klik **Buat Template**
3. Isi:
   - **Nama** — nama deskriptif (misal: "Bayar Beban Asuransi")
   - **Deskripsi** — penjelasan kapan template ini digunakan
   - **Tipe** — SIMPLE atau DETAILED
   - **Kategori** — INCOME, EXPENSE, PAYMENT, RECEIPT, atau TRANSFER
   - **Sistem** — biarkan kosong (hanya untuk template yang dijalankan otomatis)
4. Tambahkan baris jurnal:
   - Pilih akun atau hint
   - Isi formula debit atau kredit
   - Isi template deskripsi (opsional)
5. Klik **Simpan**

### Contoh: Membuat Template "Bayar Beban Asuransi"

Tipe: SIMPLE, Kategori: EXPENSE

| Baris | Akun/Hint | Debit | Kredit |
|-------|-----------|-------|--------|
| 1 | 5.1.14 Beban Asuransi | `amount` | |
| 2 | BANK | | `amount` |

Langkah:
1. Pastikan akun 5.1.14 Beban Asuransi sudah ada di COA (tambahkan jika belum)
2. Buat template dengan tipe SIMPLE
3. Baris 1: account_code = 5.1.14, debit_formula = `amount`
4. Baris 2: account_hint = BANK, credit_formula = `amount`

### Contoh: Membuat Template "Pendapatan Jasa + PPN"

Tipe: SIMPLE, Kategori: INCOME

| Baris | Akun/Hint | Debit | Kredit |
|-------|-----------|-------|--------|
| 1 | BANK | `amount * 1.11` | |
| 2 | PENDAPATAN | | `amount` |
| 3 | 2.1.03 Hutang PPN | | `amount * 0.11` |

Pengguna isi amount = 10.000.000. Hasil:
- Debit Bank 11.100.000 (DPP + PPN)
- Kredit Pendapatan 10.000.000 (DPP)
- Kredit Hutang PPN 1.100.000 (PPN 11%)

## Menonaktifkan Template

Template yang tidak digunakan klien bisa dinonaktifkan:

1. Buka **Master Data > Template Jurnal**
2. Klik template yang ingin dinonaktifkan
3. Hapus centang **Aktif**
4. Klik **Simpan**

Template yang dinonaktifkan tidak muncul di dropdown saat input transaksi.

## Tips Konfigurasi per Industri

### IT Service

- Pastikan template pendapatan+PPN sudah benar untuk PKP
- Jika klien menerima potongan PPh 23 dari customer, pastikan ada template "Pendapatan Jasa + PPh 23"
- Template "Pendapatan Jasa BUMN FP03" untuk transaksi dengan instansi pemerintah

### Online Seller

- Sesuaikan template penjualan per marketplace yang digunakan klien
- Jika klien hanya jualan di Tokopedia dan Shopee, nonaktifkan template Lazada dan TikTok
- Pastikan persentase admin fee di formula sesuai dengan rate marketplace terbaru

### Coffee Shop

- Review BOM (Bill of Materials) untuk memastikan resep dan biaya produksi benar
- Template penjualan tunai sudah termasuk perhitungan HPP otomatis

### Campus

- Sesuaikan jenis tagihan dengan program studi yang ada
- Pastikan template beasiswa sudah sesuai kebijakan kampus

## Langkah Selanjutnya

[Setup Pajak](06-tax-setup.md)
