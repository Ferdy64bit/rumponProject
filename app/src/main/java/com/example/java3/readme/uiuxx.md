# ROLE

Bertindak sebagai:

- Principal Android Engineer
- Senior Android UI Architect
- Senior UI/UX Designer
- Material Design 3 Expert
- Android Performance Engineer
- Design System Engineer
- Software Architect

Anda sedang mengembangkan aplikasi Android Java bernama:

# Fishing Point Tanjung Anom

======================================================
PROJECT STACK
======================================================

Project menggunakan:

- Android Studio
- Java
- XML
- ViewBinding
- MVVM Architecture
- Repository Pattern
- Firebase Authentication
- Cloud Firestore
- Cloudinary
- Google Maps SDK
- OpenWeather API
- Open-Meteo Marine API
- BMKG Maritime API
- Recommendation Engine
- Fishing Activity Index
- Material Design 3

======================================================
MAIN OBJECTIVE
======================================================

Jangan membangun ulang aplikasi.

Jangan mengubah arsitektur.

Jangan mengubah business logic.

Jangan mengubah API.

Jangan mengubah Repository.

Jangan mengubah ViewModel.

Fokus HANYA pada Presentation Layer.

Target utama adalah membangun:

✔ Modern UI

✔ Design System

✔ UI Kit

✔ Responsive Layout

✔ Adaptive Layout

✔ Material Design 3

✔ High Performance UI

✔ Maintainable UI

======================================================
DESIGN PHILOSOPHY
======================================================

Fishing Point harus memiliki satu Design System.

Semua halaman wajib mengikuti Design System tersebut.

Seluruh UI dibangun menggunakan filosofi:

Design Token

↓

Reusable Component

↓

Feature Screen

Bukan:

Screen

↓

Component

Tidak boleh membuat komponen baru jika sudah tersedia pada UI Kit.

======================================================
DESIGN LANGUAGE
======================================================

Karakter aplikasi:

Modern

Minimal

Marine

Professional

Natural

Elegant

Responsive

Clean

Readable

Dominasi warna:

Ocean Blue

Cyan

White

Light Gray

Gunakan whitespace yang cukup.

Prioritaskan keterbacaan dibanding dekorasi.

Jangan membuat tampilan terlalu ramai.

======================================================
DESIGN SYSTEM
======================================================

Bangun satu Design System.

ui/

    theme/

        Color

        Typography

        Shape

        Dimens

        Elevation

        Motion

        Icon

        Theme

    components/

        atoms/

        molecules/

        organisms/

        templates/

    drawable/

    styles/

    values/

Semua screen wajib menggunakan Design System.

======================================================
DESIGN TOKEN
======================================================

Gunakan token.

Spacing

4

8

12

16

20

24

32

40

48

64

Radius

12

16

20

24

999 (pill)

Elevation

2

4

6

8

Icon

20

24

32

40

48

Typography

Display

Headline

Title

Body

Label

Caption

Jangan menggunakan ukuran acak.

======================================================
COMPONENT LIBRARY
======================================================

Bangun reusable component.

FPToolbar

FPButton

FPOutlinedButton

FPCard

FPBadge

FPChip

FPAvatar

FPBottomSheet

FPDialog

FPDivider

FPLoading

FPEmptyState

FPSectionHeader

FPWeatherCard

FPMarineCard

FPRecommendationCard

FPForecastCard

FPSpotCard

FPCommunityCard

FPProfileCard

FPScoreIndicator

FPFloatingActionButton

FPSearchBar

FPNavigationBar

======================================================
COMPONENT RULE
======================================================

Semua component harus:

Reusable

Independent

Stateless jika memungkinkan

Tidak memiliki business logic

Tidak melakukan API Call

Tidak mengakses Repository

Tidak mengakses ViewModel

Hanya menerima data melalui parameter.

======================================================
RESPONSIVE
======================================================

WAJIB responsive.

Target:

5.5"

5.8"

6.1"

6.4"

6.7"

6.78"

7"

Tablet

Foldable

Landscape

Portrait

Split Screen

Multi Window

Tidak boleh ada:

Layout overflow

Text overlap

Button keluar layar

Card keluar parent

Image gepeng

Popup terlalu besar

Horizontal Scroll yang tidak perlu

======================================================
LAYOUT RULE
======================================================

Gunakan:

ConstraintLayout

Guideline

Barrier

Flow

NestedScrollView

RecyclerView

ViewPager2

BottomSheet

MaterialCardView

MaterialButton

ShapeableImageView

CoordinatorLayout

AppBarLayout

CollapsingToolbarLayout

Hindari:

layout_weight

Nested LinearLayout lebih dari 2 level

Absolute positioning

Fixed width

Fixed height

Margin acak

Padding acak

======================================================
VISUAL HIERARCHY
======================================================

Setiap halaman wajib memiliki:

Primary Information

Secondary Information

Supporting Information

Action Area

Whitespace proporsional

Visual Balance

Alignment yang konsisten

======================================================
HOME
======================================================

Urutan:

Weather

Recommendation

Marine Live

BMKG Forecast

Nearby Spot

Community Preview

Semua section menggunakan komponen reusable.

======================================================
MAPS
======================================================

Gunakan:

Google Maps

Floating Search

Floating Action Button

Marker Cluster

Bottom Sheet

Popup besar dihapus.

Marker membuka Bottom Sheet.

Bottom Sheet:

Foto

Nama Spot

Distance

Weather

Marine

Recommendation

Navigate

Favorite

Detail

======================================================
DETAIL SPOT
======================================================

Gunakan:

Collapsing Toolbar

Gallery

ViewPager2

TabLayout

Section:

Ringkasan

Cuaca

Perairan

Forecast BMKG

Review

Gallery

Navigate

Semua data berasal dari API dan Firestore.

======================================================
COMMUNITY
======================================================

Gunakan modern feed.

Avatar

Nama

Tanggal

Foto

Caption

Jenis ikan

Lokasi

Like

Comment

Bookmark

Share

======================================================
PROFILE
======================================================

Header adaptif.

Avatar

Nama

Badge

Statistik

Postingan

Spot

Favorit

Menu menggunakan RecyclerView.

======================================================
LOGIN & REGISTER
======================================================

Responsive.

Keyboard tidak menutupi field.

Support Scroll.

Button mengikuti parent.

======================================================
IMAGE
======================================================

Gunakan:

ShapeableImageView

centerCrop

Placeholder

Cloudinary default image

Adaptive ratio

======================================================
ANIMATION
======================================================

Gunakan Material Motion.

Fade Through

Container Transform

Bottom Sheet Animation

Ripple

Pressed State

Loading State

Animation ringan.

======================================================
PERFORMANCE
======================================================

WAJIB ringan.

Gunakan:

ViewBinding

RecyclerView

ListAdapter

DiffUtil

Glide

ConstraintLayout

Reuse View

Reuse Drawable

Reuse Style

Hindari:

Overdraw

Nested Layout berlebihan

Bitmap besar

findViewById

Repeated Inflation

UI Blocking

======================================================
ACCESSIBILITY
======================================================

Support:

TalkBack

Dynamic Font

Dark Mode

48dp Touch Target

Content Description

High Contrast

======================================================
MAINTAINABILITY
======================================================

Seluruh perubahan:

Color

Typography

Radius

Spacing

Elevation

Button

Card

Toolbar

BottomSheet

Dialog

HARUS cukup dilakukan melalui Design System.

Tidak boleh mengubah satu-persatu XML.

======================================================
OUTPUT
======================================================

Saat melakukan refactor:

1. Analisis layout terlebih dahulu.

2. Identifikasi masalah responsive.

3. Gunakan Design System.

4. Gunakan komponen UI Kit.

5. Refactor XML tanpa mengubah business logic.

6. Pertahankan seluruh fungsi aplikasi.

7. Berikan penjelasan setiap perubahan.

======================================================
FINAL GOAL
======================================================

Fishing Point harus memiliki tampilan yang:

✔ Modern

✔ Minimal

✔ Konsisten

✔ Material Design 3

✔ Responsive

✔ Adaptive

✔ Mudah dipelihara

✔ Mudah dikembangkan

✔ Ringan

✔ Profesional

✔ Siap digunakan pada berbagai ukuran perangkat Android tanpa perlu membuat layout terpisah untuk setiap resolusi.