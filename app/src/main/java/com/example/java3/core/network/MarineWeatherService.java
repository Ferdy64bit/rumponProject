package com.example.java3.core.network;

import com.example.java3.data.remote.MarineHourlyResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MarineWeatherService {
    @GET("v1/marine")
    Call<MarineHourlyResponse> getHourlyMarineForecast(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("hourly") String hourly,
            @Query("timezone") String timezone,
            @Query("forecast_days") int forecastDays
    );
}
