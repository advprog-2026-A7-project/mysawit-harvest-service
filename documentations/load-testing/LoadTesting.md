# Load Testing

Load testing dilakukan menggunakan Apache JMeter dengan konfigurasi sebagai berikut:

- **300 virtual users**: merepresentasikan kondisi peak load realistis aplikasi internal perkebunan.
- **ramp up 10 seconds**: merepresentasikan kondisi di mana pengguna masuk ke sistem secara bergantian, bukan serentak di waktu yang sama.
- **5 loop per user**: merepresentasikan pola akses dalam satu sesi, karena user tidak hanya membuka halaman sekali, tetapi melakukan beberapa request seperti filtering dan refresh.

## 1. `GET /harvests/my` — Harvester View Harvest

`cat assets/load-testing/results/LoadTesting_HarvesterViewHarvest.jtl`

| Metric | Result |
|---|--------|
| Total Requests | 1500   |
| Success Rate | 100%   |
| Min | 25ms   |
| Max | 259ms  |
| Average | 48.2ms |
| Median (P50) | 38ms   |
| P90 | 98ms   |
| P95 | 116ms  |
| P99 | 161ms  |

Endpoint ini menunjukkan performa sangat baik dengan average **48.2ms** dan 0% error rate. P99 di angka 161ms menunjukkan bahwa bahkan di kondisi terburuk sekalipun, 99% request masih direspons di bawah 200ms.

## 2. `GET /harvests` — Foreman View Harvest

`cat assets/load-testing/results/LoadTesting_ForemanViewHarvest.jtl`

| Metric | Result  |
|---|---------|
| Total Requests | 1500    |
| Success Rate | 100%    |
| Min | 80ms    |
| Max | 1402ms  |
| Average | 831.1ms |
| Median (P50) | 871ms   |
| P90 | 1325ms  |
| P95 | 1359ms  |
| P99 | 1386ms  |

Endpoint ini menunjukkan performa lebih lambat dibanding `/harvests/my` dengan average **831.1ms** dan 0% error rate. Hal ini wajar karena foreman mengambil data seluruh harvester di bawah pengawasannya dalam satu query, sehingga volume data yang diproses jauh lebih besar dibanding harvester yang hanya mengambil datanya sendiri. P99 di angka 1386ms menunjukkan bahwa bahkan di kondisi terburuk sekalipun, 99% request masih direspons di bawah 1.5 detik.
