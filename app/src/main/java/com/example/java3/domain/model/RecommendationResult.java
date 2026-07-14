package com.example.java3.domain.model;

public class RecommendationResult {
    private double score;
    private String badgeText;
    private int badgeColor;
    private int stars;

    public RecommendationResult(double score, String badgeText, int badgeColor, int stars) {
        this.score = score;
        this.badgeText = badgeText;
        this.badgeColor = badgeColor;
        this.stars = stars;
    }

    public double getScore() { return score; }
    public String getBadgeText() { return badgeText; }
    public int getBadgeColor() { return badgeColor; }
    public int getStars() { return stars; }
    public int getScorePercentage() { return (int) Math.round(score); }
}
