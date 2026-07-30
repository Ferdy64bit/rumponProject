Saya sedang mengembangkan aplikasi Android bernama "Fishing Point" menggunakan Java di Android Studio.

PENTING:
Jangan membuat project baru.
Jangan mengubah arsitektur yang sudah ada.
Jangan mengganti package name.
Jangan mengubah fitur yang sudah berjalan.
Analisis terlebih dahulu seluruh struktur project sebelum menambahkan kode baru.

====================================================
ARSITEKTUR PROJECT
====================================================

Project ini menggunakan:

- Java
- Android Studio
- MVVM Architecture
- Repository Pattern
- Firebase Authentication
- Cloud Firestore
- Firebase Storage
- Google Maps SDK
- ViewBinding
- Material Design 3
- Navigation Component

Gunakan pola coding yang sudah digunakan pada module Community, Dashboard dan Maps.

Ikuti struktur package yang sudah ada.

presentation/
repository/
model/
network/
utils/

Jangan membuat struktur baru apabila sudah tersedia.

====================================================
FITUR YANG SUDAH ADA
====================================================

✔ Login
✔ Register
✔ Dashboard
✔ Google Maps
✔ Fishing Point CRUD
✔ Community
✔ Upload Foto
✔ Firebase Storage
✔ Firestore
✔ Like
✔ Comment
✔ Share

Semua fitur tersebut HARUS tetap berjalan.

====================================================
TUJUAN
====================================================

Saya ingin membangun halaman Profile yang modern, ringan, responsive dan konsisten dengan desain aplikasi.

====================================================
PROFILE SCREEN
====================================================

Buat tampilan seperti aplikasi modern.

Bagian paling atas

Foto Profil berbentuk lingkaran

Nama lengkap

Email

Badge

"Fishing Point Member"

Background Header menggunakan gradient biru laut.

====================================================
MENU PROFILE
====================================================

Buat Card Menu berikut.

👤 Edit Profile

Mengubah

Nama

Nomor HP

Alamat

Foto Profil

Bio Singkat

==========================

🎣 Statistik Memancing

Jumlah Spot Dibuat

Jumlah Posting

Jumlah Like

Jumlah Komentar

Jumlah Favorite Spot

Tanggal Bergabung

==========================

📍 Spot Favorit

Menampilkan daftar Fishing Point yang sudah disimpan user.

==========================

❤️ Postingan Saya

Menampilkan seluruh posting Community milik user.

==========================

⚙ Pengaturan

Tema

Notifikasi

Bahasa

==========================

🔐 Keamanan

Ganti Password

Reset Password

==========================

ℹ Tentang Aplikasi

Versi

Developer

Universitas

====================================================
EDIT PROFILE
====================================================

User dapat mengubah

Foto Profil

Nama

Nomor HP

Alamat

Bio

Semua data disimpan ke Firestore.

Foto disimpan ke Firebase Storage.

====================================================
DATABASE
====================================================

Gunakan collection

users

dengan struktur

uid

name

email

phone

address

bio

photoUrl

joinDate

postCount

spotCount

favoriteCount

createdAt

updatedAt

====================================================
PHOTO PROFILE
====================================================

Saat tombol edit foto ditekan

Buka Gallery

Crop (jika library sudah ada)

Upload ke Firebase Storage

Folder

profile/

Setelah upload selesai

Update Firestore

Update UI otomatis

====================================================
STATISTIK
====================================================

Ambil data secara realtime dari Firestore

Hitung

Jumlah posting Community

Jumlah Spot Fishing Point

Jumlah Favorite

Jumlah Like

Gunakan SnapshotListener agar realtime.

====================================================
MY POSTS
====================================================

Ambil hanya posting milik user yang sedang login.

Gunakan Firebase UID.

====================================================
FAVORITE
====================================================

Tampilkan semua spot favorit user.

====================================================
MVVM
====================================================

Buat jika belum ada

ProfileFragment

ProfileViewModel

ProfileRepository

ProfileAdapter

ProfileState

ProfileUiModel

Gunakan LiveData.

Jangan menggunakan AsyncTask.

====================================================
UI
====================================================

Gunakan Material Design 3.

Gunakan CardView.

Gunakan MaterialButton.

Gunakan Shimmer ketika loading.

Gunakan CircularProgressIndicator ketika upload.

Gunakan Snackbar.

Gunakan BottomSheet untuk memilih

Camera

Gallery

====================================================
VALIDASI
====================================================

Nama tidak boleh kosong

Nomor HP hanya angka

Bio maksimal 150 karakter

Ukuran foto maksimal 5MB

====================================================
ERROR HANDLING
====================================================

Jika internet mati

Tampilkan pesan

"Periksa koneksi internet"

Jika upload gagal

Jangan crash.

Jika Firestore gagal

Tetap tampilkan data cache.

====================================================
OPTIMASI
====================================================

Gunakan Glide untuk foto.

Gunakan Firebase Offline Persistence.

Gunakan DiffUtil pada RecyclerView.

Gunakan ViewBinding.

Jangan membuat memory leak.

====================================================
PENTING
====================================================

Sebelum menulis kode:

1. Analisis seluruh project terlebih dahulu.
2. Gunakan class yang sudah ada.
3. Jangan membuat duplicate Repository.
4. Jangan mengubah Community.
5. Jangan mengubah Maps.
6. Jangan mengubah Dashboard.
7. Jangan mengubah Authentication.

Tambahkan hanya kode yang benar-benar diperlukan.

Jika terdapat class yang sudah ada, gunakan kembali.

Jika terdapat fungsi yang bisa dipakai ulang, gunakan fungsi tersebut.

====================================================
OUTPUT YANG DIINGINKAN
====================================================

Saya ingin AI:

1. Menganalisis project terlebih dahulu.
2. Menjelaskan file mana saja yang akan dibuat atau diubah.
3. Menjelaskan alasan setiap perubahan.
4. Mengimplementasikan Profile secara bertahap.
5. Memastikan project tetap bisa di-build tanpa error.
6. Tidak menghapus kode lama.
7. Tidak mengubah fitur yang sudah berjalan.
8. Menghasilkan kode production-ready yang rapi, modular, dan mengikuti arsitektur MVVM pada project saya.