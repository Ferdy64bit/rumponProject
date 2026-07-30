# ROLE

Anda adalah Principal Android Engineer, Google Maps Specialist, Firebase Architect, GIS Engineer, dan Mobile Software Architect dengan pengalaman lebih dari 15 tahun.

Anda bertugas menjadi Technical Lead untuk menyelesaikan modul Maps pada project Android saya.

Jangan membuat project baru.

Jangan mengubah UI yang sudah saya buat.

Jangan mengubah arsitektur project.

Fokus melakukan audit, refactor, debugging, dan penyempurnaan modul Maps hingga production-ready.

====================================================

PROJECT

Nama
Fishing Point

Bahasa
Java

IDE
Android Studio

Architecture
MVVM

Pattern
Repository Pattern

Maps
Google Maps SDK

Database
Firebase Firestore

Authentication
Firebase Authentication

Storage
Firebase Storage

Location
Fused Location Provider

API
OpenWeather API
TideCheck API

====================================================

TUJUAN

Halaman Maps adalah fitur utama aplikasi Fishing Point.

Saya ingin seluruh fitur Maps berfungsi dengan stabil.

Tidak boleh ada crash.

Tidak boleh ada dummy data.

Semua data berasal dari Firestore.

====================================================

KONDISI PROJECT SAAT INI

Google Maps tampil.

Bottom Navigation berjalan.

Dashboard sudah cukup.

Community sudah memiliki struktur.

ViewModel sudah dibuat.

Repository sudah dibuat.

ViewBinding sudah digunakan.

Map menggunakan MapView.

Namun terdapat beberapa masalah:

- Marker tidak tampil.
- Add Marker force close.
- Marker belum realtime.
- Firestore belum sinkron sempurna.
- Weather belum muncul.
- Tide belum muncul.
- Recommendation belum aktif.

====================================================

TUGAS

Lakukan audit seluruh modul Maps.

Jangan hanya memperbaiki error.

Temukan seluruh kelemahan implementasi.

====================================================

1. GOOGLE MAPS

Audit seluruh konfigurasi Google Maps SDK.

Periksa

AndroidManifest

API Key

Permission

Gradle

Dependency

Lifecycle

MapView

SupportMapFragment

Renderer

Map Type

Compass

Zoom

My Location

Traffic

Buildings

Indoor

Padding

Rotation

Bearing

Tilt

Dark Mode

====================================================

2. LIFECYCLE MAPVIEW

Pastikan lifecycle lengkap.

onCreate

onStart

onResume

onPause

onStop

onDestroy

onLowMemory

Pastikan tidak terjadi Memory Leak.

====================================================

3. CURRENT LOCATION

Audit FusedLocationProvider.

Pastikan

Permission Runtime

GPS

Current Location

Realtime Update

Camera Follow User

Center Location Button

Berjalan dengan baik.

====================================================

4. FIRESTORE

Periksa collection

users

fishing_points

favorites

reviews

community_posts

notifications

Pastikan

Repository digunakan.

Tidak ada query Firestore langsung di Fragment.

Gunakan SnapshotListener.

====================================================

5. MARKER

Marker harus berasal dari Firestore.

Marker muncul otomatis.

Marker update realtime.

Marker hilang jika data dihapus.

Marker berubah jika data diubah.

Marker memiliki

Nama Spot

Jenis Spot

Latitude

Longitude

Foto

Jenis Ikan

Deskripsi

Rating

Fasilitas

====================================================

6. ADD MARKER

Audit total.

Saat tombol "+" ditekan

Dialog muncul.

Tidak force close.

Validasi seluruh input.

Pastikan

Latitude valid.

Longitude valid.

Nama tidak kosong.

Jenis Spot dipilih.

Foto dapat diupload.

Data berhasil disimpan ke Firestore.

Marker langsung muncul.

Tanpa restart aplikasi.

====================================================

7. EDIT MARKER

Tambah fitur edit.

Marker dapat diubah.

Firestore ikut berubah.

====================================================

8. DELETE MARKER

Tambah fitur hapus.

Konfirmasi sebelum menghapus.

Marker langsung hilang.

====================================================

9. CUSTOM MARKER

Gunakan icon berbeda berdasarkan

Pantai

Muara

Dermaga

Tambak

Danau

Sungai

Favorit

Recommendation

Lokasi User

====================================================

10. INFO WINDOW

Klik Marker membuka Card.

Isi

Foto

Nama Spot

Jenis Ikan

Rating

Jarak

Cuaca

Pasang Surut

Button

Detail

Navigasi

Favorite

Share

====================================================

11. SEARCH

Cari Marker berdasarkan

Nama Spot

Jenis Ikan

Lokasi

Daerah

Realtime.

====================================================

12. FILTER

Filter

Rating

Jenis Spot

Jenis Ikan

Fasilitas

Favorit

Jarak

====================================================

13. HAVERSINE

Hitung jarak user ke seluruh spot.

Urutkan dari terdekat.

====================================================

14. ROUTE

Gunakan Google Directions API atau Routes API.

Tampilkan Polyline.

Hitung

Distance

Duration

====================================================

15. WEATHER

Integrasikan OpenWeather API.

Marker menampilkan

Suhu

Humidity

Wind Speed

Pressure

Weather Icon

Description

====================================================

16. TIDE

Integrasikan TideCheck API.

Marker menampilkan

Status Pasang

Status Surut

High Tide

Low Tide

Jam berikutnya

====================================================

17. RECOMMENDATION

Buat Recommendation Engine.

Gunakan

Haversine

Weather

Tide

Rating

Review

Output

Persentase

Excellent

Good

Fair

Poor

====================================================

18. PERFORMANCE

Gunakan

ClusterManager

Lazy Loading

Background Thread

Cache Firestore

Offline Persistence

Jangan blocking UI.

====================================================

19. ERROR HANDLING

Tangani

GPS mati

Internet mati

API gagal

Firestore gagal

Permission ditolak

Google Maps gagal

Timeout

401

403

404

500

NullPointerException

IllegalStateException

ResourcesNotFoundException

====================================================

20. LOGGING

Tambahkan Log untuk

MAP_READY

MAP_DESTROY

LOCATION_UPDATE

FIRESTORE_READ

FIRESTORE_WRITE

MARKER_ADD

MARKER_EDIT

MARKER_DELETE

MARKER_CLICK

WEATHER_SUCCESS

WEATHER_FAILED

TIDE_SUCCESS

TIDE_FAILED

ROUTE_SUCCESS

ROUTE_FAILED

====================================================

21. CODE QUALITY

Refactor bila diperlukan.

Gunakan

MVVM

Repository Pattern

SOLID

Clean Code

Single Responsibility

Reusable Component

====================================================

22. OUTPUT

Jangan hanya memberikan kode.

Berikan laporan lengkap.

Untuk setiap masalah jelaskan:

- Penyebab.
- Dampaknya.
- Lokasi file.
- Cara memperbaiki.
- Kode yang diubah.
- Alasan perubahan.

Jika menemukan bug lain di luar Maps, laporkan juga tetapi jangan mengubahnya tanpa penjelasan.

====================================================

TARGET AKHIR

Saya ingin modul Maps menjadi fondasi utama aplikasi Fishing Point.

Google Maps stabil.

Marker realtime.

Add Marker berhasil.

Edit Marker berhasil.

Delete Marker berhasil.

Firestore sinkron.

Weather aktif.

Tide aktif.

Route aktif.

Recommendation aktif.

Search aktif.

Filter aktif.

Tidak ada crash.

Tidak ada memory leak.

Tidak ada dummy data.

Siap digunakan sebagai fitur utama aplikasi skripsi.