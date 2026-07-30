package com.example.java3.presentation.model;

public class SpotUiModel {
    private final String id;
    private final String name;
    private final double distance;
    private final float rating;
    private final int reviewCount;
    private final int badgeColor;
    private final int imageResId;
    private final boolean favorite;
    private final String imageUrl;
    private final String searchText;
    private final String type;
    private final String description;
    private final double latitude;
    private final double longitude;
    private final int recommendationScore;
    private final int recommendationStars;
    private final String recommendationLabel;
    private final String ownerId;
    private final String ownerName;
    private final String ownerPhoto;
    private final String visibility;
    private final long createdAt;

    public SpotUiModel(String name, double distance, float rating, int reviewCount, int badgeColor, int imageResId, boolean favorite) {
        this("", name, distance, rating, reviewCount, badgeColor, imageResId, favorite, "", name, "Spot", "", 0.0, 0.0, 0, 1, "Belum dihitung", "", "", "", "PUBLIC", 0L);
    }

    public SpotUiModel(String name, double distance, float rating, int reviewCount, int badgeColor, int imageResId,
                       boolean favorite, String imageUrl, String searchText) {
        this("", name, distance, rating, reviewCount, badgeColor, imageResId, favorite, imageUrl, searchText, "Spot", "", 0.0, 0.0, 0, 1, "Belum dihitung", "", "", "", "PUBLIC", 0L);
    }

    public SpotUiModel(String id, String name, double distance, float rating, int reviewCount, int badgeColor, int imageResId,
                       boolean favorite, String imageUrl, String searchText, String type, String description,
                       double latitude, double longitude, int recommendationScore, int recommendationStars,
                       String recommendationLabel, String ownerId, String ownerName, String ownerPhoto,
                       String visibility, long createdAt) {
        this.id = id;
        this.name = name;
        this.distance = distance;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.badgeColor = badgeColor;
        this.imageResId = imageResId;
        this.favorite = favorite;
        this.imageUrl = imageUrl;
        this.searchText = searchText;
        this.type = type;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recommendationScore = recommendationScore;
        this.recommendationStars = recommendationStars;
        this.recommendationLabel = recommendationLabel;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerPhoto = ownerPhoto;
        this.visibility = visibility;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getDistance() { return distance; }
    public float getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public int getBadgeColor() { return badgeColor; }
    public int getImageResId() { return imageResId; }
    public boolean isFavorite() { return favorite; }
    public String getImageUrl() { return imageUrl; }
    public String getSearchText() { return searchText; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getRecommendationScore() { return recommendationScore; }
    public int getRecommendationStars() { return recommendationStars; }
    public String getRecommendationLabel() { return recommendationLabel; }
    public String getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerPhoto() { return ownerPhoto; }
    public String getVisibility() { return visibility; }
    public long getCreatedAt() { return createdAt; }
}