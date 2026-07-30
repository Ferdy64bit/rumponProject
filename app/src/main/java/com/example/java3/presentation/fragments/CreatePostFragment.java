package com.example.java3.presentation.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.data.repository.CommunityRepository;
import com.example.java3.data.repository.TideRepository;
import com.example.java3.data.repository.WeatherRepository;
import com.example.java3.core.utils.Constants;
import com.example.java3.databinding.FragmentCreatePostBinding;
import com.example.java3.domain.model.Post;
import com.example.java3.presentation.viewmodels.CommunityViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreatePostFragment extends Fragment {
    private static final String TAG = "CreatePostFragment";
    private static final int MAX_IMAGE_DIMENSION = 1280;

    private FragmentCreatePostBinding binding;
    private CommunityViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherRepository weatherRepository;
    private TideRepository tideRepository;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    
    private Uri selectedImageUri;
    private double currentLat;
    private double currentLon;
    private String currentWeather;
    private String currentTide;
    private boolean isPosting;
    private Toast currentToast;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (binding != null && selectedImageUri != null) {
                        binding.ivSelectedImage.setImageURI(selectedImageUri);
                        binding.ivSelectedImage.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
                        binding.layoutAddImage.setVisibility(View.GONE);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    showMessage("Izin lokasi diperlukan untuk fitur ini");
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CommunityViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        weatherRepository = new WeatherRepository();
        tideRepository = new TideRepository();

        setupListeners();
        checkLocationPermission();
        observeViewModel();
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.cardImage.setOnClickListener(v -> openImagePicker());
        binding.btnPost.setOnClickListener(v -> validateAndPost());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        try {
            CancellationTokenSource tokenSource = new CancellationTokenSource();
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.getToken()).addOnSuccessListener(location -> {
                if (location != null && isAdded() && binding != null) {
                    applyDetectedLocation(location);
                } else {
                    requestLastKnownLocation();
                }
            }).addOnFailureListener(error -> requestLastKnownLocation());
        } catch (SecurityException e) {
            Log.w(TAG, "Location permission was revoked before reading location", e);
            useFallbackLocation();
        }
    }

    private void requestLastKnownLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null && isAdded() && binding != null) {
                            applyDetectedLocation(location);
                        } else {
                            useFallbackLocation();
                        }
                    })
                    .addOnFailureListener(error -> useFallbackLocation());
        } catch (SecurityException e) {
            Log.w(TAG, "Location permission was revoked before reading last known location", e);
            useFallbackLocation();
        }
    }

    private void applyDetectedLocation(Location location) {
        currentLat = location.getLatitude();
        currentLon = location.getLongitude();
        Log.d(TAG, "post location provider=" + location.getProvider()
                + " lat=" + currentLat
                + " lon=" + currentLon
                + " mock=" + location.isFromMockProvider());
        getAddressFromLocation(location);
        fetchWeatherAndTide(location);
    }

    private void useFallbackLocation() {
        if (!isAdded() || binding == null) {
            return;
        }
        Location fallback = new Location("tanjung_anom_fallback");
        fallback.setLatitude(Constants.TANJUNG_ANOM_LAT);
        fallback.setLongitude(Constants.TANJUNG_ANOM_LON);
        currentLat = fallback.getLatitude();
        currentLon = fallback.getLongitude();
        binding.tvLocationName.setText("Desa Tanjung Anom (fallback GPS)");
        fetchWeatherAndTide(fallback);
    }

    private void getAddressFromLocation(Location location) {
        if (!isAdded() || binding == null) {
            return;
        }

        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                binding.tvLocationName.setText(address);
            }
        } catch (IOException e) {
            binding.tvLocationName.setText("Lokasi terdeteksi (" + location.getLatitude() + ", " + location.getLongitude() + ")");
        }
    }

    private void fetchWeatherAndTide(Location location) {
        MutableLiveData<WeatherResponse> weatherData = new MutableLiveData<>();
        MutableLiveData<TideResponse> tideData = new MutableLiveData<>();
        MutableLiveData<String> errorData = new MutableLiveData<>();

        weatherRepository.getCurrentWeather(location.getLatitude(), location.getLongitude(), weatherData, errorData);
        tideRepository.getTide(location.getLatitude(), location.getLongitude(), tideData, errorData);

        errorData.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                showMessage(error);
            }
        });

        weatherData.observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getWeather() != null && !response.getWeather().isEmpty()) {
                currentWeather = response.getWeather().get(0).getDescription();
                binding.tvWeather.setText("Cuaca: " + currentWeather);
            }
        });

        tideData.observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                currentTide = response.getMarineSummary();
                binding.tvTide.setText("Perairan: " + currentTide);
            }
        });
    }

    private void validateAndPost() {
        if (binding == null || isPosting) {
            return;
        }

        binding.etCaption.setError(null);

        String caption = readText(binding.etCaption.getText());
        if (selectedImageUri == null) {
            showMessage("Pilih foto tangkapan terlebih dahulu");
            return;
        }
        if (caption.isEmpty()) {
            binding.etCaption.setError("Caption wajib diisi");
            binding.etCaption.requestFocus();
            showMessage("Berikan caption untuk postinganmu");
            return;
        }

        Post post = buildPost(caption);
        Uri imageUri = selectedImageUri;
        Context appContext = requireContext().getApplicationContext();
        setPosting(true);

        imageExecutor.execute(() -> {
            try {
                byte[] imageData = compressImage(appContext, imageUri);
                mainHandler.post(() -> submitPost(post, imageData));
            } catch (IOException | RuntimeException e) {
                Log.e(TAG, "Failed to process selected image", e);
                mainHandler.post(() -> {
                    setPosting(false);
                    showMessage("Gagal memproses gambar");
                });
            }
        });
    }

    private Post buildPost(String caption) {
        Post post = new Post();
        post.setCaption(caption);
        post.setFishType(readText(binding.etFishType.getText()));
        post.setLatitude(currentLat);
        post.setLongitude(currentLon);
        post.setLocationName(binding.tvLocationName.getText().toString());
        post.setWeatherCondition(currentWeather);
        post.setTideStatus(currentTide);
        return post;
    }

    private void submitPost(Post post, byte[] imageData) {
        if (!isAdded() || binding == null) {
            return;
        }

        CommunityRepository repository = new CommunityRepository();
        repository.createPost(post, imageData, new CommunityRepository.RepositoryCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (isAdded() && binding != null) {
                    setPosting(false);
                    showMessage("Postingan berhasil dibagikan!");
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Create post failed: " + message);
                if (isAdded() && binding != null) {
                    setPosting(false);
                    showMessage("Gagal membuat postingan: " + message);
                }
            }
        });
    }

    private byte[] compressImage(Context context, Uri uri) throws IOException {
        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        try (InputStream boundsStream = context.getContentResolver().openInputStream(uri)) {
            if (boundsStream == null) {
                throw new IOException("Gambar tidak ditemukan");
            }
            BitmapFactory.decodeStream(boundsStream, null, boundsOptions);
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = calculateInSampleSize(boundsOptions);
        decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bitmap;
        try (InputStream imageStream = context.getContentResolver().openInputStream(uri)) {
            if (imageStream == null) {
                throw new IOException("Gambar tidak ditemukan");
            }
            bitmap = BitmapFactory.decodeStream(imageStream, null, decodeOptions);
        }

        if (bitmap == null) {
            throw new IOException("Gambar tidak dapat dibaca");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        bitmap.recycle();

        if (!compressed) {
            throw new IOException("Gagal mengompres gambar");
        }
        return baos.toByteArray();
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height <= 0 || width <= 0) {
            return inSampleSize;
        }

        while ((height / inSampleSize) > MAX_IMAGE_DIMENSION || (width / inSampleSize) > MAX_IMAGE_DIMENSION) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    private String readText(@Nullable CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }

    private void setPosting(boolean posting) {
        isPosting = posting;
        if (binding == null) {
            return;
        }
        binding.loadingOverlay.setVisibility(posting ? View.VISIBLE : View.GONE);
        binding.btnPost.setEnabled(!posting);
        binding.cardImage.setEnabled(!posting);
        binding.btnPost.setText(posting ? "Mengunggah..." : "Posting Sekarang");
    }

    private void showMessage(String message) {
        if (!isAdded()) {
            return;
        }
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void observeViewModel() {
        // Observers for global error/loading if needed
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageExecutor.shutdownNow();
    }
}
