package com.example.java3.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.java3.data.repository.FishingRepository;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.MarineHourlyResponse;
import com.example.java3.domain.model.FishingPoint;
import com.example.java3.domain.model.RecommendationResult;
import com.example.java3.domain.service.RecommendationEngine;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class MapViewModel extends ViewModel {
    private final FishingRepository fishingRepository;
    private final MutableLiveData<List<FishingPoint>> fishingPointsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<WeatherResponse> weatherLiveData = new MutableLiveData<>();
    private final MutableLiveData<TideResponse> tideLiveData = new MutableLiveData<>();
    private final MutableLiveData<MarineHourlyResponse> marineHourlyLiveData = new MutableLiveData<>();
    private ListenerRegistration registration;

    public MapViewModel() {
        this.fishingRepository = new FishingRepository();
        startListening();
    }

    public LiveData<List<FishingPoint>> getFishingPointsLiveData() {
        return fishingPointsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<WeatherResponse> getWeatherLiveData() {
        return weatherLiveData;
    }

    public LiveData<TideResponse> getTideLiveData() {
        return tideLiveData;
    }

    public LiveData<MarineHourlyResponse> getMarineHourlyLiveData() {
        return marineHourlyLiveData;
    }

    private final MutableLiveData<String> recommendationLiveData = new MutableLiveData<>();
    public LiveData<String> getRecommendationLiveData() { return recommendationLiveData; }

    public void fetchWeatherAndTide(double lat, double lon, double baseRating) {
        fetchWeatherAndTide(lat, lon, baseRating, 0.0);
    }

    public void fetchWeatherAndTide(double lat, double lon, double baseRating, double distanceKm) {
        fishingRepository.getWeather(lat, lon, weatherLiveData, errorLiveData);
        fishingRepository.getTide(lat, lon, tideLiveData, errorLiveData);
        fishingRepository.getMarineHourly(lat, lon, marineHourlyLiveData, errorLiveData);
        calculateRecommendation((float) baseRating, distanceKm);
    }

    private void calculateRecommendation(float rating, double distanceKm) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            WeatherResponse w = weatherLiveData.getValue();
            TideResponse t = tideLiveData.getValue();
            MarineHourlyResponse m = marineHourlyLiveData.getValue();
            RecommendationResult result = RecommendationEngine.calculate(t, w, m, distanceKm, rating);
            recommendationLiveData.setValue(result.getBadgeText() + " (" + result.getScorePercentage() + "%)");
        }, 2000);
    }

    public void addFishingPoint(FishingPoint point) {
        fishingRepository.addFishingPoint(point, new FishingRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Real-time listener handles updates
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void updateFishingPoint(FishingPoint point) {
        fishingRepository.updateFishingPoint(point, new FishingRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void deleteFishingPoint(String pointId) {
        fishingRepository.deleteFishingPoint(pointId, new FishingRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    private void startListening() {
        if (registration != null) {
            registration.remove();
        }
        registration = fishingRepository.listenFishingPoints(new FishingRepository.FirestoreCallback<List<FishingPoint>>() {
            @Override
            public void onSuccess(List<FishingPoint> result) {
                fishingPointsLiveData.setValue(result);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    @Override
    protected void onCleared() {
        if (registration != null) {
            registration.remove();
        }
        super.onCleared();
    }
}
