package com.example.java3.data.repository;

import com.example.java3.core.utils.Constants;
import com.example.java3.domain.model.Review;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class ReviewRepository {
    private final FirebaseFirestore firestore;

    public ReviewRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public void addReview(Review review, FirestoreCallback<Void> callback) {
        firestore.collection(Constants.COL_REVIEWS)
                .document(review.getId())
                .set(review)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getReviewsByPoint(String pointId, FirestoreCallback<List<Review>> callback) {
        firestore.collection(Constants.COL_REVIEWS)
                .whereEqualTo("pointId", pointId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.toObjects(Review.class));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
