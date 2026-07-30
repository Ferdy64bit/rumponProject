package com.example.java3.domain.model;

public class BMKGCache extends TideCache {
    public BMKGCache() {
        super();
    }

    public BMKGCache(String id, String data, String area, long updatedAt, long expiresAt) {
        super(id, data, "BMKG", area, updatedAt, expiresAt);
    }
}
