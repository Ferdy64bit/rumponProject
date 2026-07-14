package com.example.java3.data.repository;

import androidx.lifecycle.MutableLiveData;

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
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;

    private final FirebaseFirestore firestore;
    private final Gson gson;

    public WeatherRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.gson = new Gson();
    }

    public void getCurrentWeather(double lat, double lon, MutableLiveData<WeatherResponse> liveData, MutableLiveData<String> errorData) {
        String cacheKey = createCacheKey(lat, lon);
        firestore.collection(Constants.COL_WEATHER_CACHE)
                .document(cacheKey)
                .get()
                .addOnSuccessListener(snapshot -> {
                    WeatherCache cache = snapshot.toObject(WeatherCache.class);
                    if (cache != null && isFresh(cache.getUpdatedAt())) {
                        WeatherResponse cachedResponse = parseCache(cache.getData(), errorData);
                        if (cachedResponse != null) {
                            liveData.setValue(cachedResponse);
                            return;
                        }
                    }
                    fetchFromApi(cacheKey, lat, lon, liveData, errorData);
                })
                .addOnFailureListener(error -> fetchFromApi(cacheKey, lat, lon, liveData, errorData));
    }

    private void fetchFromApi(String cacheKey, double lat, double lon, MutableLiveData<WeatherResponse> liveData, MutableLiveData<String> errorData) {
        String apiKey = BuildConfig.OPEN_WEATHER_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
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
                            liveData.setValue(weather);
                            saveCache(cacheKey, weather);
                        } else {
                            errorData.setValue("Weather API gagal: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable error) {
                        errorData.setValue("Weather gagal dimuat: " + safeMessage(error));
                    }
                });
    }

    private void saveCache(String cacheKey, WeatherResponse weather) {
        WeatherCache cache = new WeatherCache(cacheKey, gson.toJson(weather), System.currentTimeMillis());
        firestore.collection(Constants.COL_WEATHER_CACHE).document(cacheKey).set(cache);
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
        return System.currentTimeMillis() - updatedAt <= CACHE_TTL_MS;
    }

    private String createCacheKey(double lat, double lon) {
        return String.format(Locale.US, "%.3f_%.3f", lat, lon);
    }

    private String safeMessage(Throwable error) {
        return error.getMessage() != null ? error.getMessage() : "terjadi kesalahan jaringan";
    }
}
