# Testing Plan - Environment dan Recommendation

Tanggal update: 19 Juli 2026

Dokumen ini dipakai untuk menguji akurasi Weather, BMKG, Haversine, dan Recommendation Engine di semua elemen aplikasi.

## Target Pengujian

Fitur yang diuji:

- Dashboard cuaca dan BMKG.
- Maps spot card.
- Create Post weather dan kondisi perairan.
- Recommendation Engine FAI.
- Cache `weather_cache` dan `bmkg_cache`.

## Skenario Utama

| No | Skenario | Input | Hasil Yang Diharapkan |
|---:|---|---|---|
| 1 | Lokasi Tanjung Anom | Lat `-6.041980`, Lon `106.501318` | BMKG memilih `F.09_Teluk Jakarta`, weather tampil dari OpenWeather, rekomendasi muncul. |
| 2 | GPS tidak tersedia | Permission ditolak atau lokasi null | Aplikasi memakai fallback Tanjung Anom dan tidak crash. |
| 3 | Spot dekat user | Jarak Haversine kurang dari 3 km | Distance Score tinggi dan rekomendasi cenderung naik jika cuaca/BMKG aman. |
| 4 | Spot jauh user | Jarak lebih dari 20 km | Distance Score rendah dan rekomendasi turun. |
| 5 | BMKG warning aktif | Response BMKG memiliki warning | Fishing Activity Index menurunkan skor 30%. |
| 6 | Gelombang tinggi | Wave lebih dari 1.25 m | Skor dikurangi penalti gelombang. |
| 7 | Gelombang sangat tinggi | Wave lebih dari 2.5 m | Skor turun signifikan dan label cenderung `Tidak Direkomendasikan`. |
| 8 | Angin kuat | Wind max lebih dari 20 knot | Skor dikurangi penalti angin. |
| 9 | Cuaca baik dan gelombang tenang | Cuaca clear/cloud dan wave rendah | Skor mendapat bonus kecil, maksimal tetap 100. |
| 10 | API gagal tetapi cache ada | Matikan koneksi setelah cache terbentuk | UI tetap menampilkan fallback cache. |
| 11 | Cache kosong dan API gagal | Bersihkan cache lalu matikan koneksi | UI menampilkan error/placeholder tanpa crash. |
| 12 | Maps spot card | Tap marker spot | Weather, BMKG, jarak, dan rekomendasi memakai koordinat spot. |
| 13 | Create Post | Buat post dengan lokasi aktif | Field weather dan kondisi perairan tersimpan sebagai ringkasan. |

## Checklist Dashboard

- Temperatur tampil dari OpenWeather.
- Lokasi tampil sesuai response weather atau fallback.
- BMKG menampilkan area perairan.
- Forecast horizontal muncul berdasarkan jumlah item dari API/cache.
- Rekomendasi spot berubah mengikuti jarak, cuaca, BMKG, rating.

## Checklist Maps

- Marker Firestore tampil.
- Search/filter tidak menghapus marker permanen.
- Spot card menampilkan jarak Haversine.
- Spot card mengambil Weather dan BMKG berdasarkan koordinat spot.
- Rekomendasi Maps memakai engine yang sama dengan Dashboard.

## Checklist Recommendation Engine

Formula yang harus divalidasi:

```text
Fishing Score = 0.30D + 0.25M + 0.20W + 0.15S + 0.10U
```

Validasi:

- Skor akhir tidak kurang dari 0.
- Skor akhir tidak lebih dari 100.
- Warning BMKG menurunkan skor.
- Gelombang/angin buruk menurunkan skor.
- Kondisi ideal memberi bonus kecil.
- Label sesuai rentang skor.

## Bukti Untuk Skripsi

Minimal screenshot yang perlu disiapkan:

- Dashboard saat data Weather dan BMKG berhasil.
- Dashboard saat forecast BMKG muncul.
- Maps dengan marker spot.
- Spot card dengan jarak dan rekomendasi.
- Create Post dengan weather dan kondisi perairan otomatis.
- Firestore `weather_cache`.
- Firestore `bmkg_cache`.
- Tabel hasil uji Recommendation Engine.

