# Beasiswa

STMIK Merdeka Digital memberikan 2 jenis beasiswa: beasiswa prestasi dan beasiswa tidak mampu. Beasiswa mengurangi piutang SPP mahasiswa — bukan pembayaran tunai ke mahasiswa.

## Beasiswa Prestasi

Beasiswa untuk mahasiswa berprestasi. Diberikan Rp 7.500.000 per semester untuk 3 mahasiswa (Rp 2.500.000 per mahasiswa).

### Semester Genap

| Field | Isi |
|-------|-----|
| Template | Beasiswa Prestasi |
| Tanggal | 2025-04-15 |
| Jumlah | 7.500.000 |
| Deskripsi | Beasiswa prestasi semester genap (3 mahasiswa) |
| Referensi | BEA-P-2025-GNP |

### Semester Ganjil

| Field | Isi |
|-------|-----|
| Template | Beasiswa Prestasi |
| Tanggal | 2025-10-15 |
| Jumlah | 7.500.000 |
| Deskripsi | Beasiswa prestasi semester ganjil (3 mahasiswa) |
| Referensi | BEA-P-2025-GNJ |

Total beasiswa prestasi tahun 2025: **15.000.000** (2 semester x 7,5M).

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 5.3.01 Beban Beasiswa Prestasi | 7.500.000 | |
| 1.1.10 Piutang SPP Mahasiswa | | 7.500.000 |

Beban beasiswa bertambah (debit), piutang mahasiswa berkurang (kredit). Mahasiswa penerima beasiswa tidak perlu membayar sebagian SPP-nya.

![Form beasiswa prestasi](screenshots/tutorials/campus/tx-form-beasiswa-prestasi.png)

## Beasiswa Tidak Mampu

Beasiswa untuk mahasiswa kurang mampu. Diberikan Rp 12.500.000 per semester untuk 5 mahasiswa (Rp 2.500.000 per mahasiswa).

### Semester Genap

| Field | Isi |
|-------|-----|
| Template | Beasiswa Tidak Mampu |
| Tanggal | 2025-04-15 |
| Jumlah | 12.500.000 |
| Deskripsi | Beasiswa tidak mampu semester genap (5 mahasiswa) |
| Referensi | BEA-TM-2025-GNP |

### Semester Ganjil

| Field | Isi |
|-------|-----|
| Template | Beasiswa Tidak Mampu |
| Tanggal | 2025-10-15 |
| Jumlah | 12.500.000 |
| Deskripsi | Beasiswa tidak mampu semester ganjil (5 mahasiswa) |
| Referensi | BEA-TM-2025-GNJ |

Total beasiswa tidak mampu tahun 2025: **25.000.000** (2 semester x 12,5M).

![Form beasiswa tidak mampu](screenshots/tutorials/campus/tx-form-beasiswa-tidak-mampu.png)

## Mekanisme Beasiswa

Beasiswa **bukan pembayaran tunai** dari kampus ke mahasiswa. Beasiswa adalah **penghapusan piutang** — kampus mencoret sebagian tagihan SPP mahasiswa penerima beasiswa.

```
Tagihan SPP (Piutang naik)     Beasiswa (Piutang turun)
──────────────────────────     ─────────────────────────
D: Piutang SPP                 D: Beban Beasiswa
C: Pendapatan SPP              C: Piutang SPP
```

Alurnya:
1. Awal semester: tagihan SPP dibuat untuk seluruh angkatan (termasuk penerima beasiswa)
2. Pertengahan semester: beasiswa diproses, piutang penerima beasiswa dikurangi
3. Sepanjang semester: mahasiswa non-beasiswa membayar cicilan

## Dampak di Laporan

### Laba Rugi

Beasiswa muncul sebagai beban:

| Akun | Jumlah/Tahun |
|------|-------------|
| 5.3.01 Beban Beasiswa Prestasi | 15.000.000 |
| 5.3.02 Beban Beasiswa Tidak Mampu | 25.000.000 |
| **Total Beban Beasiswa** | **40.000.000** |

### Piutang

Beasiswa mengurangi total piutang yang harus ditagih. Dari total tagihan SPP 4.500M:

| Komponen | Jumlah |
|----------|--------|
| Total tagihan SPP | 4.500.000.000 |
| Dikurangi: pembayaran SPP | (4.300.000.000) |
| Dikurangi: beasiswa | (40.000.000) |
| **Sisa piutang** | **160.000.000** |

## Rekap Beasiswa per Semester

| Semester | Prestasi | Tidak Mampu | Total |
|----------|----------|-------------|-------|
| Genap 2024/2025 | 7.500.000 | 12.500.000 | 20.000.000 |
| Ganjil 2025/2026 | 7.500.000 | 12.500.000 | 20.000.000 |
| **Total 2025** | **15.000.000** | **25.000.000** | **40.000.000** |

## Langkah Selanjutnya

- [Payroll Kampus](05-faculty-payroll.md) — penggajian pimpinan, dosen, dan staff
