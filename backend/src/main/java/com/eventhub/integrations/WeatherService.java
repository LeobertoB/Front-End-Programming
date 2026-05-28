package com.eventhub.integrations;

import java.net.URI;
import java.time.LocalDateTime;
import com.eventhub.domain.entities.Event;
import com.eventhub.domain.entities.Venue;
import com.eventhub.integrations.dto.EventWeatherResponse;
import com.eventhub.repositories.EventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final EventRepository eventRepository;
    private final String openMeteoBaseUrl;

    public WeatherService(
            RestTemplate restTemplate,
            EventRepository eventRepository,
            @Value("${app.integrations.open-meteo-base-url}") String openMeteoBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.eventRepository = eventRepository;
        this.openMeteoBaseUrl = openMeteoBaseUrl;
    }

    @Transactional(readOnly = true)
    public EventWeatherResponse getForecastForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        Venue venue = event.getVenue();
        if (venue.getLatitude() == null || venue.getLongitude() == null) {
            throw new IllegalArgumentException("Event venue does not have coordinates for weather lookup");
        }

        URI uri = UriComponentsBuilder.fromUriString(openMeteoBaseUrl + "/forecast")
                .queryParam("latitude", venue.getLatitude())
                .queryParam("longitude", venue.getLongitude())
                .queryParam("hourly", "temperature_2m,weather_code,wind_speed_10m")
                .queryParam("timezone", "auto")
                .build()
                .toUri();

        JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
        if (response == null || !response.has("hourly")) {
            throw new IllegalArgumentException("Weather forecast is unavailable");
        }

        ForecastPoint point = closestForecastPoint(response.get("hourly"), event.getStartsAt());
        return new EventWeatherResponse(
                event.getId(),
                event.getTitle(),
                venue.getCity(),
                event.getStartsAt().toLocalDate(),
                point.time(),
                point.temperature(),
                point.windSpeed(),
                point.weatherCode(),
                summarizeWeather(point.weatherCode(), point.temperature(), point.windSpeed())
        );
    }

    private ForecastPoint closestForecastPoint(JsonNode hourly, LocalDateTime eventTime) {
        JsonNode timeArray = hourly.get("time");
        JsonNode temperatures = hourly.get("temperature_2m");
        JsonNode weatherCodes = hourly.get("weather_code");
        JsonNode windSpeeds = hourly.get("wind_speed_10m");

        if (timeArray == null || temperatures == null || weatherCodes == null || windSpeeds == null || timeArray.isEmpty()) {
            throw new IllegalArgumentException("Weather forecast is incomplete");
        }

        int closestIndex = 0;
        long closestSeconds = Long.MAX_VALUE;
        for (int i = 0; i < timeArray.size(); i++) {
            LocalDateTime forecastTime = LocalDateTime.parse(timeArray.get(i).asText());
            long seconds = Math.abs(java.time.Duration.between(eventTime, forecastTime).getSeconds());
            if (seconds < closestSeconds) {
                closestSeconds = seconds;
                closestIndex = i;
            }
        }

        return new ForecastPoint(
                LocalDateTime.parse(timeArray.get(closestIndex).asText()),
                temperatures.get(closestIndex).asDouble(),
                weatherCodes.get(closestIndex).asInt(),
                windSpeeds.get(closestIndex).asDouble()
        );
    }

    private String summarizeWeather(Integer weatherCode, Double temperature, Double windSpeed) {
        String condition = switch (weatherCode) {
            case 0 -> "clear sky";
            case 1, 2, 3 -> "partly cloudy";
            case 45, 48 -> "foggy";
            case 51, 53, 55, 61, 63, 65 -> "rain expected";
            case 71, 73, 75 -> "snow expected";
            case 95, 96, 99 -> "thunderstorm risk";
            default -> "mixed conditions";
        };
        return "Forecast suggests " + condition + ", " + temperature + "C, wind " + windSpeed + " km/h.";
    }

    private record ForecastPoint(
            LocalDateTime time,
            Double temperature,
            Integer weatherCode,
            Double windSpeed
    ) {
    }
}
