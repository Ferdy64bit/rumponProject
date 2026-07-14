package com.example.java3.core.managers;

import com.example.java3.core.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * FirebaseManager - Wrapper for Firebase Authentication and Cloud Firestore.
 */
public class FirebaseManager {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public FirebaseManager() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> register(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> resetPassword(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    public Task<AuthResult> signInWithCredential(AuthCredential credential) {
        return auth.signInWithCredential(credential);
    }

    public void logout() {
        auth.signOut();
    }

    public void sendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification();
        }
    }

    public void createUserDocument(String uid, String fullName, String email, String phone, String photoUrl, final FirestoreCallback callback) {
        long currentTime = System.currentTimeMillis();
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("fullName", fullName != null ? fullName : "");
        user.put("email", email);
        user.put("photoUrl", photoUrl != null ? photoUrl : "");
        user.put("phoneNumber", phone != null ? phone : "");
        user.put("emailVerified", false);
        user.put("createdAt", currentTime);
        user.put("updatedAt", currentTime);
        user.put("role", "user");
        user.put("favoriteCount", 0);
        user.put("totalPosts", 0);
        user.put("profileCompleted", false);

        firestore.collection(Constants.COL_USERS)
                .document(uid)
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateLastLogin(String uid) {
        firestore.collection(Constants.COL_USERS)
                .document(uid)
                .update("updatedAt", System.currentTimeMillis());
    }

    public void checkUserExists(String uid, final UserExistsCallback callback) {
        firestore.collection(Constants.COL_USERS)
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        callback.onResult(task.getResult().exists());
                    } else {
                        callback.onResult(false);
                    }
                });
    }

    public interface UserExistsCallback {
        void onResult(boolean exists);
    }

    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(String error);
    }
}
