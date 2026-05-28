package com.eventhub.api.dto;

import java.math.BigDecimal;

public record AdminStatsResponse(
        long users,
        long events,
        long bookings,
        long confirmedBookings,
        BigDecimal estimatedRevenue,
        double averageRating
) {
}
