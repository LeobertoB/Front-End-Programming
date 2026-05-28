package com.eventhub.auth.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String name,
        String surname,
        String profileImageUrl,
        Set<String> roles
) {
}
