package com.example.java3.domain.model;

/**
 * Model representing a Fishing Point.
 * Follows strict OOP principles with private fields and public accessors.
 */
public class FishingPoint {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String type; // Rumpon, Bagan, Reef, Shore
    private String fishType;
    private String area;
    private String locationName;
    private String description;
    private float rating;
    private int reviewCount;
    private String imageUrl;

    /**
     * Empty constructor required for Firebase Firestore serialization.
     */
    public FishingPoint() {
    }

    public FishingPoint(String id, String name, double latitude, double longitude, 
                        String type, String description, float rating, String imageUrl) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.description = description;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFishType() {
        return fishType;
    }

    public void setFishType(String fishType) {
        this.fishType = fishType;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
