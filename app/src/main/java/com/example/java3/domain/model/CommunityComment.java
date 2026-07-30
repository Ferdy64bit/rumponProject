package com.example.java3.domain.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class CommunityComment {
    private String id;
    private String postId;
    private String userId;
    private String userName;
    private String userProfilePic;
    private String text;

    @ServerTimestamp
    private Date timestamp;

    public CommunityComment() {
    }

    public CommunityComment(String postId, String userId, String userName, String userProfilePic, String text) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userProfilePic = userProfilePic;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfilePic() {
        return userProfilePic;
    }

    public void setUserProfilePic(String userProfilePic) {
        this.userProfilePic = userProfilePic;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
