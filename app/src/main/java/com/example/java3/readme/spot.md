Bertindaklah sebagai seorang Senior Android Developer, Software Architect, dan UI/UX Designer.

Saya sedang mengembangkan aplikasi Android bernama "Fishing Point" menggunakan:

- Java
- XML
- MVVM Architecture
- Repository Pattern
- Firebase Authentication
- Cloud Firestore
- Cloudinary
- Google Maps SDK
- Material Design 3

PENTING:
Jangan mengubah arsitektur project yang sudah ada.
Jangan melakukan refactor besar.
Gunakan struktur project yang sudah tersedia.
Semua fitur baru harus terintegrasi dengan Repository, ViewModel, dan Firestore yang sudah ada.

====================================================
FITUR 1
SPOT SAYA (PROFILE)
====================================================

Pada halaman Profile terdapat statistik:

- Postingan
- Spot
- Favorit

Saat ini bagian Spot belum berjalan.

Saya ingin membuat halaman "Spot Saya".

Halaman ini harus menampilkan seluruh spot yang dibuat oleh user yang sedang login.

Query Firestore:

ownerId == currentUser.uid

Tampilkan dalam bentuk CardView modern.

Setiap card berisi:

- Foto Spot
- Nama Spot
- Jenis Spot
- Status (Publik / Privat)
- Rating
- Jumlah Favorit
- Jumlah Review
- Tanggal dibuat

Di setiap card terdapat menu:

- Edit
- Hapus
- Ubah Status Publik / Privat

====================================================
FITUR 2
SPOT FAVORIT
====================================================

Saat ini halaman Spot Favorit kosong.

Halaman ini harus mengambil seluruh data spot yang difavoritkan oleh user.

Struktur Firestore:

users

    uid

        favorites

            spotId

Kemudian ambil detail spot berdasarkan spotId.

Tampilkan card yang sama seperti halaman Spot.

Berikan tombol:

- Hapus Favorit

====================================================
FITUR 3
SPOT PUBLIK DAN SPOT PRIBADI
====================================================

Saat user menekan tombol Tambah Spot.

Sebelum form muncul.

Tambahkan pilihan:

○ Spot Publik

○ Spot Pribadi

Spot Publik:

- tampil di Maps
- tampil di Search
- tampil di Nearby
- tampil di Community
- dapat dilihat seluruh user

Spot Pribadi:

- hanya pemilik yang dapat melihat
- tidak muncul pada user lain
- tidak muncul di Search
- tidak muncul di Maps publik
- tidak muncul di Nearby user lain

Tambahkan field baru pada Firestore:

visibility

dengan value:

PUBLIC

atau

PRIVATE

====================================================
FITUR 4
EDIT VISIBILITY
====================================================

Pada halaman Edit Spot.

Tambahkan pilihan:

Visibility

Dropdown:

- Public
- Private

Saat disimpan.

Firestore otomatis mengubah field visibility.

====================================================
FITUR 5
DETAIL SPOT
====================================================

Pada halaman Detail Spot.

Tambahkan informasi:

---------------------------------

Dibuat oleh

Foto Profile

Nama User

Fishing Point Member

Tanggal dibuat

Status Spot

(Publik / Privat)

---------------------------------

Jika Spot bersifat Private.

Maka tampilkan badge:

PRIVATE

Jika Spot bersifat Public.

Tampilkan badge:

PUBLIC

Klik nama pembuat membuka halaman Public Profile.

====================================================
FITUR 6
PROFILE STATISTIC
====================================================

Bagian statistik profile harus otomatis menghitung:

Postingan

jumlah posting user

Spot

jumlah spot milik user

Favorit

jumlah spot favorit user

Semua data realtime dari Firestore.

====================================================
FITUR 7
FILTER SPOT
====================================================

Pada halaman Spot.

Tambahkan filter:

Semua

Publik

Pribadi

Favorit

Semua menggunakan RecyclerView yang sama.

====================================================
FITUR 8
SEARCH
====================================================

Search hanya boleh menampilkan:

visibility == PUBLIC

Kecuali user adalah owner.

Owner tetap dapat mencari Spot Private miliknya sendiri.

====================================================
FITUR 9
MAPS
====================================================

Pada Maps.

Marker Public:

warna Biru.

Marker Private:

warna Hijau.

Marker Private hanya muncul untuk pemilik.

User lain tidak boleh melihat marker tersebut.

====================================================
FITUR 10
FIRESTORE
====================================================

Lengkapi struktur data FishingPoints menjadi:

spotId

ownerId

ownerName

ownerPhoto

title

description

latitude

longitude

imageUrl

spotType

visibility

rating

favoriteCount

reviewCount

createdAt

updatedAt

====================================================
UI DESIGN
====================================================

Gunakan Material Design 3.

Card:

radius 20dp

elevation 4dp

padding konsisten

Gunakan:

RecyclerView

CardView

ConstraintLayout

BottomSheet

MaterialButton

MaterialCardView

MaterialChip

Status:

PUBLIC

warna hijau

PRIVATE

warna abu-abu

====================================================
HASIL YANG DIINGINKAN
====================================================

Saya ingin AI menghasilkan:

1. Alur sistem lengkap

2. Struktur Firestore terbaru

3. Repository

4. ViewModel

5. Activity / Fragment

6. Adapter RecyclerView

7. Layout XML

8. Query Firestore

9. Validasi

10. Integrasi penuh tanpa merusak fitur lama

Pastikan seluruh kode mengikuti MVVM Architecture dan mudah dipelihara.