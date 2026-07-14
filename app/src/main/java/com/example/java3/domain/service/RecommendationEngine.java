package com.example.java3.domain.service;

import android.graphics.Color;

import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.domain.model.RecommendationResult;

/**
 * Pure Domain Service to calculate fishing recommendations based on environmental data.
 */
public class RecommendationEngine {

    /**
     * Calculates a recommendation score and result.
     *
     * @param tide     Current tide information.
     * @param weather  Current weather information.
     * @param distance Distance to the spot in KM.
     * @param rating   User rating of the spot.
     * @return A RecommendationResult containing the score, badge text, and color.
     */
    public static RecommendationResult calculate(TideResponse tide, WeatherResponse weather, double distance, float rating) {
        double tideScore = getTideScore(tide);
        double weatherScore = getWeatherScore(weather);
        double distanceScore = getDistanceScore(distance);
        double ratingScore = getRatingScore(rating);

        double finalScore = (tideScore * 0.40) + (weatherScore * 0.30) + (distanceScore * 0.20) + (ratingScore * 0.10);

        String badgeText;
        int color;
        int stars;

        if (finalScore >= 90) {
            badgeText = "Highly Recommended";
            color = Color.parseColor("#22C55E"); // Green
            stars = 5;
        } else if (finalScore >= 75) {
            badgeText = "Recommended";
            color = Color.parseColor("#4A90E2"); // Blue
            stars = 4;
        } else if (finalScore >= 60) {
            badgeText = "Fair";
            color = Color.parseColor("#EAB308"); // Yellow
            stars = 3;
        } else if (finalScore >= 40) {
            badgeText = "Less Recommended";
            color = Color.parseColor("#F97316"); // Orange
            stars = 2;
        } else {
            badgeText = "Not Recommended";
            color = Color.parseColor("#EF4444"); // Red
            stars = 1;
        }

        return new RecommendationResult(finalScore, badgeText, color, stars);
    }

    private static double getTideScore(TideResponse tide) {
        if (tide == null || tide.getFishingActivity() == null) return 50;
        switch (tide.getFishingActivity()) {
            case "Excellent": return 100;
            case "Good": return 80;
            case "Fair": return 60;
            case "Poor": return 40;
            case "Very Poor": return 20;
            default: return 50;
        }
    }

    private static double getWeatherScore(WeatherResponse weather) {
        if (weather == null || weather.getWeather() == null || weather.getWeather().isEmpty()) return 50;
        
        double score = 0;
        String main = weather.getWeather().get(0).getMain();
        
        if (main.equalsIgnoreCase("Clear")) score = 100;
        else if (main.equalsIgnoreCase("Clouds")) score = 90;
        else if (main.equalsIgnoreCase("Mist") || main.equalsIgnoreCase("Fog")) score = 70;
        else if (main.equalsIgnoreCase("Drizzle")) score = 50;
        else if (main.equalsIgnoreCase("Rain")) score = 30;
        else if (main.equalsIgnoreCase("Thunderstorm")) score = 10;
        else score = 50;

        if (weather.getWind() != null && weather.getWind().getSpeed() * 3.6 > 20) { 
            score -= 20;
        }
        
        String description = weather.getWeather().get(0).getDescription();
        String desc = description != null ? description.toLowerCase() : "";
        if (desc.contains("heavy")) score -= 30;
        if (desc.contains("storm")) score -= 50;

        return Math.max(0, score);
    }

    private static double getDistanceScore(double distance) {
        if (distance < 1) return 100;
        if (distance <= 3) return 90;
        if (distance <= 5) return 80;
        if (distance <= 10) return 60;
        if (distance <= 20) return 40;
        return 20;
    }

    private static double getRatingScore(float rating) {
        if (rating >= 5.0) return 100;
        if (rating >= 4.5) return 90;
        if (rating >= 4.0) return 80;
        if (rating >= 3.5) return 70;
        if (rating >= 3.0) return 60;
        return 40;
    }
}
