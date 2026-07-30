package com.example.java3.data.repository;

import androidx.lifecycle.MutableLiveData;

import android.util.Log;

import com.example.java3.core.network.NetworkModule;
import com.example.java3.core.utils.BMKGAreaSelector;
import com.example.java3.core.utils.Constants;
import com.example.java3.domain.model.BMKGForecast;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.domain.model.TideCache;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TideRepository {
    private static final String TAG = "BMKG_REPOSITORY";
    private static List<MarineAreaPolygon> cachedMarineAreaPolygons;
    private static boolean loggedPolygonSample;

    private final FirebaseFirestore firestore;
    private final Gson gson;

    public TideRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.gson = new Gson();
    }

    public void getTide(double lat, double lon, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData) {
        resolveMarineArea(lat, lon, marineArea -> loadTideForArea(marineArea, lat, lon, liveData, errorData));
    }

    private void loadTideForArea(String marineArea, double lat, double lon, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData) {
        String cacheKey = createCacheKey(marineArea);
        Log.d(TAG, "request area=" + marineArea + " lat=" + lat + " lon=" + lon);
        firestore.collection(Constants.COL_TIDE_CACHE)
                .document(cacheKey)
                .get()
                .addOnSuccessListener(snapshot -> {
                    TideCache cache = snapshot.toObject(TideCache.class);
                    if (cache != null && isFresh(cache.getUpdatedAt())) {
                        TideResponse cachedResponse = parseCache(cache.getData(), errorData);
                        if (cachedResponse != null) {
                            Log.d(TAG, "cache hit area=" + marineArea + " forecasts=" + getForecastCount(cachedResponse));
                            liveData.setValue(cachedResponse);
                            return;
                        }
                    }
                    fetchFromBmkg(cacheKey, marineArea, cache, liveData, errorData);
                })
                .addOnFailureListener(error -> fetchFromBmkg(cacheKey, marineArea, null, liveData, errorData));
    }

    private void fetchFromBmkg(String cacheKey, String marineArea, TideCache fallbackCache, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData) {
        NetworkModule.getTideService().getMarineForecast(marineArea)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            TideResponse marine = mapBmkgResponse(response.body(), marineArea);
                            Log.d(TAG, "api success area=" + marineArea + " forecasts=" + getForecastCount(marine));
                            liveData.setValue(marine);
                            saveCache(cacheKey, marine);
                        } else {
                            fetchWaveOverviewFallback(marineArea, fallbackCache, liveData, errorData, "BMKG maritim gagal: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable error) {
                        fetchWaveOverviewFallback(marineArea, fallbackCache, liveData, errorData, "BMKG maritim gagal dimuat: " + safeMessage(error));
                    }
                });
    }

    private void fetchWaveOverviewFallback(String marineArea, TideCache fallbackCache, MutableLiveData<TideResponse> liveData, MutableLiveData<String> errorData, String originalError) {
        NetworkModule.getTideService().getWaveOverview().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TideResponse overview = mapBmkgResponse(response.body(), marineArea);
                    overview.setSource("BMKG Overview Gelombang");
                    if (hasOverviewData(overview)) {
                        Log.d(TAG, "overview fallback success area=" + marineArea + " forecasts=" + getForecastCount(overview));
                        liveData.setValue(overview);
                        return;
                    }
                }
                publishFallback(fallbackCache, liveData);
                errorData.setValue(originalError);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable error) {
                publishFallback(fallbackCache, liveData);
                errorData.setValue(originalError);
            }
        });
    }

    private boolean hasOverviewData(TideResponse response) {
        return response != null && (getForecastCount(response) > 0
                || !firstNonBlank(response.getWaveCat(), response.getWaveDesc(), response.getWarningDesc()).isEmpty());
    }

    private TideResponse mapBmkgResponse(JsonObject root, String marineArea) {
        TideResponse response = new TideResponse();
        response.setSource("BMKG");
        response.setAreaName(firstNonBlank(
                findString(root, "name"),
                findString(root, "area"),
                findString(root, "area_name"),
                marineArea.replace("%20", " ")
        ));
        response.setWeather(firstNonBlank(
                findString(root, "weather"),
                findString(root, "weather_desc"),
                findString(root, "cuaca")
        ));
        response.setWarningDesc(firstNonBlank(
                findString(root, "warning_desc"),
                findString(root, "warning"),
                findString(root, "peringatan")
        ));
        response.setWaveCat(firstNonBlank(
                findString(root, "wave_cat"),
                findString(root, "wave_category"),
                findString(root, "kategori_gelombang")
        ));
        response.setWaveDesc(firstNonBlank(
                findString(root, "wave_desc"),
                findString(root, "wave_height"),
                findString(root, "gelombang")
        ));
        response.setWindFrom(firstNonBlank(findString(root, "wind_from"), findString(root, "arah_angin_dari")));
        response.setWindTo(firstNonBlank(findString(root, "wind_to"), findString(root, "arah_angin_ke")));
        response.setWindSpeedMin(firstNonBlank(findString(root, "wind_speed_min"), findString(root, "wind_min")));
        response.setWindSpeedMax(firstNonBlank(findString(root, "wind_speed_max"), findString(root, "wind_max")));
        response.setForecastTime(firstNonBlank(
                findString(root, "valid_from"),
                findString(root, "forecast_time"),
                findString(root, "datetime"),
                findString(root, "issue_datetime")
        ));
        response.setForecasts(extractForecasts(root));
        return response;
    }

    private List<BMKGForecast> extractForecasts(JsonElement root) {
        LinkedHashMap<String, BMKGForecast> forecasts = new LinkedHashMap<>();
        collectForecasts(root, forecasts);
        return new ArrayList<>(forecasts.values());
    }

    private void collectForecasts(JsonElement element, LinkedHashMap<String, BMKGForecast> forecasts) {
        if (element == null || element.isJsonNull()) {
            return;
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();

            if (looksLikeForecastObject(object)) {
                BMKGForecast forecast = mapForecastObject(object);
                String key = firstNonBlank(forecast.getValidFrom(), forecast.getTimeDesc(), forecast.getWeather(), forecast.getWaveLabel());
                if (!key.isEmpty() && !forecasts.containsKey(key)) {
                    forecasts.put(key, forecast);
                }
            }

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (value == null || value.isJsonNull()) {
                    continue;
                }
                if (value.isJsonArray()) {
                    if (isForecastArrayKey(key) || arrayLooksLikeForecasts(value.getAsJsonArray())) {
                        for (JsonElement item : value.getAsJsonArray()) {
                            collectForecasts(item, forecasts);
                        }
                    } else {
                        collectForecasts(value, forecasts);
                    }
                } else {
                    collectForecasts(value, forecasts);
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                collectForecasts(item, forecasts);
            }
        }
    }

    private boolean isForecastArrayKey(String key) {
        if (key == null) return false;
        String normalized = key.trim().toLowerCase(Locale.US);
        return normalized.equals("data")
                || normalized.equals("forecast")
                || normalized.equals("forecasts")
                || normalized.equals("items")
                || normalized.equals("details")
                || normalized.equals("daily")
                || normalized.equals("day");
    }

    private boolean arrayLooksLikeForecasts(JsonArray array) {
        for (JsonElement item : array) {
            if (item != null && item.isJsonObject() && looksLikeForecastObject(item.getAsJsonObject())) {
                return true;
            }
        }
        return false;
    }

    private boolean looksLikeForecastObject(JsonObject object) {
        return hasAnyKey(object,
                "valid_from", "valid_to", "time_desc", "weather", "weather_desc",
                "wave_category", "wave_cat", "wave_description", "wave_desc",
                "wind_from", "wind_to", "wind_speed_min", "wind_speed_max",
                "warning", "warning_desc", "station_remark", "remark");
    }

    private BMKGForecast mapForecastObject(JsonObject object) {
        BMKGForecast forecast = new BMKGForecast();
        forecast.setValidFrom(firstNonBlank(findString(object, "valid_from"), findString(object, "from")));
        forecast.setValidTo(firstNonBlank(findString(object, "valid_to"), findString(object, "to")));
        forecast.setTimeDesc(firstNonBlank(findString(object, "time_desc"), findString(object, "time"), findString(object, "timeDesc")));
        forecast.setWeather(firstNonBlank(findString(object, "weather"), findString(object, "cuaca")));
        forecast.setWeatherDesc(firstNonBlank(findString(object, "weather_desc"), findString(object, "weather_description"), findString(object, "weatherDesc")));
        forecast.setWaveCategory(firstNonBlank(findString(object, "wave_category"), findString(object, "wave_cat"), findString(object, "kategori_gelombang")));
        forecast.setWaveDescription(firstNonBlank(findString(object, "wave_description"), findString(object, "wave_desc"), findString(object, "gelombang")));
        forecast.setWindFrom(firstNonBlank(findString(object, "wind_from"), findString(object, "arah_angin_dari")));
        forecast.setWindTo(firstNonBlank(findString(object, "wind_to"), findString(object, "arah_angin_ke")));
        forecast.setWindSpeedMin(firstNonBlank(findString(object, "wind_speed_min"), findString(object, "wind_min")));
        forecast.setWindSpeedMax(firstNonBlank(findString(object, "wind_speed_max"), findString(object, "wind_max")));
        forecast.setWarning(firstNonBlank(findString(object, "warning_desc"), findString(object, "warning"), findString(object, "peringatan")));
        forecast.setStationRemark(firstNonBlank(findString(object, "station_remark"), findString(object, "remark")));
        return forecast;
    }

    private boolean hasAnyKey(JsonObject object, String... keys) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            for (String key : keys) {
                if (key.equalsIgnoreCase(entry.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String findString(JsonElement element, String targetKey) {
        if (element == null || element.isJsonNull()) {
            return "";
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (targetKey.equalsIgnoreCase(entry.getKey()) && entry.getValue() != null && !entry.getValue().isJsonNull()) {
                    return scalarToString(entry.getValue());
                }
            }
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String found = findString(entry.getValue(), targetKey);
                if (!found.isEmpty()) {
                    return found;
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                String found = findString(item, targetKey);
                if (!found.isEmpty()) {
                    return found;
                }
            }
        }
        return "";
    }

    private String scalarToString(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return "";
        }
        if (value.isJsonPrimitive()) {
            return value.getAsString();
        }
        return value.toString();
    }

    private void saveCache(String cacheKey, TideResponse tide) {
        long now = System.currentTimeMillis();
        String json = gson.toJson(tide);
        TideCache cache = new TideCache(cacheKey, json, "BMKG", tide.getAreaName(), now, now + Constants.BMKG_CACHE_TTL_MS);
        firestore.collection(Constants.COL_TIDE_CACHE).document(cacheKey).set(cache);
        cleanupExpiredCache();
    }

    private void publishFallback(TideCache cache, MutableLiveData<TideResponse> liveData) {
        if (cache == null || !isUsableFallback(cache.getUpdatedAt())) {
            return;
        }
        TideResponse cachedResponse = parseCache(cache.getData(), new MutableLiveData<>());
        if (cachedResponse != null) {
            liveData.setValue(cachedResponse);
        }
    }

    private TideResponse parseCache(String data, MutableLiveData<String> errorData) {
        try {
            return gson.fromJson(data, TideResponse.class);
        } catch (JsonSyntaxException error) {
            errorData.setValue("Cache prakiraan maritim tidak valid, memuat ulang dari BMKG.");
            return null;
        }
    }

    private boolean isFresh(long updatedAt) {
        return System.currentTimeMillis() - updatedAt <= Constants.BMKG_CACHE_TTL_MS;
    }

    private boolean isUsableFallback(long updatedAt) {
        return System.currentTimeMillis() - updatedAt <= Constants.EXTERNAL_CACHE_MAX_AGE_MS;
    }

    private void cleanupExpiredCache() {
        long cutoff = System.currentTimeMillis() - Constants.EXTERNAL_CACHE_MAX_AGE_MS;
        firestore.collection(Constants.COL_TIDE_CACHE)
                .whereLessThan("updatedAt", cutoff)
                .limit(20)
                .get()
                .addOnSuccessListener(query -> query.getDocuments().forEach(document -> document.getReference().delete()));
    }

    private void resolveMarineArea(double lat, double lon, MarineAreaCallback callback) {
        if (cachedMarineAreaPolygons != null && !cachedMarineAreaPolygons.isEmpty()) {
            callback.onResolved(resolveFromPolygons(lat, lon, cachedMarineAreaPolygons));
            return;
        }

        NetworkModule.getTideService().getMarineAreaPolygons().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MarineAreaPolygon> polygons = parseMarineAreaPolygons(response.body());
                    if (!polygons.isEmpty()) {
                        cachedMarineAreaPolygons = polygons;
                        String area = resolveFromPolygons(lat, lon, polygons);
                        Log.d(TAG, "polygon selector area=" + area + " lat=" + lat + " lon=" + lon);
                        callback.onResolved(area);
                        return;
                    }
                }
                String fallback = BMKGAreaSelector.resolveMarineArea(lat, lon);
                Log.d(TAG, "polygon selector fallback area=" + fallback + " lat=" + lat + " lon=" + lon);
                callback.onResolved(fallback);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable error) {
                String fallback = BMKGAreaSelector.resolveMarineArea(lat, lon);
                Log.d(TAG, "polygon selector failed fallback area=" + fallback + " reason=" + safeMessage(error));
                callback.onResolved(fallback);
            }
        });
    }

    private List<MarineAreaPolygon> parseMarineAreaPolygons(JsonObject root) {
        List<MarineAreaPolygon> polygons = new ArrayList<>();
        JsonArray features = findArray(root, "features");
        if (features == null) {
            return polygons;
        }

        for (JsonElement item : features) {
            if (item == null || !item.isJsonObject()) {
                continue;
            }
            JsonObject feature = item.getAsJsonObject();
            JsonObject properties = getObject(feature, "properties");
            JsonObject geometry = getObject(feature, "geometry");
            if (properties == null || geometry == null) {
                continue;
            }
            logPolygonSample(properties, geometry);

            String wp1 = findString(properties, "WP_1");
            String wpImm = firstNonBlank(findString(properties, "WP_IMM"), findString(properties, "WPIMM"));
            String code = firstNonBlank(
                    findString(properties, "kode"),
                    findString(properties, "KODE"),
                    findString(properties, "kdpp"),
                    findString(properties, "KDPP"),
                    findString(properties, "kode_area"),
                    findString(properties, "kode_perairan"),
                    findString(properties, "code"),
                    findString(properties, "area_code"),
                    findString(properties, "WP_ID"),
                    extractAreaCode(wp1),
                    extractAreaCode(wpImm)
            );
            String name = firstNonBlank(
                    findString(properties, "nama"),
                    findString(properties, "NAMA"),
                    findString(properties, "namobj"),
                    findString(properties, "NAMOBJ"),
                    findString(properties, "nama_perairan"),
                    findString(properties, "nama_wilayah"),
                    findString(properties, "name"),
                    findString(properties, "area"),
                    findString(properties, "wilayah"),
                    findString(properties, "wilayah_perairan"),
                    findString(properties, "perairan"),
                    extractAreaName(wpImm),
                    extractAreaName(wp1),
                    wpImm,
                    wp1
            );
            String endpoint = firstNonBlank(
                    findString(properties, "url"),
                    findString(properties, "endpoint"),
                    buildMarineAreaEndpoint(code, name)
            );
            if (endpoint.startsWith("http")) {
                endpoint = endpoint.substring(endpoint.lastIndexOf('/') + 1).replace(".json", "");
            }
            if (endpoint.isEmpty()) {
                continue;
            }

            MarineAreaPolygon polygon = new MarineAreaPolygon(endpoint, name);
            String type = firstNonBlank(findString(geometry, "type"));
            JsonArray coordinates = getArray(geometry, "coordinates");
            if (coordinates == null) {
                continue;
            }
            if ("MultiPolygon".equalsIgnoreCase(type)) {
                parseMultiPolygon(coordinates, polygon);
            } else {
                parsePolygon(coordinates, polygon);
            }
            if (!polygon.polygons.isEmpty()) {
                polygons.add(polygon);
            }
        }
        Log.d(TAG, "polygon selector loaded regions=" + polygons.size());
        return polygons;
    }

    private void logPolygonSample(JsonObject properties, JsonObject geometry) {
        if (loggedPolygonSample) {
            return;
        }
        loggedPolygonSample = true;
        Log.d(TAG, "polygon sample properties=" + properties.keySet()
                + " wp1=" + findString(properties, "WP_1")
                + " wpImm=" + firstNonBlank(findString(properties, "WP_IMM"), findString(properties, "WPIMM"))
                + " geometry=" + geometry.keySet());
    }

    private String extractAreaCode(String value) {
        if (value == null) {
            return "";
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("([A-Z]\\.\\d{2})")
                .matcher(value.toUpperCase(Locale.US));
        return matcher.find() ? matcher.group(1) : "";
    }

    private String extractAreaName(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.replaceFirst("(?i)^[A-Z]\\.\\d{2}\\s*[-–:]?\\s*", "").trim();
    }

    private void parseMultiPolygon(JsonArray coordinates, MarineAreaPolygon target) {
        for (JsonElement polygonElement : coordinates) {
            if (polygonElement != null && polygonElement.isJsonArray()) {
                parsePolygon(polygonElement.getAsJsonArray(), target);
            }
        }
    }

    private void parsePolygon(JsonArray polygonCoordinates, MarineAreaPolygon target) {
        List<List<Point>> polygon = new ArrayList<>();
        for (JsonElement ringElement : polygonCoordinates) {
            if (ringElement == null || !ringElement.isJsonArray()) {
                continue;
            }
            List<Point> ring = new ArrayList<>();
            for (JsonElement pointElement : ringElement.getAsJsonArray()) {
                if (pointElement == null || !pointElement.isJsonArray()) {
                    continue;
                }
                JsonArray pair = pointElement.getAsJsonArray();
                if (pair.size() < 2) {
                    continue;
                }
                ring.add(new Point(pair.get(1).getAsDouble(), pair.get(0).getAsDouble()));
            }
            if (ring.size() >= 3) {
                polygon.add(ring);
            }
        }
        if (!polygon.isEmpty()) {
            target.polygons.add(polygon);
        }
    }

    private String resolveFromPolygons(double lat, double lon, List<MarineAreaPolygon> polygons) {
        for (MarineAreaPolygon area : polygons) {
            if (area.contains(lat, lon)) {
                return area.endpoint;
            }
        }
        return BMKGAreaSelector.resolveMarineArea(lat, lon);
    }

    private String buildMarineAreaEndpoint(String code, String name) {
        if (code.isEmpty() || name.isEmpty()) {
            return "";
        }
        return code + "_" + encodePathSegment(name);
    }

    private String encodePathSegment(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ignored) {
            return value.replace(" ", "%20");
        }
    }

    private JsonObject getObject(JsonObject object, String key) {
        JsonElement value = object != null ? object.get(key) : null;
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    private JsonArray getArray(JsonObject object, String key) {
        JsonElement value = object != null ? object.get(key) : null;
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : null;
    }

    private JsonArray findArray(JsonElement element, String targetKey) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (targetKey.equalsIgnoreCase(entry.getKey()) && entry.getValue() != null && entry.getValue().isJsonArray()) {
                    return entry.getValue().getAsJsonArray();
                }
            }
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                JsonArray found = findArray(entry.getValue(), targetKey);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private String createCacheKey(String marineArea) {
        return "bmkg_" + marineArea.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "_");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String safeMessage(Throwable error) {
        return error.getMessage() != null ? error.getMessage() : "terjadi kesalahan jaringan";
    }

    private int getForecastCount(TideResponse response) {
        return response != null && response.getForecasts() != null ? response.getForecasts().size() : 0;
    }

    private interface MarineAreaCallback {
        void onResolved(String marineArea);
    }

    private static class MarineAreaPolygon {
        private final String endpoint;
        private final String name;
        private final List<List<List<Point>>> polygons = new ArrayList<>();

        MarineAreaPolygon(String endpoint, String name) {
            this.endpoint = endpoint;
            this.name = name;
        }

        boolean contains(double lat, double lon) {
            for (List<List<Point>> polygon : polygons) {
                if (polygon.isEmpty()) {
                    continue;
                }
                if (!isPointInRing(lat, lon, polygon.get(0))) {
                    continue;
                }
                boolean insideHole = false;
                for (int i = 1; i < polygon.size(); i++) {
                    if (isPointInRing(lat, lon, polygon.get(i))) {
                        insideHole = true;
                        break;
                    }
                }
                if (!insideHole) {
                    return true;
                }
            }
            return false;
        }

        private boolean isPointInRing(double lat, double lon, List<Point> ring) {
            boolean inside = false;
            for (int i = 0, j = ring.size() - 1; i < ring.size(); j = i++) {
                Point pi = ring.get(i);
                Point pj = ring.get(j);
                boolean intersects = ((pi.lat > lat) != (pj.lat > lat))
                        && (lon < (pj.lon - pi.lon) * (lat - pi.lat) / ((pj.lat - pi.lat) + 0.0) + pi.lon);
                if (intersects) {
                    inside = !inside;
                }
            }
            return inside;
        }
    }

    private static class Point {
        private final double lat;
        private final double lon;

        Point(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}
