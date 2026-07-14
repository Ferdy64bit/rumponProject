package com.example.java3.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.java3.core.network.NetworkModule;
import com.example.java3.core.utils.Constants;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.TideStation;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.domain.model.FishingPoint;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FishingRepository {
    private final FirebaseFirestore firestore;

    public FishingRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void getWeather(double lat, double lon, MutableLiveData<WeatherResponse> liveData, MutableLiveData<String> errorData) {
        NetworkModule.getWeatherService().getCurrentWeather(lat, lon, Constants.WEATHER_API_KEY, "metric")
            .enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        liveData.setValue(response.body());
                    } else {
                        errorData.setValue("Cuaca: API error " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    errorData.setValue("Cuaca: " + t.getMessage());
                }
            });
    }

    public void getTide(double lat, double lon, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData) {
        // Step 1: Find nearest station
        NetworkModule.getTideService().getNearestStation(Constants.TIDE_API_KEY, lat, lon)
            .enqueue(new Callback<List<TideStation>>() {
                @Override
                public void onResponse(Call<List<TideStation>> call, Response<List<TideStation>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        String stationId = response.body().get(0).getId();
                        // Step 2: Get tides for this station
                        fetchTideData(stationId, liveData, errorData);
                    } else {
                        errorData.setValue("Pasang: Stasiun tidak ditemukan.");
                    }
                }

                @Override
                public void onFailure(Call<List<TideStation>> call, Throwable t) {
                    errorData.setValue("Pasang: " + t.getMessage());
                }
            });
    }

    private void fetchTideData(String stationId, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData) {
        NetworkModule.getTideService().getTideData(Constants.TIDE_API_KEY, stationId, 1)
            .enqueue(new Callback<TideResponse>() {
                @Override
                public void onResponse(Call<TideResponse> call, Response<TideResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        liveData.setValue(response.body());
                    } else {
                        errorData.setValue("Pasang: Gagal muat data.");
                    }
                }

                @Override
                public void onFailure(Call<TideResponse> call, Throwable t) {
                    errorData.setValue("Pasang: " + t.getMessage());
                }
            });
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public void addFishingPoint(FishingPoint point, FirestoreCallback<Void> callback) {
        if (point.getId() == null || point.getId().isEmpty()) {
            point.setId(firestore.collection(Constants.COL_FISHING_POINTS).document().getId());
        }
        firestore.collection(Constants.COL_FISHING_POINTS)
                .document(point.getId())
                .set(point)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public ListenerRegistration listenFishingPoints(FirestoreCallback<List<FishingPoint>> callback) {
        return firestore.collection(Constants.COL_FISHING_POINTS)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<FishingPoint> points = value.toObjects(FishingPoint.class);
                        if (points.isEmpty()) {
                            seedFishingPoints();
                        }
                        callback.onSuccess(points);
                    }
                });
    }

    public void getFishingPoints(FirestoreCallback<List<FishingPoint>> callback) {
        firestore.collection(Constants.COL_FISHING_POINTS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FishingPoint> points = queryDocumentSnapshots.toObjects(FishingPoint.class);
                    if (points.isEmpty()) {
                        seedFishingPoints();
                        callback.onSuccess(getMockFishingPoints());
                    } else {
                        callback.onSuccess(points);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void seedFishingPoints() {
        List<FishingPoint> points = getMockFishingPoints();
        for (FishingPoint p : points) {
            firestore.collection(Constants.COL_FISHING_POINTS).document(p.getId()).set(p);
        }
    }

    private List<FishingPoint> getMockFishingPoints() {
        List<FishingPoint> points = new ArrayList<>();
        points.add(new FishingPoint("1", "Pantai Tanjung Kait", -6.0150, 106.6800, "Shore", "Pasir putih", 4.8f, ""));
        points.add(new FishingPoint("2", "Rumpon Tengah", -6.0000, 106.7000, "Rumpon", "Ikan Karang", 4.2f, ""));
        points.add(new FishingPoint("3", "Bagan Apung", -6.0200, 106.6700, "Bagan", "Spot Cumi", 3.5f, ""));
        return points;
    }
}
