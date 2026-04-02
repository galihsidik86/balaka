# Support Playbook

Panduan menangani pertanyaan dan masalah umum dari klien.

## Prinsip Dukungan

1. **Partner menangani pertanyaan akuntansi** — cara pencatatan, pemilihan template, pembacaan laporan
2. **ArtiVisi menangani masalah teknis** — error sistem, bug, masalah server, update aplikasi
3. **Dokumentasikan masalah** — catat masalah yang sering muncul untuk perbaikan proses

## Masalah Umum dan Solusinya

### "Trial balance tidak balance (debit tidak sama dengan kredit)"

**Penyebab:** Ini seharusnya tidak terjadi karena setiap jurnal di Balaka selalu memaksa debit = kredit. Jika terjadi, kemungkinan:

1. Ada transaksi saldo awal yang salah input
2. Ada modifikasi data langsung di database (bukan melalui aplikasi)

**Langkah penyelesaian:**

1. Buka **Laporan > Trial Balance**
2. Catat selisih antara total debit dan total kredit
3. Cek transaksi saldo awal — apakah total debit = total kredit
4. Jika selisih ditemukan di saldo awal, buat jurnal koreksi menggunakan template **Jurnal Manual**
5. Jika penyebab tidak jelas, **eskalasi ke ArtiVisi** — kemungkinan ada bug

---

### "Perhitungan PPh 21 karyawan salah"

**Penyebab umum:**

1. Status PTKP karyawan tidak sesuai
2. Gaji yang diinput tidak sesuai
3. Bulan Desember memang berbeda (rekonsiliasi tahunan)

**Langkah penyelesaian:**

1. Buka **Master Data > Karyawan**
2. Cek status PTKP karyawan — apakah sudah sesuai (TK/0, K/1, dll)?
3. Cek gaji bruto yang digunakan
4. Jika bulan Desember: PPh 21 di bulan terakhir memang dihitung menggunakan **tarif progresif Pasal 17** (bukan TER). Angka ini bisa jauh lebih besar atau lebih kecil dari bulan-bulan sebelumnya. Ini normal.
5. Hitung manual menggunakan kalkulator PPh 21 TER:
   - Januari-November: Gaji Bruto x Tarif TER sesuai kategori
   - Desember: (PPh 21 Setahun dengan tarif progresif) - (Total PPh 21 Jan-Nov)

**Referensi tarif TER:** PMK 168/2023

---

### "Tidak bisa posting transaksi ke periode yang sudah ditutup"

**Penyebab:** Periode fiskal bulan tersebut sudah berstatus CLOSED.

**Langkah penyelesaian:**

1. Buka **Master Data > Periode Fiskal**
2. Cari periode yang dimaksud
3. Jika memang perlu membuka kembali periode:
   - Klik periode tersebut
   - Ubah status dari CLOSED ke OPEN
   - Klik Simpan
   - Posting transaksi yang tertunda
   - Tutup kembali periode setelah selesai
4. Jika transaksi seharusnya di bulan yang sudah ditutup, pertimbangkan: apakah bisa dicatat di bulan berjalan dengan keterangan yang jelas?

**Pertimbangan:** Membuka kembali periode yang sudah ditutup bisa mempengaruhi laporan yang sudah diterbitkan. Konsultasikan dengan klien sebelum membuka periode.

---

### "Laporan PPN menunjukkan angka yang salah"

**Penyebab umum:**

1. Transaksi belum di-posting (masih draft)
2. Template yang digunakan tidak memperhitungkan PPN
3. Formula PPN di template tidak sesuai

**Langkah penyelesaian:**

1. Buka **Laporan > Pajak > PPN**
2. Filter per bulan yang bermasalah
3. Cek daftar transaksi yang masuk laporan PPN:
   - Apakah semua transaksi penjualan+PPN sudah tercatat?
   - Apakah ada transaksi yang seharusnya kena PPN tapi pakai template tanpa PPN?
4. Cek akun 2.1.03 Hutang PPN di Trial Balance — saldonya harus cocok dengan total PPN yang belum disetor
5. Jika ada transaksi yang salah template:
   - Void transaksi yang salah
   - Input ulang dengan template yang benar

**Pencegahan:** Saat training, tekankan perbedaan antara template "Pendapatan Jasa" (tanpa PPN) dan "Pendapatan Jasa + PPN" (dengan PPN). Untuk klien PKP, hampir semua pendapatan harus menggunakan template +PPN.

---

### "Bagaimana cara void transaksi yang sudah diposting?"

**Langkah:**

1. Buka daftar transaksi
2. Cari transaksi yang ingin di-void
3. Klik transaksi tersebut untuk melihat detail
4. Klik tombol **Void**
5. Isi alasan void
6. Konfirmasi

**Catatan:**
- Void tidak menghapus transaksi, melainkan membuat jurnal pembalik (reversal)
- Transaksi yang sudah di-void tetap terlihat dalam daftar dengan status VOID
- Jika periode sudah ditutup, buka dulu periodenya sebelum void

---

### "Saldo bank di Balaka tidak cocok dengan mutasi bank"

**Penyebab umum:**

1. Ada transaksi yang belum dicatat di Balaka
2. Ada transaksi yang salah jumlahnya
3. Saldo awal bank tidak sesuai

**Langkah penyelesaian:**

1. Buka **Laporan > Trial Balance** dan catat saldo akun bank di Balaka
2. Bandingkan dengan saldo di rekening koran/mutasi bank
3. Jika ada selisih:
   - Gunakan fitur **Rekonsiliasi Bank** (menu Akuntansi > Rekonsiliasi Bank)
   - Import file CSV mutasi bank
   - Sistem akan melakukan auto-matching
   - Review transaksi yang belum match
   - Untuk transaksi yang ada di bank tapi belum di Balaka: catat transaksi baru
   - Untuk transaksi yang ada di Balaka tapi belum di bank: cek apakah memang belum clearing

---

### "Payroll tidak bisa dijalankan"

**Penyebab umum:**

1. Belum ada karyawan aktif di sistem
2. Data karyawan tidak lengkap (status PTKP, NPWP)
3. Payroll bulan tersebut sudah ada

**Langkah penyelesaian:**

1. Buka **Master Data > Karyawan** — pastikan ada karyawan aktif
2. Cek kelengkapan data setiap karyawan: PTKP, NPWP, tanggal masuk
3. Buka **Payroll** — cek apakah sudah ada payroll run untuk bulan tersebut
4. Jika error tetap terjadi, **eskalasi ke ArtiVisi** dengan menyertakan pesan error yang muncul

---

### "Laporan neraca tidak balance (Aset tidak sama dengan Liabilitas + Ekuitas)"

**Penyebab:** Sama seperti trial balance tidak balance — ini menunjukkan ada data yang tidak konsisten.

**Langkah penyelesaian:**

1. Buka **Laporan > Trial Balance** terlebih dahulu
2. Pastikan total debit = total kredit
3. Jika trial balance balance tapi neraca tidak: kemungkinan ada akun yang salah tipe (misal akun beban dikategorikan sebagai aset)
4. Cek **Master Data > Bagan Akun** — pastikan tipe setiap akun sudah benar
5. Jika masalah tetap ada, **eskalasi ke ArtiVisi**

---

### "Bagaimana cara menutup buku akhir tahun?"

**Langkah:**

1. Pastikan semua transaksi tahun berjalan sudah diposting
2. Pastikan semua periode fiskal (Januari-Desember) sudah CLOSED
3. Jalankan fitur **Tutup Buku Akhir Tahun**
4. Sistem akan membuat jurnal penutup yang:
   - Menutup semua akun pendapatan (saldo menjadi 0)
   - Menutup semua akun beban (saldo menjadi 0)
   - Memindahkan selisih (laba/rugi bersih) ke akun 3.2.01 Laba Ditahan
5. Verifikasi: buka Neraca — akun 3.2.02 Laba Berjalan harus 0, dan 3.2.01 Laba Ditahan bertambah

---

### "Import data gagal"

**Penyebab umum:**

1. File bukan format ZIP
2. Struktur folder di dalam ZIP tidak sesuai
3. Format CSV tidak sesuai (kolom kurang/lebih, format tanggal salah)
4. Data duplikat (kode akun atau nama template sudah ada)

**Langkah penyelesaian:**

1. Periksa format file — harus ZIP
2. Buka ZIP dan periksa apakah file CSV ada langsung di dalam folder (bukan nested folder)
3. Periksa header CSV — harus sesuai dengan format yang didokumentasikan di [Import Data](04-data-import.md)
4. Jika error "duplicate", berarti data tersebut sudah ada di sistem. Hapus baris duplikat dari CSV dan coba import ulang.
5. Jika error teknis yang tidak jelas, **eskalasi ke ArtiVisi** dengan menyertakan file ZIP dan pesan error

## Kapan Harus Eskalasi ke ArtiVisi

Eskalasi ke ArtiVisi jika:

| Situasi | Contoh |
|---------|--------|
| Error teknis / halaman error | Muncul halaman "500 Internal Server Error" |
| Aplikasi tidak bisa diakses | URL tidak merespons, timeout |
| Bug — perilaku tidak sesuai harapan | Klik simpan tapi data tidak tersimpan |
| Data inkonsisten yang tidak bisa dijelaskan | Trial balance tidak balance tanpa penyebab yang jelas |
| Permintaan fitur | Klien butuh template atau laporan khusus |
| Masalah keamanan | Akun tidak bisa login, suspect unauthorized access |

## Cara Mengajukan Tiket ke ArtiVisi

Kirim email atau pesan dengan informasi:

1. **Nama klien** dan URL instance
2. **Deskripsi masalah** — apa yang terjadi, apa yang diharapkan
3. **Langkah reproduksi** — langkah-langkah yang menyebabkan masalah
4. **Screenshot** — tangkap layar halaman yang bermasalah
5. **Waktu kejadian** — kapan masalah pertama muncul
6. **Urgensi** — apakah blocking (klien tidak bisa kerja) atau non-blocking

### Waktu Respons

| Urgensi | Target Respons | Target Resolusi |
|---------|---------------|-----------------|
| Blocking (aplikasi down) | 2 jam | 8 jam |
| Blocking (fitur utama error) | 4 jam | 24 jam |
| Non-blocking (bug minor) | 1 hari kerja | 5 hari kerja |
| Permintaan fitur | 3 hari kerja | Sesuai roadmap |

## Checklist Review Bulanan

Untuk menjaga kualitas data klien, lakukan review bulanan:

- [ ] Trial balance: total debit = total kredit
- [ ] Saldo bank di Balaka cocok dengan rekening koran
- [ ] Tidak ada transaksi draft yang seharusnya sudah diposting
- [ ] Semua transaksi bulan sebelumnya sudah tercatat
- [ ] Payroll sudah dijalankan dan diposting
- [ ] Hutang gaji, BPJS, PPh 21 sudah dibayar/disetor
- [ ] PPN sudah disetor dan dilaporkan (PKP)
- [ ] Periode bulan sebelumnya sudah di-close

## Kembali ke Awal

[Model Kemitraan Balaka](01-partner-model.md)
