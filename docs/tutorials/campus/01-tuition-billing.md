# Tagihan Semester

Siklus keuangan kampus mengikuti kalender akademik. Setiap semester dimulai dengan pembuatan tagihan SPP, uang pangkal, dan biaya praktikum.

## Kalender Akademik

| Semester | Periode | Keterangan |
|----------|---------|-----------|
| Genap 2024/2025 | Januari - Juni 2025 | Angkatan 2023 dan 2024 |
| Ganjil 2025/2026 | Juli - Desember 2025 | Angkatan 2023, 2024, dan 2025 (maba) |

Awal semester adalah momen tagihan dibuat. Mahasiswa kemudian membayar secara cicilan sepanjang semester.

## Tagihan SPP

SPP (Sumbangan Pembinaan Pendidikan) ditagihkan per angkatan, per semester. Setiap angkatan (120 mahasiswa) ditagih total **Rp 900.000.000** per semester.

### Semester Genap (Januari)

Tagihan dibuat di awal Januari untuk 2 angkatan:

**Angkatan 2023:**

| Field | Isi |
|-------|-----|
| Template | Tagihan SPP Mahasiswa |
| Tanggal | 2025-01-10 |
| Jumlah | 900.000.000 |
| Deskripsi | Tagihan SPP Angkatan 2023 - Genap 2024/2025 |
| Referensi | SPP-2023-GNP |

**Angkatan 2024:**

| Field | Isi |
|-------|-----|
| Template | Tagihan SPP Mahasiswa |
| Tanggal | 2025-01-10 |
| Jumlah | 900.000.000 |
| Deskripsi | Tagihan SPP Angkatan 2024 - Genap 2024/2025 |
| Referensi | SPP-2024-GNP |

Jurnal yang dihasilkan (per tagihan):

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.10 Piutang SPP Mahasiswa | 900.000.000 | |
| 4.1.01 Pendapatan SPP | | 900.000.000 |

Piutang bertambah (debit), pendapatan diakui (kredit). Pendapatan diakui saat tagihan dibuat — bukan saat uang diterima. Ini adalah prinsip akrual. Lihat [Debit dan Kredit](../common/03-debit-credit.md) untuk penjelasan konsep ini.

### Semester Ganjil (Juli)

Di Juli, 3 angkatan ditagihkan SPP:

| Angkatan | Jumlah | Referensi |
|----------|--------|-----------|
| 2023 | 900.000.000 | SPP-2023-GNJ |
| 2024 | 900.000.000 | SPP-2024-GNJ |
| 2025 (maba) | 900.000.000 | SPP-2025-GNJ |

Total tagihan SPP tahun 2025: **4.500.000.000** (5 tagihan x 900M).

## Tagihan Uang Pangkal

Uang pangkal ditagihkan satu kali untuk mahasiswa baru. Angkatan 2025 (120 mahasiswa) masuk di semester Ganjil:

| Field | Isi |
|-------|-----|
| Template | Tagihan Uang Pangkal |
| Tanggal | 2025-07-10 |
| Jumlah | 1.800.000.000 |
| Deskripsi | Tagihan Uang Pangkal Angkatan 2025 |
| Referensi | UP-2025 |

Jurnal:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.10 Piutang SPP Mahasiswa | 1.800.000.000 | |
| 4.1.02 Pendapatan Uang Pangkal | | 1.800.000.000 |

## Tagihan Biaya Praktikum

Biaya praktikum laboratorium ditagihkan per semester, Rp 120.000.000 per angkatan.

### Semester Genap

| Angkatan | Tanggal | Jumlah | Referensi |
|----------|---------|--------|-----------|
| 2023 | 2025-01-15 | 120.000.000 | PRKT-2023-GNP |
| 2024 | 2025-01-15 | 120.000.000 | PRKT-2024-GNP |

### Semester Ganjil

Tagihan praktikum semester ganjil dibuat agregat untuk semua angkatan:

| Field | Isi |
|-------|-----|
| Template | Tagihan Biaya Praktikum |
| Tanggal | 2025-09-20 |
| Jumlah | 120.000.000 |
| Deskripsi | Tagihan Praktikum semua angkatan - Ganjil |
| Referensi | PRKT-ALL-GNJ |

Total tagihan praktikum tahun 2025: **360.000.000**.

## Pola Template: Tagihan dan Pembayaran

Setiap jenis pendapatan menggunakan **2 template berpasangan**:

| Tagihan (INCOME) | Pembayaran (RECEIPT) |
|-------------------|---------------------|
| Tagihan SPP Mahasiswa | Pembayaran SPP |
| Tagihan Uang Pangkal | Pembayaran Uang Pangkal |
| Tagihan Biaya Praktikum | Pembayaran Biaya Praktikum |

Template tagihan **menambah piutang** dan **mengakui pendapatan**.
Template pembayaran **mengurangi piutang** dan **menambah kas/bank**.

Alur lengkap:

```
Tagihan SPP             Pembayaran SPP
─────────────           ──────────────
D: Piutang SPP          D: Bank BCA
C: Pendapatan SPP       C: Piutang SPP
```

Piutang naik saat tagihan dibuat, turun saat pembayaran diterima.

![Form tagihan SPP mahasiswa](screenshots/tutorials/campus/tx-form-tagihan-spp-mahasiswa.png)

![Form tagihan uang pangkal](screenshots/tutorials/campus/tx-form-tagihan-uang-pangkal.png)

## Rekap Total Tagihan 2025

| Jenis | Semester Genap | Semester Ganjil | Total |
|-------|---------------|----------------|-------|
| SPP | 1.800.000.000 | 2.700.000.000 | 4.500.000.000 |
| Uang Pangkal | — | 1.800.000.000 | 1.800.000.000 |
| Praktikum | 240.000.000 | 120.000.000 | 360.000.000 |
| **Total** | **2.040.000.000** | **4.620.000.000** | **6.660.000.000** |

Semester Ganjil lebih besar karena ada 3 angkatan (termasuk maba) dan tagihan uang pangkal.

## Langkah Selanjutnya

- [Menerima Pembayaran](02-receiving-payments.md) — bagaimana piutang berkurang saat SPP dicicil
