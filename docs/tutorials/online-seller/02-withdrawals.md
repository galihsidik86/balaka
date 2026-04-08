# Withdraw Saldo Marketplace

Setelah penjualan dicatat, uang masih "terjebak" di akun Saldo Marketplace. Withdraw memindahkan saldo tersebut ke rekening Bank BCA.

## Konsep

Withdraw adalah **transfer antar akun**, bukan pendapatan baru. Tidak ada beban atau pendapatan yang berubah — hanya perpindahan lokasi uang:

- Saldo Marketplace berkurang
- Bank BCA bertambah

## Template Withdraw

Masing-masing marketplace punya template sendiri:

| Template | Dari Akun | Ke Akun |
|----------|----------|--------|
| Withdraw Saldo Tokopedia | 1.1.03 Saldo Tokopedia | 1.1.02 Bank BCA |
| Withdraw Saldo Shopee | 1.1.04 Saldo Shopee | 1.1.02 Bank BCA |
| Withdraw Saldo TikTok Shop | 1.1.06 Saldo TikTok Shop | 1.1.02 Bank BCA |

## Contoh: Withdraw Tokopedia Januari

Penjualan Tokopedia Januari: grossSales 25 juta, adminFee 1,25 juta → Saldo Tokopedia = 23,75 juta.

| Field | Isi |
|-------|-----|
| Template | Withdraw Saldo Tokopedia |
| Tanggal | 2025-01-25 |
| Jumlah | 23.750.000 |
| Deskripsi | Withdraw saldo Tokopedia Januari |
| Referensi | WD-TOKPED-0101 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.02 Bank BCA | 23.750.000 | |
| 1.1.03 Saldo Tokopedia | | 23.750.000 |

Setelah withdraw: Saldo Tokopedia = 0, Bank BCA bertambah 23,75 juta.

![Form withdraw saldo Tokopedia](screenshots/tutorials/online-seller/tx-form-withdraw-saldo-tokopedia.png)

## Contoh: Withdraw Shopee Januari

| Field | Isi |
|-------|-----|
| Template | Withdraw Saldo Shopee |
| Tanggal | 2025-01-25 |
| Jumlah | 16.920.000 |
| Deskripsi | Withdraw saldo Shopee Januari |
| Referensi | WD-SHOPEE-0101 |

Jurnal:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.02 Bank BCA | 16.920.000 | |
| 1.1.04 Saldo Shopee | | 16.920.000 |

## Contoh: Withdraw TikTok Shop Januari

| Field | Isi |
|-------|-----|
| Template | Withdraw Saldo TikTok Shop |
| Tanggal | 2025-01-25 |
| Jumlah | 11.520.000 |
| Deskripsi | Withdraw TikTok Januari |
| Referensi | WD-TIKTOK-0103 |

Jurnal:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.02 Bank BCA | 11.520.000 | |
| 1.1.06 Saldo TikTok Shop | | 11.520.000 |

## Kapan Harus Withdraw

Withdraw biasanya dilakukan setelah settlement marketplace diproses. Di demo data, semua withdraw dilakukan tanggal 25 setiap bulan.

**Rekomendasi:**
- Withdraw minimal 1x per bulan
- Jangan biarkan saldo menumpuk di marketplace — saldo di marketplace bukan penghasilan bank Anda
- Sebelum withdraw, pastikan penjualan bulan tersebut sudah dicatat

## Saldo Marketplace yang Belum di-Withdraw

Di akhir tahun 2025, ada saldo yang masih tertahan:

| Akun | Saldo 31 Des 2025 | Keterangan |
|------|-------------------|-----------|
| Saldo Tokopedia | 0 | Semua sudah ditarik |
| Saldo Shopee | 31.020.000 | Shopee April belum ditarik |
| Saldo TikTok Shop | 0 | Semua sudah ditarik |

Saldo Shopee 31 juta ini tetap muncul di neraca sebagai aset lancar. Uang ini milik Anda, tapi belum masuk ke Bank BCA.

**Perhatian:** Jika ada saldo marketplace yang tidak pernah di-withdraw, periksa apakah ada masalah (dispute, refund pending, atau lupa tarik).

## Verifikasi

Setelah semua penjualan dan withdraw bulan ini dicatat:

1. Buka **Trial Balance**
2. Cek akun Saldo Marketplace (1.1.03, 1.1.04, 1.1.06):
   - Jika 0 → semua saldo sudah ditarik
   - Jika > 0 → ada saldo yang belum di-withdraw (pastikan memang belum jatuh tempo settlement)

## Langkah Selanjutnya

- [Inventori & Pembelian](03-inventory.md) — beli stok barang dagangan
