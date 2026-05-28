package com.eventhub.integrations;

import com.eventhub.integrations.dto.CountryInfoResponse;
import com.eventhub.integrations.dto.EventWeatherResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IntegrationController {

    private final WeatherService weatherService;
    private final CountryInfoService countryInfoService;

    public IntegrationController(WeatherService weatherService, CountryInfoService countryInfoService) {
        this.weatherService = weatherService;
        this.countryInfoService = countryInfoService;
    }

    @GetMapping("/api/events/{eventId}/weather")
    public EventWeatherResponse getWeather(@PathVariable Long eventId) {
        return weatherService.getForecastForEvent(eventId);
    }

    @GetMapping("/api/venues/country-info")
    public CountryInfoResponse getCountryInfo(@RequestParam String country) {
        return countryInfoService.getCountryInfo(country);
    }
}
