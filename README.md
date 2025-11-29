# Weather Analytics API - Spring Boot Backend

A secure weather analytics application that retrieves weather data from OpenWeatherMap API, processes it using a custom Comfort Index algorithm, and provides meaningful insights with Redis caching.

## 🌟 Features

- ✅ Weather data retrieval from OpenWeatherMap API
- ✅ Custom Comfort Index algorithm for city ranking
- ✅ Redis server-side caching (5 minutes TTL)
- ✅ RESTful API endpoints
- ✅ Cache status debug endpoint

## 📊 Comfort Index Algorithm

### Formula

The Comfort Index Score is calculated using four key weather parameters:

```
Comfort Score = (Temperature Score × 0.40) + (Humidity Score × 0.30) + 
                (Wind Score × 0.20) + (Cloudiness Score × 0.10)
```

### Parameters & Weights

1. **Temperature (40% weight)**
   - Optimal range: 18°C - 26°C
   - Human comfort is most affected by temperature
   - Scores decrease as temperature moves away from the ideal range
   - Converted from Kelvin to Celsius for calculation

2. **Humidity (30% weight)**
   - Optimal range: 40% - 60%
   - High humidity makes heat feel worse; low humidity causes discomfort
   - Second most important factor for comfort
   - Values range from 0-100%

3. **Wind Speed (20% weight)**
   - Optimal range: 1 - 3 m/s (3.6 - 10.8 km/h)
   - Light breeze is pleasant; strong winds are uncomfortable
   - Moderate impact on overall comfort
   - Measured in meters per second

4. **Cloudiness (10% weight)**
   - Optimal: 20% - 50% (partly cloudy)
   - Too sunny can be harsh; completely overcast can be gloomy
   - Least weighted but affects overall atmosphere
   - Values range from 0-100%

### Reasoning

- **Temperature** receives the highest weight (40%) because humans are most sensitive to ambient temperature
- **Humidity** is weighted at 30% as it significantly affects how temperature feels (heat index/feels-like temperature)
- **Wind Speed** at 20% moderates temperature perception and affects outdoor comfort
- **Cloudiness** at 10% has psychological impact on mood and perceived comfort

### Trade-offs Considered

1. **Excluded Pressure**: Atmospheric pressure has minimal direct impact on comfort for most people at sea level
2. **Excluded Visibility**: While useful, it's less directly related to physical comfort than other parameters
3. **Included Cloudiness**: Provides a balance between sunny and overcast conditions that affect mood
4. **Simplified Algorithm**: Real comfort indices (like Heat Index or Humidex) are complex; this provides a good balance between accuracy and simplicity
5. **Regional Bias**: Algorithm assumes temperate climate preferences; optimal ranges may vary for people accustomed to tropical/arctic regions
6. **Linear Scoring**: Uses linear degradation from optimal values rather than exponential, making it more predictable

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
   ``

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

