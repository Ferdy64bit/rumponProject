Saya sedang mengembangkan aplikasi Android Native Java bernama "Fishing Point" sebagai project skripsi.

====================================================
PENTING
====================================================

Sebelum melakukan perubahan apa pun:

1. Analisis seluruh struktur project.
2. Jangan membuat project baru.
3. Jangan mengubah arsitektur MVVM yang sudah ada.
4. Jangan mengubah package.
5. Jangan menghapus fitur yang sudah berjalan.
6. Jangan membuat duplicate Repository.
7. Pastikan project tetap bisa di-build tanpa error.

Semua perubahan harus mengikuti struktur project yang sudah ada.

====================================================
STACK PROJECT
====================================================

- Java
- Android Studio
- MVVM
- Repository Pattern
- Firebase Authentication
- Cloud Firestore
- Cloudinary
- Google Maps SDK
- Retrofit
- Gson
- Glide
- Material Design 3
- ViewBinding

====================================================
TUJUAN UTAMA
====================================================

Saya ingin melakukan migrasi penuh dari TideCheck API menuju BMKG Peta Maritim API.

Sekaligus mengoptimalkan project agar:

- lebih ringan
- lebih rapi
- lebih modular
- mudah dipelihara
- siap untuk kebutuhan skripsi

====================================================
MIGRASI BMKG
====================================================

Saat ini project masih memiliki:

TideService

TideRepository

TideResponse

TideCache

TideStation

Seluruh komponen tersebut harus dianalisis.

Jika masih relevan boleh dipakai ulang.

Jika tidak relevan, refactor tanpa merusak project.

====================================================
SUMBER DATA BMKG
====================================================

Saya memiliki file Excel berisi seluruh endpoint BMKG Peta Maritim.

Gunakan file tersebut sebagai referensi utama.

Endpoint berbentuk

https://peta-maritim.bmkg.go.id/public_api/perairan/{AREA}.json

Contoh

F.09_Teluk Jakarta.json

Jangan hardcode URL satu per satu.

Buat sistem yang scalable.

====================================================
YANG HARUS DIBANGUN
====================================================

Saya ingin AI membuat arsitektur berikut.

network

BMKGApiService

↓

repository

BMKGRepository

↓

model

BMKGResponse

↓

cache

BMKGCache

↓

viewmodel

BMKGViewModel

↓

UI

Dashboard

Maps

Detail Spot

Recommendation

====================================================
JSON LOKAL
====================================================

Gunakan seluruh data endpoint BMKG yang tersedia.

Buat file

assets/bmkg_regions.json

berisi

code

area

url

Contoh

[
 {
   "code":"F.09",
   "area":"Teluk Jakarta",
   "url":"https://....json"
 }
]

Aplikasi harus membaca JSON tersebut.

Jangan menggunakan if else panjang.

====================================================
PEMILIHAN AREA BMKG
====================================================

AI harus membuat algoritma untuk menentukan area BMKG.

Prioritas:

1. Lokasi user

2. Lokasi Fishing Point

3. Area BMKG terdekat

Jika GPS tidak tersedia

gunakan default

Teluk Jakarta








====================================================
BMKG MODEL
====================================================

Model harus mampu membaca

Area

Cuaca

Gelombang

Kategori Gelombang

Kecepatan Angin

Arah Angin

Warning

Visibility

Tanggal Update

Jika response berubah

buat parser yang fleksibel.

====================================================
PREDIKSI BMKG (MULTI DAY FORECAST)
====================================================

BMKG Peta Maritim mengembalikan data forecast dalam bentuk array.

Contoh

data[]

berisi

Hari Ini

Besok

H+2

H+3

AI harus memanfaatkan seluruh data tersebut.

Jangan hanya mengambil item pertama.

====================================================

AI harus membuat parser yang mampu membaca seluruh forecast.

Contoh

Hari Ini

Besok

H+2

H+3

dan mengubahnya menjadi model

BMKGForecast

====================================================

Model

BMKGForecast

harus memiliki

validFrom

validTo

timeDesc

weather

weatherDesc

waveCategory

waveDescription

windFrom

windTo

windSpeedMin

windSpeedMax

warning

stationRemark

====================================================

Repository harus mengembalikan

List<BMKGForecast>

bukan hanya satu object.

====================================================

Dashboard harus menampilkan

Hari Ini

Besok

H+2

H+3

menggunakan RecyclerView horizontal
atau ViewPager2.

====================================================

Setiap card forecast harus berisi

Tanggal

Icon Cuaca

Cuaca

Gelombang

Kategori Gelombang

Angin

Warning BMKG

====================================================

Tambahkan indikator warna

Hijau

Kondisi Aman

Kuning

Perlu Waspada

Merah

Tidak Disarankan

====================================================

RECOMMENDATION BERDASARKAN BMKG

AI harus membuat Recommendation Analyzer.

Input

Forecast BMKG

↓

Output

Fishing Recommendation

Contoh

Excellent

Good

Fair

Poor

Avoid

====================================================

Contoh rule

Jika

wave <= 0.5 m

dan

wind <= 15 knot

Status

Excellent

Jika

wave <=1.25 m

Status

Good

Jika

wave <=2.5 m

Status

Fair

Jika

wave >2.5 m

atau

warning tidak kosong

Status

Avoid

====================================================

Dashboard harus memiliki section

"Prediksi Kondisi Memancing"

berisi

Hari Ini

Besok

H+2

H+3

dengan badge

⭐ Sangat Direkomendasikan

✅ Direkomendasikan

⚠ Perlu Waspada

❌ Tidak Direkomendasikan

====================================================

Jika BMKG menambahkan forecast baru

misalnya

H+4

H+5

maka aplikasi harus otomatis menampilkannya

tanpa mengubah source code.

Gunakan RecyclerView berdasarkan jumlah data dari API.

====================================================

Jangan melakukan hardcode

Hari Ini

Besok

H+2

H+3

Seluruh UI harus dinamis berdasarkan response JSON BMKG.

====================================================

Tambahkan cache forecast BMKG selama 30 menit.

Jika API gagal

gunakan cache.

Jika cache kosong

gunakan placeholder.

Jangan sampai aplikasi crash.

====================================================
DASHBOARD
====================================================

Dashboard harus menampilkan

Cuaca

Kondisi Laut

Gelombang

Angin

Warning

Status aman memancing

====================================================
DETAIL SPOT
====================================================

Tambahkan

Kondisi Perairan BMKG

Status Gelombang

Status Angin

Warning

====================================================
RECOMMENDATION ENGINE
====================================================

Refactor Recommendation Engine.

Gunakan bobot

Distance (Haversine)

Weather

BMKG Wave

BMKG Wind

Rating

Review

Favorite

Output

Excellent

Good

Fair

Poor

Skor

0 - 100

====================================================
CACHE
====================================================

Gunakan cache Firestore

bmkg_cache

atau cache lokal

Jangan request API setiap membuka aplikasi.

Cache

30 menit

====================================================
ERROR HANDLING
====================================================

Jika API gagal

gunakan cache.

Jika cache kosong

gunakan placeholder.

Jangan crash.

====================================================
OPTIMASI PROJECT
====================================================

AI harus melakukan audit project.

Cari

Class yang duplicate

Repository yang tidak dipakai

Model yang tidak dipakai

Adapter yang tidak dipakai

Layout yang duplicate

Drawable yang duplicate

Dependency yang tidak dipakai

Import yang tidak dipakai

Code yang tidak dipakai

====================================================
DEPENDENCY
====================================================

Audit seluruh Gradle.

Cari dependency

yang sudah tidak digunakan.

Jangan menghapus dependency
yang masih dipakai.

====================================================
BUILD
====================================================

Optimalkan

Gradle

Build

APK Size

Memory

Build Time

====================================================
PERFORMA
====================================================

Optimalkan

RecyclerView

DiffUtil

SnapshotListener

Pagination

Image Loading

Glide Cache

Retrofit

====================================================
MEMORY
====================================================

Pastikan

Tidak ada

Memory Leak

Context Leak

Fragment Leak

Bitmap Leak

====================================================
PROJECT CLEANUP
====================================================

AI harus memberi daftar

File yang aman dihapus

Folder build

Generated

Unused Resources

Unused Layout

Unused Drawable

Unused Assets

Tanpa menghapus source code penting.

====================================================
FIRESTORE
====================================================

Audit seluruh collection

users

community_posts

fishing_points

favorites

reviews

notifications

bmkg_cache

Pastikan field konsisten.

====================================================
CLOUDINARY
====================================================

Pastikan seluruh upload gambar

Community

Profile

Fishing Point

menggunakan Cloudinary.

Tidak boleh ada Firebase Storage.

====================================================
HASIL YANG SAYA INGINKAN
====================================================

AI harus bekerja bertahap.

Tahap 1

Analisis project.

Tahap 2

Daftar file yang akan diubah.

Tahap 3

Migrasi BMKG.

Tahap 4

Optimasi Recommendation.

Tahap 5

Optimasi Dashboard.

Tahap 6

Audit seluruh dependency.

Tahap 7

Audit ukuran project.

Tahap 8

Optimasi performa.

Tahap 9

Testing.

Tahap 10

Pastikan project bisa di-build tanpa error.

====================================================
PENTING
====================================================

Jangan membuat ulang project.

Jangan mengubah fitur yang sudah berjalan.

Jangan menghapus kode yang masih dipakai.

Seluruh perubahan harus production-ready, mengikuti MVVM, mudah dipahami, mudah dikembangkan, dan sesuai dengan kebutuhan skripsi "Fishing Point Berbasis Android Menggunakan Metode Haversine".