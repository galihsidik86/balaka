# Menerima Pembayaran

Setelah tagihan dibuat, mahasiswa membayar secara cicilan. Setiap pembayaran mengurangi piutang SPP.

## Pembayaran SPP — Cicilan per Angkatan

Pembayaran SPP dicatat agregat per angkatan. Dalam praktik, bagian keuangan mengumpulkan pembayaran dari banyak mahasiswa dan mencatat totalnya dalam satu transaksi.

### Contoh: Angkatan 2023 Semester Genap

Tagihan SPP Angkatan 2023 Genap: **Rp 900.000.000**

Pembayaran dicicil 3 kali:

| Cicilan | Tanggal | Jumlah | Sisa Piutang |
|---------|---------|--------|-------------|
| Cicilan 1 | 2025-01-20 | 450.000.000 | 450.000.000 |
| Cicilan 2 | 2025-02-05 | 300.000.000 | 150.000.000 |
| Cicilan 3 | 2025-03-05 | 150.000.000 | 0 |

Angkatan 2023 semester genap lunas di bulan Maret.

### Cara Mencatat

Gunakan template **Pembayaran SPP**:

| Field | Isi |
|-------|-----|
| Template | Pembayaran SPP |
| Tanggal | 2025-01-20 |
| Jumlah | 450.000.000 |
| Deskripsi | Pembayaran SPP Angkatan 2023 - Cicilan 1 |
| Referensi | RCV-SPP-2023-01 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.02 Bank BCA | 450.000.000 | |
| 1.1.10 Piutang SPP Mahasiswa | | 450.000.000 |

Bank bertambah (debit), piutang berkurang (kredit).

### Angkatan 2024 Semester Genap

Tagihan: **Rp 900.000.000**

| Cicilan | Tanggal | Jumlah | Sisa Piutang |
|---------|---------|--------|-------------|
| Cicilan 1 | 2025-01-25 | 400.000.000 | 500.000.000 |
| Cicilan 2 | 2025-02-10 | 350.000.000 | 150.000.000 |
| Cicilan 3 | 2025-03-10 | 150.000.000 | 0 |

## Pembayaran SPP — Semester Ganjil

Di semester ganjil, ada 3 angkatan. Pola cicilan serupa tetapi tidak semua angkatan melunasi:

### Angkatan 2023

| Cicilan | Tanggal | Jumlah |
|---------|---------|--------|
| Cicilan 1 | 2025-07-20 | 500.000.000 |
| Cicilan 2 | 2025-08-15 | 300.000.000 |
| Cicilan 3 | 2025-10-05 | 100.000.000 |
| **Total dibayar** | | **900.000.000** |

Angkatan 2023 lunas.

### Angkatan 2024

| Cicilan | Tanggal | Jumlah |
|---------|---------|--------|
| Cicilan 1 | 2025-07-25 | 450.000.000 |
| Cicilan 2 | 2025-09-15 | 250.000.000 |
| **Total dibayar** | | **700.000.000** |

Sisa piutang: 900M - 700M = **200.000.000** (belum lunas di akhir tahun).

### Angkatan 2025 (Maba)

| Cicilan | Tanggal | Jumlah |
|---------|---------|--------|
| Cicilan 1 | 2025-08-10 | 300.000.000 |
| Cicilan 2 | 2025-09-10 | 400.000.000 |
| Cicilan 3 | 2025-11-05 | 200.000.000 |
| **Total dibayar** | | **900.000.000** |

Angkatan 2025 lunas.

## Pembayaran Uang Pangkal

Uang pangkal Angkatan 2025: **Rp 1.800.000.000**

| Cicilan | Tanggal | Jumlah |
|---------|---------|--------|
| Cicilan 1 | 2025-07-15 | 900.000.000 |
| Cicilan 2 | 2025-08-05 | 600.000.000 |
| Cicilan 3 | 2025-09-05 | 300.000.000 |
| **Total** | | **1.800.000.000** |

Uang pangkal lunas di September.

## Pembayaran Biaya Praktikum

### Semester Genap

| Angkatan | Cicilan | Tanggal | Jumlah |
|----------|---------|---------|--------|
| 2023 | Cicilan 1 | 2025-02-15 | 60.000.000 |
| 2023 | Cicilan 2 | 2025-05-10 | 60.000.000 |
| 2024 | Cicilan 1 | 2025-03-20 | 60.000.000 |
| 2024 | Cicilan 2 | 2025-06-10 | 60.000.000 |

### Semester Ganjil

| Cicilan | Tanggal | Jumlah |
|---------|---------|--------|
| Cicilan 1 | 2025-10-10 | 80.000.000 |
| Cicilan 2 | 2025-11-10 | 40.000.000 |

Total pembayaran praktikum ganjil: 120.000.000 (lunas).

## Piutang Outstanding di Akhir Tahun

Tidak semua mahasiswa membayar tepat waktu. Per 31 Desember 2025:

| Akun | Saldo |
|------|-------|
| 1.1.10 Piutang SPP Mahasiswa | 160.000.000 |

Piutang ini berasal dari Angkatan 2024 semester ganjil yang belum lunas (200M ditagih, baru 700M + beasiswa terbayar).

Piutang SPP yang outstanding perlu dilaporkan ke yayasan dan menjadi perhatian di semester berikutnya. Bagian keuangan perlu menindaklanjuti penagihan ke mahasiswa yang menunggak.

## Rekap Arus Penerimaan 2025

| Sumber | Ditagihkan | Dibayar | Outstanding |
|--------|-----------|---------|------------|
| SPP (semua angkatan) | 4.500.000.000 | 4.300.000.000 | 160.000.000 |
| Uang Pangkal | 1.800.000.000 | 1.800.000.000 | 0 |
| Biaya Praktikum | 360.000.000 | 360.000.000 | 0 |
| **Total** | **6.660.000.000** | **6.460.000.000** | **160.000.000** |

Selisih 40M antara ditagihkan dan dibayar+outstanding adalah beasiswa yang mengurangi piutang (lihat [Beasiswa](04-scholarships.md)).

## Langkah Selanjutnya

- [Hibah dan Donasi](03-grants-donations.md) — sumber pendanaan dari luar mahasiswa
