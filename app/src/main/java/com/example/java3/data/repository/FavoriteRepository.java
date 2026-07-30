package com.example.java3.data.repository;

import com.example.java3.core.utils.Constants;
import com.example.java3.domain.model.Favorite;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FavoriteRepository {
    private final FirebaseFirestore firestore;

    public FavoriteRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public void toggleFavorite(String userId, String pointId, FirestoreCallback<Boolean> callback) {
        String docId = userId + "_" + pointId;
        firestore.collection(Constants.COL_FAVORITES)
                .document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Unfavorite
                        firestore.collection(Constants.COL_FAVORITES).document(docId).delete()
                                .addOnSuccessListener(aVoid -> callback.onSuccess(false))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    } else {
                        // Favorite
                        Favorite favorite = new Favorite(docId, userId, pointId, System.currentTimeMillis());
                        firestore.collection(Constants.COL_FAVORITES).document(docId).set(favorite)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void isFavorite(String userId, String pointId, FirestoreCallback<Boolean> callback) {
        String docId = userId + "_" + pointId;
        firestore.collection(Constants.COL_FAVORITES)
                .document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> callback.onSuccess(documentSnapshot.exists()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getUserFavorites(String userId, FirestoreCallback<List<Favorite>> callback) {
        firestore.collection(Constants.COL_FAVORITES)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.toObjects(Favorite.class));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
