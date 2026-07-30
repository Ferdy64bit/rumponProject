package com.example.java3.presentation.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.java3.R;
import com.example.java3.core.utils.Constants;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.data.remote.MarineHourlyResponse;
import com.example.java3.databinding.FragmentHomeBinding;
import com.example.java3.domain.model.BMKGForecast;
import com.example.java3.domain.model.DashboardStats;
import com.example.java3.domain.model.FishingPointWithRecommendation;
import com.example.java3.domain.model.RecommendationResult;
import com.example.java3.presentation.adapters.BMKGForecastAdapter;
import com.example.java3.presentation.activities.SpotListActivity;
import com.example.java3.presentation.adapters.FishingPointAdapter;
import com.example.java3.presentation.viewmodels.HomeViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private static final String TAG = "HOME_LOCATION";
    private static final long REFRESH_INTERVAL_MS = 60_000L;

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private FishingPointAdapter adapter;
    private BMKGForecastAdapter bmkgForecastAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = Constants.TANJUNG_ANOM_LAT;
    private double currentLon = Constants.TANJUNG_ANOM_LON;
    private MarineHourlyResponse latestMarineHourly;
    private long lastDashboardRefreshAt = 0L;

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fineLocationGranted) || Boolean.TRUE.equals(coarseLocationGranted)) {
                    loadDashboardFromDeviceLocation();
                } else {
                    loadFallbackLocation("Izin lokasi ditolak. Dashboard memakai lokasi default.");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        checkLocationPermission();
    }

    private void setupRecyclerView() {
        adapter = new FishingPointAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvRecommendedSpots.setLayoutManager(layoutManager);
        binding.rvRecommendedSpots.setAdapter(adapter);

        bmkgForecastAdapter = new BMKGForecastAdapter(new ArrayList<>());
        LinearLayoutManager forecastLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvBmkgForecast.setLayoutManager(forecastLayoutManager);
        binding.rvBmkgForecast.setAdapter(bmkgForecastAdapter);
    }

    private void observeViewModel() {
        viewModel.getGreetingLiveData().observe(getViewLifecycleOwner(), greeting -> binding.tvHallo.setText(greeting));
        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), this::showLoadingState);
        viewModel.getWeatherLiveData().observe(getViewLifecycleOwner(), this::renderWeather);
        viewModel.getTideLiveData().observe(getViewLifecycleOwner(), this::renderTide);
        viewModel.getMarineHourlyLiveData().observe(getViewLifecycleOwner(), this::renderMarineHourly);
        viewModel.getRecommendedSpotsLiveData().observe(getViewLifecycleOwner(), this::renderRecommendations);
        viewModel.getUnreadNotificationCountLiveData().observe(getViewLifecycleOwner(), this::renderNotificationCount);
        viewModel.getDashboardStatsLiveData().observe(getViewLifecycleOwner(), this::renderStats);
        viewModel.getEmptyStateLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                binding.tvRecommendationTitle.setText(message);
                binding.progressRecommendation.setProgress(0);
                binding.tvRecommendationPercent.setText("0%");
                binding.tvSafetyScore.setText("Safety --");
            binding.tvSafetyLabel.setText("Menunggu data");
            binding.tvActivityScore.setText("Ikan --");
            binding.tvActivityLabel.setText("Menunggu data");
            }
        });
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                renderErrorState(error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnMenu.setOnClickListener(v -> openSpotList());
        binding.btnNotification.setOnClickListener(v -> {
            int unreadCount = viewModel.getUnreadNotificationCount();
            String message = unreadCount > 0
                    ? unreadCount + " notifikasi belum dibaca."
                    : "Tidak ada notifikasi baru.";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
        binding.weatherCard.setOnClickListener(v -> loadDashboardFromDeviceLocation());
        binding.tvTideDetailLink.setOnClickListener(v -> showTideSummary());
        binding.recommendationCard.setOnClickListener(v -> openSpotList());
        binding.tvSeeAllSpots.setOnClickListener(v -> openSpotList());
    }

    private void checkLocationPermission() {
        if (hasLocationPermission()) {
            loadDashboardFromDeviceLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void loadDashboardFromDeviceLocation() {
        if (!hasLocationPermission()) {
            loadFallbackLocation("Izin lokasi belum aktif. Dashboard memakai lokasi default.");
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastDashboardRefreshAt < REFRESH_INTERVAL_MS) {
            return;
        }
        lastDashboardRefreshAt = now;

        if (binding != null) {
            binding.tvLocationName.setText("Mendeteksi lokasi...");
        }
        
        CancellationTokenSource tokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (binding == null) return;
                    if (location != null) {
                        loadDashboard(location, "Lokasi Anda");
                    } else {
                        requestLastKnownLocation();
                    }
                })
                .addOnFailureListener(error -> {
                    if (binding == null) return;
                    requestLastKnownLocation();
                });
    }

    @SuppressLint("MissingPermission")
    private void requestLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (binding == null) return;
                    if (location != null) {
                        loadDashboard(location, "Lokasi Anda");
                    } else {
                        loadFallbackLocation("Lokasi belum ditemukan. Dashboard memakai lokasi default.");
                    }
                })
                .addOnFailureListener(error -> {
                    if (binding == null) return;
                    loadFallbackLocation("GPS gagal dimuat. Dashboard memakai lokasi default.");
                });
    }

    private void loadDashboard(Location location, String locationName) {
        if (binding == null) return;
        currentLat = location.getLatitude();
        currentLon = location.getLongitude();
        Log.d(TAG, "dashboard location provider=" + location.getProvider()
                + " lat=" + currentLat
                + " lon=" + currentLon
                + " mock=" + location.isFromMockProvider());

        // Emulator Fix: If location is Mountain View (Google HQ), force to Tanjung Anom
        if (Math.abs(currentLat - 37.42) < 0.1 && Math.abs(currentLon - (-122.08)) < 0.1) {
            currentLat = Constants.TANJUNG_ANOM_LAT;
            currentLon = Constants.TANJUNG_ANOM_LON;
            binding.tvLocationName.setText("Tanjung Anom (Tangerang)");
        } else {
            binding.tvLocationName.setText(locationName);
        }
        
        viewModel.fetchData(currentLat, currentLon);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && hasLocationPermission()) {
            loadDashboardFromDeviceLocation();
        }
    }

    private void loadFallbackLocation(String message) {
        if (binding != null) {
            binding.tvLocationName.setText("Tanjung Anom (fallback)");
        }
        if (getContext() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
        viewModel.fetchData(Constants.TANJUNG_ANOM_LAT, Constants.TANJUNG_ANOM_LON);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void showLoadingState(Boolean isLoading) {
        if (!Boolean.TRUE.equals(isLoading)) {
            return;
        }
        binding.tvTemperatureLarge.setText("--\u00B0C");
        binding.tvCondition.setText("Memuat cuaca...");
        binding.tvHumidityVal.setText("--%");
        binding.tvWindVal.setText("-- km/h");
        binding.tvPressureVal.setText("-- hPa");
        binding.tvHighTideTime.setText("--:--");
        binding.tvHighTideHeight.setText("--");
        binding.tvLowTideTime.setText("--:--");
        binding.tvLowTideHeight.setText("--");
        binding.tvCurrentTideStatus.setText("Memuat");
        binding.waveHourlyChart.setWaveData(new ArrayList<>(), new ArrayList<>(), 0);
        bmkgForecastAdapter.updateData(new ArrayList<>());
    }

    private void renderWeather(WeatherResponse weather) {
        if (weather == null || weather.getMain() == null) {
            return;
        }

        binding.tvTemperatureLarge.setText(String.format(Locale.getDefault(), "%.0f\u00B0C", weather.getMain().getTemp()));
        binding.tvHumidityVal.setText(String.format(Locale.getDefault(), "%d%%", weather.getMain().getHumidity()));
        binding.tvPressureVal.setText(String.format(Locale.getDefault(), "%d hPa", weather.getMain().getPressure()));

        if (weather.getWind() != null) {
            binding.tvWindVal.setText(String.format(Locale.getDefault(), "%.1f km/h", weather.getWind().getSpeed() * 3.6));
        }

        if (weather.getWeather() != null && !weather.getWeather().isEmpty()) {
            binding.tvCondition.setText(capitalize(weather.getWeather().get(0).getDescription()));
        }

        if (weather.getName() != null && !weather.getName().trim().isEmpty()) {
            binding.tvLocationName.setText(weather.getName());
        }
    }

    private void renderTide(TideResponse tide) {
        if (tide == null) {
            return;
        }

        if (!hasUsableTideData(tide)) {
            binding.tvHighTideTime.setText("--:--");
            binding.tvHighTideHeight.setText("--");
            binding.tvLowTideTime.setText("--:--");
            binding.tvLowTideHeight.setText("--");
            binding.tvCurrentTideStatus.setText("Belum tersedia");
            binding.tvCurrentTideStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            bmkgForecastAdapter.updateData(new ArrayList<>());
            return;
        }

        renderBmkgForecasts(tide);

        binding.tvHighTideTime.setText(nonBlank(tide.getHighTide(), "--:--"));
        binding.tvLowTideTime.setText(nonBlank(tide.getLowTide(), "--:--"));
        binding.tvHighTideHeight.setText(String.format(Locale.getDefault(), "%.1f m", tide.getTideHeight()));
        binding.tvLowTideHeight.setText(nonBlank(tide.getBestFishingWindow(), "--"));

        String activity = nonBlank(tide.getFishingActivity(), "Normal");
        binding.tvCurrentTideStatus.setText(toMarineStatusLabel(activity));

        if ("Excellent".equalsIgnoreCase(activity)) {
            binding.tvRecommendationStars.setText("\u2605\u2605\u2605\u2605\u2605");
            binding.tvCurrentTideStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success));
        } else if ("Good".equalsIgnoreCase(activity)) {
            binding.tvRecommendationStars.setText("\u2605\u2605\u2605\u2605\u2606");
            binding.tvCurrentTideStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        } else {
            binding.tvCurrentTideStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        }
        if (latestMarineHourly != null) {
            renderMarineHourly(latestMarineHourly);
        }
    }

    private void renderMarineHourly(MarineHourlyResponse marine) {
        if (marine == null || binding == null) {
            return;
        }
        latestMarineHourly = marine;
        float currentWave = marine.getCurrentWaveHeight();
        float maxWave = marine.getTodayMaxWaveHeight();
        binding.waveHourlyChart.setWaveData(marine.getNext24WaveHeights(), marine.getNext24TimeLabels(), marine.getCurrentHourIndexInNext24());
        if (currentWave > 0f) {
            binding.tvHighTideTime.setText(marine.getWaveLabel());
            binding.tvHighTideHeight.setText(String.format(Locale.getDefault(), "%.1f m", currentWave));
            binding.tvWaveSource.setText(String.format(Locale.getDefault(),
                    "Suhu laut %.0f C | arus %.1f m/s %s | sekarang %.1f m, maksimum %.1f m",
                    marine.getCurrentSeaSurfaceTemperature(),
                    marine.getCurrentOceanCurrentVelocity(),
                    marine.getCurrentDirectionCompass(),
                    currentWave,
                    maxWave));
        }
        if (maxWave > 0f) {
            binding.tvLowTideHeight.setText(String.format(Locale.getDefault(), "Maks %.1f m/24j", maxWave));
        }
    }

    private void renderBmkgForecasts(TideResponse tide) {
        List<BMKGForecast> forecasts = tide.getForecasts();
        if (forecasts != null && !forecasts.isEmpty()) {
            bmkgForecastAdapter.updateData(forecasts);
            return;
        }

        BMKGForecast fallback = new BMKGForecast();
        fallback.setTimeDesc(nonBlank(tide.getForecastTime(), "BMKG"));
        fallback.setWeatherDesc(tide.getMarineSummary());
        fallback.setWaveCategory(tide.getHighTide());
        fallback.setWaveDescription(tide.getHighTide());
        fallback.setWindFrom(tide.getWindFrom());
        fallback.setWindTo(tide.getWindTo());
        fallback.setWindSpeedMin(tide.getWindSpeedMin());
        fallback.setWindSpeedMax(tide.getWindSpeedMax());
        fallback.setWarning(tide.getWarningDesc());
        List<BMKGForecast> fallbackList = new ArrayList<>();
        fallbackList.add(fallback);
        bmkgForecastAdapter.updateData(fallbackList);
    }

    private boolean hasUsableTideData(TideResponse tide) {
        return isFilled(tide.getHighTide())
                || isFilled(tide.getLowTide())
                || isFilled(tide.getFishingActivity())
                || isFilled(tide.getBestFishingWindow())
                || tide.getTideHeight() > 0f;
    }

    private void renderRecommendations(List<FishingPointWithRecommendation> spots) {
        if (spots == null || spots.isEmpty()) {
            adapter.updateData(new ArrayList<>());
            binding.tvRecommendationTitle.setText("Belum ada spot rekomendasi");
            binding.progressRecommendation.setProgress(0);
            binding.tvRecommendationPercent.setText("0%");
            binding.tvSafetyScore.setText("Safety --");
            binding.tvSafetyLabel.setText("Menunggu data");
            binding.tvActivityScore.setText("Ikan --");
            binding.tvActivityLabel.setText("Menunggu data");
            return;
        }

        adapter.updateData(spots);
        FishingPointWithRecommendation top = findBestRecommendation(spots);
        RecommendationResult result = top.getRecommendation();
        int score = result.getScorePercentage();
        binding.tvRecommendationTitle.setText(result.getBadgeText());
        binding.progressRecommendation.setProgress(score);
        binding.tvRecommendationPercent.setText(String.format(Locale.getDefault(), "%d%%", score));
        binding.tvRecommendationStars.setText(createStars(result.getStars()));
        binding.tvRecommendationSubtitle.setText(getRecommendationSubtitle(score));
        renderRecommendationInsights(result);
    }

    private FishingPointWithRecommendation findBestRecommendation(List<FishingPointWithRecommendation> spots) {
        FishingPointWithRecommendation best = spots.get(0);
        for (FishingPointWithRecommendation spot : spots) {
            if (spot.getRecommendation().getScore() > best.getRecommendation().getScore()) {
                best = spot;
            }
        }
        return best;
    }

    private void renderRecommendationInsights(RecommendationResult result) {
        int safetyPercent = (int) Math.round(Math.max(0.0, Math.min(1.0, result.getSafetyMultiplier())) * 100.0);
        int activityPercent = (int) Math.round(Math.max(0.0, Math.min(100.0, result.getActivityScore())));
        binding.tvSafetyScore.setText(String.format(Locale.getDefault(), "Safe %d%%", safetyPercent));
        binding.tvSafetyLabel.setText(getSafetyLabel(safetyPercent));
        binding.tvActivityScore.setText(String.format(Locale.getDefault(), "Ikan %d%%", activityPercent));
        binding.tvActivityLabel.setText(getActivityLabel(activityPercent));
    }

    private String getSafetyLabel(int score) {
        if (score >= 90) return "Aman";
        if (score >= 75) return "Cukup Aman";
        if (score >= 55) return "Waspada";
        if (score >= 40) return "Berisiko";
        return "Tidak aman";
    }

    private String getActivityLabel(int score) {
        if (score >= 85) return "Tinggi";
        if (score >= 70) return "Baik";
        if (score >= 55) return "Cukup";
        return "Rendah";
    }
    private void renderNotificationCount(Integer count) {
        int unreadCount = count != null ? count : 0;
        binding.btnNotification.setContentDescription(
                unreadCount > 0
                        ? "Notifikasi, " + unreadCount + " belum dibaca"
                        : getString(R.string.notifications)
        );
    }

    private void renderStats(DashboardStats stats) {
        if (stats == null) {
            return;
        }
        binding.tvWelcomeBack.setText(String.format(
                Locale.getDefault(),
                "%d spot tersedia, %d terdekat",
                stats.getSpotCount(),
                stats.getNearbySpotCount()
        ));
    }

    private void showTideSummary() {
        TideResponse tide = viewModel.getTideLiveData().getValue();
        if (tide == null) {
            Toast.makeText(requireContext(), "Data perairan BMKG belum tersedia.", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = "Gelombang " + nonBlank(tide.getHighTide(), "--")
                + " | " + nonBlank(tide.getLowTide(), "Angin --");
        MarineHourlyResponse marine = viewModel.getMarineHourlyLiveData().getValue();
        if (marine != null && marine.getCurrentWaveHeight() > 0f) {
            message = String.format(Locale.getDefault(),
                    "Gelombang hourly %.1f m (%s), suhu laut %.0f C, arus %.1f m/s %s",
                    marine.getCurrentWaveHeight(),
                    marine.getWaveLabel(),
                    marine.getCurrentSeaSurfaceTemperature(),
                    marine.getCurrentOceanCurrentVelocity(),
                    marine.getCurrentDirectionCompass());
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void openSpotList() {
        Intent intent = new Intent(requireContext(), SpotListActivity.class);
        intent.putExtra(SpotListActivity.EXTRA_USER_LAT, currentLat);
        intent.putExtra(SpotListActivity.EXTRA_USER_LON, currentLon);
        startActivity(intent);
    }

    private String createStars(int count) {
        StringBuilder stars = new StringBuilder();
        int safeCount = Math.max(1, Math.min(count, 5));
        for (int i = 0; i < safeCount; i++) {
            stars.append('\u2605');
        }
        for (int i = safeCount; i < 5; i++) {
            stars.append('\u2606');
        }
        return stars.toString();
    }

    private String getRecommendationSubtitle(int score) {
        if (score >= 90) {
            return "Kondisi sangat mendukung untuk memancing hari ini.";
        }
        if (score >= 75) {
            return "Kondisi cukup aman dan layak untuk memancing.";
        }
        if (score >= 60) {
            return "Kondisi masih bisa dicoba dengan persiapan.";
        }
        if (score >= 40) {
            return "Perhatikan cuaca, angin, dan gelombang sebelum berangkat.";
        }
        return "Kondisi kurang mendukung untuk aktivitas memancing.";
    }

    private String toMarineStatusLabel(String activity) {
        if ("Excellent".equalsIgnoreCase(activity)) return "Sangat Baik";
        if ("Good".equalsIgnoreCase(activity)) return "Baik";
        if ("Fair".equalsIgnoreCase(activity)) return "Cukup";
        if ("Poor".equalsIgnoreCase(activity)) return "Waspada";
        if ("Very Poor".equalsIgnoreCase(activity)) return "Tidak Aman";
        return activity;
    }

    private String capitalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Cuaca tidak diketahui";
        }
        String trimmed = value.trim();
        return trimmed.substring(0, 1).toUpperCase(Locale.getDefault()) + trimmed.substring(1);
    }

    private String nonBlank(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private boolean isFilled(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void renderErrorState(String error) {
        String lowerError = error.toLowerCase(Locale.ROOT);
        if (lowerError.contains("tide") || lowerError.contains("pasang") || lowerError.contains("bmkg") || lowerError.contains("maritim")) {
            binding.tvHighTideTime.setText("--:--");
            binding.tvHighTideHeight.setText("--");
            binding.tvLowTideTime.setText("--:--");
            binding.tvLowTideHeight.setText("--");
            binding.tvCurrentTideStatus.setText("Layanan BMKG");
            binding.tvCurrentTideStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
        }

        if (lowerError.contains("weather") || lowerError.contains("cuaca")) {
            binding.tvCondition.setText("Gagal sinkron");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
