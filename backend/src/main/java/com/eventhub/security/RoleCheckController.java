package com.eventhub.security;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleCheckController {

    @GetMapping("/api/users/me")
    public Map<String, Object> currentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of(
                "id", principal.getId(),
                "email", principal.getUsername(),
                "roles", principal.getAuthorities().stream()
                        .map(Object::toString)
                        .toList()
        );
    }

    @GetMapping("/api/organizer/ping")
    public Map<String, String> organizerPing() {
        return Map.of("message", "Organizer access granted");
    }

    @GetMapping("/api/admin/ping")
    public Map<String, String> adminPing() {
        return Map.of("message", "Admin access granted");
    }
}
