package com.example.java3.domain.service;

import android.graphics.Color;

import com.example.java3.data.remote.MarineHourlyResponse;
import com.example.java3.data.remote.TideResponse;
import com.example.java3.data.remote.WeatherResponse;
import com.example.java3.domain.model.RecommendationResult;

import java.util.Calendar;
import java.util.Locale;

/**
 * Central fishing recommendation engine.
 *
 * Current-day score is based on live OpenWeather + Open-Meteo Marine data.
 * BMKG is intentionally kept as an official forecast/warning layer in the UI,
 * not as the primary numerical recommendation source.
 */
public class RecommendationEngine {
    private static final long SYNODIC_MONTH_MILLIS = (long) (29.53058867d * 24d * 60d * 60d * 1000d);
    private static final long KNOWN_NEW_MOON_UTC = 947182440000L; // 2000-01-06 18:14:00 UTC
    private static final double DISTANCE_WEIGHT = 0.20;
    private static final double WEATHER_WEIGHT = 0.20;
    private static final double MARINE_WEIGHT = 0.25;
    private static final double ACTIVITY_WEIGHT = 0.15;
    private static final double SPOT_WEIGHT = 0.10;
    private static final double USER_WEIGHT = 0.10;

    public static RecommendationResult calculate(TideResponse tide, WeatherResponse weather, double distance, double rating) {
        return calculate(tide, weather, null, distance, rating, 70.0);
    }

    public static RecommendationResult calculate(TideResponse tide, WeatherResponse weather, MarineHourlyResponse marineHourly, double distance, double rating) {
        return calculate(tide, weather, marineHourly, distance, rating, 70.0);
    }

    public static RecommendationResult calculate(TideResponse tide, WeatherResponse weather, double distance, double rating, double userPreferenceScore) {
        return calculate(tide, weather, null, distance, rating, userPreferenceScore);
    }

    public static RecommendationResult calculate(TideResponse tide, WeatherResponse weather, MarineHourlyResponse marineHourly,
                                                 double distance, double rating, double userPreferenceScore) {
        double distanceScore = getDistanceScore(distance);
        double weatherScore = getWeatherScore(weather);
        double marineScore = getMarineScore(marineHourly);
        double activityScore = getFishActivityScore(tide, weather, marineHourly);
        double spotQualityScore = getSpotQualityScore((float) rating);
        double preferenceScore = clamp(userPreferenceScore, 0, 100);

        double baseScore = (distanceScore * DISTANCE_WEIGHT)
                + (weatherScore * WEATHER_WEIGHT)
                + (marineScore * MARINE_WEIGHT)
                + (activityScore * ACTIVITY_WEIGHT)
                + (spotQualityScore * SPOT_WEIGHT)
                + (preferenceScore * USER_WEIGHT);
        baseScore = clamp(baseScore + getLiveConditionBonus(weather, marineHourly, weatherScore, marineScore, activityScore), 0, 100);

        double safetyMultiplier = getSafetyMultiplier(tide, weather, marineHourly);
        double recommendationSafetyFactor = getRecommendationSafetyFactor(safetyMultiplier);
        double finalScore = clamp(Math.min(baseScore * recommendationSafetyFactor, getDistanceScoreCap(distance)), 0, 100);

        String badgeText;
        int color;
        int stars;

        if (finalScore >= 85) {
            badgeText = "Sangat Direkomendasikan";
            color = Color.parseColor("#22C55E");
            stars = 5;
        } else if (finalScore >= 70) {
            badgeText = "Direkomendasikan";
            color = Color.parseColor("#4A90E2");
            stars = 4;
        } else if (finalScore >= 55) {
            badgeText = "Cukup Layak";
            color = Color.parseColor("#EAB308");
            stars = 3;
        } else if (finalScore >= 40) {
            badgeText = "Perlu Waspada";
            color = Color.parseColor("#F97316");
            stars = 2;
        } else {
            badgeText = "Tidak Direkomendasikan";
            color = Color.parseColor("#EF4444");
            stars = 1;
        }

        return new RecommendationResult(finalScore, badgeText, color, stars,
                distanceScore, weatherScore, marineScore, spotQualityScore,
                activityScore, preferenceScore, safetyMultiplier);
    }

    public static String getFishActivityLabel(TideResponse tide, WeatherResponse weather, MarineHourlyResponse marineHourly) {
        double score = getFishActivityScore(tide, weather, marineHourly);
        if (score >= 85) return "Sangat Tinggi";
        if (score >= 70) return "Tinggi";
        if (score >= 55) return "Sedang";
        if (score >= 40) return "Rendah";
        return "Sangat Rendah";
    }

    public static String getFishActivityLabel(WeatherResponse weather, MarineHourlyResponse marineHourly) {
        return getFishActivityLabel(null, weather, marineHourly);
    }

    private static double getMarineScore(MarineHourlyResponse marineHourly) {
        if (marineHourly == null) return 55;
        double currentWave = scoreWaveMeters(marineHourly.getCurrentWaveHeight());
        double next24Wave = scoreWaveMeters(marineHourly.getTodayMaxWaveHeight());
        double stability = getWaveStabilityScore(marineHourly);
        double currentSafety = getCurrentVelocityScore(marineHourly.getCurrentOceanCurrentVelocity());
        return clamp((currentWave * 0.40) + (next24Wave * 0.25) + (stability * 0.20) + (currentSafety * 0.15), 0, 100);
    }

    private static double getWaveStabilityScore(MarineHourlyResponse marineHourly) {
        if (marineHourly == null) return 60;
        float max = marineHourly.getTodayMaxWaveHeight();
        float avg = marineHourly.getTodayAverageWaveHeight();
        if (max <= 0f || avg <= 0f) return 60;
        double spread = max - avg;
        if (spread <= 0.15) return 100;
        if (spread <= 0.35) return 85;
        if (spread <= 0.75) return 65;
        if (spread <= 1.25) return 45;
        return 25;
    }

    private static double scoreWaveMeters(double meters) {
        if (meters <= 0) return 55;
        if (meters <= 0.5) return 100;
        if (meters <= 1.25) return 85;
        if (meters <= 2.5) return 55;
        if (meters <= 4.0) return 25;
        return 5;
    }

    private static double getWeatherScore(WeatherResponse weather) {
        if (weather == null || weather.getWeather() == null || weather.getWeather().isEmpty()) return 55;

        double score;
        String main = safeLower(weather.getWeather().get(0).getMain());
        String description = safeLower(weather.getWeather().get(0).getDescription());

        if (main.contains("clear")) score = 100;
        else if (main.contains("cloud")) score = 88;
        else if (main.contains("mist") || main.contains("fog") || main.contains("haze")) score = 70;
        else if (main.contains("drizzle")) score = 55;
        else if (main.contains("rain")) score = 35;
        else if (main.contains("thunderstorm")) score = 10;
        else score = 60;

        double windKmH = getWeatherWindKmH(weather);
        if (windKmH > 30) score -= 30;
        else if (windKmH > 20) score -= 15;

        if (description.contains("heavy") || description.contains("lebat")) score -= 25;
        if (description.contains("storm") || description.contains("petir")) score -= 40;
        if (weather.getVisibility() > 0 && weather.getVisibility() < 5000) score -= 10;

        return clamp(score, 0, 100);
    }

    private static double getLiveConditionBonus(WeatherResponse weather, MarineHourlyResponse marineHourly,
                                                double weatherScore, double marineScore, double activityScore) {
        double bonus = 0;
        if (weatherScore >= 80) bonus += 3.0;
        else if (weatherScore >= 70) bonus += 2.0;

        if (marineScore >= 80) bonus += 3.0;
        else if (marineScore >= 70) bonus += 2.0;

        if (activityScore >= 60) bonus += 2.0;

        double windKmH = getWeatherWindKmH(weather);
        if (windKmH > 0 && windKmH <= 16) bonus += 1.5;
        if (marineHourly != null && marineHourly.getCurrentWaveHeight() > 0f && marineHourly.getCurrentWaveHeight() <= 0.6f) {
            bonus += 1.5;
        }

        if (getSevereWeatherPenalty(weather) > 0) bonus -= 3.0;
        return clamp(bonus, 0, 8.0);
    }

    private static double getFishActivityScore(TideResponse tide, WeatherResponse weather, MarineHourlyResponse marineHourly) {
        double solunarScore = getSolunarScore(tide, weather);
        double weatherScore = getFishWeatherScore(weather);
        double pressureScore = getFishPressureScore(weather);
        double waterMovementScore = getFishWaterMovementScore(marineHourly);

        double activity = (solunarScore * 0.35)
                + (weatherScore * 0.15)
                + (pressureScore * 0.25)
                + (waterMovementScore * 0.25);

        activity += getFishDawnDuskBonus();
        activity += getStableConditionActivityBonus(weather, marineHourly, weatherScore, pressureScore, waterMovementScore);
        activity -= getFishWindPenalty(weather);
//        activity -= getNightPenalty();
        activity -= getSevereWeatherPenalty(weather);

        return clamp(activity, 0, 100);
    }

    private static double getSolunarScore(TideResponse tide, WeatherResponse weather) {
        if (tide != null && tide.getDailyConditions() != null && !tide.getDailyConditions().isEmpty()) {
            int tideRating = tide.getDailyConditions().get(0).getSolunarRating();
            if (tideRating > 0) {
                return clamp(tideRating, 0, 100);
            }
        }

        double solarScore = getSolarWindowScore(weather);
        double lunarScore = getMoonPhaseScore();
        double blended = (solarScore * 0.60) + (lunarScore * 0.40);
        return clamp(blended, 0, 100);
    }

    private static double getSolarWindowScore(WeatherResponse weather) {
        long now = System.currentTimeMillis();
        if (weather != null && weather.getSys() != null) {
            long sunrise = weather.getSys().getSunrise() * 1000L;
            long sunset = weather.getSys().getSunset() * 1000L;
            long sunriseDiff = Math.abs(now - sunrise);
            long sunsetDiff = Math.abs(now - sunset);
            long nearest = Math.min(sunriseDiff, sunsetDiff);

            if (nearest <= 30 * 60 * 1000L) return 100;
            if (nearest <= 60 * 60 * 1000L) return 90;
            if (nearest <= 90 * 60 * 1000L) return 80;
            if (nearest <= 2 * 60 * 60 * 1000L) return 65;

            if (now >= sunrise && now <= sunset) return 55;
            return 42;
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour <= 7) return 90;
        if (hour >= 16 && hour <= 18) return 85;
        if (hour >= 8 && hour <= 15) return 55;
        return 42;
    }

    private static double getMoonPhaseScore() {
        long now = System.currentTimeMillis();
        double phase = ((double) (now - KNOWN_NEW_MOON_UTC) / (double) SYNODIC_MONTH_MILLIS) % 1d;
        if (phase < 0) phase += 1d;

        double distanceToNew = Math.min(phase, 1d - phase);
        double distanceToFull = Math.abs(phase - 0.5d);
        double nearestPeak = Math.min(distanceToNew, distanceToFull);

        if (nearestPeak <= 0.08d) return 95;
        if (nearestPeak <= 0.15d) return 82;
        if (nearestPeak <= 0.22d) return 68;
        if (nearestPeak <= 0.30d) return 58;
        return 50;
    }

//    private static double getNightPenalty() {
//        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//        return hour >= 19 || hour < 4 ? 15 : 0;
//    }

    private static double getFishDawnDuskBonus() {
        Calendar now = Calendar.getInstance();
        int totalMinutes = (now.get(Calendar.HOUR_OF_DAY) * 60) + now.get(Calendar.MINUTE);
        boolean dawn = totalMinutes >= (4 * 60 + 30) && totalMinutes <= (6 * 60);
        boolean dusk = totalMinutes >= (17 * 60) && totalMinutes <= (18 * 60 + 30);
        return dawn || dusk ? 15 : 0;
    }

    private static double getStableConditionActivityBonus(WeatherResponse weather, MarineHourlyResponse marineHourly,
                                                          double weatherScore, double pressureScore, double waterMovementScore) {
        boolean stableWeather = weatherScore >= 70 && pressureScore >= 65;
        boolean stableMarine = marineHourly != null
                && marineHourly.getCurrentSeaSurfaceTemperature() >= 27f
                && marineHourly.getCurrentSeaSurfaceTemperature() <= 31f
                && waterMovementScore >= 50;
        return stableWeather && stableMarine && getSevereWeatherPenalty(weather) == 0 ? 10 : 0;
    }

    private static double getFishWindPenalty(WeatherResponse weather) {
        double windKmH = getWeatherWindKmH(weather);
        if (windKmH > 35) return 40;
        if (windKmH >= 26) return 20;
        if (windKmH >= 15) return 10;
        return 0;
    }

    private static double getRecommendationSafetyFactor(double safetyMultiplier) {
        double safety = clamp(safetyMultiplier, 0, 1);
        if (safety >= 0.75) {
            return 0.94 + ((safety - 0.75) / 0.25 * 0.06);
        }
        if (safety >= 0.55) {
            return 0.82 + ((safety - 0.55) / 0.20 * 0.10);
        }
        if (safety >= 0.40) {
            return 0.64 + ((safety - 0.40) / 0.15 * 0.12);
        }
        return safety;
    }

    private static double getSevereWeatherPenalty(WeatherResponse weather) {
        if (weather == null || weather.getWeather() == null || weather.getWeather().isEmpty()) return 0;
        String main = safeLower(weather.getWeather().get(0).getMain());
        String desc = safeLower(weather.getWeather().get(0).getDescription());
        if (main.contains("thunderstorm") || desc.contains("storm") || desc.contains("petir")) return 30;
        if (desc.contains("heavy") || desc.contains("lebat")) return 18;
        if (main.contains("rain")) return 12;
        if (main.contains("drizzle")) return 6;
        return 0;
    }

    private static double getFishWaterMovementScore(MarineHourlyResponse marineHourly) {
        if (marineHourly == null) return 60;
        double wave = marineHourly.getCurrentWaveHeight();
        double current = marineHourly.getCurrentOceanCurrentVelocity();
        if (current >= 0.7 && current <= 1.2 && wave >= 0.4 && wave <= 0.7) return 95;
        if (current > 0 && current <= 1.5 && wave > 0 && wave <= 1.25) return 80;
        if (current <= 0.2 && wave <= 0.2) return 40;
        if (current > 1.8 || wave > 2.5) return 35;
        return 65;
    }

    private static double getFishPressureScore(WeatherResponse weather) {
        if (weather == null || weather.getMain() == null) return 60;
        int pressure = weather.getMain().getPressure();
        if (pressure >= 1006 && pressure <= 1009) return 90;
        if (pressure == 1010) return 82;
        if (pressure >= 1011 && pressure <= 1014) return 75;
        if (pressure >= 1000 && pressure <= 1025) return 60;
        return 45;
    }

    private static double getFishWeatherScore(WeatherResponse weather) {
        if (weather == null || weather.getWeather() == null || weather.getWeather().isEmpty()) return 60;
        String main = safeLower(weather.getWeather().get(0).getMain());
        String description = safeLower(weather.getWeather().get(0).getDescription());
        boolean lowLight = isDawnOrDuskNow();

        if (main.contains("thunderstorm") || description.contains("storm") || description.contains("petir")) return 10;
        if (description.contains("heavy") || description.contains("lebat")) return 30;
        if (main.contains("rain")) return description.contains("light") || description.contains("ringan") ? 85 : 45;
        if (main.contains("drizzle")) return 85;
        if (main.contains("cloud") || description.contains("scattered") || description.contains("overcast")) return 88;
        if (main.contains("clear")) return lowLight ? 75 : 55;
        if (main.contains("mist") || main.contains("fog") || main.contains("haze")) return 65;
        return 60;
    }

    private static boolean isDawnOrDuskNow() {
        Calendar now = Calendar.getInstance();
        int totalMinutes = (now.get(Calendar.HOUR_OF_DAY) * 60) + now.get(Calendar.MINUTE);
        return (totalMinutes >= (4 * 60 + 30) && totalMinutes <= (6 * 60))
                || (totalMinutes >= (17 * 60) && totalMinutes <= (18 * 60 + 30));
    }

    private static double getSafetyMultiplier(TideResponse tide, WeatherResponse weather, MarineHourlyResponse marineHourly) {
        double weatherSafety = getWeatherSafety(weather);
        double windKmH = getEffectiveSafetyWindKmH(weather, tide, marineHourly);
        double windSafety = getWindSafety(windKmH);
        double waveSafety = getWaveSafety(marineHourly != null ? marineHourly.getTodayMaxWaveHeight() : 0);
        double currentSafety = getCurrentSafety(marineHourly != null ? marineHourly.getCurrentOceanCurrentVelocity() : 0);
        double bmkgSafety = getBmkgWarningSafety(tide);
        double liveSafety = Math.min(weatherSafety, Math.min(windSafety, Math.min(waveSafety, currentSafety)));
        double bmkgAdjustment = 1.0 - ((1.0 - bmkgSafety) * 0.10);
        if (hasExtremeBmkgWarning(tide)) {
            bmkgAdjustment = Math.min(bmkgAdjustment, 0.90);
        }
        double baseSafety = liveSafety * bmkgAdjustment;
        return clamp(baseSafety - getCombinationPenalty(weather, windKmH, marineHourly), 0, 1);
    }

    private static double getWeatherSafety(WeatherResponse weather) {
        if (weather == null || weather.getWeather() == null || weather.getWeather().isEmpty()) return 1.00;
        String main = safeLower(weather.getWeather().get(0).getMain());
        String desc = safeLower(weather.getWeather().get(0).getDescription());
        if (main.contains("thunderstorm") || desc.contains("storm") || desc.contains("petir")) return 0.35;
        if (desc.contains("heavy") || desc.contains("lebat")) return 0.50;
        if (main.contains("rain")) return 0.70;
        if (main.contains("drizzle")) return 0.85;
        return 1.00;
    }

    private static double getWaveSafety(double meters) {
        if (meters <= 0) return 1.00;
        if (meters <= 0.5) return 1.00;
        if (meters <= 1.25) return 0.95;
        if (meters <= 2.5) return 0.75;
        if (meters <= 4.0) return 0.55;
        return 0.35;
    }

    private static double getWindSafety(double windKmH) {
        if (windKmH <= 8) return 1.00;
        if (windKmH <= 14) return 0.95;
        if (windKmH <= 19) return 0.85;
        if (windKmH <= 28) return 0.70;
        if (windKmH <= 38) return 0.55;
        if (windKmH <= 49) return 0.40;
        return 0.25;
    }

    private static double getCurrentSafety(double currentMs) {
        if (currentMs <= 0) return 1.00;
        if (currentMs <= 0.5) return 1.00;
        if (currentMs <= 1.2) return 0.90;
        if (currentMs <= 1.5) return 0.75;
        if (currentMs <= 2.0) return 0.55;
        return 0.35;
    }

    private static double getCurrentVelocityScore(double currentMs) {
        if (currentMs <= 0) return 70;
        if (currentMs <= 0.5) return 100;
        if (currentMs <= 1.0) return 85;
        if (currentMs <= 1.5) return 60;
        if (currentMs <= 2.0) return 35;
        return 10;
    }

    private static double getWeatherWindKmH(WeatherResponse weather) {
        return weather != null && weather.getWind() != null ? weather.getWind().getSpeed() * 3.6 : 0;
    }

    private static double getEffectiveSafetyWindKmH(WeatherResponse weather, TideResponse tide, MarineHourlyResponse marineHourly) {
        double liveWind = getWeatherWindKmH(weather);
        double bmkgWind = getBmkgWindKmH(tide);
        if (liveWind <= 0) return bmkgWind;
        if (bmkgWind <= liveWind) return liveWind;

        double maxWave = marineHourly != null ? marineHourly.getTodayMaxWaveHeight() : 0;
        double current = marineHourly != null ? marineHourly.getCurrentOceanCurrentVelocity() : 0;
        boolean seriousWarning = hasSeriousBmkgWarning(tide);
        boolean unstableMarine = maxWave >= 1.25 || current >= 1.2;

        if (seriousWarning || unstableMarine || liveWind >= 19) {
            return bmkgWind;
        }

        if (tide != null && tide.hasWarning()) {
            return Math.max(liveWind, Math.min(bmkgWind, liveWind + 8));
        }

        return Math.max(liveWind, Math.min(bmkgWind, liveWind + 5));
    }

    private static double getBmkgWindKmH(TideResponse tide) {
        if (tide == null) return 0;
        double maxWind = Math.max(parseWindSpeed(tide.getWindSpeedMax()), parseWindSpeed(tide.getWindSpeedMin()));
        if (maxWind > 0) return maxWind;
        if (tide.getForecasts() == null || tide.getForecasts().isEmpty()) return 0;
        double forecastMax = 0;
        for (com.example.java3.domain.model.BMKGForecast forecast : tide.getForecasts()) {
            if (forecast == null) continue;
            forecastMax = Math.max(forecastMax, parseWindSpeed(forecast.getWindSpeedMax()));
            forecastMax = Math.max(forecastMax, parseWindSpeed(forecast.getWindSpeedMin()));
        }
        return forecastMax;
    }

    private static double parseWindSpeed(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+(?:[.,]\\d+)?)").matcher(value);
        double max = 0;
        while (matcher.find()) {
            try {
                max = Math.max(max, Double.parseDouble(matcher.group(1).replace(',', '.')));
            } catch (NumberFormatException ignored) {
            }
        }
        if (max <= 0) return 0;
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.contains("km") || lower.contains("km/h") || lower.contains("kmj")) return max;
        return max * 1.852;
    }

    private static double getBmkgWarningSafety(TideResponse tide) {
        if (tide == null || !tide.hasWarning()) return 1.00;
        String warning = safeLower(tide.getWarningDesc());
        if (warning.contains("ekstrem") || warning.contains("sangat tinggi") || warning.contains("bahaya")) return 0.50;
        if (warning.contains("tinggi") || warning.contains("badai") || warning.contains("petir")) return 0.75;
        return 0.90;
    }

    private static boolean hasExtremeBmkgWarning(TideResponse tide) {
        if (tide == null || !tide.hasWarning()) return false;
        String warning = safeLower(tide.getWarningDesc());
        return warning.contains("ekstrem")
                || warning.contains("sangat tinggi")
                || warning.contains("bahaya")
                || warning.contains("badai")
                || warning.contains("petir");
    }

    private static boolean hasSeriousBmkgWarning(TideResponse tide) {
        if (tide == null || !tide.hasWarning()) return false;
        String warning = safeLower(tide.getWarningDesc());
        return warning.contains("ekstrem")
                || warning.contains("sangat tinggi")
                || warning.contains("bahaya")
                || warning.contains("badai")
                || warning.contains("petir");
    }

    private static double getCombinationPenalty(WeatherResponse weather, double windKmH, MarineHourlyResponse marineHourly) {
        double penalty = 0;
        double maxWave = marineHourly != null ? marineHourly.getTodayMaxWaveHeight() : 0;
        double current = marineHourly != null ? marineHourly.getCurrentOceanCurrentVelocity() : 0;

        if (windKmH >= 16 && maxWave >= 1.0) penalty += 0.10;
        if (windKmH >= 20 && maxWave >= 1.25) penalty += 0.05;
        if (windKmH >= 20 && current >= 0.8) penalty += 0.10;
        if (windKmH >= 16 && isRainy(weather)) penalty += 0.10;
        return Math.min(penalty, 0.30);
    }

    private static boolean isRainy(WeatherResponse weather) {
        if (weather == null || weather.getWeather() == null || weather.getWeather().isEmpty()) return false;
        String main = safeLower(weather.getWeather().get(0).getMain());
        String desc = safeLower(weather.getWeather().get(0).getDescription());
        return main.contains("rain") || main.contains("drizzle") || desc.contains("hujan") || desc.contains("gerimis");
    }

    private static double getDistanceScore(double distance) {
        if (distance < 0) return 50;
        if (distance <= 2) return 100;
        if (distance <= 5) return 90;
        if (distance <= 10) return 75;
        if (distance <= 20) return 60;
        if (distance <= 50) return 40;
        return 20;
    }

    private static double getDistanceScoreCap(double distance) {
        if (distance < 0) return 70;
        if (distance <= 50) return 100;
        if (distance <= 100) return 70;
        if (distance <= 500) return 55;
        if (distance <= 1000) return 45;
        if (distance <= 10000) return 40;
        return 35;
    }

    private static double getSpotQualityScore(float rating) {
        if (rating >= 5.0) return 100;
        if (rating >= 4.5) return 90;
        if (rating >= 4.0) return 80;
        if (rating >= 3.5) return 70;
        if (rating >= 3.0) return 60;
        return 45;
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
