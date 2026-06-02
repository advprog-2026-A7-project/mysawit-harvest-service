# Monitoring

Monitoring diimplementasikan menggunakan **Prometheus** dan **Grafana** yang dijalankan
via Docker. Metrics diambil dari Spring Boot Actuator endpoint `/actuator/prometheus`
dan divisualisasikan dalam bentuk dashboard di Grafana.

## Menjalankan Monitoring

```bash
docker-compose -f docker-compose.local.yml up -d
```

## Mengakses Dashboard

| Service | URL | Username | Password |
|---|---|---|---|
| Prometheus | http://localhost:9090 | - | - |
| Grafana | http://localhost:3001 | admin | admin |

## Metrics Endpoint

Endpoint berikut digunakan oleh Prometheus untuk mengambil metrics:

```
http://localhost:8083/actuator/prometheus
```

## Menghentikan Monitoring

```bash
docker-compose -f docker-compose.local.yml down
```

## Hasil
![Monitoring - Dashboard](documentations/monitoring/images/Dashboard.png)

Monitoring MySawit Harvest Service dibangun di atas dashboard Grafana untuk memantau health service secara real-time. Metric yang ditampilkan meliputi uptime, request rate, 5xx error ratio, HTTP p95 latency, JVM memory usage, CPU usage, database connections, dan JVM threads. Harvest service menangani proses-proses yang cukup vital sehingga deteksi dini terhadap gangguan menjadi hal yang penting.

Dari dashboard yang ada, terlihat bahwa:
- **5xx Error Ratio menunjukkan angka 0%**, artinya tidak ada error server yang terjadi selama periode pemantauan. 
- **Request rate** berjalan di 0.0729 req/s dan **HTTP p95 Latency** tercatat di **102ms**, yang berarti 95% request direspons dalam waktu di bawah 102ms. 
- **JVM Memory** terpantau stabil dengan heap di sekitar 144 MiB dan nonheap di 112 MiB, tidak ada indikasi memory leak. 
- **CPU Usage** sempat mengalami spike hingga 25% namun kembali normal, yang wajar terjadi saat ada lonjakan request. 
- **Database Connections (HikariCP)** menunjukkan 10 koneksi total dengan active dan pending yang minimal, menandakan koneksi ke database berjalan normal. 
- **JVM Threads** stabil di 35 threads tanpa anomali yang perlu diwaspadai.
