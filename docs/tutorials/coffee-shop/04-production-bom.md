# Produksi & BOM (Bill of Materials)

Selain minuman kopi, Kedai Kopi Nusantara juga menjual produk bakery: croissant dan roti bakar coklat. Produk ini dibuat sendiri dari bahan baku menggunakan resep (BOM).

## Konsep BOM

BOM (Bill of Materials) adalah daftar bahan baku beserta jumlahnya yang dibutuhkan untuk membuat 1 batch produk jadi. Di Balaka, BOM digunakan untuk:

1. **Menghitung biaya produksi** — berapa biaya bahan baku per batch
2. **Memindahkan nilai persediaan** — dari Persediaan Bahan Baku ke Persediaan Barang Jadi
3. **Mengontrol stok** — memastikan bahan baku cukup sebelum produksi

## BOM Croissant (BOM-CRS)

Resep untuk 1 batch = **24 pcs** croissant:

| Bahan | Kode | Jumlah | Satuan |
|-------|------|--------|--------|
| Tepung Terigu | TEPUNG-TERIGU | 3 | kg |
| Butter | BUTTER | 1,5 | kg |
| Telur Ayam | TELUR | 12 | butir |
| Ragi Instan | RAGI | 10 | gram |
| Garam | GARAM | 6 | gram |
| Susu Segar | SUSU-SEGAR | 0,6 | liter |

Harga jual per pcs: **Rp 25.000**

## BOM Roti Bakar Coklat (BOM-RBC)

Resep untuk 1 batch = **20 pcs** roti bakar coklat:

| Bahan | Kode | Jumlah | Satuan |
|-------|------|--------|--------|
| Tepung Terigu | TEPUNG-TERIGU | 2,5 | kg |
| Butter | BUTTER | 1 | kg |
| Telur Ayam | TELUR | 10 | butir |
| Ragi Instan | RAGI | 8 | gram |
| Garam | GARAM | 5 | gram |
| Coklat Blok | COKLAT | 0,4 | kg |

Harga jual per pcs: **Rp 20.000**

## Alur Produksi

```
Persediaan Bahan Baku  →  Produksi  →  Persediaan Barang Jadi  →  Penjualan
  (1.1.20)                               (1.1.21)                  (4.1.01)
  kopi, susu, tepung,                     croissant,               customer
  butter, telur, dll                      roti bakar
```

### Langkah 1: Buat Production Order

Buka **Inventori → Produksi → Buat Baru**:

| Field | Isi |
|-------|-----|
| BOM | BOM Croissant (BOM-CRS) |
| Jumlah Batch | 1 |
| Tanggal | Sesuai tanggal produksi |

Sistem menampilkan kebutuhan bahan baku berdasarkan BOM.

### Langkah 2: Eksekusi Produksi

Klik **Eksekusi** — sistem membuat jurnal:

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.21 Persediaan Barang Jadi | (biaya bahan) | |
| 1.1.20 Persediaan Bahan Baku | | (biaya bahan) |

Nilai persediaan berpindah dari bahan baku ke barang jadi. Total aset tidak berubah — hanya berpindah antar akun persediaan.

### Langkah 3: Penjualan

Saat croissant atau roti terjual, dicatat melalui template **Penjualan Tunai + COGS** yang mengurangi Persediaan Barang Jadi dan mengakui HPP.

## Template: Produksi Barang Jadi

Jika ingin mencatat produksi secara manual tanpa fitur Production Order:

**Template:** Produksi Barang Jadi

| Akun | Debit | Kredit |
|------|-------|--------|
| 1.1.21 Persediaan Barang Jadi | (jumlah) | |
| 1.1.20 Persediaan Bahan Baku | | (jumlah) |

## Produk Master

Produk yang terdaftar di sistem:

### Bahan Baku

| Kode | Nama | Satuan | Akun Persediaan |
|------|------|--------|----------------|
| KOPI-ARABICA | Biji Kopi Arabica | kg | 1.1.20 |
| SUSU-SEGAR | Susu Segar | liter | 1.1.20 |
| GULA-AREN | Gula Aren Cair | liter | 1.1.20 |
| ES-BATU | Es Batu | kg | 1.1.20 |
| TEPUNG-TERIGU | Tepung Terigu | kg | 1.1.20 |
| BUTTER | Butter | kg | 1.1.20 |
| TELUR | Telur Ayam | butir | 1.1.20 |
| RAGI | Ragi Instan | gram | 1.1.20 |
| GARAM | Garam | gram | 1.1.20 |
| COKLAT | Coklat Blok | kg | 1.1.20 |

### Barang Jadi

| Kode | Nama | Satuan | Harga Jual | Akun Persediaan |
|------|------|--------|-----------|----------------|
| CROISSANT | Croissant | pcs | 25.000 | 1.1.21 |
| ROTI-COKLAT | Roti Bakar Coklat | pcs | 20.000 | 1.1.21 |

Semua produk menggunakan metode costing **Weighted Average** — harga bahan baku dihitung rata-rata tertimbang dari semua pembelian.

## Tips

- **Produksi sesuai demand** — jangan overproduksi karena bakery cepat basi
- **Cek stok bahan** — pastikan bahan baku cukup sebelum memulai produksi
- **Konsistensi resep** — jika resep berubah (misalnya menambah butter), update BOM di Balaka agar biaya produksi akurat

## Langkah Selanjutnya

- [Pengeluaran Operasional](05-expenses.md) — sewa, listrik, dan beban lainnya
