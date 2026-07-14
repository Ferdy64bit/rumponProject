package com.example.java3.domain.model;

/**
 * Model representing a Notification for a user.
 */
public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type; // e.g., "social", "system", "weather"
    private boolean isRead;
    private long createdAt;

    public Notification() {
    }

    public Notification(String id, String userId, String title, String message, 
                        String type, boolean isRead, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
