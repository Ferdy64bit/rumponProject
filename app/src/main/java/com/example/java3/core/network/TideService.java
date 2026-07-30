package com.example.java3.core.network;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TideService {
    @GET("public_api/perairan/{area}.json")
    Call<JsonObject> getMarineForecast(
        @Path(value = "area", encoded = true) String area
    );

    @GET("public_api/static/wilayah_perairan.json")
    Call<JsonObject> getMarineAreaPolygons();

    @GET("public_api/overview/gelombang.json")
    Call<JsonObject> getWaveOverview();
}
