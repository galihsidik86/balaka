# Monitoring

Panduan pemantauan kesehatan aplikasi, server, dan database Balaka.

## Health Check Endpoint

Spring Boot Actuator menyediakan endpoint health check:

```bash
curl -s http://localhost:10000/actuator/health
# {"status":"UP"}
```

Endpoint ini memeriksa:
- Koneksi database
- Disk space
- JVM status

Gunakan endpoint ini untuk monitoring otomatis (UptimeRobot, Uptime Kuma, dll).

## Systemd Status

```bash
# Status aplikasi
sudo systemctl status aplikasi-akunting

# Status PostgreSQL
sudo systemctl status postgresql

# Status Nginx
sudo systemctl status nginx

# Semua service terkait
sudo systemctl is-active aplikasi-akunting postgresql nginx
```

## Log

### Application Log

```bash
# Live log via journalctl
sudo journalctl -u aplikasi-akunting -f

# 100 baris terakhir
sudo journalctl -u aplikasi-akunting -n 100

# Log sejak waktu tertentu
sudo journalctl -u aplikasi-akunting --since "2026-04-01 08:00"

# File log
tail -f /var/log/aplikasi-akunting/app.log
```

### Nginx Access/Error Log

```bash
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### GC Log

```bash
# JVM Garbage Collection log
tail -f /var/log/aplikasi-akunting/gc.log
```

### Backup Log

```bash
cat /var/log/aplikasi-akunting/backup.log
```

## Monitoring Eksternal

### UptimeRobot / Uptime Kuma

Konfigurasi HTTP monitor:

| Parameter | Value |
|-----------|-------|
| URL | `https://<domain>/login` |
| Method | `GET` |
| Expected Status | `200` |
| Interval | 5 menit |
| Alert | Email / Telegram |

Alternatif, monitor health endpoint (perlu expose via Nginx):

| Parameter | Value |
|-----------|-------|
| URL | `https://<domain>/actuator/health` |
| Keyword | `UP` |

**Perhatian:** Actuator endpoint sebaiknya tidak di-expose publik. Gunakan halaman login untuk monitoring eksternal, atau konfigurasi Nginx untuk membatasi akses actuator ke IP tertentu:

```nginx
location /actuator/health {
    proxy_pass http://127.0.0.1:10000;
    allow 127.0.0.1;
    allow <monitoring-ip>;
    deny all;
}
```

## Monitoring Memori

### JVM Heap

```bash
# Cek penggunaan memori JVM via jcmd
sudo -u akunting jcmd $(pgrep -f aplikasi-akunting.jar) VM.native_memory summary

# Atau via jstat (GC statistics)
sudo -u akunting jstat -gc $(pgrep -f aplikasi-akunting.jar)
```

### Resident Set Size (RSS)

RSS menunjukkan total memori fisik yang digunakan proses JVM:

```bash
# RSS dalam KB
ps -o rss= -p $(pgrep -f aplikasi-akunting.jar)

# Dengan format yang lebih readable
ps -o pid,rss,vsz,comm -p $(pgrep -f aplikasi-akunting.jar)
```

Patokan penggunaan memori normal:

| Metrik | Normal | Perlu investigasi |
|--------|--------|-------------------|
| RSS | 600-900 MB | > 1200 MB |
| Heap Used | 200-500 MB | > 800 MB (mendekati Xmx) |

### Memori Sistem

```bash
# Overview memori
free -h

# Detail per proses (top 10)
ps aux --sort=-%mem | head -11
```

## Monitoring Disk

```bash
# Disk usage
df -h

# Direktori terbesar
du -sh /opt/aplikasi-akunting/*
du -sh /var/log/aplikasi-akunting/*
du -sh /opt/aplikasi-akunting/backup/*
```

### Alert Disk Space

Buat script monitoring disk:

```bash
#!/bin/bash
# /opt/aplikasi-akunting/scripts/check-disk.sh
THRESHOLD=85
USAGE=$(df -h / | awk 'NR==2 {print $5}' | sed 's/%//')

if [ "$USAGE" -gt "$THRESHOLD" ]; then
    echo "ALERT: Disk usage ${USAGE}% exceeds threshold ${THRESHOLD}%"
    # Kirim notifikasi (email, Telegram, dll)
fi
```

Tambahkan ke cron:

```bash
# Cek disk setiap jam
0 * * * * /opt/aplikasi-akunting/scripts/check-disk.sh >> /var/log/aplikasi-akunting/monitoring.log 2>&1
```

## Monitoring Database

### Koneksi Aktif

```bash
sudo -u postgres psql -d accountingdb \
  -c "SELECT count(*) as active_connections FROM pg_stat_activity WHERE datname = 'accountingdb';"
```

### Database Size

```bash
sudo -u postgres psql -d accountingdb \
  -c "SELECT pg_size_pretty(pg_database_size('accountingdb')) as db_size;"
```

### Slow Queries

```bash
sudo -u postgres psql -d accountingdb \
  -c "SELECT pid, now() - pg_stat_activity.query_start AS duration, query
      FROM pg_stat_activity
      WHERE (now() - pg_stat_activity.query_start) > interval '5 seconds'
      AND state != 'idle';"
```

### Table Bloat

```bash
sudo -u postgres psql -d accountingdb \
  -c "SELECT schemaname, relname, n_dead_tup, last_autovacuum
      FROM pg_stat_user_tables
      WHERE n_dead_tup > 1000
      ORDER BY n_dead_tup DESC;"
```

## Checklist Monitoring

### Harian

- [ ] Backup log menunjukkan sukses
- [ ] Aplikasi berjalan (`systemctl is-active aplikasi-akunting`)
- [ ] Health check merespons OK

### Mingguan

- [ ] Review application log untuk error
- [ ] Cek disk usage (< 85%)
- [ ] SSL certificate > 30 hari valid
- [ ] Cek jumlah koneksi database

### Bulanan

- [ ] Test prosedur restore
- [ ] Rotasi backup lama
- [ ] Update system packages (`sudo apt update && sudo apt upgrade`)
- [ ] Review GC log untuk memory leak

## Selanjutnya

Lihat [Updates](05-updates.md) untuk prosedur upgrade aplikasi.
