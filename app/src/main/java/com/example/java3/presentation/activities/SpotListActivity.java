package com.example.java3.presentation.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.java3.R;
import com.example.java3.core.utils.Constants;
import com.example.java3.core.utils.LocationUtils;
import com.example.java3.data.repository.FavoriteRepository;
import com.example.java3.data.repository.FishingRepository;
import com.example.java3.databinding.ActivitySpotListBinding;
import com.example.java3.domain.model.Favorite;
import com.example.java3.domain.model.FishingPoint;
import com.example.java3.presentation.adapters.SpotListAdapter;
import com.example.java3.presentation.model.SpotUiModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SpotListActivity extends AppCompatActivity {
    public static final String EXTRA_USER_LAT = "extra_user_lat";
    public static final String EXTRA_USER_LON = "extra_user_lon";

    private static final int FILTER_BEST = 0;
    private static final int FILTER_NEAREST = 1;
    private static final int FILTER_FAVORITE = 2;

    private ActivitySpotListBinding binding;
    private SpotListAdapter adapter;
    private FishingRepository fishingRepository;
    private FavoriteRepository favoriteRepository;
    private final List<FishingPoint> baseSpots = new ArrayList<>();
    private final List<SpotUiModel> allSpots = new ArrayList<>();
    private final Set<String> favoriteSpotIds = new HashSet<>();
    private int activeFilter = FILTER_BEST;
    private double userLat;
    private double userLon;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpotListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fishingRepository = new FishingRepository();
        favoriteRepository = new FavoriteRepository();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        userLat = getIntent().getDoubleExtra(EXTRA_USER_LAT, Constants.TANJUNG_ANOM_LAT);
        userLon = getIntent().getDoubleExtra(EXTRA_USER_LON, Constants.TANJUNG_ANOM_LON);

        setupSystemBars();
        setupRecyclerView();
        setupListeners();
        loadSpots();
        loadFavoriteIds();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );
    }

    private void setupRecyclerView() {
        adapter = new SpotListAdapter();
        binding.rvSpotList.setHasFixedSize(true);
        binding.rvSpotList.setItemViewCacheSize(8);
        binding.rvSpotList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSpotList.setAdapter(adapter);
        adapter.setOnSpotClickListener(this::openDetailSpot);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnToolbarSearch.setOnClickListener(v -> binding.etSearch.requestFocus());
        binding.chipBest.setOnClickListener(v -> {
            activeFilter = FILTER_BEST;
            applyFilterAndSearch();
        });
        binding.chipNearest.setOnClickListener(v -> {
            activeFilter = FILTER_NEAREST;
            applyFilterAndSearch();
        });
        binding.chipFavorite.setOnClickListener(v -> {
            activeFilter = FILTER_FAVORITE;
            applyFilterAndSearch();
        });
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadSpots() {
        fishingRepository.getFishingPoints(new FishingRepository.FirestoreCallback<List<FishingPoint>>() {
            @Override
            public void onSuccess(List<FishingPoint> result) {
                baseSpots.clear();
                if (result != null) {
                    baseSpots.addAll(result);
                }
                rebuildSpotModels();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(SpotListActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavoriteIds() {
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            favoriteSpotIds.clear();
            applyFilterAndSearch();
            return;
        }
        favoriteRepository.getUserFavorites(currentUserId, new FavoriteRepository.FirestoreCallback<List<Favorite>>() {
            @Override
            public void onSuccess(List<Favorite> result) {
                favoriteSpotIds.clear();
                if (result != null) {
                    for (Favorite favorite : result) {
                        if (favorite != null && favorite.getPointId() != null && !favorite.getPointId().trim().isEmpty()) {
                            favoriteSpotIds.add(favorite.getPointId().trim());
                        }
                    }
                }
                applyFilterAndSearch();
            }

            @Override
            public void onFailure(String error) {
                favoriteSpotIds.clear();
                applyFilterAndSearch();
            }
        });
    }

    private void rebuildSpotModels() {
        allSpots.clear();
        for (int i = 0; i < baseSpots.size(); i++) {
            FishingPoint point = baseSpots.get(i);
            if (point != null && FishingRepository.canUserSeeSpot(point, currentUserId)) {
                allSpots.add(mapSpot(point, i));
            }
        }
        applyFilterAndSearch();
    }

    private void applyFilterAndSearch() {
        String query = binding.etSearch.getText() != null
                ? binding.etSearch.getText().toString().trim().toLowerCase(Locale.ROOT)
                : "";
        List<SpotUiModel> filtered = new ArrayList<>();
        for (SpotUiModel spot : allSpots) {
            if (query.isEmpty() || spot.getSearchText().toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(spot);
            }
        }

        if (activeFilter == FILTER_NEAREST) {
            filtered.sort(Comparator.comparingDouble(SpotUiModel::getDistance));
        } else if (activeFilter == FILTER_FAVORITE) {
            List<SpotUiModel> favorites = new ArrayList<>();
            for (SpotUiModel spot : filtered) {
                if (isFavoriteSpot(spot)) {
                    favorites.add(spot);
                }
            }
            filtered = favorites;
        } else {
            filtered.sort((a, b) -> {
                int ratingCompare = Float.compare(b.getRating(), a.getRating());
                if (ratingCompare != 0) return ratingCompare;
                int reviewCompare = Integer.compare(b.getReviewCount(), a.getReviewCount());
                if (reviewCompare != 0) return reviewCompare;
                return Double.compare(a.getDistance(), b.getDistance());
            });
        }

        adapter.submitList(filtered);
    }

    private SpotUiModel mapSpot(FishingPoint point, int index) {
        double distance = LocationUtils.calculateDistance(userLat, userLon, point.getLatitude(), point.getLongitude());
        int placeholder = getPlaceholder(index);
        String searchText = joinSearchText(
                point.getName(),
                point.getType(),
                point.getFishType(),
                point.getArea(),
                point.getLocationName(),
                point.getDescription()
        );
        return new SpotUiModel(
                point.getId(),
                nonBlank(point.getName(), "Spot tanpa nama"),
                distance,
                (float) point.getRating(),
                point.getReviewCount(),
                R.color.primary,
                placeholder,
                favoriteSpotIds.contains(point.getId()),
                point.getImageUrl(),
                searchText,
                nonBlank(point.getType(), "Spot memancing"),
                nonBlank(point.getDescription(), "Deskripsi belum tersedia"),
                point.getLatitude(),
                point.getLongitude(),
                0,
                0,
                "Rekomendasi dihitung di detail spot",
                firstNonBlank(point.getOwnerId(), point.getUserId()),
                nonBlank(point.getOwnerName(), "Fishing Point Member"),
                nonBlank(point.getOwnerPhoto(), ""),
                nonBlank(point.getVisibility(), "PUBLIC"),
                point.getCreatedAt()
        );
    }

    private void openDetailSpot(SpotUiModel spot) {
        Intent intent = new Intent(this, DetailSpotActivity.class);
        intent.putExtra(DetailSpotActivity.EXTRA_ID, spot.getId());
        intent.putExtra(DetailSpotActivity.EXTRA_NAME, spot.getName());
        intent.putExtra(DetailSpotActivity.EXTRA_DISTANCE, spot.getDistance());
        intent.putExtra(DetailSpotActivity.EXTRA_RATING, spot.getRating());
        intent.putExtra(DetailSpotActivity.EXTRA_REVIEWS, spot.getReviewCount());
        intent.putExtra(DetailSpotActivity.EXTRA_IMAGE_RES, spot.getImageResId());
        intent.putExtra(DetailSpotActivity.EXTRA_IMAGE_URL, spot.getImageUrl());
        intent.putExtra(DetailSpotActivity.EXTRA_TYPE, spot.getType());
        intent.putExtra(DetailSpotActivity.EXTRA_DESCRIPTION, spot.getDescription());
        intent.putExtra(DetailSpotActivity.EXTRA_LATITUDE, spot.getLatitude());
        intent.putExtra(DetailSpotActivity.EXTRA_LONGITUDE, spot.getLongitude());
        intent.putExtra(DetailSpotActivity.EXTRA_OWNER_ID, spot.getOwnerId());
        intent.putExtra(DetailSpotActivity.EXTRA_OWNER_NAME, spot.getOwnerName());
        intent.putExtra(DetailSpotActivity.EXTRA_OWNER_PHOTO, spot.getOwnerPhoto());
        intent.putExtra(DetailSpotActivity.EXTRA_VISIBILITY, spot.getVisibility());
        intent.putExtra(DetailSpotActivity.EXTRA_CREATED_AT, spot.getCreatedAt());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private boolean isFavoriteSpot(SpotUiModel spot) {
        return spot != null && favoriteSpotIds.contains(spot.getId());
    }

    private int getPlaceholder(int index) {
        int[] images = {
                R.drawable.img_spot_dermaga,
                R.drawable.img_spot_breakwater,
                R.drawable.img_spot_muara,
                R.drawable.img_spot_pantai,
                R.drawable.img_spot_pulau
        };
        return images[index % images.length];
    }

    private String joinSearchText(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.trim().isEmpty()) {
                builder.append(part.trim()).append(' ');
            }
        }
        return builder.toString();
    }

    private String nonBlank(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}