package com.example.java3.presentation.model;

import com.example.java3.domain.model.Post;

public class CommunityPostUiModel {
    private final String id;
    private final String userName;
    private final String time;
    private final String caption;
    private final int likeCount;
    private final int commentCount;
    private final int avatarResId;
    private final int postImageResId;
    private final String avatarUrl;
    private final String postImageUrl;

    // Catch Info
    private final String fishType;
    private final String locationName;
    private final String weather;
    private final String tide;

    private final boolean isLiked;
    private final boolean isFavorite;
    private final String userId;

    public CommunityPostUiModel(String id, String userName, String time, String caption, int likeCount,
                                int commentCount, int avatarResId, int postImageResId,
                                String avatarUrl, String postImageUrl, String fishType,
                                String locationName, String weather, String tide,
                                boolean isLiked, boolean isFavorite, String userId) {
        this.id = id;
        this.userName = userName;
        this.time = time;
        this.caption = caption;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.avatarResId = avatarResId;
        this.postImageResId = postImageResId;
        this.avatarUrl = avatarUrl;
        this.postImageUrl = postImageUrl;
        this.fishType = fishType;
        this.locationName = locationName;
        this.weather = weather;
        this.tide = tide;
        this.isLiked = isLiked;
        this.isFavorite = isFavorite;
        this.userId = userId;
    }

    // Getters
    public String getId() { return id; }
    public String getUserName() { return userName; }
    public String getTime() { return time; }
    public String getCaption() { return caption; }
    public int getLikeCount() { return likeCount; }
    public int getCommentCount() { return commentCount; }
    public int getAvatarResId() { return avatarResId; }
    public int getPostImageResId() { return postImageResId; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPostImageUrl() { return postImageUrl; }
    public String getFishType() { return fishType; }
    public String getLocationName() { return locationName; }
    public String getWeather() { return weather; }
    public String getTide() { return tide; }
    public boolean isLiked() { return isLiked; }
    public boolean isFavorite() { return isFavorite; }
    public String getUserId() { return userId; }
}
