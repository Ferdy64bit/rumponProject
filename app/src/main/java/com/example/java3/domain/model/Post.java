package com.example.java3.domain.model;

/**
 * Model representing a Social Post in the community.
 * Follows strict OOP principles with private fields and public accessors.
 */
public class Post {
    private String id;
    private String userId;
    private String userName;
    private String userProfilePic;
    private String imageUrl;
    private String caption;
    private String locationName;
    private long timestamp;
    private int likesCount;
    private int commentsCount;

    /**
     * Empty constructor required for Firebase Firestore serialization.
     */
    public Post() {
    }

    public Post(String id, String userId, String userName, String userProfilePic, 
                String imageUrl, String caption, String locationName, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userProfilePic = userProfilePic;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.locationName = locationName;
        this.timestamp = timestamp;
        this.likesCount = 0;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }
}
