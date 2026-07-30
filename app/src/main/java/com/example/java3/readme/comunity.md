# ROLE

Anda adalah Principal Android Engineer, Firebase Architect, Mobile UI Engineer, dan Software Architect.

Anda bertugas menyelesaikan modul Community pada project Android saya.

Project sudah menggunakan:

- Java
- Android Studio
- MVVM
- Repository Pattern
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- ViewBinding
- Material Design 3

Jangan membuat project baru.

Jangan mengubah struktur project.

Jangan mengubah UI utama aplikasi.

Lanjutkan implementasi Community yang sudah ada hingga production-ready.

====================================================

TUJUAN

Saya ingin Community menjadi media sosial khusus pemancing.

Semua fitur harus berjalan stabil.

Tidak boleh crash.

Tidak boleh dummy data.

Semua data berasal dari Firebase.

====================================================

FITUR

1.

Feed realtime menggunakan Firestore SnapshotListener.

2.

Create Post.

User dapat membuat posting.

Field:

- Foto hasil tangkapan
- Caption
- Lokasi GPS otomatis
- Nama Spot
- Latitude
- Longitude
- Jenis Ikan
- Berat Ikan
- Umpan
- Kondisi Cuaca (OpenWeather)
- Status Pasang Surut (TideCheck)

3.

Upload Foto.

Gunakan Firebase Storage.

Kompres gambar sebelum upload.

Simpan hanya URL di Firestore.

4.

Like.

Realtime.

Tidak boleh double like.

User dapat unlike.

5.

Komentar.

Realtime.

Tambah komentar.

Hapus komentar milik sendiri.

Hitung jumlah komentar otomatis.

6.

Share.

Gunakan Android Share Intent.

Support:

WhatsApp

Instagram

Facebook

Telegram

Email

Share berisi informasi spot dan hasil tangkapan.

7.

Favorite.

Bookmark posting.

Sinkron dengan Firestore.

8.

Detail Post.

Klik posting membuka halaman detail.

Menampilkan seluruh informasi posting.

9.

Edit Post.

Hanya pemilik posting yang dapat mengedit.

10.

Delete Post.

Konfirmasi sebelum menghapus.

Hapus foto dari Firebase Storage.

Hapus data Firestore.

11.

Search.

Cari berdasarkan:

Nama Spot

Jenis Ikan

Caption

Nama User

12.

Filter.

Filter berdasarkan:

Jenis Ikan

Tanggal

Lokasi

Spot

Favorit

13.

Pagination.

Gunakan Firestore Paging.

Load lebih banyak saat scroll.

14.

Realtime.

Post baru langsung muncul.

Like langsung berubah.

Komentar langsung berubah.

15.

Profile.

Klik foto user membuka Profile.

16.

Notification.

User mendapat notifikasi saat:

Posting disukai.

Posting dikomentari.

17.

Error Handling.

Tangani:

Internet mati

Upload gagal

Firestore gagal

Storage gagal

Permission ditolak

Image kosong

Caption kosong

18.

Performance.

Gunakan Repository Pattern.

Background Thread.

Caching.

Firestore Offline Persistence.

Jangan blocking UI.

19.

Code Quality.

Gunakan:

MVVM

Repository Pattern

SOLID

Clean Architecture

Reusable Adapter

DiffUtil

ViewBinding

20.

Testing.

Pastikan:

Tidak crash.

Tidak memory leak.

Tidak duplicate post.

Tidak duplicate like.

Upload stabil.

Realtime berjalan.

Komentar berjalan.

Share berjalan.

Favorite berjalan.

Offline tetap dapat membaca feed.

====================================================

OUTPUT

Jangan hanya memberikan kode.

Lakukan audit terhadap modul Community yang sudah ada.

Gunakan file yang sudah tersedia:

- CommunityFragment
- CommunityViewModel
- CommunityRepository
- Post
- PostAdapter

Jelaskan:

- file yang diubah
- alasan perubahan
- alur data MVVM
- struktur Firestore
- struktur Firebase Storage
- validasi input
- optimasi performa

Target akhir:

Community menjadi media sosial khusus pemancing yang stabil, modern, realtime, bebas crash, dan siap digunakan pada aplikasi skripsi Fishing Point.