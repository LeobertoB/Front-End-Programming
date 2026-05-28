package com.eventhub.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageRequest(
        @NotBlank String profileImageUrl
) {
}
