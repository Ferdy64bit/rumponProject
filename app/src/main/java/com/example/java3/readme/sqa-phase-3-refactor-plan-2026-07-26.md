# SQA Phase 3 - Refactor Plan

Tanggal plan: 26 Juli 2026

Project: **Fishing Point Tanjung Anom**

Status: **Perencanaan refactor tanpa perubahan kode**

Dokumen acuan:

- `masterruleoop.md`
- `sqa-phase-1-project-audit-2026-07-26.md`
- `sqa-phase-2-oop-solid-review-2026-07-26.md`
- `uiuxx.md`
- `auidituiux.md`

## Tujuan Phase 3

Phase ini menyusun rencana refactor yang aman, kecil, dan bertahap. Targetnya adalah meningkatkan maintainability, OOP, SOLID, dan testability tanpa mengubah fitur yang sudah berjalan.

Phase ini belum melakukan implementasi kode.

## Prinsip Refactor

Semua refactor nanti wajib mengikuti aturan berikut:

- Tidak mengubah MVVM.
- Tidak mengubah Repository Pattern.
- Tidak mengubah UI final.
- Tidak mengubah flow aplikasi.
- Tidak mengubah schema Firestore besar-besaran.
- Tidak menambah dependency besar.
- Tidak memakai Lombok.
- Tidak memakai Hibernate.
- Tidak memakai Dagger/Hilt.
- Refactor dilakukan satu class atau satu helper kecil dalam satu waktu.
- Setiap refactor harus bisa diverifikasi dengan build dan test.

## Strategi Umum

Refactor tidak dimulai dari class paling besar secara langsung. Refactor dimulai dari helper paling kecil dan paling aman agar risiko regresi rendah.

Urutan strategi:

1. Ambil pure function yang tersebar.
2. Jadikan helper/domain policy kecil.
3. Buat unit test untuk helper tersebut.
4. Baru kurangi duplikasi di repository.
5. Baru sentuh class besar seperti `MapFragment` dan `TideRepository` secara bertahap.

## Priority Tinggi

## 1. Unit-test-ready Helper Untuk Haversine

Target class:

- `LocationUtils`

Masalah:

- Tidak ada masalah besar. Class ini sudah kecil dan pure.

Rencana:

- Tidak perlu refactor kode.
- Jadikan `LocationUtils.calculateDistance` sebagai unit test pertama.

Manfaat:

- Bukti langsung untuk metode Haversine pada skripsi.
- Risiko sangat rendah.

Risiko:

- Hampir tidak ada jika tidak mengubah behavior.

Status:

- Siap lanjut ke Phase 5/6.

## 2. RecommendationEngine Public Behavior Test Plan

Target class:

- `RecommendationEngine`

Masalah:

- Class cukup besar, tetapi masih cohesive.
- Banyak private method belum bisa diuji langsung.

Rencana refactor:

- Untuk tahap awal, jangan pecah class.
- Uji public method `calculate` dan `getFishActivityLabel` berdasarkan skenario.
- Jika test public behavior sudah stabil, baru pertimbangkan extract calculator kecil.

Manfaat:

- Menghindari risiko mengubah formula final.
- Formula tetap mudah dijelaskan karena masih berada di satu engine.

Risiko:

- Private score tidak diuji langsung, tetapi masih bisa divalidasi melalui hasil akhir.

Status:

- Siap masuk Phase 5/6 tanpa refactor awal.

## 3. Extract Spot Visibility Policy

Target saat ini:

- `FishingRepository.normalizeVisibility`
- `FishingRepository.isPublicSpot`
- `FishingRepository.isOwnedByCurrentUser`
- `FishingRepository.canUserSeeSpot`

Masalah:

- Visibility dan ownership adalah business rule domain, tetapi saat ini berada di repository.
- Rule ini dipakai oleh Map, Dashboard, Profile, dan Spot List.

Rencana refactor:

- Buat helper/domain policy kecil, misalnya `SpotVisibilityPolicy`.
- Pindahkan logic pure tanpa mengubah hasil:
  - normalize `PUBLIC` / `PRIVATE`,
  - cek public spot,
  - cek owner,
  - cek user boleh melihat spot.
- Pertahankan method lama sementara sebagai delegasi jika ingin mengurangi perubahan pemanggil.

Manfaat:

- Unit test ownership/private-public lebih mudah.
- Rule akses spot menjadi eksplisit untuk skripsi.
- Mengurangi coupling domain rule dengan Firestore repository.

Risiko:

- Jika pemindahan tidak hati-hati, private/public spot bisa berubah behavior.

Mitigasi:

- Buat unit test sebelum mengganti semua pemanggil.
- Uji multi-akun setelah refactor.

Status:

- Kandidat refactor pertama setelah unit test dasar.

## 4. Extract Display Formatter Ringan

Target logic tersebar:

- `nonBlank`
- `firstNonBlank`
- `createStars`
- label safety,
- label aktivitas ikan,
- formatter jarak,
- formatter tanggal sederhana,
- label visibility.

Masalah:

- Formatter tersebar di `HomeFragment`, `ProfileFragment`, `MapFragment`, adapter, dan repository.
- Risiko label tidak konsisten antar screen.

Rencana refactor:

- Jangan buat satu helper raksasa.
- Buat helper kecil berdasarkan domain tampilan jika benar diperlukan:
  - `DistanceFormatter`,
  - `RecommendationDisplayFormatter`,
  - `SpotDisplayFormatter`.
- Pindahkan hanya logic yang dipakai lebih dari satu tempat.

Manfaat:

- Label UI konsisten.
- Unit test formatter mudah.
- Mengurangi method kecil di Fragment.

Risiko:

- Terlalu banyak helper kecil dapat membuat project terasa tersebar.

Mitigasi:

- Refactor hanya formatter yang benar-benar duplikatif.
- Jangan ubah text UI kecuali memang sama hasilnya.

Status:

- Kandidat setelah Spot Visibility Policy.

## 5. Extract Cloudinary Upload Service

Target class:

- `CommunityRepository`
- `ProfileRepository`
- `FishingRepository`

Masalah:

- Multipart upload Cloudinary muncul berulang.
- `HttpURLConnection`, boundary, form field, file field, response parsing, dan error handling tersebar.

Rencana refactor:

- Buat service/helper kecil, misalnya `CloudinaryUploadService`.
- Service menerima:
  - image byte array,
  - upload preset,
  - optional folder/type jika sudah ada pola,
  - callback/result.
- Repository tetap bertanggung jawab menyimpan URL ke Firestore.
- Jangan pindahkan Firestore write ke Cloudinary service.

Manfaat:

- Duplikasi turun signifikan.
- Error upload lebih konsisten.
- Mudah audit karena Firebase Storage tidak dipakai.

Risiko:

- Upload profile/post/spot bisa regress jika field multipart berubah.

Mitigasi:

- Refactor satu flow dulu, idealnya spot photo atau profile photo.
- Setelah pass, baru community upload.
- Uji upload nyata di device.

Status:

- Priority tinggi, tetapi dilakukan setelah helper pure function karena punya risiko network.

## Priority Sedang

## 6. Extract Nearby Spot Selector

Target class:

- `HomeViewModel`
- `SpotListActivity`

Masalah:

- Filtering, sorting, dan mapping jarak/rekomendasi spot berada di ViewModel/Activity.
- Logic ini penting untuk dashboard dan skripsi.

Rencana refactor:

- Buat helper pure, misalnya `NearbySpotSelector` atau `SpotDistanceSorter`.
- Input:
  - list `FishingPoint`,
  - user latitude/longitude,
  - optional current user id,
  - limit/radius.
- Output:
  - list dengan jarak atau wrapper sederhana.

Manfaat:

- Hasil spot terdekat lebih mudah diuji.
- Dashboard dan Spot List bisa konsisten.

Risiko:

- Jika urutan spot berubah, UI terlihat berbeda walau logic lebih benar.

Mitigasi:

- Gunakan formula jarak yang sama.
- Uji dengan data spot tetap.

Status:

- Bagus untuk Phase 8 setelah unit test dasar selesai.

## 7. Extract Spot Image Helper

Target class:

- `MapFragment`
- `DetailSpotActivity`
- `FishingPointAdapter`
- `SpotListAdapter`

Masalah:

- Placeholder spot berdasarkan tipe dan transformasi thumbnail Cloudinary berpotensi tidak konsisten.

Rencana refactor:

- Buat helper kecil, misalnya `SpotImageHelper`.
- Fungsi:
  - memilih drawable fallback berdasarkan tipe spot,
  - membuat Cloudinary thumbnail URL ringan.

Manfaat:

- Foto spot konsisten di Map, Detail, Dashboard, dan List.
- Mengurangi logic display di `MapFragment`.

Risiko:

- Jika mapping drawable berubah, tampilan spot default bisa berubah.

Mitigasi:

- Salin mapping lama persis.

Status:

- Priority sedang, aman dilakukan setelah visibility helper.

## 8. Reduce MapFragment Responsibility

Target class:

- `MapFragment`

Masalah:

- Terlalu banyak tanggung jawab dalam satu Fragment.

Rencana refactor bertahap:

1. Pindahkan helper pure: image, visibility label, formatter.
2. Pindahkan dialog add/edit marker ke helper/builder jika masih terlalu besar.
3. Pindahkan route polyline helper hanya jika sudah ada test/manual validation.

Yang tidak boleh dilakukan:

- Jangan ubah layout Map.
- Jangan ubah flow tambah/edit/delete.
- Jangan hilangkan navigasi Google Maps.
- Jangan ubah behavior private/public.

Manfaat:

- `MapFragment` lebih mudah dipelihara.
- Risiko bug marker lebih mudah dilacak.

Risiko:

- Map adalah fitur inti, refactor terlalu besar bisa menyebabkan regress.

Mitigasi:

- Refactor kecil satu per satu.
- Setelah setiap langkah, build dan uji tap marker.

Status:

- Jangan menjadi refactor pertama. Lakukan setelah helper tersedia.

## 9. TideRepository BMKG Parser Extraction

Target class:

- `TideRepository`

Masalah:

- Network, parser, polygon, endpoint, cache, fallback, dan mapper bercampur.

Rencana refactor bertahap:

1. Extract mapper/parser forecast BMKG.
2. Extract polygon resolver BMKG.
3. Extract endpoint builder jika perlu.
4. Cache tetap di repository sampai parser stabil.

Manfaat:

- Parser BMKG bisa diuji dengan JSON sample.
- Risiko perubahan format BMKG lebih mudah dikontrol.
- Penjelasan BAB IV lebih rapi.

Risiko:

- BMKG adalah data penting; refactor parser berisiko membuat data tidak tampil.

Mitigasi:

- Jangan ubah response mapping saat refactor awal.
- Gunakan JSON sample dari endpoint BMKG yang sudah berjalan.
- Uji Dashboard dan Map setelah refactor.

Status:

- Priority sedang-tinggi, tetapi dilakukan setelah unit test dasar dan black box baseline.

## 10. CommunityRepository Responsibility Reduction

Target class:

- `CommunityRepository`

Masalah:

- Feed, author enrichment, media upload, like, favorite, comment, delete cleanup berada di satu repository.

Rencana refactor:

- Extract Cloudinary upload lebih dulu.
- Extract favorite collection helper jika dibutuhkan.
- Jangan pecah seluruh repository menjadi banyak repository baru pada tahap skripsi.

Manfaat:

- Risiko upload/media lebih mudah diuji.
- Repository lebih fokus pada Firestore community.

Risiko:

- Community feed bisa regress jika cleanup/favorite schema disentuh.

Mitigasi:

- Jangan ubah query feed dulu.
- Uji create post, like, comment, bookmark, delete setelah perubahan.

Status:

- Priority sedang setelah Cloudinary service siap.

## Priority Rendah

## 11. ProfileRepository Responsibility Reduction

Target class:

- `ProfileRepository`

Masalah:

- Profile data, stats, Cloudinary, reset password, my posts/spots/favorites, dan sync author berada dalam satu class.

Rencana refactor:

- Extract Cloudinary upload melalui service yang sama.
- Jangan pecah stats dulu kecuali ada bug.
- Jangan ubah reset password karena sudah berjalan.

Manfaat:

- Duplikasi upload berkurang.
- Profile tetap stabil.

Risiko:

- Profile sudah sensitif karena menyinkronkan avatar Community.

Mitigasi:

- Uji edit profile dan avatar community setelah refactor upload.

Status:

- Priority rendah-sedang, menunggu Cloudinary service.

## 12. Audit Room Dependency

Target file:

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

Masalah:

- Dependency Room ada, tetapi belum terlihat sebagai komponen utama source.

Rencana:

- Cari penggunaan `androidx.room`, `@Entity`, `@Dao`, `@Database`.
- Jika tidak ada penggunaan, masukkan ke daftar kandidat penghapusan saat optimasi release.
- Jangan hapus sebelum testing utama selesai.

Manfaat:

- Mengurangi dependency yang tidak dipakai.
- Potensi ukuran build lebih ringan.

Risiko:

- Jika ada penggunaan tersembunyi atau rencana fitur, penghapusan bisa mengganggu.

Mitigasi:

- Audit menyeluruh sebelum menghapus.

Status:

- Priority rendah, masuk fase optimasi.

## 13. Constructor Injection Manual Untuk Testability

Target class:

- ViewModel tertentu.
- Repository tertentu.

Masalah:

- Banyak class membuat dependency langsung dengan `new`.

Rencana:

- Jangan gunakan Dagger/Hilt.
- Jika unit test membutuhkan, tambahkan constructor overload package-private atau public yang menerima dependency.
- Constructor default tetap dipertahankan agar flow aplikasi tidak berubah.

Manfaat:

- Testability meningkat tanpa dependency besar.

Risiko:

- Terlalu banyak constructor bisa membingungkan.

Mitigasi:

- Hanya lakukan pada class yang benar-benar akan diuji.

Status:

- Priority rendah, dilakukan sesuai kebutuhan test.

## Urutan Refactor Yang Disarankan

Urutan ini untuk Phase 8 nanti, setelah Phase 4-7 selesai:

1. Tambah unit test untuk `LocationUtils` tanpa refactor.
2. Tambah unit test public behavior `RecommendationEngine` tanpa refactor.
3. Refactor `SpotVisibilityPolicy` dari helper static visibility.
4. Tambah unit test `SpotVisibilityPolicy`.
5. Refactor formatter kecil yang benar-benar duplikatif.
6. Tambah unit test formatter.
7. Extract `SpotImageHelper`.
8. Extract `CloudinaryUploadService` dari satu flow upload dulu.
9. Terapkan Cloudinary service ke flow lain setelah uji device berhasil.
10. Extract nearby spot selector jika Dashboard/SpotList butuh test lebih kuat.
11. Baru refactor kecil `MapFragment` dengan bantuan helper yang sudah ada.
12. Baru refactor parser BMKG dari `TideRepository` jika baseline testing sudah aman.

## Yang Tidak Direkomendasikan

- Pecah semua repository sekaligus.
- Redesign UI.
- Rename besar-besaran dari `Tide` ke `Marine/BMKG` di kode produksi.
- Mengubah schema Firestore sekarang.
- Menambah dependency testing besar.
- Membuat UI automation besar sebelum unit test logic selesai.
- Menghapus Room dependency sebelum audit pemakaian lengkap.

## Dampak Terhadap Testing

Refactor plan ini mendukung testing dengan cara berikut:

- Haversine dapat diuji langsung.
- Recommendation Engine dapat diuji lewat skenario public behavior.
- Private/public ownership dapat diuji sebagai domain policy.
- Formatter jarak/bintang/status bisa diuji tanpa Android UI.
- BMKG parser bisa diuji setelah diekstrak.
- Cloudinary upload tetap diuji manual/integration karena bergantung network.

## Risiko Global Refactor

Risiko terbesar project saat refactor:

1. Data Maps berubah karena visibility/owner logic berubah.
2. Upload foto gagal karena Cloudinary multipart berubah.
3. Dashboard kehilangan data BMKG karena parser berubah.
4. UI final bergeser jika refactor menyentuh layout.
5. Recommendation score berubah tanpa sadar.

Mitigasi global:

- Buat baseline screenshot sebelum refactor.
- Build setelah setiap langkah.
- Jalankan unit test setelah helper dibuat.
- Uji manual fitur yang terdampak langsung.
- Catat setiap perubahan di refactor log.

## Keputusan Phase 3

Project layak lanjut ke **Phase 4 - Business Logic Audit**.

Prioritas utama bukan refactor besar, tetapi membuat jalur testing yang aman:

1. Haversine.
2. Recommendation Engine.
3. Spot visibility/ownership.
4. Formatter yang berulang.
5. Cloudinary upload duplication.
6. BMKG parser extraction.

## Catatan Untuk Skripsi

Refactor plan ini dapat dijadikan bukti bahwa kualitas software dikontrol dengan pendekatan bertahap. Aplikasi tidak diubah secara besar setelah finalisasi UI, tetapi diperkuat melalui pemisahan tanggung jawab, peningkatan testability, dan pengurangan duplikasi.
