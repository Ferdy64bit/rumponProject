package com.example.java3.domain.model;

/**
 * Model representing cached weather data.
 */
public class WeatherCache {
    private String id; // usually city name or coordinate string
    private String data; // JSON string of the weather response
    private long updatedAt;

    public WeatherCache() {
    }

    public WeatherCache(String id, String data, long updatedAt) {
        this.id = id;
        this.data = data;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
