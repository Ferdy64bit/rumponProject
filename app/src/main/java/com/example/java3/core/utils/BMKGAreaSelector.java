package com.example.java3.core.utils;

public class BMKGAreaSelector {
    private BMKGAreaSelector() {
    }

    public static String resolveMarineArea(double lat, double lon) {
        if (!isValidCoordinate(lat, lon)) {
            return Constants.BMKG_DEFAULT_MARINE_AREA;
        }

        // Java Sea and Sunda Strait.
        if (isInside(lat, lon, -6.25, -5.45, 105.75, 106.45)) return "F.07_Perairan%20utara%20Banten";
        if (isInside(lat, lon, -6.35, -5.65, 106.35, 107.25)) return "F.09_Teluk%20Jakarta";
        if (isInside(lat, lon, -6.65, -5.85, 107.20, 108.10)) return "F.10_Perairan%20Karawang%20-%20Subang";
        if (isInside(lat, lon, -6.85, -5.95, 108.05, 109.00)) return "F.11_Perairan%20Indramayu%20-%20Cirebon";
        if (isInside(lat, lon, -7.10, -6.20, 105.20, 106.60)) return "U.04_Samudera%20Hindia%20selatan%20Banten";
        if (isInside(lat, lon, -7.70, -6.70, 106.40, 108.10)) return "H.01_Perairan%20Sukabumi%20-%20Cianjur";
        if (isInside(lat, lon, -7.90, -6.95, 107.60, 108.80)) return "H.02_Perairan%20Garut%20-%20Pangandaran";
        if (isInside(lat, lon, -8.20, -6.95, 108.15, 109.05)) return "H.04_Perairan%20Cilacap";
        if (isInside(lat, lon, -8.20, -7.00, 109.00, 109.80)) return "H.05_Perairan%20Kebumen%20-%20Purworejo";
        if (isInside(lat, lon, -8.10, -7.10, 109.80, 110.70)) return "H.06_Perairan%20Yogyakarta";
        if (isInside(lat, lon, -8.80, -7.20, 110.70, 112.20)) return "I.13_Perairan%20selatan%20Jawa%20Timur";
        if (isInside(lat, lon, -9.20, -7.50, 112.20, 114.60)) return "I.14_Samudera%20Hindia%20selatan%20Jawa%20Timur";
        if (isInside(lat, lon, -7.05, -5.90, 109.00, 110.50)) return "G.05_Perairan%20Pekalongan%20-%20Kendal";
        if (isInside(lat, lon, -7.05, -5.90, 110.10, 111.10)) return "G.06_Perairan%20Semarang%20-%20Demak";
        if (isInside(lat, lon, -7.10, -5.80, 110.75, 111.90)) return "G.07_Perairan%20Jepara";
        if (isInside(lat, lon, -7.25, -6.45, 111.20, 112.40)) return "I.06_Perairan%20Tuban%20-%20Lamongan";
        if (isInside(lat, lon, -7.70, -6.55, 111.90, 113.50)) return "I.07_Perairan%20Gresik%20-%20Surabaya";

        // Sumatra and nearby islands.
        if (isInside(lat, lon, 4.00, 6.40, 94.80, 96.80)) return "A.03_Perairan%20Sabang%20-%20Banda%20Aceh";
        if (isInside(lat, lon, 2.00, 5.60, 94.60, 97.80)) return "A.06_Perairan%20barat%20Aceh";
        if (isInside(lat, lon, 0.50, 3.20, 96.70, 99.40)) return "A.09_Perairan%20Kep.%20Nias%20-%20Sibolga";
        if (isInside(lat, lon, -2.50, 0.60, 98.00, 101.50)) return "B.01_Perairan%20barat%20Sumatera%20Barat";
        if (isInside(lat, lon, -4.90, -2.30, 100.00, 103.50)) return "B.10_Perairan%20barat%20Bengkulu";
        if (isInside(lat, lon, -6.25, -4.60, 102.80, 105.80)) return "C.01_Perairan%20barat%20Lampung";
        if (isInside(lat, lon, -6.20, -4.70, 104.80, 106.20)) return "C.03_Teluk%20Lampung%20bagian%20selatan";
        if (isInside(lat, lon, -5.80, -3.80, 105.00, 106.40)) return "C.06_Perairan%20timur%20Lampung%20bagian%20selatan";
        if (isInside(lat, lon, -1.30, 2.30, 100.00, 103.80)) return "E.01_Perairan%20Riau";
        if (isInside(lat, lon, -0.60, 1.60, 103.20, 105.60)) return "E.02_Perairan%20Kep.%20Batam";

        // Kalimantan.
        if (isInside(lat, lon, -0.80, 1.30, 108.00, 110.30)) return "D.10_Perairan%20Pontianak%20-%20Mempawah";
        if (isInside(lat, lon, -3.80, -1.00, 108.60, 111.40)) return "G.01_Perairan%20Kalimantan%20Tengah%20bagian%20barat";
        if (isInside(lat, lon, -3.80, -1.20, 111.10, 114.40)) return "I.01_Perairan%20Kalimantan%20Tengah%20bagian%20timur";
        if (isInside(lat, lon, -1.90, 0.40, 116.00, 117.90)) return "M.03_Perairan%20Balikpapan";
        if (isInside(lat, lon, 2.40, 4.60, 116.50, 118.80)) return "M.09_Perairan%20Kalimantan%20Utara";

        // Bali, NTB, NTT.
        if (isInside(lat, lon, -8.95, -8.00, 114.20, 115.90)) return "J.01_Laut%20Bali";
        if (isInside(lat, lon, -9.20, -8.20, 114.70, 116.30)) return "J.06_Samudera%20Hindia%20selatan%20Bali";
        if (isInside(lat, lon, -8.85, -7.90, 115.70, 116.70)) return "J.07_Selat%20Lombok%20bagian%20utara";
        if (isInside(lat, lon, -9.25, -8.30, 115.70, 116.80)) return "J.05_Selat%20Lombok%20bagian%20selatan";
        if (isInside(lat, lon, -9.30, -7.80, 116.50, 119.30)) return "J.08_Laut%20Sumbawa";
        if (isInside(lat, lon, -9.20, -7.40, 119.00, 122.80)) return "K.01_Perairan%20utara%20Flores";
        if (isInside(lat, lon, -11.00, -9.00, 122.50, 124.60)) return "K.13_Perairan%20selatan%20Kupang%20-%20P.%20Rote";
        if (isInside(lat, lon, -10.20, -8.50, 123.40, 125.60)) return "K.12_Perairan%20utara%20Kupang%20-%20P.%20Rote";

        // Sulawesi.
        if (isInside(lat, lon, -6.20, -3.80, 118.60, 120.40)) return "L.05_Perairan%20Spermonde%20Makassar%20bagian%20barat";
        if (isInside(lat, lon, -3.80, -1.00, 118.40, 119.90)) return "M.05_Perairan%20Sulawesi%20Barat";
        if (isInside(lat, lon, -1.60, 1.40, 119.00, 121.50)) return "M.08_Perairan%20barat%20Sulawesi%20Tengah";
        if (isInside(lat, lon, 0.60, 2.40, 120.00, 125.50)) return "N.01_Perairan%20utara%20Sulawesi%20Utara";
        if (isInside(lat, lon, 0.60, 1.90, 124.00, 126.00)) return "N.08_Perairan%20Bitung%20-%20Likupang";
        if (isInside(lat, lon, -5.80, -2.60, 121.40, 123.80)) return "S.05_Perairan%20Manui%20Kendari%20bagian%20barat";
        if (isInside(lat, lon, -5.80, -2.60, 123.60, 125.80)) return "S.06_Perairan%20Manui%20Kendari%20bagian%20timur";

        // Maluku and Papua.
        if (isInside(lat, lon, -4.80, -2.40, 127.00, 129.50)) return "T.04_Perairan%20P.%20Ambon%20-%20Kep.%20Lease";
        if (isInside(lat, lon, -7.80, -4.80, 130.00, 133.80)) return "T.14_Perairan%20Kep.%20Kai";
        if (isInside(lat, lon, -8.80, -5.00, 133.00, 136.50)) return "T.15_Perairan%20Kep.%20Aru";
        if (isInside(lat, lon, -1.70, 1.30, 129.00, 132.80)) return "P.04_Perairan%20Raja%20Ampat%20-%20Sorong";
        if (isInside(lat, lon, -4.60, -1.40, 132.20, 135.60)) return "P.08_Perairan%20Fak-Fak";
        if (isInside(lat, lon, -2.20, 0.80, 135.00, 137.80)) return "Q.03_Perairan%20utara%20Biak";
        if (isInside(lat, lon, -3.80, -1.00, 137.00, 141.10)) return "Q.07_Perairan%20Jayapura%20-%20Sarmi";
        if (isInside(lat, lon, -9.70, -7.00, 139.00, 141.20)) return "R.07_Perairan%20Merauke";

        return Constants.BMKG_DEFAULT_MARINE_AREA;
    }

    private static boolean isInside(double lat, double lon, double minLat, double maxLat, double minLon, double maxLon) {
        return lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon;
    }

    private static boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180 && !(lat == 0.0 && lon == 0.0);
    }
}
