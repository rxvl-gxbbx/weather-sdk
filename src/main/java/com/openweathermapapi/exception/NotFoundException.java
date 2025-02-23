package com.openweathermapapi.exception;

/**
 * Thrown when the OpenWeatherMap API returns a 404 Not Found status.
 * <p>
 * Typically indicates:
 * <ul>
 *     <li>City name doesn't exist in OpenWeatherMap's database</li>
 *     <li>Geographic coordinates don't match any known location</li>
 * </ul>
 */
public class NotFoundException extends RuntimeException {
    /**
     * Constructs exception with location-specific message.
     *
     * @param message Should specify the invalid location (e.g., "Weather data not found for 'Lonndoon'").
     */
    public NotFoundException(String message) {
        super(message);
    }
}