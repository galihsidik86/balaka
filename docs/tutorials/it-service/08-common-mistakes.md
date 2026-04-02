# Kesalahan Umum

Hal-hal yang sering salah saat mengelola keuangan perusahaan IT service.

## 1. Tidak Membedakan PPN untuk BUMN

**Salah:** Menggunakan template "Pendapatan Jasa + PPN" untuk klien BUMN.

**Benar:** Gunakan "Pendapatan Jasa BUMN (FP 03)". PPN untuk BUMN dipungut oleh pembeli — Anda tidak menerima PPN dari klien dan tidak perlu menyetornya.

Jika salah template, Hutang PPN akan terlalu besar dan setor PPN akan lebih dari seharusnya.

## 2. Lupa Minta Bukti Potong PPh 23

**Masalah:** Klien memotong PPh 23 (2%) dari pembayaran Anda, tapi Anda tidak meminta bukti potong.

**Dampak:** Kredit Pajak PPh 23 tidak bisa diklaim saat SPT Tahunan. Anda membayar PPh Badan lebih besar.

**Solusi:** Setiap kali menerima pembayaran yang dipotong PPh 23, minta bukti potong dari klien. Di contoh demo, PT Solusi Digital memiliki kredit pajak 36,6 juta — tanpa bukti potong, uang ini hilang.

## 3. Setor PPN Terlambat

**Batas waktu:** Akhir bulan berikutnya.

**Denda:** 2% per bulan dari PPN kurang bayar.

**Solusi:** Masukkan jadwal setor PPN di checklist tutup bulan. Di bulan dimana semua income dari BUMN, PPN nihil — tetap lapor nihil.

## 4. Payroll Desember Lebih Besar

**Fenomena:** PPh 21 Desember lebih besar dari bulan biasa.

**Bukan bug.** Ini adalah rekonsiliasi tahunan PPh 21:
- Jan-Nov: tarif TER (flat rate per bulan)
- Desember: dihitung ulang dengan tarif progresif, selisih ditambahkan

Contoh: PPh 21 Jan-Nov = 3.600.000/bulan, Desember = 16.350.000. Net pay karyawan di Desember lebih kecil.

## 5. Hutang Gaji atau BPJS Tidak Nol di Akhir Bulan

**Gejala:** Trial Balance menunjukkan Hutang Gaji masih ada saldo.

**Penyebab:** Lupa mencatat "Bayar Hutang Gaji" atau "Bayar Hutang BPJS" setelah posting payroll.

**Solusi:** Pastikan setiap bulan ada 3 transaksi setelah payroll:
1. Bayar Hutang Gaji
2. Bayar Hutang BPJS
3. Setor PPh 21 (bulan berikutnya)

## 6. Mencampur Kas Pribadi dan Perusahaan

**Masalah:** Menggunakan rekening perusahaan untuk pengeluaran pribadi tanpa mencatat.

**Solusi:** Jika terpaksa, catat sebagai **Prive / Pengambilan Pribadi**. Template ini mendebit Prive dan mengkredit Bank — mengurangi ekuitas pemilik.

## 7. Tidak Membuat Koreksi Fiskal

**Masalah:** Beberapa beban tidak boleh dikurangkan secara pajak (entertainment tanpa nominatif, sumbangan, dll). Jika tidak dikoreksi, PKP terlalu rendah dan PPh Badan kurang bayar.

**Solusi:** Setiap akhir tahun, review beban yang tidak deductible dan masukkan sebagai koreksi fiskal positif di Rekonsiliasi Fiskal.

## 8. Aset Tetap Langsung Dibebankan

**Salah:** Mencatat pembelian laptop 25 juta sebagai "Bayar Beban Operasional".

**Benar:** Daftarkan sebagai aset tetap dan disusutkan selama masa manfaat (48 bulan). Ini memberikan gambaran laba yang lebih akurat — biaya laptop dibagi merata selama 4 tahun, bukan dibebankan sekaligus.

## Referensi

- [Panduan PPN](../common/07-ppn.md)
- [Panduan PPh](../common/08-pph.md)
- [Panduan Payroll](../common/06-payroll.md)
- [Panduan Aset Tetap](../common/10-fixed-assets.md)
