package com.example.java3.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("main")
    private Main main;
    @SerializedName("weather")
    private List<Weather> weather;
    @SerializedName("wind")
    private Wind wind;
    @SerializedName("visibility")
    private int visibility;
    @SerializedName("name")
    private String name;
    @SerializedName("sys")
    private Sys sys;

    public Main getMain() { return main; }
    public void setMain(Main main) { this.main = main; }

    public List<Weather> getWeather() { return weather; }
    public void setWeather(List<Weather> weather) { this.weather = weather; }

    public Wind getWind() { return wind; }
    public void setWind(Wind wind) { this.wind = wind; }

    public int getVisibility() { return visibility; }
    public void setVisibility(int visibility) { this.visibility = visibility; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Sys getSys() { return sys; }
    public void setSys(Sys sys) { this.sys = sys; }

    public static class Main {
        @SerializedName("temp")
        private float temp;
        @SerializedName("feels_like")
        private float feelsLike;
        @SerializedName("humidity")
        private int humidity;
        @SerializedName("pressure")
        private int pressure;

        public float getTemp() { return temp; }
        public void setTemp(float temp) { this.temp = temp; }

        public float getFeelsLike() { return feelsLike; }
        public void setFeelsLike(float feelsLike) { this.feelsLike = feelsLike; }

        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }

        public int getPressure() { return pressure; }
        public void setPressure(int pressure) { this.pressure = pressure; }
    }

    public static class Weather {
        @SerializedName("description")
        private String description;
        @SerializedName("icon")
        private String icon;
        @SerializedName("main")
        private String main;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public String getMain() { return main; }
        public void setMain(String main) { this.main = main; }
    }

    public static class Wind {
        @SerializedName("speed")
        private float speed;
        @SerializedName("deg")
        private int deg;

        public float getSpeed() { return speed; }
        public void setSpeed(float speed) { this.speed = speed; }

        public int getDeg() { return deg; }
        public void setDeg(int deg) { this.deg = deg; }
    }

    public static class Sys {
        @SerializedName("sunrise")
        private long sunrise;
        @SerializedName("sunset")
        private long sunset;

        public long getSunrise() { return sunrise; }
        public void setSunrise(long sunrise) { this.sunrise = sunrise; }

        public long getSunset() { return sunset; }
        public void setSunset(long sunset) { this.sunset = sunset; }
    }
}
