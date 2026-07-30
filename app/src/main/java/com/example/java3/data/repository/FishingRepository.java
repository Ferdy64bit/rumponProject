package com.example.java3.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.java3.core.utils.Constants;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.data.remote.MarineHourlyResponse;
import com.example.java3.domain.model.FishingPoint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FishingRepository {
    private static final String LINE_FEED = "\r\n";
    private final FirebaseFirestore firestore;

    public FishingRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void getWeather(double lat, double lon, MutableLiveData<WeatherResponse> liveData, MutableLiveData<String> errorData) {
        new WeatherRepository().getCurrentWeather(lat, lon, liveData, errorData);
    }

    public void getTide(double lat, double lon, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData) {
        new BMKGRepository().getTide(lat, lon, liveData, errorData);
    }

    public void getMarineHourly(double lat, double lon, MutableLiveData<MarineHourlyResponse> liveData, MutableLiveData<String> errorData) {
        new MarineHourlyRepository().getHourlyMarine(lat, lon, liveData, errorData);
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String error);
    }

    public void addFishingPoint(FishingPoint point, FirestoreCallback<Void> callback) {
        if (point == null) {
            if (callback != null) callback.onFailure("Data spot belum tersedia.");
            return;
        }
        if (point.getId() == null || point.getId().isEmpty()) {
            String newId = firestore.collection(Constants.COL_FISHING_POINTS).document().getId();
            point.setId(newId);
        }
        applyFishingPointMetadata(point, true);

        firestore.collection(Constants.COL_FISHING_POINTS)
                .document(point.getId())
                .set(point)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public ListenerRegistration listenFishingPoints(FirestoreCallback<List<FishingPoint>> callback) {
        return firestore.collection(Constants.COL_FISHING_POINTS)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<FishingPoint> points = value.toObjects(FishingPoint.class);
                        
                        // Seed only if definitely empty and not just local empty state
                        if (points.isEmpty() && !value.getMetadata().hasPendingWrites()) {
                            seedFishingPoints();
                        }

                        callback.onSuccess(points);
                    }
                });
    }

    public void getFishingPoints(FirestoreCallback<List<FishingPoint>> callback) {
        firestore.collection(Constants.COL_FISHING_POINTS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FishingPoint> points = queryDocumentSnapshots.toObjects(FishingPoint.class);
                    if (points.isEmpty()) {
                        seedFishingPoints();
                        callback.onSuccess(getMockFishingPoints());
                    } else {
                        callback.onSuccess(points);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateFishingPoint(FishingPoint point, FirestoreCallback<Void> callback) {
        if (point == null || point.getId() == null || point.getId().trim().isEmpty()) {
            if (callback != null) callback.onFailure("Data spot belum tersedia.");
            return;
        }
        applyFishingPointMetadata(point, false);
        firestore.collection(Constants.COL_FISHING_POINTS)
                .document(point.getId())
                .set(point)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void deleteFishingPoint(String pointId, FirestoreCallback<Void> callback) {
        firestore.collection(Constants.COL_FISHING_POINTS)
                .document(pointId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static boolean isPublicSpot(FishingPoint point) {
        return point != null && "PUBLIC".equalsIgnoreCase(normalizeVisibility(point.getVisibility()));
    }

    public static boolean isOwnedByCurrentUser(FishingPoint point, String userId) {
        if (point == null || userId == null || userId.trim().isEmpty()) {
            return false;
        }
        String safeUserId = userId.trim();
        String ownerId = firstNonBlank(point.getOwnerId(), point.getUserId());
        return safeUserId.equals(ownerId);
    }

    public static boolean canUserSeeSpot(FishingPoint point, String userId) {
        if (point == null) {
            return false;
        }
        if (isPublicSpot(point)) {
            return true;
        }
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        String ownerId = firstNonBlank(point.getOwnerId(), point.getUserId());
        return userId.trim().equals(ownerId);
    }

    public static String normalizeVisibility(String visibility) {
        if (visibility == null || visibility.trim().isEmpty()) {
            return "PUBLIC";
        }
        String normalized = visibility.trim().toUpperCase(java.util.Locale.ROOT);
        if ("PRIVATE".equals(normalized) || "PRIVAT".equals(normalized)) {
            return "PRIVATE";
        }
        return "PUBLIC";
    }

    private void applyFishingPointMetadata(FishingPoint point, boolean isNew) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = user != null ? user.getUid() : null;
        String currentUserName = getUserNameFallback(user, currentUserId);
        String currentUserPhoto = user != null && user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
        long now = System.currentTimeMillis();

        if (currentUserId != null && !currentUserId.trim().isEmpty()) {
            point.setOwnerId(currentUserId);
            point.setUserId(currentUserId);
        }
        if (point.getOwnerName() == null || point.getOwnerName().trim().isEmpty()) {
            point.setOwnerName(currentUserName != null ? currentUserName : "Fishing Point Member");
        }
        if (point.getOwnerPhoto() == null || point.getOwnerPhoto().trim().isEmpty()) {
            point.setOwnerPhoto(currentUserPhoto);
        }

        if (isNew && point.getCreatedAt() <= 0L) {
            point.setCreatedAt(now);
        }
        if (!isNew && point.getCreatedAt() <= 0L) {
            point.setCreatedAt(now);
        }
        if (point.getVisibility() == null || point.getVisibility().trim().isEmpty()) {
            point.setVisibility(isNew ? "PRIVATE" : "PUBLIC");
        } else {
            point.setVisibility(normalizeVisibility(point.getVisibility()));
        }
        point.setUpdatedAt(now);
    }

    private String getUserNameFallback(FirebaseUser user, String fallbackId) {
        if (user != null && user.getEmail() != null && user.getEmail().contains("@")) {
            return user.getEmail().substring(0, user.getEmail().indexOf('@')).replace('.', ' ').replace('_', ' ').trim();
        }
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            return user.getDisplayName().trim();
        }
        return fallbackId;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    public void uploadFishingPointPhoto(String pointId, byte[] imageData, FirestoreCallback<String> callback) {
        if (pointId == null || pointId.trim().isEmpty()) {
            if (callback != null) callback.onFailure("Data spot belum tersedia.");
            return;
        }
        if (imageData == null || imageData.length == 0) {
            if (callback != null) callback.onFailure("Foto tidak dapat dibaca.");
            return;
        }
        if (imageData.length > Constants.CLOUDINARY_MAX_UPLOAD_BYTES) {
            if (callback != null) callback.onFailure("Ukuran foto terlalu besar. Maksimal 5 MB.");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String imageUrl = uploadSpotImageToCloudinary(imageData);
                updateFishingPointPhoto(pointId, imageUrl, new FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (callback != null) callback.onSuccess(imageUrl);
                    }

                    @Override
                    public void onFailure(String error) {
                        if (callback != null) callback.onFailure(error);
                    }
                });
            } catch (IOException | JSONException e) {
                if (callback != null) callback.onFailure("Upload foto spot gagal: " + readableError(e));
            } finally {
                executor.shutdown();
            }
        });
    }

    public void updateFishingPointPhoto(String pointId, String imageUrl, FirestoreCallback<Void> callback) {
        if (pointId == null || pointId.trim().isEmpty()) {
            if (callback != null) callback.onFailure("Data spot belum tersedia.");
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("imageUrl", imageUrl != null ? imageUrl : "");
        data.put("updatedAt", System.currentTimeMillis());
        firestore.collection(Constants.COL_FISHING_POINTS)
                .document(pointId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(readableError(e));
                });
    }

    private String uploadSpotImageToCloudinary(byte[] imageData) throws IOException, JSONException {
        String boundary = "----FishingSpot" + UUID.randomUUID();
        HttpURLConnection connection = (HttpURLConnection) new URL(Constants.CLOUDINARY_UPLOAD_URL).openConnection();
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(30000);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream()) {
            writeFormField(outputStream, boundary, "upload_preset", Constants.CLOUDINARY_UPLOAD_PRESET);
            writeFileField(outputStream, boundary, "file", "spot.jpg", "image/jpeg", imageData);
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
        }
        return response != null && !response.trim().isEmpty() ? response : "response kosong";
    }

    private String readableError(Exception e) {
        String message = e != null ? e.getMessage() : null;
        return message != null && !message.trim().isEmpty() ? message : "Terjadi kesalahan tidak diketahui.";
    }

    private void seedFishingPoints() {
        List<FishingPoint> points = getMockFishingPoints();
        for (FishingPoint p : points) {
            firestore.collection(Constants.COL_FISHING_POINTS).document(p.getId()).set(p);
        }
    }

    private List<FishingPoint> getMockFishingPoints() {
        List<FishingPoint> points = new ArrayList<>();
        points.add(new FishingPoint("1", "Pantai Tanjung Kait", -6.0150, 106.6800, "Pantai", "Ikan Baronang, Belanak", 4.8f, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e"));
        points.add(new FishingPoint("2", "Rumpon Tengah", -6.0000, 106.7000, "Rumpon", "Ikan Kakap, Kerapu", 4.2f, ""));
        points.add(new FishingPoint("3", "Bagan Apung", -6.0200, 106.6700, "Bagan", "Spot Cumi, Kembung", 3.5f, ""));
        points.add(new FishingPoint("4", "Sungai Cisadane Hilir", -6.0350, 106.6500, "Sungai", "Ikan Mujair, Lele", 4.0f, ""));
        points.add(new FishingPoint("5", "Danau Cipondoh", -6.1850, 106.6800, "Danau", "Ikan Nila, Gurame", 4.5f, ""));
        points.add(new FishingPoint("6", "Tambak Mauk", -6.0100, 106.5800, "Tambak", "Bandeng, Udang", 4.3f, ""));
        return points;
    }
}
