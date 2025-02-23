package com.openweathermapapi.model;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;
import java.util.Optional;

/**
 * Represents the complete weather data response from the OpenWeatherMap API.
 * <p>
 * This record maps directly to the JSON structure returned by the API. All fields are nullable
 * depending on API response availability.
 *
 * @param coord      Geographic coordinates of the location.
 * @param weather    List of weather condition descriptions. Typically, contains 1 item.
 * @param base       Internal API parameter.
 * @param main       Main weather parameters' container.
 * @param visibility Visibility in meters. Maximum value: 10,000 meters (10km).
 * @param wind       Wind parameters.
 * @param clouds     Cloudiness information.
 * @param dt         Time of data calculation (unix timestamp, UTC).
 * @param sys        System/internal parameters and country data.
 * @param timezone   Shift in seconds from UTC timezone.
 * @param id         City ID. Note: Built-in geocoder functionality is deprecated.
 * @param name       City name. Note: Built-in geocoder functionality is deprecated.
 * @param cod        Internal API parameter.
 * @param rain       Precipitation volume (mm/h, optional).
 * @param snow       Snow volume (mm/h, optional).
 * @see <a href="https://openweathermap.org/current">OpenWeatherMap Current Weather API Documentation</a>
 */
public record WeatherData(
        Coord coord,
        List<WeatherItem> weather,
        String base,
        Main main,
        Integer visibility,
        Wind wind,
        Clouds clouds,
        Long dt,
        Sys sys,
        Long timezone,
        Long id,
        String name,
        Integer cod,
        Optional<String> rain,
        Optional<String> snow
) {
    /**
     * Weather condition details container.
     *
     * @param id          Weather condition ID (e.g., 800 for clear sky)
     * @param main        Group of weather parameters (Rain, Snow, Clouds etc.)
     * @param description Human-readable weather description
     * @param icon        Weather icon ID (e.g., "04d" for overcast clouds daytime)
     */
    public record WeatherItem(
            Integer id,
            String main,
            String description,
            String icon
    ) {
    }

    /**
     * Wind measurement data.
     *
     * @param speed Wind speed. Units depend on request format:
     *              Default: m/s, Metric: m/s, Imperial: mph
     * @param deg   Wind direction in degrees (meteorological)
     * @param gust  Wind gust speed. Same units as speed
     */
    public record Wind(
            String speed,
            Integer deg,
            String gust
    ) {
    }

    /**
     * System and country data.
     *
     * @param type    Internal parameter
     * @param id      Internal parameter
     * @param country Country code (e.g., "GB", "US")
     * @param sunrise Sunrise time (unix timestamp, UTC)
     * @param sunset  Sunset time (unix timestamp, UTC)
     */
    public record Sys(
            Integer type,
            Integer id,
            String country,
            Long sunrise,
            Long sunset
    ) {
    }

    /**
     * Main weather measurements.
     *
     * @param temp      Temperature. Units:
     *                  Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit
     * @param feelsLike "Feels like" temperature. Same units as temp
     * @param tempMin   Minimum observed temperature. Same units as temp
     * @param tempMax   Maximum observed temperature. Same units as temp
     * @param pressure  Atmospheric pressure in hPa
     * @param humidity  Humidity percentage
     * @param seaLevel  Sea level pressure in hPa
     * @param grndLevel Ground level pressure in hPa
     */
    public record Main(
            Long temp,
            @JSONField(name = "feels_like")
            Long feelsLike,
            @JSONField(name = "temp_min")
            String tempMin,
            @JSONField(name = "temp_max")
            String tempMax,
            Integer pressure,
            Integer humidity,
            @JSONField(name = "sea_level")
            Integer seaLevel,
            @JSONField(name = "grnd_level")
            Integer grndLevel
    ) {
    }

    /**
     * Geographic coordinates.
     *
     * @param lon Longitude (-180 to 180)
     * @param lat Latitude (-90 to 90)
     */
    public record Coord(String lon, String lat) {
    }

    /**
     * Cloudiness data.
     *
     * @param all Cloudiness percentage (0-100%)
     */
    public record Clouds(Integer all) {
    }
}