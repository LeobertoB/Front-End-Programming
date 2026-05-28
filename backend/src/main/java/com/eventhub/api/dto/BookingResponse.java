package com.eventhub.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.eventhub.domain.enums.BookingStatus;

public record BookingResponse(
        Long id,
        Long eventId,
        String eventTitle,
        Integer quantity,
        BigDecimal totalPrice,
        BookingStatus status,
        Instant createdAt
) {
}
