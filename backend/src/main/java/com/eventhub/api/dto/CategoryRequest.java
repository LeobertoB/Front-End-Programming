package com.eventhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(max = 400) String description
) {
}
