package com.example.java3.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.java3.core.utils.LocationUtils;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.data.remote.MarineHourlyResponse;
import com.example.java3.data.repository.FishingRepository;
import com.example.java3.domain.model.DashboardStats;
import com.example.java3.domain.model.FishingPoint;
import com.example.java3.domain.model.FishingPointWithRecommendation;
import com.example.java3.domain.model.RecommendationResult;
import com.example.java3.domain.service.RecommendationEngine;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private static final double NEARBY_RADIUS_KM = 10.0;
    private static final int DASHBOARD_NEARBY_LIMIT = 6;

    private final FishingRepository fishingRepository;

    private final MutableLiveData<WeatherResponse> weatherLiveData = new MutableLiveData<>();
    private final MutableLiveData<TideResponse> tideLiveData = new MutableLiveData<>();
    private final MutableLiveData<MarineHourlyResponse> marineHourlyLiveData = new MutableLiveData<>();
    private final MediatorLiveData<List<FishingPointWithRecommendation>> recommendedSpotsLiveData = new MediatorLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> greetingLiveData = new MutableLiveData<>("Halo, Pemancing \uD83D\uDC4B");
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> emptyStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<DashboardStats> dashboardStatsLiveData = new MutableLiveData<>(new DashboardStats());
    private final MutableLiveData<Integer> unreadNotificationCountLiveData = new MutableLiveData<>(0);

    private double lastLat;
    private double lastLon;
    private List<FishingPoint> cachedPoints = new ArrayList<>();

    public HomeViewModel() {
        this.fishingRepository = new FishingRepository();

        recommendedSpotsLiveData.addSource(weatherLiveData, weather -> updateRecommendations());
        recommendedSpotsLiveData.addSource(tideLiveData, tide -> updateRecommendations());
        recommendedSpotsLiveData.addSource(marineHourlyLiveData, marine -> updateRecommendations());
    }

    public LiveData<WeatherResponse> getWeatherLiveData() { return weatherLiveData; }
    public LiveData<TideResponse> getTideLiveData() { return tideLiveData; }
    public LiveData<MarineHourlyResponse> getMarineHourlyLiveData() { return marineHourlyLiveData; }
    public LiveData<List<FishingPointWithRecommendation>> getRecommendedSpotsLiveData() { return recommendedSpotsLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<String> getGreetingLiveData() { return greetingLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<String> getEmptyStateLiveData() { return emptyStateLiveData; }
    public LiveData<DashboardStats> getDashboardStatsLiveData() { return dashboardStatsLiveData; }
    public LiveData<Integer> getUnreadNotificationCountLiveData() { return unreadNotificationCountLiveData; }

    public void fetchData(double userLat, double userLon) {
        this.lastLat = userLat;
        this.lastLon = userLon;
        loadingLiveData.setValue(true);
        emptyStateLiveData.setValue(null);

        fishingRepository.getWeather(userLat, userLon, weatherLiveData, errorLiveData);
        fishingRepository.getTide(userLat, userLon, tideLiveData, errorLiveData);
        fishingRepository.getMarineHourly(userLat, userLon, marineHourlyLiveData, errorLiveData);

        fishingRepository.getFishingPoints(new FishingRepository.FirestoreCallback<List<FishingPoint>>() {
            @Override
            public void onSuccess(List<FishingPoint> result) {
                cachedPoints = new ArrayList<>();
                if (result != null) {
                    for (FishingPoint point : result) {
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                        if (FishingRepository.canUserSeeSpot(point, currentUserId)) {
                            cachedPoints.add(point);
                        }
                    }
                }
                if (cachedPoints.isEmpty()) {
                    recommendedSpotsLiveData.setValue(new ArrayList<>());
                    emptyStateLiveData.setValue("Belum ada spot memancing.");
                } else {
                    emptyStateLiveData.setValue(null);
                    updateRecommendations();
                }
                loadingLiveData.setValue(false);
            }

            @Override
            public void onFailure(String error) {
                errorLiveData.setValue(error);
                loadingLiveData.setValue(false);
            }
        });
    }

    public void refreshEnvironment() {
        if (lastLat != 0.0 || lastLon != 0.0) {
            loadingLiveData.setValue(true);
            fishingRepository.getWeather(lastLat, lastLon, weatherLiveData, errorLiveData);
            fishingRepository.getTide(lastLat, lastLon, tideLiveData, errorLiveData);
            fishingRepository.getMarineHourly(lastLat, lastLon, marineHourlyLiveData, errorLiveData);
            loadingLiveData.setValue(false);
        }
    }

    public int getUnreadNotificationCount() {
        return unreadNotificationCountLiveData.getValue() != null ? unreadNotificationCountLiveData.getValue() : 0;
    }

    private void updateRecommendations() {
        if (cachedPoints == null || cachedPoints.isEmpty()) return;

        TideResponse currentTide = tideLiveData.getValue();
        WeatherResponse currentWeather = weatherLiveData.getValue();
        MarineHourlyResponse currentMarineHourly = marineHourlyLiveData.getValue();
        List<FishingPointDistance> distances = new ArrayList<>();
        int nearbyCount = 0;

        for (FishingPoint point : cachedPoints) {
            double distance = LocationUtils.calculateDistance(lastLat, lastLon, point.getLatitude(), point.getLongitude());
            if (distance <= NEARBY_RADIUS_KM) {
                nearbyCount++;
            }
            distances.add(new FishingPointDistance(point, distance));
        }

        distances.sort(Comparator.comparingDouble(item -> item.distance));

        List<FishingPointWithRecommendation> nearbySpots = new ArrayList<>();
        int limit = Math.min(DASHBOARD_NEARBY_LIMIT, distances.size());
        for (int i = 0; i < limit; i++) {
            FishingPointDistance item = distances.get(i);
            RecommendationResult result = RecommendationEngine.calculate(
                    currentTide,
                    currentWeather,
                    currentMarineHourly,
                    item.distance,
                    item.point.getRating()
            );
            nearbySpots.add(new FishingPointWithRecommendation(item.point, result, currentWeather, currentTide, item.distance));
        }

        DashboardStats stats = new DashboardStats();
        stats.setSpotCount(cachedPoints.size());
        stats.setNearbySpotCount(nearbyCount);
        dashboardStatsLiveData.setValue(stats);

        recommendedSpotsLiveData.setValue(nearbySpots);
    }

    private static class FishingPointDistance {
        private final FishingPoint point;
        private final double distance;

        private FishingPointDistance(FishingPoint point, double distance) {
            this.point = point;
            this.distance = distance;
        }
    }
}