# Design Pattern

## 1. State Pattern

![Design Pattern - State](documentations/design-pattern/images/State.png)

Saya terapkan pada pengelolaan status harvest, yaitu `PENDING`, `APPROVED`, dan `REJECTED`. Implementasinya dapat dilihat pada `HarvestState`, `PendingState`, `ApprovedState`, dan `RejectedState`.

Pattern ini digunakan karena perilaku update status harvest bergantung pada kondisi status saat ini. Harvest dengan status `PENDING` masih dapat diubah menjadi `APPROVED` atau `REJECTED`, sedangkan harvest yang sudah `APPROVED` atau `REJECTED` tidak dapat diubah lagi. Dengan State Pattern, aturan perubahan status ini tidak ditumpuk dalam banyak `if-else` di service implementation.

Sebelum menerapkan State Pattern, logic perubahan status ditulis langsung di dalam `HarvestServiceImpl`. Service harus cek status harvest saat ini, status tujuan, rejection reason, dan aturan validasi lainnya dalam satu method yang sama. Akibatnya, method update status menjadi panjang, penuh percabangan, serta sulit dikembangkan ketika ada status baru.

Setelah menerapkan State Pattern, aturan perubahan status dipindahkan ke class state masing-masing. `PendingState` menangani perubahan dari pending ke approved atau rejected, sedangkan `ApprovedState` dan `RejectedState` menangani kondisi ketika status sudah final dan tidak boleh diubah lagi. Dengan begitu, `HarvestServiceImpl` cukup menentukan state saat ini dan memanggil aksi berikutnya yang sesuai.

Penerapan pattern ini bermanfaat karena setiap state memiliki tanggung jawabnya sendiri. Jika di masa depan ada status baru, sebagai contoh adalah `CANCELLED`, maka saya cukup untuk menambahkan class state baru tanpa mengubah banyak logic di `HarvestServiceImpl`. Hal ini membuat kode lebih mudah dibaca, diuji, dan dipelihara.

## 2. Chain of Responsibility Pattern

![Design Pattern - Chain of Responsibility](documentations/design-pattern/images/Chain-Of-Responsibility.png)

Chain of Responsibility Pattern diterapkan pada proses validasi ketika user melakukan log harvest. Implementasinya terdapat pada `AlreadyLoggedTodayHandler`, `HarvestDataHandler`, `HarvesterAssignedHandler`, `HarvestValidationChain`, dan `HarvestValidationHandler`.

Pattern ini digunakan karena proses validasi log harvest terdiri dari beberapa aturan yang berbeda dan dapat dipisahkan. Misalnya, perlu memeriksa apakah harvester sudah melakukan log pada hari yang sama, apakah harvester valid dan sudah assigned ke mandor, serta apakah data harvest seperti berat, foto, dan berita sudah sesuai.

Sebelum menerapkan Chain of Responsibility Pattern, semua validasi tersebut berada langsung di dalam method `logHarvest` pada `HarvestServiceImpl`. Method tersebut jadi memiliki terlalu banyak tanggung jawab karena harus menangani validasi user, validasi assignment, validasi data harvest, sekaligus proses pembuatan harvest. Hal ini membuat service menjadi panjang dan sulit diuji secara terpisah.

Setelah menerapkan Chain of Responsibility Pattern, setiap validasi ditempatkan pada handler yang berbeda. `AlreadyLoggedTodayHandler` menangani validasi apakah user sudah melakukan log hari ini, `HarvesterAssignedHandler` menangani validasi role dan assignment harvester, sedangkan `HarvestDataHandler` menangani validasi data harvest. Setiap handler hanya bertanggung jawab terhadap satu jenis validasi, lalu meneruskan proses ke handler berikutnya.

Manfaat maintainabilitynya adalah aturan validasi baru dapat ditambahkan dengan membuat handler baru dan memasukkannya ke dalam chain. Perubahan pada satu validasi juga tidak mengganggu validasi lain, sehingga kode lebih mudah untuk dikembangkan dan diuji secara terpisah.

## 3. Proxy Pattern

![Design Pattern - Proxy](documentations/design-pattern/images/Proxy.png)

Proxy Pattern diterapkan pada `HarvestServiceProxy`. Class ini mengimplementasikan interface `HarvestService` dan membungkus `HarvestServiceImpl`.

Pattern ini digunakan untuk menambahkan lapisan pengecekan akses sebelum request diteruskan ke service utama. Contohnya, hanya harvester yang boleh melakukan `logHarvest`, hanya harvester yang boleh melihat riwayat harvest miliknya sendiri, dan hanya foreman yang boleh melihat riwayat tim atau melakukan update status harvest.

Sebelum menerapkan Proxy Pattern, pengecekan akses berdasarkan role berpotensi ditulis langsung di dalam `HarvestServiceImpl`. Akibatnya, business logic seperti membuat harvest, mengambil data harvest, atau update status akan bercampur dengan authorization logic. Service utama menjadi kurang fokus karena menangani proses bisnis sekaligus pengecekan permission.

Setelah menerapkan Proxy Pattern, pengecekan akses dipindahkan ke `HarvestServiceProxy`. Proxy menjadi lapisan perantara yang memeriksa apakah user memiliki identity dan role yang sesuai sebelum request diteruskan ke `HarvestServiceImpl`. Jika akses tidak valid, proxy langsung menolak request. Jika valid, request baru diteruskan ke service utama.

Dengan adanya proxy, logic authorization awal tidak dicampur langsung ke dalam business logic utama. `HarvestServiceImpl` dapat fokus pada proses inti seperti membuat harvest, mengambil data, mengubah status, dan publish event. Sementara itu, `HarvestServiceProxy` bertanggung jawab sebagai gatekeeper akses.

Manfaat maintainabilitynya adalah aturan akses lebih mudah ditemukan, diubah, dan diuji. Jika terdapat perubahan role atau aturan permission baru, perubahan dapat dilakukan pada proxy tanpa harus mengubah logic utama pada service implementation.

## 4. Observer Pattern

![Design Pattern - Observer](documentations/design-pattern/images/Observer.png)

Observer Pattern diterapkan pada komunikasi event menggunakan RabbitMQ. Contohnya terdapat pada `HarvestPayrollEventPublisher`, serta beberapa consumer seperti `UserRegisteredEventConsumer`, `UserAssignedEventConsumer`, dan consumer event lainnya.

Dalam project ini, Observer Pattern diwujudkan dalam bentuk publish-subscribe menggunakan message broker RabbitMQ. Secara konsep, publisher menerbitkan event, lalu consumer atau subscriber yang tertarik terhadap event tersebut akan menerima dan memprosesnya. Perbedaannya dengan Observer Pattern klasik adalah publisher dan subscriber tidak berhubungan langsung di dalam memory aplikasi yang sama, tetapi dipisahkan oleh message broker. Karena itu, implementasi ini dapat disebut sebagai penerapan konsep Observer Pattern dalam arsitektur event-driven messaging.

Pattern ini digunakan karena harvest service perlu berkomunikasi dengan service lain tanpa membuat dependency langsung. Misalnya, ketika harvest sudah disetujui, `HarvestPayrollEventPublisher` mengirim event agar service payroll dapat memproses data tersebut. Di sisi lain, harvest service juga menerima event dari service lain, seperti event user registered atau user assigned, untuk memperbarui data replica user.

Sebelum menerapkan Observer Pattern, harvest service berpotensi harus memanggil service lain secara langsung. Contohnya, ketika harvest disetujui, harvest service harus langsung memanggil payroll service untuk membuat data payroll yang membuat harvest service bergantung langsung pada service lain. Jika payroll service berubah, mengalami gangguan, atau membutuhkan format request baru, harvest service juga ikut terdampak.

Setelah menerapkan Observer Pattern melalui RabbitMQ, harvest service cukup menerbitkan event ketika terjadi perubahan penting, seperti harvest yang sudah approved. Service lain yang membutuhkan informasi tersebut dapat menjadi consumer dari event yang dikirim. Dengan pendekatan ini, harvest service tidak perlu mengetahui detail implementasi service lain.

Manfaat maintainabilitynya adalah coupling antarservice menjadi lebih rendah. Harvest service tidak perlu memanggil service lain secara langsung, sehingga perubahan pada service lain tidak selalu memaksa perubahan besar pada harvest service. Pattern ini juga membuat sistem lebih fleksibel dan scalable sehingga cocok untuk arsitektur microservices.
