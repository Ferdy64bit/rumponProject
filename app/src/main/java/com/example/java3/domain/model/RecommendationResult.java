package com.example.java3.domain.model;

public class RecommendationResult {
    private final double score;
    private final String badgeText;
    private final int badgeColor;
    private final int stars;
    private final double distanceScore;
    private final double weatherScore;
    private final double marineScore;
    private final double spotQualityScore;
    private final double activityScore;
    private final double userPreferenceScore;
    private final double safetyMultiplier;

    public RecommendationResult(double score, String badgeText, int badgeColor, int stars) {
        this(score, badgeText, badgeColor, stars, 0, 0, 0, 0, 0, 0, 1.0);
    }

    public RecommendationResult(double score, String badgeText, int badgeColor, int stars,
                                double distanceScore, double weatherScore, double marineScore,
                                double spotQualityScore, double activityScore, double userPreferenceScore,
                                double safetyMultiplier) {
        this.score = score;
        this.badgeText = badgeText;
        this.badgeColor = badgeColor;
        this.stars = stars;
        this.distanceScore = distanceScore;
        this.weatherScore = weatherScore;
        this.marineScore = marineScore;
        this.spotQualityScore = spotQualityScore;
        this.activityScore = activityScore;
        this.userPreferenceScore = userPreferenceScore;
        this.safetyMultiplier = safetyMultiplier;
    }

    public double getScore() { return score; }
    public String getBadgeText() { return badgeText; }
    public int getBadgeColor() { return badgeColor; }
    public int getStars() { return stars; }
    public int getScorePercentage() { return (int) Math.round(score); }
    public double getDistanceScore() { return distanceScore; }
    public double getWeatherScore() { return weatherScore; }
    public double getMarineScore() { return marineScore; }
    public double getSpotQualityScore() { return spotQualityScore; }
    public double getActivityScore() { return activityScore; }
    public double getUserPreferenceScore() { return userPreferenceScore; }
    public double getSafetyMultiplier() { return safetyMultiplier; }
}
