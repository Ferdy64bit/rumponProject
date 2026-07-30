package com.example.java3.domain.model;

import java.util.ArrayList;
import java.util.List;

public class BMKGResponse {
    private String source = "BMKG";
    private String areaName;
    private String updatedAt;
    private List<BMKGForecast> forecasts = new ArrayList<>();

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<BMKGForecast> getForecasts() { return forecasts; }
    public void setForecasts(List<BMKGForecast> forecasts) {
        this.forecasts = forecasts != null ? forecasts : new ArrayList<>();
    }
}
