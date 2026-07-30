# Fishing Point - Roadmap dan Progress Project Skripsi

Tanggal update: 26 Juli 2026

Dokumen ini dibuat sebagai catatan progres terbaru project Android **Fishing Point**. Fokusnya adalah menyelaraskan kondisi implementasi aplikasi saat ini dengan tujuan skripsi, tanpa mengubah kode aplikasi.

## Catatan Sumber

- Sumber lokal yang dibaca: `roadmap.md`, `dashboard.md`, `mapsme.md`, `comunity.md`, `profile.md`, struktur package Java, layout utama, repository, model, adapter, dan viewmodel yang tersedia di project.
- File proposal Word berhasil dibaca dari `app/src/main/java/com/example/java3/readme/Proposal Skripsi - Revisi - Ferdy kirim.docx`.
- Judul proposal: **Rancang Bangun Aplikasi Fishing Point Berbasis Android Menggunakan Metode Perhitungan Jarak Haversine di Desa Tanjung Anom**.
- Link ChatGPT share yang diberikan belum bisa dibaca langsung dari environment ini, sehingga isinya belum dijadikan kutipan langsung.
- Keputusan implementasi terbaru: project menggunakan **Cloud Firestore untuk database** dan **Cloudinary untuk penyimpanan media gambar**, bukan Firebase Storage.
- Keputusan migrasi maritim: **TideCheck diganti dengan API publik BMKG Peta Maritim** melalui endpoint perairan, contoh `https://peta-maritim.bmkg.go.id/public_api/perairan/F.09_Teluk%20Jakarta.json`.
- Catatan istilah: fitur lama “Tide/Pasang Surut” kini diarahkan menjadi **Kondisi Perairan BMKG** karena data BMKG perairan lebih berisi prakiraan cuaca maritim, gelombang, angin, dan peringatan dini.

## Update Implementasi 19 Juli 2026

- Project size sudah diaudit. Pembengkakan utama berasal dari cache Gradle/JDK/build output lokal, bukan source aplikasi.
- `.gitignore` diperbarui agar `.g`, `.gradle-local`, dan semua folder `build/` tidak ikut terbawa.
- Gradle diarahkan ke JBR 21 bawaan Android Studio dan auto-download toolchain dimatikan agar cache JDK tidak membesar berulang.
- File `assets/bmkg_regions.json` dibuat dari daftar endpoint BMKG dan berisi 232 region perairan.
- Cache API eksternal distandarkan: OpenWeather 10 menit, BMKG 30 menit, fallback cache maksimal 24 jam.
- BMKG sekarang memiliki model `BMKGForecast`, `BMKGResponse`, `BMKGRegion`, `BMKGCache`, dan bridge `BMKGRepository`.
- Dashboard menampilkan daftar forecast BMKG horizontal menggunakan `BMKGForecastAdapter` dan `item_bmkg_forecast.xml`.
- Recommendation Engine tahap awal memakai Fishing Activity Index, lalu difinalkan menjadi formula `Base Score x Safety Multiplier` agar lebih mudah dijelaskan secara akademik dan lebih konservatif untuk keselamatan.
- Validasi real device pada `TECNO_LJ8k` sudah berhasil: Dashboard memuat Weather/BMKG dari source aktif, Maps memuat 10 titik Firestore, dan spot card Maps menampilkan jarak Haversine serta skor rekomendasi.
- Build Java debug berhasil setelah implementasi BMKG forecast: `BUILD SUCCESSFUL`.
- Validasi ulang real device menunjukkan Dashboard mengambil lokasi user aktif melalui fused location, Weather memakai koordinat user, dan BMKG polygon resmi memilih area `F.08_Perairan Kep. Seribu` untuk koordinat device saat pengujian.
- Selector BMKG resmi dari endpoint polygon sudah memuat 232 wilayah perairan dan tidak lagi bergantung pada default `Teluk Jakarta` selama koordinat masuk ke polygon BMKG.
- Maps sudah dikunci konsepnya: cuaca dan BMKG pada marker memakai koordinat fishing point/marker, sedangkan jarak Haversine tetap dihitung dari posisi user ke marker.
- Community/Post mulai dikuatkan: Create Post memakai lokasi user saat posting dibuat, mengambil Weather/BMKG dari lokasi tersebut, upload media tetap memakai Cloudinary unsigned preset `fishing_post_upload`, feed dibatasi 50 posting realtime, dan delete post membersihkan like, favorite, serta komentar terkait sampai batas batch client.
- Layout Create Post diperbaiki agar teks cuaca dan kondisi perairan BMKG yang panjang tidak bertabrakan pada layar kecil.
- Finalisasi Community/Profile dilanjutkan: feed Community sekarang memperkaya data author dari collection `users`, sehingga foto profil dan nama terbaru dapat tampil pada posting lama maupun posting baru.
- Setelah user memperbarui nama atau foto profile, `ProfileRepository` ikut menyinkronkan field author pada `community_posts` milik user sampai batas 50 dokumen terbaru agar feed segera konsisten.
- Build dan install debug setelah finalisasi Community/Profile berhasil pada device `TECNO_LJ8k`.
- Perbaikan lanjutan dari screenshot user: `DetailSpotActivity` tidak lagi menampilkan pesan "fitur detail spot belum tersedia" untuk tombol favorit, share, dan navigasi. Aksi favorit spot, bagikan spot, dan navigasi Google Maps sudah dibuat fungsional.
- Map sekarang mengirim data spot lengkap ke halaman detail: id, nama, tipe, deskripsi, imageUrl, koordinat, rating, ulasan, dan jarak Haversine.
- Favorite posting Community dibuat kompatibel dengan schema lama/baru: `type = community_post`, `type = post`, dan `targetType = post`.
- Menu Profile `Postingan Saya` tidak lagi memakai query Firestore yang membutuhkan composite index; data diambil berdasarkan `userId` lalu diurutkan di client sehingga error `FAILED_PRECONDITION: query requires an index` dapat dihindari.
- Reset password Profile diperkuat agar memakai email dari FirebaseAuth atau fallback email dari profile Firestore.
- Build dan install debug setelah perbaikan Map/Community/Profile berhasil pada device `TECNO_LJ8k`.
- Recommendation Engine diperbarui ke formula final berbasis `Base Score x Safety Multiplier`: `0.20D + 0.20W + 0.25M + 0.15A + 0.10S + 0.10U`.
- Open-Meteo Marine API ditambahkan sebagai sumber **gelombang hourly** (`wave_height`, `wave_direction`, `wave_period`, `wind_wave_height`, `swell_wave_height`) untuk grafik/score gelombang per jam.
- BMKG tetap dipertahankan sebagai sumber resmi Indonesia untuk area perairan, warning, angin maritim, dan forecast harian/esok hari.
- OpenWeather tetap dipakai sebagai cuaca lokal live: suhu, kelembapan, tekanan udara, deskripsi cuaca, dan angin lokasi.
- Skala angin engine distandarkan ke `km/h`: OpenWeather `m/s` dikonversi ke `km/h`, BMKG `knot` dikonversi ke `km/h`, lalu engine memakai nilai risiko tertinggi.
- Marine score sekarang menggabungkan gelombang hourly Open-Meteo, warning BMKG, dan angin maritim/lokal. Warning BMKG dapat membatasi label maksimal menjadi `Perlu Waspada`.
- Dashboard dan Map kini meminta data Open-Meteo Marine sesuai koordinat aktif: Dashboard berdasarkan lokasi user, Map berdasarkan koordinat marker/spot.
- Build dan install debug setelah implementasi Open-Meteo Marine + formula engine baru berhasil pada device `TECNO_LJ8k`.

## Update Implementasi 22 Juli 2026

- Implementasi rekomendasi live dilanjutkan setelah proses sebelumnya sempat stuck.
- `RecommendationEngine` sudah menjadi pusat formula final dengan bobot: `0.20D + 0.20W + 0.25M + 0.15A + 0.10S + 0.10U`, lalu hasilnya dikalikan `Safety Multiplier`.
- Sumber data hari ini dipisahkan dengan jelas: OpenWeather untuk cuaca live, Open-Meteo Marine untuk gelombang/jalur laut hourly, dan BMKG untuk forecast resmi, area perairan, serta warning.
- Dashboard sudah menampilkan grafik gelombang per jam melalui custom `WaveHourlyChartView`, tanpa menambah dependency chart baru agar build tetap stabil.
- Dashboard menampilkan ringkasan live perairan: tinggi gelombang saat ini, maksimum 24 jam, suhu laut, kecepatan arus, dan arah arus.
- Detail Spot sekarang mengambil data live berdasarkan koordinat spot, bukan placeholder: cuaca, angin, kelembapan, aktivitas ikan, gelombang, dan skor rekomendasi.
- Spot list dan spot card memakai bintang dari hasil kalkulasi rekomendasi, bukan rating statis saja.
- BMKG forecast card tidak lagi memakai label `Tidak Direkomendasikan` sebagai badge forecast; BMKG ditampilkan sebagai `Prakiraan` atau `Waspada` agar tidak bercampur dengan skor rekomendasi engine.
- Artefak validasi device `window.xml`, `window2.xml`, dan `window3.xml` sudah dibersihkan dari root project.
- Verifikasi 22 Juli 2026: `:app:compileDebugJavaWithJavac` berhasil, `:app:installDebug` berhasil pada device `TECNO LJ8k`, dan launch aplikasi tidak menunjukkan `FATAL EXCEPTION` pada logcat.
- Perbaikan lanjutan engine 22 Juli 2026: skor rekomendasi sekarang memiliki distance cap berbasis Haversine, sehingga spot sangat jauh tidak bisa tampil 4-5 bintang hanya karena rating/cuaca bagus.
- Spot list sekarang meminta data Weather, BMKG, dan Open-Meteo Marine berdasarkan koordinat masing-masing spot ketika tersedia, sehingga bintang list lebih konsisten dengan Detail Spot.
- Tampilan bintang di Spot List, Dashboard spot terdekat, dan Detail Spot diseragamkan memakai bintang terisi dan kosong (`★★★★★` sampai `★☆☆☆☆`) agar pengguna tidak salah membaca jumlah bintang.
- Fish Activity Score diperbaiki menjadi model solunar-lite: menggabungkan sunrise/sunset window, fase bulan, tekanan udara, cuaca, pergerakan air, dan penalti jam malam. Perubahan ini membuat kondisi cerah pada tengah malam tidak otomatis tampil `Tinggi`.
- Map diperkuat dengan garis route internal berbasis polyline geodesic dari posisi user ke spot. Fitur navigasi Google Maps tetap dipertahankan; polyline menjadi fallback visual saat rute Google tidak tersedia di wilayah perairan.
- Foto spot pada card Map tetap memakai `imageUrl` asli dari Firestore/Cloudinary dengan transformasi thumbnail Cloudinary ringan jika tersedia. Jika foto belum ada, aplikasi memakai fallback gambar sesuai tipe spot.
- Dialog tambah/edit marker Map dibuat lebih ramah user: input URL foto dihapus, jenis spot ditambah `Bagan` dan `Rumpon`, sedangkan ganti/hapus foto dipindahkan ke halaman Detail Spot.
- Detail Spot sekarang memiliki kontrol ganti foto dari galeri dan hapus foto. Upload memakai Cloudinary unsigned preset yang sudah ada, lalu menyimpan URL ke field `imageUrl`; jika foto dihapus, spot kembali ke gambar default sesuai tipe.
- Community Create Post disederhanakan: form upload kini berisi foto, deskripsi/caption, jenis ikan opsional, dan lokasi/lingkungan otomatis. Field berat ikan dan umpan dihapus dari UI agar alur posting lebih ringan.
- Preview gambar Create Post dibuat responsive dengan rasio stabil `16:10` dan `centerCrop`, sehingga foto dari galeri tetap rapi saat dipilih, diupload, dan tampil di feed.
- Feed Community disederhanakan: caption tetap menjadi konten utama, sedangkan jenis ikan dan lokasi hanya tampil jika tersedia. Metadata cuaca/perairan tetap bisa disimpan di data post, tetapi tidak lagi memenuhi card feed.
- Gambar di feed komunitas juga dibuat responsive dengan rasio stabil `16:10` dan `centerCrop`, agar foto potret maupun landscape tetap proporsional saat tampil di postingan.

## Progress Keseluruhan

Estimasi progres keseluruhan project saat ini: **94%**.

Angka ini bukan angka final akademik, tetapi estimasi teknis berdasarkan kesesuaian antara proposal skripsi, dokumen target fitur, dan struktur implementasi yang sudah ada di project. Fokus proposal adalah Android, LBS, Google Maps API, metode Haversine, data cuaca/kondisi perairan, rekomendasi fishing point, dan media sharing komunitas.

| Bagian | Progress | Status Singkat |
| --- | ---: | --- |
| Authentication dan session | 92% | Login, register, splash, session, logout, verifikasi email, dan reset password sudah berjalan; perlu tabel uji kondisi gagal. |
| Arsitektur MVVM + Repository | 88% | Struktur model, repository, viewmodel, fragment/activity sudah kuat; tahap berikutnya cukup refactor kecil jika ada duplikasi nyata. |
| Cloud Firestore integration | 88% | Repository utama sudah memakai Firestore dan data lama sudah dibersihkan; schema/rules produksi tetap perlu dibekukan sebelum final. |
| Cloudinary media upload | 90% | Community, Profile, dan foto spot memakai Cloudinary unsigned preset; perlu uji upload berulang pada jaringan berbeda. |
| Dashboard / Home | 92% | Dashboard sudah memuat lokasi user, Weather live, BMKG polygon selector, Open-Meteo hourly wave chart, cache, rekomendasi, spot terdekat, dan forecast horizontal. |
| Google Maps dan LBS | 94% | Map, marker, clustering, Haversine, environment per marker, polyline, navigasi Google Maps, detail spot, ownership, visibility, dan favorite sudah terintegrasi. |
| Metode Haversine | 95% | Perhitungan jarak user-ke-spot sudah dipakai di Dashboard, Map, Detail, dan Spot List; local unit test Haversine sudah tersedia dan lolos. |
| Weather API | 91% | OpenWeather live dipakai untuk suhu, kelembapan, tekanan, cuaca, dan angin lokal; satuan angin distandarkan ke km/h. |
| Kondisi Perairan BMKG | 94% | BMKG tetap menjadi sumber resmi warning, area perairan, angin maritim, dan forecast harian/esok; dipadukan dengan Open-Meteo Marine hourly wave. |
| Open-Meteo Marine hourly wave | 90% | Service, model, repository, cache memori, Dashboard chart, Map, Detail Spot, dan engine sudah terhubung; perlu validasi lintas koordinat pesisir. |
| Recommendation Engine | 94% | Formula final `Base Score x Safety Multiplier` sudah diterapkan dengan Distance, Weather, Marine, Spot, Fish Activity, Preference, wind safety, current safety, dan hourly wave safety; unit test dasar engine sudah tersedia dan lolos. |
| Fishing Point CRUD/detail | 92% | Tambah/edit/hapus spot, private/public, owner info, detail live data, foto Cloudinary, favorite, share, navigasi, dan akses owner-only sudah berjalan. |
| Favorite spot/post | 90% | Favorite spot dan post sudah terhubung ke Profile, Detail, Map, dan Community; perlu uji end-to-end multi-akun. |
| Review dan rating spot | 58% | Model/repository tersedia, tetapi UI review/rating belum menjadi modul matang; bisa ditahan jika tidak wajib untuk demo. |
| Community / media sharing | 90% | Feed, create post, upload foto Cloudinary, auto lokasi/cuaca/perairan, like, comment, favorite/bookmark, share, delete cleanup, dan sinkron avatar profile sudah ada. |
| Profile | 93% | Profile modern, edit profile, upload Cloudinary, statistik, postingan saya, spot saya/favorit, reset password via email, dan sinkron author community sudah ada. |
| Notification | 45% | Repository/model tersedia, integrasi event dan UI belum menjadi fitur utama yang matang. |
| Error handling dan empty state | 72% | Cache API eksternal sudah punya fallback; perlu uji kondisi internet mati, GPS mati, API gagal, dan Firestore permission. |
| Testing skripsi | 76% | Testing plan, screenshot, build/install device, validasi real device, 11 local unit test, dan 1 instrumented smoke test sudah ada; tahap berikutnya black box final, multi-akun, dan bukti multi-skenario. |
| Dokumentasi BAB IV/BAB V | 62% | Roadmap, schema Firestore, formula rekomendasi, audit cache, testing plan, dan screenshot tersedia; perlu disusun menjadi narasi skripsi final. |

## Update Audit Runtime dan UX 26 Juli 2026

Audit runtime dilakukan pada device nyata `TECNO_LJ8k` setelah fitur inti dinyatakan masuk tahap finalisasi UI. Fokus audit kali ini adalah memastikan aplikasi dapat dibuka, berpindah menu utama, dan tidak mengalami force close pada alur awal.

Hasil audit:

- Device nyata `TECNO_LJ8k` terbaca melalui ADB dengan serial `150092556K005458`; emulator lama masih terdeteksi offline sehingga perintah ADB perlu memakai serial device agar tidak salah target.
- Aplikasi berhasil masuk dari `SplashActivity` ke `MainActivity` tanpa `FATAL EXCEPTION` atau ANR pada logcat terbaru.
- Dashboard berhasil tampil dengan data cuaca live, rekomendasi hari ini, safety score, fish activity, dan kartu kondisi perairan.
- Maps berhasil dibuka di device nyata, Google Maps aktif, marker Firestore tampil, tombol pencarian/filter, map type, tambah marker, dan lokasi pengguna tersedia.
- Community berhasil dibuka, feed tampil dengan post nyata, gambar posting, caption, jenis ikan/lokasi opsional, tombol like, comment, share, dan bookmark.
- Profile berhasil dibuka, data user, foto profile, statistik postingan/spot/favorit, dan menu profile tampil.
- Reset password sebelumnya sudah terbukti terkirim, tetapi user perlu diberi informasi bahwa email reset bisa masuk ke folder Spam.
- Build dan unit test dasar sebelumnya sudah berhasil; `LocationUtilsTest` untuk Haversine sudah menjadi bukti awal testing fungsi murni.
- Folder `app/src/test` mulai diisi test bermakna sesuai phase SQA: `LocationUtilsTest`, `RecommendationEngineTest`, dan `TideResponseTest`.
- Unit test lokal terbaru berjumlah 11 test: 5 test Haversine, 4 test Recommendation Engine, dan 2 test helper BMKG/Tide response.
- Test bawaan template `ExampleUnitTest` dihapus karena tidak memberi nilai validasi project.
- Dependency test-only `Robolectric` ditambahkan untuk menjalankan unit test logic yang masih menyentuh Android SDK class seperti `android.graphics.Color` pada `RecommendationEngine`.
- Verifikasi terbaru: `./gradlew.bat testDebugUnitTest` berhasil.

Catatan risiko dari audit:

- Dashboard bagian `Kondisi Perairan Live` masih terlihat terlalu dekat dengan bottom navigation pada viewport `1080x2436`; ini bukan crash, tetapi perlu dicatat sebagai risiko mikro-layout jika diuji pada device yang lebih kecil atau font scale lebih besar.
- Map sudah stabil secara tampilan awal, tetapi tetap menjadi area performa paling rawan karena marker, Google Maps, polyline, environment data, dan FAB aktif bersamaan.
- Community sudah menampilkan data nyata, tetapi masih perlu uji upload Cloudinary nyata dari galeri, state bookmark setelah reload, dan delete cleanup.

## Update Safety Engine 26 Juli 2026

Revisi safety dilakukan setelah validasi lapangan menunjukkan kondisi angin sekitar 16-20 km/jam terasa kurang bersahabat untuk aktivitas memancing/perahu kecil, sementara skor lama masih terlalu optimistis.

Yang diperbaiki:

- `RecommendationEngine` sekarang menghitung `Safety Multiplier` dari kombinasi OpenWeather live, angin maritim BMKG, gelombang Open-Meteo Marine, arus laut, cuaca buruk, dan warning BMKG.
- Satuan angin diseragamkan ke `km/h`: OpenWeather dari `m/s` dikonversi ke `km/h`, sedangkan `wind_speed_min` dan `wind_speed_max` BMKG dikonversi dari `knot` ke `km/h` sesuai dokumentasi field BMKG yang ada di project.
- Engine memakai nilai risiko angin tertinggi antara OpenWeather dan BMKG, sehingga kondisi laut tidak terlihat aman hanya karena angin lokal darat lebih rendah.
- Skala safety angin dibuat lebih konservatif untuk konteks perairan Indonesia dan perahu kecil: `0-8 km/h` aman, `>8-14 km/h` sepoi/cukup stabil, `>14-19 km/h` cukup aman tetapi mulai hati-hati, `>19-28 km/h` waspada, `>28-38 km/h` berisiko, `>38-49 km/h` tinggi risiko, dan `>49 km/h` tidak disarankan.
- Warning BMKG sekarang menjadi rem keselamatan: warning umum membatasi safety, warning tinggi/badai/petir lebih berat, dan warning ekstrem/sangat tinggi/bahaya menjadi kondisi risiko besar.
- Penalti kombinasi ditambahkan untuk kondisi yang secara lapangan lebih rawan: angin sedang-kuat + gelombang naik, angin + arus kencang, serta angin + hujan.
- Label dashboard untuk safety dibuat tidak terlalu longgar: `>=90 Aman`, `>=75 Cukup Aman`, `>=55 Waspada`, `>=40 Berisiko`, dan di bawah itu `Tidak aman`.

Validasi teknis:

- Unit test engine ditambah untuk skenario BMKG warning dan angin BMKG tinggi agar safety turun secara konservatif.
- `./gradlew.bat testDebugUnitTest` berhasil.
- `./gradlew.bat assembleDebug` berhasil.
- Install debug dan launch di device nyata `TECNO_LJ8k` berhasil tanpa `FATAL EXCEPTION` atau ANR pada alur dashboard awal.
- Pada kondisi uji nyata dengan OpenWeather sekitar `12,4 km/h`, BMKG menampilkan angin maritim `19-35 knot`, gelombang sekitar `0,5 m`, dan arus sekitar `1,1 m/s`; engine menurunkan hasil menjadi `Tidak Direkomendasikan` dengan safety rendah. Ini konsisten karena 35 knot setara sekitar 64,8 km/h dan masuk kategori tidak disarankan untuk perahu kecil.

Catatan finalisasi:

- Safety engine sekarang sengaja defensif. Untuk skripsi, ini lebih mudah dipertanggungjawabkan karena rekomendasi memancing tidak hanya mengejar peluang ikan, tetapi juga memprioritaskan keselamatan pengguna.
- Profile sudah tampil baik, tetapi menu bagian bawah perlu tetap diuji pada variasi layar/font scale karena berada dekat area bottom navigation.
- Log masih menampilkan `GoogleApiManager: DEVELOPER_ERROR`; saat audit ini tidak menyebabkan crash, tetapi perlu dicatat sebagai risiko konfigurasi Google service/OAuth jika fitur Google Sign-In dipakai.

Keputusan status setelah audit:

- UI utama tetap dianggap final; perubahan berikutnya sebaiknya hanya micro-fix berbasis bukti, bukan redesign besar.
- Project sudah layak masuk fase **black box testing final, multi-akun Firestore, freeze schema/rules, dokumentasi BAB IV/BAB V, dan optimasi rilis**.
- Modul review/rating dan notification tetap diposisikan sebagai fitur pendukung kecuali ada waktu tambahan untuk mematangkannya.

## Progress Berdasarkan Rumusan Masalah Proposal

| Rumusan Masalah Proposal | Progress | Keterangan |
| --- | ---: | --- |
| Merancang dan membangun aplikasi Android rekomendasi dan pemetaan fishing point berbasis LBS dengan Google Maps API | 82% | Struktur aplikasi, Google Maps, lokasi, marker, dan Firestore sudah ada. Validasi device sudah membuktikan marker dan spot card berjalan. |
| Menerapkan metode Haversine untuk menghitung jarak pengguna ke fishing point | 82% | Komponen jarak sudah selaras dengan kebutuhan proposal. Perlu dokumentasi rumus, contoh perhitungan, dan pembanding hasil. |
| Menampilkan informasi cuaca dan kondisi perairan untuk rekomendasi waktu memancing | 95% | TideCheck diganti BMKG Peta Maritim, lalu dilengkapi Open-Meteo Marine hourly wave. BMKG menguatkan warning/forecast, OpenWeather memberi cuaca live, Open-Meteo memberi grafik/score gelombang per jam. |
| Memberikan rekomendasi fishing point berdasarkan jarak terdekat dan rekomendasi sistem | 90% | RecommendationEngine sudah memakai formula final Base Score x Safety Multiplier dengan OpenWeather, Open-Meteo Marine, Haversine, rating spot, fish activity sederhana, preferensi pengguna, dan lapisan safety. |
| Menerapkan fitur media sharing pengalaman pengguna dan spot pemancingan | 88% | Community sudah memiliki feed, foto Cloudinary, caption, lokasi, auto Weather/BMKG, like, comment, favorite/bookmark, share, cleanup delete, dan sinkron foto profil terbaru. Perlu uji upload/post nyata. |

## Progress Berdasarkan Metode Waterfall Proposal

Proposal memakai tahapan pengembangan sistem bergaya Waterfall. Status saat ini dapat dipetakan seperti berikut.

| Tahap Waterfall | Progress | Bukti/Kondisi Saat Ini | Sisa Pekerjaan |
| --- | ---: | --- | --- |
| Requirement Analysis | 85% | Proposal, readme modul, dan target fitur sudah cukup jelas. | Bekukan perubahan scope: Firestore + Cloudinary, bukan Firebase Storage. |
| System Design | 75% | MVVM, repository, model, layout, dan collection utama sudah terbentuk. | Buat diagram final: use case, activity, sequence, ERD Firestore. |
| Coding / Implementation | 85% | Modul utama sudah diimplementasikan, termasuk BMKG forecast list, polygon selector, cache API eksternal, Open-Meteo hourly wave chart, Recommendation Engine final, dan validasi real device. | Validasi response nyata lanjutan, review, notification, dan state error. |
| Testing | 60% | Compile Java, install device, testing plan environment/rekomendasi, Dashboard/Maps, SpotList, dan Detail Spot sudah divalidasi awal di device. | Black box testing, uji CreatePost/Profile/Community, uji API BMKG nyata, uji GPS, screenshot hasil. |
| Maintenance / Finalisasi | 45% | Roadmap, audit ukuran, schema Firestore, formula rekomendasi, testing plan, dan screenshot awal sudah dibuat. | Bug fixing, polishing UI, dokumentasi BAB IV/BAB V, demo script. |

## Progress Berdasarkan Modul Utama Aplikasi

### Authentication - 90%

Fitur login, register, splash, session, dan logout sudah menjadi fondasi aplikasi. Modul ini relatif matang untuk kebutuhan demo, tetapi masih perlu pengujian kondisi gagal seperti password salah, email belum terdaftar, koneksi mati, dan session Firebase habis.

Sisa pekerjaan:

- Buat tabel pengujian login/register/logout.
- Pastikan semua pesan error mudah dipahami pengguna.
- Pastikan redirect splash benar untuk user login dan belum login.

### Dashboard / Home - 89%

Dashboard sudah memiliki struktur UI, ViewModel, repository pendukung, informasi cuaca, kondisi perairan BMKG, rekomendasi, spot terdekat, dan daftar forecast BMKG horizontal. Validasi ulang pada device real membuktikan Dashboard mengambil lokasi user aktif, Weather memakai koordinat user, dan BMKG memilih wilayah melalui polygon resmi.

Sisa pekerjaan:

- Pastikan seluruh card tetap memakai data asli pada beberapa skenario lokasi, bukan hanya satu titik pengujian.
- Ambil screenshot final Dashboard untuk BAB IV.
- Tambahkan retry dan empty state yang lebih konsisten.
- Pastikan rekomendasi spot tampil dengan skor yang bisa dijelaskan di skripsi.

### Maps / LBS / Fishing Point - 89%

Maps adalah fitur paling dekat dengan judul proposal. Project sudah memiliki MapFragment, MapViewModel, repository spot, model FishingPoint, clustering, dan detail/list spot. Alur data sudah diarahkan dengan benar: marker memakai koordinat spot untuk Weather/BMKG, sedangkan metode Haversine memakai jarak user ke spot. Halaman detail spot sekarang sudah memiliki aksi nyata untuk favorite, share, dan navigasi.

Sisa pekerjaan:

- Ulang validasi marker realtime dari Firestore untuk screenshot final.
- Pastikan koordinat Desa Tanjung Anom dan spot memancing akurat.
- Pastikan search/filter spot berjalan sesuai nama, lokasi, jenis ikan, dan tipe spot.
- Pastikan permission lokasi dan GPS mati tertangani.
- Uji detail spot pada device: favorite, share, navigasi, dan kembali ke peta.
- Pastikan status favorite spot muncul juga di Profile > Spot Favorit.

### Haversine - 82%

Metode Haversine sudah menjadi dasar perhitungan jarak antara posisi pengguna dan fishing point. Karena Haversine adalah kata kunci di judul skripsi, bagian ini perlu dibuat sangat jelas dalam dokumentasi.

Sisa pekerjaan:

- Buat file khusus `recommendation-formula.md` atau `haversine-formula.md`.
- Tulis rumus Haversine dalam format akademik.
- Tambahkan contoh koordinat user dan spot, lalu hitung jaraknya.
- Bandingkan hasil dengan jarak Google Maps sebagai validasi sederhana.

### Weather, BMKG, dan Open-Meteo Marine - 95%

Integrasi API cuaca dan kondisi perairan sudah memiliki service, repository, model, cache, polygon selector resmi, tampilan forecast, dan hourly wave. Proposal menyebut informasi cuaca dan kondisi perairan sebagai faktor pendukung rekomendasi waktu memancing. Karena TideCheck memiliki limit request kecil, sumber data maritim dimigrasikan ke API publik BMKG Peta Maritim dan dilengkapi Open-Meteo Marine untuk gelombang per jam.

Perubahan implementasi:

- `TideService` kini memanggil endpoint BMKG `public_api/perairan/{area}.json`.
- `TideRepository` kini mengambil data dari BMKG dan menyimpannya ke cache Firestore `bmkg_cache` melalui alias kompatibilitas `COL_TIDE_CACHE`.
- `TideResponse` dipertahankan sebagai nama kompatibilitas, tetapi sekarang membawa `List<BMKGForecast>`.
- `BMKGForecast` memuat `validFrom`, `validTo`, `timeDesc`, `weather`, `weatherDesc`, `waveCategory`, `waveDescription`, `windFrom`, `windTo`, `windSpeedMin`, `windSpeedMax`, `warning`, dan `stationRemark`.
- Parser BMKG dibuat fleksibel untuk membaca array forecast seperti `data`, `forecast`, `forecasts`, `items`, dan `details`.
- Dashboard menampilkan forecast BMKG secara dinamis memakai `RecyclerView` horizontal.
- Cache BMKG fresh selama 30 menit dan fallback cache maksimal 24 jam agar aplikasi tetap menampilkan data ketika API gagal.
- `assets/bmkg_regions.json` sudah tersedia dan berisi 232 endpoint BMKG dari file referensi pengguna.
- Polygon resmi BMKG sudah berhasil dimuat sebanyak 232 wilayah perairan dari endpoint `wilayah_perairan.json`.
- Area default tetap tersedia sebagai fallback, tetapi jalur utama sekarang memakai polygon resmi berdasarkan koordinat.
- `MarineWeatherService`, `MarineHourlyResponse`, dan `MarineHourlyRepository` ditambahkan untuk membaca Open-Meteo Marine hourly wave tanpa API key.
- Open-Meteo Marine mengambil `wave_height`, `wave_direction`, `wave_period`, `wind_wave_height`, `swell_wave_height`, `sea_surface_temperature`, `ocean_current_velocity`, dan `ocean_current_direction` selama 2 hari forecast.
- Cache Open-Meteo Marine dibuat di memori selama 30 menit agar tidak boros request tetapi tetap cukup live untuk Dashboard dan Map.
- Dashboard menampilkan grafik gelombang hourly, gelombang sekarang, maksimum 24 jam, suhu laut, arus, dan arah arus dari Open-Meteo, sementara BMKG tetap memberi status/warning/forecast.
- Map marker memakai Open-Meteo Marine berdasarkan koordinat spot, sehingga ringkasan gelombang marker mengikuti lokasi spot.

Sisa pekerjaan:

- Uji response nyata BMKG dan Open-Meteo Marine pada beberapa wilayah berbeda, bukan hanya lokasi device saat ini.
- Validasi apakah struktur JSON BMKG terbaru tetap terbaca penuh oleh parser fleksibel setelah beberapa endpoint dicoba.
- Simpan bukti log/screenshot area BMKG, wave hourly Open-Meteo, dan marker Maps.
- Validasi kode area BMKG yang paling cocok untuk Tanjung Anom dan titik laut sekitar spot.
- Dokumentasikan sumber API BMKG untuk BAB III/BAB IV.

### Recommendation Engine - 90%

RecommendationEngine sudah diperbarui memakai formula final yang lebih mudah dijelaskan secara akademik. Skor utama dihitung dari jarak Haversine, cuaca OpenWeather, marine score Open-Meteo, kualitas spot, fish activity sederhana, dan preferensi pengguna. Setelah itu hasil dikalikan Safety Multiplier dari cuaca buruk, angin live, arus laut, dan gelombang hourly. BMKG tetap tampil sebagai sumber resmi forecast/warning, tetapi tidak lagi menjadi sumber angka utama skor hari ini.

Formula final:

```text
Base Score = 0.20D + 0.20W + 0.25M + 0.15A + 0.10S + 0.10U
Final Score = Base Score x Safety Multiplier
```

Keterangan:

- `D`: Distance Score dari Haversine.
- `W`: Weather Score dari OpenWeather.
- `M`: Marine Score dari Open-Meteo hourly wave, stabilitas gelombang, dan arus laut.
- `S`: Spot Quality Score dari rating spot.
- `A`: Fish Activity Score sederhana dari waktu lokal, tekanan udara, kenyamanan cuaca, dan pergerakan air.
- `U`: User Preference Score.
- `Safety Multiplier`: nilai konservatif dari warning BMKG, angin, dan gelombang.

Sisa pekerjaan:

- Uji beberapa skenario nyata: dekat tetapi warning BMKG, jauh tetapi cuaca bagus, spot rating tinggi tetapi gelombang hourly tinggi.
- Tambahkan preferensi pengguna final untuk mengisi komponen `U` selain default 70.
- Tambahkan contoh perhitungan rekomendasi di dokumen BAB IV.

### Community / Media Sharing - 88%

Fitur community sudah mendukung tujuan proposal tentang media sharing pengalaman pemancing. Pengguna dapat membuat posting dengan foto, caption, informasi tangkapan, lokasi, cuaca, kondisi perairan BMKG, like, comment, favorite/bookmark, dan share. Feed sudah dibatasi agar tidak boros query, delete post sudah membersihkan like, favorite, serta komentar terkait, avatar author mengikuti foto profil terbaru dari collection `users`, dan pembacaan bookmark post kompatibel dengan schema lama/baru.

Sisa pekerjaan:

- Pastikan upload Cloudinary stabil untuk preset `fishing_post_upload`.
- Pastikan komentar realtime atau setidaknya refresh setelah submit.
- Uji Create Post nyata di device: pilih foto, upload, simpan Firestore, lalu muncul di feed.
- Uji ikon bookmark/simpan postingan setelah tap, reload, dan scroll feed.
- Tambahkan pagination lanjutan jika data posting melebihi kebutuhan limit 50 realtime.

### Profile - 89%

Profile sudah menjadi modul modern dan cukup fungsional: edit profile, foto Cloudinary, statistik, postingan saya, spot favorit, pengaturan, keamanan, dan tentang aplikasi. Update nama/foto profile juga disinkronkan ke author data pada posting community agar feed tidak menampilkan foto lama. Menu `Postingan Saya` sudah diubah agar tidak membutuhkan composite index Firestore, dan reset password memakai fallback email dari profile jika email Auth tidak tersedia.

Sisa pekerjaan:

- Uji upload Cloudinary preset `fishingpoint_profile` pada device.
- Uji statistik realtime sesuai data user.
- Pastikan setelah foto profile diganti, avatar di feed Community ikut berubah.
- Uji `Postingan Saya` setelah membuat post baru.
- Uji `Spot Favorit` setelah menyimpan spot dari Detail Spot.
- Uji reset password dan pastikan email reset masuk.
- Jika waktu cukup, ubah dialog list menjadi halaman/bottom sheet lebih rapi.

### Favorite, Review, Notification - 57%

Favorite sudah lebih maju dibanding review dan notification. Review/rating penting karena proposal menyebut rekomendasi berdasarkan jarak terdekat dan rating spot. Notification bagus sebagai fitur pendukung, tetapi bukan inti proposal.

Sisa pekerjaan:

- Pastikan favorite spot sinkron di Map, Detail, Dashboard, dan Profile.
- Integrasikan review/rating ke detail spot dan recommendation score.
- Notification dapat diposisikan sebagai fitur tambahan jika waktu terbatas.

### Testing dan Dokumentasi - 38%

Ini bagian yang paling perlu dikejar untuk kebutuhan skripsi. Kode sudah berhasil di-compile setelah implementasi BMKG, tetapi skripsi tetap membutuhkan bukti pengujian, screenshot, diagram, dan penjelasan alur.

Sisa pekerjaan:

- Buat testing plan black box.
- Buat screenshot seluruh fitur utama.
- Buat Firestore schema final.
- Buat diagram use case, activity, sequence, dan ERD/struktur koleksi.
- Buat dokumen formula Haversine dan rekomendasi.

## Ringkasan Tujuan Project

Fishing Point adalah aplikasi Android berbasis Java untuk membantu pemancing menemukan, menilai, menyimpan, dan membagikan lokasi memancing. Aplikasi diarahkan untuk mendukung skripsi dengan tema sistem informasi/rekomendasi spot memancing berbasis lokasi, cuaca, kondisi perairan, komunitas, dan data pengguna.

Target akhir aplikasi:

- Autentikasi pengguna.
- Dashboard informasi utama.
- Peta spot memancing berbasis Google Maps.
- Data spot dari Cloud Firestore.
- Rekomendasi spot berdasarkan lokasi, cuaca, kondisi perairan BMKG, rating, dan jarak.
- Community feed untuk berbagi hasil tangkapan.
- Favorite spot dan favorite posting.
- Review/rating spot.
- Profile pengguna dengan statistik memancing.
- Media gambar melalui Cloudinary.
- Dokumentasi teknis untuk kebutuhan BAB IV dan demonstrasi sidang.

## Stack dan Arsitektur Saat Ini

- Bahasa: Java.
- Platform: Android Studio / Android native.
- Arsitektur: MVVM + Repository Pattern.
- UI: XML Layout, ViewBinding, Material Design.
- Backend: Firebase Authentication + Cloud Firestore.
- Media: Cloudinary unsigned upload preset.
- Maps: Google Maps SDK, Maps Utils clustering.
- Location: Fused Location Provider.
- API eksternal: OpenWeather API dan BMKG Peta Maritim API.
- Networking: Retrofit untuk API cuaca/kondisi perairan, HTTP multipart untuk Cloudinary pada beberapa modul.

## Status Modul Saat Ini

### 1. Authentication

Status: berjalan sebagai fondasi aplikasi.

Yang sudah ada:

- LoginActivity.
- RegisterActivity.
- SplashActivity.
- AuthRepository.
- LoginViewModel, RegisterViewModel, SplashViewModel, AuthViewModel.
- SessionManager.
- Firebase Authentication.

Catatan lanjutan:

- Perlu final check flow logout, session expired, dan fallback ketika Firebase user null.
- Perlu dokumentasi skenario uji login/register untuk skripsi.

### 2. Dashboard / Home

Status: sebagian besar pondasi sudah ada, perlu audit data realtime dan empty/error state.

Yang sudah ada:

- HomeFragment.
- HomeViewModel.
- DashboardStatsRepository.
- WeatherRepository.
- TideRepository.
- SpotRepository / FishingRepository untuk sumber data spot.
- RecommendationEngine.
- Model DashboardStats, WeatherCache, TideCache, RecommendationResult.

Target yang perlu dipastikan:

- Greeting user dari Firestore.
- Weather card dari OpenWeather berdasarkan lokasi user.
- Kondisi perairan dari BMKG Peta Maritim.
- Rekomendasi spot berdasarkan skor.
- Preview spot terdekat.
- Statistik dashboard dari Firestore.
- Preview community terbaru.
- Loading, success, error, retry, dan empty state.

Prioritas berikutnya:

1. Pastikan seluruh card dashboard tidak memakai dummy data.
2. Tambahkan indikator error yang ramah pengguna untuk GPS mati, API gagal, dan Firestore gagal.
3. Dokumentasikan rumus rekomendasi untuk BAB IV.

### 3. Maps dan Fishing Point

Status: modul besar sudah aktif, tetapi perlu stabilisasi dan penyelarasan field Firestore.

Yang sudah ada:

- MapFragment.
- MapViewModel.
- FishingRepository.
- SpotRepository.
- FishingMarkerRenderer.
- FishingPoint, FishingPointWithRecommendation, FishingClusterItem.
- Dialog tambah marker.
- DetailSpotActivity.
- SpotListActivity.
- SpotListAdapter dan FishingPointAdapter.
- Google Maps SDK dan Maps Utils.

Target yang perlu dipastikan:

- Marker berasal dari collection `fishing_points`.
- Marker realtime menggunakan SnapshotListener.
- Tambah/edit/hapus spot sesuai role/owner jika dibutuhkan.
- Search dan filter spot.
- Favorite spot tersimpan di collection `favorites`.
- Detail spot menampilkan rating, review, cuaca, kondisi perairan, dan navigasi.
- Marker clustering berjalan stabil.
- Jarak memakai Haversine.

Prioritas berikutnya:

1. Audit struktur dokumen `fishing_points` agar konsisten dengan model `FishingPoint`.
2. Pastikan create marker tidak membuat data duplikat.
3. Pastikan permission lokasi dan GPS mati tidak menyebabkan crash.
4. Buat tabel field Firestore untuk dokumentasi skripsi.

### 4. Weather dan Tide

Status: repository dan service sudah tersedia.

Yang sudah ada:

- WeatherService.
- TideService.
- NetworkModule.
- WeatherRepository.
- TideRepository.
- WeatherResponse, TideResponse, TideStation.
- WeatherCache dan TideCache.

Target yang perlu dipastikan:

- Cache tidak request API terus menerus.
- Error handling API timeout, 401/403, 404, 500.
- UI tetap menampilkan data cache ketika API gagal.
- Data cuaca/pasang masuk ke rekomendasi spot.

Prioritas berikutnya:

1. Validasi API key dari `local.properties` dan hindari hardcode key sensitif.
2. Dokumentasikan sumber data API untuk BAB III/BAB IV.
3. Buat skenario uji API gagal dan GPS mati.

### 5. Recommendation Engine

Status: class sudah tersedia, perlu dokumentasi dan validasi formula.

Yang sudah ada:

- RecommendationEngine.
- RecommendationResult.
- FishingPointWithRecommendation.

Aspek rekomendasi yang perlu dijelaskan di skripsi:

- Jarak user ke spot.
- Kondisi cuaca.
- Kondisi perairan BMKG/aktivitas ikan.
- Rating/review spot.
- Skor akhir 0-100 persen.
- Label rekomendasi seperti Excellent, Good, Fair, Poor.

Prioritas berikutnya:

1. Bekukan formula final agar tidak berubah-ubah saat penulisan skripsi.
2. Tambahkan contoh perhitungan manual untuk BAB IV.
3. Cocokkan output rekomendasi dengan UI dashboard dan maps.

### 6. Community

Status: sudah cukup kuat sebagai MVP sosial pemancing, masih perlu beberapa peningkatan production-ready.

Yang sudah ada:

- CommunityFragment.
- CreatePostFragment.
- CommunityViewModel.
- CommunityRepository.
- PostAdapter.
- Post dan CommunityComment.
- Feed realtime dari Firestore.
- Create post dengan foto Cloudinary.
- Like/unlike.
- Favorite/bookmark post.
- Komentar dasar.
- Share intent.
- Detail post dialog.
- Delete post milik sendiri.

Catatan penting:

- Media community menggunakan Cloudinary preset `fishing_post_upload`.
- Komentar masih perlu dipastikan realtime jika target akhirnya mensyaratkan realtime comment penuh.
- Adapter masih perlu dipertimbangkan memakai DiffUtil untuk performa feed realtime.

Prioritas berikutnya:

1. Tambahkan/harden realtime komentar.
2. Tambahkan pagination feed agar Firestore read tidak membengkak.
3. Bersihkan data terkait saat delete post: comments, likes, favorites, dan media jika memakai backend cleanup.
4. Dokumentasikan struktur `community_posts`, `likes`, dan subcollection `comments`.

### 7. Profile

Status: sudah dikembangkan menjadi halaman profile modern dan fungsional.

Yang sudah ada:

- ProfileFragment.
- ProfileViewModel.
- ProfileRepository.
- ProfileUiModel.
- ProfileStatsUiModel.
- Header profile modern dengan avatar, nama, email, badge, dan bio.
- Edit profile: nama, nomor HP, alamat, bio.
- Upload foto profile ke Cloudinary preset `fishingpoint_profile`.
- Statistik memancing realtime: spot, post, like, komentar, favorite, join date.
- Menu spot favorit.
- Menu postingan saya.
- Pengaturan dasar.
- Keamanan reset password via email.
- Tentang aplikasi.

Catatan penting:

- Profile tidak memakai Firebase Storage.
- `api_secret` Cloudinary tidak boleh ditanam di aplikasi Android. Android client cukup memakai unsigned upload preset.

Prioritas berikutnya:

1. Jika ingin UI lebih lengkap, ubah dialog list menjadi bottom sheet/halaman detail khusus.
2. Tambahkan cache setting lokal untuk tema/notifikasi/bahasa jika diperlukan.
3. Tambahkan flow ganti password yang meminta password lama jika login email-password.

### 8. Favorite, Review, Notification

Status: repository/model sudah ada, perlu finalisasi integrasi penuh ke semua UI.

Yang sudah ada:

- FavoriteRepository.
- ReviewRepository.
- NotificationRepository.
- Favorite, Review, Notification.

Target yang perlu dipastikan:

- Favorite spot tersinkron di Map, Detail Spot, Dashboard, dan Profile.
- Review/rating spot tampil di Detail Spot dan memengaruhi rekomendasi.
- Notification muncul untuk event penting, misalnya post disukai/dikomentari.

Prioritas berikutnya:

1. Pastikan semua repository ini dipakai oleh UI terkait.
2. Tambahkan dokumentasi collection `reviews`, `favorites`, dan `notifications`.
3. Tambahkan pengujian CRUD review/favorite.

## Struktur Firestore yang Direkomendasikan

### users

Field utama:

- `uid`
- `name` / `fullName`
- `email`
- `phone` / `phoneNumber`
- `address`
- `bio`
- `photoUrl`
- `role`
- `joinDate`
- `createdAt`
- `updatedAt`
- `profileCompleted`

### fishing_points

Field utama:

- `id` atau document id.
- `name`
- `latitude`
- `longitude`
- `type`
- `fishType`
- `area`
- `locationName`
- `description`
- `rating`
- `reviewCount`
- `imageUrl`
- `userId` / `createdBy`
- `createdAt`
- `updatedAt`

### community_posts

Field utama:

- `userId`
- `userName`
- `userProfilePic`
- `imageUrl`
- `cloudinaryPublicId`
- `caption`
- `locationName`
- `latitude`
- `longitude`
- `fishType`
- `fishWeight`
- `bait`
- `weatherCondition`
- `tideStatus`
- `timestamp`
- `likesCount`
- `commentsCount`

Subcollection:

- `comments`

### likes

Field utama:

- `userId`
- `postId`
- `timestamp`

Document id direkomendasikan:

- `{userId}_{postId}` agar tidak double like.

### favorites

Field utama:

- `id`
- `userId`
- `pointId`
- `postId`
- `type`
- `createdAt`

Catatan:

- `type = community_post` untuk favorite posting.
- Selain itu dianggap favorite spot.

### reviews

Field utama:

- `userId`
- `pointId`
- `rating`
- `comment`
- `createdAt`
- `updatedAt`

### notifications

Field utama:

- `userId`
- `title`
- `message`
- `type`
- `isRead`
- `createdAt`
- `targetId`

### weather_cache dan bmkg_cache

Field utama:

- `id`
- `data`
- `source`
- `area` khusus `bmkg_cache`
- `updatedAt`
- `expiresAt`
- `dataSizeBytes`

Catatan: nama `tide_cache` masih ada sebagai alias kompatibilitas di kode lama, tetapi arah schema final adalah `bmkg_cache` karena sumber data maritim sudah memakai BMKG Peta Maritim.

## Roadmap Lanjutan

### Phase 1 - Stabilitas Data dan Build

Target: aplikasi bisa dibuild dan semua fitur utama tidak crash.

Checklist:

- Jalankan `compileDebugJavaWithJavac` dan `assembleDebug` di Android Studio.
- Pastikan Cloudinary preset `fishing_post_upload` dan `fishingpoint_profile` aktif sebagai unsigned preset.
- Pastikan Firestore rules mengizinkan operasi sesuai kebutuhan user login.
- Pastikan field Firestore konsisten dengan model Java.
- Hilangkan data dummy dari UI utama.

Output:

- APK debug berjalan.
- Daftar bug prioritas tinggi.
- Screenshot semua halaman utama.

### Phase 2 - Finalisasi Maps dan Spot

Target: Maps menjadi fitur inti yang stabil untuk demo skripsi.

Checklist:

- Marker Firestore tampil realtime.
- Tambah spot berjalan.
- Detail spot lengkap.
- Search/filter spot berjalan.
- Favorite spot berjalan.
- Navigasi/share lokasi berjalan.
- Jarak user ke spot akurat.
- Error permission dan GPS mati tertangani.

Output:

- Dokumentasi flow Maps.
- Screenshot marker, detail, search, favorite, dan navigasi.

### Phase 3 - Finalisasi Dashboard dan Rekomendasi

Target: Dashboard menunjukkan alasan ilmiah aplikasi berguna bagi pemancing.

Checklist:

- Weather card real API.
- Kondisi perairan BMKG real API.
- Rekomendasi spot memakai formula final.
- Spot terdekat tampil.
- Statistik dashboard real Firestore.
- Community preview tampil.
- Loading/error/empty state rapi.

Output:

- Formula rekomendasi siap masuk BAB IV.
- Contoh perhitungan rekomendasi.
- Screenshot dashboard final.

### Phase 4 - Finalisasi Community dan Profile

Target: fitur sosial dan identitas user siap dipakai untuk demo.

Checklist Community:

- Feed realtime stabil.
- Create post dengan Cloudinary stabil.
- Like tidak double.
- Komentar berjalan.
- Favorite post berjalan.
- Delete post aman.
- Share post berjalan.

Checklist Profile:

- Edit profile tersimpan Firestore.
- Upload foto profile Cloudinary berjalan.
- Statistik realtime sesuai data.
- Spot favorit dan postingan saya tampil.
- Reset password berjalan.

Output:

- Dokumentasi struktur community dan profile.
- Screenshot feed, create post, detail, profile, edit profile.

### Phase 5 - Testing Skripsi

Target: bukti pengujian siap masuk BAB IV.

Jenis pengujian:

- Black box testing.
- Uji login/register/logout.
- Uji CRUD spot.
- Uji Maps marker dan lokasi.
- Uji weather/tide API.
- Uji rekomendasi spot.
- Uji community post, like, comment, share.
- Uji favorite spot/post.
- Uji profile edit dan upload foto.
- Uji kondisi gagal: internet mati, GPS mati, permission ditolak, API gagal.

Output:

- Tabel pengujian.
- Hasil aktual vs hasil yang diharapkan.
- Catatan bug dan perbaikan.

### Phase 6 - Dokumentasi BAB IV dan BAB V

Target: aplikasi dan dokumen skripsi saling cocok.

Checklist BAB IV:

- Deskripsi implementasi sistem.
- Arsitektur MVVM.
- Struktur Firestore.
- Alur data aplikasi.
- Penjelasan API cuaca dan BMKG Peta Maritim.
- Penjelasan rumus rekomendasi.
- Screenshot setiap fitur.
- Hasil pengujian black box.

Checklist BAB V:

- Kesimpulan.
- Keterbatasan sistem.
- Saran pengembangan.

## Dokumen Tambahan yang Sebaiknya Dibuat

Disarankan membuat file markdown berikut setelah roadmap ini:

- `firestore-schema.md`: struktur collection dan field final.
- `testing-plan.md`: tabel uji black box.
- `recommendation-formula.md`: rumus skor rekomendasi spot.
- `demo-script-sidang.md`: urutan demo aplikasi saat sidang.
- `bab-iv-implementation-notes.md`: catatan teknis untuk penulisan BAB IV.

## Risiko Project Saat Ini

### Risiko Teknis

- Field Firestore belum tentu konsisten antara dokumen lama dan model Java saat ini.
- Beberapa target dokumen lama masih menyebut Firebase Storage, sedangkan implementasi saat ini memakai Cloudinary.
- API eksternal bisa gagal karena key, limit, jaringan, atau response berubah.
- Feed community dan maps bisa mahal jika query realtime belum dipagination/dibatasi.

### Risiko Skripsi

- Jika formula rekomendasi tidak didokumentasikan, fitur rekomendasi sulit dipertanggungjawabkan saat sidang.
- Jika screenshot tidak sesuai fitur final, BAB IV bisa tidak sinkron dengan aplikasi.
- Jika pengujian hanya dilakukan manual tanpa tabel, bukti validasi sistem kurang kuat.

## Keputusan Implementasi yang Perlu Dicatat

1. Firebase digunakan untuk Authentication dan Cloud Firestore.
2. Cloudinary digunakan untuk media gambar, yaitu foto profile dan foto posting community.
3. Aplikasi mempertahankan arsitektur MVVM + Repository Pattern.
4. Data utama aplikasi harus berasal dari Firestore atau API, bukan dummy data.
5. Dashboard, Maps, Community, dan Profile adalah modul utama untuk demo skripsi.

## Prioritas Paling Dekat

Urutan kerja yang disarankan:

1. Buat `firestore-schema.md` final agar semua modul memakai field yang sama.
2. Audit Maps dan Fishing Point karena ini kemungkinan fitur utama skripsi.
3. Bekukan formula RecommendationEngine dan dokumentasikan.
4. Lengkapi dashboard agar semua card real data.
5. Finalisasi community dengan pagination dan realtime comment jika waktu cukup.
6. Buat testing plan dan screenshot untuk BAB IV.
7. Jalankan demo end-to-end dari login sampai rekomendasi spot dan community post.

## Definisi Selesai

Project dapat dianggap siap demo skripsi jika:

- User bisa login/register/logout.
- User bisa melihat dashboard dengan data nyata.
- User bisa melihat peta dan marker spot dari Firestore.
- User bisa membuat atau melihat detail spot memancing.
- User bisa melihat rekomendasi spot dengan alasan/skor.
- User bisa melihat cuaca dan kondisi perairan BMKG.
- User bisa menyimpan favorite spot.
- User bisa membuat posting community dengan foto.
- User bisa like, comment, share, dan bookmark post.
- User bisa melihat dan mengedit profile.
- Aplikasi tidak crash pada kondisi umum.
- Dokumentasi BAB IV memiliki screenshot dan tabel pengujian yang sesuai aplikasi final.

## Audit Ulang Project 26 Juli 2026

Audit ini dilakukan setelah UI dinyatakan final dan data lama Firestore sudah dibersihkan. Fokus audit bukan menambah fitur baru, tetapi menilai apakah aplikasi sudah cukup kuat untuk masuk tahap optimalisasi, testing, dan dokumentasi skripsi.

### Kesimpulan Audit

Project sudah berada pada fase **finalisasi**, bukan lagi fase pembangunan fitur inti. Modul yang menjadi inti skripsi sudah tersedia dan saling terhubung: Dashboard berbasis lokasi user, Maps/LBS, Haversine, data cuaca/perairan, Recommendation Engine, Fishing Point, Community, Profile, Firestore, dan Cloudinary.

Kondisi project saat ini cukup untuk masuk ke tahap berikut:

- Black box testing final.
- Validasi multi-skenario pada device nyata.
- Freeze schema Firestore dan security rules.
- Dokumentasi BAB IV/BAB V.
- Optimasi rilis: app size, ProGuard/R8, signing, dan pembersihan artefak lokal.

### Modul yang Sudah Kuat

- Dashboard sudah memakai data lokasi user, OpenWeather live, BMKG polygon selector, Open-Meteo Marine hourly wave, grafik gelombang, rekomendasi, safety, aktivitas ikan, dan spot terdekat.
- Maps sudah memakai marker Firestore, clustering, polyline user-ke-spot, tombol navigasi Google Maps, filter tipe spot, card environment berbasis koordinat spot, dan akses edit/hapus hanya untuk owner.
- Fishing Point sudah mendukung private/public spot, owner info, foto default per tipe, foto Cloudinary dari Detail Spot, favorite, share, dan navigasi.
- Recommendation Engine sudah memakai formula final `Base Score x Safety Multiplier` dengan sumber live untuk cuaca dan gelombang hourly.
- Community sudah mendukung create post lebih sederhana, upload foto, caption, jenis ikan/lokasi opsional, like, comment, favorite/bookmark, share, delete cleanup, dan sinkron avatar author dari Profile.
- Profile sudah mendukung edit profile, foto Cloudinary, statistik, postingan saya, spot saya/favorit, reset password via email, dan informasi reset yang mengingatkan user untuk cek Spam.

### Hal yang Masih Perlu Ditangani

- **Testing multi-akun**: pastikan private spot tidak terlihat oleh akun lain, public spot terlihat, dan edit/hapus hanya muncul untuk owner.
- **Review/rating spot**: model dan repository sudah ada, tetapi fitur ini belum sematang modul lain. Jika waktu terbatas, jadikan fitur pendukung, bukan fokus demo utama.
- **Notification**: repository/model tersedia, tetapi event notifikasi belum menjadi fitur final. Aman ditunda jika tidak masuk kebutuhan utama proposal.
- **Firestore rules**: perlu dibuat aturan produksi untuk membatasi edit/delete spot, post, profile, dan favorite berdasarkan `request.auth.uid`.
- **Query dan pagination**: feed Community sudah dibatasi, tetapi untuk rilis lebih serius perlu pagination/load more agar tidak boros Firestore.
- **State gagal**: perlu uji internet mati, GPS mati, permission lokasi ditolak, API BMKG/OpenWeather/Open-Meteo gagal, Cloudinary gagal, dan Firestore permission denied.
- **Dokumentasi perhitungan**: Haversine, formula rekomendasi, safety multiplier, fish activity, dan alasan penggunaan BMKG + Open-Meteo perlu dijelaskan dalam BAB IV.
- **Keamanan kredensial**: pastikan API secret Cloudinary tidak ditanam di APK, tidak dicatat di dokumen final publik, dan Android hanya memakai unsigned preset yang dibatasi.
- **Release build**: sebelum sidang/rilis, jalankan audit ukuran APK, aktifkan shrinker bila aman, cek signing config, dan hapus artefak build/cache lokal yang tidak diperlukan.

### Rekomendasi Keputusan Scope

Untuk menjaga project tetap stabil, jangan menambah fitur besar baru sebelum testing final. Fitur yang boleh masuk hanya bug fix kecil, security rules, dokumentasi, dan optimasi performa yang terbukti perlu.

Jika harus memilih prioritas, urutannya adalah:

1. Black box testing end-to-end pada device nyata.
2. Firestore schema dan rules final.
3. Dokumentasi BAB IV/BAB V dengan screenshot fitur final.
4. Optimasi release build dan ukuran project.
5. Review/rating atau notification hanya jika semua prioritas utama sudah selesai.
