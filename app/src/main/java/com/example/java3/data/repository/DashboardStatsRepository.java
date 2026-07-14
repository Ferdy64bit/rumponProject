package com.example.java3.data.repository;

import com.example.java3.core.utils.Constants;
import com.example.java3.domain.model.DashboardStats;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class DashboardStatsRepository {
    private final FirebaseFirestore firestore;

    public DashboardStatsRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface StatsCallback {
        void onChanged(DashboardStats stats);
        void onFailure(String error);
    }

    public List<ListenerRegistration> listenStats(String userId, StatsCallback callback) {
        DashboardStats stats = new DashboardStats();
        List<ListenerRegistration> registrations = new ArrayList<>();

        registrations.add(firestore.collection(Constants.COL_FISHING_POINTS)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    stats.setSpotCount(snapshots != null ? snapshots.size() : 0);
                    callback.onChanged(new DashboardStats(stats));
                }));

        registrations.add(firestore.collection(Constants.COL_FAVORITES)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    stats.setFavoriteCount(snapshots != null ? snapshots.size() : 0);
                    callback.onChanged(new DashboardStats(stats));
                }));

        registrations.add(firestore.collection(Constants.COL_REVIEWS)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    stats.setReviewCount(snapshots != null ? snapshots.size() : 0);
                    callback.onChanged(new DashboardStats(stats));
                }));

        registrations.add(firestore.collection(Constants.COL_COMMUNITY_POSTS)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    stats.setPostCount(snapshots != null ? snapshots.size() : 0);
                    callback.onChanged(new DashboardStats(stats));
                }));

        return registrations;
    }
}
