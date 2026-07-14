package com.example.java3.domain.model;

/**
 * Model representing a Favorite fishing point for a user.
 */
public class Favorite {
    private String id;
    private String userId;
    private String pointId;
    private long createdAt;

    public Favorite() {
    }

    public Favorite(String id, String userId, String pointId, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.pointId = pointId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPointId() { return pointId; }
    public void setPointId(String pointId) { this.pointId = pointId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
