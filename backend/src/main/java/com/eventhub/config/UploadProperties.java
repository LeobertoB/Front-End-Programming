package com.eventhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.uploads")
public record UploadProperties(
        String directory,
        String publicPath
) {
    public UploadProperties {
        if (directory == null || directory.isBlank()) {
            directory = "uploads";
        }
        if (publicPath == null || publicPath.isBlank()) {
            publicPath = "/uploads";
        }
    }
}
