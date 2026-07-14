package com.example.java3.presentation.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.java3.R;
import com.example.java3.databinding.ActivityDetailSpotBinding;

import java.util.Locale;

public class DetailSpotActivity extends AppCompatActivity {
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_DISTANCE = "extra_distance";
    public static final String EXTRA_RATING = "extra_rating";
    public static final String EXTRA_REVIEWS = "extra_reviews";
    public static final String EXTRA_IMAGE_RES = "extra_image_res";

    private ActivityDetailSpotBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailSpotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupSystemBars();
        bindSpotData();
        setupListeners();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    private void bindSpotData() {
        String name = getIntent().getStringExtra(EXTRA_NAME);
        if (name == null || name.trim().isEmpty()) {
            name = "Spot memancing";
        }

        double distance = getIntent().getDoubleExtra(EXTRA_DISTANCE, 0.0);
        float rating = getIntent().getFloatExtra(EXTRA_RATING, 0.0f);
        int reviews = getIntent().getIntExtra(EXTRA_REVIEWS, 0);
        int imageRes = getIntent().getIntExtra(EXTRA_IMAGE_RES, R.drawable.img_spot_dermaga);

        binding.tvSpotName.setText(name);
        binding.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", distance));
        binding.tvInfoDistance.setText(String.format(Locale.getDefault(), "%.1f km", distance));
        binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f (%d ulasan)", rating, reviews));
        binding.ivHeroImage.setImageResource(imageRes);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnFavorite.setOnClickListener(v -> showUnavailableMessage());
        binding.btnShare.setOnClickListener(v -> showUnavailableMessage());
        binding.btnNavigate.setOnClickListener(v -> showUnavailableMessage());
        binding.btnSaveFavorite.setOnClickListener(v -> showUnavailableMessage());
    }

    private void showUnavailableMessage() {
        Toast.makeText(this, "Fitur detail spot ini belum tersedia.", Toast.LENGTH_SHORT).show();
    }
}
