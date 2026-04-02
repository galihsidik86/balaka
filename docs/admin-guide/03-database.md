# Database

Panduan konfigurasi PostgreSQL, migrasi Flyway, backup, dan restore.

## PostgreSQL Setup

### Instalasi

PostgreSQL 18 diinstall dari PGDG repository oleh Ansible (`site.yml`). Untuk instalasi manual:

```bash
# Tambah PGDG repository
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo apt update

# Install PostgreSQL 18
sudo apt install postgresql-18
```

### Buat Database dan User

```bash
sudo -u postgres psql

CREATE USER akunting WITH PASSWORD '<password>';
CREATE DATABASE accountingdb OWNER akunting;
GRANT ALL PRIVILEGES ON DATABASE accountingdb TO akunting;
\q
```

### SSL Certificates

Koneksi database menggunakan `sslmode=require`. Setup SSL untuk PostgreSQL:

```bash
# Generate self-signed certificate untuk PostgreSQL
sudo -u postgres openssl req -new -x509 -days 3650 \
  -nodes -text \
  -out /etc/postgresql/18/main/server.crt \
  -keyout /etc/postgresql/18/main/server.key \
  -subj "/CN=localhost"

# Set permissions
sudo -u postgres chmod 600 /etc/postgresql/18/main/server.key
```

Edit `postgresql.conf`:

```
ssl = on
ssl_cert_file = 'server.crt'
ssl_key_file = 'server.key'
```

Restart PostgreSQL:

```bash
sudo systemctl restart postgresql
```

### Optimasi PostgreSQL

Konfigurasi yang dioptimasi untuk OLTP workload di VPS kecil:

| Setting | Value | Keterangan |
|---------|-------|------------|
| `shared_buffers` | 128 MB | ~6% RAM untuk shared server |
| `effective_cache_size` | 384 MB | Estimasi OS cache |
| `work_mem` | 4 MB | Memori per-operasi sort |
| `maintenance_work_mem` | 64 MB | VACUUM, CREATE INDEX |
| `max_connections` | 20 | Sesuai HikariCP pool |
| `random_page_cost` | 1.1 | Untuk SSD storage |

Edit `/etc/postgresql/18/main/postgresql.conf`:

```
shared_buffers = 128MB
effective_cache_size = 384MB
work_mem = 4MB
maintenance_work_mem = 64MB
max_connections = 20
random_page_cost = 1.1
```

Autovacuum dikonfigurasi agresif (scale factor 5%) untuk OLTP.

## Flyway Migrations

Balaka menggunakan Flyway untuk schema management. Migrasi dijalankan otomatis saat aplikasi start.

### Daftar Migrasi

| Versi | File | Isi |
|-------|------|-----|
| V001 | `V001__security.sql` | Tabel user, role, session. Seed admin user |
| V002 | `V002__core_schema.sql` | Schema akuntansi inti (COA, journal, transaksi) |
| V003 | `V003__feature_schema.sql` | Schema fitur (payroll, asset, inventory, dll) |
| V004 | `V004__seed_data.sql` | Data awal (template jurnal, komponen gaji, dll) |

Migrasi terletak di `src/main/resources/db/migration/`.

### JPA Validation Mode

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate hanya memvalidasi bahwa schema cocok dengan entity. Tidak pernah membuat atau mengubah tabel.

## Backup

### Jadwal Backup

| Tipe | Jadwal | Retensi | Lokasi |
|------|--------|---------|--------|
| Lokal | Harian 02:00 | 7 hari | `/opt/aplikasi-akunting/backup/` |
| Backblaze B2 | Harian 03:00 | 4 minggu | Backblaze B2 |
| Google Drive | Harian 04:00 | 12 bulan | Google Drive |

### Isi Backup

```
aplikasi-akunting_20260402_020000.tar.gz
└── aplikasi-akunting_20260402_020000/
    ├── database.sql         # pg_dump output
    ├── documents.tar.gz     # Dokumen upload
    └── manifest.json        # Metadata backup
```

### Manual Backup

```bash
sudo -u akunting /opt/aplikasi-akunting/scripts/backup.sh
```

Verifikasi backup:

```bash
ls -lh /opt/aplikasi-akunting/backup/ | tail -1
```

### Backup dengan pg_dump (Manual)

```bash
# Full database dump
sudo -u postgres pg_dump -Fc accountingdb > /tmp/accountingdb.dump

# SQL format (untuk inspeksi)
sudo -u postgres pg_dump accountingdb > /tmp/accountingdb.sql
```

### Cron Job Backup

Cron job dikonfigurasi oleh Ansible. Untuk verifikasi:

```bash
sudo crontab -u akunting -l
```

Contoh output:

```
0 2 * * * /opt/aplikasi-akunting/scripts/backup.sh >> /var/log/aplikasi-akunting/backup.log 2>&1
0 3 * * * /opt/aplikasi-akunting/scripts/backup-b2.sh >> /var/log/aplikasi-akunting/backup.log 2>&1
0 4 * * * /opt/aplikasi-akunting/scripts/backup-gdrive.sh >> /var/log/aplikasi-akunting/backup.log 2>&1
```

## Restore

### Prosedur Restore

```bash
# Lihat backup yang tersedia
ls -la /opt/aplikasi-akunting/backup/

# Restore
sudo /opt/aplikasi-akunting/scripts/restore.sh \
  /opt/aplikasi-akunting/backup/aplikasi-akunting_20260402_020000.tar.gz
```

Proses restore:

1. Validasi checksum
2. Stop aplikasi
3. Drop dan recreate database
4. Import database dump
5. Restore dokumen
6. Start aplikasi

### Restore dari pg_dump

```bash
# Stop aplikasi
sudo systemctl stop aplikasi-akunting

# Drop dan recreate database
sudo -u postgres psql -c "DROP DATABASE IF EXISTS accountingdb;"
sudo -u postgres psql -c "CREATE DATABASE accountingdb OWNER akunting;"

# Restore (custom format)
sudo -u postgres pg_restore -d accountingdb /tmp/accountingdb.dump

# Atau restore (SQL format)
sudo -u postgres psql -d accountingdb < /tmp/accountingdb.sql

# Start aplikasi
sudo systemctl start aplikasi-akunting
```

### Disaster Recovery

1. Provision VPS baru
2. Jalankan `site.yml` untuk setup server
3. Copy file backup ke server baru
4. Jalankan restore script

| Metrik | Target |
|--------|--------|
| RTO (Recovery Time Objective) | ~4 jam |
| RPO (Recovery Point Objective) | 24 jam |

## Encryption Key Backup

Lokasi encryption key untuk backup: `/opt/aplikasi-akunting/.backup-key`

**Kritis:** Simpan key ini di lokasi terpisah. Tanpa key ini, backup terenkripsi tidak bisa dibuka.

Simpan minimal di DUA lokasi:
- Password manager (Bitwarden, 1Password)
- Printed copy di tempat aman
- USB drive terenkripsi

## Troubleshooting Migrasi

### Flyway Checksum Mismatch

Terjadi saat file migrasi (misal V003) dimodifikasi setelah sudah dijalankan di production. Flyway menyimpan checksum dari migrasi yang sudah dijalankan dan menolak jika berbeda.

```
Migration checksum mismatch for migration version 003
-> Applied to database : -1535984110
-> Resolved locally    : -1414456606
```

**Perbaikan:**

1. Identifikasi perubahan schema yang ditambahkan ke migrasi:

```bash
sudo -u postgres psql -d accountingdb -c "\d <nama_tabel>"
```

2. Terapkan perubahan schema secara manual:

```bash
sudo -u postgres psql -d accountingdb \
  -c "ALTER TABLE <nama_tabel> ADD COLUMN <nama_kolom> <tipe>;"
```

3. Update checksum di `flyway_schema_history`:

```bash
sudo -u postgres psql -d accountingdb \
  -c "UPDATE flyway_schema_history SET checksum = <checksum_baru> WHERE version = '<versi>';"
```

Nilai `<checksum_baru>` adalah yang tertulis di error message sebagai "Resolved locally".

4. Restart aplikasi:

```bash
sudo systemctl restart aplikasi-akunting
```

### Database Reset (Hapus Semua Data)

**Peringatan:** Ini menghapus SEMUA data. Hanya gunakan untuk deployment baru.

```bash
sudo systemctl stop aplikasi-akunting
sudo -u postgres psql -c "DROP DATABASE IF EXISTS accountingdb;"
sudo -u postgres psql -c "CREATE DATABASE accountingdb OWNER akunting;"
sudo systemctl start aplikasi-akunting
# Flyway akan membuat schema dan seed data dari awal
```

## Upgrade PostgreSQL

Gunakan playbook `upgrade-postgresql.yml` untuk upgrade major version (misal 17 ke 18):

```bash
ansible-playbook -i inventory.ini upgrade-postgresql.yml \
  -e "pg_old_version=17 pg_new_version=18"
```

Playbook akan:
1. Stop aplikasi
2. Backup database dengan `pg_dump`
3. Install PostgreSQL versi baru dari PGDG
4. Konfigurasi cluster baru di port 5432
5. Restore database ke cluster baru
6. Start aplikasi
7. Verifikasi koneksi

Verifikasi setelah upgrade:

```bash
sudo -u postgres psql -c "SELECT version();"
sudo -u postgres psql -c "SHOW shared_buffers; SHOW work_mem;"
curl -s http://localhost:10000/actuator/health
```

Jika perlu rollback, cluster lama masih tersedia di port 5433:

```bash
sudo pg_ctlcluster 18 main stop
sudo sed -i 's/port = 5433/port = 5432/' /etc/postgresql/17/main/postgresql.conf
sudo pg_ctlcluster 17 main start
sudo systemctl restart aplikasi-akunting
```

## Selanjutnya

Lihat [Monitoring](04-monitoring.md) untuk memantau kesehatan aplikasi.
