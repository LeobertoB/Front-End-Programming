package com.eventhub.api.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.eventhub.config.UploadProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final long MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final Path uploadDirectory;
    private final String publicPath;

    public FileStorageService(UploadProperties uploadProperties) {
        this.uploadDirectory = Path.of(uploadProperties.directory()).toAbsolutePath().normalize();
        this.publicPath = uploadProperties.publicPath().replaceAll("/+$", "");
    }

    public String storeImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image file must be 5 MB or smaller");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, WEBP, or GIF images are supported");
        }

        try {
            Path targetDirectory = uploadDirectory.resolve(sanitizeFolder(folder)).normalize();
            Files.createDirectories(targetDirectory);
            String filename = UUID.randomUUID() + extensionFor(contentType);
            Path target = targetDirectory.resolve(filename).normalize();
            if (!target.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException("Invalid upload path");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(publicPath)
                    .path("/")
                    .path(sanitizeFolder(folder))
                    .path("/")
                    .path(filename)
                    .toUriString();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store uploaded image", exception);
        }
    }

    private String sanitizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "images";
        }
        return folder.replaceAll("[^a-zA-Z0-9_-]", "");
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
