# Troubleshooting

Panduan menyelesaikan masalah umum pada deployment Balaka.

## Aplikasi Tidak Bisa Start

### Diagnosis

```bash
# Cek status service
sudo systemctl status aplikasi-akunting

# Cek apakah proses Java berjalan
ps aux | grep java

# Cek apakah port sudah digunakan
sudo netstat -tlnp | grep 10000

# Lihat log terakhir
sudo journalctl -u aplikasi-akunting -n 100 --no-pager
tail -100 /var/log/aplikasi-akunting/app.log
```

### Penyebab Umum

| Gejala | Penyebab | Solusi |
|--------|----------|-------|
| `Address already in use: 10000` | Port sudah dipakai | Kill proses lama: `sudo kill $(sudo lsof -t -i:10000)` |
| `Connection refused: localhost:5432` | PostgreSQL tidak jalan | `sudo systemctl start postgresql` |
| `FATAL: password authentication failed` | Credentials salah | Cek `application.properties` |
| `Migration checksum mismatch` | Migrasi dimodifikasi | Lihat bagian Flyway di bawah |
| `Metaspace OutOfMemoryError` | Metaspace penuh | Naikkan `-XX:MaxMetaspaceSize` |

## Flyway Checksum Mismatch

Error:

```
Migration checksum mismatch for migration version 003
-> Applied to database : -1535984110
-> Resolved locally    : -1414456606
```

**Penyebab:** File migrasi (misal V003) dimodifikasi setelah sudah dijalankan di database.

**Perbaikan:**

1. Identifikasi perubahan schema (diff antara versi lama dan baru)
2. Terapkan perubahan manual ke database:

```bash
# Cek schema tabel saat ini
sudo -u postgres psql -d accountingdb -c "\d <nama_tabel>"

# Tambahkan kolom yang belum ada
sudo -u postgres psql -d accountingdb \
  -c "ALTER TABLE <nama_tabel> ADD COLUMN <nama_kolom> <tipe>;"
```

3. Update checksum di `flyway_schema_history` (gunakan nilai "Resolved locally" dari error message):

```bash
sudo -u postgres psql -d accountingdb \
  -c "UPDATE flyway_schema_history SET checksum = -1414456606 WHERE version = '003';"
```

4. Restart:

```bash
sudo systemctl restart aplikasi-akunting
```

## OOM Kill

### Gejala

Aplikasi tiba-tiba mati tanpa log error. Ditemukan di `dmesg`:

```bash
sudo dmesg | grep -i oom
# Out of memory: Killed process <PID> (java) total-vm:...
```

### Penyebab

JVM menggunakan lebih banyak memori daripada yang tersedia di VPS.

### Solusi

1. **Kurangi heap size:**

```bash
# Untuk VPS 2GB
-Xms256m -Xmx512m

# Untuk multi-instance di VPS 4GB
-Xms256m -Xmx384m
```

2. **Cek memory leak:**

```bash
# RSS saat ini
ps -o rss= -p $(pgrep -f aplikasi-akunting.jar)

# Heap usage
sudo -u akunting jcmd $(pgrep -f aplikasi-akunting.jar) GC.heap_info
```

3. **Tambah swap (solusi sementara):**

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

4. **Set cgroup memory limit** untuk mencegah OOM kill terhadap proses lain:

```ini
# Di systemd service file
MemoryMax=800M
```

## Slow Startup

### Gejala

Aplikasi butuh lebih dari 2 menit untuk start.

### Diagnosis

```bash
# Cek waktu startup di log
sudo journalctl -u aplikasi-akunting | grep "Started" | tail -1

# Cek apakah Flyway migration sedang berjalan
sudo journalctl -u aplikasi-akunting | grep -i "flyway\|migration"
```

### Penyebab dan Solusi

| Penyebab | Solusi |
|----------|-------|
| Flyway migration pertama kali | Normal, tunggu selesai |
| DNS resolution lambat | Tambah entry di `/etc/hosts` |
| Disk I/O lambat | Pindah ke SSD |
| GC pause saat startup | Naikkan initial heap (`-Xms`) |

## Port Conflicts

```bash
# Cek proses yang menggunakan port 10000
sudo lsof -i :10000
sudo netstat -tlnp | grep 10000

# Kill proses
sudo kill $(sudo lsof -t -i:10000)

# Jika tidak bisa di-kill
sudo kill -9 $(sudo lsof -t -i:10000)
```

## SSL Certificate Renewal

### Gejala

Browser menampilkan error sertifikat expired atau Nginx gagal start.

### Diagnosis

```bash
# Cek status sertifikat
sudo certbot certificates

# Cek expiry
echo | openssl s_client -servername <domain> \
  -connect <domain>:443 2>/dev/null | \
  openssl x509 -noout -dates
```

### Solusi

```bash
# Test renewal
sudo certbot renew --dry-run

# Force renewal
sudo certbot renew --force-renewal

# Restart Nginx
sudo systemctl restart nginx
```

### Jika Auto-Renewal Gagal

```bash
# Cek timer certbot
sudo systemctl status certbot.timer
sudo systemctl list-timers | grep certbot

# Cek log certbot
sudo journalctl -u certbot

# Manual renewal
sudo certbot certonly --nginx -d <domain>
```

## Database Connection Failed

```bash
# Cek PostgreSQL berjalan
sudo systemctl status postgresql

# Test koneksi
sudo -u postgres psql -d accountingdb -c "SELECT 1;"

# Cek credentials
grep -i "datasource" /opt/aplikasi-akunting/application.properties

# Cek koneksi aktif (terlalu banyak?)
sudo -u postgres psql -c "SELECT count(*) FROM pg_stat_activity WHERE datname = 'accountingdb';"

# Cek max_connections
sudo -u postgres psql -c "SHOW max_connections;"
```

### Connection Pool Exhausted

Jika semua koneksi terpakai:

```bash
# Lihat koneksi aktif
sudo -u postgres psql -c "SELECT pid, state, query_start, query
  FROM pg_stat_activity WHERE datname = 'accountingdb';"

# Terminate idle connections
sudo -u postgres psql -c "SELECT pg_terminate_backend(pid)
  FROM pg_stat_activity
  WHERE datname = 'accountingdb'
  AND state = 'idle'
  AND query_start < now() - interval '30 minutes';"
```

## CORS Issues

Balaka adalah aplikasi same-origin, CORS dinonaktifkan. Jika muncul CORS error:

1. Pastikan akses melalui domain yang sama (bukan mix IP dan domain)
2. Pastikan Nginx proxy header dikonfigurasi:

```nginx
proxy_set_header Host $host;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
```

## Backup Failed

```bash
# Cek log backup
cat /var/log/aplikasi-akunting/backup.log

# Cek disk space
df -h

# Cek pgpass
cat /opt/aplikasi-akunting/.pgpass

# Debug backup script
sudo -u akunting bash -x /opt/aplikasi-akunting/scripts/backup.sh
```

## Testcontainers (Development)

### Docker Requirement

Full test suite memerlukan Docker untuk Testcontainers (PostgreSQL, ZAP):

```bash
# Cek Docker
docker info

# Jika Docker tidak terinstall
sudo apt install docker.io
sudo usermod -aG docker $USER
```

### Test Gagal karena Testcontainers

```bash
# Error: Could not find a valid Docker environment
# Solusi: Pastikan Docker daemon berjalan
sudo systemctl start docker

# Error: Container startup timed out
# Solusi: Pull image terlebih dahulu
docker pull postgres:18
```

### Tips Test

```bash
# Full test suite (60-90 menit, SELALU jalankan di background)
nohup ./mvnw test > target/test-output.log 2>&1 &

# JANGAN jalankan beberapa instance test bersamaan

# Test spesifik
./mvnw test -Dtest=NamaTestClass

# Debug dengan browser visible
./mvnw test -Dtest=NamaTestClass -Dplaywright.headless=false -Dplaywright.slowmo=100
```

## Health Check Timeout Saat Deploy

### Gejala

Ansible deployment gagal karena health check timeout (30 retry x 5 detik = 2.5 menit).

### Diagnosis

```bash
# Cek log di server
ssh <user>@<server>
sudo journalctl -u aplikasi-akunting --no-pager | tail -50
```

### Penyebab Umum

| Penyebab | Log Indicator | Solusi |
|----------|--------------|-------|
| Flyway error | `Migration checksum mismatch` | Lihat bagian Flyway di atas |
| Database down | `Connection refused: localhost:5432` | Start PostgreSQL |
| Port conflict | `Address already in use` | Kill proses lama |
| OutOfMemory | `java.lang.OutOfMemoryError` | Kurangi heap atau upgrade VPS |

## Database Reset (Fresh Start)

**Peringatan:** Menghapus SEMUA data.

```bash
sudo systemctl stop aplikasi-akunting
sudo -u postgres psql -c "DROP DATABASE IF EXISTS accountingdb;"
sudo -u postgres psql -c "CREATE DATABASE accountingdb OWNER akunting;"
sudo systemctl start aplikasi-akunting
# Flyway akan membuat schema dan seed data dari awal
```

## Command Reference

Rangkuman command yang sering digunakan:

```bash
# Service
sudo systemctl {start|stop|restart|status} aplikasi-akunting
sudo systemctl {start|stop|restart|status} postgresql
sudo systemctl {start|stop|restart|status|reload} nginx

# Logs
sudo journalctl -u aplikasi-akunting -f
tail -f /var/log/aplikasi-akunting/app.log
tail -f /var/log/nginx/access.log

# Database
sudo -u postgres psql -d accountingdb
sudo -u postgres psql -c "SELECT version();"

# Health check
curl -s http://localhost:10000/actuator/health

# SSL
sudo certbot certificates
sudo certbot renew --dry-run

# Backup
sudo -u akunting /opt/aplikasi-akunting/scripts/backup.sh

# Memori
free -h
ps -o rss= -p $(pgrep -f aplikasi-akunting.jar)
```
