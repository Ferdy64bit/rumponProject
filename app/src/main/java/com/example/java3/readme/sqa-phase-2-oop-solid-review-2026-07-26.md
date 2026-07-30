# SQA Phase 2 - OOP dan SOLID Review

Tanggal review: 26 Juli 2026

Project: **Fishing Point Tanjung Anom**

Status: **Review tanpa perubahan kode**

Dokumen acuan:

- `masterruleoop.md`
- `sqa-phase-1-project-audit-2026-07-26.md`
- `uiuxx.md`
- `auidituiux.md`

## Tujuan Phase 2

Phase ini menilai kualitas OOP dan penerapan SOLID pada class prioritas. Tujuannya bukan mencari kesalahan sebanyak mungkin, tetapi menentukan bagian mana yang sudah sehat, bagian mana yang berisiko, dan bagian mana yang perlu refactor ringan sebelum unit test.

Sesuai master rule, phase ini tidak melakukan:

- perubahan kode,
- refactor,
- perubahan UI,
- perubahan flow aplikasi,
- perubahan schema Firestore,
- penambahan dependency,
- pembuatan unit test.

## Ringkasan OOP

Project sudah memakai pemisahan layer yang cukup baik:

- `presentation` untuk UI dan ViewModel,
- `data/repository` untuk Firestore/API/media upload,
- `domain/model` untuk data aplikasi,
- `domain/service` untuk Recommendation Engine,
- `core` untuk network, manager, constants, dan utility.

Kualitas OOP secara umum: **cukup baik untuk project skripsi Android Java**, tetapi beberapa class sudah melewati batas ideal tanggung jawab. Fokus peningkatan sebaiknya bukan mengganti arsitektur, melainkan memindahkan logic kecil yang berulang ke helper/domain service agar lebih mudah diuji.

## SOLID Review Summary

| Prinsip | Status Umum | Catatan |
| --- | --- | --- |
| SRP - Single Responsibility | Perlu penguatan | Beberapa Fragment/Repository terlalu banyak tanggung jawab. |
| OCP - Open/Closed | Cukup | Formula engine dan repository bisa dikembangkan, tetapi beberapa logic masih hardcoded di class besar. |
| LSP - Liskov Substitution | Aman | Tidak banyak inheritance custom; risiko rendah. |
| ISP - Interface Segregation | Cukup | Callback sederhana sudah ada, tetapi belum ada interface kecil untuk service yang mudah diuji. |
| DIP - Dependency Inversion | Perlu penguatan ringan | Banyak class membuat dependency langsung dengan `new`, tetapi tidak perlu framework DI besar. |

## Class Review Prioritas

## 1. LocationUtils

File: `core/utils/LocationUtils.java`

Tanggung jawab saat ini:

- Menghitung jarak Haversine dalam kilometer.

### SRP

Status: **Baik**

Class ini kecil, fokus, dan pure. Sangat cocok sebagai contoh OOP utility yang sehat.

### OCP

Status: **Cukup**

Jika nanti butuh satuan meter/mil, sebaiknya ditambah method baru tanpa mengubah behavior `calculateDistance` yang sudah dipakai.

### LSP

Status: **Tidak relevan / aman**

Tidak ada inheritance.

### ISP

Status: **Tidak relevan / aman**

Tidak ada interface yang dipaksakan.

### DIP

Status: **Baik**

Tidak bergantung pada framework Android/Firebase/API.

### Testability

Status: **Sangat baik**

Kandidat unit test pertama. Input dan output deterministik.

### Catatan

Tidak perlu refactor sebelum unit test. Class ini bisa langsung masuk Phase 5/6 sebagai kandidat utama.

## 2. RecommendationEngine

File: `domain/service/RecommendationEngine.java`

Tanggung jawab saat ini:

- Menghitung skor rekomendasi.
- Menghitung distance score.
- Menghitung weather score.
- Menghitung marine score.
- Menghitung fish activity score.
- Menghitung safety multiplier.
- Menghasilkan badge, warna, dan jumlah bintang.

### SRP

Status: **Cukup, tetapi mulai padat**

Semua method masih berada dalam satu domain yang sama: rekomendasi memancing. Jadi secara konsep masih cohesive. Namun di dalamnya sudah ada beberapa sub-domain:

- scoring jarak,
- scoring cuaca,
- scoring gelombang/arus,
- solunar-lite,
- safety,
- label UI.

### OCP

Status: **Cukup**

Formula bisa dikembangkan dengan menambah method internal, tetapi perubahan bobot atau kategori saat ini harus mengubah class yang sama. Untuk skripsi ini masih aman karena formula sudah difinalisasi.

### LSP

Status: **Tidak relevan / aman**

Tidak ada inheritance.

### ISP

Status: **Cukup**

Tidak ada interface besar yang dipaksakan. Namun belum ada interface kecil seperti `ScoreCalculator`, dan master rule belum mengizinkan implementasi baru pada fase ini.

### DIP

Status: **Baik**

Tidak bergantung ke repository, Firebase, Retrofit, Activity, atau Fragment. Ini keputusan arsitektur yang sangat baik.

### Testability

Status: **Baik untuk public method, sedang untuk internal score**

`calculate` dan `getFishActivityLabel` bisa diuji. Private method seperti `getWaveSafety`, `getWindSafety`, dan `getDistanceScoreCap` belum bisa diuji langsung tanpa refactor.

### Catatan

Tidak perlu refactor besar. Jika nanti refactor dilakukan, pecah secara kecil ke calculator pure function, misalnya:

- `DistanceScoreCalculator`
- `WeatherScoreCalculator`
- `MarineScoreCalculator`
- `SafetyCalculator`
- `FishingActivityCalculator`

Namun untuk tahap awal testing, cukup uji public behavior `calculate` dengan skenario nyata.

## 3. FishingRepository

File: `data/repository/FishingRepository.java`

Tanggung jawab saat ini:

- CRUD fishing point Firestore.
- Listener realtime fishing point.
- Delegasi weather/BMKG/marine ke repository lain.
- Metadata owner/visibility spot.
- Helper static visibility dan ownership.
- Upload foto spot ke Cloudinary.

### SRP

Status: **Perlu penguatan**

Repository ini memegang beberapa tanggung jawab berbeda:

- data access Firestore,
- metadata ownership,
- visibility policy,
- media upload,
- delegasi API environment.

Secara praktis masih berjalan, tetapi dari sisi OOP sudah terlalu melebar.

### OCP

Status: **Sedang**

Jika nanti aturan visibility bertambah, misalnya `FRIENDS_ONLY` atau `ADMIN`, class repository harus diubah. Ini tanda policy sebaiknya dipisah saat refactor.

### LSP

Status: **Aman**

Tidak ada inheritance custom.

### ISP

Status: **Cukup**

Callback `FirestoreCallback<T>` sederhana dan tidak terlalu besar. Masih layak.

### DIP

Status: **Perlu penguatan ringan**

Repository membuat dependency langsung:

- `FirebaseFirestore.getInstance()`
- `FirebaseAuth.getInstance()`
- `new WeatherRepository()`
- `new BMKGRepository()`
- `new MarineHourlyRepository()`

Ini menyulitkan unit test, tetapi masih dapat diterima tanpa framework DI. Jika perlu, gunakan constructor overload/manual injection nanti, bukan Dagger/Hilt.

### Testability

Status: **Campuran**

Helper static visibility bisa diuji cukup mudah. CRUD/upload sulit diuji unit karena bergantung Firestore/Cloudinary.

### Catatan

Sebelum unit test visibility lebih matang, sebaiknya pertimbangkan refactor ringan ke domain policy pada Phase 3/8. Tidak dilakukan sekarang.

## 4. TideRepository

File: `data/repository/TideRepository.java`

Tanggung jawab saat ini:

- Resolve area BMKG.
- Fetch data BMKG perairan.
- Fetch fallback overview gelombang.
- Parse response JSON BMKG.
- Extract forecast.
- Parse polygon wilayah BMKG.
- Build endpoint area.
- Cache Firestore.
- Fallback cache.
- Cleanup cache.

### SRP

Status: **Lemah / risiko tinggi**

Class ini menjadi pusat semua logic BMKG. Secara fitur berhasil, tetapi dari sisi OOP terlalu banyak tugas untuk satu repository.

### OCP

Status: **Lemah sedang**

Jika format BMKG berubah, endpoint bertambah, atau parser polygon disesuaikan, perubahan akan terjadi di class yang sama dan berisiko memengaruhi cache/fallback.

### LSP

Status: **Aman**

Tidak ada inheritance custom.

### ISP

Status: **Cukup**

Tidak ada interface besar yang membebani client. Namun belum ada kontrak kecil untuk parser/selector/cache.

### DIP

Status: **Perlu penguatan**

Repository langsung bergantung pada Firestore, Retrofit service, Gson, dan detail parsing JSON. Ini menyulitkan unit test parser BMKG tanpa network.

### Testability

Status: **Sulit untuk unit test saat ini**

Parser dan selector sebenarnya sangat penting untuk diuji, tetapi saat ini masih private dan bercampur dengan network/cache.

### Catatan

Ini kandidat refactor paling penting setelah unit test Haversine/Recommendation. Refactor sebaiknya bertahap:

1. Extract parser BMKG.
2. Extract polygon area resolver.
3. Extract cache policy jika diperlukan.

Jangan ubah endpoint atau behavior data saat refactor.

## 5. MapFragment

File: `presentation/fragments/MapFragment.java`

Tanggung jawab saat ini:

- Lifecycle `MapView`.
- Permission lokasi.
- Location updates.
- Cluster manager.
- Search/filter marker.
- Spot card rendering.
- Environment data per marker.
- Polyline route.
- Dialog tambah/edit marker.
- Delete confirmation.
- Image loading thumbnail.
- Owner-only button visibility.
- Navigation intent.

### SRP

Status: **Lemah / risiko tinggi**

Ini adalah class dengan tanggung jawab paling banyak di presentation layer. Meski fungsional, class ini sulit dipelihara karena banyak alasan untuk berubah.

### OCP

Status: **Sedang lemah**

Jika tipe spot, card data, dialog field, route behavior, atau marker behavior berubah, `MapFragment` akan berubah. Ini tanda beberapa bagian cocok diekstrak ke helper/binder.

### LSP

Status: **Aman**

Extends `Fragment` secara normal.

### ISP

Status: **Cukup**

Tidak ada interface besar. Namun listener/callback UI cukup banyak dan bercampur dalam fragment.

### DIP

Status: **Sedang**

Fragment bergantung langsung ke Google Maps, FirebaseAuth, Glide, LocationServices, dan dialog Material. Untuk UI ini wajar, tetapi business/helper logic sebaiknya tidak bertambah di sini.

### Testability

Status: **Rendah untuk unit test, cocok untuk black box/manual test**

Unit test fragment ini tidak efisien pada tahap skripsi. Lebih baik pindahkan helper pure function dulu, lalu sisanya diuji black box.

### Catatan

Refactor nanti sebaiknya sangat kecil:

- Extract spot filter helper.
- Extract spot image helper.
- Extract add/edit dialog builder jika dibutuhkan.
- Extract route/polyline helper hanya jika risiko rendah.

UI final tidak boleh berubah.

## 6. HomeViewModel

File: `presentation/viewmodels/HomeViewModel.java`

Tanggung jawab saat ini:

- Fetch dashboard environment data.
- Fetch fishing points.
- Filter spot visibility.
- Hitung jarak Haversine.
- Sort spot berdasarkan jarak.
- Hitung recommendation untuk nearby spot.
- Publish LiveData dashboard.

### SRP

Status: **Cukup**

Sebagai ViewModel, class ini masih cukup sesuai karena mengatur state dashboard. Namun filtering/sorting/recommendation mapping bisa menjadi helper agar lebih mudah diuji.

### OCP

Status: **Cukup**

Jika aturan nearby radius atau limit berubah, cukup ubah konstanta. Jika aturan sorting/filtering makin kompleks, perlu helper.

### LSP

Status: **Aman**

Extends `ViewModel` secara normal.

### ISP

Status: **Aman**

Tidak ada interface besar.

### DIP

Status: **Perlu penguatan ringan**

`HomeViewModel` membuat `FishingRepository` langsung dan memakai `FirebaseAuth` langsung untuk current user. Ini membuat unit test ViewModel lebih sulit.

### Testability

Status: **Sedang**

Pure logic sorting/filtering bisa diuji jika diekstrak. ViewModel langsung saat ini lebih cocok diuji manual/integration.

### Catatan

Kandidat helper nanti: `NearbySpotSelector` atau `SpotRecommendationMapper`.

## 7. MapViewModel

File: `presentation/viewmodels/MapViewModel.java`

Tanggung jawab saat ini:

- Listen fishing points.
- Fetch Weather/BMKG/Marine untuk marker.
- Menghitung recommendation setelah delay 2 detik.
- Add/update/delete fishing point.
- Publish LiveData untuk Map.

### SRP

Status: **Cukup, dengan catatan**

Sebagai ViewModel Map, tanggung jawab masih terkait satu screen. Namun delay 2 detik untuk menghitung recommendation adalah tanda flow async yang rapuh.

### OCP

Status: **Sedang**

Jika jumlah API source berubah atau callback timing berubah, method `calculateRecommendation` perlu diubah.

### LSP

Status: **Aman**

Extends `ViewModel` normal.

### ISP

Status: **Aman**

Tidak ada interface besar.

### DIP

Status: **Perlu penguatan ringan**

Membuat repository langsung. Masih diterima, tapi menyulitkan testing.

### Testability

Status: **Sedang rendah**

Karena bergantung LiveData async, repository, dan delay Handler. Untuk unit test awal, lebih baik jangan mulai dari class ini.

### Catatan

Pada refactor kecil nanti, recommendation sebaiknya dihitung ketika semua data source yang tersedia sudah masuk, bukan berdasarkan fixed delay. Namun perubahan ini menyentuh behavior async, jadi harus hati-hati dan ditunda sampai plan final.

## 8. CommunityRepository

File: `data/repository/CommunityRepository.java`

Tanggung jawab saat ini:

- Feed realtime community.
- Enrich author profile.
- Resolve like/favorite state current user.
- Create post.
- Upload image Cloudinary.
- Save post Firestore.
- Toggle like.
- Toggle favorite/bookmark.
- Add/get comment.
- Delete post.
- Cleanup related likes/favorites/comments.

### SRP

Status: **Lemah sedang**

Masih satu domain community, tetapi repository ini sudah menggabungkan terlalu banyak sub-feature.

### OCP

Status: **Sedang**

Jika schema favorite berubah, upload media berubah, atau comment handling berubah, class ini ikut berubah.

### LSP

Status: **Aman**

Tidak ada inheritance custom.

### ISP

Status: **Cukup**

Callback generic sederhana. Tidak membebani client.

### DIP

Status: **Perlu penguatan**

Bergantung langsung pada FirebaseAuth, Firestore, Handler, Executor, dan HttpURLConnection.

### Testability

Status: **Rendah untuk unit test langsung**

Sebagian besar perlu integration/manual test. Helper author/favorite extraction bisa diuji jika diekstrak.

### Catatan

Refactor paling aman nanti adalah ekstraksi Cloudinary uploader karena duplikasi dengan repository lain.

## 9. ProfileRepository

File: `data/repository/ProfileRepository.java`

Tanggung jawab saat ini:

- Listen profile.
- Listen stats.
- Update profile.
- Upload profile photo Cloudinary.
- Reset password.
- Load my posts.
- Load my spots.
- Load favorite spots.
- Sync community author fields.
- Cleanup listeners.

### SRP

Status: **Sedang lemah**

Semua masih berkaitan dengan profile, tetapi upload media, stats aggregation, and sync community author adalah tanggung jawab yang berbeda.

### OCP

Status: **Sedang**

Jika statistik bertambah atau field profile berubah, class ini akan berubah. Masih wajar, tetapi perlu dijaga.

### LSP

Status: **Aman**

Tidak ada inheritance custom.

### ISP

Status: **Cukup**

Callback sederhana.

### DIP

Status: **Perlu penguatan**

Bergantung langsung pada FirebaseAuth, Firestore, Executor, dan HttpURLConnection.

### Testability

Status: **Rendah untuk unit test langsung**

Lebih cocok black box/integration. Formatter kecil dan aggregation bisa diuji jika diekstrak.

### Catatan

Jangan refactor besar karena profile sudah stabil. Fokus hanya duplikasi Cloudinary dan helper text jika diperlukan.

## 10. DetailSpotActivity

File: `presentation/activities/DetailSpotActivity.java`

Tanggung jawab saat ini:

- Render detail spot.
- Owner info dan visibility badge.
- Load foto spot.
- Ganti/hapus foto spot.
- Favorite spot.
- Share spot.
- Navigasi Google Maps.
- Fetch live environment data.
- Render recommendation, safety, fish activity.
- Owner-only control.

### SRP

Status: **Sedang lemah**

Sebagai detail screen, semua masih terkait satu fitur. Namun activity sudah mengandung banyak action dan helper.

### OCP

Status: **Sedang**

Jika detail screen bertambah section seperti review/gallery, activity akan makin besar.

### LSP

Status: **Aman**

Extends `AppCompatActivity` normal.

### ISP

Status: **Aman**

Tidak ada interface besar.

### DIP

Status: **Sedang**

Bergantung langsung pada repository, FirebaseAuth, Glide, Intent, dan Activity Result. Wajar untuk Activity, tetapi logic display bisa diekstrak.

### Testability

Status: **Rendah untuk unit test langsung**

Lebih cocok black box. Helper rendering label/recommendation bisa diuji jika dipisah.

### Catatan

Refactor nanti harus sangat hati-hati karena screen ini penting dan UI final. Jangan ubah layout kecuali ada bug visual.

## Cross-Cutting SOLID Findings

## SRP Findings

Paling perlu perhatian:

1. `MapFragment`
2. `TideRepository`
3. `CommunityRepository`
4. `ProfileRepository`
5. `DetailSpotActivity`

Paling sehat:

1. `LocationUtils`
2. `RecommendationEngine` secara domain, meski besar
3. Model POJO sederhana

## OCP Findings

Area yang kurang open/closed:

- Visibility spot masih hardcoded `PUBLIC`/`PRIVATE` di beberapa tempat.
- Spot type dan placeholder image masih hardcoded di UI helper.
- BMKG parser masih menyatu dengan repository.
- Recommendation weights hardcoded, tetapi ini bisa diterima karena formula skripsi perlu dibekukan.

## LSP Findings

Risiko LSP rendah karena project tidak memakai inheritance custom yang kompleks. Ini baik untuk maintainability.

## ISP Findings

Callback repository relatif kecil dan tidak melanggar ISP secara berat. Namun belum ada interface kecil untuk parser/uploader/calculator, sehingga testability belum maksimal.

## DIP Findings

Area yang perlu diperbaiki ringan:

- Repository dan ViewModel banyak membuat dependency langsung.
- FirebaseAuth dipakai langsung di beberapa presentation class.
- Cloudinary upload memakai `HttpURLConnection` langsung di beberapa repository.

Keputusan: jangan memakai Dagger/Hilt. Jika refactor diperlukan, gunakan constructor overload atau helper service sederhana.

## Class yang Siap Unit Test Tanpa Refactor

1. `LocationUtils`
2. `RecommendationEngine.calculate`
3. `RecommendationEngine.getFishActivityLabel`
4. `FishingRepository.normalizeVisibility`
5. `FishingRepository.isPublicSpot`
6. `FishingRepository.isOwnedByCurrentUser`
7. `FishingRepository.canUserSeeSpot`

Catatan: item 4-7 masih berada di repository, tetapi method static dan pure-ish sehingga bisa diuji. Secara desain jangka panjang lebih baik menjadi domain policy.

## Class yang Perlu Refactor Ringan Sebelum Unit Test Efektif

1. `TideRepository` untuk parser/area selector BMKG.
2. `CommunityRepository` untuk Cloudinary upload dan favorite extraction.
3. `ProfileRepository` untuk Cloudinary upload dan profile formatter.
4. `MapFragment` untuk spot filter/image helper.
5. `HomeViewModel` untuk nearby spot selector.

## Rekomendasi Phase 2

Project layak dilanjutkan ke **Phase 3 - Refactor Plan**.

Urutan refactor plan yang disarankan:

1. Refactor plan untuk helper pure function yang tidak mengubah behavior.
2. Refactor plan untuk Cloudinary uploader agar duplikasi berkurang.
3. Refactor plan untuk visibility/owner policy spot.
4. Refactor plan untuk formatter distance/recommendation/spot image.
5. Refactor plan untuk BMKG parser setelah unit test awal siap.

## Keputusan Penting

- Jangan mengubah MVVM.
- Jangan mengubah Repository Pattern.
- Jangan menambah Lombok, Hibernate, Dagger, atau Hilt.
- Jangan mengubah UI final.
- Jangan melakukan refactor besar.
- Unit test pertama sebaiknya dimulai dari class pure function, bukan Fragment/Repository besar.

## Catatan Untuk Skripsi

Phase 2 ini dapat dipakai sebagai bukti bahwa kualitas software dinilai menggunakan prinsip OOP dan SOLID. Bagian yang paling mudah dijelaskan saat sidang adalah:

- Haversine berada di utility pure function.
- Recommendation Engine berada di domain service.
- Firestore/API berada di repository.
- UI berada di presentation layer.
- Refactor dilakukan bertahap untuk menjaga stabilitas aplikasi.
