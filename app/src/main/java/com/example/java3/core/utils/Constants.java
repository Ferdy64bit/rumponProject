package com.example.java3.core.utils;

public class Constants {
    public static final String WEATHER_API_KEY = "f7b999cba4c8c1aa0a25a856be9e7f71";
    public static final String TIDE_API_KEY = "tc_live_0c149d49b26dfb4553d893825e77c5c7";
    public static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    public static final String TIDE_BASE_URL = "https://tidecheck.com/";
    
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
    public static final String COL_TIDE_CACHE = "tide_cache";
}
