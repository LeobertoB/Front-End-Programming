package com.eventhub.api.dto;

import java.time.Instant;
import java.util.Set;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String surname,
        Instant registrationDate,
        String profileImageUrl,
        String city,
        String favoriteEventType,
        Set<String> roles
) {
}
