package com.openweathermapapi.exception;

/**
 * Thrown when the OpenWeatherMap API returns a 401 Unauthorized status.
 * <p>
 * Common causes:
 * <ul>
 *     <li>Missing API key in requests</li>
 *     <li>Invalid/revoked API key</li>
 *     <li>Expired subscription</li>
 * </ul>
 */
public class UnauthorizedException extends RuntimeException {
    /**
     * Constructs exception with authentication failure details.
     *
     * @param message Should clarify key validation issues (e.g., "Invalid API key format").
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}