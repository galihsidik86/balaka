# Onboarding Klien Baru

Checklist dan langkah-langkah lengkap untuk menambahkan klien baru ke Balaka.

## Checklist Onboarding

Sebelum memulai, kumpulkan informasi berikut dari klien:

### Data Perusahaan

| Data | Keterangan | Contoh |
|------|-----------|--------|
| Nama perusahaan | Nama resmi sesuai akta/NPWP | PT Maju Teknologi |
| Alamat | Alamat kantor/tempat usaha | Jl. Sudirman No. 10, Jakarta |
| Telepon | Nomor telepon kantor | 021-5551234 |
| Email | Email resmi perusahaan | admin@majuteknologi.co.id |
| NPWP | Nomor Pokok Wajib Pajak (format baru 16 digit) | 01.234.567.8-012.345 |
| NITKU | Nomor Identitas Tempat Kegiatan Usaha | 0000000000000001 |
| Status PKP | Apakah terdaftar sebagai Pengusaha Kena Pajak | Ya/Tidak |
| PKP sejak | Tanggal pengukuhan PKP (jika PKP) | 2023-01-15 |
| Tanggal berdiri | Tanggal pendirian usaha | 2020-05-01 |

### Informasi Akuntansi

| Data | Keterangan |
|------|-----------|
| Industri | IT Service, Online Seller, Coffee Shop, Campus |
| Tahun fiskal | Bulan awal tahun buku (umumnya Januari) |
| Mata uang | IDR (saat ini hanya mendukung Rupiah) |
| Rekening bank | Nama bank dan nomor rekening yang digunakan |
| Daftar karyawan | Nama, PTKP, tanggal masuk, gaji |

### Keputusan Konfigurasi

| Keputusan | Opsi |
|-----------|------|
| Seed pack | IT Service / Online Seller / Coffee Shop / Campus |
| Perlu kustomisasi COA? | Ya → lihat [Kustomisasi COA](03-coa-customization.md) |
| Ada data historis? | Ya → lihat [Import Data](04-data-import.md) |
| Jumlah user | Berapa orang yang akan menggunakan aplikasi |

## Langkah-Langkah Onboarding

### 1. Request Provisioning Instance

Kirim request ke ArtiVisi dengan informasi:
- Nama perusahaan klien
- Seed pack yang dipilih (IT Service / Online Seller / Coffee Shop / Campus)
- Subdomain yang diinginkan (misal: `majuteknologi.balaka.id`)

ArtiVisi akan menyediakan:
- Instance baru dengan database kosong
- URL akses
- Akun admin awal (username + password sementara)

Proses provisioning memakan waktu 1-2 hari kerja.

### 2. Login dan Ganti Password

1. Buka URL instance klien di browser
2. Login menggunakan akun admin dari ArtiVisi
3. Buka menu **Pengaturan > Pengguna**
4. Ganti password admin

### 3. Import Seed Pack

Seed pack berisi data awal yang sudah disesuaikan per industri:

| Seed Pack | Isi | Cocok Untuk |
|-----------|-----|-------------|
| IT Service | 75 akun, 40 template, PKP-ready | Jasa IT, konsultan, software house |
| Online Seller | 84 akun, 39 template, marketplace | Toko online Tokopedia/Shopee/TikTok |
| Coffee Shop | 74 akun, 8 template, BOM/produksi | Kedai kopi, restoran kecil, F&B |
| Campus | COA pendidikan, 40 template | Perguruan tinggi, lembaga pendidikan |

Untuk mengimpor seed pack:

1. Buka menu **Pengaturan > Import Data**
2. Klik tombol **Choose File** dan pilih file ZIP seed pack
3. Klik **Import**
4. Tunggu proses selesai — akan muncul pesan berapa record yang berhasil diimport

### 4. Konfigurasi Data Perusahaan

Buka menu **Pengaturan > Perusahaan** dan lengkapi:

1. **Nama Perusahaan** — nama resmi
2. **Alamat** — alamat lengkap
3. **Telepon** dan **Email**
4. **NPWP** dan **NITKU**
5. **Status PKP** — centang jika PKP
6. **PKP Sejak** — isi tanggal pengukuhan (jika PKP)
7. **Tahun Fiskal** — pilih bulan awal tahun buku
8. **Industri** — pilih sesuai jenis usaha
9. **Tanggal Berdiri**
10. **Nama dan Jabatan Penandatangan** — untuk keperluan laporan dan dokumen pajak

Klik **Simpan**.

### 5. Konfigurasi Pajak

Buka menu **Pengaturan > Profil Pajak** dan lengkapi:

- Detail konfigurasi pajak ada di [Setup Pajak](06-tax-setup.md)

### 6. Buat Periode Fiskal

Buka menu **Master Data > Periode Fiskal** dan buat periode untuk tahun berjalan:

1. Klik **Buat Periode**
2. Isi tahun dan bulan
3. Status: **OPEN**
4. Ulangi untuk semua bulan dalam tahun berjalan

Atau import periode via seed pack yang sudah menyertakan file `11_fiscal_periods.csv`.

### 7. Review dan Kustomisasi COA

Buka menu **Master Data > Bagan Akun** untuk mereview akun-akun bawaan:

1. Cek apakah ada akun yang perlu ditambahkan
2. Nonaktifkan akun yang tidak relevan
3. Lihat panduan lengkap di [Kustomisasi COA](03-coa-customization.md)

### 8. Review Template Jurnal

Buka menu **Master Data > Template Jurnal** untuk mereview template bawaan:

1. Cek apakah template sudah sesuai kebutuhan klien
2. Tambah template baru jika perlu
3. Nonaktifkan template yang tidak dipakai
4. Lihat panduan lengkap di [Konfigurasi Template](05-template-config.md)

### 9. Input Rekening Bank

Buka menu **Master Data > Bagan Akun** dan pastikan akun bank sudah sesuai:

- Contoh: jika klien menggunakan Bank BCA dan Bank Mandiri, pastikan kedua akun tersebut ada
- Jika klien menggunakan bank lain (BNI, BRI, dll), tambahkan akun baru di bawah grup 1.1 Aset Lancar

### 10. Import Saldo Awal (Jika Ada)

Jika klien sudah memiliki data keuangan sebelumnya (dari spreadsheet atau aplikasi lain):

1. Gunakan template **Saldo Awal Tahun** atau **Jurnal Manual**
2. Input saldo per akun per tanggal cutover
3. Lihat panduan lengkap di [Import Data](04-data-import.md)

### 11. Buat User Accounts

Buka menu **Pengaturan > Pengguna** untuk membuat akun pengguna:

1. Klik **Buat Pengguna**
2. Isi username, nama lengkap, email, password
3. Pilih role yang sesuai:
   - **ADMIN** — akses penuh (untuk pemilik usaha)
   - **ACCOUNTANT** — akses transaksi dan laporan
   - **CASHIER** — akses input transaksi saja
   - **VIEWER** — hanya bisa melihat laporan

### 12. Pelatihan Awal

Jadwalkan sesi pelatihan untuk staf klien. Panduan pelatihan ada di [Training Klien](07-training-clients.md).

## Checklist Final

Sebelum menyerahkan instance ke klien, pastikan:

- [ ] Data perusahaan lengkap (nama, NPWP, NITKU, PKP)
- [ ] COA sudah direview dan disesuaikan
- [ ] Template jurnal sudah direview
- [ ] Periode fiskal tahun berjalan sudah dibuat
- [ ] Konfigurasi pajak sudah diisi
- [ ] Rekening bank sudah sesuai
- [ ] Saldo awal sudah diinput (jika ada migrasi)
- [ ] User accounts sudah dibuat untuk semua pengguna
- [ ] Pelatihan awal sudah dijadwalkan
- [ ] Password admin sudah diganti dari default

## Langkah Selanjutnya

[Kustomisasi Bagan Akun (COA)](03-coa-customization.md)
