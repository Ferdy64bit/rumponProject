package com.example.java3.presentation.model;

public class ProfileStatsUiModel {
    private final int spotCount;
    private final int postCount;
    private final int likeCount;
    private final int commentCount;
    private final int favoriteCount;
    private final long joinDate;

    public ProfileStatsUiModel(int spotCount, int postCount, int likeCount, int commentCount,
                               int favoriteCount, long joinDate) {
        this.spotCount = spotCount;
        this.postCount = postCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.favoriteCount = favoriteCount;
        this.joinDate = joinDate;
    }

    public int getSpotCount() { return spotCount; }
    public int getPostCount() { return postCount; }
    public int getLikeCount() { return likeCount; }
    public int getCommentCount() { return commentCount; }
    public int getFavoriteCount() { return favoriteCount; }
    public long getJoinDate() { return joinDate; }
}
