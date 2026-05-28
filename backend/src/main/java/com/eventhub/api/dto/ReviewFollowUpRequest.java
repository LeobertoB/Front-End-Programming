package com.eventhub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewFollowUpRequest(
        @NotBlank @Size(max = 1200) String followUp
) {
}
