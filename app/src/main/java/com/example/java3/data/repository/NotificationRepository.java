package com.example.java3.data.repository;

import com.example.java3.core.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class NotificationRepository {
    private final FirebaseFirestore firestore;

    public NotificationRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface CountCallback {
        void onCountChanged(int count);
        void onFailure(String error);
    }

    public ListenerRegistration listenUnreadCount(String userId, CountCallback callback) {
        return firestore.collection(Constants.COL_NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }

                    int count = 0;
                    if (snapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : snapshots.getDocuments()) {
                            Boolean isRead = document.getBoolean("isRead");
                            if (isRead == null) {
                                isRead = document.getBoolean("read");
                            }
                            if (isRead == null || !isRead) {
                                count++;
                            }
                        }
                    }
                    callback.onCountChanged(count);
                });
    }
}
