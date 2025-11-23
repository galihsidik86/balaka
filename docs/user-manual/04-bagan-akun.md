# Bagan Akun (Chart of Accounts)

## Pengertian

Bagan Akun adalah daftar semua akun yang digunakan untuk mencatat transaksi keuangan dalam sistem akuntansi.

## Struktur Kode Akun

Sistem menggunakan format kode akun sebagai berikut:

| Kode | Tipe Akun | Contoh |
|------|-----------|--------|
| 1.x.xx | Aset (Harta) | Kas, Bank, Piutang |
| 2.x.xx | Liabilitas (Kewajiban) | Hutang Usaha, Hutang Bank |
| 3.x.xx | Ekuitas (Modal) | Modal Disetor, Laba Ditahan |
| 4.x.xx | Pendapatan | Pendapatan Jasa, Pendapatan Bunga |
| 5.x.xx | Beban | Beban Gaji, Beban Listrik |

## Melihat Daftar Akun

1. Klik menu **Akun** di sidebar
2. Daftar akun akan ditampilkan dalam struktur hierarki
3. Gunakan filter untuk mencari akun tertentu

## Menambah Akun Baru

1. Klik tombol **Tambah Akun** di halaman Bagan Akun
2. Isi form dengan data berikut:
   - **Kode Akun** - Format: X.X.XX (contoh: 1.1.01)
   - **Nama Akun** - Nama deskriptif (contoh: Kas Kecil)
   - **Tipe Akun** - Pilih: Aset, Liabilitas, Ekuitas, Pendapatan, atau Beban
   - **Akun Induk** - Pilih akun induk jika merupakan sub-akun
   - **Saldo Normal** - Debit atau Kredit
3. Klik **Simpan**

## Saldo Normal

Saldo normal menentukan sisi mana yang menambah nilai akun:

| Tipe Akun | Saldo Normal | Bertambah | Berkurang |
|-----------|--------------|-----------|-----------|
| Aset | Debit | Debit | Kredit |
| Beban | Debit | Debit | Kredit |
| Liabilitas | Kredit | Kredit | Debit |
| Ekuitas | Kredit | Kredit | Debit |
| Pendapatan | Kredit | Kredit | Debit |

## Akun Permanen vs Temporer

- **Akun Permanen** - Saldo dibawa ke periode berikutnya (Aset, Liabilitas, Ekuitas)
- **Akun Temporer** - Saldo di-reset ke nol saat tutup buku (Pendapatan, Beban)

## Mengubah Akun

1. Klik nama akun yang ingin diubah
2. Klik tombol **Edit**
3. Ubah data yang diperlukan
4. Klik **Simpan Perubahan**

> Catatan: Akun yang sudah memiliki transaksi tidak dapat dihapus, hanya dapat dinonaktifkan.
