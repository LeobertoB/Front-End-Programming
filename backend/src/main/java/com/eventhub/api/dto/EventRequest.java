package com.eventhub.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eventhub.domain.enums.EventStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotNull @Future LocalDateTime startsAt,
        @NotNull @Future LocalDateTime endsAt,
        @NotNull @DecimalMin("0.00") BigDecimal basePrice,
        @NotNull @Min(1) Integer availableSeats,
        @NotBlank String imageUrl,
        @NotNull Long venueId,
        @NotNull Long categoryId,
        EventStatus status
) {
}
