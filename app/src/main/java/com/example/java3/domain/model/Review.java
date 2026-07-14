package com.example.java3.domain.model;

/**
 * Model representing a Review for a fishing point.
 */
public class Review {
    private String id;
    private String pointId;
    private String userId;
    private String userName;
    private String userPhoto;
    private float rating;
    private String comment;
    private long createdAt;

    public Review() {
    }

    public Review(String id, String pointId, String userId, String userName, 
                  String userPhoto, float rating, String comment, long createdAt) {
        this.id = id;
        this.pointId = pointId;
        this.userId = userId;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPointId() { return pointId; }
    public void setPointId(String pointId) { this.pointId = pointId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhoto() { return userPhoto; }
    public void setUserPhoto(String userPhoto) { this.userPhoto = userPhoto; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
