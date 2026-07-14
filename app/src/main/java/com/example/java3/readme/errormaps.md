# ROLE

Anda adalah Principal Android Engineer, Google Maps SDK Expert, Firebase Expert, dan Software Architect dengan pengalaman lebih dari 15 tahun.

Anda bertugas melakukan AUDIT TOTAL terhadap modul Maps pada aplikasi Android "Fishing Point".

Jangan hanya memperbaiki crash.

Temukan akar masalahnya.

Perbaiki seluruh implementasi agar stabil.

Gunakan Java.

Gunakan Android Studio.

Gunakan MVVM.

Gunakan Repository Pattern.

Gunakan Google Maps SDK.

Gunakan Firebase Firestore.

Gunakan Firebase Authentication.

Gunakan Firebase Storage.

Gunakan Fused Location Provider.

Gunakan ViewBinding.

Jangan mengubah desain UI.

Jangan mengubah arsitektur project.

================================================

PROJECT

Nama :
Fishing Point

Bahasa :
Java

IDE :
Android Studio

Maps :
Google Maps SDK

Database :
Firestore

Authentication :
Firebase Authentication

Storage :
Firebase Storage

Weather :
OpenWeather API

Sea Tide :
TideCheck API

Architecture :
MVVM

================================================

MASALAH SAAT INI

1.

Google Maps berhasil tampil.

2.

Lokasi pengguna berhasil tampil.

3.

Marker Firestore belum tampil.

4.

Saat tombol

+

(Add Marker)

ditekan

Aplikasi FORCE CLOSE.

================================================

LOGCAT

Error utama adalah

android.content.res.Resources$NotFoundException

Resource ID #0x0

Crash berasal dari

MapFragment.showAddMarkerDialog()

sekitar

MapFragment.java line 254

Stacktrace menunjukkan

AlertDialog.Builder.show()

↓

LayoutInflater.inflate()

↓

Resources$NotFoundException

Artinya kemungkinan besar terdapat Resource Layout yang bernilai 0, salah, atau tidak ditemukan.

================================================

TUGAS

Saya ingin Anda melakukan audit total.

Jangan hanya memperbaiki satu baris.

Cari seluruh kemungkinan penyebab.

================================================

STEP 1

Periksa

MapFragment.java

khususnya

showAddMarkerDialog()

Periksa apakah

LayoutInflater

menggunakan layout yang benar.

Pastikan

inflate()

tidak pernah menerima

0

null

resource yang salah

layout yang sudah dihapus

layout yang belum dibuat

================================================

STEP 2

Periksa

AlertDialog.Builder

Pastikan

setView()

menggunakan layout yang valid.

Periksa

setTitle()

setMessage()

setIcon()

setCustomTitle()

setPositiveButton()

setNegativeButton()

Pastikan tidak ada Resource ID yang bernilai 0.

================================================

STEP 3

Periksa seluruh

layout

yang digunakan dialog.

Misalnya

dialog_add_marker.xml

atau nama lain.

Pastikan

layout benar-benar ada.

Tidak corrupt.

Tidak duplicate.

Tidak salah package.

Tidak salah import.

================================================

STEP 4

Periksa seluruh

R.layout.*

Pastikan

resource tersebut benar-benar dibuat.

Jika menggunakan ViewBinding

Pastikan binding sesuai.

================================================

STEP 5

Periksa seluruh EditText

Spinner

Button

ImageView

AutoComplete

TextInputLayout

Pastikan

findViewById()

tidak mengembalikan null.

================================================

STEP 6

Periksa Marker

Saat user menekan

Simpan

Pastikan

Latitude

Longitude

tidak null.

Nama Spot

tidak kosong.

Jenis Ikan

tidak kosong.

Semua validasi dilakukan.

================================================

STEP 7

Periksa Firestore

Collection

fishing_points

Pastikan

berhasil dibuat.

Pastikan

Repository

dipanggil.

Pastikan

tidak ada exception.

================================================

STEP 8

Periksa Google Maps

Pastikan

Marker berhasil dibuat.

Pastikan

googleMap

tidak null.

Pastikan

MarkerOptions

menggunakan

LatLng

yang valid.

================================================

STEP 9

Periksa Lifecycle

MapView

Pastikan

onCreate()

onStart()

onResume()

onPause()

onStop()

onDestroy()

onLowMemory()

dipanggil dengan benar.

================================================

STEP 10

Periksa ClusterManager

Pastikan

clusterManager

diinisialisasi.

Pastikan

addItem()

dipanggil.

Pastikan

cluster()

dipanggil.

================================================

STEP 11

Periksa Marker Renderer

Pastikan

Custom Marker

tidak menyebabkan crash.

================================================

STEP 12

Periksa Repository

Pastikan

tidak ada query Firestore

langsung dari Fragment.

Gunakan ViewModel.

================================================

STEP 13

Periksa seluruh kemungkinan

NullPointerException

IllegalArgumentException

IllegalStateException

ResourcesNotFoundException

FirebaseException

Google Maps Exception

IndexOutOfBoundsException

SecurityException

================================================

STEP 14

Tambahkan Logging

Saya ingin setiap proses memiliki log.

Misalnya

MAP_INIT

MAP_READY

LOCATION_READY

MARKER_LOAD

MARKER_CLICK

MARKER_SAVE

MARKER_DELETE

FIRESTORE_SUCCESS

FIRESTORE_FAILED

DIALOG_OPEN

DIALOG_SAVE

DIALOG_CANCEL

Supaya debugging jauh lebih mudah.

================================================

STEP 15

Marker

Saya ingin marker memiliki

Nama Spot

Jenis Spot

Latitude

Longitude

Rating

Foto

Jenis Ikan

Deskripsi

Status Cuaca

Status Pasang

Marker dapat

Klik

Edit

Delete

Favorite

Share

================================================

STEP 16

Pastikan Add Marker

Tidak Force Close.

Dialog muncul.

Data tervalidasi.

Berhasil disimpan.

Firestore berhasil update.

Marker langsung muncul.

Tanpa restart aplikasi.

================================================

STEP 17

Marker Realtime

Gunakan SnapshotListener.

Jika Firestore berubah

Marker berubah otomatis.

================================================

STEP 18

Performance

Pastikan

Semua proses Firestore

berjalan di Repository.

UI tidak melakukan proses berat.

Gunakan Background Thread.

Tidak boleh ada

Skipped Frames

Tidak boleh ada

Memory Leak.

================================================

STEP 19

OUTPUT

Saya ingin laporan lengkap.

Jelaskan

1.

Apa penyebab crash.

2.

Mengapa crash terjadi.

3.

File mana yang salah.

4.

Baris mana yang salah.

5.

Cara memperbaikinya.

6.

Kode yang harus diubah.

7.

Mengapa solusi tersebut benar.

8.

Perbaikan apa saja yang dilakukan.

9.

Kemungkinan bug lain yang ditemukan.

10.

Rekomendasi refactor.

================================================

TARGET AKHIR

Saya ingin halaman Maps menjadi stabil.

Google Maps tampil.

Marker tampil.

Marker realtime.

Add Marker berhasil.

Firestore berhasil.

Cluster berjalan.

Current Location berjalan.

Tidak ada Force Close.

Tidak ada Dummy Data.

Tidak ada Memory Leak.

Tidak ada Resource Error.

Tidak ada Crash.

Semua fitur Maps siap digunakan sebagai fitur utama aplikasi skripsi Fishing Point.