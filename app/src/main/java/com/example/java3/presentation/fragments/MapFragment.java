package com.example.java3.presentation.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.java3.R;
import com.example.java3.core.utils.Constants;
import com.example.java3.data.remote.MarineHourlyResponse;
import com.example.java3.data.repository.FishingRepository;
import com.example.java3.databinding.FragmentMapBinding;
import com.example.java3.domain.model.FishingPoint;
import com.example.java3.presentation.viewmodels.MapViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.example.java3.presentation.activities.DetailSpotActivity;
import com.example.java3.presentation.maps.FishingMarkerRenderer;
import com.google.android.material.textfield.TextInputEditText;
import com.google.maps.android.clustering.ClusterManager;
import com.example.java3.domain.model.FishingClusterItem;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.bumptech.glide.Glide;
import com.example.java3.core.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MAP_FRAGMENT";
    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private MapViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private ClusterManager<FishingClusterItem> clusterManager;
    private List<FishingPoint> allPoints = new ArrayList<>();
    private Location latestUserLocation;
    private Polyline routeLine;
    private String currentUserId;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            Location lastLocation = locationResult.getLastLocation();
            if (lastLocation != null) {
                latestUserLocation = lastLocation;
            }
        }
    };

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    enableMyLocation();
                } else {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Izin lokasi diperlukan untuk fitur GPS.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);
        
        setupSearch();
        observeViewModel();

        binding.btnFilter.setOnClickListener(v -> showFilterDialog());
        binding.fabMyLocation.setOnClickListener(v -> centerOnUser());
        binding.fabMapType.setOnClickListener(v -> toggleMapType());
        binding.fabAddMarker.setOnClickListener(v -> showAddMarkerDialog(null));
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMarkers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMarkers(newText);
                return true;
            }
        });
    }

    private void showFilterDialog() {
        Context context = getContext();
        if (context == null) return;
        
        String[] types = {"Semua", "Pantai", "Muara", "Dermaga", "Bagan", "Rumpon", "Sungai", "Danau", "Tambak"};
        new MaterialAlertDialogBuilder(context)
                .setTitle("Filter Jenis Spot")
                .setItems(types, (dialog, which) -> {
                    if (which == 0) {
                        updateClusterItems(getVisiblePoints(allPoints));
                    } else {
                        filterMarkers(types[which]);
                    }
                })
                .show();
    }

    private void filterMarkers(String query) {
        if (query == null || query.isEmpty()) {
            updateClusterItems(allPoints);
            return;
        }
        String lowerQuery = query.toLowerCase();
        List<FishingPoint> filtered = new ArrayList<>();
        for (FishingPoint p : allPoints) {
            if ((p.getName() != null && p.getName().toLowerCase().contains(lowerQuery)) || 
                (p.getType() != null && p.getType().toLowerCase().contains(lowerQuery))) {
                filtered.add(p);
            }
        }
        updateClusterItems(filtered);
    }

    private void centerOnUser() {
        if (googleMap != null && binding != null && getContext() != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (binding == null || googleMap == null) return;
                if (location != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
                }
            });
        }
    }

    private void toggleMapType() {
        if (googleMap == null) return;
        int type = googleMap.getMapType();
        if (type == GoogleMap.MAP_TYPE_NORMAL) googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        else if (type == GoogleMap.MAP_TYPE_HYBRID) googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else if (type == GoogleMap.MAP_TYPE_SATELLITE) googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        else googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void observeViewModel() {
        viewModel.getFishingPointsLiveData().observe(getViewLifecycleOwner(), points -> {
            Log.d(TAG, "FIRESTORE_READ: " + points.size() + " points");
            this.allPoints = points;
            updateClusterItems(points);
        });
        
        viewModel.getWeatherLiveData().observe(getViewLifecycleOwner(), weather -> {
            if (binding != null && weather != null && weather.getMain() != null) {
                binding.tvCardWeather.setText(formatMapWeather(weather.getMain().getTemp(), weather.getMain().getHumidity()));
            }
        });

        viewModel.getTideLiveData().observe(getViewLifecycleOwner(), tide -> {
            if (binding != null && tide != null) {
                binding.tvCardTide.setText("Perairan: " + tide.getMarineSummary());
            }
        });

        viewModel.getMarineHourlyLiveData().observe(getViewLifecycleOwner(), marine -> {
            if (binding != null && marine != null && marine.getCurrentWaveHeight() > 0f) {
                binding.tvCardTide.setText(String.format(
                        Locale.getDefault(),
                        "Gelombang: %.1f m (%s), maks %.1f m",
                        marine.getCurrentWaveHeight(),
                        marine.getWaveLabel(),
                        marine.getTodayMaxWaveHeight()
                ));
            }
        });

        viewModel.getRecommendationLiveData().observe(getViewLifecycleOwner(), rec -> {
            if (binding != null) {
                binding.tvCardRecommendation.setText("Rekomendasi: " + rec);
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && getContext() != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateClusterItems(List<FishingPoint> points) {
        if (clusterManager == null || googleMap == null) {
            Log.w(TAG, "updateClusterItems: ClusterManager or GoogleMap not ready");
            return;
        }

        // Use a Handler to ensure this runs on the main thread and 
        // give the map a frame to settle if needed.
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                clusterManager.clearItems();
                if (points != null && !points.isEmpty()) {
                    for (FishingPoint point : points) {
                        if (point != null && point.getLatitude() != 0 && point.getLongitude() != 0) {
                            clusterManager.addItem(new FishingClusterItem(point));
                        }
                    }
                }
                clusterManager.cluster();
                Log.d(TAG, "updateClusterItems: Successfully updated " + (points != null ? points.size() : 0) + " markers");
            } catch (Exception e) {
                Log.e(TAG, "Error updating cluster items", e);
            }
        });
    }

    private String formatMapWeather(double temperature, int humidity) {
        return String.format(Locale.getDefault(), "Cuaca: %.1f\u00B0C, %d%% hum", temperature, humidity);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        Context context = getContext();
        if (context == null) return;
        try {
            this.googleMap = map;
            Log.d(TAG, "MAP_READY");

            UiSettings uiSettings = map.getUiSettings();
            uiSettings.setCompassEnabled(true);
            uiSettings.setRotateGesturesEnabled(true);
            uiSettings.setTiltGesturesEnabled(true);
            uiSettings.setMyLocationButtonEnabled(false);

            clusterManager = new ClusterManager<>(context, googleMap);
            clusterManager.setRenderer(new FishingMarkerRenderer(context, googleMap, clusterManager));
            
            googleMap.setOnCameraIdleListener(clusterManager);
            googleMap.setOnMarkerClickListener(clusterManager);
            
            clusterManager.setOnClusterItemClickListener(item -> {
                if (item != null && item.getData() != null) {
                    showSpotCard(item.getData());
                }
                return false;
            });

            googleMap.setOnMapClickListener(latLng -> {
                if (binding != null) binding.cardSpotInfo.setVisibility(View.GONE);
                clearRouteLine();
            });
            
            googleMap.setOnMapLongClickListener(latLng -> {
                FishingPoint p = new FishingPoint();
                p.setLatitude(latLng.latitude);
                p.setLongitude(latLng.longitude);
                showAddMarkerDialog(p);
            });

            LatLng tanjungAnom = new LatLng(Constants.TANJUNG_ANOM_LAT, Constants.TANJUNG_ANOM_LON);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(tanjungAnom, 12f));

            checkLocationPermissions();
            startLocationUpdates();
            
            if (!allPoints.isEmpty()) {
                updateClusterItems(getVisiblePoints(allPoints));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onMapReady", e);
        }
    }

    private void showSpotCard(FishingPoint point) {
        if (binding == null) return;
        binding.cardSpotInfo.setVisibility(View.VISIBLE);
        binding.tvCardSpotName.setText(point.getName());
        drawRouteLineToSpot(point);
        
        if (getContext() != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (binding == null) return;
                if (location != null) {
                    double dist = LocationUtils.calculateDistance(location.getLatitude(), location.getLongitude(), point.getLatitude(), point.getLongitude());
                    binding.tvCardSpotType.setText(String.format(Locale.getDefault(), "%s - %.1f km", point.getType(), dist));
                } else {
                    binding.tvCardSpotType.setText(point.getType());
                }
            });
        } else {
            binding.tvCardSpotType.setText(point.getType());
        }

        int placeholder = getSpotPlaceholder(point.getType());
        String imageUrl = point.getImageUrl();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(toCloudinaryThumbnailUrl(imageUrl))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .centerCrop()
                    .into(binding.ivCardSpot);
        } else {
            binding.ivCardSpot.setImageResource(placeholder);
        }
        
        binding.tvCardWeather.setText("Loading cuaca...");
        binding.tvCardTide.setText("Loading perairan...");
        binding.tvCardRecommendation.setText("Rekomendasi: menghitung...");

        loadEnvironmentForSpot(point);
        
        binding.btnCardDetail.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(requireContext(), DetailSpotActivity.class);
                intent.putExtra(DetailSpotActivity.EXTRA_ID, point.getId());
                intent.putExtra(DetailSpotActivity.EXTRA_NAME, point.getName());
                intent.putExtra(DetailSpotActivity.EXTRA_RATING, point.getRating());
                intent.putExtra(DetailSpotActivity.EXTRA_REVIEWS, point.getReviewCount());
                intent.putExtra(DetailSpotActivity.EXTRA_TYPE, point.getType());
                intent.putExtra(DetailSpotActivity.EXTRA_DESCRIPTION, point.getDescription());
                intent.putExtra(DetailSpotActivity.EXTRA_IMAGE_URL, point.getImageUrl());
                intent.putExtra(DetailSpotActivity.EXTRA_LATITUDE, point.getLatitude());
                intent.putExtra(DetailSpotActivity.EXTRA_LONGITUDE, point.getLongitude());
                intent.putExtra(DetailSpotActivity.EXTRA_OWNER_ID, point.getOwnerId());
                intent.putExtra(DetailSpotActivity.EXTRA_OWNER_NAME, point.getOwnerName());
                intent.putExtra(DetailSpotActivity.EXTRA_OWNER_PHOTO, point.getOwnerPhoto());
                intent.putExtra(DetailSpotActivity.EXTRA_VISIBILITY, point.getVisibility());
                intent.putExtra(DetailSpotActivity.EXTRA_CREATED_AT, point.getCreatedAt());

                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            double d = LocationUtils.calculateDistance(location.getLatitude(), location.getLongitude(), point.getLatitude(), point.getLongitude());
                            intent.putExtra(DetailSpotActivity.EXTRA_DISTANCE, d);
                        }
                        startActivity(intent);
                    });
                } else {
                    startActivity(intent);
                }
            }
        });

        binding.btnCardRoute.setOnClickListener(v -> {
            if (getContext() != null) {
                String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f", point.getLatitude(), point.getLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Intent fallback = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(String.format(Locale.ENGLISH,
                            "https://www.google.com/maps/search/?api=1&query=%f,%f", point.getLatitude(), point.getLongitude())));
                    startActivity(fallback);
                }
            }
        });

        boolean owner = FishingRepository.isOwnedByCurrentUser(point, currentUserId);
        binding.btnCardEdit.setVisibility(owner ? View.VISIBLE : View.GONE);
        binding.btnCardDelete.setVisibility(owner ? View.VISIBLE : View.GONE);
        binding.btnCardEdit.setOnClickListener(v -> {
            if (FishingRepository.isOwnedByCurrentUser(point, currentUserId)) {
                showAddMarkerDialog(point);
            } else {
                Toast.makeText(requireContext(), "Hanya pembuat spot yang dapat mengedit.", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnCardDelete.setOnClickListener(v -> {
            if (FishingRepository.isOwnedByCurrentUser(point, currentUserId)) {
                confirmDelete(point);
            } else {
                Toast.makeText(requireContext(), "Hanya pembuat spot yang dapat menghapus.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEnvironmentForSpot(FishingPoint point) {
        if (getContext() == null || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            viewModel.fetchWeatherAndTide(point.getLatitude(), point.getLongitude(), point.getRating(), 0.0);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (binding == null) return;
                    double distance = location != null
                            ? LocationUtils.calculateDistance(location.getLatitude(), location.getLongitude(), point.getLatitude(), point.getLongitude())
                            : 0.0;
                    Log.d(TAG, "spot environment uses spot location lat=" + point.getLatitude()
                            + " lon=" + point.getLongitude()
                            + " spot=" + point.getName()
                            + " userDistanceKm=" + distance);
                    viewModel.fetchWeatherAndTide(point.getLatitude(), point.getLongitude(), point.getRating(), distance);
                })
                .addOnFailureListener(error -> viewModel.fetchWeatherAndTide(point.getLatitude(), point.getLongitude(), point.getRating(), 0.0));
    }

    private void confirmDelete(FishingPoint point) {
        if (!FishingRepository.isOwnedByCurrentUser(point, currentUserId)) {
            if (getContext() != null) {
                Toast.makeText(requireContext(), "Hanya pembuat spot yang dapat menghapus.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        Context context = getContext();
        if (context == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle("Hapus Titik Pancing")
                .setMessage("Yakin ingin menghapus " + point.getName() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    viewModel.deleteFishingPoint(point.getId());
                    if (binding != null) binding.cardSpotInfo.setVisibility(View.GONE);
                    clearRouteLine();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showAddMarkerDialog(@Nullable FishingPoint existingPoint) {
        Context context = getContext();
        if (context == null) return;

        View v = LayoutInflater.from(context).inflate(R.layout.dialog_add_marker, null);
        TextInputEditText etName = v.findViewById(R.id.etName);
        AutoCompleteTextView actvType = v.findViewById(R.id.actvType);
        AutoCompleteTextView actvVisibility = v.findViewById(R.id.actvVisibility);
        TextInputEditText etLat = v.findViewById(R.id.etLat);
        TextInputEditText etLon = v.findViewById(R.id.etLon);

        String[] types = {"Pantai", "Muara", "Dermaga", "Bagan", "Rumpon", "Sungai", "Danau", "Tambak"};
        String[] visibilities = {"Pribadi", "Publik"};
        actvType.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, types));
        actvVisibility.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, visibilities));

        if (existingPoint != null) {
            if (existingPoint.getName() != null) etName.setText(existingPoint.getName());
            if (existingPoint.getType() != null) actvType.setText(existingPoint.getType(), false);
            etLat.setText(String.format(Locale.US, "%.6f", existingPoint.getLatitude()));
            etLon.setText(String.format(Locale.US, "%.6f", existingPoint.getLongitude()));
            actvVisibility.setText(isPublicVisibility(existingPoint.getVisibility()) ? "Publik" : "Pribadi", false);
        } else {
            actvVisibility.setText("Pribadi", false);
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(existingPoint == null || existingPoint.getId() == null ? "Tambah Titik Pancing" : "Edit Titik Pancing")
                .setView(v)
                .setCancelable(false)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    if (etName.getText() == null || actvType.getText() == null || etLat.getText() == null || etLon.getText() == null || actvVisibility.getText() == null) return;

                    String name = etName.getText().toString().trim();
                    String type = actvType.getText().toString().trim();
                    String visibilityInput = actvVisibility.getText().toString().trim();
                    String latS = etLat.getText().toString().trim().replace(",", ".");
                    String lonS = etLon.getText().toString().trim().replace(",", ".");

                    if (name.isEmpty() || type.isEmpty() || latS.isEmpty() || lonS.isEmpty()) {
                        Toast.makeText(context, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double latitude = Double.parseDouble(latS);
                        double longitude = Double.parseDouble(lonS);

                        FishingPoint p = existingPoint != null ? existingPoint : new FishingPoint();
                        p.setName(name);
                        p.setType(type);
                        p.setLatitude(latitude);
                        p.setLongitude(longitude);
                        p.setVisibility(isPublicVisibility(visibilityInput) ? "PUBLIC" : "PRIVATE");
                        if (existingPoint == null) {
                            p.setImageUrl("");
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                p.setOwnerId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                p.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                p.setOwnerName(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                                if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
                                    p.setOwnerPhoto(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                                }
                            }
                        }

                        if (p.getId() == null || p.getId().isEmpty()) {
                            p.setRating(5.0f);
                            viewModel.addFishingPoint(p);
                            Log.d(TAG, "MARKER_ADD: Triggered for " + name);
                            Toast.makeText(context, "Menyimpan titik pancing baru...", Toast.LENGTH_SHORT).show();
                        } else {
                            viewModel.updateFishingPoint(p);
                            Log.d(TAG, "MARKER_EDIT: Triggered for " + p.getId());
                            Toast.makeText(context, "Memperbarui data...", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing dialog data", e);
                        Toast.makeText(context, "Gagal menyimpan: Pastikan koordinat berupa angka", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void startLocationUpdates() {
        if (getContext() == null || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build();
        fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
    }

    private void checkLocationPermissions() {
        if (getContext() == null) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void enableMyLocation() {
        if (googleMap != null && getContext() != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    latestUserLocation = location;
                }
            });
        }
    }

    private void drawRouteLineToSpot(FishingPoint point) {
        if (googleMap == null || point == null || !hasValidCoordinate(point)) {
            return;
        }

        if (getContext() == null || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latestUserLocation = location;
                    }
                    drawDirectRouteLine(point);
                })
                .addOnFailureListener(error -> drawDirectRouteLine(point));
    }

    private void drawDirectRouteLine(FishingPoint point) {
        Context context = getContext();
        if (context == null || googleMap == null || latestUserLocation == null || point == null || !hasValidCoordinate(point)) {
            return;
        }

        clearRouteLine();
        LatLng user = new LatLng(latestUserLocation.getLatitude(), latestUserLocation.getLongitude());
        LatLng spot = new LatLng(point.getLatitude(), point.getLongitude());
        routeLine = googleMap.addPolyline(new PolylineOptions()
                .add(user, spot)
                .width(dpToPx(6))
                .color(ContextCompat.getColor(context, R.color.error))
                .geodesic(true)
                .zIndex(20f));

        try {
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(user)
                    .include(spot)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, dpToPx(80)));
        } catch (Exception e) {
            Log.w(TAG, "Unable to fit route line bounds", e);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(spot, 13f));
        }
    }

    private void clearRouteLine() {
        if (routeLine != null) {
            routeLine.remove();
            routeLine = null;
        }
    }

    private boolean hasValidCoordinate(FishingPoint point) {
        return point.getLatitude() != 0.0 || point.getLongitude() != 0.0;
    }

    private boolean isPublicVisibility(String visibility) {
        String value = visibility != null ? visibility.trim().toUpperCase(Locale.ROOT) : "";
        return "PUBLIC".equals(value) || "PUBLIK".equals(value) || "UMUM".equals(value);
    }

    private List<FishingPoint> getVisiblePoints(List<FishingPoint> points) {
        List<FishingPoint> visible = new ArrayList<>();
        if (points == null) {
            return visible;
        }
        for (FishingPoint point : points) {
            if (FishingRepository.canUserSeeSpot(point, currentUserId)) {
                visible.add(point);
            }
        }
        return visible;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private int getSpotPlaceholder(String type) {
        String safeType = type != null ? type.toLowerCase(Locale.ROOT) : "";
        if (safeType.contains("dermaga")) return R.drawable.img_spot_dermaga;
        if (safeType.contains("muara") || safeType.contains("sungai")) return R.drawable.img_spot_muara;
        if (safeType.contains("pantai")) return R.drawable.img_spot_pantai;
        if (safeType.contains("pulau")) return R.drawable.img_spot_pulau;
        if (safeType.contains("bagan") || safeType.contains("rumpon") || safeType.contains("tambak")) return R.drawable.img_spot_breakwater;
        return R.drawable.img_spot_dermaga;
    }

    private String toCloudinaryThumbnailUrl(String url) {
        if (url == null) return null;
        String trimmed = url.trim();
        if (!trimmed.contains("res.cloudinary.com") || !trimmed.contains("/upload/")) {
            return trimmed;
        }
        if (trimmed.contains("/upload/w_") || trimmed.contains("/upload/c_")) {
            return trimmed;
        }
        return trimmed.replace("/upload/", "/upload/w_240,h_240,c_fill,q_auto,f_auto/");
    }

    @Override public void onStart() { super.onStart(); if (binding != null) binding.mapView.onStart(); }
    @Override public void onResume() { super.onResume(); if (binding != null) binding.mapView.onResume(); }
    @Override public void onPause() { super.onPause(); if (binding != null) binding.mapView.onPause(); }
    @Override public void onStop() { super.onStop(); if (binding != null) binding.mapView.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (binding != null) binding.mapView.onLowMemory(); }
    @Override public void onDestroyView() { 
        clearRouteLine();
        if (binding != null) binding.mapView.onDestroy(); 
        super.onDestroyView(); 
        binding = null; 
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) { 
        super.onSaveInstanceState(out); 
        if (binding != null) binding.mapView.onSaveInstanceState(out);
    }
}
