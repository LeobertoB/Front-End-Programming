package com.eventhub.api.controllers;

import com.eventhub.api.dto.UpdateProfileImageRequest;
import com.eventhub.api.dto.UserProfileResponse;
import com.eventhub.api.services.UserProfileService;
import com.eventhub.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PutMapping("/api/users/me/profile-image")
    public UserProfileResponse updateProfileImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileImageRequest request
    ) {
        return userProfileService.updateProfileImage(principal.getId(), request);
    }

    @PostMapping("/api/users/me/profile-image/upload")
    public UserProfileResponse uploadProfileImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file
    ) {
        return userProfileService.uploadProfileImage(principal.getId(), file);
    }
}
