package com.example.java3.domain.model;

import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.data.remote.TideResponse;

public class FishingPointWithRecommendation {
    private FishingPoint fishingPoint;
    private RecommendationResult recommendation;
    private WeatherResponse weather;
    private TideResponse tide;
    private double distance;

    public FishingPointWithRecommendation(FishingPoint fishingPoint, RecommendationResult recommendation, WeatherResponse weather, TideResponse tide, double distance) {
        this.fishingPoint = fishingPoint;
        this.recommendation = recommendation;
        this.weather = weather;
        this.tide = tide;
        this.distance = distance;
    }

    public FishingPoint getFishingPoint() { return fishingPoint; }
    public RecommendationResult getRecommendation() { return recommendation; }
    public WeatherResponse getWeather() { return weather; }
    public TideResponse getTide() { return tide; }
    public double getDistance() { return distance; }
}
