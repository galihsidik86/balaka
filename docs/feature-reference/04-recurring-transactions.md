# Transaksi Berulang

Referensi layar fitur transaksi berulang: pembuatan jadwal, frekuensi, eksekusi otomatis, dan pengelolaan jadwal.

---

## Daftar Transaksi Berulang

**Navigasi:** Sidebar > **Akuntansi** > **Transaksi Berulang**

**URL:** `/recurring`

### Kartu Ringkasan

Tiga kartu di bagian atas halaman:
- **Aktif** -- Jumlah transaksi berulang yang aktif (badge hijau)
- **Dijeda** -- Jumlah yang dihentikan sementara (badge kuning)
- **Selesai** -- Jumlah yang sudah selesai (badge abu-abu)

### Filter

| Field | Keterangan |
|-------|------------|
| Status | Filter berdasarkan status: Semua, Aktif, Dijeda, Selesai |

### Tabel

| Kolom | Keterangan |
|-------|------------|
| Nama | Nama deskriptif transaksi berulang |
| Template | Template jurnal yang digunakan |
| Frekuensi | Harian, Mingguan, Bulanan, Triwulanan, Tahunan |
| Jumlah | Nominal transaksi (Rp) |
| Jadwal Berikut | Tanggal eksekusi berikutnya |
| Total Eksekusi | Berapa kali sudah dieksekusi |
| Status | Badge berwarna (Aktif/Dijeda/Selesai) |

**Tombol:**
- **+ Transaksi Berulang Baru** -- Membuka formulir pembuatan

---

## Membuat Transaksi Berulang

**Navigasi:** Daftar Transaksi Berulang > **+ Transaksi Berulang Baru**

**URL:** `/recurring/new`

### Field Formulir

| Field | Wajib | Tipe Input | Keterangan |
|-------|-------|------------|------------|
| Nama | Ya | Text | Nama deskriptif, contoh: "Sewa Kantor Bulanan" |
| Template Jurnal | Ya | Dropdown | Template yang digunakan untuk membuat transaksi |
| Jumlah (Rp) | Ya | Number | Nominal transaksi |
| Deskripsi | Ya | Text | Deskripsi yang akan muncul di transaksi yang dibuat |
| Frekuensi | Ya | Dropdown | Harian, Mingguan, Bulanan, Triwulanan, Tahunan |
| Tanggal | Kondisional | Number (1-28) | Tanggal eksekusi -- tampil jika frekuensi Bulanan, Triwulanan, atau Tahunan |
| Hari | Kondisional | Dropdown | Hari eksekusi (Senin-Minggu) -- tampil jika frekuensi Mingguan |
| Tanggal Mulai | Ya | Date | Tanggal pertama eksekusi |
| Tanggal Selesai | Tidak | Date | Tanggal terakhir eksekusi. Kosongkan jika tanpa batas waktu |
| Maks. Eksekusi | Tidak | Number | Jumlah maksimum eksekusi. Kosongkan jika tanpa batas |

### Opsi Tambahan

| Opsi | Tipe | Keterangan |
|------|------|------------|
| Lewati Akhir Pekan | Checkbox | Jika jadwal jatuh pada Sabtu/Minggu, eksekusi dipindah ke Senin berikutnya |
| Posting Otomatis | Checkbox | Jika dicentang, transaksi langsung POSTED. Jika tidak, disimpan sebagai DRAFT |

**Tombol:**
- **Simpan** -- Membuat transaksi berulang dengan status ACTIVE

---

## Detail Transaksi Berulang

**URL:** `/recurring/{id}`

### Informasi

| Field | Keterangan |
|-------|------------|
| Nama | Nama transaksi berulang |
| Template | Template jurnal yang digunakan |
| Jumlah | Nominal (Rp) |
| Frekuensi | Jadwal eksekusi |
| Deskripsi | Deskripsi transaksi |
| Tanggal Mulai | Tanggal pertama |
| Tanggal Selesai | Tanggal terakhir (jika ada) |
| Jadwal Berikut | Tanggal eksekusi berikutnya |
| Total Eksekusi | Berapa kali sudah dieksekusi |
| Status | ACTIVE, PAUSED, atau COMPLETED |

### Jadwal 5 Eksekusi Berikutnya

Tabel preview menampilkan 5 tanggal eksekusi mendatang berdasarkan frekuensi dan konfigurasi saat ini.

### Riwayat Eksekusi

Tabel log eksekusi menampilkan:

| Kolom | Keterangan |
|-------|------------|
| Tanggal | Tanggal eksekusi |
| Status | Berhasil (hijau), Gagal (merah), Dilewati (kuning) |
| Transaksi | Link ke transaksi yang dibuat (jika berhasil) |
| Pesan | Pesan error (jika gagal) atau alasan dilewati |

### Tombol Aksi

| Tombol | Tersedia di Status | Hasil |
|--------|--------------------|-------|
| Edit | ACTIVE, PAUSED | Membuka formulir edit |
| Jeda | ACTIVE | Menghentikan sementara eksekusi, status berubah ke PAUSED |
| Lanjutkan | PAUSED | Melanjutkan eksekusi, jadwal dihitung ulang dari hari ini |
| Selesaikan | ACTIVE, PAUSED | Menghentikan permanen, status berubah ke COMPLETED |
| Hapus | Semua | Soft delete -- transaksi yang sudah dibuat tidak terpengaruh |

---

## Scheduler Otomatis

Scheduler berjalan otomatis setiap hari pukul **05:00**. Proses:

1. Query semua transaksi berulang ACTIVE dengan `nextRunDate <= hari ini`
2. Untuk setiap item:
   - Cek opsi "Lewati Akhir Pekan" -- jika tanggal jatuh pada Sabtu/Minggu, pindah ke Senin
   - Buat transaksi baru dari template + jumlah + deskripsi
   - Jika "Posting Otomatis" aktif: posting transaksi
   - Catat log eksekusi (SUCCESS)
   - Update `nextRunDate`, `lastRunDate`, `totalRuns`
3. Jika `maxOccurrences` tercapai atau `nextRunDate` melewati `endDate`: status berubah ke COMPLETED
4. Jika terjadi error: catat log (FAILED), lanjut ke item berikutnya

### Status Log Eksekusi

| Status | Keterangan |
|--------|------------|
| SUCCESS | Transaksi berhasil dibuat |
| FAILED | Terjadi error saat pembuatan |
| SKIPPED | Dilewati karena jatuh pada akhir pekan |

---

## Frekuensi yang Didukung

| Frekuensi | Keterangan |
|-----------|------------|
| DAILY (Harian) | Setiap hari |
| WEEKLY (Mingguan) | Hari tertentu dalam minggu (Senin-Minggu) |
| MONTHLY (Bulanan) | Tanggal tertentu dalam bulan (1-28) |
| QUARTERLY (Triwulanan) | Setiap 3 bulan pada tanggal tertentu |
| YEARLY (Tahunan) | Setiap tahun pada tanggal tertentu |

Tanggal dibatasi maksimal 28 untuk menghindari masalah bulan pendek (Februari).

---

## Hak Akses

| Permission | Admin | Owner | Accountant | Staff | Auditor |
|-----------|-------|-------|------------|-------|---------|
| RECURRING_VIEW | Ya | Ya | Ya | Ya | Ya |
| RECURRING_CREATE | Ya | Ya | Ya | -- | -- |
| RECURRING_EDIT | Ya | Ya | Ya | -- | -- |
| RECURRING_DELETE | Ya | Ya | Ya | -- | -- |

---

Selanjutnya: [Peringatan (Smart Alerts)](05-smart-alerts.md)
