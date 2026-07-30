package com.example.java3.data.remote;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MarineHourlyResponse {
    @SerializedName("latitude")
    private double latitude;
    @SerializedName("longitude")
    private double longitude;
    @SerializedName("hourly_units")
    private HourlyUnits hourlyUnits;
    @SerializedName("hourly")
    private Hourly hourly;

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public HourlyUnits getHourlyUnits() { return hourlyUnits; }
    public Hourly getHourly() { return hourly; }

    public float getCurrentWaveHeight() {
        return getFloatAt(hourly != null ? hourly.waveHeight : null, getCurrentHourIndex());
    }

    public float getCurrentWavePeriod() {
        return getFloatAt(hourly != null ? hourly.wavePeriod : null, getCurrentHourIndex());
    }

    public float getCurrentSeaSurfaceTemperature() {
        return getFloatAt(hourly != null ? hourly.seaSurfaceTemperature : null, getCurrentHourIndex());
    }

    public float getCurrentOceanCurrentVelocity() {
        return getFloatAt(hourly != null ? hourly.oceanCurrentVelocity : null, getCurrentHourIndex());
    }

    public float getCurrentOceanCurrentDirection() {
        return getFloatAt(hourly != null ? hourly.oceanCurrentDirection : null, getCurrentHourIndex());
    }

    public float getCurrentWaveDirection() {
        return getFloatAt(hourly != null ? hourly.waveDirection : null, getCurrentHourIndex());
    }

    public float getCurrentWindWaveHeight() {
        return getFloatAt(hourly != null ? hourly.windWaveHeight : null, getCurrentHourIndex());
    }

    public float getCurrentSwellWaveHeight() {
        return getFloatAt(hourly != null ? hourly.swellWaveHeight : null, getCurrentHourIndex());
    }

    public float getTodayMaxWaveHeight() {
        List<Float> values = getNext24WaveHeights();
        float max = 0f;
        for (Float value : values) {
            if (value != null && value > max) {
                max = value;
            }
        }
        return max;
    }

    public float getTodayAverageWaveHeight() {
        List<Float> values = getNext24WaveHeights();
        if (values.isEmpty()) return 0f;
        float sum = 0f;
        int count = 0;
        for (Float value : values) {
            if (value != null) {
                sum += value;
                count++;
            }
        }
        return count == 0 ? 0f : sum / count;
    }

    public int getCurrentHourIndexInNext24() {
        return 0;
    }

    public List<Float> getNext24WaveHeights() {
        return sliceFloatList(hourly != null ? hourly.waveHeight : null);
    }

    public List<String> getNext24TimeLabels() {
        return sliceStringList(hourly != null ? hourly.time : null);
    }

    public int getCurrentHourIndex() {
        if (hourly == null || hourly.time == null || hourly.time.isEmpty()) {
            return 0;
        }
        long now = System.currentTimeMillis();
        long bestDiff = Long.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < hourly.time.size(); i++) {
            Date date = parseTime(hourly.time.get(i));
            if (date == null) continue;
            long diff = Math.abs(date.getTime() - now);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    public String getWaveLabel() {
        return labelForWave(getCurrentWaveHeight());
    }

    public String getTodayMaxWaveLabel() {
        return labelForWave(getTodayMaxWaveHeight());
    }

    public String getSeaTemperatureLabel() {
        float value = getCurrentSeaSurfaceTemperature();
        return value > 0f ? String.format(Locale.getDefault(), "%.0f°", value) : "--";
    }

    public String getCurrentVelocityLabel() {
        float value = getCurrentOceanCurrentVelocity();
        return value > 0f ? String.format(Locale.getDefault(), "%.1f m/s", value) : "-- m/s";
    }

    public String getCurrentDirectionLabel() {
        float value = getCurrentOceanCurrentDirection();
        if (value <= 0f) return "--";
        return String.format(Locale.getDefault(), "%.0f°", value);
    }

    public String getCurrentDirectionCompass() {
        float value = getCurrentOceanCurrentDirection();
        return value > 0f ? degreesToCompass(value) : "--";
    }

    public String getWaveDirectionLabel() {
        float value = getCurrentWaveDirection();
        if (value <= 0f) return "--";
        return String.format(Locale.getDefault(), "%.0f° %s", value, degreesToCompass(value));
    }

    public static String labelForWave(float meters) {
        if (meters <= 0f) return "Belum tersedia";
        if (meters <= 0.5f) return "Tenang";
        if (meters <= 1.25f) return "Rendah";
        if (meters <= 2.5f) return "Sedang";
        if (meters <= 4.0f) return "Tinggi";
        return "Sangat Tinggi";
    }

    private List<Float> sliceFloatList(List<Float> source) {
        List<Float> result = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return result;
        }
        int start = getCurrentHourIndex();
        if (start < 0 || start >= source.size()) {
            start = 0;
        }
        int limit = Math.min(source.size(), start + 24);
        for (int i = start; i < limit; i++) {
            Float value = source.get(i);
            result.add(value != null ? value : 0f);
        }
        return result;
    }

    private List<String> sliceStringList(List<String> source) {
        List<String> result = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return result;
        }
        int start = getCurrentHourIndex();
        if (start < 0 || start >= source.size()) {
            start = 0;
        }
        int limit = Math.min(source.size(), start + 24);
        for (int i = start; i < limit; i++) {
            String value = source.get(i);
            result.add(value != null ? value : "");
        }
        return result;
    }

    private float getFloatAt(List<Float> source, int index) {
        if (source == null || source.isEmpty() || index < 0 || index >= source.size()) {
            return 0f;
        }
        Float value = source.get(index);
        return value != null ? value : 0f;
    }

    private Date parseTime(String value) {
        if (value == null) return null;
        String[] patterns = new String[]{"yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd HH:mm"};
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.US).parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private String degreesToCompass(float degrees) {
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = Math.round((degrees % 360f) / 22.5f) % 16;
        return directions[index];
    }

    public static class HourlyUnits {
        @SerializedName("time")
        private String time;
        @SerializedName("wave_height")
        private String waveHeight;
        @SerializedName("wave_direction")
        private String waveDirection;
        @SerializedName("wave_period")
        private String wavePeriod;
        @SerializedName("wind_wave_height")
        private String windWaveHeight;
        @SerializedName("swell_wave_height")
        private String swellWaveHeight;
        @SerializedName("sea_surface_temperature")
        private String seaSurfaceTemperature;
        @SerializedName("ocean_current_velocity")
        private String oceanCurrentVelocity;
        @SerializedName("ocean_current_direction")
        private String oceanCurrentDirection;

        public String getTime() { return time; }
        public String getWaveHeight() { return waveHeight; }
        public String getWaveDirection() { return waveDirection; }
        public String getWavePeriod() { return wavePeriod; }
        public String getWindWaveHeight() { return windWaveHeight; }
        public String getSwellWaveHeight() { return swellWaveHeight; }
        public String getSeaSurfaceTemperature() { return seaSurfaceTemperature; }
        public String getOceanCurrentVelocity() { return oceanCurrentVelocity; }
        public String getOceanCurrentDirection() { return oceanCurrentDirection; }
    }

    public static class Hourly {
        @SerializedName("time")
        private List<String> time;
        @SerializedName("wave_height")
        private List<Float> waveHeight;
        @SerializedName("wave_direction")
        private List<Float> waveDirection;
        @SerializedName("wave_period")
        private List<Float> wavePeriod;
        @SerializedName("wind_wave_height")
        private List<Float> windWaveHeight;
        @SerializedName("swell_wave_height")
        private List<Float> swellWaveHeight;
        @SerializedName("sea_surface_temperature")
        private List<Float> seaSurfaceTemperature;
        @SerializedName("ocean_current_velocity")
        private List<Float> oceanCurrentVelocity;
        @SerializedName("ocean_current_direction")
        private List<Float> oceanCurrentDirection;

        public List<String> getTime() { return time; }
        public List<Float> getWaveHeight() { return waveHeight; }
        public List<Float> getWaveDirection() { return waveDirection; }
        public List<Float> getWavePeriod() { return wavePeriod; }
        public List<Float> getWindWaveHeight() { return windWaveHeight; }
        public List<Float> getSwellWaveHeight() { return swellWaveHeight; }
        public List<Float> getSeaSurfaceTemperature() { return seaSurfaceTemperature; }
        public List<Float> getOceanCurrentVelocity() { return oceanCurrentVelocity; }
        public List<Float> getOceanCurrentDirection() { return oceanCurrentDirection; }
    }
}
