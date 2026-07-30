package com.example.java3.domain.service;

import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.data.remote.MarineHourlyResponse;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.domain.model.RecommendationResult;
import com.example.java3.domain.model.BMKGForecast;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class RecommendationEngineTest {

    @Test
    public void calculate_nullData_returnsSafeRecommendation() {
        RecommendationResult result = RecommendationEngine.calculate(null, null, null, 8.0, 4.0, 70.0);

        assertNotNull(result);
        assertTrue(result.getScore() >= 0.0);
        assertTrue(result.getScore() <= 100.0);
        assertTrue(result.getStars() >= 1 && result.getStars() <= 5);
        assertNotNull(result.getBadgeText());
    }

    @Test
    public void calculate_badWeatherProducesLowerScoreThanClearWeather() {
        WeatherResponse clear = createWeather("Clear", "clear sky", 4.0f, 1012, 10_000);
        WeatherResponse storm = createWeather("Thunderstorm", "heavy thunderstorm", 12.0f, 998, 3_000);

        RecommendationResult clearResult = RecommendationEngine.calculate(null, clear, null, 4.0, 4.5, 70.0);
        RecommendationResult stormResult = RecommendationEngine.calculate(null, storm, null, 4.0, 4.5, 70.0);

        assertTrue(clearResult.getScore() > stormResult.getScore());
        assertTrue(clearResult.getSafetyMultiplier() >= stormResult.getSafetyMultiplier());
    }

    @Test
    public void calculate_userPreferenceAboveRange_isClamped() {
        WeatherResponse weather = createWeather("Clouds", "broken clouds", 4.5f, 1010, 10_000);

        RecommendationResult result = RecommendationEngine.calculate(null, weather, null, 3.0, 4.5, 250.0);

        assertTrue(result.getUserPreferenceScore() <= 100.0);
        assertTrue(result.getScore() <= 100.0);
    }

    @Test
    public void getFishActivityLabel_goodConditions_returnsReadableLabel() {
        WeatherResponse weather = createWeather("Clear", "clear sky", 3.0f, 1012, 10_000);

        String label = RecommendationEngine.getFishActivityLabel(null, weather, null);

        assertNotNull(label);
        assertTrue(label.length() > 0);
    }

    @Test
    public void calculate_moderateWindWithBmkgWarning_staysCukupAmanWhenMarineIsStable() {
        WeatherResponse weather = createWeather("Clouds", "overcast clouds", 4.5f, 1011, 10_000);
        TideResponse tide = createTideWithWarning("Warning gelombang sedang. Perahu kecil waspada.");

        RecommendationResult result = RecommendationEngine.calculate(tide, weather, null, 4.0, 4.0, 70.0);

        assertTrue(result.getSafetyMultiplier() >= 0.80);
        assertTrue(result.getSafetyMultiplier() <= 0.90);
        assertTrue(result.getScore() <= 85.0);
    }

    @Test
    public void calculate_bmkgWindHigh_isMoreConservativeThanOpenWeatherOnly() {
        WeatherResponse weather = createWeather("Clouds", "overcast clouds", 3.0f, 1011, 10_000);
        TideResponse tide = createTideWithWind("16", "40");

        RecommendationResult result = RecommendationEngine.calculate(tide, weather, null, 4.0, 4.0, 70.0);

        assertTrue(result.getSafetyMultiplier() <= 0.85);
        assertTrue(result.getScore() <= 90.0);
    }

    @Test
    public void calculate_fishActivityUsesLiveWindNotBmkgWindLimit() throws Exception {
        WeatherResponse weather = createWeather("Clouds", "scattered clouds", 24.2f / 3.6f, 1008, 10_000);
        TideResponse tide = createTideWithWind("16", "40");
        setSolunarRating(tide, 60);
        MarineHourlyResponse marine = createMarineHourly(0.6f, 0.9f, 29.0f);

        RecommendationResult result = RecommendationEngine.calculate(tide, weather, marine, 4.0, 4.0, 70.0);

        assertTrue("Fish activity should follow live wind scenario, not BMKG max wind cap", result.getActivityScore() >= 60.0);
    }

    @Test
    public void calculate_fishActivityScenario_lowSolunar_returnsLowerActivity() throws Exception {
        RecommendationResult result = calculateFishActivityScenario(20);

        System.out.println("Fish Activity Scenario S20 = " + result.getActivityScore());
        assertTrue(result.getActivityScore() >= 45.0);
        assertTrue(result.getActivityScore() <= 85.0);
    }

    @Test
    public void calculate_fishActivityScenario_mediumSolunar_returnsModerateActivity() throws Exception {
        RecommendationResult result = calculateFishActivityScenario(60);

        System.out.println("Fish Activity Scenario S60 = " + result.getActivityScore());
        assertTrue(result.getActivityScore() >= 60.0);
        assertTrue(result.getActivityScore() <= 95.0);
    }

    @Test
    public void calculate_fishActivityScenario_highSolunar_returnsHighActivity() throws Exception {
        RecommendationResult low = calculateFishActivityScenario(20);
        RecommendationResult medium = calculateFishActivityScenario(60);
        RecommendationResult high = calculateFishActivityScenario(90);

        System.out.println("Fish Activity Scenario S90 = " + high.getActivityScore());
        assertTrue(high.getActivityScore() >= 70.0);
        assertTrue(high.getActivityScore() <= 100.0);
        assertTrue(medium.getActivityScore() > low.getActivityScore());
        assertTrue(high.getActivityScore() > medium.getActivityScore());
    }

    private WeatherResponse createWeather(String main, String description, float windSpeedMs, int pressure, int visibility) {
        WeatherResponse weather = new WeatherResponse();

        WeatherResponse.Main mainBlock = new WeatherResponse.Main();
        mainBlock.setPressure(pressure);
        weather.setMain(mainBlock);

        WeatherResponse.Weather w = new WeatherResponse.Weather();
        w.setMain(main);
        w.setDescription(description);
        List<WeatherResponse.Weather> list = new ArrayList<>();
        list.add(w);
        weather.setWeather(list);

        WeatherResponse.Wind wind = new WeatherResponse.Wind();
        wind.setSpeed(windSpeedMs);
        weather.setWind(wind);

        weather.setVisibility(visibility);
        return weather;
    }

    private TideResponse createTideWithWarning(String warning) {
        TideResponse tide = new TideResponse();
        tide.setSource("BMKG");
        tide.setWarningDesc(warning);
        return tide;
    }

    private TideResponse createTideWithWind(String min, String max) {
        TideResponse tide = new TideResponse();
        tide.setSource("BMKG");
        tide.setWindSpeedMin(min);
        tide.setWindSpeedMax(max);

        BMKGForecast forecast = new BMKGForecast();
        forecast.setWindSpeedMin(min);
        forecast.setWindSpeedMax(max);
        java.util.List<BMKGForecast> list = new java.util.ArrayList<>();
        list.add(forecast);
        tide.setForecasts(list);
        return tide;
    }

    private RecommendationResult calculateFishActivityScenario(int solunarRating) throws Exception {
        WeatherResponse weather = createWeather("Clouds", "scattered clouds", 24.2f / 3.6f, 1008, 10_000);
        TideResponse tide = createTideWithWind("16", "40");
        setSolunarRating(tide, solunarRating);
        MarineHourlyResponse marine = createMarineHourly(0.6f, 0.9f, 29.0f);
        return RecommendationEngine.calculate(tide, weather, marine, 4.0, 4.0, 70.0);
    }

    private MarineHourlyResponse createMarineHourly(float waveHeight, float currentVelocity, float seaTemperature) throws Exception {
        MarineHourlyResponse response = new MarineHourlyResponse();
        MarineHourlyResponse.Hourly hourly = new MarineHourlyResponse.Hourly();
        setField(hourly, "time", Arrays.asList("2026-07-27T00:00"));
        setField(hourly, "waveHeight", Arrays.asList(waveHeight));
        setField(hourly, "oceanCurrentVelocity", Arrays.asList(currentVelocity));
        setField(hourly, "seaSurfaceTemperature", Arrays.asList(seaTemperature));
        setField(response, "hourly", hourly);
        return response;
    }

    private void setSolunarRating(TideResponse tide, int rating) throws Exception {
        TideResponse.DailyCondition condition = new TideResponse.DailyCondition();
        setField(condition, "solunarRating", rating);
        setField(tide, "dailyConditions", Arrays.asList(condition));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
