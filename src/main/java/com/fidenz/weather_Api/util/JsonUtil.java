package com.fidenz.weather_Api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    /**
     * Convert JSON string to an object of specified type
     *
     * @param json  JSON string
     * @param clazz Class type to convert into
     * @param <T>   Type parameter
     * @return Object of type T, or null if conversion fails
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to {}", clazz.getSimpleName(), e);
            return null;
        }
    }

    /**
     * Convert an object to JSON string
     *
     * @param obj Object to convert
     * @return JSON string representation, or null if conversion fails
     */
    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON", e);
            return null;
        }
    }
}
