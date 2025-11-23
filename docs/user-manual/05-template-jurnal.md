# Template Jurnal

## Pengertian

Template Jurnal adalah pola pencatatan transaksi yang sudah dikonfigurasi sebelumnya untuk mempermudah pencatatan transaksi berulang.

## Kategori Template

| Kategori | Warna | Keterangan |
|----------|-------|------------|
| Pendapatan | Hijau | Penerimaan dari penjualan/jasa |
| Pengeluaran | Merah | Biaya operasional |
| Pembayaran | Biru | Pembayaran hutang |
| Penerimaan | Cyan | Penerimaan piutang |
| Transfer | Ungu | Perpindahan antar akun |

## Melihat Daftar Template

1. Klik menu **Template** di sidebar
2. Template ditampilkan dalam bentuk kartu dengan kategori
3. Gunakan tab kategori untuk memfilter template

## Menggunakan Template

1. Dari halaman Template, klik tombol **Gunakan** pada template yang diinginkan
2. Atau dari halaman Transaksi, klik **Transaksi Baru** dan pilih template
3. Anda akan diarahkan ke form transaksi yang sudah terisi sesuai template

## Membuat Template Baru

1. Klik tombol **Template Baru**
2. Isi informasi template:
   - **Nama Template** - Nama yang menjelaskan fungsi template
   - **Kategori** - Pilih kategori yang sesuai
   - **Klasifikasi Arus Kas** - Operasional, Investasi, atau Pendanaan
   - **Tipe Template** - Sederhana atau Terperinci
3. Konfigurasi baris jurnal:
   - Pilih **Akun** untuk setiap baris
   - Tentukan **Posisi** (Debit/Kredit)
   - Masukkan **Formula** jika diperlukan
4. Klik **Simpan Template**

## Formula yang Didukung

| Formula | Keterangan | Contoh |
|---------|------------|--------|
| `amount` | Jumlah yang diinput | Rp 1.000.000 |
| `amount * 0.11` | PPN 11% | Rp 110.000 |
| `amount / 1.11` | DPP dari harga inklusif | Rp 900.901 |
| `amount - ppn` | Jumlah setelah dikurangi PPN | Rp 890.000 |

## Contoh Template: Pendapatan Jasa dengan PPN

Struktur jurnal:
```
Debit  : Bank BCA          = amount
Kredit : Hutang PPN        = amount * 0.11 / 1.11
Kredit : Pendapatan Jasa   = amount / 1.11
```

## Menduplikasi Template

1. Buka detail template yang ingin diduplikasi
2. Klik tombol **Duplikat**
3. Ubah nama dan konfigurasi sesuai kebutuhan
4. Klik **Simpan Template**

## Menonaktifkan Template

Template yang tidak digunakan lagi dapat dinonaktifkan agar tidak muncul di pilihan transaksi:

1. Buka detail template
2. Klik **Edit**
3. Nonaktifkan toggle **Status Template**
4. Klik **Simpan Perubahan**
