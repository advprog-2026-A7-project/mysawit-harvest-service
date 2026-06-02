# mysawit-harvest-service

Spring Boot (Java + Gradle) microservice for MySawit.

---

## Run (local)
```
./gradlew bootRun
```

Runs at:

```
http://localhost:8083
```

---

## Health

* GET `/actuator/health`

---

## Main Endpoints

Semua request yang membutuhkan autentikasi wajib menyertakan header:
`Authorization: Bearer <JWT_TOKEN>`

### 1. Log Harvest (Catat Hasil Panen)

Digunakan oleh Harvester untuk mencatat hasil panen baru beserta unggahan foto bukti 

* **URL:** `/harvests`
* **Method:** `POST`
* **Content-Type:** `multipart/form-data`
* **Headers:** `Authorization`
* **Payload (Form Data):**
* `request` (application/json): Data detail panen.
```json
{
  "plantationId": "PLT-001",
  "weight": 150.5,
  "news": "Panen berjalan lancar di blok B"
}

```
* `files` (Binary): Satu atau beberapa file foto hasil panen (Wajib).


* **Response Success (`201 Created`):**
```json
{
  "id": "c9a646d3-9c61-4c7e-9d2a-8742b66a5a22",
  "message": "Harvest successfully logged"
}

```

---

### 2. View My History (Riwayat Panen Mandiri)

Digunakan oleh Harvester untuk melihat riwayat panen mereka sendiri. Mendukung filter data melalui query parameters.

* **URL:** `/harvests/my`
* **Method:** `GET`
* **Headers:** `Authorization`
* **Query Parameters (Optional Filters):**
* `startDate` (e.g., `2026-05-01`)
* `endDate` (e.g., `2026-05-22`)
* `status` (e.g., `PENDING`, `REJECTED`, `APPROVED`)


* **Response Success (`200 OK`):**
```json
[
  {
    "id": "5c2495a2-9b04-4547-a1f0-f08fa23683ad",
    "plantationId": "PLT-001",
    "harvesterId": "fd754333-a241-4ca9-893d-6d6f490beac1",
    "foremanId": "75913015-3ea0-456a-9e47-c4287db90047",
    "harvesterName": "buruhtest1",
    "weight": 150.5,
    "news": "Panen berjalan lancar di blok B",
    "photos": [
      "https://brgpwleqgvgnrvxvvpzn.supabase.co/storage/v1/object/public/harvest-photos/5c6d97fe-18b6-46fa-84e4-951f120f675f.jpeg",
      "https://brgpwleqgvgnrvxvvpzn.supabase.co/storage/v1/object/public/harvest-photos/4dca6151-c073-4fbf-8579-1beba0f7988d.jpg"
    ],
    "status": "APPROVED",
    "rejectionReason": null,
    "harvestDate": "2026-05-21T20:01:11.687974",
    "statusUpdatedDate": "2026-05-21T20:02:41.2798"
  }
]

```


---

### 3. View All History (Semua Riwayat Panen)

Digunakan oleh Foreman (Mandor) untuk melihat seluruh atau memfilter riwayat panen di bawah pengawasannya.

* **URL:** `/harvests`
* **Method:** `GET`
* **Headers:** `Authorization`
* **Query Parameters (Optional Filters):**
* `harvesterId`
* `status`


* **Response Success (`200 OK`):**
```json
[
  {
    "id": "c402946c-995a-49ea-afd1-d06f3871f685",
    "plantationId": "PLT-001",
    "harvesterId": "4ef19fab-e2dd-416d-a4fa-21007cfe4a78",
    "foremanId": "75913015-3ea0-456a-9e47-c4287db90047",
    "harvesterName": "buruhtest3",
    "weight": 150.5,
    "news": "Panen berjalan lancar di blok B",
    "photos": [
      "https://brgpwleqgvgnrvxvvpzn.supabase.co/storage/v1/object/public/harvest-photos/4281b6f0-e9ec-451d-8f30-402685cc5759.jpeg",
      "https://brgpwleqgvgnrvxvvpzn.supabase.co/storage/v1/object/public/harvest-photos/74a8ec1e-ab95-42fe-ad44-fd17e2d2acb7.jpg"
    ],
    "status": "APPROVED",
    "rejectionReason": null,
    "harvestDate": "2026-05-21T23:51:57.362803",
    "statusUpdatedDate": "2026-05-21T23:52:37.224886"
  }
]
```

---

### 4. Get Harvest Detail (Detail Riwayat Panen)

Mengambil data spesifik dari satu log panen berdasarkan ID uniknya. Bisa diakses oleh Harvester pemilik atau Foreman terkait.

* **URL:** `/harvests/{id}`
* **Method:** `GET`
* **Headers:** `Authorization`
* **URL Path Variables:**
* `id` (UUID) -> Contoh: `/harvests/c9a646d3-9c61-4c7e-9d2a-8742b66a5a22`


* **Response Success (`200 OK`):**
```json
{
  "id": "5c2495a2-9b04-4547-a1f0-f08fa23683ad",
  "plantationId": "PLT-001",
  "harvesterId": "fd754333-a241-4ca9-893d-6d6f490beac1",
  "foremanId": "75913015-3ea0-456a-9e47-c4287db90047",
  "harvesterName": "buruhtest1",
  "weight": 150.5,
  "news": "Panen berjalan lancar di blok B",
  "photos": [
    "https://brgpwleqgvgnrvxvvpzn.supabase.co/storage/v1/object/public/harvest-photos/5c6d97fe-18b6-46fa-84e4-951f120f675f.jpeg",
    "https://brgpwleqgvgnrvxvvpzn.supabase.co/storage/v1/object/public/harvest-photos/4dca6151-c073-4fbf-8579-1beba0f7988d.jpg"
  ],
  "status": "APPROVED",
  "rejectionReason": null,
  "harvestDate": "2026-05-21T20:01:11.687974",
  "statusUpdatedDate": "2026-05-21T20:02:41.2798"
}
```

---

### 5. Update Status (Ubah Status Panen)

Digunakan oleh Foreman untuk menyetujui (`APPROVED`) atau menolak (`REJECTED`) ajuan catatan panen dari Harvester.

* **URL:** `/harvests/update`
* **Method:** `PATCH`
* **Content-Type:** `application/json`
* **Headers:** `Authorization`
* **Body:**
```json
{
  "harvestId": "c9a646d3-9c61-4c7e-9d2a-8742b66a5a22",
  "status": "APPROVED"
}

```

* **Response Success (`200 OK`):**
```json
{
  "id": "c9a646d3-9c61-4c7e-9d2a-8742b66a5a22",
  "status": "APPROVED",
  "updatedAt": "2026-05-22T16:00:00Z"
}

```

---
