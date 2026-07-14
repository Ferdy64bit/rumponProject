package com.example.java3.presentation.model;

public class CommunityPostUiModel {
    private final String userName;
    private final String time;
    private final String caption;
    private final int likeCount;
    private final int commentCount;
    private final int avatarResId;
    private final int postImageResId;
    private final String avatarUrl;
    private final String postImageUrl;

    public CommunityPostUiModel(String userName, String time, String caption, int likeCount, int commentCount, int avatarResId, int postImageResId) {
        this(userName, time, caption, likeCount, commentCount, avatarResId, postImageResId, "", "");
    }

    public CommunityPostUiModel(String userName, String time, String caption, int likeCount, int commentCount,
                                int avatarResId, int postImageResId, String avatarUrl, String postImageUrl) {
        this.userName = userName;
        this.time = time;
        this.caption = caption;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.avatarResId = avatarResId;
        this.postImageResId = postImageResId;
        this.avatarUrl = avatarUrl;
        this.postImageUrl = postImageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getTime() {
        return time;
    }

    public String getCaption() {
        return caption;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public int getPostImageResId() {
        return postImageResId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }
}
