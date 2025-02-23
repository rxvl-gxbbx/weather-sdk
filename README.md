# Weather SDK

This SDK allows you to interact with the OpenWeatherMap API and retrieve weather data for a given location.

## Installation

You can use the SDK by including the JAR file in your project. Follow the instructions below for installation.

### Option 1: Manual Installation (Direct JAR Download)

1. **Download the JAR file**:

   Download the latest version of the SDK JAR file
   from [this GitHub repository](https://github.com/rxvl-gxbbx/weather-sdk/blame/687f499c1ac00ea3119e693e8f8b810685e6b466/libs/weather-sdk-1.0-SNAPSHOT.jar).

2. **Add the JAR file to your project**:

    - **For Maven projects**:

      Add the following dependency to your `pom.xml` file:

      ```xml
      <dependency>
          <groupId>com.openweathermapapi</groupId>
          <artifactId>weather-sdk</artifactId>
          <version>1.0.0</version>
          <scope>system</scope>
          <systemPath>${project.basedir}/libs/weather-sdk-1.0-SNAPSHOT.jar</systemPath>
      </dependency>
      ```

    - **For Gradle projects**:

      Add the following to your `build.gradle` file:

      ```groovy
      dependencies {
          implementation files('libs/weather-sdk-1.0-SNAPSHOT.jar')
      }
      ```

    - **For standalone Java projects**:

      Simply add the downloaded JAR file to your `classpath`.

## Usage Example

Here is an example of how to use the SDK to retrieve weather data for a specific city:

1) Create an instance of the WeatherSDK:

```java
import com.openweathermapapi.core.WeatherSDK;
import com.openweathermapapi.model.WeatherData;

public class WeatherExample {
   public static void main(String[] args) {
      // Initialize the WeatherSDK with your API key
      String apiKey = "your-api-key-here";
      WeatherSDK sdk = new WeatherSDK(apiKey);

      // Call the method to retrieve weather data for a given city
      try {
         WeatherData weather = sdk.getWeather("London");
         System.out.println("Weather in " + weather.name() + ": " + weather.weather().getFirst().description());
      } catch (Exception e) {
         System.out.println("Error retrieving weather data: " + e.getMessage());
      }
   }
}
```

2) Retrieving weather for a city:

You can retrieve weather data for any city by simply passing the city name to the `getWeather` method. The SDK handles
API
calls and parsing the data into a `WeatherData` object.

```java
public static void main(String[] args) {
    // Initialize the WeatherSDK with your API key
    String apiKey = "your-api-key-here";
    WeatherSDK sdk = new WeatherSDK(apiKey);
    
    WeatherData weather = sdk.getWeather("New York");

    System.out.println("Current temperature: " + weather.main().temp() + "°C");
}
```

## API Key

To use the SDK, you need an OpenWeatherMap API key. You can get a free API key by signing up
at [OpenWeatherMap](https://openweathermap.org/).

## Validating API Key

The SDK automatically checks whether the provided API key is valid when making API calls. If your key is invalid or
missing, the SDK will throw an exception.

## SDK Features

* Retrieve current weather data for a given city.
* Handle various weather parameters such as temperature, humidity, wind speed, and visibility.
* Supports both metric and imperial units.
* Handle different types of weather conditions, such as clear sky, rain, snow, etc.
* Provides a simple and easy-to-use interface for integration into your Java projects.

## Exception Handling

The SDK will throw exceptions if something goes wrong during the API call:

* **BadRequestException**: If any parameters are missing or incorrect.
* **UnauthorizedException**: If the API key is invalid.
* **NotFoundException**: If the city is not found in the OpenWeatherMap database.
* **TooManyRequestsException**: If you've exceeded your API rate limit.
* **UnexpectedErrorException**: For server errors or other unexpected issues.

## Additional Information

* **API Documentation**: [OpenWeatherMap Current Weather API Documentation](https://openweathermap.org/current)
* **Support**: If you have any issues or questions, please open an issue on GitHub.