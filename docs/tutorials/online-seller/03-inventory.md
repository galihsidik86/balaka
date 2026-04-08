# Inventori & Pembelian

Toko Gadget Sejahtera menjual barang fisik — perlu beli stok, simpan di gudang, dan sesekali stock opname untuk memastikan catatan sesuai kondisi fisik.

## Pembelian Barang Dagangan

Setiap kali beli stok dari supplier, gunakan template **Pembelian Barang Dagangan**.

### Contoh: Restock Smartphone Q1

| Field | Isi |
|-------|-----|
| Template | Pembelian Barang Dagangan |
| Tanggal | 2025-01-05 |
| Jumlah | 45.000.000 |
| Deskripsi | Pembelian stok smartphone Q1 |
| Referensi | PO-2025-001 |

Jurnal yang dihasilkan:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.20 Persediaan Barang Dagangan | 45.000.000 | |
| 1.1.02 Bank BCA | | 45.000.000 |

Persediaan bertambah (aset naik), bank berkurang (aset turun). Pembelian stok bukan beban — barang masih punya nilai sampai dijual.

![Form pembelian barang dagangan](screenshots/tutorials/online-seller/tx-form-pembelian-barang-dagangan.png)

## Jadwal Pembelian 2025

| Tanggal | Deskripsi | Jumlah | Referensi |
|---------|-----------|--------|-----------|
| 5 Jan | Stok smartphone Q1 | 45.000.000 | PO-2025-001 |
| 20 Feb | Restock aksesoris dan kabel | 30.000.000 | PO-2025-002 |
| 3 Mar | Restock smartphone Q1 | 50.000.000 | PO-2025-003 |
| 5 Mei | Restock earbuds dan charger | 35.000.000 | PO-2025-004 |
| 20 Jul | Restock smartphone midrange | 40.000.000 | PO-2025-005 |
| 3 Okt | Restock besar pra-promo 10.10 | 55.000.000 | PO-2025-006 |
| 1 Nov | Restock besar pra-promo 11.11 | 65.000.000 | PO-2025-007 |
| 1 Des | Restock besar pra-promo 12.12 + Natal | 70.000.000 | PO-2025-008 |
| **Total** | | **390.000.000** | |

Perhatikan pola: pembelian meningkat drastis di Q4 untuk menyiapkan stok musim promo (10.10, 11.11, 12.12).

## Katalog Produk (18 SKU)

Toko Gadget Sejahtera memiliki 18 produk terdaftar dalam 5 kategori:

| Kategori | SKU | Produk | Harga Jual |
|----------|-----|--------|-----------|
| Smartphone | PHONE-IP15 | iPhone 15 128GB | 16.500.000 |
| Smartphone | PHONE-SS24 | Samsung Galaxy S24 | 13.500.000 |
| Smartphone | PHONE-XI14 | Xiaomi 14 | 9.500.000 |
| Smartphone | PHONE-OPPO | OPPO Reno 11 | 6.500.000 |
| Aksesoris | ACC-SP | Screen Protector | 75.000 |
| Aksesoris | ACC-STAND | Phone Stand Holder | 150.000 |
| Aksesoris | ACC-PB10 | Power Bank 10000mAh | 350.000 |
| Aksesoris | ACC-WC | Wireless Charger 15W | 250.000 |
| Kabel & Charger | CBL-USBC | Kabel USB-C 1M | 65.000 |
| Kabel & Charger | CBL-FC | Fast Charger 33W | 175.000 |
| Kabel & Charger | CBL-CAR | Car Charger Dual Port | 150.000 |
| Kabel & Charger | CBL-MULTI | Kabel Multi 3in1 | 85.000 |
| Case | CASE-IP | Case iPhone 15 | 85.000 |
| Case | CASE-SS | Case Samsung S24 | 85.000 |
| Case | CASE-POUCH | Universal Pouch | 120.000 |
| Audio | AUD-TWS | TWS Earbuds | 450.000 |
| Audio | AUD-SPK | Bluetooth Speaker Mini | 350.000 |
| Audio | AUD-WIRE | Earphone Wired USB-C | 150.000 |

Semua produk menggunakan metode **Weighted Average** untuk kalkulasi HPP dan tracking persediaan.

## Stock Opname (Penyesuaian Persediaan)

Minimal sekali per kuartal, hitung fisik barang di gudang dan cocokkan dengan catatan di Balaka.

### Jika Stok Fisik Lebih Banyak dari Catatan

Gunakan template **Penyesuaian Persediaan Masuk**:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.20 Persediaan Barang Dagangan | jumlah selisih | |
| 4.9.01 Pendapatan Lain-lain | | jumlah selisih |

### Jika Stok Fisik Kurang dari Catatan (rusak/hilang)

Gunakan template **Penyesuaian Persediaan Keluar**:

| Akun | Debit | Kredit |
|------|-------|--------|
| 5.9.02 Beban Persediaan Rusak/Hilang | jumlah selisih | |
| 1.1.20 Persediaan Barang Dagangan | | jumlah selisih |

Barang rusak atau hilang dicatat sebagai beban — mengurangi laba.

## Saldo Persediaan Akhir Tahun

Per 31 Desember 2025:

| Akun | Saldo |
|------|-------|
| 1.1.20 Persediaan Barang Dagangan | 390.000.000 |

Angka ini muncul di neraca sebagai aset lancar.

## Tips

- **Catat pembelian segera** — begitu barang datang dan dibayar, langsung catat. Jangan tunggu akhir bulan
- **Simpan faktur supplier** — isi nomor faktur di field referensi untuk pelacakan
- **Stock opname rutin** — gadget kecil (kabel, case, screen protector) rawan hilang atau tertukar. Hitung fisik minimal tiap kuartal
- **Perhatikan minimum stock** — setiap produk punya batas minimum stok. Restock sebelum habis, terutama menjelang musim promo

## Langkah Selanjutnya

- [Pengeluaran Operasional](04-expenses.md) — ongkir, packing, iklan, sewa gudang
