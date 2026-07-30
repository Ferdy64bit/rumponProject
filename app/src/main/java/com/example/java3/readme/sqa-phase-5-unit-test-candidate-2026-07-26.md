# SQA Phase 5 - Unit Test Candidate

Tanggal: 26 Juli 2026

Project: **Fishing Point Tanjung Anom**

Status: **Analisis kandidat unit test tanpa perubahan kode**

Dokumen acuan:

- `masterruleoop.md`
- `sqa-phase-1-project-audit-2026-07-26.md`
- `sqa-phase-2-oop-solid-review-2026-07-26.md`
- `sqa-phase-3-refactor-plan-2026-07-26.md`
- `sqa-phase-4-business-logic-audit-2026-07-26.md`

## Tujuan Phase 5

Phase ini menentukan class dan method yang paling layak dibuat unit test. Fokusnya adalah logic yang:

- pure function,
- deterministic,
- tidak bergantung langsung pada Android UI,
- tidak membutuhkan device,
- tidak membutuhkan internet,
- tidak membutuhkan Firestore live,
- tidak membutuhkan Cloudinary live,
- penting untuk skripsi dan fitur inti aplikasi.

Phase ini belum membuat kode test.

## Prinsip Pemilihan Candidate

Unit test pertama harus memberi manfaat besar dengan risiko kecil. Karena project Android ini memakai Firestore, Maps, GPS, Cloudinary, dan API live, tidak semua fitur cocok diuji dengan unit test murni.

Kategori pengujian:

| Kategori | Cocok Untuk Unit Test? | Alasan |
| --- | --- | --- |
| Haversine | Ya | Pure function dan inti skripsi. |
| Recommendation Engine | Ya | Logic utama rekomendasi dan tidak melakukan API call. |
| Visibility/Owner Policy | Ya | Rule penting, input/output jelas. |
| Formatter | Ya | Pure dan menjaga konsistensi UI text. |
| Sorting/Filtering Spot | Ya, setelah helper | Logic deterministic. |
| BMKG Parser | Ya, setelah extract | Bisa diuji dengan sample JSON. |
| Repository Firestore | Tidak untuk unit test awal | Butuh integration/mock kompleks. |
| Cloudinary Upload | Tidak untuk unit test awal | Bergantung network dan multipart HTTP. |
| Fragment/Activity | Tidak untuk unit test awal | Lebih cocok black box/device test. |
| Adapter/UI XML | Tidak untuk unit test awal | Lebih cocok screenshot/manual test. |

## Candidate Prioritas 1 - Siap Unit Test Tanpa Refactor

## 1. LocationUtils.calculateDistance

File:

- `core/utils/LocationUtils.java`

Method:

- `calculateDistance(double lat1, double lon1, double lat2, double lon2)`

Alasan dipilih:

- Inti metode Haversine pada judul skripsi.
- Pure function.
- Tidak bergantung Android framework.
- Tidak bergantung API/Firebase.
- Mudah dibuat boundary test.

Jenis test yang cocok:

- titik sama menghasilkan 0 km,
- jarak Tanjung Anom ke Teluk Jakarta masuk kisaran realistis,
- urutan titik dibalik menghasilkan jarak sama,
- koordinat negatif/positif tetap valid,
- koordinat ekstrem tetap tidak crash.

Nilai untuk skripsi:

- Sangat tinggi.

Status:

- **Siap masuk Phase 6 Unit Test Plan.**

## 2. RecommendationEngine.calculate

File:

- `domain/service/RecommendationEngine.java`

Method:

- `calculate(TideResponse tide, WeatherResponse weather, MarineHourlyResponse marineHourly, double distance, double rating)`
- overload lain dari `calculate`

Alasan dipilih:

- Inti fitur rekomendasi.
- Tidak melakukan network call.
- Menghasilkan `RecommendationResult` yang bisa diverifikasi.
- Mewakili formula `Base Score x Safety Multiplier`.

Jenis test yang cocok:

- kondisi bagus menghasilkan skor tinggi,
- gelombang tinggi menurunkan skor,
- cuaca buruk menurunkan safety,
- jarak sangat jauh terkena distance cap,
- data null tetap menghasilkan skor aman tanpa crash,
- rating spot rendah menurunkan spot quality score,
- user preference default tetap stabil.

Catatan:

- Karena banyak method internal private, test difokuskan pada output akhir, bukan setiap komponen score internal.

Nilai untuk skripsi:

- Sangat tinggi.

Status:

- **Siap masuk Phase 6 Unit Test Plan.**

## 3. RecommendationEngine.getFishActivityLabel

File:

- `domain/service/RecommendationEngine.java`

Method:

- `getFishActivityLabel(TideResponse tide, WeatherResponse weather, MarineHourlyResponse marineHourly)`
- `getFishActivityLabel(WeatherResponse weather, MarineHourlyResponse marineHourly)`

Alasan dipilih:

- Aktivitas ikan adalah bagian yang pernah dibandingkan dengan aplikasi Fishing Points.
- Output berupa label mudah diverifikasi.
- Memakai logic solunar-lite, weather, pressure, dan water movement.

Jenis test yang cocok:

- data null menghasilkan label valid,
- kondisi laut/cuaca baik tidak menghasilkan label kosong,
- severe weather menurunkan aktivitas,
- solunar rating tinggi dari `TideResponse` mendorong label naik.

Nilai untuk skripsi:

- Tinggi.

Status:

- **Siap masuk Phase 6 Unit Test Plan.**

## 4. FishingRepository Visibility Static Helpers

File:

- `data/repository/FishingRepository.java`

Method:

- `normalizeVisibility(String visibility)`
- `isPublicSpot(FishingPoint point)`
- `isOwnedByCurrentUser(FishingPoint point, String userId)`
- `canUserSeeSpot(FishingPoint point, String userId)`

Alasan dipilih:

- Private/public spot dan owner-only edit/delete adalah fitur penting.
- Method static relatif mudah diuji.
- Tidak butuh Firestore jika hanya memakai object `FishingPoint` lokal.

Catatan desain:

- Secara OOP, logic ini lebih ideal berada di `SpotVisibilityPolicy`, tetapi saat ini masih bisa diuji dari method static yang ada.

Jenis test yang cocok:

- `null` visibility menjadi `PUBLIC`,
- `PRIVATE` dan `PRIVAT` menjadi `PRIVATE`,
- input tidak dikenal menjadi `PUBLIC`,
- public spot terlihat oleh user null,
- private spot hanya terlihat oleh owner,
- ownerId dan legacy userId sama-sama terbaca,
- user lain tidak dianggap owner.

Nilai untuk skripsi:

- Sangat tinggi untuk skenario multi-akun.

Status:

- **Siap masuk Phase 6 Unit Test Plan**, atau refactor dulu ke domain policy jika ingin lebih bersih.

## Candidate Prioritas 2 - Perlu Refactor Ringan Sebelum Ideal

## 5. SpotVisibilityPolicy

Status saat ini:

- Belum ada sebagai class terpisah.
- Logic masih berada di `FishingRepository`.

Alasan kandidat:

- Rule private/public dan owner-only adalah domain policy.
- Sangat cocok untuk unit test.

Rencana sebelum test ideal:

- Extract logic dari `FishingRepository` tanpa mengubah behavior.
- Pertahankan compatibility method lama jika diperlukan.

Nilai untuk skripsi:

- Sangat tinggi.

Status:

- **Candidate setelah refactor ringan.**

## 6. DistanceFormatter

Status saat ini:

- Belum ada helper khusus.
- Label jarak dibuat di beberapa UI/adapter.

Alasan kandidat:

- Jarak adalah inti Haversine, sehingga format tampilan harus konsisten.
- Formatter pure dan mudah diuji.

Contoh behavior yang nanti bisa diuji:

- `0.4` menjadi `0.4 km`,
- `1.25` dibulatkan sesuai aturan,
- nilai negatif menjadi fallback,
- jarak sangat jauh tetap terbaca.

Nilai untuk skripsi:

- Sedang-tinggi.

Status:

- **Candidate setelah helper dibuat.**

## 7. RecommendationDisplayFormatter

Status saat ini:

- Belum ada helper khusus.
- Label safety, aktivitas ikan, bintang, subtitle rekomendasi tersebar di UI.

Alasan kandidat:

- Menjaga konsistensi Dashboard, Spot List, Map, dan Detail Spot.
- Pure function dan mudah diuji.

Contoh behavior yang nanti bisa diuji:

- score 90 menjadi label sangat baik,
- score 70 menjadi direkomendasikan,
- score rendah menjadi waspada/tidak aman,
- bintang 1-5 selalu valid,
- score di luar range tetap di-clamp.

Nilai untuk skripsi:

- Tinggi.

Status:

- **Candidate setelah helper dibuat.**

## 8. SpotImageHelper

Status saat ini:

- Belum ada helper khusus.
- Placeholder spot dan Cloudinary thumbnail helper berada di UI.

Alasan kandidat:

- Foto/default image spot harus konsisten antar screen.
- Transformasi URL Cloudinary bisa diuji sebagai pure string function.

Contoh behavior yang nanti bisa diuji:

- tipe `Pantai` memilih drawable pantai,
- tipe `Muara` memilih drawable muara,
- tipe `Bagan/Rumpon/Tambak` memilih fallback sesuai aturan,
- URL Cloudinary diberi transformasi thumbnail,
- URL non-Cloudinary tidak diubah,
- URL kosong tetap aman.

Nilai untuk skripsi:

- Sedang.

Status:

- **Candidate setelah helper dibuat.**

## 9. NearbySpotSelector / SpotDistanceSorter

Status saat ini:

- Belum ada helper khusus.
- Sorting/filtering spot terdekat berada di `HomeViewModel` dan sebagian activity/list.

Alasan kandidat:

- Spot terdekat adalah bagian penting dari LBS dan Haversine.
- Logic bisa diuji tanpa UI jika diekstrak.

Contoh behavior yang nanti bisa diuji:

- spot diurutkan dari jarak terdekat,
- spot private user lain tidak masuk hasil,
- limit list diterapkan,
- radius nearby dihitung benar,
- koordinat invalid diabaikan atau diberi fallback sesuai aturan.

Nilai untuk skripsi:

- Tinggi.

Status:

- **Candidate setelah helper dibuat.**

## 10. BMKG Parser / Area Resolver

Status saat ini:

- Logic parsing dan area resolving masih dominan di `TideRepository`.

Alasan kandidat:

- BMKG adalah sumber data resmi penting.
- Parser bisa diuji dengan sample JSON dari endpoint BMKG.
- Area polygon selector perlu bukti akurasi.

Contoh behavior yang nanti bisa diuji:

- JSON forecast valid menghasilkan list forecast,
- warning BMKG terbaca,
- wave category terbaca,
- wind direction/speed terbaca,
- koordinat dalam polygon memilih area benar,
- koordinat luar polygon memakai fallback aman.

Nilai untuk skripsi:

- Sangat tinggi.

Risiko:

- Butuh refactor cukup hati-hati karena sekarang bercampur network/cache.

Status:

- **Candidate setelah parser/resolver diekstrak.**

## Candidate Yang Tidak Disarankan Untuk Unit Test Awal

## 1. MapFragment

Alasan:

- Bergantung pada Google Maps SDK, Fragment lifecycle, permission, location, Glide, dialog, dan UI.
- Lebih cocok black box test di device.

Testing yang cocok:

- Manual/device test: tap marker, filter, route polyline, detail, edit/delete owner-only.

## 2. HomeFragment

Alasan:

- Mayoritas logic berupa render LiveData ke UI.
- Formatter bisa diuji setelah diekstrak.

Testing yang cocok:

- Manual/device test: Dashboard tampil, chart gelombang, forecast BMKG, rekomendasi.

## 3. DetailSpotActivity

Alasan:

- Bergantung pada Activity lifecycle, intent, galeri, upload foto, repository, dan UI.

Testing yang cocok:

- Manual/device test: favorite, share, navigasi, ganti/hapus foto, owner-only.

## 4. CommunityRepository

Alasan:

- Bergantung pada Firestore, FirebaseAuth, Cloudinary, Handler, Executor, dan network.
- Lebih cocok integration/manual test.

Testing yang cocok:

- Black box: create post, upload foto, like, comment, bookmark, delete.

## 5. ProfileRepository

Alasan:

- Bergantung pada FirebaseAuth, Firestore, Cloudinary, dan realtime listener.

Testing yang cocok:

- Black box: edit profile, upload foto, reset password, my posts, favorite spots.

## 6. Adapter dan XML Layout

Alasan:

- Fokus pada UI binding dan visual.

Testing yang cocok:

- Screenshot/manual responsive test.

## Urutan Unit Test Candidate

Urutan paling aman untuk project ini:

1. `LocationUtils.calculateDistance`
2. `RecommendationEngine.calculate`
3. `RecommendationEngine.getFishActivityLabel`
4. `FishingRepository.normalizeVisibility`
5. `FishingRepository.isOwnedByCurrentUser`
6. `FishingRepository.canUserSeeSpot`
7. `SpotVisibilityPolicy` setelah refactor
8. `DistanceFormatter` setelah helper dibuat
9. `RecommendationDisplayFormatter` setelah helper dibuat
10. `NearbySpotSelector` setelah helper dibuat
11. `BMKG Parser` setelah extraction

## Minimal Unit Test Scope Untuk Skripsi

Jika waktu terbatas, minimal unit test yang paling penting adalah:

1. Haversine distance.
2. Recommendation score normal/good condition.
3. Recommendation score bad weather/high wave.
4. Distance cap untuk spot sangat jauh.
5. Private/public visibility.
6. Owner-only access.

Enam kelompok test ini sudah cukup kuat untuk mendukung inti skripsi.

## Coverage Target

Target realistis:

| Area | Target Coverage | Catatan |
| --- | ---: | --- |
| `LocationUtils` | 90-100% | Class kecil dan pure. |
| Public `RecommendationEngine` behavior | 60-75% | Fokus skenario penting, bukan semua private branch. |
| Visibility/Owner policy | 90-100% | Pure dan penting. |
| Formatter helper | 80-100% | Setelah dibuat. |
| Repository | Tidak ditarget unit coverage tinggi | Lebih cocok integration/manual. |
| UI | Tidak ditarget unit coverage tinggi | Lebih cocok black box/screenshot. |

## Risiko Unit Test

- Test terlalu bergantung pada waktu saat ini karena `RecommendationEngine` memakai jam dan fase bulan.
- Weather/BMKG model setup untuk test bisa verbose.
- Private method engine tidak bisa diuji langsung.
- Static helper di repository bisa diuji, tetapi desain idealnya bukan di repository.
- Jika test terlalu banyak mengejar UI, effort akan besar dan kurang relevan untuk skripsi.

Mitigasi:

- Mulai dari skenario output final, bukan internal detail.
- Gunakan tolerance range untuk score jika perlu.
- Hindari assert terlalu rapuh pada label yang bisa berubah kecil.
- Refactor pure helper sebelum mengejar test yang lebih detail.

## Keputusan Phase 5

Project layak lanjut ke **Phase 6 - Unit Test Plan**.

Candidate final untuk Phase 6:

1. `LocationUtils.calculateDistance`
2. `RecommendationEngine.calculate`
3. `RecommendationEngine.getFishActivityLabel`
4. `FishingRepository` visibility static helpers
5. Rencana helper lanjutan: `SpotVisibilityPolicy`, `DistanceFormatter`, `RecommendationDisplayFormatter`, `SpotImageHelper`, `NearbySpotSelector`, dan BMKG parser.

## Catatan Untuk Skripsi

Phase ini membantu menentukan bagian aplikasi mana yang diuji otomatis dan bagian mana yang diuji black box. Pendekatan ini realistis untuk aplikasi Android yang memakai GPS, Maps, Firestore, Cloudinary, dan API live.
