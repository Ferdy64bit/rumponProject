package com.example.java3.data.remote;

import com.google.gson.annotations.SerializedName;
import com.example.java3.domain.model.BMKGForecast;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TideResponse {
    private String source;
    private String areaName;
    private String weather;
    private String warningDesc;
    private String waveCat;
    private String waveDesc;
    private String windFrom;
    private String windTo;
    private String windSpeedMin;
    private String windSpeedMax;
    private String forecastTime;
    private List<BMKGForecast> forecasts;

    @SerializedName("station")
    private TideStation station;

    @SerializedName("extremes")
    private List<TideExtreme> extremes;

    @SerializedName("dailyConditions")
    private List<DailyCondition> dailyConditions;

    public TideStation getStation() { return station; }
    public List<TideExtreme> getExtremes() { return extremes; }
    public List<DailyCondition> getDailyConditions() { return dailyConditions; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    public String getWarningDesc() { return warningDesc; }
    public void setWarningDesc(String warningDesc) { this.warningDesc = warningDesc; }

    public String getWaveCat() { return waveCat; }
    public void setWaveCat(String waveCat) { this.waveCat = waveCat; }

    public String getWaveDesc() { return waveDesc; }
    public void setWaveDesc(String waveDesc) { this.waveDesc = waveDesc; }

    public String getWindFrom() { return windFrom; }
    public void setWindFrom(String windFrom) { this.windFrom = windFrom; }

    public String getWindTo() { return windTo; }
    public void setWindTo(String windTo) { this.windTo = windTo; }

    public String getWindSpeedMin() { return windSpeedMin; }
    public void setWindSpeedMin(String windSpeedMin) { this.windSpeedMin = windSpeedMin; }

    public String getWindSpeedMax() { return windSpeedMax; }
    public void setWindSpeedMax(String windSpeedMax) { this.windSpeedMax = windSpeedMax; }

    public String getForecastTime() { return forecastTime; }
    public void setForecastTime(String forecastTime) { this.forecastTime = forecastTime; }

    public List<BMKGForecast> getForecasts() { return forecasts; }
    public void setForecasts(List<BMKGForecast> forecasts) { this.forecasts = forecasts; }

    // Helper methods for UI compatibility
    public String getHighTide() {
        if (isBmkg()) {
            BMKGForecast forecast = getPrimaryForecast();
            if (forecast != null) {
                return firstNonBlank(forecast.getWaveLabel(), waveCat, waveDesc, "Gelombang --");
            }
            return firstNonBlank(waveCat, waveDesc, "Gelombang --");
        }
        return getExtremeTime("high");
    }

    public String getLowTide() {
        if (isBmkg()) {
            BMKGForecast forecast = getPrimaryForecast();
            if (forecast != null) {
                return forecast.getWindLabel();
            }
            return getWindSummary();
        }
        return getExtremeTime("low");
    }

    public float getTideHeight() {
        if (isBmkg()) {
            return extractFirstNumber(waveDesc);
        }
        if (extremes != null && !extremes.isEmpty()) {
            return extremes.get(0).getHeight();
        }
        return 0f;
    }

    public String getFishingActivity() {
        if (isBmkg()) {
            BMKGForecast forecast = getPrimaryForecast();
            if (forecast != null) {
                return toLegacyRecommendation(forecast.getRecommendationLabel());
            }
            return calculateMarineActivity();
        }
        if (dailyConditions != null && !dailyConditions.isEmpty()) {
            return dailyConditions.get(0).getSolunarLabel();
        }
        return "Normal";
    }

    public String getBestFishingWindow() {
        if (isBmkg()) {
            BMKGForecast forecast = getPrimaryForecast();
            if (forecast != null) {
                return firstNonBlank(forecast.getDisplayDate(), forecastTime, "BMKG");
            }
            return firstNonBlank(forecastTime, "BMKG");
        }
        if (dailyConditions != null && !dailyConditions.isEmpty() && dailyConditions.get(0).getSolunarPeriods() != null) {
            for (SolunarPeriod p : dailyConditions.get(0).getSolunarPeriods()) {
                if ("major".equalsIgnoreCase(p.getType())) {
                    return formatIsoTime(p.getPeak());
                }
            }
        }
        return "--";
    }

    public String getMarineSummary() {
        if (!isBmkg()) {
            return getFishingActivity();
        }
        BMKGForecast forecast = getPrimaryForecast();
        if (forecast != null) {
            return firstNonBlank(forecast.getWeatherLabel(), forecast.getWaveLabel(), "Kondisi perairan tersedia");
        }
        return firstNonBlank(weather, waveCat, waveDesc, "Kondisi perairan tersedia");
    }

    public boolean hasWarning() {
        BMKGForecast forecast = getPrimaryForecast();
        if (forecast != null && forecast.hasWarning()) {
            return true;
        }
        String warning = warningDesc != null ? warningDesc.trim().toLowerCase(Locale.getDefault()) : "";
        return !warning.isEmpty() && !"null".equals(warning) && !"-".equals(warning) && !warning.contains("nihil");
    }

    private BMKGForecast getPrimaryForecast() {
        if (forecasts != null && !forecasts.isEmpty()) {
            return forecasts.get(0);
        }
        return null;
    }

    private String toLegacyRecommendation(String label) {
        if ("Sangat Direkomendasikan".equals(label)) return "Excellent";
        if ("Direkomendasikan".equals(label)) return "Good";
        if ("Cukup Baik".equals(label)) return "Fair";
        if ("Perlu Waspada".equals(label)) return "Poor";
        if ("Tidak Direkomendasikan".equals(label)) return "Poor";
        return "Fair";
    }

    private boolean isBmkg() {
        return "BMKG".equalsIgnoreCase(source)
                || areaName != null
                || waveCat != null
                || waveDesc != null
                || warningDesc != null;
    }

    private String calculateMarineActivity() {
        if (hasWarning()) {
            return "Poor";
        }

        String wave = firstNonBlank(waveCat, waveDesc).toLowerCase(Locale.getDefault());
        String wind = firstNonBlank(windSpeedMax, windSpeedMin).toLowerCase(Locale.getDefault());
        String condition = firstNonBlank(weather).toLowerCase(Locale.getDefault());

        if (wave.contains("sangat tinggi") || wave.contains("ekstrem") || wave.contains("tinggi")) {
            return "Poor";
        }
        if (condition.contains("hujan") || condition.contains("badai") || condition.contains("petir")) {
            return "Fair";
        }
        float windSpeed = extractFirstNumber(wind);
        if (windSpeed >= 20f) {
            return "Fair";
        }
        if (wave.contains("rendah") || wave.contains("tenang") || wave.contains("smooth") || wave.contains("slight")) {
            return "Good";
        }
        return "Fair";
    }

    private String getWindSummary() {
        String speed = firstNonBlank(windSpeedMin, windSpeedMax);
        if (!isBlank(windSpeedMin) && !isBlank(windSpeedMax) && !windSpeedMin.equals(windSpeedMax)) {
            speed = windSpeedMin + "-" + windSpeedMax;
        }
        String direction = firstNonBlank(windFrom, windTo);
        if (!isBlank(windFrom) && !isBlank(windTo) && !windFrom.equals(windTo)) {
            direction = windFrom + " ke " + windTo;
        }
        if (isBlank(speed) && isBlank(direction)) {
            return "Angin --";
        }
        if (isBlank(direction)) {
            return "Angin " + speed;
        }
        if (isBlank(speed)) {
            return "Angin " + direction;
        }
        return "Angin " + direction + " " + speed;
    }

    private float extractFirstNumber(String value) {
        if (value == null) {
            return 0f;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+(?:[.,]\\d+)?)").matcher(value);
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1).replace(',', '.'));
            } catch (NumberFormatException ignored) {
                return 0f;
            }
        }
        return 0f;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String getExtremeTime(String type) {
        if (extremes != null) {
            for (TideExtreme e : extremes) {
                if (type.equalsIgnoreCase(e.getType())) {
                    return formatIsoTime(e.getTime());
                }
            }
        }
        return "--:--";
    }

    private String formatIsoTime(String isoTime) {
        if (isoTime == null) return "--:--";
        try {
            // Tidecheck uses ISO 8601 format: 2026-03-09T03:24:00.000Z
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(isoTime);
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "--:--";
        }
    }

    public static class TideExtreme {
        @SerializedName("time")
        private String time;
        @SerializedName("height")
        private float height;
        @SerializedName("type")
        private String type;

        public String getTime() { return time; }
        public float getHeight() { return height; }
        public String getType() { return type; }
    }

    public static class DailyCondition {
        @SerializedName("solunarRating")
        private int solunarRating;
        @SerializedName("solunarLabel")
        private String solunarLabel;
        @SerializedName("solunarPeriods")
        private List<SolunarPeriod> solunarPeriods;

        public int getSolunarRating() { return solunarRating; }
        public String getSolunarLabel() { return solunarLabel; }
        public List<SolunarPeriod> getSolunarPeriods() { return solunarPeriods; }
    }

    public static class SolunarPeriod {
        @SerializedName("type")
        private String type;
        @SerializedName("peak")
        private String peak;
        @SerializedName("enhanced")
        private boolean enhanced;

        public String getType() { return type; }
        public String getPeak() { return peak; }
        public boolean isEnhanced() { return enhanced; }
    }
}
