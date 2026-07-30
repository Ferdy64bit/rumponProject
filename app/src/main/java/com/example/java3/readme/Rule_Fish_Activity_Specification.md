# Dokumen Spesifikasi Formula & Simulasi Aktivitas Ikan (Fish Activity Score)
Dokumen ini menyajikan standarisasi matematika harian untuk memprediksi tingkat keaktifan ikan berdasarkan integrasi faktor Astronomi Solunar, Tekanan Udara, Suhu Lingkungan, dan Karakteristik Massa Air (Arus/Angin) harian.

---

## 📐 1. Rumus Utama (Core Mathematical Formula)

Formula akhir dirancang seimbang dengan total bobot variabel utama tepat bernilai **1.00 (100%)** ditambah koreksi pengkondisian lingkungan (*Modifiers*):

$$\\text{Fish Activity Score} = (0.35 \\times S) + (0.15 \\times W) + (0.25 \\times P) + (0.25 \\times M) + \\text{Bonus} - \\text{Penalti}$$

### Batasan Nilai Akhir (Boundary Conditions):
* Jika $\\text{Score} > 100$, maka $\\text{Score} = 100$ (Aktivitas Penuh/Agresif).
* Jika $\\text{Score} < 0$, maka $\\text{Score} = 0$ (Ikan Pasif Total).

---

## 📊 2. Kriteria & Standarisasi Skor Variabel Utama (Skala 0 - 100)

Sebelum dimasukkan ke dalam formula, setiap parameter mentah dari alat ukur atau prakiraan cuaca wajib dikonversi ke dalam skor internal (0-100) menggunakan standarisasi berikut:

### A. Solunar Score ($S$) - Bobot: 0.35
Menghitung pengaruh jam biologis internal ikan harian akibat tarikan gaya gravitasi bulan.
* **Skor 85 - 100 (Sangat Baik):** Tepat pada jendela waktu **Periode Mayor** (bulan berada di atas kepala atau sebaliknya di bawah pijakan bumi) dikombinasikan dengan Fase Bulan Baru (*New Moon*) atau Bulan Purnama (*Full Moon*).
* **Skor 55 - 84 (Baik):** Tepat pada jendela waktu **Periode Minor** (Bulan terbit atau Bulan terbenam).
* **Skor 15 - 54 (Rendah):** Di luar jam periode mayor/minor pada kondisi fase bulan sabit atau kuartal.

### B. Weather Score ($W$) - Bobot: 0.15
Mengukur kenyamanan pandangan visual ikan berdasarkan penetrasi sinar matahari menembus kolom air.
* **Skor 85 - 100 (Sangat Baik):** *Scattered clouds* (Cerah berawan), mendung tipis, atau gerimis kecil harian. Sinar matahari tidak terlalu terik menyengat mata ikan predator.
* **Skor 50 - 84 (Normal):** Langit cerah bersih (*Clear Sky*) pada pagi atau sore hari.
* **Skor 0 - 49 (Buruk):** Siang bolong terik menyengat tanpa awan, atau malam pekat tanpa cahaya bulan (untuk ikan visual predator).

### C. Pressure Score ($P$) - Bobot: 0.25
Mengukur kenyamanan fisik organ dalam ikan (Kantung renang / *Swim Bladder*).
* **Skor 85 - 100 (Sangat Baik):** Tekanan udara sedang turun perlahan (*Gradual Barometric Drop*) di bawah rata-rata normal (kisaran **1006 hPa - 1009 hPa**). Ini tanda badai/hujan akan datang, memicu nafsu makan ikan secara masif.
* **Skor 65 - 84 (Normal):** Tekanan stabil di angka rata-rata bumi (**1011 hPa - 1014 hPa**).
* **Skor 0 - 64 (Buruk):** Tekanan udara melonjak naik sangat tinggi pasca badai berlalu (*Post-Cold Front High Pressure*).

### D. Water Movement Score ($M$) - Bobot: 0.25
Menilai pergerakan sirkulasi massa air pembawa nutrisi pakan serta ketersediaan oksigen terlarut (*Dissolved Oxygen*).
* **Skor 85 - 100 (Sangat Baik):** Kecepatan arus laut aktif berkisar **0.7 m/s - 1.2 m/s** dengan tinggi gelombang rendah harian (**0.4 m - 0.7 m**).
* **Skor 50 - 84 (Normal):** Arus lambat atau kondisi menjelang air mati harian (*Slack Water*).
* **Skor 0 - 49 (Buruk):** Air mati total tanpa pergerakan sirkulasi, atau sebaliknya terjadi arus ekstrem/banjir bandang berlumpur pekat.

---

## ⚡ 3. Aturan Modifikator Lingkungan (Bonus & Penalti Konstan)

Nilai *Modifiers* langsung ditambahkan atau dikurangi dari total perhitungan skor sub-variabel utama:

### 🎁 Modifikator Bonus (Penambah Nilai)
1. **Fajar / Senja Alami (+15 Poin):** Berlaku spesifik hanya pada pukul **04:30 - 06:00 WIB** (Fajar) dan **17:00 - 18:30 WIB** (Senja). Ini adalah puncak insting berburu harian bagi ikan predator.
2. **Kondisi Cuaca Stabil (+10 Poin):** Diberikan jika grafik tekanan udara dan suhu air laut bertahan konstan tanpa anomali ekstrem selama 48-72 jam ke belakang.

### ⚠️ Modifikator Penalti (Pengurang Nilai)
1. **Angin Kencang / Waspada BMKG (-10 hingga -40 Poin):**
   * Kecepatan angin permukaan **15 km/jam - 25 km/jam**: Potong **-10 Poin** (Riak mengganggu akurasi senar).
   * Kecepatan angin permukaan **> 35 km/jam** atau status badai aktif: Potong **-40 Poin** (Ikan menyelam ke dasar laut terdalam untuk berlindung, berhenti makan total).
2. **Penalti Kegelapan Malam (-15 Poin):** Hanya dipotong untuk tipe ikan predator siang (*Diurnal visual feeders*). *Catatan: Berubah menjadi Bonus +10 jika target memancing adalah jenis ikan nokturnal (misal: Kakap Putih/Barramundi atau Lele).*

---

## 📝 4. Kasus Simulasi Nyata (Real Case Study)

### Kondisi Lapangan Terukur (Berdasarkan Data Gambar):
* **Lokasi Analisis:** Sepatan, Tangerang Perairan Pantai.
* **Kondisi Cuaca:** *Scattered clouds*, Suhu udara 28°C $\\rightarrow$ **Skor internal $W = 85$**
* **Tekanan Udara:** 1008 hPa $\\rightarrow$ **Skor internal $P = 75$**
* **Massa Air:** Arus Laut 0.9 m/s, Gelombang rendah 0.6 m, Suhu air hangat 29°C $\\rightarrow$ **Skor internal $M = 80$**
* **Status Kecepatan Angin:** 24.2 km/jam $\\rightarrow$ **Dikenakan Penalti Angin Kencang = -10 Poin**

### Skenario Perhitungan Atau testing Berdasarkan 3 Fase Waktu Solunar:

#### 🔴 Skenario 1: Di luar jam makan harian (Solunar Rendah = 20)
* $\\text{Subtotal Utama} = (0.35 \\times 20) + (0.15 \\times 85) + (0.25 \\times 75) + (0.25 \\times 80) = 7 + 12.75 + 18.75 + 20 = 58.5$
* $\\text{Skor Akhir} = 58.5 - 10 \\text{ (Penalti Angin)} = \\mathbf{48.5\\%}$
* *Kesimpulan:* Ikan cenderung pasif, disarankan memancing di area dasar perairan dekat struktur karang.

#### 🟡 Skenario 2: Jam makan sekunder (Solunar Sedang = 60 pada Periode Minor)
* $\\text{Subtotal Utama} = (0.35 \\times 60) + (0.15 \\times 85) + (0.25 \\times 75) + (0.25 \\times 80) = 21 + 12.75 + 18.75 + 20 = 72.5$
* $\\text{Skor Akhir} = 72.5 - 10 \\text{ (Penalti Angin)} = \\mathbf{62.5\\%}$
* *Kesimpulan:* Kondisi bagus potensial, ikan mulai aktif keluar mencari mangsa.

#### 🟢 Skenario 3: Jam makan utama puncak (Solunar Tinggi = 90 pada Periode Mayor Sore Hari)
* $\\text{Subtotal Utama} = (0.35 \\times 90) + (0.15 \\times 85) + (0.25 \\times 75) + (0.25 \\times 80) = 31.5 + 12.75 + 18.75 + 20 = 83$
* $\\text{Skor Akhir} = 83 - 10 \\text{ (Penalti Angin)} = \\mathbf{73.0\\%}$
* *Kesimpulan:* Aktivitas ikan sangat tinggi/agresif. Walaupun angin bertiup kencang, riak air membantu menyamarkan bayangan senar pancing Anda dari mata ikan predator permukaan.
