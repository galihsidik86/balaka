# Label Transaksi (Tags)

Referensi layar fitur label transaksi: tipe label, label, penandaan transaksi, filter, dan laporan ringkasan per label.

---

## Konsep

Label (tag) digunakan untuk menandai transaksi dengan kategori tambahan di luar chart of accounts. Setiap label memiliki **tipe label** (tag type) sebagai pengelompokan. Contoh:

- Tipe label: "Departemen" -- Label: Marketing, Engineering, Finance
- Tipe label: "Proyek" -- Label: Website Redesign, Mobile App
- Tipe label: "Sumber Dana" -- Label: Dana Hibah, Dana Operasional

Satu transaksi bisa memiliki banyak label dari berbagai tipe.

---

## Tipe Label

### Daftar Tipe Label

**Navigasi:** Sidebar > **Master Data** > **Label Transaksi**

**URL:** `/tags/types`

Tabel menampilkan semua tipe label. Setiap baris:

| Kolom | Keterangan |
|-------|------------|
| Kode | Kode unik tipe label (maks 20 karakter) |
| Nama | Nama deskriptif |
| Deskripsi | Keterangan tambahan |
| Status | Aktif / Nonaktif |

**Tombol:**
- **Tipe Label Baru** -- Membuka formulir pembuatan
- **Pencarian** -- Field pencarian dengan live search (HTMX, delay 300ms)

Klik nama tipe label untuk melihat daftar label di dalam tipe tersebut.

### Formulir Tipe Label

**URL:** `/tags/types/new` (baru) atau `/tags/types/{id}/edit` (edit)

**Permission:** `TAG_CREATE` (baru) atau `TAG_EDIT` (edit)

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Kode | Ya | Text | Kode unik, maks 20 karakter. Contoh: `DEPT` |
| Nama | Ya | Text | Nama tipe label, maks 100 karakter. Contoh: "Departemen" |
| Deskripsi | Tidak | Text | Keterangan, maks 255 karakter |
| Aktif | -- | Checkbox | Default: aktif |

**Tombol:**
- **Simpan** -- Menyimpan tipe label

**Validasi:**
- Kode harus unik -- jika sudah ada, form menampilkan pesan error "Kode sudah digunakan"

### Menghapus Tipe Label

Klik tombol **Hapus** pada tipe label. Soft delete -- tipe label dan label di dalamnya tidak lagi ditampilkan tapi data tetap tersimpan.

**Permission:** `TAG_DELETE`

Jika tipe label masih memiliki label yang digunakan di transaksi, penghapusan ditolak dengan pesan error.

---

## Label

### Daftar Label

**Navigasi:** Klik nama tipe label dari daftar tipe label

**URL:** `/tags/types/{tagTypeId}/tags`

Breadcrumb: **Label Transaksi** > **[Nama Tipe]**

Tabel menampilkan label di dalam tipe yang dipilih:

| Kolom | Keterangan |
|-------|------------|
| Kode | Kode unik label dalam tipe (maks 20 karakter) |
| Nama | Nama label |
| Deskripsi | Keterangan tambahan |
| Status | Aktif / Nonaktif |

**Tombol:**
- **Label Baru** -- Membuka formulir pembuatan
- **Pencarian** -- Field pencarian dengan live search (HTMX, delay 300ms)

### Formulir Label

**URL:** `/tags/types/{tagTypeId}/tags/new` (baru) atau `/tags/types/{tagTypeId}/tags/{id}/edit` (edit)

**Permission:** `TAG_CREATE` (baru) atau `TAG_EDIT` (edit)

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Kode | Ya | Text | Kode unik dalam tipe, maks 20 karakter. Contoh: `MKT` |
| Nama | Ya | Text | Nama label, maks 100 karakter. Contoh: "Marketing" |
| Deskripsi | Tidak | Text | Keterangan, maks 255 karakter |
| Aktif | -- | Checkbox | Default: aktif |

**Validasi:**
- Kode harus unik dalam satu tipe label (kombinasi `id_tag_type` + `code` unik)

### Menghapus Label

Klik tombol **Hapus**. Soft delete.

**Permission:** `TAG_DELETE`

Jika label masih digunakan di transaksi, penghapusan ditolak.

---

## Menandai Transaksi dengan Label

### Saat Membuat/Mengedit Transaksi

Di formulir transaksi (`/transactions/new` atau `/transactions/{id}/edit`), bagian bawah menampilkan panel **Label** yang dikelompokkan per tipe label.

Setiap tipe label ditampilkan sebagai heading, dengan checkbox untuk setiap label di dalamnya. Centang label yang relevan.

Label yang dipilih disimpan bersama transaksi saat klik **Simpan**.

### Filter Transaksi per Label

Di halaman daftar transaksi (`/transactions`), tersedia dropdown filter **Label** di samping filter status dan kategori.

Pilih label dari dropdown untuk menampilkan hanya transaksi yang memiliki label tersebut. Filter berlaku bersama filter lain (status, kategori, tanggal, proyek).

---

## Laporan Ringkasan per Label

**Navigasi:** Sidebar > **Laporan** > **Ringkasan per Label**

**URL:** `/reports/tag-summary`

**Permission:** `REPORT_VIEW` dan `TAG_VIEW`

### Filter

| Field | Keterangan |
|-------|------------|
| Tanggal Mulai | Awal periode (default: tanggal 1 bulan ini) |
| Tanggal Akhir | Akhir periode (default: hari ini) |

### Isi Laporan

Laporan dikelompokkan per tipe label. Untuk setiap tipe label:

**Header:** Nama tipe label

**Tabel:**

| Kolom | Keterangan |
|-------|------------|
| Label | Nama label |
| Transaksi | Jumlah transaksi yang memiliki label ini |
| Pendapatan | Total nominal transaksi pendapatan (hijau) |
| Pengeluaran | Total nominal transaksi pengeluaran (merah) |
| Netto | Selisih pendapatan - pengeluaran (hijau jika positif, merah jika negatif) |

**Footer:** Baris subtotal per tipe label.

Jika tidak ada transaksi dengan label pada periode yang dipilih, halaman menampilkan pesan "Tidak ada data transaksi dengan label pada periode ini".

---

## Hak Akses

| Permission | Keterangan |
|-----------|------------|
| `TAG_VIEW` | Melihat tipe label dan label |
| `TAG_CREATE` | Membuat tipe label dan label baru |
| `TAG_EDIT` | Mengubah tipe label dan label |
| `TAG_DELETE` | Menghapus tipe label dan label |
| `REPORT_VIEW` + `TAG_VIEW` | Melihat laporan ringkasan per label |

---

Selanjutnya: [Integrasi AI](07-ai-integration.md)
