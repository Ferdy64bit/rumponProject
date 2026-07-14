package com.example.java3.presentation.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.java3.R;
import com.example.java3.core.utils.Constants;
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
import com.example.java3.presentation.activities.DetailSpotActivity;
import com.example.java3.presentation.maps.FishingMarkerRenderer;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.maps.android.clustering.ClusterManager;
import com.example.java3.domain.model.FishingClusterItem;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

/**
 * MapFragment - Google Maps implementation.
 * Goal: Display Google Maps successfully inside the existing Map Fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private MapViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private ClusterManager<FishingClusterItem> clusterManager;

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (googleMap != null) {
                    LatLng userPos = new LatLng(location.getLatitude(), location.getLongitude());
                    // Real-time camera follow can be toggled, but here we just ensure the dot is updated
                }
            }
        }
    };

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    enableMyLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    enableMyLocation();
                } else {
                    Toast.makeText(requireContext(), "Izin lokasi ditolak. Peta tidak dapat menampilkan posisi Anda.", Toast.LENGTH_SHORT).show();
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);
        observeViewModel();

        binding.fabMyLocation.setOnClickListener(v -> {
            if (googleMap != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
                    }
                });
            }
        });

        binding.fabMapType.setOnClickListener(v -> toggleMapType());

        binding.fabAddMarker.setOnClickListener(v -> showAddMarkerDialog(0, 0, false));
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
        viewModel.getFishingPointsLiveData().observe(getViewLifecycleOwner(), this::updateClusterItems);
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateClusterItems(List<FishingPoint> points) {
        if (clusterManager == null) return;
        clusterManager.clearItems();
        for (FishingPoint point : points) {
            clusterManager.addItem(new FishingClusterItem(point));
        }
        clusterManager.cluster();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // 1. Interactions
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setCompassEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);
        uiSettings.setMyLocationButtonEnabled(false);

        // 2. Clustering
        clusterManager = new ClusterManager<>(requireContext(), googleMap);
        clusterManager.setRenderer(new FishingMarkerRenderer(requireContext(), googleMap, clusterManager));
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        
        clusterManager.setOnClusterItemClickListener(item -> {
            showSpotCard(item.getData());
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> {
            binding.cardSpotInfo.setVisibility(View.GONE);
        });

        googleMap.setOnMapLongClickListener(latLng -> {
            showAddMarkerDialog(latLng.latitude, latLng.longitude, true);
        });

        // 3. Default Position
        LatLng tanjungAnom = new LatLng(Constants.TANJUNG_ANOM_LAT, Constants.TANJUNG_ANOM_LON);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(tanjungAnom, 13f));

        checkLocationPermissions();
        startLocationUpdates();
    }

    private void showSpotCard(FishingPoint point) {
        binding.cardSpotInfo.setVisibility(View.VISIBLE);
        binding.tvCardSpotName.setText(point.getName());
        binding.tvCardSpotType.setText(point.getType());
        
        binding.btnCardDetail.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DetailSpotActivity.class);
            intent.putExtra("pointId", point.getId());
            startActivity(intent);
        });

        binding.btnCardRoute.setOnClickListener(v -> {
            // Intent for Google Maps Navigation
            String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f", point.getLatitude(), point.getLongitude());
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });
    }

    private void showAddMarkerDialog(double lat, double lon, boolean isFromLongClick) {
        if (getContext() == null) return;

        try {
            // Using standard AlertDialog to avoid Material3 theme issues causing ResourceNotFoundException 0x0
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_marker, null);
            
            TextInputEditText etName = dialogView.findViewById(R.id.etName);
            AutoCompleteTextView actvType = dialogView.findViewById(R.id.actvType);
            TextInputEditText etLat = dialogView.findViewById(R.id.etLat);
            TextInputEditText etLon = dialogView.findViewById(R.id.etLon);

            if (etName == null || actvType == null || etLat == null || etLon == null) {
                Toast.makeText(requireContext(), "Error: View dialog tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            // Setup Dropdown for Type
            String[] types = {"Pantai", "Muara", "Dermaga", "Sungai", "Danau", "Tambak"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, types);
            actvType.setAdapter(adapter);

            if (isFromLongClick) {
                etLat.setText(String.valueOf(lat));
                etLon.setText(String.valueOf(lon));
            }

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Tambah Titik Pancing")
                    .setView(dialogView)
                    .setPositiveButton("Simpan", (dialog, which) -> {
                        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                        String type = actvType.getText() != null ? actvType.getText().toString().trim() : "";
                        String latStr = etLat.getText() != null ? etLat.getText().toString().trim() : "";
                        String lonStr = etLon.getText() != null ? etLon.getText().toString().trim() : "";

                        if (name.isEmpty() || type.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) {
                            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            double latitude = Double.parseDouble(latStr);
                            double longitude = Double.parseDouble(lonStr);

                            FishingPoint newPoint = new FishingPoint();
                            newPoint.setName(name);
                            newPoint.setType(type);
                            newPoint.setLatitude(latitude);
                            newPoint.setLongitude(longitude);
                            newPoint.setRating(5.0f);

                            viewModel.addFishingPoint(newPoint);
                            Toast.makeText(requireContext(), "Titik pancing berhasil disimpan", Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Format koordinat salah", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Gagal menampilkan dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateDistanceMeters(10)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.getMainLooper());
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void enableMyLocation() {
        if (googleMap != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    // --- Mandatory MapView Lifecycle Management ---

    @Override
    public void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            binding.mapView.onDestroy();
        }
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            binding.mapView.onSaveInstanceState(outState);
        }
    }
}
