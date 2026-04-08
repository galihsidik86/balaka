# Hibah dan Donasi

Selain pendapatan dari mahasiswa, kampus menerima dana dari pemerintah (hibah), donatur, dan yayasan pengelola.

## Terima Hibah — Kemendikbud MBKM

STMIK Merdeka Digital menerima **hibah Kemendikbud program MBKM** sebesar Rp 500.000.000 di bulan Maret 2025.

| Field | Isi |
|-------|-----|
| Template | Terima Hibah |
| Tanggal | 2025-03-15 |
| Jumlah | 500.000.000 |
| Deskripsi | Hibah Kemendikbud program MBKM |
| Referensi | HIBAH-2025-01 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.02 Bank BCA | 500.000.000 | |
| 4.2.01 Pendapatan Hibah | | 500.000.000 |

Bank bertambah, pendapatan hibah diakui. Hibah masuk sebagai pendapatan lain-lain (bukan pendapatan operasional SPP).

![Form penerimaan hibah](screenshots/tutorials/campus/tx-form-terima-hibah.png)

### Hibah vs Pendapatan SPP

| Aspek | Pendapatan SPP | Pendapatan Hibah |
|-------|---------------|-----------------|
| Sumber | Mahasiswa | Pemerintah/pihak ketiga |
| Sifat | Rutin, setiap semester | Tidak tetap, tergantung proposal |
| Piutang | Ada (tagihan dulu, bayar kemudian) | Tidak ada (langsung cair ke bank) |
| Akun pendapatan | 4.1.01 Pendapatan SPP | 4.2.01 Pendapatan Hibah |
| Pelaporan | Pendapatan Operasional | Pendapatan Lain-lain |

Hibah dicatat saat dana diterima di rekening — tidak ada proses tagihan dan piutang.

### Kewajiban Pelaporan Hibah

Hibah dari Kemendikbud memiliki kewajiban pelaporan penggunaan dana. Pastikan setiap pengeluaran yang didanai hibah diberi referensi yang jelas untuk audit.

## Terima Donasi

Template **Terima Donasi** tersedia untuk mencatat sumbangan dari alumni atau donatur. Jurnalnya serupa dengan hibah — masuk ke rekening bank dan diakui sebagai pendapatan.

Dalam demo data 2025, STMIK Merdeka Digital tidak menerima donasi. Template ini tersedia untuk digunakan saat ada donasi masuk.

## Setoran Modal Yayasan

Yayasan Merdeka Digital menyetor dana operasional ke kampus. Ini bukan pendapatan — ini adalah tambahan modal dari pemilik (yayasan).

| Field | Isi |
|-------|-----|
| Template | Setoran Modal Yayasan |
| Tanggal | 2025-01-02 |
| Jumlah | 200.000.000 |
| Deskripsi | Dana operasional dari yayasan Q1 |
| Referensi | YAY-2025-Q1 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.02 Bank BCA | 200.000.000 | |
| 3.1.01 Modal Yayasan | | 200.000.000 |

Bank bertambah, modal bertambah. Setoran modal **tidak masuk laporan laba rugi** — ini dicatat di neraca sebagai ekuitas.

![Form setoran modal yayasan](screenshots/tutorials/campus/tx-form-setoran-modal-yayasan.png)

### Perbedaan Modal vs Hibah vs Pendapatan

| Jenis | Akun | Laporan | Pengaruh |
|-------|------|---------|----------|
| Setoran Modal | 3.1.01 Modal Yayasan | Neraca (Ekuitas) | Menambah modal |
| Hibah | 4.2.01 Pendapatan Hibah | Laba Rugi (Pendapatan Lain) | Menambah surplus |
| SPP | 4.1.01 Pendapatan SPP | Laba Rugi (Pendapatan Utama) | Menambah surplus |

Setoran modal dari yayasan adalah investasi pemilik — meskipun menambah saldo bank, ini bukan pendapatan dan tidak masuk perhitungan surplus/defisit.

## Langkah Selanjutnya

- [Beasiswa](04-scholarships.md) — pemberian beasiswa yang mengurangi piutang mahasiswa
