package com.eventhub.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VenueRequest(
        @NotBlank @Size(max = 140) String name,
        @NotBlank @Size(max = 140) String city,
        @NotBlank @Size(max = 140) String country,
        @NotBlank @Size(max = 240) String address,
        @NotNull @Min(1) Integer capacity,
        Double latitude,
        Double longitude
) {
}
