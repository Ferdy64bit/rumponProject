# SQA Phase 4 - Business Logic Audit

Tanggal audit: 26 Juli 2026

Project: **Fishing Point Tanjung Anom**

Status: **Audit tanpa perubahan kode**

Dokumen acuan:

- `masterruleoop.md`
- `sqa-phase-1-project-audit-2026-07-26.md`
- `sqa-phase-2-oop-solid-review-2026-07-26.md`
- `sqa-phase-3-refactor-plan-2026-07-26.md`
- `uiuxx.md`
- `auidituiux.md`

## Tujuan Phase 4

Phase ini memeriksa lokasi business logic dalam project. Targetnya adalah memastikan logic penting berada pada layer yang tepat dan tidak terlalu banyak tersebar di Activity, Fragment, Adapter, atau XML.

Sesuai master rule, business logic sebaiknya berada pada:

- `domain`,
- `service`,
- `engine`,
- `helper`,
- `utils`.

Business logic sebaiknya tidak berada pada:

- `Activity`,
- `Fragment`,
- `Adapter`,
- `XML`,
- `Repository` jika logic tersebut murni domain/policy dan bukan data access.

Phase ini belum melakukan refactor atau implementasi kode.

## Ringkasan Eksekutif

Business logic utama project sudah cukup baik karena dua inti skripsi berada di tempat yang tepat:

- Haversine berada di `LocationUtils`.
- Formula rekomendasi berada di `RecommendationEngine`.

Namun masih ada beberapa logic pendukung yang tersebar di presentation layer dan repository. Ini tidak membuat aplikasi salah, tetapi membuat unit test dan maintenance lebih sulit.

Prioritas audit menunjukkan bahwa logic yang paling perlu dipindahkan nanti adalah:

1. Spot visibility dan owner policy.
2. Formatter jarak, bintang, safety, aktivitas ikan, dan fallback text.
3. Spot image placeholder dan Cloudinary thumbnail transformation.
4. Nearby spot sorting/filtering.
5. BMKG parsing dan polygon resolving.
6. Cloudinary multipart upload duplication.

## Business Logic Yang Sudah Berada Di Tempat Tepat

## 1. Haversine Distance

Lokasi saat ini:

- `core/utils/LocationUtils.java`

Status: **Tepat**

Alasan:

- Pure function.
- Tidak bergantung pada Android UI.
- Tidak bergantung pada Firebase/API.
- Bisa diuji dengan JUnit.
- Sesuai kebutuhan skripsi karena metode Haversine mudah dirujuk.

Catatan:

- Tidak perlu dipindah.
- Langsung menjadi kandidat unit test.

## 2. Recommendation Engine

Lokasi saat ini:

- `domain/service/RecommendationEngine.java`

Status: **Tepat, tetapi padat**

Alasan:

- Formula rekomendasi berada di domain service, bukan UI.
- Tidak melakukan API call.
- Tidak mengakses Firestore.
- Tidak bergantung pada Activity/Fragment.

Catatan:

- Tetap di domain/service.
- Jika nanti dipecah, pecah menjadi calculator pure function dalam domain/service/helper, bukan ke UI.

## 3. API dan Firestore Data Access

Lokasi saat ini:

- `data/repository/WeatherRepository.java`
- `data/repository/TideRepository.java`
- `data/repository/BMKGRepository.java`
- `data/repository/MarineHourlyRepository.java`
- `data/repository/FishingRepository.java`
- `data/repository/CommunityRepository.java`
- `data/repository/ProfileRepository.java`

Status: **Umumnya tepat**

Alasan:

- Network dan Firestore memang tepat berada di repository.
- UI tidak langsung memanggil Retrofit service.
- Cache API eksternal juga tepat berada dekat repository.

Catatan:

- Repository boleh mengatur data access, tetapi domain policy yang pure sebaiknya tidak terlalu lama tinggal di repository.

## Business Logic Yang Masih Berada Di Presentation Layer

## 1. MapFragment

Lokasi:

- `presentation/fragments/MapFragment.java`

Logic yang ditemukan:

- Filter marker berdasarkan nama dan tipe.
- Menentukan visibility button edit/delete berdasarkan owner.
- Format cuaca card map.
- Menghitung jarak user-ke-spot untuk card.
- Memilih placeholder gambar berdasarkan tipe spot.
- Membuat transformasi URL thumbnail Cloudinary.
- Menormalisasi visibility `PUBLIC`/`PRIVATE` dari label UI.
- Membuat polyline direct route user-ke-spot.
- Validasi koordinat spot.

Klasifikasi:

| Logic | Boleh di UI? | Rekomendasi |
| --- | --- | --- |
| Map lifecycle dan GoogleMap setup | Ya | Tetap di Fragment. |
| Permission location | Ya | Tetap di Fragment atau helper Android jika makin besar. |
| Marker filtering | Sebaiknya helper | Kandidat `SpotFilterHelper`. |
| Owner button visibility | Sebaiknya domain policy | Kandidat `SpotVisibilityPolicy`. |
| Weather card formatting | Sebaiknya formatter | Kandidat `WeatherDisplayFormatter`. |
| Distance calculation | Sudah utility | Tetap pakai `LocationUtils`, label bisa formatter. |
| Spot placeholder image | Sebaiknya helper | Kandidat `SpotImageHelper`. |
| Cloudinary thumbnail URL | Sebaiknya helper | Kandidat `CloudinaryImageUrlHelper` atau gabung `SpotImageHelper`. |
| Polyline route drawing | UI/Map concern | Tetap di Fragment dulu. |

Kesimpulan:

`MapFragment` masih menyimpan cukup banyak helper/domain display logic. Refactor nanti harus kecil dan bertahap karena Map adalah fitur inti.

## 2. HomeFragment

Lokasi:

- `presentation/fragments/HomeFragment.java`

Logic yang ditemukan:

- Label safety dari angka safety.
- Label aktivitas ikan dari score.
- Membuat bintang rekomendasi.
- Subtitle rekomendasi berdasarkan score.
- Label status marine/BMKG.
- Fallback text `nonBlank`.
- Capitalize text.
- Error state rendering.
- Summary perairan untuk Toast.

Klasifikasi:

| Logic | Boleh di UI? | Rekomendasi |
| --- | --- | --- |
| Render Weather ke TextView | Ya | Tetap di Fragment. |
| Render LiveData ke UI | Ya | Tetap di Fragment. |
| Safety label | Sebaiknya formatter | Kandidat `RecommendationDisplayFormatter`. |
| Activity label | Sebaiknya formatter | Kandidat `RecommendationDisplayFormatter`. |
| Stars | Sebaiknya formatter | Kandidat `RecommendationDisplayFormatter`. |
| Recommendation subtitle | Sebaiknya formatter | Kandidat `RecommendationDisplayFormatter`. |
| nonBlank/capitalize | Sebaiknya shared helper jika banyak dipakai | Kandidat helper kecil. |

Kesimpulan:

`HomeFragment` masih cukup banyak menyimpan display decision logic. Karena UI sudah final, refactor nanti harus memastikan text output tetap sama.

## 3. DetailSpotActivity

Lokasi:

- `presentation/activities/DetailSpotActivity.java`

Logic yang ditemukan:

- Owner check untuk kontrol edit foto.
- Normalize visibility.
- Render recommendation score dan badge.
- Render fish activity.
- Render safety/marine/weather detail.
- Upload/hapus foto spot melalui repository.
- Share dan navigation intent.
- Placeholder gambar spot.

Klasifikasi:

| Logic | Boleh di Activity? | Rekomendasi |
| --- | --- | --- |
| Activity result pilih foto | Ya | Tetap di Activity. |
| Share intent | Ya | Tetap di Activity. |
| Navigation intent | Ya | Tetap di Activity. |
| Owner check | Sebaiknya domain policy | Kandidat `SpotVisibilityPolicy`. |
| Visibility normalization | Sebaiknya domain policy | Kandidat `SpotVisibilityPolicy`. |
| Recommendation display | Sebaiknya formatter | Kandidat `RecommendationDisplayFormatter`. |
| Spot placeholder | Sebaiknya helper | Kandidat `SpotImageHelper`. |

Kesimpulan:

Detail Spot masih cocok diuji black box lebih dulu. Refactor helper display/policy bisa dilakukan setelah unit test dasar ada.

## 4. ProfileFragment

Lokasi:

- `presentation/fragments/ProfileFragment.java`

Logic yang ditemukan:

- Fallback profile text.
- Format tanggal bergabung.
- Menyusun teks daftar spot/post/favorite untuk dialog/list.
- Reset password confirmation messaging.
- Logout flow.

Klasifikasi:

| Logic | Boleh di Fragment? | Rekomendasi |
| --- | --- | --- |
| Dialog UI | Ya | Tetap di Fragment. |
| Logout navigation | Ya | Tetap di Fragment. |
| Date formatting | Bisa helper | Kandidat formatter ringan. |
| Fallback text | Bisa helper | Kandidat shared text helper. |
| List summary formatting | Bisa formatter | Refactor rendah prioritas. |

Kesimpulan:

Profile sudah stabil. Jangan refactor besar. Ambil helper hanya jika duplikasi nyata dan unit test dibutuhkan.

## 5. CreatePostFragment

Lokasi:

- `presentation/fragments/CreatePostFragment.java`

Logic yang ditemukan:

- Mengambil lokasi user.
- Mengambil Weather dan BMKG untuk lokasi post.
- Memproses image byte dari galeri.
- Menyiapkan object Post.
- Submit post melalui repository.

Klasifikasi:

| Logic | Boleh di Fragment? | Rekomendasi |
| --- | --- | --- |
| Activity result galeri | Ya | Tetap di Fragment. |
| Permission/location request | Ya | Tetap di Fragment atau ViewModel jika diperkuat. |
| Image byte processing | Sebaiknya helper/service jika bertambah | Kandidat rendah. |
| Repository direct creation | Bisa diterima | Jangan refactor dulu. |
| Post validation | Sebaiknya helper jika kompleks | Kandidat `PostValidationHelper` jika dibutuhkan. |

Kesimpulan:

Create Post sebaiknya diuji black box/manual terlebih dahulu karena banyak bergantung pada device, galeri, lokasi, dan network.

## Business Logic Yang Masih Berada Di Repository Tetapi Lebih Cocok Domain/Helper

## 1. Spot Visibility dan Ownership

Lokasi saat ini:

- `FishingRepository`

Method:

- `normalizeVisibility`
- `isPublicSpot`
- `isOwnedByCurrentUser`
- `canUserSeeSpot`

Status: **Bekerja, tetapi layer kurang ideal**

Alasan:

- Ini adalah business policy, bukan data access.
- Rule ini penting untuk multi-akun dan security behavior.
- Sangat cocok untuk unit test.

Rekomendasi:

- Pindahkan nanti ke domain/helper `SpotVisibilityPolicy`.
- Pertahankan hasil behavior sama persis.

## 2. Cloudinary Multipart Upload

Lokasi saat ini:

- `CommunityRepository`
- `ProfileRepository`
- `FishingRepository`

Status: **Bekerja, tetapi duplikatif**

Alasan:

- Upload media adalah infrastructure/service concern.
- Repository seharusnya fokus menyimpan URL ke Firestore setelah upload sukses.
- Multipart code berulang meningkatkan risiko inkonsistensi.

Rekomendasi:

- Extract ke `CloudinaryUploadService` nanti.
- Jangan pindahkan Firestore write ke service ini.

## 3. BMKG Parsing dan Polygon Resolving

Lokasi saat ini:

- `TideRepository`
- sebagian konteks tersedia juga di `BMKGAreaSelector`

Status: **Bekerja, tetapi terlalu padat dalam repository**

Alasan:

- Parser JSON dan polygon resolver merupakan pure-ish logic yang bisa diuji dengan sample JSON.
- Saat berada di repository, unit test menjadi sulit karena bercampur dengan network/cache.

Rekomendasi:

- Extract parser dan resolver pada fase refactor setelah baseline test.
- Gunakan sample JSON BMKG yang sudah valid.

## 4. Author/Profile Enrichment Community

Lokasi saat ini:

- `CommunityRepository`
- `ProfileRepository` untuk sync author fields.

Status: **Masih dapat diterima**

Alasan:

- Ini berkaitan dengan data Firestore dan denormalisasi author di post.
- Cocok berada di repository, tetapi class menjadi besar.

Rekomendasi:

- Jangan refactor dulu kecuali ada bug.
- Prioritas lebih rendah daripada Cloudinary dan visibility policy.

## Business Logic Pada ViewModel

## 1. HomeViewModel Nearby Recommendation

Lokasi:

- `HomeViewModel`

Logic:

- Filter visible spot.
- Hitung jarak.
- Sort berdasarkan jarak.
- Batasi nearby list.
- Hitung recommendation untuk setiap spot terdekat.

Status: **Cukup, tetapi bisa lebih testable**

Alasan:

- ViewModel boleh mengolah UI state.
- Namun sorting/filtering/recommendation mapping adalah logic yang bagus untuk unit test.

Rekomendasi:

- Extract nanti ke `NearbySpotSelector` atau `SpotRecommendationMapper` jika testing membutuhkan.

## 2. MapViewModel Recommendation Delay

Lokasi:

- `MapViewModel`

Logic:

- Fetch Weather, Tide/BMKG, Marine secara async.
- Menunggu 2 detik dengan Handler lalu menghitung recommendation.

Status: **Berfungsi, tetapi async timing rapuh**

Alasan:

- Fixed delay tidak menjamin semua API selesai tepat waktu.
- Bisa menghasilkan recommendation dengan sebagian data null.

Rekomendasi:

- Jangan ubah sekarang karena dapat mengubah behavior.
- Dalam refactor nanti, pertimbangkan MediatorLiveData atau state aggregator agar recommendation dihitung saat data masuk, bukan berdasarkan fixed delay.

Risiko:

- Refactor area ini perlu uji Map marker sangat hati-hati.

## Business Logic Pada Adapter

Adapter yang terlihat:

- `PostAdapter`
- `FishingPointAdapter`
- `SpotListAdapter`
- `BMKGForecastAdapter`

Status umum: **Cukup aman**

Adapter terutama melakukan binding UI. Namun beberapa adapter memiliki formatter text/count/image.

Rekomendasi:

- Jangan refactor adapter lebih dulu.
- Jika formatter sudah diekstrak, adapter boleh memakai formatter tersebut agar konsisten.

## Business Logic Pada XML

Status: **Tidak ditemukan business logic berat**

XML digunakan untuk layout dan styling. Ini sesuai aturan. Masalah UI yang pernah muncul lebih terkait responsive/micro UI, bukan business logic.

Rekomendasi:

- Jangan ubah XML pada fase testing logic.
- UI sudah final, hanya micro fix jika ditemukan bug visual nyata.

## Prioritas Pemindahan Logic

## Priority Tinggi

1. `SpotVisibilityPolicy`

   Alasan: private/public dan owner-only adalah fitur penting, pure, dan mudah diuji.

2. `RecommendationDisplayFormatter`

   Alasan: bintang, label safety, label aktivitas, dan subtitle rekomendasi dipakai lintas screen.

3. `DistanceFormatter`

   Alasan: jarak Haversine harus konsisten di Dashboard, Map, Detail, dan Spot List.

## Priority Sedang

4. `SpotImageHelper`

   Alasan: placeholder dan thumbnail Cloudinary harus konsisten.

5. `CloudinaryUploadService`

   Alasan: mengurangi duplikasi upload, tetapi butuh uji device/network.

6. `NearbySpotSelector`

   Alasan: membuat pemilihan spot terdekat lebih mudah diuji.

## Priority Rendah

7. BMKG parser extraction

   Alasan: penting, tetapi risikonya lebih tinggi karena menyentuh data maritim utama.

8. Profile summary formatter

   Alasan: meningkatkan kerapian, tetapi bukan core logic.

9. Create post validation helper

   Alasan: berguna jika validasi form bertambah.

## Daftar Logic Yang Tidak Perlu Dipindah

Logic berikut tetap layak berada di UI layer:

- lifecycle Activity/Fragment,
- ViewBinding,
- click listener,
- dialog presentation,
- Activity Result galeri,
- permission request,
- Google Maps object lifecycle,
- navigation intent,
- share intent,
- render TextView/ImageView/RecyclerView,
- show Toast/Snackbar/dialog.

Logic berikut tetap layak berada di repository:

- Firestore query/write/delete/listener,
- Retrofit API request,
- cache read/write/cleanup,
- mapping response ke model jika tidak terlalu kompleks,
- cleanup relation Firestore,
- auth operation seperti login/register/reset password.

## Risiko Jika Logic Tidak Dipindahkan

- Unit test akan terbatas pada `LocationUtils` dan public `RecommendationEngine` saja.
- Private/public spot sulit dijelaskan sebagai domain policy karena masih berada di repository.
- Format jarak/bintang bisa tidak konsisten antar screen.
- Cloudinary upload bug perlu diperbaiki di beberapa repository sekaligus.
- BMKG parser sulit diuji dengan sample JSON.
- Class besar tetap sulit dipelihara saat bug kecil muncul.

## Rekomendasi Setelah Phase 4

Project layak lanjut ke **Phase 5 - Unit Test Candidate**.

Unit test candidate harus dipilih berdasarkan dua kategori:

1. Sudah pure dan siap test:
   - `LocationUtils`
   - `RecommendationEngine.calculate`
   - `RecommendationEngine.getFishActivityLabel`

2. Perlu refactor kecil sebelum test ideal:
   - spot visibility/ownership,
   - distance/recommendation formatter,
   - nearby spot selector,
   - BMKG parser.

## Keputusan Phase 4

Business logic inti sudah berada di tempat yang cukup tepat. Tidak diperlukan perubahan arsitektur.

Perbaikan kualitas paling bernilai adalah memindahkan logic pure yang masih tersebar di UI/repository menjadi helper/domain service kecil, lalu menambahkan unit test secara bertahap.

## Catatan Untuk Skripsi

Phase ini dapat digunakan untuk menjelaskan pemisahan tanggung jawab aplikasi:

- UI hanya menampilkan dan menerima interaksi.
- ViewModel mengelola state.
- Repository mengambil data.
- Domain service menghitung rekomendasi.
- Utility menghitung Haversine.

Ini memperkuat argumen bahwa aplikasi tidak hanya dibuat berjalan, tetapi juga dirancang agar maintainable dan testable.
