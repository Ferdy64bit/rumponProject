# Draft Penambahan Isi Skripsi Berdasarkan Implementasi Aplikasi

Dokumen ini merangkum bagian-bagian skripsi yang sudah dapat diisi dari hasil implementasi aplikasi Fishing Point sejauh ini. Isinya disusun agar selaras dengan judul penelitian: **Rancang Bangun Aplikasi Fishing Point Berbasis Android Menggunakan Metode Perhitungan Jarak Haversine di Desa Tanjung Anom**.

## 1. BAB I - PENDAHULUAN

### 1.1 Latar Belakang

Masyarakat pesisir dan pemancing membutuhkan informasi yang cepat, akurat, dan mudah diakses untuk menentukan lokasi memancing yang potensial. Selama ini, pencarian titik memancing sering dilakukan secara manual berdasarkan pengalaman, informasi lisan, atau perkiraan kondisi lapangan. Cara tersebut memiliki keterbatasan karena tidak selalu mempertimbangkan jarak lokasi pengguna, kondisi cuaca, situasi perairan, dan tingkat keamanan memancing pada saat tertentu.

Perkembangan perangkat mobile Android memungkinkan proses pencarian informasi lokasi dilakukan secara lebih praktis melalui aplikasi berbasis lokasi. Dengan memanfaatkan GPS, peta digital, dan perhitungan jarak Haversine, sistem dapat membantu pengguna menemukan titik pancing terdekat secara lebih terstruktur. Selain itu, integrasi data cuaca dan kondisi perairan dari sumber eksternal seperti OpenWeather, BMKG, dan Open-Meteo Marine dapat meningkatkan kualitas informasi yang diberikan kepada pengguna.

Aplikasi Fishing Point yang dibangun pada penelitian ini tidak hanya menampilkan titik pancing, tetapi juga mendukung rekomendasi spot, informasi cuaca dan gelombang, navigasi peta, komunitas berbagi tangkapan, profile pengguna, serta pengelolaan spot publik dan private. Dengan demikian, aplikasi ini diharapkan mampu menjadi solusi yang lebih informatif dan relevan bagi pengguna di Desa Tanjung Anom.

### 1.2 Rumusan Masalah

1. Bagaimana membangun aplikasi Android yang dapat menampilkan titik pancing terdekat berdasarkan lokasi pengguna?
2. Bagaimana menerapkan metode Haversine untuk menghitung jarak antara lokasi pengguna dan titik pancing?
3. Bagaimana mengintegrasikan data cuaca dan kondisi perairan ke dalam sistem rekomendasi memancing?
4. Bagaimana merancang aplikasi agar mendukung pengelolaan spot, komunitas, dan profile pengguna secara terpusat?
5. Bagaimana memastikan aplikasi dapat berjalan stabil pada device nyata dan sesuai kebutuhan pengguna skripsi?

### 1.3 Tujuan Penelitian

1. Membangun aplikasi Fishing Point berbasis Android yang mampu menampilkan titik pancing secara informatif.
2. Menerapkan metode Haversine untuk menghitung jarak lokasi pengguna ke titik pancing.
3. Mengintegrasikan data cuaca, BMKG, dan kondisi perairan sebagai pendukung rekomendasi.
4. Menyediakan fitur komunitas, favorite, profile, dan pengelolaan spot publik/private.
5. Menghasilkan aplikasi yang dapat diuji secara langsung pada device nyata dan layak dijadikan bahan skripsi.

### 1.4 Batasan Masalah

1. Aplikasi dikembangkan untuk platform Android.
2. Sistem menggunakan Firestore sebagai basis data utama.
3. Media gambar disimpan melalui Cloudinary, bukan Firebase Storage.
4. Informasi lokasi, cuaca, dan perairan memanfaatkan layanan pihak ketiga yang sudah dipilih dalam implementasi.
5. Fokus penelitian berada pada pencarian spot, rekomendasi, navigasi, komunitas, dan pengelolaan profile.

## 2. BAB II - TINJAUAN PUSTAKA

Bagian ini dapat diperkuat dengan materi berikut:

1. Android dan arsitektur aplikasi mobile.
2. Google Maps API dan Location Based Service.
3. Metode Haversine untuk perhitungan jarak dua koordinat.
4. Firebase Firestore sebagai database cloud.
5. Cloudinary sebagai layanan penyimpanan media.
6. OpenWeather dan BMKG sebagai sumber data cuaca dan maritim.
7. Open-Meteo Marine untuk data gelombang per jam.
8. Recommendation engine berbasis skor dan faktor keselamatan.
9. Community sharing dan user-generated content pada aplikasi mobile.
10. Pengujian perangkat lunak dengan black box dan unit test.

## 3. BAB III - METODOLOGI PENELITIAN DAN PERANCANGAN SISTEM

### 3.1 Metode Pengembangan

Sistem dapat dijelaskan menggunakan pendekatan iteratif berbasis implementasi bertahap. Struktur project yang digunakan sudah mengarah ke pola MVVM dan repository, sehingga cocok dijelaskan sebagai arsitektur yang memisahkan tampilan, logika data, dan proses bisnis.

### 3.2 Arsitektur Sistem

Komponen utama aplikasi yang sudah dibangun:

1. Dashboard untuk menampilkan lokasi user, cuaca live, kondisi perairan, safety score, dan rekomendasi harian.
2. Maps untuk menampilkan marker spot, jarak, polyline navigasi, detail spot, dan spot terdekat.
3. Fishing Point untuk tambah, edit, hapus, private/public spot, serta ownership.
4. Community untuk posting tangkapan, like, komentar, favorite, share, dan bookmark.
5. Profile untuk edit data user, upload foto, statistik, spot saya, spot favorit, dan postingan saya.
6. Recommendation Engine untuk menghitung skor memancing dari jarak, cuaca, gelombang, spot quality, fish activity, preference, dan safety.

### 3.3 Perancangan Data

Data yang sudah dapat dimasukkan ke skripsi:

1. Firestore collection untuk user, fishing points, posts, favorites, comments, dan cache.
2. Field spot mencakup nama, jenis, lokasi, koordinat, visibility, owner, foto, dan rating.
3. Field post mencakup caption, foto, ikan, lokasi opsional, like, komentar, dan author.
4. Field profile mencakup nama, email, bio, photoUrl, dan statistik terkait.

### 3.4 Perhitungan Jarak Haversine

Metode Haversine digunakan untuk menghitung jarak antara koordinat pengguna dengan koordinat spot memancing. Hasil jarak ini dipakai untuk:

1. Menentukan spot terdekat.
2. Menampilkan label jarak pada card dan marker.
3. Menjadi komponen utama dalam recommendation engine.

### 3.5 Recommendation Engine

Formula final yang sudah dipakai dapat dijelaskan sebagai skor utama yang dipengaruhi oleh beberapa faktor:

1. Distance Score.
2. Weather Score.
3. Marine Score.
4. Spot Quality Score.
5. Fish Activity Score.
6. User Preference Score.
7. Safety Multiplier sebagai penyesuai kondisi cuaca dan perairan.

Skor ini cocok dijelaskan sebagai pendekatan hybrid antara kedekatan lokasi, data lingkungan live, dan preferensi pengguna.

## 4. BAB IV - HASIL DAN PEMBAHASAN

Bagian ini sudah bisa diisi dari implementasi aplikasi yang ada:

### 4.1 Hasil Implementasi Dashboard

Jelaskan bahwa dashboard menampilkan:

1. Lokasi pengguna saat ini.
2. Cuaca live.
3. Kondisi perairan.
4. Gelombang per jam.
5. Safety indicator.
6. Rekomendasi memancing hari ini.
7. Spot terdekat berdasarkan lokasi.

### 4.2 Hasil Implementasi Maps

Jelaskan bahwa maps sudah memiliki:

1. Marker spot pancing.
2. Filter spot.
3. Jarak ke lokasi pengguna.
4. Navigasi Google Maps.
5. Polyline rute ke spot.
6. Detail spot dengan data live.
7. Akses edit/hapus berdasarkan owner.

### 4.3 Hasil Implementasi Community

Jelaskan bahwa komunitas mendukung:

1. Upload foto dari device.
2. Caption dan deskripsi singkat.
3. Like, unlike, komentar, favorite, share, delete.
4. Sinkron profile pengguna sebagai author.

### 4.4 Hasil Implementasi Profile

Jelaskan bahwa profile mendukung:

1. Edit data pengguna.
2. Ganti foto profile.
3. Reset password via email.
4. Daftar postingan saya.
5. Daftar spot saya.
6. Daftar spot favorit.
7. Statistik penggunaan.

### 4.5 Hasil Pengujian

Cantumkan hasil yang sudah tersedia:

1. Build debug berhasil.
2. Install dan launch pada device nyata berhasil.
3. Unit test dasar berjalan.
4. Tidak ada crash pada alur utama yang sudah diuji.

## 5. BAB V - PENUTUP

### 5.1 Kesimpulan

Tuliskan bahwa aplikasi Fishing Point berhasil dibangun sebagai aplikasi Android berbasis lokasi yang membantu pengguna mencari spot memancing berdasarkan jarak, kondisi cuaca, kondisi perairan, dan rekomendasi sistem. Integrasi community, profile, cloud media, dan Firestore membuat aplikasi lebih lengkap dan mendekati kebutuhan nyata pengguna.

### 5.2 Saran

1. Penambahan notifikasi kondisi perairan ekstrem.
2. Penguatan pengujian multi-akun.
3. Penyempurnaan dokumentasi BAB IV dan BAB V dengan screenshot final.
4. Pengembangan analitik rekomendasi yang lebih adaptif jika dibutuhkan pada versi berikutnya.

## 6. Catatan Implementasi yang Sudah Bisa Dijadikan Bukti Skripsi

1. Dashboard live data cuaca dan perairan.
2. Maps dengan marker spot, navigasi, dan polyline.
3. Rekomendasi memancing berbasis skor.
4. Community post dengan media upload.
5. Profile dengan statistik dan reset password.
6. Spot private/public dengan ownership.
7. Pengujian pada device nyata.

## 7. Rekomendasi Langkah Lanjut Penulisan

1. Ubah isi BAB I sesuai rumusan masalah dan tujuan di atas.
2. Tambahkan teori pada BAB II berdasarkan fitur yang sudah diimplementasikan.
3. Buat diagram alur dan arsitektur pada BAB III.
4. Ambil screenshot final untuk BAB IV.
5. Susun kesimpulan dan saran pada BAB V.