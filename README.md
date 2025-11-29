# Weather Analytics API - Spring Boot Backend

A secure weather analytics application that retrieves weather data from OpenWeatherMap API, processes it using a custom Comfort Index algorithm, and provides meaningful insights with Redis caching.

## 🌟 Features

- ✅ Weather data retrieval from OpenWeatherMap API
- ✅ Custom Comfort Index algorithm for city ranking
- ✅ Redis server-side caching (5 minutes TTL)
- ✅ RESTful API endpoints
- ✅ Cache status debug endpoint
- ⏳ Authentication & Authorization (Auth0) - In Progress

## 📊 Comfort Index Algorithm

### Formula

The Comfort Index Score is calculated using four key weather parameters:

```
Comfort Score = (Temperature Score × 0.40) + (Humidity Score × 0.30) + 
                (Wind Score × 0.20) + (Cloudiness Score × 0.10)

Where:
- Temperature Score = 100 - |temperature - 22| × 4
- Humidity Score = 100 - |humidity - 50| × 1.2
- Wind Score = 100 - windSpeed × 10
- Cloudiness Score = 100 - |cloudiness - 40| × 1.5

Final Score = max(0, min(calculated_score, 100))
```

### Parameters & Weights

1. **Temperature (40% weight)**
   - **Optimal value**: 22°C
   - **Penalty factor**: 4 points per degree deviation
   - **Reasoning**: Human comfort peaks around 22°C (room temperature). Each degree away reduces comfort significantly
   - **Example**: At 18°C or 26°C → Score = 84, At 30°C → Score = 68

2. **Humidity (30% weight)**
   - **Optimal value**: 50%
   - **Penalty factor**: 1.2 points per percentage deviation
   - **Reasoning**: 50% relative humidity is ideal. Too dry (<30%) causes discomfort; too humid (>70%) makes air feel heavy
   - **Example**: At 30% or 70% → Score = 76, At 80% → Score = 64

3. **Wind Speed (20% weight)**
   - **Optimal value**: 0 m/s (calm)
   - **Penalty factor**: 10 points per m/s
   - **Reasoning**: Light breezes are pleasant, but strong winds create discomfort. Score decreases linearly with wind speed
   - **Example**: At 2 m/s → Score = 80, At 5 m/s → Score = 50

4. **Cloudiness (10% weight)**
   - **Optimal value**: 40% (partly cloudy)
   - **Penalty factor**: 1.5 points per percentage deviation
   - **Reasoning**: Partial cloud cover is ideal - provides shade without being gloomy. Clear skies (0%) can be harsh; overcast (100%) can be depressing
   - **Example**: At 20% or 60% → Score = 70, At 100% → Score = 10

### Reasoning

- **Temperature (40%)** receives the highest weight because humans are most sensitive to ambient temperature. The ideal of 22°C represents comfortable room temperature
- **Humidity (30%)** significantly affects perceived comfort. 50% is the sweet spot - not too dry, not too humid
- **Wind Speed (20%)** can make comfortable temperatures feel cold or help cooling, but strong winds are universally uncomfortable
- **Cloudiness (10%)** has psychological impact. Partial cloudiness (40%) provides the best balance between sunshine and shade

### Mathematical Approach

The algorithm uses **absolute deviation from optimal values** with different penalty factors:
- Higher penalties (4.0 for temperature) mean faster score degradation
- Lower penalties (1.2 for humidity) create gentler curves
- Linear degradation makes the scoring predictable and fair
- Bounds ensure scores stay within 0-100 range

### Trade-offs Considered

1. **Linear vs Non-linear**: Chose linear degradation for simplicity and predictability, though real human comfort follows slightly curved patterns
2. **Single Optimal Point vs Range**: Used single optimal values (e.g., 22°C) rather than ranges for mathematical precision
3. **Excluded Pressure**: Atmospheric pressure has minimal direct impact on comfort for most people at typical elevations
4. **Excluded Visibility**: While useful for outdoor activities, it's less directly related to physical comfort
5. **Regional Bias**: Optimal values reflect temperate climate preferences; tropical residents might prefer 26°C
6. **Weight Distribution**: 40-30-20-10 split balances physical impact with psychological factors

## 🏗️ Architecture

### Technology Stack

- **Backend Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Caching**: Redis with Jedis client
- **Build Tool**: Maven
- **API Integration**: OpenWeatherMap API
- **Data Format**: JSON

### Project Structure

```
weather-analytics/
├── src/main/java/com/fidenz/weather_Api/
│   ├── config/
│   │   ├── CacheConfig.java          # Redis cache configuration
│   │   ├── RedisConfig.java          # Redis connection setup
│   │   └── ObjectMapperConfig.java   # JSON serialization config
│   ├── controller/
│   │   ├── WeatherController.java    # REST API endpoints
│   │   └── response/
│   │       └── CityWeatherResponse.java
│   ├── model/
│   │   ├── City.java                 # City entity
│   │   ├── WeatherData.java          # OpenWeatherMap response model
│   │   └── CityListWrapper.java
│   ├── repository/
│   │   ├── CityRepository.java       # City data access
│   │   └── WeatherDataRepository.java
│   ├── service/
│   │   ├── WeatherService.java       # Service interface
│   │   └── impl/
│   │       └── WeatherServiceImpl.java
│   ├── util/
│   │   ├── CityLoader.java           # Load cities from JSON
│   │   └── JsonUtil.java
│   └── WeatherApiApplication.java    # Main application
├── src/main/resources/
│   ├── application.properties        # Configuration
│   └── cities.json                   # City data (10+ cities)
└── pom.xml                           # Maven dependencies
```

## 🚀 Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis Server (running on localhost:6379)
- OpenWeatherMap API Key

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/NimeshChanuka2000/weather_Api.git
   cd weather_Api
   ```

2. **Install and Start Redis**
   ```bash
   # Ubuntu/Debian
   sudo apt-get install redis-server
   sudo systemctl start redis
   sudo systemctl enable redis
   
   # Verify Redis is running
   redis-cli ping
   # Should return: PONG
   ```

3. **Configure API Key**
   
   Open `src/main/resources/application.properties` and add your OpenWeatherMap API key:
   ```properties
   openweathermap.api.key=YOUR_API_KEY_HERE
   ```
   
   Get your API key from: https://openweathermap.org/api

4. **Build the project**
   ```bash
   ./mvnw clean install
   ```

5. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Or run the JAR:
   ```bash
   java -jar target/weather-analytics-1.0.0.jar
   ```

6. **Verify the application**
   
   The application will start on `http://localhost:8080`

## 📡 API Endpoints

### 1. Get Weather with Comfort Index (All Cities)

```http
GET /api/weather/comfort-index
```

**Response:**
```json
[
  {
    "cityId": 2172797,
    "cityName": "Cairns",
    "weatherDescription": "overcast clouds",
    "temperature": 26.0,
    "humidity": 73,
    "windSpeed": 2.57,
    "cloudiness": 100,
    "comfortScore": 68.5,
    "rank": 1,
    "comfortLevel": "Good"
  }
]
```

### 2. Get Weather for Specific City

```http
GET /api/weather/{cityId}
```

**Example:**
```http
GET /api/weather/2172797
```

### 3. Cache Status (Debug Endpoint)

```http
GET /api/weather/cache/status
```

**Response:**
```json
{
  "cacheKey": "weather:all_cities",
  "status": "HIT",
  "timestamp": "2025-01-29T10:47:00",
  "ttlSeconds": 245,
  "totalCachedItems": 12
}
```

## 💾 Cache Design

### Caching Strategy

1. **Raw Weather Data Caching**
   - Each city's weather data cached separately
   - Key format: `weather:raw:{cityId}`
   - TTL: 5 minutes (300 seconds)

2. **Processed Comfort Index Caching**
   - Complete ranked list cached
   - Key: `weather:comfort_index:all`
   - TTL: 5 minutes (300 seconds)

### Benefits

- **Reduced API Calls**: Minimizes requests to OpenWeatherMap API
- **Faster Response**: Cached data served in <10ms vs 200-500ms for API calls
- **Cost Savings**: Avoids API rate limits and reduces costs
- **Scalability**: Can handle more concurrent users

### Cache Invalidation

- Time-based: Automatic expiration after 5 minutes
- Weather data updates frequently, 5 minutes is a good balance

## ⚙️ Configuration

### Application Properties

Key configurations in `application.properties`:

```properties
# Server
server.port=8080

# OpenWeatherMap API
openweathermap.api.url=https://api.openweathermap.org/data/2.5/weather
openweathermap.api.key=YOUR_API_KEY

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Cache TTL
weather.cache.ttl=300
```



