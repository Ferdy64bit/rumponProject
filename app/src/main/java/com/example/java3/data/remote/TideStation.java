package com.example.java3.data.remote;

import com.google.gson.annotations.SerializedName;

public class TideStation {
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("distanceKm")
    private double distanceKm;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;

    public String getId() { return id; }
    public String getName() { return name; }
    public double getDistanceKm() { return distanceKm; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}
