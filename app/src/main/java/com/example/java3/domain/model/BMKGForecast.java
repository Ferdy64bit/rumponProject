package com.example.java3.domain.model;

import java.util.Locale;

public class BMKGForecast {
    private String validFrom;
    private String validTo;
    private String timeDesc;
    private String weather;
    private String weatherDesc;
    private String waveCategory;
    private String waveDescription;
    private String windFrom;
    private String windTo;
    private String windSpeedMin;
    private String windSpeedMax;
    private String warning;
    private String stationRemark;

    public String getValidFrom() { return validFrom; }
    public void setValidFrom(String validFrom) { this.validFrom = validFrom; }

    public String getValidTo() { return validTo; }
    public void setValidTo(String validTo) { this.validTo = validTo; }

    public String getTimeDesc() { return timeDesc; }
    public void setTimeDesc(String timeDesc) { this.timeDesc = timeDesc; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    public String getWeatherDesc() { return weatherDesc; }
    public void setWeatherDesc(String weatherDesc) { this.weatherDesc = weatherDesc; }

    public String getWaveCategory() { return waveCategory; }
    public void setWaveCategory(String waveCategory) { this.waveCategory = waveCategory; }

    public String getWaveDescription() { return waveDescription; }
    public void setWaveDescription(String waveDescription) { this.waveDescription = waveDescription; }

    public String getWindFrom() { return windFrom; }
    public void setWindFrom(String windFrom) { this.windFrom = windFrom; }

    public String getWindTo() { return windTo; }
    public void setWindTo(String windTo) { this.windTo = windTo; }

    public String getWindSpeedMin() { return windSpeedMin; }
    public void setWindSpeedMin(String windSpeedMin) { this.windSpeedMin = windSpeedMin; }

    public String getWindSpeedMax() { return windSpeedMax; }
    public void setWindSpeedMax(String windSpeedMax) { this.windSpeedMax = windSpeedMax; }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }

    public String getStationRemark() { return stationRemark; }
    public void setStationRemark(String stationRemark) { this.stationRemark = stationRemark; }

    public String getDisplayDate() {
        return firstNonBlank(timeDesc, validFrom, validTo, "BMKG");
    }

    public String getWeatherLabel() {
        return firstNonBlank(weatherDesc, weather, "Cuaca tersedia");
    }

    public String getWaveLabel() {
        return firstNonBlank(waveCategory, waveDescription, "Gelombang --");
    }

    public String getWindLabel() {
        String speed = firstNonBlank(windSpeedMin, windSpeedMax);
        if (!isBlank(windSpeedMin) && !isBlank(windSpeedMax) && !windSpeedMin.equals(windSpeedMax)) {
            speed = windSpeedMin + "-" + windSpeedMax;
        }

        String direction = firstNonBlank(windFrom, windTo);
        if (!isBlank(windFrom) && !isBlank(windTo) && !windFrom.equals(windTo)) {
            direction = windFrom + " ke " + windTo;
        }

        if (isBlank(speed) && isBlank(direction)) return "Angin --";
        if (isBlank(direction)) return "Angin " + speed;
        if (isBlank(speed)) return "Angin " + direction;
        return "Angin " + direction + " " + speed;
    }

    public boolean hasWarning() {
        String value = warning != null ? warning.trim().toLowerCase(Locale.getDefault()) : "";
        return !value.isEmpty() && !"null".equals(value) && !"-".equals(value) && !value.contains("nihil");
    }

    public String getRecommendationLabel() {
        if (hasWarning()) return "Tidak Direkomendasikan";

        float wave = extractFirstNumber(firstNonBlank(waveDescription, waveCategory));
        float wind = extractFirstNumber(firstNonBlank(windSpeedMax, windSpeedMin));
        String text = (getWeatherLabel() + " " + getWaveLabel()).toLowerCase(Locale.getDefault());

        if (text.contains("sangat tinggi") || text.contains("ekstrem") || wave > 2.5f) {
            return "Tidak Direkomendasikan";
        }
        if (text.contains("hujan") || text.contains("badai") || text.contains("petir") || wind > 20f) {
            return "Perlu Waspada";
        }
        if ((wave > 0f && wave <= 0.5f) && (wind == 0f || wind <= 15f)) {
            return "Sangat Direkomendasikan";
        }
        if (wave > 0f && wave <= 1.25f) {
            return "Direkomendasikan";
        }
        return "Cukup Baik";
    }

    public int getRecommendationScore() {
        switch (getRecommendationLabel()) {
            case "Sangat Direkomendasikan": return 95;
            case "Direkomendasikan": return 82;
            case "Cukup Baik": return 68;
            case "Perlu Waspada": return 48;
            default: return 25;
        }
    }

    private float extractFirstNumber(String value) {
        if (value == null) return 0f;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+(?:[.,]\\d+)?)").matcher(value);
        if (!matcher.find()) return 0f;
        try {
            return Float.parseFloat(matcher.group(1).replace(',', '.'));
        } catch (NumberFormatException ignored) {
            return 0f;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) return value.trim();
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
