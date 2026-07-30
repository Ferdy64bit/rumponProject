package com.example.java3.core.utils;

public class Constants {
    public static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    public static final String BMKG_MARINE_BASE_URL = "https://peta-maritim.bmkg.go.id/";
    public static final String OPEN_METEO_MARINE_BASE_URL = "https://marine-api.open-meteo.com/";
    public static final String BMKG_DEFAULT_MARINE_AREA = "F.09_Teluk%20Jakarta";

    // Cloudinary unsigned upload configuration. Do not put API secret in the Android app.
    public static final String CLOUDINARY_CLOUD_NAME = "w8zmbrpu";
    public static final String CLOUDINARY_UPLOAD_PRESET = "fishing_post_upload";
    public static final String CLOUDINARY_PROFILE_UPLOAD_PRESET = "fishingpoint_profile";
    public static final String CLOUDINARY_UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/" + CLOUDINARY_CLOUD_NAME + "/image/upload";
    public static final int CLOUDINARY_MAX_UPLOAD_BYTES = 5 * 1024 * 1024;
    
    // Tanjung Anom Coordinates (Approximate)
    public static final double TANJUNG_ANOM_LAT = -6.041980;
    public static final double TANJUNG_ANOM_LON = 106.501318;

    // Firestore Collections
    public static final String COL_USERS = "users";
    public static final String COL_FISHING_POINTS = "fishing_points";
    public static final String COL_REVIEWS = "reviews";
    public static final String COL_FAVORITES = "favorites";
    public static final String COL_COMMUNITY_POSTS = "community_posts";
    public static final String COL_NOTIFICATIONS = "notifications";
    public static final String COL_WEATHER_CACHE = "weather_cache";
    public static final String COL_BMKG_CACHE = "bmkg_cache";
    public static final String COL_TIDE_CACHE = COL_BMKG_CACHE;

    // Cache policy for external forecast APIs.
    public static final long WEATHER_CACHE_TTL_MS = 10 * 60 * 1000L;
    public static final long BMKG_CACHE_TTL_MS = 30 * 60 * 1000L;
    public static final long MARINE_HOURLY_CACHE_TTL_MS = 30 * 60 * 1000L;
    public static final long EXTERNAL_CACHE_MAX_AGE_MS = 24 * 60 * 60 * 1000L;
    public static final int EXTERNAL_CACHE_MAX_DOCS = 80;
}
