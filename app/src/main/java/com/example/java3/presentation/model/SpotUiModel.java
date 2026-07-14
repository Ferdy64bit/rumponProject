package com.example.java3.presentation.model;

public class SpotUiModel {
    private final String name;
    private final double distance;
    private final float rating;
    private final int reviewCount;
    private final int badgeColor;
    private final int imageResId;
    private final boolean favorite;
    private final String imageUrl;
    private final String searchText;

    public SpotUiModel(String name, double distance, float rating, int reviewCount, int badgeColor, int imageResId, boolean favorite) {
        this(name, distance, rating, reviewCount, badgeColor, imageResId, favorite, "", name);
    }

    public SpotUiModel(String name, double distance, float rating, int reviewCount, int badgeColor, int imageResId,
                       boolean favorite, String imageUrl, String searchText) {
        this.name = name;
        this.distance = distance;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.badgeColor = badgeColor;
        this.imageResId = imageResId;
        this.favorite = favorite;
        this.imageUrl = imageUrl;
        this.searchText = searchText;
    }

    public String getName() {
        return name;
    }

    public double getDistance() {
        return distance;
    }

    public float getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public int getBadgeColor() {
        return badgeColor;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSearchText() {
        return searchText;
    }
}
