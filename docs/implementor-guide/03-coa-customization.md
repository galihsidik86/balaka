# Kustomisasi Bagan Akun (COA)

Panduan menyesuaikan Chart of Accounts untuk kebutuhan spesifik klien.

## Kapan Pakai COA Bawaan

Seed pack Balaka sudah menyediakan COA yang dirancang sesuai SAK EMKM untuk masing-masing industri. Untuk sebagian besar usaha kecil, COA bawaan sudah cukup.

**Gunakan COA bawaan tanpa perubahan jika:**
- Klien baru mulai pembukuan
- Struktur bisnis sederhana (satu lokasi, satu departemen)
- Tidak ada kebutuhan pelaporan khusus dari investor atau regulator

**Kustomisasi diperlukan jika:**
- Klien punya beberapa departemen atau cabang
- Ada kebutuhan tracking biaya per cost center
- Klien migrasi dari sistem lain dan ingin mempertahankan struktur akun lama
- Ada jenis pendapatan atau beban yang tidak tercakup di COA bawaan

## Struktur Penomoran Akun

COA Balaka menggunakan penomoran hierarki dengan titik sebagai separator:

```
1           ASET (header level 1)
1.1         Aset Lancar (header level 2)
1.1.01      Kas (akun detail)
1.1.02      Bank BCA (akun detail)
```

### Konvensi Penomoran per Grup

| Kode | Grup | Jenis |
|------|------|-------|
| 1.x | Aset | ASSET |
| 2.x | Liabilitas | LIABILITY |
| 3.x | Ekuitas | EQUITY |
| 4.x | Pendapatan | REVENUE |
| 5.x | Beban | EXPENSE |

### Sub-grup Umum

| Kode | Sub-grup |
|------|----------|
| 1.1.xx | Aset Lancar (kas, bank, piutang) |
| 1.2.xx | Aset Tetap (peralatan, kendaraan, bangunan) |
| 1.3.xx | Aset Tak Berwujud |
| 2.1.xx | Liabilitas Jangka Pendek (hutang, pajak) |
| 2.2.xx | Liabilitas Jangka Panjang |
| 3.1.xx | Modal |
| 3.2.xx | Laba (ditahan, berjalan) |
| 4.1.xx | Pendapatan Usaha |
| 4.2.xx | Pendapatan Lain-lain |
| 5.1.xx | Beban Operasional |
| 5.2.xx | Beban Lain-lain |
| 5.9.xx | Beban Pajak |

## COA per Industri

### IT Service (75 akun)

Fokus pada jasa dan proyek. Akun pendapatan dibedakan per jenis jasa:
- 4.1.01 Pendapatan Jasa Training
- 4.1.02 Pendapatan Jasa Konsultasi
- 4.1.03 Pendapatan Jasa Development

Akun beban mencakup beban khas IT:
- 5.1.20 Beban Cloud & Server
- 5.1.21 Beban Software & Lisensi

Akun pajak lengkap untuk PKP:
- 1.1.25 PPN Masukan
- 1.1.26 Kredit Pajak PPh 23
- 2.1.03 Hutang PPN
- 2.1.21 Hutang PPh 23

### Online Seller (84 akun)

Fokus pada marketplace dan inventory. Akun khusus:
- 1.1.04-07 Saldo Marketplace (Tokopedia, Shopee, Lazada, TikTok)
- 1.1.30 Persediaan Barang Dagangan
- 5.1.30 HPP (Harga Pokok Penjualan)
- 5.1.31-34 Beban Admin per marketplace

### Coffee Shop (74 akun)

Fokus pada manufaktur dan produksi:
- 1.1.30 Persediaan Bahan Baku
- 1.1.31 Barang Dalam Proses (WIP)
- 1.1.32 Persediaan Barang Jadi
- 5.1.30 HPP Penjualan
- 5.1.31 Beban Bahan Baku

### Campus (COA pendidikan)

Fokus pada pendapatan pendidikan:
- 4.1.01-xx Pendapatan SPP, Uang Pangkal, Praktikum, Wisuda
- 5.1.xx Beban Dosen, Beban Akademik

## Menambah Akun Baru

1. Buka menu **Master Data > Bagan Akun**
2. Klik **Buat Akun**
3. Isi:
   - **Kode Akun** — ikuti penomoran yang sudah ada (misal: 5.1.22 untuk beban baru di bawah 5.1)
   - **Nama Akun** — nama deskriptif
   - **Tipe** — ASSET, LIABILITY, EQUITY, REVENUE, atau EXPENSE
   - **Parent** — akun induk (misal: 5.1 Beban Operasional)
   - **Normal Balance** — DEBIT (untuk aset dan beban) atau CREDIT (untuk liabilitas, ekuitas, pendapatan)
   - **Permanen** — centang untuk akun neraca (aset, liabilitas, ekuitas), kosongkan untuk akun laba rugi (pendapatan, beban)
4. Klik **Simpan**

### Contoh: Menambah Akun Bank

Klien menggunakan Bank BNI yang belum ada di COA bawaan:

- Kode: `1.1.04`
- Nama: `Bank BNI`
- Tipe: ASSET
- Parent: 1.1 Aset Lancar
- Normal Balance: DEBIT
- Permanen: Ya

### Contoh: Menambah Sub-akun Beban per Departemen

Klien ingin memisahkan beban listrik per cabang:

- `5.1.07` Beban Listrik (akun induk, sudah ada)
- `5.1.07.01` Beban Listrik Cabang Jakarta (baru)
- `5.1.07.02` Beban Listrik Cabang Surabaya (baru)

## Menonaktifkan Akun

Akun yang tidak relevan bisa dinonaktifkan agar tidak muncul di dropdown saat input transaksi:

1. Buka **Master Data > Bagan Akun**
2. Klik akun yang ingin dinonaktifkan
3. Hapus centang **Aktif**
4. Klik **Simpan**

Akun yang sudah memiliki transaksi tidak bisa dihapus, hanya bisa dinonaktifkan.

## Pola Kustomisasi Umum

### Multi-Cabang

Tambahkan sub-akun per cabang untuk beban yang perlu dipisahkan:

```
5.1.05    Beban Sewa
5.1.05.01   Beban Sewa Cabang Pusat
5.1.05.02   Beban Sewa Cabang Bandung

5.1.07    Beban Listrik
5.1.07.01   Beban Listrik Cabang Pusat
5.1.07.02   Beban Listrik Cabang Bandung
```

### Multi-Produk (Online Seller)

Pisahkan pendapatan per kategori produk:

```
4.1       Pendapatan Usaha
4.1.01      Pendapatan Penjualan Smartphone
4.1.02      Pendapatan Penjualan Aksesoris
4.1.03      Pendapatan Penjualan Kabel
```

### Proyek (IT Service)

Tracking biaya per jenis proyek:

```
4.1       Pendapatan Usaha
4.1.01      Pendapatan Jasa Training
4.1.02      Pendapatan Jasa Konsultasi
4.1.03      Pendapatan Jasa Development
4.1.04      Pendapatan Jasa Maintenance
```

### Tambahan Rekening Bank

Tambahkan akun untuk setiap rekening bank yang digunakan:

```
1.1.02    Bank BCA
1.1.03    Bank Mandiri
1.1.04    Bank BNI
1.1.05    Bank BRI
1.1.06    Bank Permata
```

## Akun Sistem — Jangan Diubah

Beberapa akun digunakan oleh fitur internal Balaka. Jangan menghapus atau mengubah tipe akun ini:

| Akun | Digunakan Oleh |
|------|----------------|
| 2.1.10 Hutang Gaji | Fitur Payroll |
| 2.1.13 Hutang BPJS | Fitur Payroll |
| 2.1.20 Hutang PPh 21 | Fitur Payroll |
| 3.2.01 Laba Ditahan | Tutup Buku Akhir Tahun |
| 3.2.02 Laba Berjalan | Tutup Buku Akhir Tahun |

Nama akun boleh diganti, tetapi kode dan tipe harus tetap sama.

## Langkah Selanjutnya

[Import Data](04-data-import.md)
