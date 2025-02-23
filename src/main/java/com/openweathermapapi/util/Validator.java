package com.openweathermapapi.util;

import com.openweathermapapi.core.WeatherSDK;
import com.openweathermapapi.exception.*;
import com.openweathermapapi.model.WeatherData;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Central validation utility for the WeatherSDK library.
 * <p>
 * Provides static methods to enforce data integrity and API compliance across SDK operations.
 * This class performs critical checks for:
 * <ul>
 *     <li>API key validity (format and presence)</li>
 *     <li>City name requirements</li>
 *     <li>HTTP response status codes</li>
 *     <li>Weather data model consistency</li>
 * </ul>
 *
 * <p>Key features:
 * <ul>
 *     <li>Throws meaningful exceptions with detailed error messages</li>
 *     <li>Implements OpenWeatherMap API-specific validation rules</li>
 *     <li>Ensures safe data caching through pre-cache checks</li>
 * </ul>
 *
 * @implNote All methods are thread-safe and stateless. Validation failures throw
 * {@link IllegalArgumentException} or domain-specific exceptions from
 * {@link com.openweathermapapi.exception} package.
 * @see WeatherSDK Main SDK class utilizing these validations
 * @see WeatherData Model class validation
 */
public class Validator {

    /**
     * Validates the API key to ensure it is non-null and non-empty.
     *
     * @param apiKey OpenWeatherMap API key to validate.
     * @throws IllegalArgumentException If the API key is null or empty.
     * @implSpec This check is critical for all API requests. An invalid key will cause authentication failures.
     */
    public static void validateApiKey(String apiKey) {
        checkNotNullOrEmpty(apiKey, "API key");
        validateApiKeyFormat(apiKey);
    }

    /**
     * Validates the API key format (32-character hexadecimal string).
     *
     * @param apiKey OpenWeatherMap API key to validate.
     * @throws IllegalArgumentException If the key format is invalid.
     * @implNote OpenWeatherMap API keys typically consist of 32 alphanumeric characters.
     */
    public static void validateApiKeyFormat(String apiKey) {
        if (!apiKey.matches("^[a-fA-F0-9]{32}$")) {
            throw new IllegalArgumentException("Invalid API key format. Expected 32-character alphanumeric string.");
        }
    }

    /**
     * Validates the operational mode of the SDK.
     *
     * @param mode The mode to check (ON_DEMAND or POLLING).
     * @throws IllegalArgumentException If the mode is null.
     * @see com.openweathermapapi.core.WeatherSDK.Mode
     */
    public static void validateMode(WeatherSDK.Mode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Mode cannot be null");
        }
    }

    /**
     * Validates a city name for API requests.
     *
     * @param city City name to validate (case-sensitive, whitespace-sensitive).
     * @throws IllegalArgumentException If the city name is null, empty, or contains only whitespace.
     * @implNote The validation follows OpenWeatherMap API requirements. Leading/trailing spaces are considered significant.
     */
    public static void validateCity(String city) {
        checkNotNullOrEmpty(city, "City");
    }

    /**
     * Validates weather data object integrity before caching.
     *
     * @param weatherData Weather data object to validate.
     * @throws IllegalArgumentException If weatherData is null.
     * @implSpec This prevents caching of invalid or unparsed API responses.
     */
    public static void validateWeatherData(WeatherData weatherData) {
        if (weatherData == null) {
            throw new IllegalArgumentException("Weather data cannot be null");
        }
    }

    /**
     * Validates the HTTP response and throws domain-specific exceptions for error codes.
     *
     * @param con {@link HttpURLConnection} object to validate.
     * @throws IOException              Propagates I/O exceptions from the connection.
     * @throws BadRequestException      For HTTP 400 errors (invalid parameters).
     * @throws UnauthorizedException    For HTTP 401 errors (invalid API key).
     * @throws NotFoundException        For HTTP 404 errors (city not found).
     * @throws TooManyRequestsException For HTTP 429 errors (rate limiting).
     * @throws UnexpectedErrorException For 5xx server errors.
     */
    public static void validateResponse(HttpURLConnection con) throws IOException {
        if (!isStatusSucceeded(con.getResponseCode())) {
            switch (StatusCode.fromCode(con.getResponseCode())) {
                case BAD_REQUEST ->
                        throw new BadRequestException("Error 400 - Bad Request: Some mandatory parameters are missing or have incorrect values.");
                case UNAUTHORIZED ->
                        throw new UnauthorizedException("Error 401 - Unauthorized: API token is missing or invalid.");
                case NOT_FOUND ->
                        throw new NotFoundException("Error 404 - Not Found: The requested data does not exist in the service database.");
                case TOO_MANY_REQUESTS ->
                        throw new TooManyRequestsException("Error 429 - Too Many Requests: API request quota exceeded. Retry after some time.");
                case UNEXPECTED_ERROR ->
                        throw new UnexpectedErrorException("Error 5xx - Unexpected Server Error: Internal server error. You may retry or contact support.");
                default -> throw new UnexpectedErrorException("Unexpected response code: " + con.getResponseCode());
            }
        }
    }

    /**
     * Checks if the HTTP response status code indicates success (2xx range).
     *
     * @param statusCode HTTP status code to evaluate.
     * @return true if status code is between 200-299 (inclusive), false otherwise.
     * @apiNote Used internally to distinguish successful API responses from errors.
     */
    private static boolean isStatusSucceeded(int statusCode) {
        return statusCode >= 200 && statusCode <= 299;
    }

    /**
     * Generic validation for non-empty string fields.
     *
     * @param value     The string value to validate.
     * @param fieldName The name of the field being checked (for exception messaging).
     * @throws IllegalArgumentException If the value is null or blank.
     * @example {@code checkNotNullOrEmpty(apiKey, "API key");}
     */
    private static void checkNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
}
