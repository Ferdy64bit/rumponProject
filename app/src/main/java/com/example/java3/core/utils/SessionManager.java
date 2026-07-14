package com.example.java3.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager - Handles user session using SharedPreferences.
 */
public class SessionManager {
    private static final String PREF_NAME = "FishingPointSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_UID = "userUid";
    private static final String KEY_USER_EMAIL = "userEmail";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(String uid, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_UID, uid);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserUid() {
        return sharedPreferences.getString(KEY_USER_UID, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
