package com.fidenz.weather_Api.service;

import com.fidenz.weather_Api.controller.response.CityWeatherResponse;

import java.util.List;

public interface WeatherService {
    List<CityWeatherResponse> getWeatherForAllCities();
}
