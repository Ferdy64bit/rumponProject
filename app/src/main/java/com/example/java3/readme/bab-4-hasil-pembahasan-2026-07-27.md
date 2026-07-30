# BAB IV
# HASIL DAN PEMBAHASAN

## 4.1 Hasil Implementasi Sistem

Pada bab ini dijelaskan hasil implementasi aplikasi Fishing Point berbasis Android yang telah dibangun berdasarkan rancangan pada bab sebelumnya. Aplikasi ini dikembangkan untuk membantu pengguna, khususnya pemancing di wilayah Desa Tanjung Anom dan sekitarnya, dalam mencari lokasi memancing berdasarkan jarak, kondisi cuaca, kondisi perairan, serta rekomendasi sistem.

Aplikasi Fishing Point dibangun menggunakan platform Android dengan bahasa pemrograman Java. Sistem menggunakan Firebase Firestore sebagai basis data utama untuk menyimpan data pengguna, titik pancing, postingan komunitas, komentar, favorite, dan informasi pendukung lainnya. Untuk penyimpanan media gambar, aplikasi menggunakan layanan Cloudinary agar upload foto profile, foto spot, dan foto postingan komunitas dapat dilakukan tanpa menggunakan Firebase Storage.

Secara umum, aplikasi yang telah dibangun memiliki beberapa modul utama, yaitu authentication, dashboard, maps, detail spot, community, profile, dan recommendation engine. Setiap modul saling terhubung sehingga membentuk alur aplikasi yang utuh. Pengguna dapat login, melihat rekomendasi memancing, membuka peta, menambah titik pancing, melihat detail spot, menyimpan spot favorit, membuat postingan komunitas, serta mengelola profile pribadi.

## 4.2 Implementasi Authentication dan Session

Fitur authentication digunakan sebagai pintu masuk utama ke dalam aplikasi. Pada tahap implementasi, sistem telah menyediakan fitur login, register, session pengguna, logout, dan reset password melalui email. Setelah pengguna berhasil login, aplikasi menyimpan status session sehingga pengguna dapat langsung masuk ke halaman utama tanpa login ulang selama session masih aktif.

Fitur reset password telah terintegrasi dengan Firebase Authentication. Ketika pengguna memilih reset password, sistem mengirimkan email reset ke alamat email yang terdaftar. Berdasarkan hasil pengujian, email reset password berhasil dikirim, namun terdapat kemungkinan email masuk ke folder Spam. Oleh karena itu, aplikasi telah diberi informasi kepada pengguna agar memeriksa folder Inbox maupun Spam.

## 4.3 Implementasi Dashboard

Dashboard merupakan halaman utama aplikasi yang menampilkan informasi penting secara ringkas kepada pengguna. Dashboard dirancang untuk membantu pengguna mengetahui kondisi saat ini sebelum menentukan lokasi memancing.

Informasi yang ditampilkan pada dashboard meliputi lokasi pengguna, cuaca live, kondisi perairan, grafik gelombang per jam, safety score, fish activity, rekomendasi memancing hari ini, serta daftar spot terdekat. Data cuaca live diperoleh dari OpenWeather, sedangkan informasi kondisi perairan dan prakiraan maritim menggunakan data BMKG serta Open-Meteo Marine untuk kebutuhan gelombang per jam.

Pada implementasi terbaru, dashboard sudah menampilkan rekomendasi memancing berdasarkan kombinasi data lokasi, cuaca, gelombang, aktivitas ikan, dan faktor keselamatan. Sistem juga menampilkan spot terdekat berdasarkan posisi pengguna saat ini. Perhitungan jarak antara pengguna dan spot dilakukan menggunakan metode Haversine.

Dashboard dibuat responsif agar informasi utama tetap terbaca pada berbagai ukuran layar perangkat Android. Beberapa perbaikan UI/UX telah dilakukan, seperti merapikan card gelombang, mencegah teks terpotong, menyesuaikan tinggi menu, dan memastikan area konten tidak bertabrakan dengan bottom navigation.

## 4.4 Implementasi Google Maps dan Location Based Service

Modul Maps digunakan untuk menampilkan titik pancing dalam bentuk marker pada Google Maps. Pengguna dapat melihat lokasi spot, mencari spot, memfilter jenis spot, membuka detail spot, serta menggunakan navigasi ke lokasi tujuan.

Aplikasi menggunakan Location Based Service untuk mendapatkan posisi pengguna. Posisi tersebut digunakan untuk menghitung jarak ke spot pancing dan menentukan spot terdekat. Metode Haversine digunakan karena mampu menghitung jarak antara dua titik koordinat berdasarkan garis lengkung permukaan bumi.

Selain navigasi Google Maps, aplikasi juga menyediakan fitur polyline dari posisi pengguna ke lokasi spot. Fitur ini ditambahkan karena pada beberapa wilayah perairan, rute Google Maps tidak selalu tersedia. Dengan polyline, pengguna tetap dapat melihat garis arah langsung dari lokasi saat ini menuju spot tujuan.

Pada modul Maps juga diterapkan aturan ownership. Tombol edit dan hapus hanya ditampilkan kepada pengguna yang merupakan pemilik spot. Dengan demikian, pengguna lain tidak dapat mengubah atau menghapus spot yang bukan miliknya. Spot juga memiliki status akses, yaitu PUBLIC dan PRIVATE. Spot PUBLIC dapat dilihat oleh pengguna lain, sedangkan spot PRIVATE hanya dapat dilihat oleh pemiliknya.

## 4.5 Implementasi Detail Spot

Halaman detail spot menampilkan informasi lebih lengkap mengenai titik pancing yang dipilih. Informasi yang ditampilkan mencakup nama spot, jenis spot, foto spot, lokasi, pembuat spot, status favorite, navigasi, share, dan informasi kondisi lingkungan berdasarkan koordinat spot tersebut.

Pada implementasi saat ini, data cuaca dan kondisi perairan pada detail spot tidak lagi menggunakan placeholder. Sistem mengambil data berdasarkan koordinat spot sehingga informasi yang ditampilkan lebih relevan dengan lokasi spot, bukan hanya berdasarkan lokasi pengguna. Hal ini penting karena kondisi cuaca dan gelombang dapat berbeda antara lokasi pengguna dan lokasi spot pancing.

Fitur foto spot juga telah ditingkatkan. Saat menambahkan titik pancing, pengguna tidak diwajibkan memasukkan URL foto karena hal tersebut kurang ramah untuk pengguna awam. Foto default akan digunakan terlebih dahulu. Jika pengguna membuka detail spot dan merupakan pemilik spot, pengguna dapat mengganti atau menghapus foto spot. Apabila foto dihapus, sistem akan mengembalikan tampilan ke foto default sesuai jenis spot.

## 4.6 Implementasi Metode Haversine

Metode Haversine digunakan untuk menghitung jarak antara dua titik koordinat, yaitu koordinat pengguna dan koordinat spot pancing. Hasil perhitungan jarak digunakan pada beberapa bagian aplikasi, seperti dashboard, maps, detail spot, dan daftar spot terdekat.

Secara konsep, metode Haversine menghitung jarak berdasarkan latitude dan longitude dengan mempertimbangkan bentuk bumi yang bulat. Metode ini cocok digunakan pada aplikasi berbasis lokasi karena dapat memberikan estimasi jarak yang cukup baik antara dua titik di permukaan bumi.

Pada aplikasi Fishing Point, hasil Haversine digunakan untuk menentukan Distance Score dalam recommendation engine. Semakin dekat spot dengan pengguna, maka nilai jarak akan semakin baik. Akan tetapi, jarak bukan satu-satunya faktor penentu rekomendasi, karena sistem juga mempertimbangkan cuaca, gelombang, keamanan, kualitas spot, dan aktivitas ikan.

## 4.7 Implementasi Recommendation Engine

Recommendation Engine merupakan bagian penting dari aplikasi karena digunakan untuk memberikan rekomendasi memancing kepada pengguna. Sistem tidak hanya memilih spot berdasarkan jarak, tetapi juga mempertimbangkan kondisi lingkungan yang sedang terjadi.

Formula rekomendasi yang digunakan menggabungkan beberapa komponen utama, yaitu Distance Score, Weather Score, Marine Score, Spot Quality Score, Fish Activity Score, dan User Preference Score. Setelah skor dasar dihitung, nilai tersebut disesuaikan menggunakan Safety Multiplier agar rekomendasi tetap memperhatikan faktor keselamatan.

Komponen yang digunakan dalam recommendation engine adalah sebagai berikut:

1. Distance Score, yaitu nilai berdasarkan jarak pengguna ke spot.
2. Weather Score, yaitu nilai berdasarkan kondisi cuaca live seperti suhu, kelembapan, angin, dan kondisi cuaca.
3. Marine Score, yaitu nilai berdasarkan kondisi gelombang dan data perairan.
4. Spot Quality Score, yaitu nilai kualitas spot berdasarkan data spot.
5. Fish Activity Score, yaitu estimasi aktivitas ikan berdasarkan kondisi lingkungan.
6. User Preference Score, yaitu nilai preferensi pengguna.
7. Safety Multiplier, yaitu pengali keselamatan berdasarkan angin, gelombang, cuaca buruk, dan warning maritim.

BMKG tetap digunakan sebagai sumber resmi untuk prakiraan dan warning maritim. Namun, untuk perhitungan live hari ini, aplikasi lebih menekankan data cuaca live dan gelombang per jam agar rekomendasi lebih responsif terhadap kondisi lapangan.

## 4.8 Implementasi Community

Modul Community digunakan sebagai media berbagi pengalaman dan hasil tangkapan antar pengguna. Pengguna dapat membuat postingan dengan foto, menulis deskripsi, menambahkan jenis ikan secara opsional, serta menambahkan lokasi secara opsional.

Pada tahap pengembangan, beberapa field yang dianggap terlalu berlebihan seperti berat ikan dan umpan telah disederhanakan agar proses upload lebih mudah digunakan. Hal ini dilakukan agar fitur community tetap ringan, responsif, dan tidak membebani pengguna awam.

Fitur yang sudah berjalan pada community meliputi upload foto melalui Cloudinary, menampilkan feed postingan, like dan unlike, komentar, favorite atau bookmark, share, dan hapus postingan oleh pemiliknya. Tampilan gambar postingan juga telah diperbaiki agar tidak terpotong dan tetap responsif pada ukuran layar berbeda.

## 4.9 Implementasi Profile

Modul Profile digunakan untuk menampilkan dan mengelola informasi pengguna. Fitur yang telah diterapkan meliputi edit profile, ganti foto profile, statistik pengguna, daftar postingan saya, daftar spot saya, daftar spot favorit, reset password, dan logout.

Foto profile disimpan menggunakan Cloudinary dan disinkronkan ke tampilan community sehingga author postingan dapat menampilkan foto terbaru pengguna. Bagian profile juga terintegrasi dengan data Firestore untuk menampilkan jumlah postingan, jumlah spot, dan jumlah favorite.

Pada tahap finalisasi, beberapa perbaikan dilakukan pada menu profile, terutama pada dialog Spot Saya, Spot Favorit, dan Postingan Saya. Perbaikan dilakukan untuk mengatasi teks yang tidak beraturan akibat masalah encoding, serta memastikan status PUBLIC dan PRIVATE ditampilkan dalam bentuk bahasa Indonesia, yaitu Publik dan Pribadi.

## 4.10 Implementasi Pengelolaan Spot Public dan Private

Aplikasi mendukung dua jenis akses spot, yaitu PUBLIC dan PRIVATE. Spot PUBLIC dapat dilihat oleh pengguna lain, sedangkan spot PRIVATE hanya dapat dilihat oleh pemilik spot. Fitur ini penting karena tidak semua pengguna ingin membagikan lokasi pancing pribadi kepada publik.

Pada implementasi Maps, pengguna dapat memilih akses spot saat menambahkan atau mengedit titik pancing. Sistem telah diperbaiki agar pilihan Publik pada UI benar-benar disimpan sebagai PUBLIC di Firestore. Sebelumnya terdapat masalah karena sistem hanya mengenali teks PUBLIC, sedangkan UI menampilkan label Publik. Perbaikan dilakukan dengan menambahkan pengenalan terhadap variasi PUBLIC, PUBLIK, dan UMUM.

Selain itu, akses edit dan hapus spot dibatasi hanya untuk owner. Hal ini menjaga keamanan data agar pengguna tidak dapat mengubah spot milik orang lain.

## 4.11 Implementasi Penyimpanan Data dan Media

Aplikasi menggunakan Firestore sebagai database utama. Data yang disimpan meliputi data pengguna, titik pancing, postingan komunitas, komentar, favorite, dan data pendukung lainnya. Firestore dipilih karena mendukung sinkronisasi data secara realtime dan cocok untuk aplikasi mobile berbasis cloud.

Untuk media gambar, aplikasi menggunakan Cloudinary. Cloudinary digunakan untuk upload foto profile, foto postingan komunitas, dan foto spot. Penggunaan Cloudinary dipilih karena project tidak menggunakan Firebase Storage. Dengan pendekatan ini, aplikasi tetap dapat mendukung fitur upload media tanpa menambah kompleksitas penyimpanan di Firebase Storage.

## 4.12 Hasil Pengujian Aplikasi

Pengujian dilakukan untuk memastikan aplikasi dapat berjalan dengan baik pada perangkat nyata. Berdasarkan pengujian terakhir, aplikasi berhasil dibangun menggunakan perintah assembleDebug dan berhasil dipasang pada device nyata TECNO LJ8k. Aplikasi juga berhasil dijalankan dari SplashActivity menuju MainActivity tanpa force close.

Pengujian runtime menunjukkan bahwa dashboard, maps, community, dan profile dapat dibuka. Data utama seperti cuaca live, rekomendasi, spot, postingan komunitas, dan profile tampil sesuai alur aplikasi. Selain itu, logcat tidak menunjukkan FATAL EXCEPTION atau ANR pada proses launch dan navigasi awal.

Unit test dasar juga telah dijalankan untuk beberapa bagian penting, seperti perhitungan Haversine, recommendation engine, dan helper response data. Pengujian ini menjadi bukti awal bahwa sebagian logika utama aplikasi dapat diuji secara terpisah dari tampilan.

Tabel ringkas hasil pengujian awal:

| No | Modul | Skenario Pengujian | Hasil yang Diharapkan | Status |
| --- | --- | --- | --- | --- |
| 1 | Authentication | Login dan session pengguna | Pengguna masuk ke halaman utama | Berhasil |
| 2 | Dashboard | Menampilkan cuaca dan rekomendasi | Data utama tampil pada dashboard | Berhasil |
| 3 | Maps | Menampilkan marker spot | Marker tampil pada Google Maps | Berhasil |
| 4 | Maps | Menampilkan polyline ke spot | Garis arah dari user ke spot tampil | Berhasil |
| 5 | Detail Spot | Membuka informasi spot | Detail spot tampil dengan data lingkungan | Berhasil |
| 6 | Community | Menampilkan feed postingan | Postingan tampil dengan gambar dan caption | Berhasil |
| 7 | Profile | Menampilkan data pengguna | Data profile, statistik, dan menu tampil | Berhasil |
| 8 | Reset Password | Mengirim email reset | Email reset terkirim ke user | Berhasil |
| 9 | Unit Test | Menjalankan test dasar | Test berjalan tanpa error | Berhasil |
| 10 | Runtime | Launch aplikasi di device nyata | Tidak terjadi force close | Berhasil |

## 4.13 Pembahasan

Berdasarkan hasil implementasi dan pengujian, aplikasi Fishing Point telah memenuhi kebutuhan utama penelitian. Aplikasi mampu menampilkan spot pancing berdasarkan lokasi pengguna, menghitung jarak menggunakan metode Haversine, menampilkan kondisi cuaca dan perairan, serta memberikan rekomendasi memancing dengan mempertimbangkan faktor keselamatan.

Fitur Maps menjadi salah satu bagian utama karena berhubungan langsung dengan Location Based Service dan metode Haversine. Pengguna dapat melihat spot pada peta, mengetahui jarak, membuka detail, menyimpan favorite, serta menggunakan navigasi. Penambahan polyline juga membuat aplikasi tetap berguna pada area perairan ketika rute Google Maps tidak tersedia.

Recommendation engine menjadi pengembangan dari konsep awal pencarian spot berdasarkan jarak. Sistem tidak hanya menghitung jarak terdekat, tetapi juga menggabungkan data cuaca live, gelombang per jam, aktivitas ikan, dan safety score. Dengan pendekatan ini, rekomendasi yang diberikan menjadi lebih relevan dengan kondisi lapangan.

Fitur community dan profile melengkapi aplikasi agar tidak hanya menjadi alat pencari lokasi, tetapi juga media berbagi informasi antar pengguna. Pengguna dapat membagikan hasil tangkapan, menyimpan spot favorit, mengelola profile, serta membedakan spot pribadi dan spot publik.

Meskipun aplikasi sudah berada pada tahap finalisasi, masih terdapat beberapa hal yang dapat diperkuat pada tahap berikutnya, seperti pengujian black box secara lengkap, pengujian multi-akun Firestore, dokumentasi screenshot final, dan penyusunan hasil pengujian ke dalam format skripsi. Selain itu, fitur notification dan review/rating spot masih dapat dikembangkan lebih lanjut apabila dibutuhkan pada versi berikutnya.

## 4.14 Kesimpulan Sementara BAB IV

Dari hasil implementasi, aplikasi Fishing Point telah berhasil dibangun sesuai dengan tujuan penelitian. Aplikasi dapat membantu pengguna mencari titik pancing, menghitung jarak menggunakan metode Haversine, melihat kondisi cuaca dan perairan, memperoleh rekomendasi memancing, menggunakan peta dan navigasi, serta berinteraksi melalui fitur komunitas. Hasil pengujian awal pada device nyata menunjukkan aplikasi dapat berjalan tanpa crash pada alur utama yang telah diuji.