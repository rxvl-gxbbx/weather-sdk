package com.openweathermapapi.exception;

/**
 * Thrown for any 5xx Server Error responses from OpenWeatherMap API.
 * <p>
 * Represents temporary server-side issues such as:
 * <ul>
 *     <li>Database connection failures</li>
 *     <li>Unexpected server crashes</li>
 *     <li>Maintenance downtime</li>
 * </ul>
 * Clients should implement retry logic with exponential backoff.
 */
public class UnexpectedErrorException extends RuntimeException {
    /**
     * Constructs exception with server error details.
     *
     * @param message Should include error code if available (e.g., "Internal Server Error (503)").
     */
    public UnexpectedErrorException(String message) {
        super(message);
    }
}