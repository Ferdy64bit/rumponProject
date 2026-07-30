# SQA Phase 7 - Black Box Test Plan

Tanggal: 26 Juli 2026

Project: **Fishing Point Tanjung Anom**

Status: **Perencanaan black box test tanpa implementasi kode**

Dokumen acuan:

- `masterruleoop.md`
- `sqa-phase-1-project-audit-2026-07-26.md`
- `sqa-phase-2-oop-solid-review-2026-07-26.md`
- `sqa-phase-3-refactor-plan-2026-07-26.md`
- `sqa-phase-4-business-logic-audit-2026-07-26.md`
- `sqa-phase-5-unit-test-candidate-2026-07-26.md`
- `sqa-phase-6-unit-test-plan-2026-07-26.md`

## Tujuan Phase 7

Phase ini menyusun rencana black box testing untuk seluruh fitur utama aplikasi. Pengujian dilakukan dari sudut pandang pengguna tanpa melihat kode internal.

Black box test ini penting karena aplikasi bergantung pada:

- device Android nyata,
- Firebase Authentication,
- Cloud Firestore,
- GPS/location permission,
- Google Maps,
- Cloudinary upload,
- OpenWeather API,
- BMKG API,
- Open-Meteo Marine API,
- koneksi internet.

Phase ini belum melakukan testing aktual dan belum mengubah kode.

## Format Pengujian

Setiap test menggunakan format:

| Field | Keterangan |
| --- | --- |
| ID | Kode test case. |
| Nama Fitur | Modul yang diuji. |
| Skenario | Kondisi pengujian. |
| Input/Langkah | Langkah pengguna. |
| Expected Result | Hasil yang diharapkan. |
| Actual Result | Diisi saat testing aktual. |
| Status | PASS/FAIL/BLOCKED. |
| Bukti | Screenshot/log jika perlu. |

## Test Environment

Perangkat utama:

- Device Android real: TECNO LJ8k atau device lain yang dipakai user.

Kondisi yang perlu diuji:

- Internet aktif.
- Internet mati.
- GPS aktif.
- GPS mati.
- Permission lokasi diizinkan.
- Permission lokasi ditolak.
- Akun owner.
- Akun non-owner.
- Foto portrait.
- Foto landscape.

## Data Uji Minimum

Minimal data yang perlu tersedia saat pengujian:

- Akun A: owner spot dan post.
- Akun B: non-owner.
- Minimal 1 spot `PUBLIC`.
- Minimal 1 spot `PRIVATE` milik Akun A.
- Minimal 1 post community dengan foto.
- Minimal 1 favorite spot.
- Minimal 1 comment pada post.

## Authentication Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AUTH-001 | Login | Login akun valid | Masukkan email/password valid, tap login | User masuk ke Dashboard |  |  |  |
| AUTH-002 | Login | Password salah | Masukkan email valid dan password salah | Aplikasi menampilkan pesan error yang mudah dipahami |  |  |  |
| AUTH-003 | Register | Register akun baru | Isi nama, email baru, password valid | Akun dibuat dan user diarahkan sesuai flow aplikasi |  |  |  |
| AUTH-004 | Register | Email sudah terdaftar | Isi email yang sudah ada | Aplikasi menampilkan pesan email sudah digunakan |  |  |  |
| AUTH-005 | Session | Buka ulang aplikasi setelah login | Tutup app, buka ulang | User tetap login atau diarahkan sesuai session Firebase |  |  |  |
| AUTH-006 | Logout | Logout dari Profile | Tap logout dan konfirmasi | User keluar ke halaman login |  |  |  |
| AUTH-007 | Reset Password | Reset password via email | Buka Profile/Keamanan, kirim reset password | Email reset terkirim; user diberi info cek Inbox/Spam |  |  |  |

## Dashboard Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| DASH-001 | Dashboard | Load normal dengan GPS dan internet aktif | Buka app setelah login | Weather, lokasi, rekomendasi, marine live, BMKG forecast, spot terdekat tampil |  |  |  |
| DASH-002 | Dashboard Location | Lokasi user aktif | Izinkan permission lokasi | Dashboard memakai lokasi user, bukan default Teluk Jakarta kecuali fallback diperlukan |  |  |  |
| DASH-003 | Dashboard Permission | Permission lokasi ditolak | Tolak permission lokasi | App tidak crash dan memakai fallback dengan pesan jelas |  |  |  |
| DASH-004 | Weather | Cuaca live tampil | Refresh Dashboard | Suhu, kelembapan, tekanan, angin, dan deskripsi cuaca tampil |  |  |  |
| DASH-005 | Marine Chart | Grafik gelombang hourly tampil | Buka section kondisi perairan live | Grafik tidak kosong, tidak terpotong, dan label gelombang tampil |  |  |  |
| DASH-006 | BMKG Forecast | Forecast BMKG tampil | Scroll Dashboard ke forecast BMKG | List forecast horizontal tampil dan teks tidak terpotong fatal |  |  |  |
| DASH-007 | Recommendation | Safety dan aktivitas ikan tampil | Lihat card rekomendasi | Score, safety, dan aktivitas ikan tampil responsif |  |  |  |
| DASH-008 | Nearby Spot | Spot terdekat tampil | Lihat section spot terdekat | Spot terdekat tampil berdasarkan jarak, bukan bintang massal berat |  |  |  |
| DASH-009 | Offline | Internet mati | Matikan internet lalu refresh | App tidak crash; cache/fallback/error state tampil |  |  |  |

## Maps Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MAP-001 | Maps | Buka halaman map | Tap menu Peta | Google Maps tampil dan marker Firestore muncul |  |  |  |
| MAP-002 | Marker | Tap marker public | Tap salah satu marker | Card/bottom info spot tampil dengan nama, tipe, jarak, cuaca/perairan |  |  |  |
| MAP-003 | Marker Environment | Data marker memakai koordinat spot | Tap beberapa marker berbeda | Weather/marine/BMKG mengikuti koordinat spot, bukan selalu lokasi user |  |  |  |
| MAP-004 | Search | Cari marker | Ketik nama/tipe spot | Marker/list terfilter sesuai query |  |  |  |
| MAP-005 | Filter | Filter tipe spot | Pilih Pantai/Muara/Dermaga/Bagan/Rumpon | Marker sesuai tipe terpilih |  |  |  |
| MAP-006 | Polyline | Garis user-ke-spot | Tap marker | Polyline muncul dari user ke spot sebagai route visual |  |  |  |
| MAP-007 | Google Navigation | Navigasi Google Maps | Tap tombol navigasi | Google Maps terbuka ke koordinat spot atau fallback web maps |  |  |  |
| MAP-008 | My Location | Center lokasi user | Tap tombol lokasi | Kamera map bergerak ke lokasi user |  |  |  |
| MAP-009 | Map Type | Ganti tipe map | Tap tombol map type | Tipe peta berubah normal/hybrid/satellite/terrain |  |  |  |
| MAP-010 | Permission Denied | Lokasi ditolak | Tolak permission lokasi | App tidak crash dan fitur map tetap bisa digunakan terbatas |  |  |  |

## Spot CRUD dan Visibility Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| SPOT-001 | Tambah Spot | Tambah spot private | Long press map, isi nama/tipe/koordinat, pilih Pribadi | Spot tersimpan sebagai `PRIVATE` dan terlihat oleh owner |  |  |  |
| SPOT-002 | Tambah Spot | Tambah spot public | Long press map, isi data, pilih Publik | Spot tersimpan sebagai `PUBLIC` dan terlihat di map |  |  |  |
| SPOT-003 | Edit Spot | Owner edit spot | Login Akun A, tap spot milik sendiri, edit data | Data spot berubah dan tidak crash |  |  |  |
| SPOT-004 | Delete Spot | Owner hapus spot | Login Akun A, hapus spot milik sendiri | Spot hilang dari map/list |  |  |  |
| SPOT-005 | Owner Only | Non-owner melihat spot public | Login Akun B, tap spot Akun A | Tombol edit/hapus tidak tampil |  |  |  |
| SPOT-006 | Private Visibility | Non-owner tidak melihat private spot | Login Akun B setelah Akun A membuat private spot | Spot private Akun A tidak tampil |  |  |  |
| SPOT-007 | Change Access | Owner ubah private ke public | Edit visibility spot | Spot menjadi terlihat sesuai akses baru |  |  |  |
| SPOT-008 | Spot Type | Tipe Bagan/Rumpon tersedia | Tambah/edit spot | Pilihan Bagan dan Rumpon tersedia |  |  |  |

## Detail Spot Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| DETAIL-001 | Detail Spot | Buka detail dari Map | Tap marker, tap detail | Detail spot terbuka dengan data spot benar |  |  |  |
| DETAIL-002 | Live Data | Data environment detail | Buka detail spot | Cuaca, angin, kelembapan, gelombang, safety, aktivitas ikan tampil |  |  |  |
| DETAIL-003 | Favorite | Simpan favorite | Tap simpan favorite | Status favorite berubah dan tersimpan |  |  |  |
| DETAIL-004 | Unfavorite | Hapus favorite | Tap favorite lagi | Status favorite hilang |  |  |  |
| DETAIL-005 | Share | Bagikan spot | Tap share | Share sheet muncul dengan info spot |  |  |  |
| DETAIL-006 | Navigation | Navigasi ke spot | Tap navigasi | Google Maps/fallback terbuka |  |  |  |
| DETAIL-007 | Photo Owner | Owner ganti foto spot | Login owner, pilih foto galeri | Foto spot terupload Cloudinary dan tampil |  |  |  |
| DETAIL-008 | Photo Delete | Owner hapus foto spot | Tap hapus foto | Foto kembali ke default tipe spot |  |  |  |
| DETAIL-009 | Non Owner | Non-owner buka detail | Login Akun B, buka detail spot Akun A | Kontrol owner-only tidak tersedia |  |  |  |

## Recommendation Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| REC-001 | Recommendation | Spot dekat kondisi baik | Buka Dashboard/Detail spot dekat | Score dan label layak/recommended sesuai kondisi |  |  |  |
| REC-002 | Recommendation | Spot jauh | Buka spot sangat jauh | Score tidak terlalu tinggi karena distance cap |  |  |  |
| REC-003 | Safety | Gelombang tinggi | Uji koordinat/periode data gelombang tinggi jika tersedia | Safety turun dan label lebih waspada |  |  |  |
| REC-004 | Fish Activity | Aktivitas ikan tampil | Buka Dashboard/Detail | Label aktivitas ikan tampil dan tidak kosong |  |  |  |
| REC-005 | Data Null/Fallback | API lambat/gagal | Kondisi jaringan lambat/mati | App tidak crash dan skor tetap punya fallback aman |  |  |  |

## Community Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| COM-001 | Feed | Buka Community | Tap menu Komunitas | Feed tampil dan tidak crash |  |  |  |
| COM-002 | Create Post | Buat post foto portrait | Pilih foto portrait, isi caption, optional jenis ikan/lokasi | Post berhasil dibuat dan foto tampil proporsional |  |  |  |
| COM-003 | Create Post | Buat post foto landscape | Pilih foto landscape | Preview dan feed tetap responsive |  |  |  |
| COM-004 | Optional Field | Jenis ikan/lokasi kosong | Buat post hanya caption/foto | Post tetap bisa dibuat jika field opsional kosong |  |  |  |
| COM-005 | Like | Like post | Tap like | Like count naik dan icon berubah |  |  |  |
| COM-006 | Unlike | Unlike post | Tap like lagi | Like count turun dan icon normal |  |  |  |
| COM-007 | Comment | Tambah comment | Isi komentar, kirim | Komentar muncul di post |  |  |  |
| COM-008 | Bookmark | Simpan post | Tap bookmark | Status bookmark tersimpan setelah reload/feed refresh |  |  |  |
| COM-009 | Share | Share post | Tap share | Share sheet muncul |  |  |  |
| COM-010 | Delete | Owner delete post | Hapus post sendiri | Post hilang dan data terkait dibersihkan sesuai batas client |  |  |  |
| COM-011 | Avatar Sync | Foto profile berubah | Update foto profile lalu buka community | Avatar post user mengikuti foto terbaru |  |  |  |
| COM-012 | Upload Failure | Internet mati saat upload | Matikan internet lalu create post | App menampilkan error, tidak crash |  |  |  |

## Profile Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| PROF-001 | Profile | Buka profile | Tap menu Profil | Data user, avatar, statistik, menu tampil |  |  |  |
| PROF-002 | Edit Profile | Ubah nama/bio | Edit profile lalu simpan | Data profile berubah |  |  |  |
| PROF-003 | Upload Photo | Ganti foto profile | Pilih foto dari galeri | Foto terupload Cloudinary dan tampil |  |  |  |
| PROF-004 | My Posts | Buka Postingan Saya | Tap menu Postingan Saya | Post milik user tampil tanpa error index |  |  |  |
| PROF-005 | My Spots | Buka Spot Saya | Tap menu Spot Saya | Spot yang dibuat user tampil |  |  |  |
| PROF-006 | Favorite Spots | Buka Spot Favorit | Simpan spot favorit lalu buka Profile | Spot favorit tampil |  |  |  |
| PROF-007 | Reset Password | Kirim reset password | Tap reset password | Email terkirim dan pesan cek Inbox/Spam tampil |  |  |  |
| PROF-008 | Logout | Logout | Tap logout | User keluar ke Login |  |  |  |

## Weather, BMKG, dan Marine API Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| API-001 | OpenWeather | Cuaca lokasi user | Buka Dashboard | Weather live tampil sesuai lokasi |  |  |  |
| API-002 | BMKG | Area selector BMKG | Buka Dashboard/Map di koordinat pesisir | Area BMKG sesuai polygon terdekat |  |  |  |
| API-003 | BMKG Forecast | Forecast harian/esok | Buka list BMKG forecast | Forecast tampil dan warning terbaca jika ada |  |  |  |
| API-004 | Open-Meteo Marine | Gelombang hourly | Buka Dashboard/Detail | Current wave dan max 24h tampil |  |  |  |
| API-005 | Cache | API gagal setelah pernah berhasil | Matikan internet setelah cache ada | Cache/fallback tampil tanpa crash |  |  |  |
| API-006 | Non Coastal | Koordinat non-pesisir | Uji lokasi jauh dari laut | App tidak crash dan menampilkan fallback/empty state aman |  |  |  |

## Notification Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| NOTIF-001 | Notification Icon | Tidak ada notifikasi | Tap icon notifikasi | Pesan tidak ada notifikasi baru tampil |  |  |  |
| NOTIF-002 | Notification Count | Ada unread count jika data tersedia | Siapkan data notifikasi jika fitur aktif | Content description/count sesuai data |  |  |  |

Catatan: Notification bukan fitur inti yang matang. Jika belum lengkap, catat sebagai fitur pendukung atau saran pengembangan.

## Navigation dan Regression Test Plan

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| NAV-001 | Bottom Navigation | Pindah menu | Tap Dashboard, Map, Community, Profile | Semua menu terbuka tanpa crash |  |  |  |
| NAV-002 | FAB Create Post | Tap FAB utama | Tap tombol tambah tengah | Create Post terbuka |  |  |  |
| NAV-003 | Back Navigation | Back dari detail/create | Tekan back | Kembali ke screen sebelumnya tanpa state rusak |  |  |  |
| NAV-004 | App Relaunch | Buka ulang aplikasi | Force close lalu buka | App masuk ke screen sesuai session |  |  |  |
| NAV-005 | Waiting Debugger Regression | Launch manual | Buka app dari launcher | Tidak muncul Waiting For Debugger |  |  |  |

## Responsive UI Smoke Test

UI sudah final, tetapi tetap perlu smoke test agar tidak ada clipping fatal.

| ID | Nama Fitur | Skenario | Input/Langkah | Expected Result | Actual Result | Status | Bukti |
| --- | --- | --- | --- | --- | --- | --- | --- |
| UI-001 | Dashboard | Scroll seluruh Dashboard | Buka Dashboard, scroll atas-bawah | Tidak ada text/card terpotong fatal |  |  |  |
| UI-002 | Map Card | Tap marker | Lihat card/bottom info | Text jarak, cuaca, perairan tidak overlap |  |  |  |
| UI-003 | Detail Spot | Scroll Detail | Buka detail spot | Card tidak saling tindih |  |  |  |
| UI-004 | Community Feed | Foto portrait/landscape | Buat/lihat post | Gambar proporsional, caption terbaca |  |  |  |
| UI-005 | Profile | Buka menu profile | Lihat statistik dan menu | Postingan/spot/favorit tidak terpotong |  |  |  |
| UI-006 | Bottom Nav/FAB | Navigasi bawah | Amati bottom nav | Tidak gepeng, tidak double layer, FAB proporsional |  |  |  |

## Urutan Eksekusi Black Box Test

Urutan test yang disarankan:

1. AUTH-001 sampai AUTH-007.
2. DASH-001 sampai DASH-009.
3. MAP-001 sampai MAP-010.
4. SPOT-001 sampai SPOT-008.
5. DETAIL-001 sampai DETAIL-009.
6. REC-001 sampai REC-005.
7. COM-001 sampai COM-012.
8. PROF-001 sampai PROF-008.
9. API-001 sampai API-006.
10. NAV-001 sampai NAV-005.
11. UI-001 sampai UI-006.

## Minimal Black Box Test Untuk Skripsi

Jika waktu terbatas, minimal test yang wajib dilakukan:

1. Login berhasil.
2. Dashboard menampilkan Weather/BMKG/Marine/rekomendasi.
3. Maps menampilkan marker Firestore.
4. Tap marker menampilkan data environment sesuai koordinat spot.
5. Haversine jarak user-ke-spot tampil.
6. Tambah spot private dan public.
7. Non-owner tidak bisa edit/hapus spot owner lain.
8. Detail spot menampilkan live data dan favorite.
9. Community create post dengan foto.
10. Like/comment/bookmark/share post.
11. Profile edit dan upload foto.
12. Reset password via email.
13. App tidak crash saat internet/GPS bermasalah.

## Bukti Yang Perlu Dikumpulkan

Untuk BAB IV dan demo sidang, simpan bukti berikut:

- Screenshot Login/Register.
- Screenshot Dashboard final.
- Screenshot grafik gelombang hourly.
- Screenshot BMKG forecast.
- Screenshot Maps marker dan polyline.
- Screenshot Detail Spot live data.
- Screenshot tambah/edit spot private-public.
- Screenshot akun non-owner tanpa tombol edit/hapus.
- Screenshot Community feed dan Create Post.
- Screenshot komentar/bookmark/like.
- Screenshot Profile dan Spot Favorit.
- Screenshot reset password info cek Inbox/Spam.
- Log penting jika API/area BMKG perlu dibuktikan.

## Kriteria PASS Umum

Test dianggap PASS jika:

- fitur berjalan sesuai expected result,
- aplikasi tidak crash,
- tidak ada UI fatal clipping/overlap,
- data tersimpan atau terbaca sesuai flow,
- pesan error mudah dipahami saat gagal,
- tidak ada akses owner-only yang bocor ke user lain.

## Kriteria FAIL Umum

Test dianggap FAIL jika:

- aplikasi force close,
- data salah sumber, misalnya marker memakai lokasi user padahal harus koordinat spot,
- private spot terlihat oleh non-owner,
- non-owner bisa edit/hapus spot owner lain,
- upload berhasil tetapi URL tidak tersimpan,
- favorite/bookmark tidak sinkron,
- UI utama tidak dapat dibaca karena clipping/overlap,
- reset password tidak memberi informasi jelas.

## Keputusan Phase 7

Project sudah memiliki black box test plan yang cukup lengkap untuk masuk ke **Phase 8 - Implementasi Bertahap**.

Sesuai master rule, Phase 8 baru boleh dilakukan setelah audit, review, planning, unit test plan, dan black box plan selesai. Implementasi nanti harus dilakukan satu class atau satu bagian kecil dalam satu waktu dengan urutan:

1. Audit class.
2. Refactor ringan jika diperlukan.
3. Pastikan fitur tidak berubah.
4. Buat unit test.
5. Jalankan test.
6. Pastikan PASS.
7. Catat dokumentasi perubahan.

## Catatan Untuk Skripsi

Black box plan ini dapat langsung diadaptasi menjadi tabel pengujian BAB IV. Kolom `Actual Result`, `Status`, dan `Bukti` diisi setelah testing aktual pada device.
