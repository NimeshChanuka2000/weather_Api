package com.fidenz.weather_Api.controller.response;

import com.fidenz.weather_Api.model.WeatherData;
import lombok.Data;

import java.io.Serializable;

@Data
public class CityWeatherResponse implements Serializable {
    private Long cityId;
    private String cityName;
    private WeatherData weatherData;
    private double comfortIndex;
    private int rank;
}
