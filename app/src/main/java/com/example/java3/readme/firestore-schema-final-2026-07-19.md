# Firestore Schema Final - Fishing Point

Tanggal update: 19 Juli 2026

Dokumen ini membekukan arah schema Firestore agar data Weather, BMKG, Maps, Community, Profile, Favorite, Review, dan Recommendation tetap konsisten.

## users

Digunakan untuk profile dan identitas pengguna.

Field utama:

- `uid`: string
- `email`: string
- `displayName`: string
- `photoUrl`: string Cloudinary URL
- `bio`: string
- `location`: string
- `role`: string
- `emailVerified`: boolean
- `createdAt`: long timestamp
- `updatedAt`: long timestamp

## fishing_points

Digunakan untuk marker Maps, list spot, detail spot, Haversine, dan Recommendation Engine.

Field utama:

- `id`: string
- `name`: string
- `latitude`: double
- `longitude`: double
- `type`: string
- `fishType`: string
- `area`: string
- `locationName`: string
- `description`: string
- `rating`: double
- `reviewCount`: int
- `imageUrl`: string Cloudinary URL
- `createdBy`: string uid
- `createdAt`: long timestamp
- `updatedAt`: long timestamp

Catatan akurasi:

- `latitude` dan `longitude` wajib valid karena dipakai Haversine, Maps marker, Weather API, dan BMKG selector.
- Spot laut/pesisir lebih ideal untuk BMKG forecast daripada spot danau/sungai.

## community_posts

Digunakan untuk feed komunitas, create post, like, comment, favorite, dan share.

Field utama:

- `id`: string
- `userId`: string
- `userName`: string
- `userPhotoUrl`: string Cloudinary URL
- `caption`: string
- `imageUrl`: string Cloudinary URL
- `locationName`: string
- `latitude`: double
- `longitude`: double
- `weather`: string
- `tideStatus`: string berisi ringkasan kondisi perairan BMKG
- `fishType`: string
- `catchWeight`: double
- `likeCount`: int
- `commentCount`: int
- `favoriteCount`: int
- `createdAt`: long timestamp
- `updatedAt`: long timestamp

## comments

Subcollection yang disarankan:

```text
community_posts/{postId}/comments/{commentId}
```

Field utama:

- `id`: string
- `postId`: string
- `userId`: string
- `userName`: string
- `userPhotoUrl`: string
- `message`: string
- `createdAt`: long timestamp

## favorites

Digunakan untuk favorite spot dan favorite post.

Field utama:

- `id`: string
- `userId`: string
- `targetId`: string
- `targetType`: string, contoh `spot` atau `post`
- `createdAt`: long timestamp

Index logis:

- `userId + targetType`
- `userId + targetId`

## reviews

Digunakan untuk rating spot dan kualitas spot pada Recommendation Engine.

Field utama:

- `id`: string
- `userId`: string
- `pointId`: string
- `rating`: double
- `comment`: string
- `createdAt`: long timestamp
- `updatedAt`: long timestamp

Catatan:

- Setelah review dibuat/diubah, `fishing_points.rating` dan `fishing_points.reviewCount` perlu disinkronkan.

## notifications

Fitur pendukung, bukan inti proposal.

Field utama:

- `id`: string
- `userId`: string
- `title`: string
- `message`: string
- `type`: string
- `targetId`: string
- `isRead`: boolean
- `createdAt`: long timestamp

## weather_cache

Digunakan oleh `WeatherRepository` agar OpenWeather tidak dipanggil terus menerus.

Field utama:

- `id`: string, key koordinat dibulatkan
- `data`: string JSON `WeatherResponse`
- `source`: string `OpenWeather`
- `updatedAt`: long timestamp
- `expiresAt`: long timestamp
- `dataSizeBytes`: int

Kebijakan:

- Fresh TTL: 10 menit.
- Fallback maksimal: 24 jam.

## bmkg_cache

Digunakan oleh `BMKGRepository/TideRepository` agar BMKG Peta Maritim tidak dipanggil terus menerus.

Field utama:

- `id`: string, key area BMKG
- `data`: string JSON `TideResponse` yang membawa `List<BMKGForecast>`
- `source`: string `BMKG`
- `area`: string nama area BMKG
- `updatedAt`: long timestamp
- `expiresAt`: long timestamp
- `dataSizeBytes`: int

Kebijakan:

- Fresh TTL: 30 menit.
- Fallback maksimal: 24 jam.
- `tide_cache` hanya dianggap nama lama; arah final adalah `bmkg_cache`.

## Validasi Data Lingkungan

Data lingkungan yang digunakan aplikasi harus mengikuti alur ini:

1. Koordinat user atau fishing point.
2. OpenWeather berdasarkan koordinat tersebut.
3. BMKG area selector berdasarkan koordinat tersebut.
4. Cache dipakai jika fresh.
5. Fallback cache dipakai jika API gagal.
6. Recommendation Engine memakai data Weather dan BMKG dari repository yang sama.

