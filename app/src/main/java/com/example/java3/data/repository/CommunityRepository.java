package com.example.java3.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.java3.core.utils.Constants;
import com.example.java3.domain.model.CommunityComment;
import com.example.java3.domain.model.Post;
import com.example.java3.domain.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing community posts with Realtime and Production-ready features.
 */
public class CommunityRepository {
    private static final String TAG = "CommunityRepository";
    private static final String LINE_FEED = "\r\n";
    private static final String COL_LIKES = "likes";
    private static final String SUBCOL_COMMENTS = "comments";
    private static final String FAVORITE_TYPE_POST = "community_post";
    private static final long COMMUNITY_FEED_LIMIT = 50L;

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final Handler mainHandler;
    private ListenerRegistration postsListener;

    public CommunityRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Get posts with realtime updates using SnapshotListener.
     */
    public LiveData<List<Post>> getRealtimePosts() {
        MutableLiveData<List<Post>> liveData = new MutableLiveData<>();
        
        if (postsListener != null) {
            postsListener.remove();
        }

        postsListener = firestore.collection(Constants.COL_COMMUNITY_POSTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(COMMUNITY_FEED_LIMIT)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Failed to listen community posts", error);
                        return;
                    }
                    if (value != null) {
                        List<Post> posts = value.toObjects(Post.class);
                        for (int i = 0; i < value.getDocuments().size(); i++) {
                            posts.get(i).setId(value.getDocuments().get(i).getId());
                        }
                        enrichAuthorProfiles(posts, () -> enrichCurrentUserStates(posts, liveData));
                    }
                });

        return liveData;
    }

    private void enrichAuthorProfiles(List<Post> posts, Runnable onComplete) {
        if (posts == null || posts.isEmpty()) {
            onComplete.run();
            return;
        }

        Set<String> userIds = new HashSet<>();
        for (Post post : posts) {
            if (!isBlank(post.getUserId())) {
                userIds.add(post.getUserId());
            }
        }

        if (userIds.isEmpty()) {
            onComplete.run();
            return;
        }

        Map<String, AuthorProfile> usersById = new HashMap<>();
        final int[] remaining = {userIds.size()};
        for (String userId : userIds) {
            firestore.collection(Constants.COL_USERS).document(userId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            usersById.put(userId, new AuthorProfile(
                                    firstNonBlank(snapshot.getString("fullName"), snapshot.getString("name"), snapshot.getString("nama")),
                                    firstNonBlank(snapshot.getString("photoUrl"), snapshot.getString("photo"))
                            ));
                        }
                        finishAuthorLookup(posts, usersById, remaining, onComplete);
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Failed to enrich community author profile: " + userId, e);
                        finishAuthorLookup(posts, usersById, remaining, onComplete);
                    });
        }
    }

    private void finishAuthorLookup(List<Post> posts, Map<String, AuthorProfile> usersById, int[] remaining, Runnable onComplete) {
        remaining[0]--;
        if (remaining[0] > 0) {
            return;
        }
        for (Post post : posts) {
            AuthorProfile user = usersById.get(post.getUserId());
            if (user != null) {
                String latestName = firstNonBlank(user.name, post.getUserName(), "Pemancing");
                String latestPhoto = firstNonBlank(user.photoUrl, post.getUserProfilePic(), "");
                post.setUserName(latestName);
                post.setUserProfilePic(latestPhoto);
            }
        }
        onComplete.run();
    }

    private void enrichCurrentUserStates(List<Post> posts, MutableLiveData<List<Post>> liveData) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || posts == null || posts.isEmpty()) {
            liveData.setValue(posts);
            return;
        }

        String userId = currentUser.getUid();
        firestore.collection(COL_LIKES)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(likesSnapshot -> {
                    Set<String> likedPostIds = collectFieldValues(likesSnapshot, "postId");
                    firestore.collection(Constants.COL_FAVORITES)
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(favoritesSnapshot -> {
                                Set<String> favoritePostIds = collectPostFavorites(favoritesSnapshot);
                                for (Post post : posts) {
                                    post.setLiked(likedPostIds.contains(post.getId()));
                                    post.setFavorite(favoritePostIds.contains(post.getId()));
                                }
                                liveData.setValue(posts);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Failed to resolve favorite state", e);
                                for (Post post : posts) {
                                    post.setLiked(likedPostIds.contains(post.getId()));
                                }
                                liveData.setValue(posts);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to resolve like state", e);
                    liveData.setValue(posts);
                });
    }

    private Set<String> collectFieldValues(QuerySnapshot snapshot, String fieldName) {
        Set<String> values = new HashSet<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String value = document.getString(fieldName);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private Set<String> collectPostFavorites(QuerySnapshot snapshot) {
        Set<String> favoritePostIds = new HashSet<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String type = document.getString("type");
            String postId = document.getString("postId");
            String pointId = document.getString("pointId");
            if ((FAVORITE_TYPE_POST.equals(type) || "post".equals(type) || "community_post".equals(document.getString("targetType"))) && postId != null) {
                favoritePostIds.add(postId);
            } else if (document.getId().contains("_post_") && pointId != null) {
                favoritePostIds.add(pointId);
            }
        }
        return favoritePostIds;
    }

    /**
     * Create a new post with image upload.
     */
    public void createPost(Post post, byte[] imageData, RepositoryCallback<String> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return;
        }

        String userId = currentUser.getUid();

        // Prefer Firestore profile data, but do not block posting when the profile document is missing.
        firestore.collection(Constants.COL_USERS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    applyAuthorData(post, currentUser, user);
                    uploadImageAndSavePost(post, imageData, callback);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to fetch user profile. Falling back to FirebaseAuth profile.", e);
                    applyAuthorData(post, currentUser, null);
                    uploadImageAndSavePost(post, imageData, callback);
                });
    }

    private void uploadImageAndSavePost(Post post, byte[] imageData, RepositoryCallback<String> callback) {
        if (imageData == null || imageData.length == 0) {
            callback.onError("Foto tidak dapat dibaca.");
            return;
        }

        if (imageData.length > Constants.CLOUDINARY_MAX_UPLOAD_BYTES) {
            callback.onError("Ukuran foto terlalu besar. Maksimal 5 MB.");
            return;
        }

        ExecutorService uploadExecutor = Executors.newSingleThreadExecutor();
        uploadExecutor.execute(() -> {
            try {
                CloudinaryUploadResult result = uploadToCloudinary(imageData);
                mainHandler.post(() -> {
                    post.setImageUrl(result.secureUrl);
                    post.setCloudinaryPublicId(result.publicId);
                    savePostToFirestore(post, callback);
                });
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Failed to upload post image to Cloudinary", e);
                mainHandler.post(() -> callback.onError("Upload foto ke Cloudinary gagal: " + getErrorMessage(e)));
            } finally {
                uploadExecutor.shutdown();
            }
        });
    }

    private CloudinaryUploadResult uploadToCloudinary(byte[] imageData) throws IOException, JSONException {
        String boundary = "----FishingPoint" + UUID.randomUUID();
        HttpURLConnection connection = (HttpURLConnection) new URL(Constants.CLOUDINARY_UPLOAD_URL).openConnection();
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(30000);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream()) {
            writeFormField(outputStream, boundary, "upload_preset", Constants.CLOUDINARY_UPLOAD_PRESET);
            writeFileField(outputStream, boundary, "file", "post.jpg", "image/jpeg", imageData);
            writeString(outputStream, "--" + boundary + "--" + LINE_FEED);
        }

        int responseCode = connection.getResponseCode();
        String response = readResponse(connection, responseCode);
        connection.disconnect();

        if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
            throw new IOException("Cloudinary HTTP " + responseCode + ": " + extractCloudinaryError(response));
        }

        JSONObject json = new JSONObject(response);
        String secureUrl = json.optString("secure_url");
        String publicId = json.optString("public_id");
        if (secureUrl == null || secureUrl.trim().isEmpty()) {
            throw new IOException("Cloudinary tidak mengembalikan secure_url");
        }
        return new CloudinaryUploadResult(secureUrl, publicId);
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

    private String extractCloudinaryError(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject error = json.optJSONObject("error");
            if (error != null) {
                String message = error.optString("message");
                if (message != null && !message.trim().isEmpty()) {
                    return message;
                }
            }
        } catch (JSONException ignored) {
            // Fall through to raw response.
        }
        return response == null || response.trim().isEmpty() ? "Terjadi kesalahan tidak diketahui" : response;
    }

    private void savePostToFirestore(Post post, RepositoryCallback<String> callback) {
        firestore.collection(Constants.COL_COMMUNITY_POSTS)
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    // Increment user's total post count
                    firestore.collection(Constants.COL_USERS).document(post.getUserId())
                            .update("totalPosts", FieldValue.increment(1))
                            .addOnFailureListener(e -> Log.w(TAG, "Failed to increment totalPosts", e));
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save post to Firestore", e);
                    callback.onError("Simpan postingan gagal: " + getErrorMessage(e));
                });
    }

    private void applyAuthorData(Post post, FirebaseUser firebaseUser, User user) {
        post.setUserId(firebaseUser.getUid());
        post.setUserName(resolveDisplayName(firebaseUser, user));
        post.setUserProfilePic(resolvePhotoUrl(firebaseUser, user));
    }

    private String resolveDisplayName(FirebaseUser firebaseUser, User user) {
        String fullName = user != null ? user.getFullName() : null;
        String displayName = firstNonBlank(fullName, firebaseUser.getDisplayName(), nameFromEmail(firebaseUser.getEmail()));
        return firstNonBlank(displayName, "Pemancing");
    }

    private String resolvePhotoUrl(FirebaseUser firebaseUser, User user) {
        String userPhoto = user != null ? user.getPhotoUrl() : null;
        String authPhoto = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;
        return firstNonBlank(userPhoto, authPhoto, "");
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
        return localPart.isEmpty() ? "" : localPart;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String getErrorMessage(Exception e) {
        String message = e.getLocalizedMessage();
        return message != null && !message.trim().isEmpty() ? message : "Terjadi kesalahan tidak diketahui";
    }

    /**
     * Toggle Like on a post.
     */
    public void toggleLike(String postId, RepositoryCallback<Boolean> callback) {
        String userId = getRequiredUserId(callback);
        if (userId == null || isBlank(postId)) {
            return;
        }

        String likeId = userId + "_" + postId;
        DocumentReference likeRef = firestore.collection(COL_LIKES).document(likeId);
        DocumentReference postRef = firestore.collection(Constants.COL_COMMUNITY_POSTS).document(postId);

        firestore.runTransaction(transaction -> {
                    DocumentSnapshot likeDoc = transaction.get(likeRef);
                    if (likeDoc.exists()) {
                        transaction.delete(likeRef);
                        transaction.update(postRef, "likesCount", FieldValue.increment(-1));
                        return false;
                    }
                    Map<String, Object> likeData = new HashMap<>();
                    likeData.put("userId", userId);
                    likeData.put("postId", postId);
                    likeData.put("timestamp", FieldValue.serverTimestamp());
                    transaction.set(likeRef, likeData);
                    transaction.update(postRef, "likesCount", FieldValue.increment(1));
                    return true;
                })
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to toggle like", e);
                    callback.onError("Gagal memperbarui like: " + getErrorMessage(e));
                });
    }

    public void toggleFavorite(String postId, RepositoryCallback<Boolean> callback) {
        String userId = getRequiredUserId(callback);
        if (userId == null || isBlank(postId)) {
            return;
        }

        String favoriteId = userId + "_post_" + postId;
        DocumentReference favoriteRef = firestore.collection(Constants.COL_FAVORITES).document(favoriteId);
        favoriteRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        favoriteRef.delete()
                                .addOnSuccessListener(aVoid -> callback.onSuccess(false))
                                .addOnFailureListener(e -> callback.onError("Gagal menghapus simpanan: " + getErrorMessage(e)));
                        return;
                    }

                    Map<String, Object> favoriteData = new HashMap<>();
                    favoriteData.put("id", favoriteId);
                    favoriteData.put("userId", userId);
                    favoriteData.put("postId", postId);
                    favoriteData.put("pointId", postId);
                    favoriteData.put("type", FAVORITE_TYPE_POST);
                    favoriteData.put("targetType", "post");
                    favoriteData.put("createdAt", System.currentTimeMillis());
                    favoriteRef.set(favoriteData)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                            .addOnFailureListener(e -> callback.onError("Gagal menyimpan postingan: " + getErrorMessage(e)));
                })
                .addOnFailureListener(e -> callback.onError("Gagal mengecek simpanan: " + getErrorMessage(e)));
    }

    public void addComment(String postId, String text, RepositoryCallback<Void> callback) {
        String userId = getRequiredUserId(callback);
        if (userId == null || isBlank(postId)) {
            return;
        }
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return;
        }
        if (isBlank(text)) {
            callback.onError("Komentar tidak boleh kosong.");
            return;
        }

        firestore.collection(Constants.COL_USERS).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    CommunityComment comment = new CommunityComment(
                            postId,
                            userId,
                            resolveDisplayName(currentUser, user),
                            resolvePhotoUrl(currentUser, user),
                            text.trim()
                    );
                    saveComment(postId, comment, callback);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to fetch commenter profile. Falling back to FirebaseAuth profile.", e);
                    CommunityComment comment = new CommunityComment(
                            postId,
                            userId,
                            resolveDisplayName(currentUser, null),
                            resolvePhotoUrl(currentUser, null),
                            text.trim()
                    );
                    saveComment(postId, comment, callback);
                });
    }

    private void saveComment(String postId, CommunityComment comment, RepositoryCallback<Void> callback) {
        DocumentReference postRef = firestore.collection(Constants.COL_COMMUNITY_POSTS).document(postId);
        CollectionReference commentsRef = postRef.collection(SUBCOL_COMMENTS);
        commentsRef.add(comment)
                .addOnSuccessListener(documentReference -> postRef.update("commentsCount", FieldValue.increment(1))
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Comment saved but commentsCount update failed", e);
                            callback.onSuccess(null);
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add comment", e);
                    callback.onError("Gagal menambahkan komentar: " + getErrorMessage(e));
                });
    }

    public void getComments(String postId, RepositoryCallback<List<CommunityComment>> callback) {
        if (isBlank(postId)) {
            callback.onError("Postingan tidak valid.");
            return;
        }

        firestore.collection(Constants.COL_COMMUNITY_POSTS)
                .document(postId)
                .collection(SUBCOL_COMMENTS)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CommunityComment> comments = queryDocumentSnapshots.toObjects(CommunityComment.class);
                    for (int i = 0; i < queryDocumentSnapshots.getDocuments().size(); i++) {
                        comments.get(i).setId(queryDocumentSnapshots.getDocuments().get(i).getId());
                    }
                    callback.onSuccess(comments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load comments", e);
                    callback.onError("Gagal memuat komentar: " + getErrorMessage(e));
                });
    }

    /**
     * Delete post data. Cloudinary asset deletion requires a signed backend/Admin API.
     */
    public void deletePost(Post post, RepositoryCallback<Void> callback) {
        if (post.getCloudinaryPublicId() != null && !post.getCloudinaryPublicId().trim().isEmpty()) {
            Log.i(TAG, "Cloudinary asset is left in place. Delete via backend if needed: " + post.getCloudinaryPublicId());
        }
        firestore.collection(Constants.COL_COMMUNITY_POSTS).document(post.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Decrement user's total post count
                    firestore.collection(Constants.COL_USERS).document(post.getUserId())
                            .update("totalPosts", FieldValue.increment(-1));
                    cleanupPostRelations(post.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void cleanupPostRelations(String postId) {
        firestore.collection(COL_LIKES)
                .whereEqualTo("postId", postId)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshot -> snapshot.getDocuments().forEach(document -> document.getReference().delete()))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to cleanup post likes", e));

        firestore.collection(Constants.COL_FAVORITES)
                .whereEqualTo("postId", postId)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshot -> snapshot.getDocuments().forEach(document -> document.getReference().delete()))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to cleanup post favorites", e));

        firestore.collection(Constants.COL_COMMUNITY_POSTS)
                .document(postId)
                .collection(SUBCOL_COMMENTS)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshot -> snapshot.getDocuments().forEach(document -> document.getReference().delete()))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to cleanup post comments", e));
    }

    public void cleanup() {
        if (postsListener != null) {
            postsListener.remove();
        }
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private String getRequiredUserId(RepositoryCallback<?> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Sesi login tidak ditemukan. Silakan login ulang.");
            return null;
        }
        return currentUser.getUid();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class CloudinaryUploadResult {
        final String secureUrl;
        final String publicId;

        CloudinaryUploadResult(String secureUrl, String publicId) {
            this.secureUrl = secureUrl;
            this.publicId = publicId;
        }
    }

    private static class AuthorProfile {
        final String name;
        final String photoUrl;

        AuthorProfile(String name, String photoUrl) {
            this.name = name;
            this.photoUrl = photoUrl;
        }
    }
}
