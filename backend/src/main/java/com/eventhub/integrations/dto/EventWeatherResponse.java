package com.eventhub.integrations.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventWeatherResponse(
        Long eventId,
        String eventTitle,
        String city,
        LocalDate eventDate,
        LocalDateTime forecastTime,
        Double temperatureCelsius,
        Double windSpeedKmh,
        Integer weatherCode,
        String summary
) {
}
