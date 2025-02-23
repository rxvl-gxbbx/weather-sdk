package com.openweathermapapi.exception;

/**
 * Thrown when the OpenWeatherMap API returns a 400 Bad Request status.
 * <p>
 * Indicates invalid request parameters, such as:
 * <ul>
 *     <li>Missing mandatory query parameters</li>
 *     <li>Invalid coordinate values</li>
 *     <li>Malformed request syntax</li>
 * </ul>
 */
public class BadRequestException extends RuntimeException {
    /**
     * Constructs exception with detailed error message.
     *
     * @param message Should include specific validation failures (e.g., "Latitude must be between -90 and 90").
     */
    public BadRequestException(String message) {
        super(message);
    }
}