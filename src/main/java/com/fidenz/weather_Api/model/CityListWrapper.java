package com.fidenz.weather_Api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CityListWrapper {

    @JsonProperty("List")   // ⭐ Matches the JSON top-level key
    private List<RawCity> list;

    @Data
    public static class RawCity {

        @JsonProperty("CityCode")
        private String cityCode;

        @JsonProperty("CityName")
        private String cityName;

        @JsonProperty("Temp")
        private String temp;

        @JsonProperty("Status")
        private String status;
    }
}

