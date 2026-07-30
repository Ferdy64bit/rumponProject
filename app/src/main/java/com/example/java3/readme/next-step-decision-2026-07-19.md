# Fishing Point - Next Step Decision

Tanggal update: 23 Juli 2026

Dokumen ini berisi analisis kondisi project saat ini dan urutan langkah berikutnya yang paling masuk akal untuk dikerjakan setelah migrasi BMKG, cache, dan Recommendation Engine FAI selesai.

## Kondisi Saat Ini

Project sudah memiliki fondasi yang cukup kuat untuk skripsi:

- Auth, Firestore, Cloudinary, Maps, Home, Community, dan Profile sudah berjalan.
- BMKG Peta Maritim sudah menggantikan TideCheck.
- Dashboard sudah menampilkan forecast BMKG dalam bentuk list horizontal.
- Cache API eksternal sudah diberi TTL dan fallback agar tidak membengkak.
- Recommendation Engine sudah memakai formula final Base Score x Safety Multiplier dengan lapisan live weather dan hourly wave.
- Dashboard dan Maps sudah mengikuti pemisahan sumber data yang benar: Dashboard memakai lokasi user, sedangkan marker Maps memakai koordinat fishing point untuk Weather/BMKG dan posisi user untuk Haversine.
- Community/Post sudah masuk fase penguatan: Create Post memakai lokasi aktif user, menyimpan ringkasan Weather/BMKG, upload gambar memakai Cloudinary, feed realtime dibatasi, dan delete post membersihkan data terkait.
- Profile dan Community sudah disambungkan lebih kuat: perubahan nama/foto profile disinkronkan ke posting community, dan feed membaca author terbaru dari collection `users` agar avatar tidak tertinggal pada post lama.
- Perbaikan dari screenshot user sudah dikerjakan: detail spot pada Maps kini memiliki aksi favorite, share, dan navigasi; Community bookmark post kompatibel schema lama/baru; Profile `Postingan Saya` tidak lagi memicu error composite index; reset password memakai fallback email profile.
- Recommendation Engine sudah diperbarui ke formula final `Base Score x Safety Multiplier` dengan OpenWeather untuk cuaca live, Open-Meteo Marine untuk gelombang hourly, dan BMKG untuk warning/forecast maritim.
- Dashboard sudah menampilkan grafik gelombang per jam melalui custom chart, jadi dependency chart tambahan belum diperlukan.
- Engine rekomendasi sudah diperkuat dengan distance cap berbasis Haversine agar spot yang sangat jauh tidak lagi terlihat terlalu direkomendasikan.
- Spot List sekarang memakai data lingkungan per koordinat spot ketika tersedia, sehingga hasil bintang lebih sejalan dengan Detail Spot.
- Fish Activity Score sudah diperbaiki dengan pendekatan solunar-lite agar lebih dekat dengan pola aplikasi Fishing Points: sunrise/sunset window, fase bulan, tekanan udara, cuaca, pergerakan air, dan penalti jam malam.
- Map sekarang memiliki polyline geodesic user-ke-spot sebagai fallback visual untuk area perairan, tanpa menghapus tombol navigasi Google Maps.
- Card Map memakai foto spot asli dari `imageUrl`/Cloudinary jika tersedia, dengan fallback gambar sesuai tipe spot. Input URL foto tidak lagi ditampilkan di tambah marker; ganti/hapus foto dilakukan dari Detail Spot.
- Community Create Post sekarang memakai foto, caption, dan jenis ikan opsional saja. Berat ikan dan umpan sudah dihapus dari UI agar upload lebih ringan untuk user awam.
- Preview gambar Create Post memakai rasio responsif agar tampil baik saat foto potret maupun landscape dipilih dari galeri.
- Feed Community sebaiknya diuji lagi supaya yang tampil cukup caption sebagai konten utama, dengan jenis ikan dan lokasi hanya bila tersedia.
- Gambar pada feed komunitas juga memakai rasio responsif, jadi tampilan posting tetap rapi saat foto potret atau landscape dibagikan.
- Skala angin engine disatukan ke km/h: OpenWeather `m/s` dikonversi ke km/h, BMKG `knot` dikonversi ke km/h, lalu engine memakai risiko tertinggi untuk safety.
- Build Java debug sudah berhasil.

## Update Safety Engine 26 Juli 2026

Safety engine sudah diperketat berdasarkan temuan lapangan user: angin sekitar 16-20 km/jam di perairan Indonesia dapat terasa kurang bersahabat untuk perahu kecil, sehingga nilai `Aman` lama terlalu optimistis.

Keputusan terbaru:

- Safety tidak lagi hanya mengandalkan angin OpenWeather live. Engine juga membaca angin maritim BMKG dari `wind_speed_min` dan `wind_speed_max`.
- Satuan disatukan: OpenWeather `m/s` ke `km/h`, BMKG `knot` ke `km/h` sesuai catatan dokumentasi BMKG di project.
- Nilai angin yang dipakai adalah risiko tertinggi antara OpenWeather dan BMKG.
- Warning BMKG sekarang ikut menekan `Safety Multiplier`, terutama warning tinggi, badai, petir, ekstrem, sangat tinggi, atau bahaya.
- Penalti kombinasi diterapkan untuk angin + gelombang, angin + arus, dan angin + hujan.
- Label safety dashboard diperketat: `Aman` hanya untuk minimal 90%, `Cukup Aman` untuk minimal 75%, `Waspada` untuk minimal 55%, `Berisiko` untuk minimal 40%, dan sisanya `Tidak aman`.

Tabel safety angin yang digunakan sebagai acuan implementasi:

| Angin | Interpretasi UI/Engine |
| --- | --- |
| 0-8 km/h | Aman / sangat tenang |
| >8-14 km/h | Sepoi / stabil |
| >14-19 km/h | Cukup aman, mulai hati-hati |
| >19-28 km/h | Waspada |
| >28-38 km/h | Berisiko untuk perahu kecil |
| >38-49 km/h | Risiko tinggi |
| >49 km/h | Tidak disarankan |

Validasi terakhir:

- Unit test engine untuk warning BMKG dan angin BMKG tinggi sudah ditambahkan.
- `testDebugUnitTest`, `assembleDebug`, install debug, dan launch device nyata berhasil.
- Pada device nyata, kondisi BMKG `19-35 knot` dikonversi menjadi risiko tinggi sehingga dashboard turun ke safety rendah dan `Tidak Direkomendasikan`. Ini sesuai prinsip konservatif untuk keselamatan.

## Validasi Real Device 22 Juli 2026

Pengecekan di device `TECNO_LJ8k` sudah menunjukkan alur data utama berjalan:

- Dashboard memuat Weather, BMKG, dan Open-Meteo Marine dari sumber aktif.
- Build Java debug berhasil, install debug berhasil di device `TECNO LJ8k`, dan launch aplikasi tidak memunculkan `FATAL EXCEPTION` pada logcat.
- Dashboard menampilkan rekomendasi live, forecast BMKG, dan grafik gelombang per jam.
- Maps memuat spot Firestore dan marker environment berdasarkan koordinat spot.
- Spot list menampilkan bintang rekomendasi berbasis kalkulasi engine, bukan rating statis saja.
- Detail spot menampilkan data live untuk cuaca, angin, kelembapan, aktivitas ikan, gelombang, dan skor rekomendasi.
- Artefak dump UI hasil validasi sudah dibersihkan dari root project.

Screenshot validasi disimpan di folder `readme/screenshots`.

## Update Community/Post 19 Juli 2026

Bagian Community/Post sudah mulai difinalkan karena Dashboard dan Maps sudah cukup kuat untuk kebutuhan inti skripsi.

Yang sudah dikerjakan:

- `CreatePostFragment` memakai `getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY)` lebih dahulu, lalu fallback ke last known location, lalu fallback Tanjung Anom jika lokasi tidak tersedia.
- Create Post mengambil Weather dan BMKG berdasarkan lokasi saat posting dibuat.
- Data post menyimpan latitude, longitude, nama lokasi, kondisi cuaca, dan kondisi perairan.
- Upload gambar community tetap memakai Cloudinary unsigned preset `fishing_post_upload`; secret Cloudinary tidak ditanam di APK.
- Tampilan info cuaca dan perairan di Create Post dibuat vertikal agar teks BMKG panjang tetap rapi.
- Feed realtime community dibatasi `50` posting untuk mencegah pembengkakan query Firestore.
- Delete post membersihkan dokumen terkait di `likes`, `favorites`, dan subcollection `comments` sampai batas batch client.
- Verifikasi compile dan install device setelah perubahan: berhasil.
- Bug avatar Community yang tidak mengikuti foto profile terbaru sudah dibenahi dengan dua lapis sinkronisasi: author enrichment saat feed dibaca dan update field author pada `community_posts` saat profile berubah.
- Build dan install debug setelah perbaikan avatar Community/Profile berhasil pada device `TECNO_LJ8k`.
- Detail spot yang sebelumnya menampilkan toast "fitur detail spot belum tersedia" sudah diperbaiki: tombol favorite, share, navigasi, dan simpan favorit sekarang menjalankan fungsi nyata.
- Map mengirim data lengkap ke Detail Spot, termasuk id spot, koordinat, tipe, deskripsi, imageUrl, rating, review, dan jarak.
- Favorite/bookmark posting Community sekarang membaca beberapa bentuk schema: `type = community_post`, `type = post`, dan `targetType = post`.
- Profile `Postingan Saya` diubah dari query `whereEqualTo + orderBy` menjadi query `whereEqualTo` lalu sort di client, sehingga tidak membutuhkan composite index Firestore.
- Reset password Profile memakai email dari FirebaseAuth atau fallback email dari profile Firestore.
- Verifikasi teknis: `:app:compileDebugJavaWithJavac` dan `:app:installDebug` berhasil setelah perbaikan.
- `MarineWeatherService`, `MarineHourlyResponse`, dan `MarineHourlyRepository` ditambahkan untuk Open-Meteo Marine hourly wave.
- HomeViewModel dan MapViewModel sekarang memuat Open-Meteo Marine sesuai koordinat aktif.
- Dashboard menampilkan gelombang hourly sekarang dan maksimum hari ini dari Open-Meteo Marine.
- Map marker menampilkan ringkasan gelombang Open-Meteo Marine berdasarkan koordinat spot.
- RecommendationEngine menghitung `Base Score = 0.20D + 0.20W + 0.25M + 0.15A + 0.10S + 0.10U`, lalu dikoreksi `Safety Multiplier`.
- Compile dan install debug berhasil pada device `TECNO_LJ8k` setelah implementasi engine baru.

Yang belum selesai:

- Uji membuat post nyata dengan foto dari galeri di device.
- Pastikan preset Cloudinary `fishing_post_upload` aktif sebagai unsigned upload preset melalui upload nyata.
- Pastikan post baru muncul di feed setelah upload.
- Uji like, unlike, comment, favorite, share, dan delete dari akun yang sama.
- Uji ganti foto profile lalu kembali ke Community untuk memastikan avatar feed berubah.
- Uji Detail Spot dari Maps: favorite, share, navigasi, dan simpan ke favorit.
- Uji Profile > Postingan Saya tanpa error index.
- Uji Profile > Spot Favorit setelah menyimpan spot dari Detail Spot.
- Uji reset password dan cek inbox email akun.
- Uji Dashboard dengan koneksi aktif sampai grafik gelombang per jam dan ringkasan laut tampil konsisten.
- Uji beberapa marker Maps dan pastikan gelombang hourly mengikuti koordinat marker, bukan koordinat user.
- Uji skenario rekomendasi saat BMKG warning aktif, angin BMKG tinggi, dan Open-Meteo wave tinggi.
- Jika jumlah komentar sangat banyak, cleanup recursive tetap lebih tepat dilakukan melalui backend/Admin API, bukan langsung dari Android client.

## Update Eksekusi Lanjutan 19 Juli 2026

Langkah dari rekomendasi kerja sudah mulai dieksekusi:

- Area selector BMKG dibuat melalui `BMKGAreaSelector`.
- BMKG tidak lagi selalu memakai default; koordinat Tanjung Anom/Teluk Jakarta dan beberapa wilayah pesisir utama sudah dipetakan ke endpoint BMKG yang sesuai.
- `FishingRepository.getWeather()` sekarang memakai `WeatherRepository`, sehingga Home dan Maps memakai cache/fallback cuaca yang sama.
- Hardcoded `WEATHER_API_KEY` lama di `Constants` dihapus dari jalur aktif; OpenWeather memakai `BuildConfig.OPEN_WEATHER_API_KEY` dari `local.properties`.
- Maps recommendation sekarang menerima jarak Haversine user-ke-spot, bukan jarak `0`.
- Firestore schema final dibuat di `firestore-schema-final-2026-07-19.md`.
- Testing plan environment dan recommendation dibuat di `testing-plan-environment-recommendation-2026-07-19.md`.
- Verifikasi build: `:app:compileDebugJavaWithJavac` berhasil.

## Analisis Kebutuhan Berikutnya

Masalah terbesar yang masih tersisa bukan lagi di inti logika, tetapi di konsistensi data, pemilihan area BMKG, dan bukti skripsi.

Hal yang masih paling penting:

1. Testing nyata perlu diarahkan ke Community/Post dan Profile karena Dashboard/BMKG sudah tervalidasi lebih kuat.
2. Tap marker Maps masih perlu diulang sebagai bukti screenshot final bahwa Weather/BMKG marker memakai koordinat spot.
3. Dokumentasi BAB IV/BAB V belum final.
4. Schema Firestore final sudah dibuat, tetapi perlu dicocokkan dengan data Firestore nyata setelah post/community diuji.
5. Selector area BMKG sudah memakai polygon resmi, tetapi tetap perlu contoh uji beberapa wilayah agar bisa dijelaskan saat sidang.

## Prioritas Langkah Berikutnya

### 1. Testing Real Device Community/Post

Ini sekarang menjadi prioritas utama karena core Dashboard/Maps/BMKG sudah cukup stabil.

Target hasil:

- Buka Community dan Create Post pada device real.
- Pilih foto dari galeri dan upload ke Cloudinary preset `fishing_post_upload`.
- Pastikan lokasi, cuaca, dan perairan otomatis terisi sesuai lokasi device.
- Pastikan post baru muncul di feed realtime.
- Uji like/unlike, comment, favorite/bookmark, share, detail, dan delete.
- Pastikan avatar author di feed memakai foto terbaru dari Profile, bukan placeholder atau foto lama.
- Pastikan ikon bookmark tetap aktif setelah feed reload, pindah tab, dan scroll.
- Simpan screenshot feed, create post, detail post, komentar, dan hasil upload untuk BAB IV.

### 2. Validasi Maps Marker Final

Ini perlu sebagai bukti akademik bahwa Maps tidak memakai data lingkungan dari lokasi user untuk semua marker.

Kenapa penting:

- Judul skripsi sangat bergantung pada Maps, LBS, dan Haversine.
- Marker harus menampilkan perkiraan lingkungan berdasarkan area perairan di sekitar spot.
- Jarak harus tetap user-ke-spot agar metode Haversine bisa dibuktikan.

Target hasil:

- Tap beberapa marker berbeda.
- Cocokkan log `MAP_FRAGMENT spot environment uses spot location lat=... lon=...` dengan marker yang dipilih.
- Pastikan `WEATHER_REPOSITORY request lat=... lon=...` memakai koordinat spot.
- Pastikan `BMKG_REPOSITORY polygon selector area=...` berubah sesuai wilayah marker jika marker berada di wilayah perairan berbeda.
- Ambil screenshot marker card berisi jarak, cuaca, BMKG, dan skor rekomendasi.
- Buka detail spot dari marker, lalu uji favorite, share, navigasi, dan tombol simpan favorit.
- Setelah menyimpan favorit, buka Profile > Spot Favorit untuk memastikan spot yang sama muncul.

### 3. Profile dan Cloudinary Profile

Ini penting karena seluruh rekomendasi bergantung pada alur lokasi.

Kenapa penting:

- Profile menjadi bukti bahwa data user dan media tidak memakai Firebase Storage.
- Cloudinary dipakai untuk foto profil dan foto community, sehingga kedua preset harus terbukti berjalan.

Target hasil:

- Uji edit profile.
- Uji upload foto profile dengan preset `fishingpoint_profile`.
- Setelah upload profile berhasil, kembali ke Community dan pastikan avatar post milik user ikut berubah.
- Pastikan statistik user tampil masuk akal: jumlah post, favorite, spot, dan aktivitas lain.
- Buka `Postingan Saya` dan pastikan tidak muncul error `FAILED_PRECONDITION`.
- Buka `Spot Favorit` dan pastikan data berasal dari collection `favorites`.
- Pilih `Keamanan > Reset Password via Email` dan pastikan email reset diterima.
- Ambil screenshot profile, edit profile, dan hasil foto profil.

### 4. Testing Skenario Nyata Recommendation dan Hourly Wave

Ini wajib untuk kebutuhan skripsi.

Kenapa penting:

- Kode sudah compile, tetapi sidang membutuhkan bukti pengujian.
- Penguji biasanya akan melihat apakah skor rekomendasi benar-benar masuk akal.

Target hasil:

- Uji cuaca cerah, hujan, angin kuat, warning BMKG, gelombang rendah, gelombang tinggi.
- Uji spot dekat dan jauh.
- Uji hasil rekomendasi pada beberapa kombinasi data.
- Uji Open-Meteo Marine pada koordinat pesisir dan non-pesisir untuk melihat apakah wave hourly tersedia.
- Bandingkan BMKG kategori gelombang dengan Open-Meteo hourly wave; jika berbeda, aplikasi harus tetap konservatif dengan warning BMKG.
- Simpan contoh perhitungan Base Score, Marine Score, Fish Activity Score, dan Safety Multiplier untuk BAB IV.

### 5. Dokumentasi BAB IV dan BAB V

Ini penting supaya kerja teknis Anda bisa dijelaskan secara akademik.

Target hasil:

- Jelaskan Haversine.
- Jelaskan BMKG migration.
- Jelaskan Fishing Activity Index.
- Jelaskan Open-Meteo Marine sebagai pelengkap hourly wave, bukan pengganti BMKG.
- Jelaskan standar satuan angin km/h dan konversi BMKG knot/OpenWeather m/s.
- Jelaskan cache policy.
- Siapkan tabel hasil uji.

### 6. Firestore Schema Final

Ini penting untuk menghindari perubahan schema di akhir.

Target hasil:

- Bekukan collection `users`, `community_posts`, `fishing_points`, `favorites`, `reviews`, `notifications`, `weather_cache`, dan `bmkg_cache`.
- Cocokkan field dengan model Java.

## Yang Sebaiknya Tidak Dikerjakan Dulu

Supaya project tidak melebar, beberapa hal sebaiknya ditahan dulu:

- Refactor besar seluruh UI.
- Mengganti semua nama class lama sekaligus.
- Menambah fitur baru yang tidak dibutuhkan proposal.
- Mengubah arsitektur utama yang sudah stabil.

## Rekomendasi Urutan Kerja

Urutan yang saya sarankan:

1. Uji Community/Post end-to-end. Status: kode sudah dikuatkan termasuk avatar author terbaru dan bookmark kompatibel schema, perlu upload Cloudinary nyata dan screenshot.
2. Ulang tap marker Maps dan buka detail spot. Status: alur kode sudah benar, detail favorite/share/navigasi sudah dibuat, perlu uji visual device.
3. Uji Profile dan upload foto. Status: implementasi, sinkron author community, Postingan Saya tanpa index, Spot Favorit, dan reset password fallback sudah ada; perlu validasi nyata di device.
4. Uji Recommendation Engine dan Open-Meteo hourly wave dengan skenario nyata. Status: formula final dan service sudah dibuat, perlu bukti log/screenshot data live.
5. Cocokkan Firestore schema final dengan data nyata setelah uji community/profile.
6. Lengkapi screenshot dan tabel black box testing.
7. Tulis BAB IV/BAB V berdasarkan hasil uji final.
8. Uji ulang spot yang sangat jauh seperti `Bobrok` dan `Lokasi Bagan Aku`; hasil list seharusnya tidak lagi tampak 4 bintang.
9. Uji ulang aktivitas ikan pada tiga waktu berbeda: malam hari, pagi menjelang sunrise, dan sore menjelang sunset; label seharusnya turun pada malam biasa dan naik pada feeding window.
10. Uji Map dengan memilih beberapa marker: garis polyline harus muncul dari posisi user ke spot, tombol `Navigasi` tetap membuka Google Maps, dan foto card harus memakai gambar asli jika `imageUrl` tersedia.
11. Uji tambah/edit marker Map: jenis spot `Bagan` dan `Rumpon` harus tersedia, form tidak lagi meminta URL foto, dan foto spot dapat diganti/hapus dari Detail Spot.
12. Uji Community Create Post: pilih foto potret dan landscape dari galeri, pastikan preview responsif dan feed tetap rapi setelah posting.

## Kesimpulan

Saat ini project sudah masuk fase penguatan, bukan fase pembangunan dasar.

Kalau ingin hasil akhir yang kuat untuk skripsi, fokus utama berikutnya adalah:

- memvalidasi BMKG relevan per lokasi dengan screenshot dan log,
- memastikan rekomendasi bisa dijelaskan melalui contoh hitung,
- menyelesaikan Community/Post dan Profile sebagai fitur pendukung proposal,
- dan menyiapkan bukti pengujian serta dokumentasi.

## Update Next Step 26 Juli 2026

Setelah audit ulang seluruh project dari struktur modul, repository, viewmodel, layout utama, roadmap, dan dokumen testing, keputusan terbaru adalah: **fitur inti aplikasi sudah cukup untuk masuk tahap finalisasi**. UI sudah dianggap final, data lama Firestore sudah dibersihkan, dan implementasi spot owner/private-public sudah mengurangi risiko benturan data lama.

### Status Terbaru

- Dashboard, Maps, Fishing Point, Recommendation Engine, Community, dan Profile sudah menjadi satu alur aplikasi yang utuh.
- Maps sudah memiliki akses edit/hapus owner-only, polyline user-ke-spot, navigasi Google Maps, dan card environment berdasarkan koordinat spot.
- Fishing Point sudah mendukung `PUBLIC` dan `PRIVATE`, owner info, foto detail spot, favorite, dan integrasi Profile.
- Dashboard dan Detail Spot memakai data live OpenWeather dan Open-Meteo Marine, sedangkan BMKG tetap menjadi forecast/warning resmi.
- Reset password sudah berjalan; pesan ke user perlu tetap mengingatkan untuk cek Inbox dan Spam.
- Build dan install debug terakhir berhasil pada device nyata sebelum audit ini.
- Audit runtime dan UX 26 Juli 2026 pada `TECNO_LJ8k` menunjukkan aplikasi dapat launch normal, berpindah tab tanpa crash, dan menampilkan data utama di Dashboard, Maps, Community, dan Profile.
- Satu risiko layout yang masih perlu diawasi adalah bagian bawah Dashboard `Kondisi Perairan Live` yang sangat dekat dengan bottom navigation pada viewport device nyata.
- Log masih menampilkan `GoogleApiManager: DEVELOPER_ERROR`, tetapi tidak memicu crash pada audit kali ini.
- Phase unit test mulai dinaikkan: folder `app/src/test` sekarang berisi 11 local unit test bermakna untuk Haversine, Recommendation Engine, dan helper BMKG/Tide response.
- Unit test template `ExampleUnitTest` dihapus agar folder test hanya berisi validasi yang relevan dengan project.
- `./gradlew.bat testDebugUnitTest` sudah berhasil setelah penambahan test.

### Langkah Berikutnya yang Direkomendasikan

1. **Black box testing final**

   Jalankan aplikasi dari awal: register/login, izin lokasi, dashboard, maps, tambah spot, edit spot, private/public spot, detail spot, favorite, community post, profile, reset password, dan logout. Catat hasil aktual, hasil harapan, status, dan screenshot.

2. **Perluasan unit test Phase berikutnya**

   Tambahkan unit test berikutnya secara bertahap untuk helper yang tidak membutuhkan Firebase live: visibility owner/private-public, formatter jarak, helper gambar spot, selector spot terdekat, dan parser/area resolver BMKG. Ini akan menaikkan kualitas testing tanpa membuat test bergantung pada internet atau device.

3. **Uji multi-akun Firestore**

   Buat minimal dua akun. Akun A membuat spot private dan public. Akun B hanya boleh melihat spot public, tidak boleh melihat tombol edit/hapus milik Akun A, dan tidak boleh mengubah data owner lain.

4. **Freeze schema dan Firestore rules**

   Bekukan collection `users`, `fishing_points`, `community_posts`, `favorites`, `reviews`, `notifications`, `weather_cache`, `bmkg_cache`, dan cache marine jika disimpan. Setelah itu buat rules produksi yang mengunci operasi berdasarkan `request.auth.uid`.

5. **Dokumentasi BAB IV/BAB V**

   Susun narasi implementasi: arsitektur MVVM, Haversine, BMKG polygon selector, OpenWeather, Open-Meteo Marine hourly wave, Recommendation Engine, Cloudinary, Firestore schema, dan hasil black box testing.

6. **Optimasi rilis dan ukuran project**

   Bersihkan cache lokal/build output, audit APK, pastikan `.gitignore` menahan folder besar, cek dependency yang tidak dipakai, dan siapkan release build/signing. Optimasi ini dilakukan setelah testing utama agar tidak mengganggu debugging.

7. **Tentukan nasib review/rating dan notification**

   Review/rating dan notification masih lebih lemah dibanding modul utama. Jika waktu skripsi terbatas, posisikan sebagai fitur pendukung atau saran pengembangan. Jika waktu cukup, matangkan review/rating lebih dahulu karena masih berhubungan dengan kualitas spot.

### Checklist Finalisasi Paling Dekat

- Uji Dashboard dengan GPS aktif, GPS mati, dan internet mati.
- Uji Map pada beberapa spot berbeda dan pastikan cuaca/perairan mengikuti koordinat spot.
- Uji tambah spot private/public dari akun owner.
- Uji akun lain tidak bisa edit/hapus spot owner berbeda.
- Uji favorite spot muncul di Profile.
- Uji create post dengan foto potret dan landscape.
- Uji like, comment, bookmark, share, dan delete post.
- Uji foto profile berubah dan avatar Community ikut sinkron.
- Uji reset password dan pastikan user diberi arahan cek Spam.
- Ambil screenshot final untuk Dashboard, Maps, Detail Spot, Add Spot, Community, Create Post, Profile, dan Reset Password.
- Uji lagi spacing Dashboard bagian `Kondisi Perairan Live` pada layar lebih kecil atau font scale lebih besar untuk memastikan tidak tertutup bottom navigation.
- Catat hasil audit runtime hari ini sebagai bukti bahwa focus berikutnya sudah bergeser dari crash hunting ke final testing dan dokumentasi.
- Tambah unit test berikutnya untuk `SpotVisibilityPolicy` atau helper visibility yang setara setelah refactor kecil tersedia.
- Tambah unit test formatter jarak/rekomendasi agar tampilan skor dan jarak konsisten di Dashboard, Map, Detail Spot, dan Spot List.

### Keputusan Penting

Untuk tahap berikutnya, hindari penambahan fitur besar baru. Project lebih membutuhkan bukti pengujian, keamanan data, dokumentasi, dan optimasi rilis daripada ekspansi fitur. Ini akan membuat aplikasi lebih siap untuk demo skripsi dan lebih mudah dipertanggungjawabkan saat sidang.
