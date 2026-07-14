package com.example.java3.data.repository;

import com.example.java3.core.utils.Constants;
import com.example.java3.domain.model.FishingPoint;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpotRepository {
    private final FirebaseFirestore firestore;

    public SpotRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public ListenerRegistration listenFishingPoints(FirestoreCallback<List<FishingPoint>> callback) {
        return firestore.collection(Constants.COL_FISHING_POINTS)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }

                    List<FishingPoint> points = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot document : snapshots.getDocuments()) {
                            FishingPoint point = document.toObject(FishingPoint.class);
                            if (point != null) {
                                if (point.getId() == null || point.getId().trim().isEmpty()) {
                                    point.setId(document.getId());
                                }
                                points.add(point);
                            }
                        }
                    }
                    callback.onSuccess(points);
                });
    }

    public void getFishingPoints(FirestoreCallback<List<FishingPoint>> callback) {
        firestore.collection(Constants.COL_FISHING_POINTS)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<FishingPoint> points = new ArrayList<>();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        FishingPoint point = document.toObject(FishingPoint.class);
                        if (point != null) {
                            if (point.getId() == null || point.getId().trim().isEmpty()) {
                                point.setId(document.getId());
                            }
                            points.add(point);
                        }
                    }
                    callback.onSuccess(points);
                })
                .addOnFailureListener(error -> callback.onFailure(error.getMessage()));
    }

    public void searchFishingPoints(String query, FirestoreCallback<List<FishingPoint>> callback) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        getFishingPoints(new FirestoreCallback<List<FishingPoint>>() {
            @Override
            public void onSuccess(List<FishingPoint> result) {
                if (normalizedQuery.isEmpty()) {
                    callback.onSuccess(result);
                    return;
                }

                List<FishingPoint> filtered = new ArrayList<>();
                for (FishingPoint point : result) {
                    if (matches(point, normalizedQuery)) {
                        filtered.add(point);
                    }
                }
                callback.onSuccess(filtered);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    private boolean matches(FishingPoint point, String query) {
        return contains(point.getName(), query)
                || contains(point.getType(), query)
                || contains(point.getFishType(), query)
                || contains(point.getArea(), query)
                || contains(point.getLocationName(), query)
                || contains(point.getDescription(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
