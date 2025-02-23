package com.openweathermapapi.core;

import com.alibaba.fastjson2.JSON;
import com.openweathermapapi.model.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.openweathermapapi.util.Validator.*;

/**
 * WeatherSDK - Java Weather API SDK
 * <p>
 * WeatherSDK is a Java-based SDK designed to fetch and cache weather data using the OpenWeatherMap API.
 * <p>
 * It provides an efficient mechanism for retrieving weather information with configurable caching and polling mechanisms.
 * <p>
 * Key Features:
 * <ul>
 *     <li>Fetches real-time weather data from the OpenWeatherMap API.</li>
 *     <li>Caches up to 10 city weather reports to minimize redundant API calls.</li>
 *     <li>Supports two operational modes: on-demand updates and scheduled polling.</li>
 *     <li>Implements LRU (Least Recently Used) cache eviction when the city limit is reached.</li>
 *     <li>Includes an internal scheduler for periodic cache refreshes (configurable interval).</li>
 * </ul>
 */

public class WeatherSDK {
    /**
     * Logger instance for the WeatherSDK class.
     * <p>
     * Used for structured logging of:
     * <ul>
     *   <li>Cache operations (hits/misses/evictions)</li>
     *   <li>API request failures</li>
     *   <li>System warnings and errors</li>
     * </ul>
     *
     * @implNote Configured via SLF4J facade to support multiple logging implementations.
     * Log level should be set to INFO for production, DEBUG for troubleshooting.
     * @see org.slf4j.LoggerFactory
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherSDK.class);

    /**
     * Thread-safe map storing singleton instances of WeatherSDK, keyed by API key.
     * Ensures only one instance per API key exists.
     */
    private final static Map<String, WeatherSDK> INSTANCES = new ConcurrentHashMap<>();

    /**
     * Maximum number of cities allowed in the cache.
     * When exceeded, the least recently used entry is evicted.
     */
    private final static int MAX_CACHED_CITIES = 10;

    /**
     * Cache storing weather data with city names as keys.
     * Uses ConcurrentHashMap for thread-safe LRU eviction.
     */
    private final Map<String, CachedWeather> cache = new ConcurrentHashMap<>(MAX_CACHED_CITIES);

    /**
     * Time-to-live (TTL) for cached weather data in milliseconds.
     * Default: 10 minutes (10 * 60 * 1000 ms).
     */
    private final static int CACHE_EXPIRATION_MILLIS = 10 * 60 * 1000;

    /**
     * Interval for automatic cache refresh in POLLING mode (minutes).
     * Default: 5 minutes between updates.
     */
    private final static int CACHE_REFRESH_INTERVAL_MINUTES = 5;

    /**
     * OpenWeatherMap API endpoint template.
     * Placeholders: %s = city name, %s = API key.
     */
    private final static String URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";

    /**
     * OpenWeatherMap API key for authentication.
     * Validated during SDK initialization.
     */
    private final String apiKey;

    /**
     * Constructs a WeatherSDK instance with the specified API key and operational mode.
     * <p>
     * Initializes the SDK and automatically starts periodic cache updates if {@link Mode#POLLING} is selected.
     * This constructor is private to enforce singleton pattern access via {@link #getInstance}.
     *
     * @param apiKey OpenWeatherMap API key (must be non-null and non-empty).
     * @param mode   Operational mode ({@link Mode#ON_DEMAND} or {@link Mode#POLLING}).
     * @throws IllegalArgumentException If API key validation fails or mode is null.
     * @implNote In POLLING mode, creates a single-threaded scheduler to refresh all cached cities every
     * {@value #CACHE_REFRESH_INTERVAL_MINUTES} minutes.
     */
    private WeatherSDK(String apiKey, Mode mode) {
        validateApiKey(apiKey);
        validateMode(mode);

        this.apiKey = apiKey;

        if (mode.equals(Mode.POLLING)) {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(
                    this::updateAllCities,
                    CACHE_REFRESH_INTERVAL_MINUTES,
                    CACHE_REFRESH_INTERVAL_MINUTES,
                    TimeUnit.MINUTES
            );
        }
    }

    /**
     * Provides singleton instance per API key with thread-safe initialization.
     * <p>
     * Ensures only one SDK instance exists per unique API key. New instances are created
     * only when no existing instance matches the provided key.
     *
     * @param apiKey OpenWeatherMap API key (case-sensitive).
     * @param mode   Operational mode to use for new instances.
     * @return Existing or newly created WeatherSDK instance.
     * @throws IllegalArgumentException If API key validation fails or mode is null.
     * @see #deleteInstance For removing instances from the registry.
     */
    public static synchronized WeatherSDK getInstance(String apiKey, Mode mode) {
        validateApiKey(apiKey);
        validateMode(mode);

        return INSTANCES.computeIfAbsent(apiKey, key -> new WeatherSDK(key, mode));
    }

    /**
     * Removes and invalidates the WeatherSDK instance associated with the specified API key.
     * <p>
     * This operation is irreversible - subsequent calls to {@link #getInstance} with the same key
     * will create a new instance. Primarily used for cleanup/testing purposes.
     *
     * @param apiKey API key associated with the instance to remove.
     * @throws IllegalStateException    If no instance exists for the provided API key.
     * @throws IllegalArgumentException If API key validation fails.
     * @implSpec Synchronized to prevent race conditions during instance removal.
     */
    public static synchronized void deleteInstance(String apiKey) {
        validateApiKey(apiKey);

        WeatherSDK remove = INSTANCES.remove(apiKey);

        if (remove == null) {
            throw new IllegalStateException("No instance found for the provided API key");
        }
    }

    /**
     * Modes for weather data retrieval.
     * <ul>
     *     <li>{@link Mode#ON_DEMAND}: Fetches data only when explicitly requested.</li>
     *     <li>{@link Mode#POLLING}: Periodically updates cached data at predefined intervals.</li>
     * </ul>
     */
    public enum Mode {
        ON_DEMAND,
        POLLING
    }

    /**
     * Inner class representing cached weather data with a timestamp.
     */
    private static class CachedWeather {
        private final WeatherData weatherData;
        private final long timestampMillis;

        /**
         * Constructs a CachedWeather object with weather data and a timestamp.
         *
         * @param weatherData Weather data object (must not be null).
         */
        public CachedWeather(WeatherData weatherData) {
            validateWeatherData(weatherData);

            this.weatherData = weatherData;
            this.timestampMillis = System.currentTimeMillis();
        }
    }

    /**
     * Retrieves weather data for a city, using cached data if valid.
     * <p>
     * Implements the core cache-aside pattern:
     * <ol>
     *   <li>Checks cache for valid (under 10 minutes old) data</li>
     *   <li>Returns cached data if available and fresh</li>
     *   <li>Triggers synchronous update if cache is stale/missing</li>
     * </ol>
     *
     * @param city City name (case-sensitive, whitespace-sensitive)
     * @return {@link WeatherData} or null if update fails
     * @throws IllegalArgumentException If city validation fails
     * @see #fetchAndCacheWeatherForCity For update logic details
     */
    public WeatherData getWeather(String city) {
        validateCity(city);

        CachedWeather cachedWeather = cache.get(city);

        if (cachedWeather != null && System.currentTimeMillis() - cachedWeather.timestampMillis < CACHE_EXPIRATION_MILLIS) {
            return cachedWeather.weatherData;
        }

        return fetchAndCacheWeatherForCity(city);
    }

    /**
     * Fetches fresh weather data and updates cache for a specific city.
     * <p>
     * Handles three types of failures:
     * <ul>
     *   <li>Network errors (IOException) - connection/timeout issues</li>
     *   <li>URI errors (URISyntaxException) - invalid city format</li>
     *   <li>Unexpected errors - general API failures</li>
     * </ul>
     *
     * @param city City name to update
     * @return Updated {@link WeatherData} or null if update failed
     * @implNote Failed updates will overwrite cache with null value,
     * triggering re-fetch on next request. Consider adding
     * stale-while-revalidate logic for better resilience.
     */
    private WeatherData fetchAndCacheWeatherForCity(String city) {
        WeatherData weatherData = null;
        try {
            weatherData = fetchWeather(city);
        } catch (IOException e) {
            LOGGER.error("Error: IOException occurred while fetching weather for city: {} - {}", city, e.getMessage());
        } catch (URISyntaxException e) {
            LOGGER.error("Error: Invalid URI syntax while fetching weather for city: {} - {}", city, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while fetching weather for city: {} - {}", city, e.getMessage());
        }
        cacheWeather(city, weatherData);

        return weatherData;
    }

    /**
     * Fetches fresh weather data from the OpenWeatherMap API.
     *
     * @param city Name of the city to query.
     * @return Parsed {@link WeatherData} object.
     * @throws IOException        If the API request fails.
     * @throws URISyntaxException If the API URL is malformed.
     */
    private WeatherData fetchWeather(String city) throws IOException, URISyntaxException {
        validateCity(city);

        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        URL url = new URI(String.format(URL, encodedCity, this.apiKey)).toURL();

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        StringBuilder content;

        validateResponse(con);

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream())
        )) {
            String inputLine;
            content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) content.append(inputLine);
        }

        con.disconnect();

        return JSON.parseObject(content.toString(), WeatherData.class);
    }

    /**
     * Removes the least recently used entry when the cache exceeds its capacity.
     *
     * @param city        City name to cache.
     * @param weatherData Weather data to store.
     */
    private void cacheWeather(String city, WeatherData weatherData) {
        validateCity(city);
        validateWeatherData(weatherData);

        if (cache.size() >= MAX_CACHED_CITIES) {
            cache.entrySet().stream()
                    .min(Comparator.comparingLong(entry -> entry.getValue().timestampMillis))
                    .map(Map.Entry::getKey)
                    .ifPresent(cache::remove);
        }

        cache.put(city, new CachedWeather(weatherData));
    }

    /**
     * Periodically refreshes cached weather data for all currently stored cities.
     * <p>
     * This method is automatically scheduled in POLLING mode to maintain up-to-date cache.
     * Iterates through all cached cities and attempts to update their weather data.
     *
     * @implNote Executed by the scheduler at fixed intervals defined by
     * {@link #CACHE_REFRESH_INTERVAL_MINUTES}. Failed updates are logged
     * but don't interrupt the process.
     */
    private void updateAllCities() {
        cache.keySet().forEach(this::fetchAndCacheWeatherForCity);
    }
}