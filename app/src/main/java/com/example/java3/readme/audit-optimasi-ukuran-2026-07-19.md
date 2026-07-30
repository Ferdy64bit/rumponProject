# Audit Optimasi Ukuran Project - 2026-07-19

## Ringkasan

Project sempat terlihat sangat besar bukan karena source code utama, tetapi karena cache Gradle, JDK hasil download Gradle, dependency transform, dan output build lokal.

Ukuran terbesar sebelum optimasi:

| Folder | Ukuran | Status |
|---|---:|---|
| `.g` | 1569.43 MB | Cache Gradle lokal, aman dibersihkan |
| `.gradle-local` | 341.26 MB | Cache Gradle lokal, aman dibersihkan |
| `.gradle` | 181.44 MB | Cache Gradle project, aman dibersihkan |
| `app` | 73.94 MB | Membesar karena `app/build`, bukan source utama |
| `build` | 6.15 MB | Output build root, aman dibersihkan |

Setelah pembersihan cache dan build output, source utama project kembali ringan. Folder cache bisa muncul lagi ketika Gradle build dijalankan, tetapi sekarang sudah masuk `.gitignore`.

## Perbaikan Yang Dilakukan

- Membersihkan cache/build output lokal: `.g`, `.gradle-local`, `.gradle`, `build`, dan `app/build`.
- Menambahkan `.g`, `.gradle-local`, dan `**/build/` ke `.gitignore` agar folder besar tidak ikut terbawa ke version control.
- Menghapus `TIDE_API_KEY` dari `app/build.gradle.kts` karena migrasi maritim sudah memakai BMKG Peta Maritim yang tidak membutuhkan API key TideCheck.
- Membuat `app/src/main/assets/bmkg_regions.json` dari file daftar endpoint BMKG.
- `bmkg_regions.json` berisi 232 region BMKG dengan struktur `code`, `area`, dan `url`.

## Hasil Build

Perintah verifikasi:

```text
.\gradlew.bat :app:compileDebugJavaWithJavac
```

Hasil:

```text
BUILD SUCCESSFUL in 8m 25s
12 actionable tasks: 12 executed
```

Catatan: build pertama setelah cache dibersihkan akan lebih lama karena Gradle perlu mengunduh ulang wrapper/dependency. Build berikutnya akan lebih cepat selama cache lokal tidak dibersihkan lagi.

## Catatan BMKG

Data endpoint BMKG sekarang tersedia dalam aset lokal:

```text
app/src/main/assets/bmkg_regions.json
```

File ini disiapkan agar tahap berikutnya bisa memilih area BMKG secara dinamis tanpa hardcode URL satu per satu di source code Java.

## Kebijakan Cache API Eksternal

Cache API eksternal dibuat terbatas agar data JSON cuaca/BMKG tidak menyebabkan pembengkakan Firestore atau storage lokal aplikasi.

| Cache | Collection | TTL fresh | Fallback maksimal | Cleanup |
|---|---|---:|---:|---|
| OpenWeather | `weather_cache` | 10 menit | 24 jam | Hapus batch dokumen lama setelah cache baru disimpan |
| BMKG Peta Maritim | `tide_cache` sementara | 30 menit | 24 jam | Hapus batch dokumen lama setelah cache baru disimpan |

Field cache yang disiapkan:

| Field | Fungsi |
|---|---|
| `id` | Key stabil per koordinat/area |
| `data` | JSON response yang sudah diparsing ke model aplikasi |
| `source` | Sumber API, misalnya `OpenWeather` atau `BMKG` |
| `area` | Nama area BMKG, khusus cache maritim |
| `updatedAt` | Waktu cache terakhir diperbarui |
| `expiresAt` | Waktu cache dianggap tidak fresh |
| `dataSizeBytes` | Ukuran data JSON untuk audit pembengkakan |

Perilaku saat API gagal:

- Jika cache fresh tersedia, aplikasi langsung memakai cache.
- Jika cache sudah tidak fresh tetapi masih di bawah 24 jam, cache dipakai sebagai fallback agar UI tetap menampilkan data.
- Jika cache kosong atau terlalu lama, UI menerima error/placeholder dan aplikasi tidak crash.

Catatan migrasi berikutnya: ketika struktur BMKG sudah dibuat eksplisit, collection `tide_cache` sebaiknya diganti/diarahkan ke `bmkg_cache` agar penamaan sesuai sumber data baru.

Prioritas tahap berikutnya:

1. Buat loader untuk membaca `bmkg_regions.json` dari assets.
2. Buat model region BMKG.
3. Buat selector area BMKG berdasarkan lokasi user atau lokasi fishing point.
4. Refactor `TideRepository/TideResponse` menjadi struktur BMKG yang lebih eksplisit bila sudah siap migrasi penuh.
5. Tampilkan forecast multi-day secara dinamis di dashboard.

## Implementasi BMKG Forecast - 2026-07-19

Progress implementasi setelah audit cache:

- Menambahkan model `BMKGForecast` untuk forecast multi-item BMKG.
- Menambahkan model `BMKGResponse`, `BMKGRegion`, dan `BMKGCache` sebagai fondasi penamaan domain BMKG.
- Menambahkan `BMKGRepository` sebagai bridge repository agar migrasi dari nama lama Tide menuju BMKG bisa dilakukan bertahap tanpa memutus ViewModel lama.
- `TideResponse` sekarang membawa `List<BMKGForecast>` agar UI lama tetap kompatibel tetapi data BMKG sudah bisa tampil sebagai daftar.
- Parser BMKG di repository dibuat fleksibel untuk membaca array seperti `data`, `forecast`, `forecasts`, `items`, `details`, dan objek forecast yang memiliki field cuaca/gelombang/angin/warning.
- Dashboard menampilkan daftar forecast BMKG secara horizontal melalui `rvBmkgForecast`.
- Menambahkan `BMKGForecastAdapter` dan `item_bmkg_forecast.xml` untuk card forecast dinamis.
- Cache maritim sekarang diarahkan ke collection `bmkg_cache` melalui alias `COL_TIDE_CACHE` agar kode lama tetap berjalan.
- Gradle diarahkan ke JBR 21 bawaan Android Studio agar tidak mengunduh JDK toolchain berulang dan tidak membengkakkan cache.

Verifikasi build:

```text
.\gradlew.bat :app:compileDebugJavaWithJavac
BUILD SUCCESSFUL in 3m 29s
```

## File Yang Aman Dibersihkan Secara Berkala

Folder berikut aman dibersihkan saat project terasa besar:

```text
.g
.gradle-local
.gradle
build
app/build
```

Konsekuensinya, build pertama setelah pembersihan akan lebih lama karena cache dibuat ulang.
