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
import com.example.java3.data.repository.SpotRepository;
import com.example.java3.databinding.ActivitySpotListBinding;
import com.example.java3.domain.model.FishingPoint;
import com.example.java3.presentation.adapters.SpotListAdapter;
import com.example.java3.presentation.model.SpotUiModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SpotListActivity extends AppCompatActivity {
    public static final String EXTRA_USER_LAT = "extra_user_lat";
    public static final String EXTRA_USER_LON = "extra_user_lon";

    private static final int FILTER_BEST = 0;
    private static final int FILTER_NEAREST = 1;
    private static final int FILTER_FAVORITE = 2;

    private ActivitySpotListBinding binding;
    private SpotListAdapter adapter;
    private SpotRepository spotRepository;
    private final List<SpotUiModel> allSpots = new ArrayList<>();
    private int activeFilter = FILTER_BEST;
    private double userLat;
    private double userLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpotListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        spotRepository = new SpotRepository();
        userLat = getIntent().getDoubleExtra(EXTRA_USER_LAT, Constants.TANJUNG_ANOM_LAT);
        userLon = getIntent().getDoubleExtra(EXTRA_USER_LON, Constants.TANJUNG_ANOM_LON);

        setupSystemBars();
        setupRecyclerView();
        setupListeners();
        loadSpots();
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
        spotRepository.getFishingPoints(new SpotRepository.FirestoreCallback<List<FishingPoint>>() {
            @Override
            public void onSuccess(List<FishingPoint> result) {
                allSpots.clear();
                for (int i = 0; i < result.size(); i++) {
                    allSpots.add(mapSpot(result.get(i), i));
                }
                applyFilterAndSearch();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(SpotListActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
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
                if (spot.isFavorite()) {
                    favorites.add(spot);
                }
            }
            filtered = favorites;
        } else {
            filtered.sort((a, b) -> Float.compare(b.getRating(), a.getRating()));
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
                nonBlank(point.getName(), "Spot tanpa nama"),
                distance,
                point.getRating(),
                point.getReviewCount(),
                badgeColor(point.getRating()),
                placeholder,
                false,
                point.getImageUrl(),
                searchText
        );
    }

    private void openDetailSpot(SpotUiModel spot) {
        Intent intent = new Intent(this, DetailSpotActivity.class);
        intent.putExtra(DetailSpotActivity.EXTRA_NAME, spot.getName());
        intent.putExtra(DetailSpotActivity.EXTRA_DISTANCE, spot.getDistance());
        intent.putExtra(DetailSpotActivity.EXTRA_RATING, spot.getRating());
        intent.putExtra(DetailSpotActivity.EXTRA_REVIEWS, spot.getReviewCount());
        intent.putExtra(DetailSpotActivity.EXTRA_IMAGE_RES, spot.getImageResId());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private int badgeColor(float rating) {
        if (rating >= 4.5f) {
            return Color.parseColor("#22C55E");
        }
        if (rating >= 4.0f) {
            return Color.parseColor("#00B4D8");
        }
        if (rating >= 3.5f) {
            return Color.parseColor("#F59E0B");
        }
        return Color.parseColor("#EF4444");
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
}
