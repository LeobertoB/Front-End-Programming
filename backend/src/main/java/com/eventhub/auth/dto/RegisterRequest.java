package com.eventhub.auth.dto;

import com.eventhub.domain.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 80) String password,
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(max = 80) String surname,
        @NotBlank String profileImageUrl,
        @Size(max = 120) String city,
        @Size(max = 120) String favoriteEventType,
        @NotNull RoleName role
) {
}
