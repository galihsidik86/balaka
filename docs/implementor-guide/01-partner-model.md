# Model Kemitraan Balaka

Panduan lengkap tentang program kemitraan Balaka untuk kantor akuntan dan konsultan pajak.

## Apa Itu Partner Balaka

Partner Balaka adalah kantor akuntan, konsultan pajak, atau firma akuntansi yang menjadi mitra resmi untuk mengimplementasikan dan mendukung aplikasi Balaka bagi klien-klien mereka.

Partner bertindak sebagai perpanjangan tangan ArtiVisi di lapangan: melakukan onboarding klien, konfigurasi sistem, pelatihan, dan dukungan akuntansi sehari-hari. ArtiVisi menangani infrastruktur teknis (server, database, deployment, update aplikasi).

## Pembagian Tanggung Jawab

### Partner Bertanggung Jawab Atas

| Area | Detail |
|------|--------|
| Onboarding klien | Pengumpulan data perusahaan, konfigurasi awal, import data |
| Konfigurasi akuntansi | Penyesuaian COA, template jurnal, pengaturan pajak |
| Pelatihan | Training penggunaan aplikasi untuk staf klien |
| Dukungan akuntansi | Menjawab pertanyaan tentang pencatatan, pelaporan, pajak |
| Review berkala | Cek trial balance, rekonsiliasi, kelengkapan data |

### ArtiVisi Bertanggung Jawab Atas

| Area | Detail |
|------|--------|
| Infrastruktur | Server, database, SSL, backup, monitoring |
| Provisioning instance | Pembuatan instance baru untuk klien baru |
| Update aplikasi | Rilis fitur baru, perbaikan bug, patch keamanan |
| Dukungan teknis | Masalah teknis yang tidak bisa diselesaikan partner |
| SLA uptime | Target 99.5% uptime per bulan |

## Model Harga

| Item | Harga |
|------|-------|
| Harga wholesale per klien | Rp 100.000/bulan |
| Minimum klien aktif | 5 klien |
| Minimum commitment bulanan | Rp 500.000/bulan |

Partner bebas menentukan harga jual ke klien. Selisih antara harga jual dan harga wholesale menjadi pendapatan partner.

**Contoh model pendapatan:**

| Skenario | Harga ke klien | Jumlah klien | Pendapatan kotor | Biaya wholesale | Margin partner |
|----------|---------------|--------------|-------------------|-----------------|----------------|
| Awal | Rp 300.000/bln | 5 | Rp 1.500.000 | Rp 500.000 | Rp 1.000.000 |
| Berkembang | Rp 300.000/bln | 20 | Rp 6.000.000 | Rp 2.000.000 | Rp 4.000.000 |
| Mapan | Rp 250.000/bln | 50 | Rp 12.500.000 | Rp 5.000.000 | Rp 7.500.000 |

Partner juga bisa menambah pendapatan dari jasa tambahan:
- Setup awal dan migrasi data (one-time fee)
- Pelatihan lanjutan
- Review dan konsultasi akuntansi bulanan
- Jasa pelaporan pajak

## Isolasi Data Klien

Setiap klien mendapatkan:
- **Database terpisah** — data antar klien tidak pernah tercampur
- **URL terpisah** — setiap klien mengakses instance sendiri
- **Akun admin terpisah** — partner mendapat akses admin ke setiap instance klien

Ini berarti: satu klien mengalami masalah tidak mempengaruhi klien lain.

## Menjadi Partner

### Syarat

1. Berbadan hukum (CV, PT, atau firma perorangan terdaftar)
2. Memiliki kompetensi akuntansi dan/atau perpajakan
3. Sanggup meng-handle minimum 5 klien aktif dalam 6 bulan pertama
4. Bersedia mengikuti pelatihan implementor dari ArtiVisi

### Proses Pendaftaran

1. **Ajukan permohonan** — Hubungi ArtiVisi melalui email atau formulir di website
2. **Diskusi awal** — Review profil partner, diskusi target pasar
3. **Pelatihan implementor** — Training penggunaan Balaka dari sisi implementor (2-3 hari)
4. **Penandatanganan perjanjian** — Kontrak kemitraan dan SLA
5. **Setup demo instance** — ArtiVisi menyediakan instance demo untuk presentasi ke calon klien
6. **Mulai onboarding klien** — Partner mulai menawarkan ke klien mereka

### Instance Demo

Partner mendapat satu instance demo gratis yang sudah terisi data contoh lengkap (transaksi, payroll, laporan). Instance ini digunakan untuk:
- Presentasi ke calon klien
- Latihan konfigurasi dan penggunaan
- Testing skenario-skenario klien

## Langkah Selanjutnya

Setelah memahami model kemitraan, pelajari proses onboarding klien baru:

[Onboarding Klien Baru](02-client-onboarding.md)
