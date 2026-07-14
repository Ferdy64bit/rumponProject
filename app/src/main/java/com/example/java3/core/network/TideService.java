package com.example.java3.core.network;

import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.TideStation;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TideService {
    @GET("api/stations/nearest")
    Call<List<TideStation>> getNearestStation(
        @Header("X-API-Key") String apiKey,
        @Query("lat") double lat,
        @Query("lng") double lng
    );

    @GET("api/station/{id}/tides")
    Call<TideResponse> getTideData(
        @Header("X-API-Key") String apiKey,
        @Path("id") String stationId,
        @Query("days") int days
    );
}
