# SQA Phase 1 - Project Audit

Tanggal audit: 26 Juli 2026

Project: **Fishing Point Tanjung Anom**

Status: **Audit tanpa perubahan kode**

Dokumen acuan:

- `masterruleoop.md`
- `uiuxx.md`
- `auidituiux.md`
- `roadmap-progress-2026-07-19.md`
- `next-step-decision-2026-07-19.md`

## Tujuan Audit

Audit ini dibuat sebagai langkah awal peningkatan kualitas software sebelum refactor dan testing. Fokusnya adalah membaca struktur project, menemukan risiko maintainability, dan menentukan area yang perlu diprioritaskan tanpa mengubah behavior aplikasi.

Sesuai master rule, tahap ini tidak melakukan:

- perubahan kode,
- perubahan UI,
- perubahan arsitektur,
- perubahan schema Firestore,
- penambahan dependency,
- implementasi unit test.

## Ringkasan Eksekutif

Project sudah memiliki struktur dasar yang sehat untuk aplikasi Android skripsi berbasis Java, XML, MVVM, Repository Pattern, Firestore, Google Maps, Cloudinary, BMKG, OpenWeather, dan Open-Meteo Marine.

Struktur package utama sudah cukup jelas:

```text
core/
data/
domain/
presentation/
```

Keputusan arsitektur yang sudah baik:

- Presentation layer dipisahkan dalam `activities`, `fragments`, `adapters`, `viewmodels`, `views`, dan `maps`.
- Data access dipusatkan di `data/repository`.
- Response API diletakkan di `data/remote`.
- Model domain diletakkan di `domain/model`.
- Formula rekomendasi utama sudah berada di `domain/service/RecommendationEngine`.
- Utility umum seperti Haversine berada di `core/utils/LocationUtils`.

Kesimpulan awal: project **tidak perlu ganti arsitektur**. Kualitas software lebih tepat ditingkatkan melalui refactor kecil, pemindahan helper logic yang tersebar, dan penambahan testing pada logic murni.

## Dependency Audit

Dependency utama yang ditemukan:

| Dependency | Status | Catatan |
| --- | --- | --- |
| AndroidX AppCompat | Dipakai | Aman untuk support UI klasik Java/XML. |
| Material Components | Dipakai | Sesuai target Material Design 3. |
| ConstraintLayout | Dipakai | Sesuai rule UI responsive. |
| Google Maps SDK | Dipakai | Inti fitur Maps/LBS. |
| Play Services Location | Dipakai | Inti fitur lokasi user. |
| Android Maps Utils | Dipakai | Mendukung marker clustering. |
| Retrofit + Gson Converter | Dipakai | Dipakai untuk API OpenWeather/BMKG/Open-Meteo. |
| Firebase Auth | Dipakai | Login/register/session/reset password. |
| Cloud Firestore | Dipakai | Database utama. |
| Glide | Dipakai | Image loading Cloudinary/default image. |
| Lifecycle ViewModel + LiveData | Dipakai | Mendukung MVVM. |
| Room | Perlu audit lanjutan | Ada di dependency, tetapi belum terlihat sebagai komponen utama pada source. Jika tidak dipakai, kandidat pengurangan dependency saat optimasi release. |
| JUnit | Dipakai nanti | Cocok untuk unit test logic murni. |
| Espresso | Opsional | Cocok untuk UI smoke test, tetapi black box manual tetap lebih realistis untuk skripsi. |

Keputusan dependency:

- Jangan menambah Lombok.
- Jangan menambah Hibernate.
- Jangan menambah Dagger/Hilt.
- Jangan menambah dependency besar sebelum Phase 1-7 selesai.
- Audit Room pada fase optimasi: jika benar tidak dipakai, bisa dipertimbangkan untuk dihapus setelah testing utama.

## Struktur Package

### core

Isi utama:

- `FirebaseManager`
- `NetworkModule`
- `WeatherService`
- `TideService`
- `MarineWeatherService`
- `BMKGAreaSelector`
- `Constants`
- `LocationUtils`
- `SessionManager`

Kondisi:

- Package `core` sudah menjalankan fungsi umum dan infrastruktur.
- `LocationUtils` sudah tepat sebagai utility Haversine.
- `NetworkModule` sudah tepat sebagai pembuat Retrofit service.
- `Constants` menjadi pusat konfigurasi, tetapi perlu terus dijaga agar tidak menjadi class terlalu besar.

Risiko:

- Jika helper baru terus dimasukkan ke `Constants`, class ini bisa menjadi God Object kecil.
- `BMKGAreaSelector` dan logic polygon BMKG perlu dicek duplikasi dengan `TideRepository` karena ada indikasi parsing/selector juga berada di repository.

### data/remote

Isi utama:

- `WeatherResponse`
- `TideResponse`
- `MarineHourlyResponse`
- `TideStation`

Kondisi:

- Response API sudah dipisahkan dari UI.
- Beberapa response object memiliki helper label/formatter.

Risiko:

- Jika response object terlalu banyak berisi formatter UI, model data menjadi bercampur dengan presentation concern.
- `TideResponse` cukup besar dan menyimpan beberapa legacy compatibility method; ini masih masuk akal karena migrasi dari TideCheck ke BMKG, tetapi perlu dokumentasi.

### data/repository

Isi utama:

- `AuthRepository`
- `BMKGRepository`
- `CommunityRepository`
- `DashboardStatsRepository`
- `FavoriteRepository`
- `FishingRepository`
- `MarineHourlyRepository`
- `NotificationRepository`
- `ProfileRepository`
- `ReviewRepository`
- `SpotRepository`
- `TideRepository`
- `UserRepository`
- `WeatherRepository`

Kondisi:

- Repository Pattern sudah diterapkan konsisten.
- API eksternal dan Firestore mayoritas sudah melewati repository.
- Cache Weather/BMKG/Marine sudah diarahkan ke repository.

Risiko utama:

- Beberapa repository terlalu besar dan memiliki banyak tanggung jawab.
- Upload Cloudinary muncul di beberapa repository dengan pola mirip.
- `TideRepository` memuat network, cache, parser JSON, polygon selector, fallback, mapper, dan formatter endpoint dalam satu class.
- `CommunityRepository` memuat feed realtime, author enrichment, Cloudinary upload, like, favorite, comment, dan cleanup delete.
- `ProfileRepository` memuat listener profile, stats, upload Cloudinary, reset password, my posts, my spots, favorite spots, dan sync community author.

### domain/model

Isi utama:

- `FishingPoint`
- `Post`
- `User`
- `RecommendationResult`
- `BMKGForecast`
- `BMKGRegion`
- `WeatherCache`
- `TideCache`
- `BMKGCache`
- `Favorite`
- `Review`
- `Notification`

Kondisi:

- Model utama sudah eksplisit dan mudah dibaca oleh penguji skripsi.
- Keputusan tidak memakai Lombok adalah tepat karena getter/setter eksplisit lebih mudah dijelaskan.

Risiko:

- Beberapa model memiliki logic label/formatting. Ini boleh untuk compatibility, tetapi jika bertambah banyak sebaiknya dipindah ke formatter/helper.
- `FishingPoint` perlu terus dijaga agar schema `ownerId`, `userId`, `ownerName`, `ownerPhoto`, dan `visibility` konsisten.

### domain/service

Isi utama:

- `RecommendationEngine`

Kondisi:

- Ini adalah keputusan arsitektur yang baik karena business logic rekomendasi tidak berada di Fragment.
- Formula final sudah terkonsentrasi di satu tempat.

Risiko:

- `RecommendationEngine` cukup besar dan menggabungkan distance score, weather score, marine score, fish activity, safety multiplier, solunar-lite, label, dan color.
- Banyak private method sebenarnya kandidat pure function yang sangat cocok untuk unit test, tetapi karena private tidak bisa diuji langsung tanpa menguji lewat `calculate`.

### presentation

Isi utama:

- Activities: `LoginActivity`, `RegisterActivity`, `SplashActivity`, `MainActivity`, `SpotListActivity`, `DetailSpotActivity`
- Fragments: `HomeFragment`, `MapFragment`, `CommunityFragment`, `CreatePostFragment`, `ProfileFragment`
- ViewModels: `HomeViewModel`, `MapViewModel`, `CommunityViewModel`, `ProfileViewModel`, auth viewmodels
- Adapters: `PostAdapter`, `FishingPointAdapter`, `SpotListAdapter`, `BMKGForecastAdapter`
- Custom view: `WaveHourlyChartView`
- Maps renderer: `FishingMarkerRenderer`

Kondisi:

- UI sudah final menurut keputusan project.
- ViewBinding dipakai.
- RecyclerView adapter sudah tersedia untuk list/feed/forecast.

Risiko:

- Beberapa Fragment/Activity masih memegang logic yang seharusnya bisa berada di helper/domain.
- `MapFragment` menjadi class terbesar dan memegang terlalu banyak tanggung jawab.
- `HomeFragment` memiliki banyak formatter/rendering helper.
- `DetailSpotActivity` memegang live environment loading, upload foto, favorite, owner check, dan render rekomendasi.
- `CreatePostFragment` membuat `WeatherRepository`, `TideRepository`, dan `CommunityRepository` langsung di Fragment sehingga unit testing UI logic lebih sulit.

## Class Size Audit

Class terbesar yang ditemukan:

| Class | Estimasi Baris | Risiko |
| --- | ---: | --- |
| `MapFragment.java` | 627 | God Fragment: maps lifecycle, marker, filter, route, dialog, permission, image, visibility. |
| `TideRepository.java` | 605 | Repository terlalu banyak tugas: BMKG network, parser, polygon, cache, fallback. |
| `CommunityRepository.java` | 585 | Repository besar: feed, author enrichment, upload, like, favorite, comment, cleanup. |
| `HomeFragment.java` | 507 | Rendering dashboard, lokasi, formatter, insight recommendation, error state dalam satu Fragment. |
| `ProfileRepository.java` | 472 | Profile, stats, upload, reset, post/spot/favorite, sync author dalam satu repository. |
| `DetailSpotActivity.java` | 447 | Detail screen terlalu banyak tugas: UI, upload, favorite, live API, recommendation, owner check. |
| `CommunityFragment.java` | 424 | Feed interaction dan dialog comment masih cukup padat. |
| `ProfileFragment.java` | 392 | Profile UI, dialog edit, stats, list ringkas, reset, logout. |
| `CreatePostFragment.java` | 366 | Create post UI, lokasi, API environment, image processing, submit post. |
| `FishingRepository.java` | 351 | Fishing point CRUD + Cloudinary + visibility helper. |
| `RecommendationEngine.java` | 333 | Business logic pusat, cocok untuk unit test dan refactor ringan. |

Catatan: class besar tidak otomatis salah. Namun class besar meningkatkan risiko bug saat maintenance dan membuat unit test lebih sulit.

## Duplicated Logic Audit

Duplikasi/pola berulang yang terlihat:

### 1. Cloudinary upload multipart

Muncul pada:

- `CommunityRepository`
- `ProfileRepository`
- `FishingRepository`

Risiko:

- Jika format upload berubah, tiga tempat perlu diubah.
- Handling error Cloudinary bisa tidak konsisten.
- Sulit membuat unit test karena logic network manual bercampur dengan repository domain.

Rekomendasi nanti:

- Refactor ringan ke helper/service kecil, misalnya `CloudinaryUploadService`, tetapi hanya setelah Phase 1-7 selesai.

### 2. String fallback helper

Pola seperti `nonBlank`, `firstNonBlank`, `isBlank`, `safeMessage`, dan `readableError` muncul di beberapa class.

Risiko:

- Format fallback bisa tidak konsisten antar screen.
- Unit test formatter sulit karena tersebar.

Rekomendasi nanti:

- Buat helper kecil jika benar-benar mengurangi duplikasi, misalnya `TextUtils` atau `DisplayTextFormatter`.
- Jangan bentrok dengan `android.text.TextUtils`; gunakan nama yang spesifik project jika dibuat.

### 3. Spot placeholder/image helper

Pola `getSpotPlaceholder` dan transformasi Cloudinary thumbnail muncul di presentation.

Risiko:

- Placeholder tipe spot bisa berbeda antara Map, Detail, dan List.

Rekomendasi nanti:

- Kandidat `SpotImageHelper` atau `SpotDisplayMapper`.

### 4. Visibility/owner helper

Sebagian sudah baik karena ada static helper di `FishingRepository`, seperti `canUserSeeSpot`, `isOwnedByCurrentUser`, dan `normalizeVisibility`.

Risiko:

- Karena helper ini berada di repository, domain rule visibility bercampur dengan data access.

Rekomendasi nanti:

- Untuk testability, kandidat dipindah ringan ke domain/helper `SpotVisibilityPolicy`, tetapi tidak wajib segera jika risiko perubahan dianggap tinggi.

### 5. Distance/Recommendation rendering

Distance Haversine sudah berada di `LocationUtils`, tetapi rendering jarak dan bintang masih tersebar di UI/adapters.

Risiko:

- Label jarak dan bintang bisa tidak konsisten.

Rekomendasi nanti:

- Kandidat `DistanceFormatter` dan `RecommendationFormatter`.

## Coupling Audit

Temuan coupling utama:

### Presentation langsung membuat repository

Contoh:

- `CreatePostFragment` membuat `WeatherRepository`, `TideRepository`, dan `CommunityRepository`.
- `DetailSpotActivity` membuat `FavoriteRepository` dan `FishingRepository`.
- `SpotListActivity` membuat `FishingRepository` dan `FavoriteRepository`.
- Beberapa ViewModel juga membuat repository langsung.

Status:

- Masih dapat diterima untuk project Android Java skripsi.
- Tidak perlu Dagger/Hilt karena master rule melarang dependency besar dan arsitektur sudah stabil.

Risiko:

- Unit testing ViewModel/Activity lebih sulit karena dependency tidak bisa diinjeksi dengan mudah.

Rekomendasi nanti:

- Jika perlu testability, tambahkan constructor overload pada ViewModel/repository tertentu secara bertahap, bukan framework DI.

### FirebaseAuth dipakai di presentation

Contoh:

- `MapFragment`
- `SpotListActivity`
- `DetailSpotActivity`
- `RegisterActivity`
- `LoginActivity`
- `CommunityViewModel`
- `HomeViewModel`

Status:

- Untuk login/register masih wajar.
- Untuk feature screen, lebih baik user identity disediakan melalui repository/session abstraction.

Risiko:

- Coupling ke Firebase menyulitkan unit test dan mock multi-akun.

Rekomendasi nanti:

- Buat helper ringan `CurrentUserProvider` hanya jika dibutuhkan pada fase refactor.

## Business Logic Location Audit

Business logic yang sudah berada di tempat baik:

- Haversine: `LocationUtils.calculateDistance`.
- Recommendation formula: `RecommendationEngine`.
- Weather/BMKG/Marine fetch/cache: repository.
- Firestore CRUD: repository.

Business/helper logic yang masih berada di UI layer:

- `MapFragment`: filter marker, map weather formatting, spot placeholder selection, Cloudinary thumbnail URL, visibility label conversion, route bounds logic.
- `HomeFragment`: recommendation subtitle, stars, safety label, activity label, marine status label, capitalize, nonBlank, error state rendering.
- `DetailSpotActivity`: owner check helper, visibility normalization, image placeholder, recommendation rendering, live data rendering.
- `ProfileFragment`: date formatting, profile list text construction, fallback text.
- `SpotListActivity`: filtering/sorting/favorite combination dan distance mapping.

Status:

- Tidak semua harus dipindah. UI rendering logic boleh tetap di UI.
- Yang perlu diprioritaskan untuk dipindah adalah logic yang ingin diuji otomatis atau dipakai ulang di banyak screen.

## OOP Risk Summary

### High Risk

1. `MapFragment` terlalu banyak tanggung jawab.
2. `TideRepository` terlalu besar dan sulit diuji karena parsing BMKG, polygon, cache, dan network bercampur.
3. `CommunityRepository`, `ProfileRepository`, dan `FishingRepository` memiliki duplikasi Cloudinary upload.
4. Helper formatting tersebar di UI layer.

### Medium Risk

1. `RecommendationEngine` besar, tetapi masih cohesive karena semua terkait scoring.
2. `DetailSpotActivity` cukup besar karena banyak action dalam satu screen.
3. `CreatePostFragment` memiliki image processing dan environment loading langsung.
4. Room dependency perlu dicek apakah benar dipakai.

### Low Risk

1. Model Java eksplisit tanpa Lombok masih baik untuk skripsi.
2. Repository direct construction masih dapat diterima selama tidak menambah framework DI.
3. UI final tidak perlu redesign ulang.

## Unit Test Candidate Awal

Kandidat paling kuat:

| Candidate | Alasan |
| --- | --- |
| `LocationUtils.calculateDistance` | Pure function, inti metode Haversine skripsi. |
| `RecommendationEngine.calculate` | Inti rekomendasi, perlu validasi skor dan boundary. |
| `RecommendationEngine.getFishActivityLabel` | Aktivitas ikan penting dan pernah dibandingkan dengan Fishing Points. |
| Visibility helper spot | Private/public dan owner-only adalah fitur penting. |
| Distance formatter | Menjaga label jarak konsisten. |
| Recommendation/star formatter | Menjaga bintang dan label rekomendasi konsisten. |
| Spot filtering/sorting helper | Spot terdekat/favorit perlu konsisten. |
| BMKG parser/area selector | Penting, tetapi perlu refactor agar parser bisa diuji tanpa network. |

Kandidat yang tidak disarankan untuk unit test pertama:

- Fragment/Activity besar.
- Repository yang langsung menyentuh Firebase/API.
- Adapter UI.
- Custom view chart.

Untuk komponen tersebut, black box/manual test lebih realistis pada tahap awal.

## Black Box Test Candidate Awal

Fitur yang wajib masuk black box test:

- Authentication: login, register, logout, reset password.
- Dashboard: lokasi user, weather live, marine hourly chart, BMKG forecast, recommendation insight.
- Maps: marker, clustering, search/filter, card spot, polyline, navigasi, detail.
- Spot: tambah, edit, delete, private/public, owner-only access.
- Detail Spot: live data, favorite, share, navigasi, foto spot, owner info.
- Community: create post, upload foto, like, unlike, comment, bookmark, share, delete.
- Profile: edit profile, upload foto, postingan saya, spot saya, spot favorit, reset password.
- API edge cases: internet mati, GPS mati, permission ditolak, API gagal, data kosong.
- Multi-akun: owner vs non-owner, private vs public.

## Refactor Candidate Awal

Belum boleh diimplementasikan pada fase ini. Ini hanya kandidat untuk Phase 3.

### Priority Tinggi

- Extract Cloudinary upload multipart yang duplikatif.
- Extract pure helper untuk visibility/owner spot agar bisa di-unit-test.
- Extract formatter yang dipakai lintas Dashboard/Map/Detail/List.
- Buat unit test Haversine dan RecommendationEngine.

### Priority Sedang

- Pecah parsing BMKG dari `TideRepository` menjadi mapper/parser yang bisa diuji.
- Kurangi tanggung jawab `MapFragment` secara bertahap: dialog builder, spot card binder, image helper.
- Kurangi tanggung jawab `DetailSpotActivity` dengan helper display/recommendation binder.

### Priority Rendah

- Audit Room dependency.
- Rapikan naming method yang masih legacy `Tide` menjadi `Marine/BMKG` pada dokumentasi dan komentar, bukan rename besar di kode.
- Tambahkan constructor injection manual untuk ViewModel/repository tertentu jika unit test membutuhkannya.

## Risiko Jika Langsung Testing Tanpa Refactor Ringan

- Unit test hanya bisa menguji class yang sudah pure, seperti `LocationUtils` dan public method `RecommendationEngine`.
- Logic private di `RecommendationEngine` tidak bisa diuji per bagian kecuali melalui skenario calculate.
- Visibility rule yang berada di repository bisa diuji sebagai static helper, tetapi secara desain lebih cocok di domain policy.
- BMKG parser sulit diuji tanpa network karena logic berada dalam repository besar.

## Keputusan Phase 1

Project layak dilanjutkan ke **Phase 2 - OOP Review**.

Prioritas review Phase 2:

1. `LocationUtils`
2. `RecommendationEngine`
3. `FishingRepository` visibility/owner helper
4. `TideRepository`
5. `MapFragment`
6. `CommunityRepository`
7. `ProfileRepository`
8. `DetailSpotActivity`

Tidak ada rekomendasi untuk mengganti arsitektur, mengganti UI final, atau menambah dependency besar.

## Catatan Untuk Skripsi

Audit ini dapat dipakai sebagai bahan pendukung kualitas software pada BAB IV atau lampiran, terutama untuk menunjukkan bahwa project tidak hanya dibuat berjalan, tetapi juga dievaluasi dari sisi maintainability, OOP, dan testing readiness.
