# FISHING POINT UI/UX MICRO FIX - BOTTOM NAVIGATION & FLOATING ACTION BUTTON

Anda bekerja pada project Android Java **Fishing Point Tanjung Anom**.

WAJIB membaca dan mengikuti dua aturan UI berikut sebelum melakukan perubahan:

1. app/src/main/java/com/example/java3/readme/uiuxx.md
2. app/src/main/java/com/example/java3/readme/auidituiux.md

======================================================
TUJUAN
======================================================

Lakukan audit dan perbaikan pada Bottom Navigation dan Floating Action Button.

Prompt ini BUKAN untuk redesign.

Prompt ini BUKAN untuk membuat Bottom Navigation baru.

Prompt ini hanya memperbaiki implementasi existing agar kembali mengikuti seluruh Fishing Point UI Master Rule.

Semua perubahan WAJIB tetap:

✔ Responsive
✔ Adaptive
✔ Material Design 3
✔ Design System
✔ UI Kit
✔ Maintainable

======================================================
MASALAH YANG TERLIHAT
======================================================

Berdasarkan hasil pengujian pada perangkat nyata:

- Floating Action Button terlihat gepeng.
- Floating Action Button tampak terpotong.
- Bottom Navigation terlihat seperti memiliki dua layer.
- Terdapat area kosong besar di bawah Bottom Navigation.
- Bottom Navigation tidak menyatu secara visual dengan FAB.
- FAB tidak berada tepat di tengah cradle.
- Content screen terlihat kehilangan area karena Bottom Navigation.

Lihat screenshot sebagai referensi visual.

======================================================
ROOT CAUSE ANALYSIS (WAJIB)
======================================================

Sebelum mengubah kode,

WAJIB mencari penyebab sebenarnya.

Analisis file berikut:

- activity_main.xml
- MainActivity.java
- BottomAppBar
- BottomNavigationView
- FloatingActionButton
- CoordinatorLayout
- fragment container
- spacing.xml
- dimens.xml
- styles.xml
- themes.xml
- values.xml

Cari kemungkinan masalah berikut:

1.

BottomAppBar height
tidak sinkron dengan
BottomNavigationView height.

2.

FAB menggunakan
anchor atau gravity
yang salah.

3.

FAB memiliki
translationY
atau margin
yang menyebabkan terpotong.

4.

BottomAppBar cradle:

fabCradleMargin

fabCradleRoundedCornerRadius

fabCradleVerticalOffset

tidak proporsional.

5.

BottomNavigationView
dan BottomAppBar
sama-sama menggambar background
sehingga terlihat dua layer.

6.

CoordinatorLayout
memberi clipChildren
atau clipToPadding
yang menyebabkan FAB terpotong.

7.

Fragment Container
memberikan bottom padding
yang bertabrakan dengan
Bottom Navigation.

8.

WindowInsets
atau NavigationBarInsets
diterapkan dua kali.

9.

bottom_nav_height
dan bottom_nav_safe_padding
tidak menggunakan Design Token.

10.

Material Active Indicator
menyebabkan ilusi visual
seperti dua layer.

======================================================
TUGAS
======================================================

Lakukan audit terlebih dahulu.

Jangan langsung mengubah XML.

Setelah akar masalah ditemukan:

- Perbaiki hanya penyebabnya.
- Jangan memperbaiki gejalanya.

Pastikan:

FloatingActionButton:

- berbentuk lingkaran sempurna.
- diameter konsisten sesuai Material Design 3.
- elevation mengikuti Design Token.
- ripple tetap aktif.
- tidak gepeng.
- tidak clipping.
- tidak overlap.

BottomAppBar:

- tinggi konsisten.
- cradle proporsional.
- shadow konsisten.
- tidak menggambar background ganda.

BottomNavigationView:

- hanya satu layer.
- icon center.
- label center.
- active indicator tidak merusak visual.
- tidak memberi padding tambahan yang tidak diperlukan.

CoordinatorLayout:

- clipChildren benar.
- clipToPadding benar.
- anchor benar.
- layout behavior benar.

Fragment Container:

- tidak memiliki double bottom padding.
- tidak kehilangan area content.
- tidak overlap dengan Bottom Navigation.

======================================================
DESIGN SYSTEM RULE
======================================================

WAJIB menggunakan kembali:

spacing.xml

radius.xml

colors.xml

typography.xml

styles.xml

buttons.xml

cards.xml

theme.xml

Material Design Component existing.

Jangan membuat:

Bottom Navigation baru.

FloatingActionButton baru.

Drawable baru.

Style baru.

Dimen baru.

Shape baru.

Jika token yang sama sudah tersedia.

======================================================
RESPONSIVE RULE
======================================================

Bottom Navigation harus tetap benar pada:

5.5"

5.8"

6.1"

6.4"

6.7"

6.78"

Tablet

Landscape

Portrait

Split Screen

Tidak boleh:

FAB terpotong.

FAB gepeng.

Label keluar.

Icon keluar.

Bottom Navigation overlap.

Content tertutup.

Gap kosong besar.

Double layer.

======================================================
EXPECTED RESULT
======================================================

Bottom Navigation:

✔ hanya satu layer

✔ tinggi proporsional

✔ icon sejajar

✔ label sejajar

✔ tidak ada area kosong berlebihan

Floating Action Button:

✔ bulat sempurna

✔ berada tepat di tengah

✔ tidak clipping

✔ tidak gepeng

✔ mengikuti Material Design 3

✔ elevation konsisten

✔ shadow konsisten

Content:

✔ tidak tertutup Bottom Navigation

✔ responsive

✔ adaptive

✔ mengikuti Fishing Point Design System

======================================================
IMPLEMENTATION LIMIT
======================================================

Jangan mengubah:

Repository

ViewModel

Business Logic

API

Navigation Graph

Recommendation Engine

Firestore

Cloudinary

Google Maps

OpenWeather

BMKG

Open-Meteo

======================================================
VALIDASI WAJIB
======================================================

Setelah selesai:

1.

Build project.

2.

Install pada perangkat nyata.

3.

Periksa:

FAB

Bottom Navigation

Window Insets

Landscape

Portrait

4.

Pastikan tidak ada:

Layout Overflow

Double Layer

Text Clipping

Icon Clipping

Bottom Padding berlebih

5.

Pastikan implementasi tetap mengikuti seluruh Fishing Point UI Master Rule.