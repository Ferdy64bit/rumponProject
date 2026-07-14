package com.example.java3.data.repository;

import com.example.java3.core.utils.Constants;
import com.example.java3.domain.model.UserProfileSummary;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class UserRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public UserRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface UserCallback {
        void onSuccess(UserProfileSummary user);
        void onFailure(String error);
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public ListenerRegistration listenCurrentUser(UserCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Sesi login tidak ditemukan.");
            return null;
        }

        return firestore.collection(Constants.COL_USERS)
                .document(currentUser.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        callback.onSuccess(fromFirebaseUser(currentUser));
                        return;
                    }

                    callback.onSuccess(fromDocument(currentUser, snapshot));
                });
    }

    private UserProfileSummary fromDocument(FirebaseUser currentUser, DocumentSnapshot snapshot) {
        String email = firstNonBlank(snapshot.getString("email"), currentUser.getEmail());
        String displayName = resolveDisplayName(
                email,
                snapshot.getString("nama"),
                snapshot.getString("fullName"),
                currentUser.getDisplayName()
        );
        String photoUrl = firstNonBlank(
                snapshot.getString("photo"),
                snapshot.getString("photoUrl"),
                currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null
        );
        String role = firstNonBlank(snapshot.getString("role"), "user");
        return new UserProfileSummary(currentUser.getUid(), displayName, email, photoUrl, role);
    }

    private UserProfileSummary fromFirebaseUser(FirebaseUser user) {
        String displayName = resolveDisplayName(user.getEmail(), user.getDisplayName());
        String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
        return new UserProfileSummary(user.getUid(), displayName, user.getEmail(), photoUrl, "user");
    }

    private String resolveDisplayName(String email, String... candidates) {
        for (String candidate : candidates) {
            if (isHumanName(candidate)) {
                return candidate.trim();
            }
        }

        String emailName = nameFromEmail(email);
        return isHumanName(emailName) ? emailName : "Pemancing";
    }

    private boolean isHumanName(String value) {
        return value != null && !value.trim().isEmpty() && !value.contains("@");
    }

    private String nameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }

        String localPart = email.substring(0, email.indexOf('@'))
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();
        if (localPart.isEmpty() || localPart.length() > 12) {
            return "";
        }

        String[] words = localPart.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(word.substring(0, 1).toUpperCase())
                    .append(word.length() > 1 ? word.substring(1).toLowerCase() : "");
        }
        return builder.toString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
