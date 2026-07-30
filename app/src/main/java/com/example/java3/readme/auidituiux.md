======================================================
FISHING POINT MICRO UI FIX RULE
======================================================

Seluruh pekerjaan pada prompt ini WAJIB mengikuti seluruh aturan pada:

Fishing Point UI Master Rule

Rule tersebut memiliki prioritas tertinggi.

Tidak boleh ada satu pun perubahan yang bertentangan dengan:

- Design Philosophy
- Design System
- Design Token
- Component Library
- Component Rule
- Responsive Rule
- Layout Rule
- Maintainability Rule
- Accessibility Rule
- Performance Rule

======================================================
OBJECTIVE
======================================================

Prompt ini BUKAN untuk melakukan redesign.

Prompt ini BUKAN untuk membuat UI baru.

Prompt ini hanya bertujuan memperbaiki Micro UI Issue tanpa mengubah identitas visual aplikasi.

Semua perubahan harus mempertahankan:

✔ Responsive
✔ Adaptive
✔ Material Design 3
✔ Design System
✔ UI Kit
✔ Maintainability

======================================================
MICRO UI ISSUE
======================================================

Contoh:

- Text clipping
- Text overlap
- RecyclerView item terlalu kecil
- RecyclerView item terlalu besar
- Card terpotong
- Image tidak proporsional
- Divider tidak sejajar
- Padding tidak konsisten
- Margin tidak konsisten
- FAB tidak simetris
- Bottom Navigation tidak proporsional
- BottomSheet terlalu tinggi
- Toolbar tidak sejajar
- Shadow tidak konsisten
- Elevation berbeda
- Radius berbeda
- Constraint conflict
- Baseline tidak sejajar

======================================================
ROOT CAUSE ANALYSIS
======================================================

Sebelum melakukan perubahan:

WAJIB mencari akar masalah.

Analisis:

- ConstraintLayout
- Guideline
- Barrier
- Flow
- RecyclerView
- Adapter Item
- CoordinatorLayout
- BottomAppBar
- FloatingActionButton
- Material Theme
- Design Token
- UI Component
- Parent Layout
- Clip Children
- Clip Padding
- Typography
- Dynamic Font

Jangan memperbaiki gejala.

Perbaiki penyebabnya.

======================================================
DESIGN SYSTEM RULE
======================================================

Semua perubahan WAJIB menggunakan:

FPTheme

FPSpacing

FPRadius

FPTypography

FPColor

FPCard

FPButton

FPBottomSheet

FPToolbar

FPBadge

FPComponent

Tidak boleh membuat:

style baru

drawable baru

shape baru

dimens baru

component baru

jika fungsi yang sama sudah tersedia.

======================================================
RESPONSIVE RULE
======================================================

Micro UI Fix WAJIB tetap memenuhi seluruh Responsive Rule pada Master Rule.

Tidak boleh menggunakan:

Hardcoded Width

Hardcoded Height

Negative Margin

Scale View

Absolute Position

layout_weight

Nested Layout baru

Semua solusi harus tetap:

Adaptive

Reusable

Responsive

Maintainable

======================================================
REGRESSION PREVENTION
======================================================

Perbaikan tidak boleh menyebabkan:

Screen lain berubah

Component lain berubah

Spacing berubah

Typography berubah

Theme berubah

Radius berubah

Elevation berubah

Navigation berubah

Bottom Sheet berubah

Card lain berubah

Perubahan hanya dilakukan pada bagian yang bermasalah.

======================================================
OUTPUT
======================================================

Untuk setiap Micro UI Fix:

1. Jelaskan akar masalah.

2. Jelaskan mengapa masalah terjadi.

3. Jelaskan aturan Master Rule yang digunakan.

4. Gunakan kembali Design System.

5. Gunakan kembali UI Kit.

6. Jangan membuat implementasi baru apabila sudah tersedia.

7. Lakukan perubahan seminimal mungkin.

8. Pastikan hasil tetap memenuhi seluruh Fishing Point UI Master Rule.