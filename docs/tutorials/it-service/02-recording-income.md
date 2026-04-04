# Mencatat Pendapatan

PT Solusi Digital Nusantara sebagai perusahaan PKP memiliki 3 pola pencatatan pendapatan, tergantung tipe klien.

Untuk konsep dasar PPN dan PPh 23, lihat [Panduan PPN](../common/07-ppn.md) dan [Panduan PPh](../common/08-pph.md).

## Pola 1: Klien Swasta PKP (Grab)

Klien swasta yang bukan pemungut PPN. Anda memungut PPN 11% dan menerima pembayaran penuh.

**Template:** Pendapatan Jasa + PPN

**Contoh:** Invoice ke Grab, harga jual Rp 80.000.000

| Field | Isi |
|-------|-----|
| Template | Pendapatan Jasa + PPN |
| Tanggal | 2025-02-05 |
| Jumlah | 80.000.000 |
| Deskripsi | Driver Onboarding Platform - Feb |
| Referensi | INV-SDN-2025-003 |
| Proyek | PRJ-GRB-01 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit | Perhitungan |
|------|-------|--------|------------|
| Bank BCA | 88.800.000 | | 80jt × 1,11 |
| Pendapatan Jasa | | 80.000.000 | Harga jual |
| Hutang PPN | | 8.800.000 | 80jt × 11% |

Anda menerima Rp 88,8 juta (termasuk PPN). PPN 8,8 juta menjadi hutang yang harus disetor ke negara.

## Pola 2: Klien Korporat Besar (Mandiri, Telkom)

Klien korporat yang memotong PPh 23 (2%) dari pembayaran. Anda memungut PPN 11%, tapi klien menahan 2% sebagai PPh 23.

**Template:** Pendapatan Jasa + PPN + PPh 23

**Contoh:** Invoice ke Bank Mandiri, harga jual Rp 150.000.000

| Field | Isi |
|-------|-----|
| Template | Pendapatan Jasa + PPN + PPh 23 |
| Tanggal | 2025-01-10 |
| Jumlah | 150.000.000 |
| Deskripsi | Konsultasi Core Banking - Januari |
| Referensi | INV-SDN-2025-001 |
| Proyek | PRJ-MND-01 |

![Form pendapatan jasa dengan PPN dan PPh 23](screenshots/tutorials/it-service/tx-form-pendapatan-jasa-ppn-pph-23.png)

Jurnal yang dihasilkan:

| Akun | Debit | Kredit | Perhitungan |
|------|-------|--------|------------|
| Bank BCA | 163.500.000 | | 150jt × 1,09 |
| Kredit Pajak PPh 23 | 3.000.000 | | 150jt × 2% |
| Pendapatan Jasa | | 150.000.000 | Harga jual |
| Hutang PPN | | 16.500.000 | 150jt × 11% |

Anda menerima Rp 163,5 juta (harga jual + PPN 11% − PPh 23 2%). Selisih 2% menjadi **kredit pajak** yang bisa dikurangkan dari PPh Badan tahunan.

![Detail transaksi pendapatan jasa dengan PPN dan PPh 23](screenshots/tutorials/it-service/tx-detail-pendapatan-jasa-ppn-pph-23.png)

**Penting:** Minta bukti potong PPh 23 dari klien — ini dibutuhkan saat pelaporan SPT.

## Pola 3: Klien BUMN / Pemerintah (PLN, Kominfo)

Untuk BUMN/pemerintah, PPN **dipungut oleh pembeli** (Wapu, Faktur Pajak kode 03). Anda tidak menerima PPN dari klien. PPh 23 tetap dipotong.

**Template:** Pendapatan Jasa BUMN (FP 03)

**Contoh:** Invoice ke PLN, harga jual Rp 200.000.000

| Field | Isi |
|-------|-----|
| Template | Pendapatan Jasa BUMN (FP 03) |
| Tanggal | 2025-01-20 |
| Jumlah | 200.000.000 |
| Deskripsi | Smart Grid Analytics - Januari |
| Referensi | INV-SDN-2025-002 |
| Proyek | PRJ-PLN-01 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit | Perhitungan |
|------|-------|--------|------------|
| Bank BCA | 196.000.000 | | 200jt × 0,98 |
| Kredit Pajak PPh 23 | 4.000.000 | | 200jt × 2% |
| Pendapatan Jasa | | 200.000.000 | Harga jual |

Tidak ada Hutang PPN — PPN dipungut dan disetor oleh PLN langsung.

## Perbandingan 3 Pola

| | Swasta (+PPN) | Korporat (+PPN+PPh23) | BUMN (FP 03) |
|---|---------------|----------------------|---------------|
| Anda terima | 111% | 109% | 98% |
| PPN | Anda setor | Anda setor | Klien setor |
| PPh 23 | Tidak ada | Klien potong 2% | Klien potong 2% |
| Kredit pajak | Tidak ada | Ya (PPh 23) | Ya (PPh 23) |

## Jadwal Pendapatan 2025

| Bulan | Klien | Template | Jumlah |
|-------|-------|----------|--------|
| Jan | Mandiri | +PPN+PPh23 | 150.000.000 |
| Jan | PLN | BUMN FP03 | 200.000.000 |
| Feb | Grab | +PPN | 80.000.000 |
| Feb | Telkom | +PPN+PPh23 | 120.000.000 |
| Mar | Kominfo | BUMN FP03 | 180.000.000 |
| Apr | Mandiri | +PPN+PPh23 | 100.000.000 |
| ... | ... | ... | ... |
| **Total** | | | **2.110.000.000** |

## Langkah Selanjutnya

- [Mencatat Pengeluaran](03-paying-expenses.md) — cloud, sewa, telecom, operasional
