package com.example.java3.data.repository;

import android.util.Log;

import com.example.java3.core.utils.Constants;
import com.example.java3.domain.model.FishingPoint;
import com.example.java3.data.repository.FishingRepository;
import com.example.java3.domain.model.Post;
import com.example.java3.presentation.model.ProfileStatsUiModel;
import com.example.java3.presentation.model.ProfileUiModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileRepository {
    private static final String TAG = "ProfileRepository";
    private static final String FAVORITE_TYPE_POST = "community_post";
    private static final String LINE_FEED = "\r\n";

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    private ListenerRegistration profileListener;
    private ListenerRegistration postsListener;
    private ListenerRegistration spotsListener;
    private ListenerRegistration favoritesListener;

    private ProfileUiModel lastProfile;
    private int postCount;
    private int spotCount;
    private int likeCount;
    private int commentCount;
    private int favoriteCount;

    public ProfileRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public ListenerRegistration listenProfile(Callback<ProfileUiModel> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return null;
        }

        profileListener = firestore.collection(Constants.COL_USERS).document(user.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(readableError(error));
                        return;
                    }
                    lastProfile = snapshot != null && snapshot.exists()
                            ? fromDocument(user, snapshot)
                            : fromFirebaseUser(user);
                    callback.onSuccess(lastProfile);
                });
        return profileListener;
    }

    public void listenStats(Callback<ProfileStatsUiModel> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return;
        }

        postsListener = firestore.collection(Constants.COL_COMMUNITY_POSTS)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(readableError(error));
                        return;
                    }
                    postCount = 0;
                    likeCount = 0;
                    commentCount = 0;
                    if (snapshot != null) {
                        postCount = snapshot.size();
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Long likes = document.getLong("likesCount");
                            Long comments = document.getLong("commentsCount");
                            likeCount += likes != null ? likes.intValue() : 0;
                            commentCount += comments != null ? comments.intValue() : 0;
                        }
                    }
                    callback.onSuccess(currentStats());
                });

        spotsListener = firestore.collection(Constants.COL_FISHING_POINTS)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(readableError(error));
                        return;
                    }
                    spotCount = snapshot != null ? snapshot.size() : 0;
                    callback.onSuccess(currentStats());
                });

        favoritesListener = firestore.collection(Constants.COL_FAVORITES)
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(readableError(error));
                        return;
                    }
                    favoriteCount = 0;
                    if (snapshot != null) {
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            String type = document.getString("type");
                            if (!FAVORITE_TYPE_POST.equals(type)) {
                                favoriteCount++;
                            }
                        }
                    }
                    callback.onSuccess(currentStats());
                });
    }

    public void updateProfile(String name, String phone, String address, String bio, Callback<Void> callback) {
        String userId = getCurrentUserId();
        FirebaseUser user = auth.getCurrentUser();
        if (userId == null || user == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("uid", userId);
        data.put("name", name);
        data.put("fullName", name);
        data.put("email", user.getEmail());
        data.put("phone", phone);
        data.put("phoneNumber", phone);
        data.put("address", address);
        data.put("bio", bio);
        data.put("updatedAt", System.currentTimeMillis());
        data.put("profileCompleted", true);
        if (lastProfile == null || lastProfile.getJoinDate() <= 0) {
            data.put("joinDate", System.currentTimeMillis());
            data.put("createdAt", System.currentTimeMillis());
        }

        firestore.collection(Constants.COL_USERS).document(userId)
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused -> syncCommunityAuthorFields(
                        userId,
                        name,
                        lastProfile != null ? lastProfile.getPhotoUrl() : null,
                        () -> callback.onSuccess(null)
                ))
                .addOnFailureListener(e -> callback.onError(readableError(e)));
    }

    public void uploadProfilePhoto(byte[] imageData, Callback<String> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return;
        }

        if (imageData == null || imageData.length == 0) {
            callback.onError("Foto tidak dapat dibaca.");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String downloadUrl = uploadProfileImageToCloudinary(imageData);
                Map<String, Object> data = new HashMap<>();
                data.put("photoUrl", downloadUrl);
                data.put("photo", downloadUrl);
                data.put("updatedAt", System.currentTimeMillis());
                firestore.collection(Constants.COL_USERS).document(userId)
                        .set(data, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(unused -> syncCommunityAuthorFields(
                                userId,
                                null,
                                downloadUrl,
                                () -> callback.onSuccess(downloadUrl)
                        ))
                        .addOnFailureListener(e -> callback.onError(readableError(e)));
            } catch (IOException | JSONException e) {
                callback.onError("Upload foto ke Cloudinary gagal: " + readableError(e));
            } finally {
                executor.shutdown();
            }
        });
    }

    private String uploadProfileImageToCloudinary(byte[] imageData) throws IOException, JSONException {
        String boundary = "----FishingProfile" + UUID.randomUUID();
        HttpURLConnection connection = (HttpURLConnection) new URL(Constants.CLOUDINARY_UPLOAD_URL).openConnection();
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(30000);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream()) {
            writeFormField(outputStream, boundary, "upload_preset", Constants.CLOUDINARY_PROFILE_UPLOAD_PRESET);
            writeFileField(outputStream, boundary, "file", "profile.jpg", "image/jpeg", imageData);
            writeString(outputStream, "--" + boundary + "--" + LINE_FEED);
        }

        int responseCode = connection.getResponseCode();
        String response = readResponse(connection, responseCode);
        connection.disconnect();

        if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
            throw new IOException("Cloudinary HTTP " + responseCode + ": " + response);
        }

        JSONObject json = new JSONObject(response);
        String secureUrl = json.optString("secure_url");
        if (secureUrl == null || secureUrl.trim().isEmpty()) {
            throw new IOException("Cloudinary tidak mengembalikan secure_url");
        }
        return secureUrl;
    }

    private void writeFormField(OutputStream outputStream, String boundary, String name, String value) throws IOException {
        writeString(outputStream, "--" + boundary + LINE_FEED);
        writeString(outputStream, "Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED);
        writeString(outputStream, LINE_FEED);
        writeString(outputStream, value + LINE_FEED);
    }

    private void writeFileField(OutputStream outputStream, String boundary, String name, String fileName,
                                String contentType, byte[] data) throws IOException {
        writeString(outputStream, "--" + boundary + LINE_FEED);
        writeString(outputStream, "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"" + LINE_FEED);
        writeString(outputStream, "Content-Type: " + contentType + LINE_FEED);
        writeString(outputStream, LINE_FEED);
        outputStream.write(data);
        writeString(outputStream, LINE_FEED);
    }

    private void writeString(OutputStream outputStream, String value) throws IOException {
        outputStream.write(value.getBytes(StandardCharsets.UTF_8));
    }

    private String readResponse(HttpURLConnection connection, int responseCode) throws IOException {
        InputStream inputStream = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST
                ? connection.getErrorStream()
                : connection.getInputStream();
        if (inputStream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    public void sendPasswordReset(Callback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        String email = firstNonBlank(
                user != null ? user.getEmail() : null,
                lastProfile != null ? lastProfile.getEmail() : null
        );
        if (user == null || email.trim().isEmpty()) {
            callback.onError("Email akun tidak ditemukan.");
            return;
        }
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(readableError(e)));
    }

    public void loadMyPosts(Callback<List<Post>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return;
        }
        firestore.collection(Constants.COL_COMMUNITY_POSTS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Post> posts = snapshot.toObjects(Post.class);
                    for (int i = 0; i < snapshot.getDocuments().size(); i++) {
                        posts.get(i).setId(snapshot.getDocuments().get(i).getId());
                    }
                    Collections.sort(posts, Comparator.comparingLong(this::postTime).reversed());
                    callback.onSuccess(posts);
                })
                .addOnFailureListener(e -> callback.onError(readableError(e)));
    }

    private long postTime(Post post) {
        return post != null && post.getTimestamp() != null ? post.getTimestamp().getTime() : 0L;
    }


    public void loadMySpots(Callback<List<FishingPoint>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return;
        }
        firestore.collection(Constants.COL_FISHING_POINTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<FishingPoint> points = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        FishingPoint point = document.toObject(FishingPoint.class);
                        if (point != null) {
                            if (point.getId() == null || point.getId().trim().isEmpty()) {
                                point.setId(document.getId());
                            }
                            if (FishingRepository.isOwnedByCurrentUser(point, userId)) {
                                points.add(point);
                            }
                        }
                    }
                    Collections.sort(points, Comparator.comparingLong(FishingPoint::getCreatedAt).reversed());
                    callback.onSuccess(points);
                })
                .addOnFailureListener(e -> callback.onError(readableError(e)));
    }
    public void loadFavoriteSpots(Callback<List<FishingPoint>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return;
        }
        firestore.collection(Constants.COL_FAVORITES)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> pointIds = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        String type = document.getString("type");
                        String pointId = document.getString("pointId");
                        if (!FAVORITE_TYPE_POST.equals(type) && pointId != null && !pointId.trim().isEmpty()) {
                            pointIds.add(pointId);
                        }
                    }
                    loadFavoriteSpotDocuments(pointIds, callback);
                })
                .addOnFailureListener(e -> callback.onError(readableError(e)));
    }

    private void loadFavoriteSpotDocuments(List<String> pointIds, Callback<List<FishingPoint>> callback) {
        if (pointIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        List<FishingPoint> points = new ArrayList<>();
        final int[] remaining = {pointIds.size()};
        for (String pointId : pointIds) {
            firestore.collection(Constants.COL_FISHING_POINTS).document(pointId).get()
                    .addOnSuccessListener(document -> {
                        FishingPoint point = document.toObject(FishingPoint.class);
                        if (point != null) {
                            point.setId(document.getId());
                            points.add(point);
                        }
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            callback.onSuccess(points);
                        }
                    })
                    .addOnFailureListener(e -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            callback.onSuccess(points);
                        }
                    });
        }
    }

    private void syncCommunityAuthorFields(String userId, String name, String photoUrl, Runnable onComplete) {
        firestore.collection(Constants.COL_COMMUNITY_POSTS)
                .whereEqualTo("userId", userId)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        onComplete.run();
                        return;
                    }

                    com.google.firebase.firestore.WriteBatch batch = firestore.batch();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Map<String, Object> updates = new HashMap<>();
                        if (name != null && !name.trim().isEmpty()) {
                            updates.put("userName", name.trim());
                        }
                        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                            updates.put("userProfilePic", photoUrl.trim());
                        }
                        if (!updates.isEmpty()) {
                            batch.update(document.getReference(), updates);
                        }
                    }
                    batch.commit()
                            .addOnSuccessListener(unused -> onComplete.run())
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Failed to sync community author fields", e);
                                onComplete.run();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to find community posts for author sync", e);
                    onComplete.run();
                });
    }

    public void cleanup() {
        if (profileListener != null) profileListener.remove();
        if (postsListener != null) postsListener.remove();
        if (spotsListener != null) spotsListener.remove();
        if (favoritesListener != null) favoritesListener.remove();
    }

    private ProfileStatsUiModel currentStats() {
        long joinDate = lastProfile != null ? lastProfile.getJoinDate() : 0L;
        return new ProfileStatsUiModel(spotCount, postCount, likeCount, commentCount, favoriteCount, joinDate);
    }

    private ProfileUiModel fromDocument(FirebaseUser user, DocumentSnapshot snapshot) {
        String email = firstNonBlank(snapshot.getString("email"), user.getEmail());
        String name = firstNonBlank(snapshot.getString("name"), snapshot.getString("fullName"), snapshot.getString("nama"), user.getDisplayName(), nameFromEmail(email), "Pemancing");
        String photoUrl = firstNonBlank(snapshot.getString("photoUrl"), snapshot.getString("photo"), user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        long joinDate = firstPositive(snapshot.getLong("joinDate"), snapshot.getLong("createdAt"));
        return new ProfileUiModel(
                user.getUid(),
                name,
                email,
                firstNonBlank(snapshot.getString("phone"), snapshot.getString("phoneNumber")),
                firstNonBlank(snapshot.getString("address"), snapshot.getString("alamat")),
                firstNonBlank(snapshot.getString("bio")),
                photoUrl,
                joinDate
        );
    }

    private ProfileUiModel fromFirebaseUser(FirebaseUser user) {
        String email = firstNonBlank(user.getEmail());
        String name = firstNonBlank(user.getDisplayName(), nameFromEmail(email), "Pemancing");
        String photoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
        return new ProfileUiModel(user.getUid(), name, email, "", "", "", photoUrl, 0L);
    }

    private String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private long firstPositive(Long... values) {
        for (Long value : values) {
            if (value != null && value > 0) {
                return value;
            }
        }
        return 0L;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String nameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        return email.substring(0, email.indexOf('@')).replace('.', ' ').replace('_', ' ').trim();
    }

    private String readableError(Exception error) {
        String message = error.getLocalizedMessage();
        if (message == null || message.trim().isEmpty()) {
            return "Periksa koneksi internet";
        }
        return message;
    }
}
