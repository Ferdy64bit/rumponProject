Bertindak sebagai Senior Android Engineer, Software Architect, Data Engineer, dan Dosen Pembimbing Skripsi.

Project ini adalah aplikasi Android Fishing Point berbasis Java menggunakan:

- MVVM
- Repository Pattern
- Google Maps SDK
- Fused Location Provider
- Firebase Authentication
- Cloud Firestore
- Cloudinary
- OpenWeather API
- BMKG Peta Maritim API

====================================================
TUJUAN
====================================================

Saya ingin membuat Recommendation Engine yang menghasilkan rekomendasi lokasi memancing menggunakan pendekatan yang terinspirasi dari konsep Solunar Prediction.

JANGAN menyalin algoritma Solunar asli karena algoritmanya proprietary.

Sebaliknya, buat formula ilmiah yang dapat dipertanggungjawabkan pada skripsi tetapi menghasilkan perilaku rekomendasi yang mirip dengan aplikasi Fishing Point/Fishbrain.

====================================================
OUTPUT
====================================================

Buat Recommendation Engine yang menghasilkan:

1. Fishing Score (0-100)

2. Recommendation Label

- Sangat Direkomendasikan
- Direkomendasikan
- Cukup
- Perlu Waspada
- Tidak Direkomendasikan

3. Confidence

Low

Medium

High

4. Alasan rekomendasi

Contoh:

✔ Cuaca cerah

✔ Gelombang rendah

✔ Angin stabil

✔ Dekat dengan lokasi pengguna

✔ Rating spot tinggi

====================================================
FAKTOR YANG DIGUNAKAN
====================================================

Gunakan seluruh data yang sudah tersedia pada aplikasi.

A.

Jarak menggunakan Haversine

Semakin dekat semakin tinggi skor.

B.

OpenWeather

Gunakan:

weather

temperature

humidity

pressure

visibility

rain

cloud

wind

C.

BMKG Peta Maritim

Gunakan:

weather

waveCategory

waveDescription

windSpeed

windDirection

warning

stationRemark

forecastTime

D.

Fishing Point

Gunakan:

rating

reviewCount

favoriteCount

visitCount

fishType

spotType

E.

User

Gunakan:

favorite history

last visited

====================================================
SKEMA BOBOT
====================================================

Gunakan bobot berikut.

Distance

30%

Marine Condition (BMKG)

25%

Weather

20%

Spot Quality

15%

User Preference

10%

Total

100%

====================================================
DETAIL PENILAIAN
====================================================

Distance

0-30 poin

0-1 km

30

1-3 km

25

3-5 km

20

5-10 km

10

>10 km

5

====================================================

Marine Condition

0-25 poin

Excellent

25

Good

20

Fair

15

Poor

5

Danger

0

Penilaian berdasarkan:

Wave Height

Wind Speed

Warning

====================================================

Weather

0-20 poin

Sunny

20

Cloudy

16

Light Rain

10

Heavy Rain

0

====================================================

Spot Quality

0-15 poin

Rating

Review

Favorite

Visit

====================================================

User Preference

0-10 poin

Spot favorit

Jenis ikan favorit

Riwayat kunjungan

====================================================
HASIL AKHIR
====================================================

Fishing Score

0-100

Kategori

90-100

Sangat Direkomendasikan

75-89

Direkomendasikan

60-74

Cukup

40-59

Perlu Waspada

0-39

Tidak Direkomendasikan

====================================================
REQUIREMENT
====================================================

Buat class:

RecommendationEngine

RecommendationCalculator

RecommendationReason

RecommendationScore

FishingRecommendation

RecommendationAnalyzer

RecommendationResult

====================================================

RecommendationResult harus berisi:

score

label

distanceScore

weatherScore

marineScore

spotScore

userPreferenceScore

confidence

reasons

====================================================

Dashboard harus menampilkan:

Fishing Score

Recommendation Label

5 alasan utama

====================================================

Maps harus:

Mengubah warna marker berdasarkan Fishing Score.

Hijau

Kuning

Merah

====================================================

Recommendation harus selalu dapat dijelaskan.

Setiap skor wajib mempunyai alasan.

Tidak boleh ada magic number.

Seluruh bobot harus disimpan sebagai konstanta.

====================================================

Tambahkan dokumentasi lengkap agar formula ini dapat langsung dimasukkan ke BAB IV skripsi.

Sertakan:

- Flowchart Recommendation Engine
- Diagram alur data
- Rumus matematis
- Contoh perhitungan manual
- Penjelasan setiap bobot
- Alasan pemilihan bobot
- Contoh kasus nyata

====================================================

Pastikan seluruh implementasi tetap mengikuti arsitektur MVVM + Repository Pattern.

Jangan merusak fitur yang sudah ada.