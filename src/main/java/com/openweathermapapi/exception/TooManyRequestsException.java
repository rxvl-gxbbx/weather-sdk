package com.openweathermapapi.exception;

/**
 * Thrown when the OpenWeatherMap API returns a 429 Too Many Requests status.
 * <p>
 * Indicates:
 * <ul>
 *     <li>Free tier API call quota exceeded</li>
 *     <li>More than 60 requests/minute sent</li>
 * </ul>
 * The SDK automatically handles retry-after headers when available.
 */
public class TooManyRequestsException extends RuntimeException {
    /**
     * Constructs exception with rate limit details.
     *
     * @param message Should include retry timing if available (e.g., "Retry after 60 seconds").
     */
    public TooManyRequestsException(String message) {
        super(message);
    }
}