package com.example.java3.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Wrapper for FishingPoint to be used with Google Maps Clustering.
 */
public class FishingClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final FishingPoint data;

    public FishingClusterItem(FishingPoint point) {
        this.data = point;
        this.position = new LatLng(point.getLatitude(), point.getLongitude());
        this.title = point.getName();
        this.snippet = point.getType();
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
    }

    public FishingPoint getData() {
        return data;
    }
}
