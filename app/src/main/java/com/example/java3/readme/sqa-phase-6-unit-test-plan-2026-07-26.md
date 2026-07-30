# SQA Phase 6 - Unit Test Plan

Tanggal: 26 Juli 2026

Project: **Fishing Point Tanjung Anom**

Status: **Sebagian sudah diimplementasikan**

Dokumen acuan:

- `masterruleoop.md`
- `sqa-phase-5-unit-test-candidate-2026-07-26.md`
- `sqa-phase-4-business-logic-audit-2026-07-26.md`
- `sqa-phase-3-refactor-plan-2026-07-26.md`

## Tujuan Phase 6

Phase ini menyusun rencana unit test detail untuk kandidat yang sudah dipilih pada Phase 5. Unit test plan ini menjadi acuan sebelum implementasi test dilakukan pada Phase 8.

Phase ini awalnya dibuat sebagai rencana unit test. Pada update 26 Juli 2026, sebagian rencana sudah dieksekusi pada folder `app/src/test`.

## Update Implementasi Unit Test 26 Juli 2026

Unit test yang sudah tersedia dan lolos:

- `LocationUtilsTest`: 5 test untuk Haversine, termasuk koordinat sama, simetri jarak, jarak pesisir realistis, latitude negatif, dan jarak jauh.
- `RecommendationEngineTest`: 4 test untuk null data, cuaca buruk vs cuaca cerah, clamp user preference, dan label aktivitas ikan.
- `TideResponseTest`: 2 test untuk helper BMKG forecast dan kompatibilitas label BMKG pada `TideResponse`.

Catatan teknis:

- `ExampleUnitTest` bawaan template dihapus karena hanya menguji `2 + 2` dan tidak memberi nilai terhadap project.
- `Robolectric` ditambahkan sebagai dependency test-only karena `RecommendationEngine` masih memakai class Android SDK `android.graphics.Color`.
- Verifikasi terbaru: `./gradlew.bat testDebugUnitTest` berhasil dengan 11 local unit test.

## Scope Unit Test

Unit test dibagi menjadi dua kelompok:

1. **Siap diuji tanpa refactor**
   - `LocationUtils.calculateDistance`
   - `RecommendationEngine.calculate`
   - `RecommendationEngine.getFishActivityLabel`
   - static helper visibility pada `FishingRepository`

2. **Diuji setelah refactor ringan**
   - `SpotVisibilityPolicy`
   - `DistanceFormatter`
   - `RecommendationDisplayFormatter`
   - `SpotImageHelper`
   - `NearbySpotSelector`
   - BMKG parser/area resolver

## Test Environment

Framework yang sudah tersedia:

- JUnit 4.13.2

Lokasi test yang disarankan nanti:

```text
app/src/test/java/com/example/java3/
```

Jenis test:

- Local JVM unit test.
- Tidak membutuhkan emulator/device.
- Tidak membutuhkan internet.
- Tidak membutuhkan Firebase live.
- Tidak membutuhkan Cloudinary live.

## Unit Test Plan - LocationUtils

Target:

- `core/utils/LocationUtils.java`

Method:

- `calculateDistance(double lat1, double lon1, double lat2, double lon2)`

Coverage target:

- 90-100%

## Test Case LU-001 - Same Coordinate Returns Zero

Tujuan:

- Memastikan jarak titik yang sama menghasilkan 0 km.

Input:

- `lat1 = -6.1000`
- `lon1 = 106.6500`
- `lat2 = -6.1000`
- `lon2 = 106.6500`

Expected result:

- Jarak `0.0 km` atau sangat dekat dengan 0 menggunakan tolerance.

Boundary:

- Titik identik.

Negative test:

- Tidak ada.

Edge case:

- Floating point precision.

Status:

- Planned.

## Test Case LU-002 - Distance Is Symmetric

Tujuan:

- Memastikan jarak A ke B sama dengan B ke A.

Input:

- A: Tanjung Anom approximate coordinate.
- B: Teluk Jakarta approximate coordinate.

Expected result:

- `distance(A, B)` sama dengan `distance(B, A)` dalam tolerance kecil.

Boundary:

- Dua titik berbeda.

Negative test:

- Tidak ada.

Edge case:

- Urutan input dibalik.

Status:

- Planned.

## Test Case LU-003 - Nearby Coastal Distance Is Reasonable

Tujuan:

- Memastikan perhitungan Haversine menghasilkan jarak realistis untuk titik sekitar wilayah project.

Input:

- Titik user sekitar Tanjung Anom.
- Titik spot sekitar pesisir/Teluk Jakarta.

Expected result:

- Jarak lebih besar dari 0.
- Jarak berada dalam rentang realistis yang ditentukan pada data uji.

Boundary:

- Jarak dekat-menengah.

Negative test:

- Tidak ada.

Edge case:

- Koordinat pesisir.

Status:

- Planned.

## Test Case LU-004 - Handles Negative Coordinates

Tujuan:

- Memastikan koordinat lintang negatif seperti Indonesia selatan ekuator tetap dihitung benar.

Input:

- Latitude negatif dan longitude positif.

Expected result:

- Jarak valid dan tidak NaN.

Boundary:

- Koordinat southern hemisphere.

Negative test:

- Tidak ada.

Edge case:

- Nilai lintang negatif.

Status:

- Planned.

## Test Case LU-005 - Long Distance Does Not Crash

Tujuan:

- Memastikan koordinat sangat jauh tetap menghasilkan angka valid.

Input:

- Titik Indonesia.
- Titik luar negeri jauh.

Expected result:

- Jarak besar, lebih dari 1000 km.
- Tidak NaN.
- Tidak Infinity.

Boundary:

- Long distance.

Negative test:

- Tidak ada.

Edge case:

- Jarak lintas negara/benua.

Status:

- Planned.

## Unit Test Plan - RecommendationEngine.calculate

Target:

- `domain/service/RecommendationEngine.java`

Method:

- `calculate(...)`

Coverage target:

- 60-75% public behavior.

Catatan penting:

- Test tidak perlu mengejar semua private branch.
- Assert sebaiknya menggunakan range score, badge, stars, dan safety/activity output.
- Karena engine memakai waktu saat ini untuk solunar-lite, beberapa expected result lebih aman menggunakan range daripada angka absolut kaku.

## Test Case RE-001 - Null Data Returns Safe Result

Tujuan:

- Memastikan engine tidak crash saat Weather/BMKG/Marine belum tersedia.

Input:

- `tide = null`
- `weather = null`
- `marineHourly = null`
- `distance = 5.0`
- `rating = 4.0`

Expected result:

- `RecommendationResult` tidak null.
- Score berada di 0-100.
- Badge tidak kosong.
- Stars berada di 1-5.

Boundary:

- Semua data environment null.

Negative test:

- Null dependency.

Edge case:

- API belum selesai saat engine dipanggil.

Status:

- Planned.

## Test Case RE-002 - Good Conditions Produce Recommended Score

Tujuan:

- Memastikan kondisi baik menghasilkan rekomendasi layak.

Input:

- Cuaca cerah/berawan ringan.
- Gelombang rendah.
- Arus rendah.
- Jarak dekat.
- Rating spot tinggi.

Expected result:

- Score minimal berada pada kategori layak/direkomendasikan.
- Stars minimal 3.
- Safety multiplier tinggi.

Boundary:

- Kondisi ideal.

Negative test:

- Tidak ada.

Edge case:

- Data marine tersedia penuh.

Status:

- Planned.

## Test Case RE-003 - High Wave Lowers Score

Tujuan:

- Memastikan gelombang tinggi menurunkan skor dan safety.

Input:

- Weather normal.
- Marine wave tinggi, misalnya lebih dari 2.5 m.
- Jarak dekat.
- Rating tinggi.

Expected result:

- Score lebih rendah daripada kondisi baik.
- Safety multiplier turun.
- Badge tidak boleh `Sangat Direkomendasikan`.

Boundary:

- Gelombang kategori tinggi.

Negative test:

- Kondisi laut berbahaya.

Edge case:

- Gelombang tinggi walau cuaca lokal terlihat baik.

Status:

- Planned.

## Test Case RE-004 - Bad Weather Lowers Safety

Tujuan:

- Memastikan hujan lebat/petir menurunkan rekomendasi.

Input:

- Weather main `Thunderstorm` atau deskripsi storm/petir.
- Marine normal.
- Jarak dekat.
- Rating tinggi.

Expected result:

- Safety multiplier rendah.
- Score turun signifikan.
- Badge masuk kategori waspada/tidak direkomendasikan sesuai hasil.

Boundary:

- Severe weather.

Negative test:

- Cuaca berbahaya.

Edge case:

- Cuaca buruk tetapi gelombang tenang.

Status:

- Planned.

## Test Case RE-005 - Far Distance Applies Score Cap

Tujuan:

- Memastikan spot sangat jauh tidak mendapat skor terlalu tinggi meskipun cuaca bagus.

Input:

- Kondisi weather/marine baik.
- Rating tinggi.
- `distance > 1000 km`.

Expected result:

- Score dibatasi oleh distance cap.
- Tidak mencapai kategori tertinggi.

Boundary:

- Jarak sangat jauh.

Negative test:

- Spot jauh tidak boleh terlihat terlalu direkomendasikan.

Edge case:

- Distance cap.

Status:

- Planned.

## Test Case RE-006 - Low Rating Reduces Spot Quality

Tujuan:

- Memastikan rating spot rendah ikut menurunkan skor.

Input:

- Weather/marine cukup baik.
- Jarak dekat.
- Rating `2.0`.

Expected result:

- Score lebih rendah dibanding rating `5.0` pada kondisi sama.

Boundary:

- Rating rendah.

Negative test:

- Tidak ada.

Edge case:

- Spot baru/rating belum matang.

Status:

- Planned.

## Test Case RE-007 - User Preference Affects Score

Tujuan:

- Memastikan parameter user preference memengaruhi hasil akhir sesuai bobot.

Input:

- Data environment sama.
- Test A: userPreferenceScore rendah.
- Test B: userPreferenceScore tinggi.

Expected result:

- Score B lebih tinggi atau sama dibanding Score A.

Boundary:

- Preference 0 dan 100.

Negative test:

- Preference di luar range harus di-clamp.

Edge case:

- Input preference negatif atau lebih dari 100 jika overload digunakan.

Status:

- Planned.

## Unit Test Plan - RecommendationEngine.getFishActivityLabel

Target:

- `RecommendationEngine`

Method:

- `getFishActivityLabel(...)`

Coverage target:

- 60-75% public behavior.

## Test Case FA-001 - Null Data Returns Valid Label

Tujuan:

- Memastikan label aktivitas ikan tidak kosong saat data belum tersedia.

Input:

- `weather = null`
- `marineHourly = null`

Expected result:

- Label salah satu dari:
  - `Sangat Tinggi`
  - `Tinggi`
  - `Sedang`
  - `Rendah`
  - `Sangat Rendah`

Boundary:

- Semua data null.

Negative test:

- Null input.

Edge case:

- UI belum menerima API response.

Status:

- Planned.

## Test Case FA-002 - Severe Weather Does Not Produce Highest Label

Tujuan:

- Memastikan cuaca buruk tidak menghasilkan aktivitas ikan paling tinggi.

Input:

- Weather storm/heavy rain.
- Marine normal.

Expected result:

- Label bukan `Sangat Tinggi`.

Boundary:

- Severe weather.

Negative test:

- Cuaca berbahaya.

Edge case:

- Cuaca buruk tetapi data marine normal.

Status:

- Planned.

## Test Case FA-003 - Solunar Rating From Tide Influences Label

Tujuan:

- Memastikan solunar rating dari `TideResponse` dapat menaikkan aktivitas ikan.

Input:

- `TideResponse` dengan daily condition solunar rating tinggi.
- Weather normal.
- Marine normal.

Expected result:

- Label berada pada kategori baik/tinggi sesuai score.

Boundary:

- Solunar rating tinggi.

Negative test:

- Tidak ada.

Edge case:

- Data tide punya rating, weather/marine tidak sempurna.

Status:

- Planned.

## Unit Test Plan - FishingRepository Visibility Helpers

Target:

- `data/repository/FishingRepository.java`

Method:

- `normalizeVisibility`
- `isPublicSpot`
- `isOwnedByCurrentUser`
- `canUserSeeSpot`

Coverage target:

- 90-100% untuk helper visibility.

Catatan:

- Method ini static dan bisa diuji dengan model `FishingPoint` lokal.
- Walau berada di repository, test ini tidak perlu Firestore selama tidak membuat instance repository.

## Test Case VS-001 - Normalize Empty Visibility Defaults Public

Tujuan:

- Memastikan visibility kosong aman sebagai `PUBLIC`.

Input:

- `null`
- `""`
- `"   "`

Expected result:

- Semua menghasilkan `PUBLIC`.

Boundary:

- Null/blank.

Negative test:

- Input kosong.

Edge case:

- Whitespace.

Status:

- Planned.

## Test Case VS-002 - Normalize Private Variants

Tujuan:

- Memastikan `PRIVATE` dan `PRIVAT` dikenali sebagai private.

Input:

- `PRIVATE`
- `private`
- `Privat`

Expected result:

- Semua menghasilkan `PRIVATE`.

Boundary:

- Case-insensitive.

Negative test:

- Tidak ada.

Edge case:

- Bahasa Indonesia `PRIVAT`.

Status:

- Planned.

## Test Case VS-003 - Unknown Visibility Defaults Public

Tujuan:

- Memastikan value tidak dikenal tidak membuat spot terkunci tanpa sengaja.

Input:

- `friends`
- `shared`
- `abc`

Expected result:

- `PUBLIC`

Boundary:

- Unknown value.

Negative test:

- Input tidak sesuai schema.

Edge case:

- Data Firestore typo.

Status:

- Planned.

## Test Case VS-004 - Public Spot Visible To Anonymous User

Tujuan:

- Memastikan spot public dapat terlihat walau user id null.

Input:

- `FishingPoint.visibility = PUBLIC`
- `userId = null`

Expected result:

- `canUserSeeSpot = true`

Boundary:

- User belum login/null.

Negative test:

- Tidak ada.

Edge case:

- Public spot pada dashboard/map.

Status:

- Planned.

## Test Case VS-005 - Private Spot Visible Only To OwnerId

Tujuan:

- Memastikan private spot hanya terlihat oleh owner.

Input:

- `visibility = PRIVATE`
- `ownerId = user_a`
- Test user `user_a` dan `user_b`

Expected result:

- `user_a = true`
- `user_b = false`

Boundary:

- Owner vs non-owner.

Negative test:

- User lain tidak boleh melihat.

Edge case:

- Multi-akun Firestore.

Status:

- Planned.

## Test Case VS-006 - Legacy UserId Still Counts As Owner

Tujuan:

- Menjaga kompatibilitas field lama `userId`.

Input:

- `ownerId = null`
- `userId = user_a`
- current user `user_a`

Expected result:

- `isOwnedByCurrentUser = true`
- `canUserSeeSpot = true` jika private.

Boundary:

- Legacy field.

Negative test:

- User berbeda false.

Edge case:

- Data lama atau dokumen yang belum lengkap.

Status:

- Planned.

## Future Unit Test Plan Setelah Refactor

## SpotVisibilityPolicy

Jika helper ini dibuat, test yang sama dengan VS-001 sampai VS-006 dipindahkan ke class policy baru. Method lama di `FishingRepository` dapat diuji minimal sebagai delegasi jika masih dipertahankan.

## DistanceFormatter

Test plan:

- DF-001: format jarak valid satu desimal.
- DF-002: jarak 0 menampilkan label dekat/0 km sesuai aturan final.
- DF-003: jarak negatif menampilkan fallback.
- DF-004: jarak besar tetap terbaca.

## RecommendationDisplayFormatter

Test plan:

- RF-001: score 90 menghasilkan kategori sangat baik.
- RF-002: score 70 menghasilkan kategori recommended.
- RF-003: score 55 menghasilkan kategori cukup.
- RF-004: score rendah menghasilkan waspada/tidak aman.
- RF-005: stars selalu berada 1-5.
- RF-006: score di luar range di-clamp.

## SpotImageHelper

Test plan:

- SI-001: pantai memilih placeholder pantai.
- SI-002: muara/sungai memilih placeholder muara.
- SI-003: dermaga memilih placeholder dermaga.
- SI-004: bagan/rumpon/tambak memilih fallback breakwater.
- SI-005: Cloudinary URL diberi transformasi thumbnail.
- SI-006: URL non-Cloudinary tidak diubah.
- SI-007: URL kosong/null tetap aman.

## NearbySpotSelector

Test plan:

- NS-001: sort spot dari jarak terdekat.
- NS-002: limit list diterapkan.
- NS-003: radius nearby dihitung benar.
- NS-004: private spot user lain dikeluarkan.
- NS-005: public spot tetap masuk.
- NS-006: koordinat invalid ditangani aman.

## BMKG Parser / Area Resolver

Test plan:

- BMKG-001: sample JSON forecast menghasilkan list forecast.
- BMKG-002: warning terbaca.
- BMKG-003: wave category terbaca.
- BMKG-004: wind speed/direction terbaca.
- BMKG-005: koordinat dalam polygon memilih area benar.
- BMKG-006: koordinat luar polygon memakai fallback aman.

## Minimal Test Set Untuk Implementasi Pertama

Jika implementasi test dimulai, urutan minimal yang paling aman:

1. LU-001
2. LU-002
3. LU-003
4. RE-001
5. RE-003
6. RE-005
7. FA-001
8. VS-001
9. VS-005
10. VS-006

Alasan:

- Mewakili Haversine.
- Mewakili recommendation engine.
- Mewakili safety/gelombang.
- Mewakili distance cap.
- Mewakili private/public owner policy.

## Expected Coverage Setelah Implementasi Minimal

| Area | Expected Coverage Awal |
| --- | ---: |
| `LocationUtils` | 90-100% |
| `RecommendationEngine` public behavior | 40-60% |
| Visibility helper | 70-90% |
| Total project coverage | Rendah, tetapi bernilai tinggi |

Catatan: Total project coverage tidak menjadi target utama karena banyak kode Android/Firebase/API tidak cocok untuk unit test lokal.

## Keputusan Phase 6

Project layak lanjut ke **Phase 7 - Black Box Test Plan**.

Unit test plan sudah cukup untuk memulai implementasi test setelah Phase 7 selesai dan setelah user menyetujui masuk Phase 8.

## Catatan Untuk Skripsi

Unit test plan ini dapat dimasukkan sebagai lampiran atau bahan BAB IV. Bagian paling penting untuk dijelaskan:

- Haversine diuji dengan titik sama, simetri, dan jarak realistis.
- Recommendation diuji dengan kondisi baik, buruk, gelombang tinggi, dan jarak jauh.
- Private/public spot diuji dengan owner dan non-owner.
