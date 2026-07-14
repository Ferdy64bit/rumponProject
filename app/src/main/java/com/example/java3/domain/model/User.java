package com.example.java3.domain.model;

/**
 * Model representing a User in the system.
 */
public class User {
    private String uid;
    private String fullName;
    private String email;
    private String photoUrl;
    private String phoneNumber;
    private boolean emailVerified;
    private long createdAt;
    private long updatedAt;
    private String role;
    private int favoriteCount;
    private int totalPosts;
    private boolean profileCompleted;

    public User() {
    }

    public User(String uid, String fullName, String email, String photoUrl, String phoneNumber, 
                boolean emailVerified, long createdAt, long updatedAt, String role, 
                int favoriteCount, int totalPosts, boolean profileCompleted) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.phoneNumber = phoneNumber;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.role = role;
        this.favoriteCount = favoriteCount;
        this.totalPosts = totalPosts;
        this.profileCompleted = profileCompleted;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }

    public int getTotalPosts() { return totalPosts; }
    public void setTotalPosts(int totalPosts) { this.totalPosts = totalPosts; }

    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }
}
