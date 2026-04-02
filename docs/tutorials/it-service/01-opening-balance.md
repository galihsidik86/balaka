# Setoran Modal & Setup Awal

Sebelum mulai mencatat transaksi, ada dua hal yang perlu dilakukan: setoran modal awal dan memastikan data perusahaan terisi.

## Setoran Modal

Pemilik menyetor modal awal **Rp 500.000.000** ke rekening perusahaan.

Gunakan template **Setoran Modal**:

| Field | Isi |
|-------|-----|
| Template | Setoran Modal |
| Tanggal | 2025-01-02 |
| Jumlah | 500.000.000 |
| Deskripsi | Setoran modal awal tahun 2025 |
| Referensi | CAP-2025-001 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.02 Bank BCA | 500.000.000 | |
| 3.1.01 Modal Disetor | | 500.000.000 |

Bank bertambah (debit), modal bertambah (kredit).

Untuk memahami kenapa debit dan kredit seperti ini, lihat [Debit dan Kredit](../common/03-debit-credit.md).

## Verifikasi Setup

Setelah setoran modal, cek:

1. **Dashboard** — Saldo bank harus menunjukkan 500 juta
2. **Trial Balance** — 1 baris: Bank BCA (D) 500jt, Modal Disetor (C) 500jt
3. **Neraca** — Total Aset = Total Ekuitas = 500 juta

## Data Karyawan

Pastikan 5 karyawan sudah terdata dengan status PTKP yang benar:

| Karyawan | Jabatan | PTKP | Keterangan |
|----------|---------|------|-----------|
| Ahmad Fauzi | CTO | K/2 | Kawin, 2 tanggungan |
| Sari Wulandari | Project Manager | TK/0 | Belum kawin |
| Riko Pratama | Senior Developer | K/1 | Kawin, 1 tanggungan |
| Maya Anggraini | Business Analyst | TK/0 | Belum kawin |
| Dian Kusuma | QA Lead | K/0 | Kawin, tanpa tanggungan |

Status PTKP mempengaruhi perhitungan PPh 21. Lihat [Panduan Payroll](../common/06-payroll.md#pph-21-ter) untuk detail.

## Data Klien

Pastikan semua klien sudah terdata dengan NPWP yang benar — ini penting untuk pembuatan faktur pajak dan bukti potong.

## Langkah Selanjutnya

Modal sudah disetor, data sudah siap. Mulai mencatat transaksi:

- [Mencatat Pendapatan](02-recording-income.md) — catat invoice pertama dari klien
