# Proses Penggajian (Payroll)

Fitur payroll memungkinkan perhitungan gaji bulanan untuk semua karyawan aktif, lengkap dengan BPJS dan PPh 21.

## Alur Kerja Payroll

```
Buat Payroll → Calculate → Approve → Post ke Jurnal
```

Status payroll:
- **Draft**: Payroll baru dibuat, belum dikalkulasi
- **Calculated**: Perhitungan selesai, siap review
- **Approved**: Disetujui, siap posting
- **Posted**: Jurnal sudah dibuat
- **Cancelled**: Dibatalkan

## Membuat Payroll Baru

1. Klik menu **Payroll** > **Daftar Payroll**
2. Klik tombol **Buat Payroll Baru**
3. Isi form:
   - **Periode**: Format YYYY-MM (contoh: 2025-01)
   - **Gaji Pokok**: Jumlah gaji pokok untuk semua karyawan
   - **Kelas Risiko JKK**: Kelas risiko kecelakaan kerja (1-5)
4. Klik **Simpan & Hitung**

Sistem akan otomatis:
- Mengambil semua karyawan dengan status ACTIVE
- Menghitung BPJS (Kesehatan, JKK, JKM, JHT, JP)
- Menghitung PPh 21 berdasarkan status PTKP masing-masing karyawan
- Menghitung total potongan dan gaji netto

## Komponen Perhitungan

### BPJS yang Dihitung

| Komponen | Ditanggung Perusahaan | Ditanggung Karyawan |
|----------|----------------------|---------------------|
| Kesehatan | 4% | 1% |
| JKK | 0.24% - 1.74% (sesuai kelas) | - |
| JKM | 0.3% | - |
| JHT | 3.7% | 2% |
| JP | 2% | 1% |

### PPh 21

PPh 21 dihitung dengan tarif progresif:
- 5% untuk PKP 0 - 60 juta
- 15% untuk PKP 60 juta - 250 juta
- 25% untuk PKP 250 juta - 500 juta
- 30% untuk PKP 500 juta - 5 miliar
- 35% untuk PKP di atas 5 miliar

PTKP 2024:
- TK/0: Rp 54.000.000
- K/0: Rp 58.500.000
- K/1: Rp 63.000.000
- K/2: Rp 67.500.000
- K/3: Rp 72.000.000

## Detail Payroll

Halaman detail menampilkan:

### Summary

- **Jumlah Karyawan**: Total karyawan yang diproses
- **Total Bruto**: Total gaji kotor
- **Total Potongan**: BPJS karyawan + PPh 21
- **Total Neto**: Gaji bersih yang dibayarkan

### Rincian per Karyawan

Tabel yang menampilkan:
- NIK dan Nama
- Gaji Bruto
- BPJS Karyawan
- PPh 21
- Total Potongan
- Gaji Neto

## Approve Payroll

Setelah review perhitungan:

1. Buka halaman detail payroll
2. Verifikasi jumlah dan perhitungan
3. Klik tombol **Approve**

Setelah di-approve, payroll siap untuk di-posting ke jurnal.

## Posting ke Jurnal

Posting akan membuat jurnal akuntansi:

1. Dari halaman detail payroll yang sudah di-approve
2. Klik tombol **Post ke Jurnal**
3. Konfirmasi posting

### Jurnal yang Dibuat

| Akun | Posisi | Jumlah |
|------|--------|--------|
| Beban Gaji | Debit | Total bruto |
| Beban BPJS | Debit | BPJS perusahaan |
| Hutang Gaji | Kredit | Total neto |
| Hutang BPJS | Kredit | Total BPJS (perusahaan + karyawan) |
| Hutang PPh 21 | Kredit | Total PPh 21 |

Setelah posting:
- Status berubah menjadi **Posted**
- Link ke transaksi ditampilkan
- Payroll tidak dapat dibatalkan

## Membatalkan Payroll

Payroll dapat dibatalkan selama belum di-posting:

1. Buka halaman detail payroll
2. Klik tombol **Batalkan**
3. Konfirmasi pembatalan

Pembatalan tidak menghapus data, hanya mengubah status menjadi Cancelled.

## Menghapus Payroll

Hanya payroll dengan status **Draft** yang dapat dihapus:

1. Buka halaman detail payroll
2. Klik tombol **Hapus**
3. Konfirmasi penghapusan

## Filter dan Pencarian

Di halaman daftar payroll:

1. Gunakan dropdown **Status** untuk filter
2. Pilih status: All, Draft, Calculated, Approved, Posted, Cancelled
3. Klik periode untuk melihat detail

## Tips

### Sebelum Memulai Payroll

- Pastikan data karyawan sudah lengkap (NPWP, status PTKP)
- Pastikan karyawan yang akan digaji berstatus **Active**
- Tentukan kelas risiko JKK sesuai jenis usaha

### Kelas Risiko JKK

| Kelas | Tarif | Contoh Jenis Usaha |
|-------|-------|-------------------|
| 1 | 0.24% | Jasa IT, Konsultan |
| 2 | 0.54% | Retail, Kuliner |
| 3 | 0.89% | Manufaktur ringan |
| 4 | 1.27% | Konstruksi |
| 5 | 1.74% | Pertambangan |

### Pembayaran Gaji

Setelah posting:

1. Transfer gaji ke rekening karyawan sesuai Total Neto
2. Catat jurnal **Transfer Bank** untuk setiap pembayaran
3. Setor BPJS dan PPh 21 sesuai jadwal

## Lihat Juga

- [Kelola Karyawan](60-kelola-karyawan.md) - Setup data karyawan
- [Komponen Gaji](61-komponen-gaji.md) - Pengaturan komponen
- [Kalkulator BPJS](62-kalkulator-bpjs.md) - Simulasi perhitungan BPJS
- [Kalkulator PPh 21](63-kalkulator-pph21.md) - Simulasi perhitungan PPh 21
- [Kalender Pajak](33-kalender-pajak.md) - Jadwal setor dan lapor PPh 21
