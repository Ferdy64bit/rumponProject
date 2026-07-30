package com.example.java3.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.java3.core.network.NetworkModule;
import com.example.java3.core.utils.Constants;
import com.example.java3.data.remote.MarineHourlyResponse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarineHourlyRepository {
    private static final String TAG = "MARINE_HOURLY_REPOSITORY";
    private static final String HOURLY_FIELDS = "wave_height,wave_direction,wave_period,wind_wave_height,swell_wave_height,sea_surface_temperature,ocean_current_velocity,ocean_current_direction";
    private static final Map<String, CacheEntry> MEMORY_CACHE = new HashMap<>();

    public void getHourlyMarine(double lat, double lon, MutableLiveData<MarineHourlyResponse> liveData, MutableLiveData<String> errorData) {
        String cacheKey = createCacheKey(lat, lon);
        CacheEntry cache = MEMORY_CACHE.get(cacheKey);
        if (cache != null && System.currentTimeMillis() - cache.updatedAt <= Constants.MARINE_HOURLY_CACHE_TTL_MS) {
            Log.d(TAG, "cache hit key=" + cacheKey);
            liveData.setValue(cache.response);
            return;
        }

        Log.d(TAG, "request lat=" + lat + " lon=" + lon + " key=" + cacheKey);
        NetworkModule.getMarineWeatherService()
                .getHourlyMarineForecast(lat, lon, HOURLY_FIELDS, "Asia/Bangkok", 2)
                .enqueue(new Callback<MarineHourlyResponse>() {
                    @Override
                    public void onResponse(Call<MarineHourlyResponse> call, Response<MarineHourlyResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            MarineHourlyResponse marine = response.body();
                            MEMORY_CACHE.put(cacheKey, new CacheEntry(marine, System.currentTimeMillis()));
                            Log.d(TAG, "api success key=" + cacheKey + " wave=" + marine.getCurrentWaveHeight());
                            liveData.setValue(marine);
                            return;
                        }
                        publishFallback(cache, liveData);
                        errorData.setValue("Open-Meteo Marine gagal: HTTP " + response.code());
                    }

                    @Override
                    public void onFailure(Call<MarineHourlyResponse> call, Throwable error) {
                        publishFallback(cache, liveData);
                        errorData.setValue("Gelombang hourly gagal dimuat: " + safeMessage(error));
                    }
                });
    }

    private void publishFallback(CacheEntry cache, MutableLiveData<MarineHourlyResponse> liveData) {
        if (cache != null && System.currentTimeMillis() - cache.updatedAt <= Constants.EXTERNAL_CACHE_MAX_AGE_MS) {
            liveData.setValue(cache.response);
        }
    }

    private String createCacheKey(double lat, double lon) {
        return String.format(Locale.US, "%.3f_%.3f", lat, lon);
    }

    private String safeMessage(Throwable error) {
        return error != null && error.getMessage() != null ? error.getMessage() : "terjadi kesalahan jaringan";
    }

    private static class CacheEntry {
        final MarineHourlyResponse response;
        final long updatedAt;

        CacheEntry(MarineHourlyResponse response, long updatedAt) {
            this.response = response;
            this.updatedAt = updatedAt;
        }
    }
}
