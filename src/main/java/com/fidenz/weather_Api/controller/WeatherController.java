package com.fidenz.weather_Api.controller;

import com.fidenz.weather_Api.controller.response.CityWeatherResponse;
import com.fidenz.weather_Api.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeatherController {

    private final WeatherService weatherService;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/cities")
    public ResponseEntity<List<CityWeatherResponse>> getWeatherForAllCities() {
        log.info("API CALL: /api/weather/cities");

        List<CityWeatherResponse> list = weatherService.getWeatherForAllCities();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/debug/redis")
    public ResponseEntity<Map<String, Object>> debugRedis() {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // Check Redis connection
            redisTemplate.getConnectionFactory().getConnection().ping();
            result.put("redis_status", "CONNECTED");

            // Get all keys from Redis
            Set<String> keys = redisTemplate.keys("*");
            result.put("keys_count", keys != null ? keys.size() : 0);

            if (keys != null) {
                List<Map<String, Object>> keyData = new ArrayList<>();
                for (String key : keys) {
                    Map<String, Object> info = new HashMap<>();

                    Long ttl = redisTemplate.getExpire(key);
                    ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                    Object value = ops.get(key);

                    info.put("key", key);
                    info.put("ttl_seconds", ttl);
                    info.put("value_preview", value != null ? value.toString().substring(0, Math.min(80, value.toString().length())) : null);

                    keyData.add(info);
                }
                result.put("keys", keyData);
            }

        } catch (Exception e) {
            result.put("redis_status", "DISCONNECTED");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
