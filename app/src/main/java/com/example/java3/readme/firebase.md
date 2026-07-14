Saya sedang mengembangkan aplikasi Android bernama "Fishing Point" menggunakan Java di Android Studio dengan arsitektur MVVM (Presentation, Domain, Data).

Saya menggunakan:

- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Google Maps SDK
- OpenWeather API
- Tide API

Saat ini Firebase Authentication sudah berjalan dengan baik.

Saya ingin mulai membangun backend Firestore yang menjadi fondasi seluruh aplikasi.

Tolong bantu saya membuat struktur backend Firestore yang profesional dan scalable.

Persyaratan:

1. Buat struktur collection Firestore berikut:

users
fishing_points
reviews
favorites
community_posts
notifications
weather_cache
tide_cache

2. Untuk setiap collection, jelaskan:

- Nama field
- Tipe data
- Kegunaan field
- Relasi dengan collection lain

3. Buat model Java untuk setiap collection.

4. Buat Repository untuk:

- UserRepository
- FishingPointRepository
- ReviewRepository
- FavoriteRepository
- CommunityRepository

5. Repository harus menggunakan Firebase Firestore SDK terbaru.

6. Repository harus mendukung:

Create
Read
Update
Delete

7. Gunakan callback atau LiveData agar sesuai dengan MVVM.

8. Buat ViewModel untuk setiap Repository.

9. Jangan menggunakan data dummy.

10. Semua data Dashboard harus berasal dari Firestore.

11. Semua marker Google Maps harus berasal dari collection fishing_points.

12. Berikan struktur folder yang rapi sesuai project Android Studio saya.

13. Berikan rekomendasi Firestore Security Rules yang aman untuk tahap pengembangan dan versi production.

14. Jangan mengubah UI yang sudah ada, fokus hanya pada backend dan integrasi Firestore.

Target akhirnya adalah seluruh aplikasi menggunakan Firestore sebagai sumber data utama sehingga Dashboard, Google Maps, Community, Favorite, Review, dan Profile saling terhubung.


Saya menggunakan Firebase Firestore dalam mode Production.

Saat ini Rules saya adalah:

allow read, write: if false;

Akibatnya aplikasi Android saya selalu mendapatkan error:

PERMISSION_DENIED
Missing or insufficient permissions.

Saya ingin membuat Firestore Security Rules yang aman untuk tahap pengembangan aplikasi.

Persyaratan:

- Hanya user yang sudah login menggunakan Firebase Authentication yang boleh membaca dan menulis data.
- User yang belum login tidak boleh mengakses Firestore.
- Jelaskan setiap bagian Rules.
- Berikan juga versi Rules yang lebih aman untuk production ketika aplikasi selesai dikembangkan.