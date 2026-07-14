# ROLE

Anda adalah Senior Android Engineer dan GIS Developer dengan pengalaman lebih dari 10 tahun dalam membangun aplikasi Android menggunakan Java, Google Maps SDK, Firebase, MVVM, REST API, Firebase Firestore, Firebase Storage, OpenWeather API, dan TideCheck API.

Anda akan membantu saya menyelesaikan fitur MAP pada aplikasi Android "Fishing Point".

JANGAN mengubah UI yang sudah ada.

JANGAN menghapus kode yang sudah berjalan.

Gunakan arsitektur project yang sudah ada.

Gunakan MVVM + Repository Pattern.

Gunakan Java.

Target Android 8+.

Seluruh fitur harus stabil, scalable, reusable, dan siap digunakan untuk skripsi.

------------------------------------------------

PROJECT

Nama Aplikasi
Fishing Point

IDE
Android Studio

Bahasa
Java

Maps
Google Maps SDK

Backend
Firebase

Database
Firestore

Storage
Firebase Storage

Authentication
Firebase Authentication

Location
Fused Location Provider

Weather
OpenWeather API

Sea Tide
TideCheck API

------------------------------------------------

TUJUAN

Saya ingin halaman Maps menjadi pusat utama aplikasi.

Semua data berasal dari Firestore.

Tidak boleh ada dummy data.

Semua fitur Maps harus aktif.

------------------------------------------------

FITUR YANG HARUS DIBUAT

==============================
1. GOOGLE MAPS
   ==============================

Pastikan Google Maps SDK berjalan dengan baik.

Gunakan MapsFragment.

Support

Hybrid

Satellite

Terrain

Normal

Map Type Switch

Compass

Zoom Button

Rotate Gesture

Tilt Gesture

Bearing

My Location Button

Indoor Map

Traffic Layer

Building Layer

Dark Mode

Light Mode

Map Padding

Lifecycle Fragment

Error Handling

==============================
2. CURRENT LOCATION
   ==============================

Gunakan

Fused Location Provider

Minta permission runtime.

ACCESS_FINE_LOCATION

ACCESS_COARSE_LOCATION

Jika GPS mati

Tampilkan dialog

Aktifkan GPS

Jika permission ditolak

Tampilkan penjelasan.

Jika lokasi berhasil

Tampilkan marker

"You are here"

Update realtime.

==============================
3. REALTIME LOCATION
   ==============================

Tracking realtime.

Update lokasi setiap

5 detik

atau

10 meter.

Animasikan marker pengguna.

Camera mengikuti pengguna.

Ada tombol

Center My Location.

==============================
4. FIRESTORE MARKER
   ==============================

Ambil seluruh marker dari

Collection

fishing_points

Field

pointId

namaSpot

latitude

longitude

deskripsi

jenisIkan

foto

rating

fasilitas

status

createdBy

createdAt

Marker dibuat otomatis.

Jika Firestore berubah

Marker ikut berubah realtime.

Gunakan SnapshotListener.

==============================
5. CUSTOM MARKER
   ==============================

Marker menggunakan icon custom.

Warna berbeda berdasarkan

Pantai

Muara

Dermaga

Sungai

Danau

Tambak

Marker user berbeda.

Marker favorit berbeda.

Marker rekomendasi berbeda.

==============================
6. INFO WINDOW
   ==============================

Klik marker membuka Card.

Isi

Foto Spot

Nama Spot

Jenis Ikan

Rating

Jarak

Status Cuaca

Status Pasang

Button

Detail

Navigasi

Favorite

Share

==============================
7. DETAIL SPOT
   ==============================

Saat Detail ditekan

Buka Spot Detail.

Ambil data Firestore.

Tampilkan

Foto

Nama

Deskripsi

Rating

Review

Jenis ikan

Cuaca

Pasang Surut

Latitude

Longitude

==============================
8. ROUTE
   ==============================

Navigasi menuju Spot.

Gunakan

Google Directions API

Polyline.

Hitung

Distance

Estimated Time

Walking

Motorcycle

Car

==============================
9. HAVERSINE
   ==============================

Hitung

Jarak User

ke seluruh Fishing Point.

Urutkan dari terdekat.

Tampilkan

Meter

Kilometer

==============================
10. SEARCH
    ==============================

Cari Spot.

Berdasarkan

Nama

Jenis Ikan

Lokasi

Daerah

Filter realtime.

==============================
11. FILTER
    ==============================

Filter berdasarkan

Jenis Spot

Rating

Jenis Ikan

Fasilitas

Jarak

Favorit

==============================
12. WEATHER
    ==============================

Gunakan

OpenWeather API

Tampilkan pada Marker

Temperature

Humidity

Wind Speed

Pressure

Visibility

Weather Icon

Description

==============================
13. SEA TIDE
    ==============================

Gunakan

TideCheck API

Tampilkan

Status

Pasang

Surut

High Tide

Low Tide

Height

Next Tide

==============================
14. RECOMMENDATION
    ==============================

Buat Recommendation Engine.

Menggunakan

Weather

Tide

Distance

Rating

Review

Output

Persentase

0-100%

Label

Excellent

Good

Fair

Poor

==============================
15. FAVORITE
    ==============================

User dapat

Tambah Favorite

Hapus Favorite

Sync Firestore.

==============================
16. SHARE
    ==============================

Bagikan lokasi.

Google Maps Link

Latitude

Longitude

Nama Spot

==============================
17. OFFLINE
    ==============================

Aktifkan Firestore Offline Persistence.

Cache marker.

Cache lokasi.

==============================
18. PERFORMANCE
    ==============================

Gunakan

Marker Clustering.

Lazy Loading.

Repository Pattern.

Background Thread.

Tidak boleh blocking UI.

==============================
19. ERROR HANDLING
    ==============================

Tangani

GPS mati

Internet mati

Firestore gagal

Google Maps gagal

Directions gagal

API timeout

API 404

API 500

Permission ditolak

Collection kosong

Marker kosong

==============================
20. FIRESTORE
    ==============================

Collection

users

fishing_points

reviews

favorites

weather_cache

tide_cache

notifications

Gunakan Repository.

Tidak boleh query langsung dari Fragment.

==============================
21. TESTING
    ==============================

Pastikan seluruh fitur dapat diuji.

Marker muncul.

Lokasi realtime.

Directions berjalan.

Search berjalan.

Filter berjalan.

Weather muncul.

Tide muncul.

Recommendation muncul.

Favorite tersimpan.

Share berhasil.

Tidak ada Crash.

Tidak ada Memory Leak.

Tidak ada Dummy Data.

------------------------------------------------

OUTPUT

Jangan hanya membuat kode.

Jelaskan

File yang diubah

Repository

ViewModel

Model

Service

API

Dependency

Permission

Manifest

Gradle

Firestore

Alur Data

Flow MVVM

Pastikan seluruh fitur Maps selesai 100% dan siap digunakan sebagai fitur utama aplikasi Fishing Point.