package com.openweathermapapi.util;

import java.util.Arrays;

/**
 * Represents HTTP status codes relevant to the WeatherSDK operations.
 * <p>
 * This enum maps common HTTP error codes to domain-specific status categories.
 * Used primarily to standardize error handling for API responses.
 *
 * <p><b>Supported Status Codes:</b>
 * <ul>
 *     <li>400 - Bad Request</li>
 *     <li>401 - Unauthorized</li>
 *     <li>404 - Not Found</li>
 *     <li>429 - Too Many Requests</li>
 *     <li>5xx - Server Errors (500-599)</li>
 * </ul>
 */
public enum StatusCode {
    /**
     * 400 Bad Request - Invalid request parameters.
     */
    BAD_REQUEST("400"),

    /**
     * 401 Unauthorized - Missing or invalid API token.
     */
    UNAUTHORIZED("401"),

    /**
     * 404 Not Found - Requested resource not found.
     */
    NOT_FOUND("404"),

    /**
     * 429 Too Many Requests - API rate limit exceeded.
     */
    TOO_MANY_REQUESTS("429"),

    /**
     * 5xx Server Error - Catch-all for unexpected server-side errors.
     */
    UNEXPECTED_ERROR("5xx");

    private final String code;

    StatusCode(String code) {
        this.code = code;
    }

    /**
     * Converts a numeric HTTP status code to its corresponding StatusCode enum.
     * <p>
     * Handles server errors (5xx codes) as a single UNEXPECTED_ERROR category.
     *
     * @param code HTTP status code (e.g., 400, 404, 500).
     * @return Corresponding StatusCode enum.
     * @throws IllegalArgumentException If no matching StatusCode exists.
     * @example {@code
     * StatusCode.fromCode(401) // returns UNAUTHORIZED
     * StatusCode.fromCode(503) // returns UNEXPECTED_ERROR
     * }
     */
    public static StatusCode fromCode(int code) {
        String tempCode = String.valueOf(code);
        final String finalCode = tempCode.startsWith("5") ? "5xx" : tempCode;

        return Arrays.stream(values())
                .filter(it -> it.code.equals(finalCode))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported HTTP status code: " + code + ". No matching StatusCode enum found."
                ));
    }
}