package com.example.java3.domain.model;

public class UserProfileSummary {
    private final String uid;
    private final String displayName;
    private final String email;
    private final String photoUrl;
    private final String role;

    public UserProfileSummary(String uid, String displayName, String email, String photoUrl, String role) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getRole() {
        return role;
    }
}
