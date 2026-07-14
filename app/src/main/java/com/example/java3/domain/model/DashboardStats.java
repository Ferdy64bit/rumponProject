package com.example.java3.domain.model;

public class DashboardStats {
    private int spotCount;
    private int favoriteCount;
    private int reviewCount;
    private int postCount;
    private int nearbySpotCount;
    private int unreadNotificationCount;

    public DashboardStats() {
    }

    public DashboardStats(DashboardStats source) {
        this.spotCount = source.spotCount;
        this.favoriteCount = source.favoriteCount;
        this.reviewCount = source.reviewCount;
        this.postCount = source.postCount;
        this.nearbySpotCount = source.nearbySpotCount;
        this.unreadNotificationCount = source.unreadNotificationCount;
    }

    public int getSpotCount() {
        return spotCount;
    }

    public void setSpotCount(int spotCount) {
        this.spotCount = spotCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public int getNearbySpotCount() {
        return nearbySpotCount;
    }

    public void setNearbySpotCount(int nearbySpotCount) {
        this.nearbySpotCount = nearbySpotCount;
    }

    public int getUnreadNotificationCount() {
        return unreadNotificationCount;
    }

    public void setUnreadNotificationCount(int unreadNotificationCount) {
        this.unreadNotificationCount = unreadNotificationCount;
    }
}
