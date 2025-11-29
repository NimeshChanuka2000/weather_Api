package com.fidenz.weather_Api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fidenz.weather_Api.model.City;
import com.fidenz.weather_Api.model.CityListWrapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CityLoader {

    private final JsonUtil jsonUtil;

    @Getter
    private List<City> cities = new ArrayList<>();

    @PostConstruct
    public void loadCities() {
        try {
            // Read cities.json from resources
            InputStream inputStream = new ClassPathResource("cities.json").getInputStream();
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Convert JSON -> Wrapper object using JsonUtil
            CityListWrapper wrapper = jsonUtil.fromJson(json, CityListWrapper.class);

            if (wrapper != null && wrapper.getList() != null) {
                // Map RawCity → City
                for (CityListWrapper.RawCity raw : wrapper.getList()) {
                    City city = new City();
                    city.setCityId(Long.parseLong(raw.getCityCode()));
                    city.setCityName(raw.getCityName());
                    cities.add(city);
                }
                log.info("Successfully loaded {} cities from cities.json", cities.size());
            } else {
                log.warn("No cities found in cities.json");
            }

        } catch (Exception e) {
            log.error("Failed to load cities.json", e);
        }
    }
}
