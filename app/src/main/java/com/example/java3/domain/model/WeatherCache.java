package com.example.java3.domain.model;

/**
 * Model representing cached weather data.
 */
public class WeatherCache {
    private String id;
    private String data;
    private String source;
    private long updatedAt;
    private long expiresAt;
    private int dataSizeBytes;

    public WeatherCache() {
    }

    public WeatherCache(String id, String data, long updatedAt) {
        this.id = id;
        this.data = data;
        this.source = "OpenWeather";
        this.updatedAt = updatedAt;
        this.expiresAt = updatedAt;
        this.dataSizeBytes = data != null ? data.length() : 0;
    }

    public WeatherCache(String id, String data, String source, long updatedAt, long expiresAt) {
        this.id = id;
        this.data = data;
        this.source = source;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.dataSizeBytes = data != null ? data.length() : 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public int getDataSizeBytes() { return dataSizeBytes; }
    public void setDataSizeBytes(int dataSizeBytes) { this.dataSizeBytes = dataSizeBytes; }
}
