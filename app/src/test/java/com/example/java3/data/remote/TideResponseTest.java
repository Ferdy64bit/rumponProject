package com.example.java3.data.remote;

import com.example.java3.domain.model.BMKGForecast;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TideResponseTest {

    @Test
    public void bmkgForecast_warningAndRecommendation_areReadable() {
        BMKGForecast forecast = new BMKGForecast();
        forecast.setWeatherDesc("Cerah Berawan");
        forecast.setWaveCategory("Gelombang rendah");
        forecast.setWaveDescription("0.5-1.0 m");
        forecast.setWindFrom("Timur");
        forecast.setWindTo("Tenggara");
        forecast.setWindSpeedMin("5 knot");
        forecast.setWindSpeedMax("10 knot");

        assertEquals("Cerah Berawan", forecast.getWeatherLabel());
        assertTrue(forecast.getWaveLabel().toLowerCase().contains("rendah"));
        assertTrue(forecast.getWindLabel().toLowerCase().contains("angin"));
        assertFalse(forecast.hasWarning());
        assertTrue(forecast.getRecommendationScore() >= 0);
    }

    @Test
    public void tideResponse_bmkgForecast_returnsBMKGCompatibleLabels() {
        BMKGForecast forecast = new BMKGForecast();
        forecast.setTimeDesc("Pagi - Siang");
        forecast.setWeatherDesc("Cerah");
        forecast.setWaveCategory("Rendah");
        forecast.setWaveDescription("0.3-0.8 m");
        forecast.setWindFrom("Utara");
        forecast.setWindTo("Barat Laut");
        forecast.setWindSpeedMin("5");
        forecast.setWindSpeedMax("10");

        TideResponse tide = new TideResponse();
        tide.setSource("BMKG");
        tide.setAreaName("F.08_Perairan Kep. Seribu");
        tide.setForecastTime("2026-07-26");
        List<BMKGForecast> forecasts = new ArrayList<>();
        forecasts.add(forecast);
        tide.setForecasts(forecasts);

        assertTrue(tide.getHighTide().toLowerCase().contains("rendah") || tide.getHighTide().toLowerCase().contains("gelombang"));
        assertTrue(tide.getLowTide().toLowerCase().contains("angin"));
        assertTrue(tide.getMarineSummary().length() > 0);
        assertTrue(tide.getBestFishingWindow().length() > 0);
    }
}
