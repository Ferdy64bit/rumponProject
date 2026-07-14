package com.example.java3.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.java3.core.network.NetworkModule;
import com.example.java3.core.utils.Constants;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.TideStation;
import com.example.java3.domain.model.TideCache;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TideRepository {
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;

    private final FirebaseFirestore firestore;
    private final Gson gson;

    public TideRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.gson = new Gson();
    }

    public void getTide(double lat, double lon, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData) {
        String cacheKey = createCacheKey(lat, lon);
        firestore.collection(Constants.COL_TIDE_CACHE)
                .document(cacheKey)
                .get()
                .addOnSuccessListener(snapshot -> {
                    TideCache cache = snapshot.toObject(TideCache.class);
                    if (cache != null && isFresh(cache.getUpdatedAt())) {
                        TideResponse cachedResponse = parseCache(cache.getData(), errorData);
                        if (cachedResponse != null) {
                            liveData.setValue(cachedResponse);
                            return;
                        }
                    }
                    fetchFromApi(cacheKey, lat, lon, liveData, errorData);
                })
                .addOnFailureListener(error -> fetchFromApi(cacheKey, lat, lon, liveData, errorData));
    }

    private void fetchFromApi(String cacheKey, double lat, double lon, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData) {
        // Step 1: Find nearest station
        NetworkModule.getTideService().getNearestStation(Constants.TIDE_API_KEY, lat, lon)
                .enqueue(new Callback<List<TideStation>>() {
                    @Override
                    public void onResponse(Call<List<TideStation>> call, Response<List<TideStation>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            String stationId = response.body().get(0).getId();
                            // Step 2: Fetch tide data for station
                            fetchTideDataForStation(cacheKey, stationId, liveData, errorData);
                        } else {
                            errorData.setValue("Pasang: Stasiun tidak ditemukan.");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<TideStation>> call, Throwable t) {
                        errorData.setValue("Pasang: " + safeMessage(t));
                    }
                });
    }

    private void fetchTideDataForStation(String cacheKey, String stationId, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData) {
        NetworkModule.getTideService().getTideData(Constants.TIDE_API_KEY, stationId, 1)
                .enqueue(new Callback<TideResponse>() {
                    @Override
                    public void onResponse(Call<TideResponse> call, Response<TideResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            TideResponse tide = response.body();
                            liveData.setValue(tide);
                            saveCache(cacheKey, tide);
                        } else {
                            errorData.setValue("Tide API gagal: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<TideResponse> call, Throwable error) {
                        errorData.setValue("Tide gagal dimuat: " + safeMessage(error));
                    }
                });
    }

    private void saveCache(String cacheKey, TideResponse tide) {
        TideCache cache = new TideCache(cacheKey, gson.toJson(tide), System.currentTimeMillis());
        firestore.collection(Constants.COL_TIDE_CACHE).document(cacheKey).set(cache);
    }

    private TideResponse parseCache(String data, MutableLiveData<String> errorData) {
        try {
            return gson.fromJson(data, TideResponse.class);
        } catch (JsonSyntaxException error) {
            errorData.setValue("Cache pasang surut tidak valid, memuat ulang dari API.");
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
