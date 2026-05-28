package com.eventhub.api.dto;

public record VenueResponse(
        Long id,
        String name,
        String city,
        String country,
        String address,
        Integer capacity,
        Double latitude,
        Double longitude
) {
}
