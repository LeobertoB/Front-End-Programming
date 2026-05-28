package com.eventhub.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.eventhub.domain.enums.EventStatus;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        BigDecimal basePrice,
        Integer availableSeats,
        String imageUrl,
        EventStatus status,
        Long organizerId,
        String organizerName,
        CategoryResponse category,
        VenueResponse venue,
        List<EventImageResponse> galleryImages
) {
}
