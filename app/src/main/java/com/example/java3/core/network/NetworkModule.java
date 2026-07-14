package com.example.java3.core.network;

import com.example.java3.core.utils.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkModule {
    private static Retrofit weatherRetrofit;
    private static Retrofit tideRetrofit;

    public static WeatherService getWeatherService() {
        if (weatherRetrofit == null) {
            weatherRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return weatherRetrofit.create(WeatherService.class);
    }

    public static TideService getTideService() {
        if (tideRetrofit == null) {
            tideRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.TIDE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return tideRetrofit.create(TideService.class);
    }
}
