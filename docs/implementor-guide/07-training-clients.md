# Training Klien

Panduan untuk melatih staf klien menggunakan Balaka.

## Prinsip Training

- **Mulai dari yang paling sering digunakan** — input transaksi harian, bukan fitur-fitur lanjutan
- **Gunakan data nyata klien** — jangan pakai contoh generik, pakai nama akun dan template yang sudah dikonfigurasi untuk klien tersebut
- **Satu sesi satu topik** — jangan campur banyak topik dalam satu sesi, pengguna akan kewalahan
- **Hands-on** — biarkan pengguna langsung praktek, jangan hanya demo

## Jadwal Training yang Direkomendasikan

### Hari 1: Pencatatan Transaksi Dasar (2-3 jam)

**Tujuan**: Pengguna bisa mencatat transaksi sehari-hari secara mandiri.

**Materi:**

1. **Login dan navigasi** (15 menit)
   - Cara login
   - Mengenal menu sidebar
   - Dashboard — apa saja yang ditampilkan

2. **Input transaksi dengan template** (60 menit)
   - Pilih template dari dropdown
   - Isi tanggal, jumlah, deskripsi
   - Simpan sebagai draft vs langsung posting
   - Coba 3-5 template yang paling sering dipakai klien
   - Latihan: catat 5 transaksi nyata hari ini

3. **Melihat dan mengedit transaksi** (30 menit)
   - Daftar transaksi (filter per tanggal, template)
   - Melihat detail jurnal
   - Mengedit transaksi draft
   - Void transaksi yang salah

4. **Template yang sering dipakai** (30 menit)
   - Review template utama per industri klien
   - Kapan pakai template mana
   - Perbedaan SIMPLE vs DETAILED

**Fokus per industri:**

| Industri | Template untuk latihan |
|----------|----------------------|
| IT Service | Pendapatan Jasa, Bayar Beban Sewa, Bayar Beban Cloud |
| Online Seller | Penjualan Tokopedia, Penjualan Shopee, Pembelian Barang |
| Coffee Shop | Penjualan Tunai, Pembelian Bahan Baku, Produksi Barang Jadi |
| Campus | Tagihan SPP, Pembayaran SPP, Bayar Beban Gaji Dosen |

### Hari 2: Laporan Keuangan (2 jam)

**Tujuan**: Pengguna bisa membaca dan memahami laporan keuangan.

**Materi:**

1. **Trial Balance** (30 menit)
   - Cara membuka laporan Trial Balance
   - Membaca saldo per akun
   - Memastikan total debit = total kredit
   - Filter per periode

2. **Laba Rugi** (30 menit)
   - Pendapatan vs Beban
   - Laba/Rugi bersih
   - Perbandingan antar bulan
   - Apa yang harus diperhatikan

3. **Neraca** (30 menit)
   - Aset = Liabilitas + Ekuitas
   - Posisi kas dan bank
   - Piutang dan hutang
   - Membaca neraca untuk menilai kesehatan keuangan

4. **Laporan Arus Kas** (30 menit)
   - Arus kas operasional, investasi, pendanaan
   - Mengapa laba tidak sama dengan kas

### Hari 3: Payroll (2 jam)

**Tujuan**: Pengguna bisa menjalankan penggajian bulanan.

**Materi:**

1. **Data karyawan** (20 menit)
   - Review data karyawan yang sudah diinput
   - Status PTKP dan pengaruhnya ke PPh 21
   - Menambah/mengubah data karyawan

2. **Proses penggajian** (40 menit)
   - Buat payroll run baru
   - Calculate — sistem menghitung gaji, BPJS, PPh 21
   - Review detail per karyawan
   - Approve
   - Post — jurnal gaji otomatis dibuat

3. **Pembayaran gaji** (30 menit)
   - Setelah posting payroll, catat transaksi "Bayar Hutang Gaji"
   - Catat transaksi "Bayar Hutang BPJS"
   - Catat transaksi "Setor PPh 21" (tanggal 10 bulan berikutnya)

4. **Review slip gaji** (30 menit)
   - Melihat detail potongan per karyawan
   - Memahami komponen gaji
   - Verifikasi PPh 21 TER

### Hari 4: Pajak (2 jam)

**Tujuan**: Pengguna memahami pelaporan pajak melalui Balaka.

**Materi:**

1. **Jadwal pajak** (15 menit)
   - Batas waktu setor dan lapor
   - Checklist pajak bulanan

2. **PPh 21** (30 menit)
   - Review PPh 21 dari payroll
   - Setor via template "Setor PPh 21"
   - Bukti potong 1721-A1

3. **PPN (khusus PKP)** (30 menit)
   - PPN Keluaran (dari penjualan)
   - PPN Masukan (dari pembelian)
   - Setor PPN via template "Setor PPN"
   - Laporan PPN

4. **SPT Tahunan** (30 menit)
   - Data apa saja yang bisa diexport dari Balaka
   - Rekonsiliasi fiskal
   - Lampiran-lampiran SPT Tahunan Badan

5. **PPh Final UMKM (khusus non-PKP)** (15 menit)
   - Perhitungan 0,5% dari omzet bruto
   - Setor via template "Bayar PPh Final UMKM"

## Materi Tambahan (Opsional)

### Aset Tetap (1 jam)
- Mencatat pembelian aset tetap
- Kategori aset dan masa manfaat
- Penyusutan otomatis bulanan
- Penjualan/penghapusan aset

### Inventori (1 jam, untuk Online Seller dan Coffee Shop)
- Stok masuk dan stok keluar
- HPP otomatis
- BOM dan produksi (Coffee Shop)
- Laporan stok

### Rekonsiliasi Bank (1 jam)
- Import mutasi bank (CSV)
- Auto-matching transaksi
- Manual matching
- Review dan complete

## Titik-Titik Kebingungan yang Sering Terjadi

Berdasarkan pengalaman, berikut hal-hal yang sering membingungkan pengguna baru:

### 1. Draft vs Posted

Pengguna sering lupa mem-posting transaksi.

**Jelaskan:**
- **Draft** = dicatat tapi belum masuk jurnal. Tidak mempengaruhi laporan.
- **Posted** = sudah final, masuk ke jurnal dan laporan.
- Transaksi penting harus di-posting agar laporan akurat.

### 2. Template Mana yang Dipakai

Pengguna bingung memilih template untuk situasi tertentu.

**Solusi:** Buat cheat sheet sederhana untuk klien:

| Situasi | Template |
|---------|----------|
| Terima bayaran dari customer | Pendapatan Jasa / Penjualan |
| Bayar sewa kantor | Bayar Beban Sewa |
| Bayar listrik/internet | Bayar Beban Listrik / Telekomunikasi |
| Transfer antar bank | Transfer Antar Bank |
| Terima barang dari supplier | Pembelian Barang |

### 3. Akun Hint (BANK, PENDAPATAN)

Pengguna bingung saat diminta memilih akun dari dropdown.

**Jelaskan:**
- Beberapa template memerlukan pilihan akun (misal: bank mana yang digunakan)
- Pilih akun yang sesuai dengan transaksi yang sesungguhnya terjadi
- Contoh: jika bayar pakai BCA, pilih "1.1.02 Bank BCA"

### 4. Saldo Bank Negatif

Pengguna panik melihat saldo bank negatif di laporan.

**Jelaskan:**
- Saldo negatif berarti ada pengeluaran yang belum diimbangi penerimaan di sistem
- Cek apakah ada penerimaan yang belum dicatat
- Cek apakah saldo awal sudah diinput dengan benar

### 5. Laba Rugi Tidak Sesuai Harapan

**Jelaskan:**
- Pastikan semua transaksi sudah di-posting (bukan draft)
- Cek filter periode — mungkin melihat periode yang salah
- Pastikan tidak ada transaksi yang tercatat di akun yang keliru

## Menggunakan Instance Demo untuk Training

ArtiVisi menyediakan instance demo yang sudah terisi data contoh lengkap. Gunakan instance ini untuk:

1. **Sebelum training** — partner berlatih mendemonstrasikan fitur
2. **Saat training** — pengguna bisa mencoba tanpa takut merusak data klien
3. **Setelah training** — pengguna bisa terus berlatih mandiri

Instance demo berisi data transaksi satu tahun penuh dengan payroll, penyusutan aset, dan laporan pajak.

## Setelah Training

1. Pastikan pengguna sudah bisa input transaksi mandiri
2. Berikan kontak partner untuk pertanyaan sehari-hari
3. Jadwalkan review pertama 1-2 minggu setelah training
4. Cek trial balance di review pertama — apakah debit = kredit, apakah saldo bank masuk akal

## Langkah Selanjutnya

[Support Playbook](08-support-playbook.md)
