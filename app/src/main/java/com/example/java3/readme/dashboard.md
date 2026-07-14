# ROLE

Anda adalah seorang Senior Android Engineer dengan pengalaman lebih dari 10 tahun dalam membangun aplikasi Android menggunakan Java, Firebase, Google Maps SDK, REST API, MVVM Architecture, Repository Pattern, dan Material Design 3.

Anda akan membantu saya mengembangkan aplikasi skripsi Android bernama **Fishing Point**.

JANGAN mengubah desain UI Dashboard yang sudah ada.

Fokus Anda adalah **mengaktifkan seluruh fitur Dashboard** dan menghubungkannya dengan backend.

---

# PROJECT INFORMATION

Nama Aplikasi :
Fishing Point

Bahasa :
Java

IDE :
Android Studio

Architecture :
MVVM

Backend :
Firebase

Database :
Cloud Firestore

Authentication :
Firebase Authentication

Storage :
Firebase Storage

Maps :
Google Maps SDK

Weather :
OpenWeather API

Sea Tide :
TideCheck API

Location :
Fused Location Provider

Target Android :
Android 8+

---

# TUJUAN

Dashboard harus menjadi pusat informasi utama aplikasi Fishing Point.

Semua informasi yang tampil harus berasal dari backend (Firestore atau API).

Jangan menggunakan dummy data.

Semua fitur harus berjalan secara realtime.

---

# YANG SUDAH ADA

- Login
- Register
- Firebase Authentication
- Dashboard UI
- Google Maps
- Bottom Navigation
- RecyclerView
- Fragment
- Splash Screen

Jangan menghapus ataupun mengubah desain UI.

---

# YANG HARUS DIKERJAKAN

## 1. Dashboard

Aktifkan seluruh komponen Dashboard.

Dashboard harus mengambil data secara realtime.

---

## 2. Greeting User

Mengambil data user dari Firestore

Collection

users

Field

uid
nama
email
photo
role

Menampilkan

Halo, Ferdy

atau

Halo, {Nama User}

---

## 3. Search Spot

Search harus bekerja.

Mencari berdasarkan

Nama Spot

Jenis Ikan

Daerah

Lokasi

Search menggunakan Firestore.

---

## 4. Quick Menu

Aktifkan seluruh menu.

Spot Terdekat

Weather

Pasang Surut

Favorite

Community

Notification

Setiap menu harus membuka halaman yang sesuai.

---

## 5. Weather Card

Gunakan

OpenWeather API

Mengambil lokasi pengguna menggunakan

Fused Location Provider

Mengirim Latitude Longitude ke API.

Tampilkan

Suhu

Feels Like

Humidity

Wind Speed

Wind Direction

Pressure

Visibility

Weather Description

Weather Icon

Sunrise

Sunset

Status Cuaca

Refresh otomatis.

Tangani kondisi

Tidak ada internet

API gagal

GPS mati

Loading

---

## 6. Tide Card

Gunakan

TideCheck API

Menggunakan koordinat lokasi pengguna.

Tampilkan

Status Pasang

Status Surut

High Tide

Low Tide

Tinggi Air

Jam Pasang Berikutnya

Jam Surut Berikutnya

Refresh otomatis.

Tangani semua error.

---

## 7. Spot Recommendation

Menggunakan data

Google Maps

GPS

Weather

Tide

Rating Spot

Review Spot

Hitung jarak menggunakan

Haversine Formula

Urutkan berdasarkan skor.

Tampilkan

Nama Spot

Foto

Rating

Jarak

Status Cuaca

Status Pasang

Persentase Rekomendasi

---

## 8. Nearby Spot

Ambil data dari

Firestore

Collection

fishing_points

Tampilkan

Foto

Nama Spot

Jenis Ikan

Latitude

Longitude

Rating

Distance

Recycler Horizontal.

---

## 9. Map Preview

Tampilkan mini Google Maps.

Marker berasal dari

Firestore

Klik Preview membuka halaman Map.

---

## 10. Community Preview

Ambil 3 posting terbaru.

Collection

community_posts

Tampilkan

Foto

Nama User

Caption

Tanggal

Jumlah Like

Jumlah Komentar

---

## 11. Favorite

Collection

favorites

Tampilkan jumlah Favorite user.

---

## 12. Notification

Collection

notifications

Tampilkan jumlah notifikasi yang belum dibaca.

---

## 13. Dashboard Statistics

Hitung

Jumlah Spot

Jumlah Favorite

Jumlah Review

Jumlah Posting

Jumlah Spot Terdekat

---

## 14. Loading State

Semua Card harus memiliki

Loading

Success

Error

Retry

Empty State

---

## 15. Repository

Pisahkan Repository

UserRepository

SpotRepository

WeatherRepository

TideRepository

CommunityRepository

FavoriteRepository

NotificationRepository

Jangan menaruh logic pada Activity maupun Fragment.

---

## 16. ViewModel

Gunakan MVVM.

Semua komunikasi Dashboard melalui ViewModel.

Gunakan LiveData.

---

## 17. Firestore

Gunakan Collection

users

fishing_points

reviews

favorites

community_posts

notifications

weather_cache

tide_cache

---

## 18. Firebase Storage

Foto Spot

Foto User

Foto Community

Harus berasal dari Storage.

---

## 19. Caching

Gunakan cache.

Dashboard tidak boleh request API terus menerus.

Gunakan cache selama 10 menit.

---

## 20. Error Handling

Tangani

Internet mati

GPS mati

API gagal

Firestore gagal

Permission ditolak

Data kosong

Timeout

Server Error

401

403

404

500

---

## 21. Performance

Dashboard harus

Ringan

Cepat

Tidak blocking UI

Gunakan Background Thread.

---

## 22. Code Quality

Gunakan

Clean Code

SOLID Principle

Repository Pattern

MVVM

Reusable Component

Dependency Injection jika diperlukan.

---

## OUTPUT YANG DIHARAPKAN

Saya ingin seluruh Dashboard berfungsi penuh.

Semua data berasal dari backend.

Semua API telah terintegrasi.

Semua fitur dapat digunakan.

Tidak ada dummy data.

Tidak ada TODO.

Tidak ada hardcode.

Tidak ada crash.

Jika menemukan bug, perbaiki secara otomatis.

Jika menemukan struktur project yang kurang baik, lakukan refactor tanpa mengubah UI.

Berikan penjelasan setiap file yang dibuat atau diubah.