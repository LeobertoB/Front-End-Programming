package com.eventhub.api.services;

import java.util.stream.Collectors;

import com.eventhub.api.dto.UpdateProfileImageRequest;
import com.eventhub.api.dto.UserProfileResponse;
import com.eventhub.domain.entities.AppUser;
import com.eventhub.repositories.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final AppUserRepository userRepository;
    private final FileStorageService fileStorageService;

    public UserProfileService(AppUserRepository userRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public UserProfileResponse updateProfileImage(Long userId, UpdateProfileImageRequest request) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setProfileImageUrl(request.profileImageUrl().trim());
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse uploadProfileImage(Long userId, org.springframework.web.multipart.MultipartFile file) {
        String imageUrl = fileStorageService.storeImage(file, "profiles");
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setProfileImageUrl(imageUrl);
        return toResponse(user);
    }

    private UserProfileResponse toResponse(AppUser user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getRegistrationDate(),
                user.getProfileImageUrl(),
                user.getCity(),
                user.getFavoriteEventType(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
    }
}
