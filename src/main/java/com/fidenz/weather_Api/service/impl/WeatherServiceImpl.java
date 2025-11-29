package com.fidenz.weather_Api.service.impl;

import com.fidenz.weather_Api.model.City;
import com.fidenz.weather_Api.controller.response.CityWeatherResponse;
import com.fidenz.weather_Api.model.WeatherData;
import com.fidenz.weather_Api.repository.WeatherDataRepository;
import com.fidenz.weather_Api.service.WeatherService;
import com.fidenz.weather_Api.util.CityLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final CityLoader cityLoader;
    private final WeatherDataRepository weatherDataRepository; // ⭐ NEW

    @Value("${open.weather.api.key}")
    private String apiKey;

    @Value("${open.weather.base.url}")
    private String baseUrl;


    @Override
    @Cacheable(value = "weatherCache", key = "'getWeatherForAllCities'")

    public List<CityWeatherResponse> getWeatherForAllCities() {

        List<City> cities = cityLoader.getCities();
        List<CityWeatherResponse> resultList = new ArrayList<>();

        for (City city : cities) {

            try {
                String url = baseUrl + "?id=" + city.getCityId() + "&appid=" + apiKey + "&units=metric";
                ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
                WeatherData weatherData = extractWeatherData(response.getBody(), city.getCityId());

                weatherDataRepository.save(weatherData);

                double comfortIndex = calculateComfortIndex(weatherData);

                CityWeatherResponse result = new CityWeatherResponse();
                result.setCityId(city.getCityId());
                result.setCityName(city.getCityName());
                result.setWeatherData(weatherData);
                result.setComfortIndex(comfortIndex);

                resultList.add(result);

            } catch (Exception e) {
                log.error("Error fetching weather for city {}", city.getCityName(), e);
            }
        }

        resultList.sort(Comparator.comparingDouble(CityWeatherResponse::getComfortIndex).reversed());

        int rank = 1;
        for (CityWeatherResponse r : resultList) {
            r.setRank(rank++);
        }

        return resultList;
    }


    private WeatherData extractWeatherData(Object responseBody, Long cityId) {

        var json = (Map<?, ?>) responseBody;

        var main = (Map<?, ?>) json.get("main");
        var wind = (Map<?, ?>) json.get("wind");
        var clouds = (Map<?, ?>) json.get("clouds");
        var weatherArray = (List<?>) json.get("weather");

        WeatherData data = new WeatherData();

        data.setTemperature(toDouble(main.get("temp")));
        data.setHumidity(toInt(main.get("humidity")));
        data.setWindSpeed(toDouble(wind.get("speed")));
        data.setCloudiness(toInt(clouds.get("all")));
        data.setCityId(cityId); // ⭐ IMPORTANT

        if (weatherArray != null && !weatherArray.isEmpty()) {
            var w = (Map<?, ?>) weatherArray.get(0);
            data.setDescription(String.valueOf(w.get("description")));
        }

        return data;
    }


    private int toInt(Object o) {
        return o == null ? 0 : Integer.parseInt(o.toString());
    }

    private double toDouble(Object o) {
        return o == null ? 0.0 : Double.parseDouble(o.toString());
    }


    private double calculateComfortIndex(WeatherData d) {

        double temperatureScore = 100 - Math.abs(d.getTemperature() - 22) * 4;
        temperatureScore = Math.max(0, temperatureScore);

        double humidityScore = 100 - Math.abs(d.getHumidity() - 50) * 1.2;
        humidityScore = Math.max(0, humidityScore);

        double windScore = 100 - d.getWindSpeed() * 10;
        windScore = Math.max(0, windScore);

        double cloudScore = 100 - Math.abs(d.getCloudiness() - 40) * 1.5;
        cloudScore = Math.max(0, cloudScore);

        return Math.max(0,
                Math.min((temperatureScore * 0.4) +
                                (humidityScore * 0.3) +
                                (windScore * 0.2) +
                                (cloudScore * 0.1),
                        100));
    }
}

