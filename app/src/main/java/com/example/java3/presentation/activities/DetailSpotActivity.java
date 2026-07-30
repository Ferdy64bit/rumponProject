package com.example.java3.presentation.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.example.java3.R;
import com.example.java3.data.remote.MarineHourlyResponse;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.data.repository.FavoriteRepository;
import com.example.java3.data.repository.FishingRepository;
import com.example.java3.databinding.ActivityDetailSpotBinding;
import com.example.java3.domain.model.RecommendationResult;
import com.example.java3.domain.service.RecommendationEngine;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

public class DetailSpotActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_DISTANCE = "extra_distance";
    public static final String EXTRA_RATING = "extra_rating";
    public static final String EXTRA_REVIEWS = "extra_reviews";
    public static final String EXTRA_IMAGE_RES = "extra_image_res";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_OWNER_ID = "extra_owner_id";
    public static final String EXTRA_OWNER_NAME = "extra_owner_name";
    public static final String EXTRA_OWNER_PHOTO = "extra_owner_photo";
    public static final String EXTRA_VISIBILITY = "extra_visibility";
    public static final String EXTRA_CREATED_AT = "extra_created_at";

    private ActivityDetailSpotBinding binding;
    private FavoriteRepository favoriteRepository;
    private FishingRepository fishingRepository;
    private String pointId;
    private String spotName;
    private String spotType;
    private String description;
    private String imageUrl;
    private int imageResId;
    private double latitude;
    private double longitude;
    private double distance;
    private float rating;
    private int reviews;
    private String ownerId;
    private String ownerName;
    private String ownerPhoto;
    private String visibility;
    private long createdAt;
    private boolean isFavorite;
    private WeatherResponse latestWeather;
    private TideResponse latestTide;
    private MarineHourlyResponse latestMarine;
    private final MutableLiveData<WeatherResponse> weatherLiveData = new MutableLiveData<>();
    private final MutableLiveData<TideResponse> tideLiveData = new MutableLiveData<>();
    private final MutableLiveData<MarineHourlyResponse> marineLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final ActivityResultLauncher<Intent> spotPhotoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    uploadSpotPhoto(result.getData().getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailSpotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        favoriteRepository = new FavoriteRepository();
        fishingRepository = new FishingRepository();

        setupSystemBars();
        bindSpotData();
        observeLiveData();
        requestLiveEnvironment();
        setupListeners();
    }

    private void setupSystemBars() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    private void bindSpotData() {
        pointId = getIntent().getStringExtra(EXTRA_ID);
        spotName = nonBlank(getIntent().getStringExtra(EXTRA_NAME), "Spot memancing");
        spotType = nonBlank(getIntent().getStringExtra(EXTRA_TYPE), "Spot");
        description = nonBlank(getIntent().getStringExtra(EXTRA_DESCRIPTION), "Spot memancing pilihan komunitas Fishing Point.");
        imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        latitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0.0);
        longitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0.0);
        distance = getIntent().getDoubleExtra(EXTRA_DISTANCE, 0.0);
        rating = getIntent().getFloatExtra(EXTRA_RATING, 0.0f);
        reviews = getIntent().getIntExtra(EXTRA_REVIEWS, 0);
        ownerId = getIntent().getStringExtra(EXTRA_OWNER_ID);
        ownerName = getIntent().getStringExtra(EXTRA_OWNER_NAME);
        ownerPhoto = getIntent().getStringExtra(EXTRA_OWNER_PHOTO);
        visibility = nonBlank(getIntent().getStringExtra(EXTRA_VISIBILITY), "PUBLIC");
        createdAt = getIntent().getLongExtra(EXTRA_CREATED_AT, 0L);
        imageResId = getIntent().getIntExtra(EXTRA_IMAGE_RES, getSpotPlaceholder(spotType));

        binding.tvSpotName.setText(spotName);
        binding.tvDistance.setText(String.format(Locale.getDefault(), "%.1f km", distance));
        binding.tvInfoDistance.setText(String.format(Locale.getDefault(), "%.1f km", distance));
        binding.tvRating.setText(String.format(Locale.getDefault(), "%.1f (%d ulasan)", rating, reviews));
        binding.tvSpotOwner.setText(renderOwnerText());
        binding.tvSpotVisibility.setText(formatVisibilityLabel(visibility));
        renderSpotPhoto();
        loadFavoriteState();
        renderFallbackState();
    }

    private void observeLiveData() {
        weatherLiveData.observe(this, weather -> {
            latestWeather = weather;
            renderEnvironment();
            renderRecommendation();
        });
        tideLiveData.observe(this, tide -> {
            latestTide = tide;
            renderEnvironment();
            renderRecommendation();
        });
        marineLiveData.observe(this, marine -> {
            latestMarine = marine;
            renderEnvironment();
            renderRecommendation();
        });
        errorLiveData.observe(this, error -> {
            if (error != null && !error.trim().isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestLiveEnvironment() {
        if (latitude == 0.0 && longitude == 0.0) {
            return;
        }
        fishingRepository.getWeather(latitude, longitude, weatherLiveData, errorLiveData);
        fishingRepository.getTide(latitude, longitude, tideLiveData, errorLiveData);
        fishingRepository.getMarineHourly(latitude, longitude, marineLiveData, errorLiveData);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());
        binding.btnShare.setOnClickListener(v -> shareSpot());
        binding.btnNavigate.setOnClickListener(v -> navigateToSpot());
        binding.btnSaveFavorite.setOnClickListener(v -> toggleFavorite());
        binding.btnChangeSpotPhoto.setOnClickListener(v -> openSpotPhotoPicker());
        binding.btnDeleteSpotPhoto.setOnClickListener(v -> confirmDeleteSpotPhoto());
    }

    private void renderOwnerActions() {
        boolean owner = isCurrentUserOwner();
        binding.btnChangeSpotPhoto.setVisibility(owner ? View.VISIBLE : View.GONE);
        if (!owner) {
            binding.btnDeleteSpotPhoto.setVisibility(View.GONE);
        }
    }

    private void renderSpotPhoto() {
        if (binding == null) return;
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(imageResId)
                    .error(imageResId)
                    .centerCrop()
                    .into(binding.ivHeroImage);
            binding.btnDeleteSpotPhoto.setVisibility(isCurrentUserOwner() ? View.VISIBLE : View.GONE);
        } else {
            binding.ivHeroImage.setImageResource(imageResId);
            binding.btnDeleteSpotPhoto.setVisibility(View.GONE);
        }
    }

    private void openSpotPhotoPicker() {
        if (pointId == null || pointId.trim().isEmpty()) {
            Toast.makeText(this, "Simpan data spot terlebih dahulu sebelum mengganti foto.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        spotPhotoPickerLauncher.launch(Intent.createChooser(intent, "Pilih foto spot"));
    }

    private void uploadSpotPhoto(Uri uri) {
        try {
            setPhotoActionsEnabled(false);
            fishingRepository.uploadFishingPointPhoto(pointId, readUriBytes(uri), new FishingRepository.FirestoreCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        imageUrl = result;
                        renderSpotPhoto();
                        setPhotoActionsEnabled(true);
                        Toast.makeText(DetailSpotActivity.this, "Foto spot berhasil diperbarui.", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        setPhotoActionsEnabled(true);
                        Toast.makeText(DetailSpotActivity.this, nonBlank(error, "Gagal memperbarui foto spot."), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (IOException e) {
            setPhotoActionsEnabled(true);
            Toast.makeText(this, "Foto tidak dapat dibaca.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteSpotPhoto() {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            renderSpotPhoto();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hapus Foto Spot")
                .setMessage("Foto spot akan dikembalikan ke gambar default sesuai jenis spot.")
                .setPositiveButton("Hapus", (dialog, which) -> deleteSpotPhoto())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteSpotPhoto() {
        setPhotoActionsEnabled(false);
        fishingRepository.updateFishingPointPhoto(pointId, "", new FishingRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    imageUrl = "";
                    renderSpotPhoto();
                    setPhotoActionsEnabled(true);
                    Toast.makeText(DetailSpotActivity.this, "Foto spot dikembalikan ke default.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setPhotoActionsEnabled(true);
                    Toast.makeText(DetailSpotActivity.this, nonBlank(error, "Gagal menghapus foto spot."), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setPhotoActionsEnabled(boolean enabled) {
        if (binding == null) return;
        binding.btnChangeSpotPhoto.setEnabled(enabled);
        binding.btnDeleteSpotPhoto.setEnabled(enabled);
        binding.btnChangeSpotPhoto.setAlpha(enabled ? 1f : 0.5f);
        binding.btnDeleteSpotPhoto.setAlpha(enabled ? 1f : 0.5f);
    }

    private byte[] readUriBytes(Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("Input stream kosong");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        }
    }

    private void toggleFavorite() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null || pointId == null || pointId.trim().isEmpty()) {
            Toast.makeText(this, "Login dan data spot diperlukan untuk menyimpan favorit.", Toast.LENGTH_SHORT).show();
            return;
        }
        favoriteRepository.toggleFavorite(userId, pointId, new FavoriteRepository.FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isFavorite = Boolean.TRUE.equals(result);
                refreshFavoriteState(true);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(DetailSpotActivity.this, nonBlank(error, "Gagal memperbarui favorit."), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavoriteState() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId == null || pointId == null || pointId.trim().isEmpty()) {
            refreshFavoriteState(false);
            return;
        }
        favoriteRepository.isFavorite(userId, pointId, new FavoriteRepository.FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                isFavorite = Boolean.TRUE.equals(result);
                refreshFavoriteState(false);
            }

            @Override
            public void onFailure(String error) {
                refreshFavoriteState(false);
            }
        });
    }

    private void refreshFavoriteState(boolean showToast) {
        binding.btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_rounded : R.drawable.ic_heart_outline);
        binding.btnSaveFavorite.setText(isFavorite ? "Hapus dari Favorit" : getString(R.string.save_favorite));
        if (showToast) {
            Toast.makeText(this, isFavorite ? "Spot disimpan ke favorit." : "Spot dihapus dari favorit.", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderFallbackState() {
        binding.tvDetailRecommendationTitle.setText("Memuat data live...");
        binding.tvDetailRecommendationStars.setText("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¹Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¹Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¹Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¹Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¹Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â ");
        binding.tvDetailRecommendationPercent.setText("--%");
        binding.progressDetailRecommendation.setProgress(0);
        binding.tvWeatherValue.setText("Memuat");
        binding.tvWindValue.setText("Memuat");
        binding.tvHumidityValue.setText("Memuat");
        binding.tvFishActivityValue.setText("Memuat");
        binding.tvTideValue.setText("Memuat");
    }

    private void renderEnvironment() {
        if (binding == null) {
            return;
        }

        if (latestWeather != null && latestWeather.getMain() != null) {
            String weatherText = latestWeather.getWeather() != null && !latestWeather.getWeather().isEmpty()
                    ? capitalize(latestWeather.getWeather().get(0).getDescription())
                    : "Cuaca tidak diketahui";
            binding.tvWeatherValue.setText(weatherText);
            binding.tvHumidityValue.setText(String.format(Locale.getDefault(), "%d%%", latestWeather.getMain().getHumidity()));
            if (latestWeather.getWind() != null) {
                binding.tvWindValue.setText(String.format(Locale.getDefault(), "%.1f km/h", latestWeather.getWind().getSpeed() * 3.6));
            }
        }

        if (latestMarine != null) {
            float wave = latestMarine.getCurrentWaveHeight();
            String waveLabel = wave > 0f ? String.format(Locale.getDefault(), "%s (%.1f m)", latestMarine.getWaveLabel(), wave) : "Gelombang tidak tersedia";
            binding.tvTideValue.setText(waveLabel);
            binding.tvFishActivityValue.setText(RecommendationEngine.getFishActivityLabel(latestTide, latestWeather, latestMarine));
        }

        if ((latestMarine == null || latestMarine.getCurrentWaveHeight() <= 0f)
                && latestTide != null
                && latestTide.getForecasts() != null
                && !latestTide.getForecasts().isEmpty()) {
            binding.tvTideValue.setText(nonBlank(latestTide.getForecasts().get(0).getWaveLabel(), binding.tvTideValue.getText().toString()));
        }
    }

    private void renderRecommendation() {
        RecommendationResult result = RecommendationEngine.calculate(latestTide, latestWeather, latestMarine, distance, rating);
        binding.tvDetailRecommendationStars.setText(createStars(result.getStars()));
        binding.tvDetailRecommendationTitle.setText(result.getBadgeText());
        binding.tvDetailRecommendationPercent.setText(String.format(Locale.getDefault(), "%d%%", result.getScorePercentage()));
        binding.progressDetailRecommendation.setProgress(result.getScorePercentage());
        binding.progressDetailRecommendation.setIndicatorColor(result.getBadgeColor());
        binding.tvDetailRecommendationTitle.setTextColor(result.getBadgeColor());
        binding.tvDetailRecommendationStars.setTextColor(result.getBadgeColor());
        binding.tvDetailRecommendationPercent.setTextColor(result.getBadgeColor());
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

    private void shareSpot() {
        String coordinate = latitude != 0.0 || longitude != 0.0
                ? String.format(Locale.ENGLISH, "https://www.google.com/maps/search/?api=1&query=%f,%f", latitude, longitude)
                : "";
        String text = "Fishing Point\n\n"
                + spotName + "\n"
                + "Jenis spot: " + spotType + "\n"
                + description
                + (coordinate.isEmpty() ? "" : "\n\nLokasi: " + coordinate);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Bagikan spot"));
    }

    private void navigateToSpot() {
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Koordinat spot belum tersedia.", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.parse(String.format(Locale.ENGLISH, "google.navigation:q=%f,%f", latitude, longitude));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(intent);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.ENGLISH,
                    "https://www.google.com/maps/search/?api=1&query=%f,%f", latitude, longitude))));
        }
    }

    private String renderOwnerText() {
        String name = nonBlank(ownerName, "Fishing Point Member");
        String dateText = createdAt > 0 ? formatDate(createdAt) : "tanggal tidak tersedia";
        return String.format(Locale.getDefault(), "Dibuat oleh: %s - %s", name, dateText);
    }

    private String formatVisibilityLabel(String value) {
        return "PRIVATE".equalsIgnoreCase(value) ? "PRIVATE" : "PUBLIC";
    }

    private boolean isCurrentUserOwner() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        return userId != null && ownerId != null && userId.equals(ownerId);
    }

    private String formatDate(long millis) {
        return java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, new Locale("id", "ID")).format(new Date(millis));
    }

    private String capitalize(String value) {
        if (value == null || value.trim().isEmpty()) return "Cuaca tidak diketahui";
        String trimmed = value.trim();
        return trimmed.substring(0, 1).toUpperCase(Locale.getDefault()) + trimmed.substring(1);
    }

    private String nonBlank(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private int getSpotPlaceholder(String type) {
        String safeType = type != null ? type.toLowerCase(Locale.ROOT) : "";
        if (safeType.contains("pantai")) return R.drawable.img_spot_pantai;
        if (safeType.contains("muara") || safeType.contains("sungai")) return R.drawable.img_spot_muara;
        if (safeType.contains("bagan") || safeType.contains("rumpon") || safeType.contains("tambak")) return R.drawable.img_spot_breakwater;
        if (safeType.contains("pulau")) return R.drawable.img_spot_pulau;
        return R.drawable.img_spot_dermaga;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
