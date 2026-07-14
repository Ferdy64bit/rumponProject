package com.example.java3.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TideResponse {
    @SerializedName("station")
    private TideStation station;

    @SerializedName("extremes")
    private List<TideExtreme> extremes;

    @SerializedName("dailyConditions")
    private List<DailyCondition> dailyConditions;

    public TideStation getStation() { return station; }
    public List<TideExtreme> getExtremes() { return extremes; }
    public List<DailyCondition> getDailyConditions() { return dailyConditions; }

    // Helper methods for UI compatibility
    public String getHighTide() {
        return getExtremeTime("high");
    }

    public String getLowTide() {
        return getExtremeTime("low");
    }

    public float getTideHeight() {
        if (extremes != null && !extremes.isEmpty()) {
            return extremes.get(0).getHeight();
        }
        return 0f;
    }

    public String getFishingActivity() {
        if (dailyConditions != null && !dailyConditions.isEmpty()) {
            return dailyConditions.get(0).getSolunarLabel();
        }
        return "Normal";
    }

    public String getBestFishingWindow() {
        if (dailyConditions != null && !dailyConditions.isEmpty() && dailyConditions.get(0).getSolunarPeriods() != null) {
            for (SolunarPeriod p : dailyConditions.get(0).getSolunarPeriods()) {
                if ("major".equalsIgnoreCase(p.getType())) {
                    return formatIsoTime(p.getPeak());
                }
            }
        }
        return "--";
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
