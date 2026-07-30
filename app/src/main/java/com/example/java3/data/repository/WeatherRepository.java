package com.example.java3.data.repository;

import androidx.lifecycle.MutableLiveData;

import android.util.Log;

import com.example.java3.BuildConfig;
import com.example.java3.core.network.NetworkModule;
import com.example.java3.core.utils.Constants;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.domain.model.WeatherCache;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private static final String TAG = "WEATHER_REPOSITORY";

    private final FirebaseFirestore firestore;
    private final Gson gson;

    public WeatherRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.gson = new Gson();
    }

    public void getCurrentWeather(double lat, double lon, MutableLiveData<WeatherResponse> liveData, MutableLiveData<String> errorData) {
        String cacheKey = createCacheKey(lat, lon);
        Log.d(TAG, "request lat=" + lat + " lon=" + lon + " key=" + cacheKey);
        firestore.collection(Constants.COL_WEATHER_CACHE)
                .document(cacheKey)
                .get()
                .addOnSuccessListener(snapshot -> {
                    WeatherCache cache = snapshot.toObject(WeatherCache.class);
                    if (cache != null && isFresh(cache.getUpdatedAt())) {
                        WeatherResponse cachedResponse = parseCache(cache.getData(), errorData);
                        if (cachedResponse != null) {
                            Log.d(TAG, "cache hit key=" + cacheKey);
                            liveData.setValue(cachedResponse);
                            return;
                        }
                    }
                    fetchFromApi(cacheKey, cache, lat, lon, liveData, errorData);
                })
                .addOnFailureListener(error -> fetchFromApi(cacheKey, null, lat, lon, liveData, errorData));
    }

    private void fetchFromApi(String cacheKey, WeatherCache fallbackCache, double lat, double lon, MutableLiveData<WeatherResponse> liveData, MutableLiveData<String> errorData) {
        String apiKey = BuildConfig.OPEN_WEATHER_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            publishFallback(fallbackCache, liveData);
            errorData.setValue("OpenWeather API key belum dikonfigurasi di local.properties.");
            return;
        }

        NetworkModule.getWeatherService()
                .getCurrentWeather(lat, lon, apiKey, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse weather = response.body();
                            Log.d(TAG, "api success key=" + cacheKey);
                            liveData.setValue(weather);
                            saveCache(cacheKey, weather);
                        } else {
                            publishFallback(fallbackCache, liveData);
                            errorData.setValue("Weather API gagal: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable error) {
                        publishFallback(fallbackCache, liveData);
                        errorData.setValue("Weather gagal dimuat: " + safeMessage(error));
                    }
                });
    }

    private void saveCache(String cacheKey, WeatherResponse weather) {
        long now = System.currentTimeMillis();
        String json = gson.toJson(weather);
        WeatherCache cache = new WeatherCache(cacheKey, json, "OpenWeather", now, now + Constants.WEATHER_CACHE_TTL_MS);
        firestore.collection(Constants.COL_WEATHER_CACHE).document(cacheKey).set(cache);
        cleanupExpiredCache();
    }

    private void publishFallback(WeatherCache cache, MutableLiveData<WeatherResponse> liveData) {
        if (cache == null || !isUsableFallback(cache.getUpdatedAt())) {
            return;
        }
        WeatherResponse cachedResponse = parseCache(cache.getData(), new MutableLiveData<>());
        if (cachedResponse != null) {
            liveData.setValue(cachedResponse);
        }
    }

    private WeatherResponse parseCache(String data, MutableLiveData<String> errorData) {
        try {
            return gson.fromJson(data, WeatherResponse.class);
        } catch (JsonSyntaxException error) {
            errorData.setValue("Cache cuaca tidak valid, memuat ulang dari API.");
            return null;
        }
    }

    private boolean isFresh(long updatedAt) {
        return System.currentTimeMillis() - updatedAt <= Constants.WEATHER_CACHE_TTL_MS;
    }

    private boolean isUsableFallback(long updatedAt) {
        return System.currentTimeMillis() - updatedAt <= Constants.EXTERNAL_CACHE_MAX_AGE_MS;
    }

    private void cleanupExpiredCache() {
        long cutoff = System.currentTimeMillis() - Constants.EXTERNAL_CACHE_MAX_AGE_MS;
        firestore.collection(Constants.COL_WEATHER_CACHE)
                .whereLessThan("updatedAt", cutoff)
                .limit(20)
                .get()
                .addOnSuccessListener(query -> {
                    query.getDocuments().forEach(document -> document.getReference().delete());
                });
    }

    private String createCacheKey(double lat, double lon) {
        return String.format(Locale.US, "%.3f_%.3f", lat, lon);
    }

    private String safeMessage(Throwable error) {
        return error.getMessage() != null ? error.getMessage() : "terjadi kesalahan jaringan";
    }
}
