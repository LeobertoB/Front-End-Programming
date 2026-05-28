package com.eventhub.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(UploadProperties.class)
public class UploadConfig implements WebMvcConfigurer {

    private final UploadProperties uploadProperties;

    public UploadConfig(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pattern = uploadProperties.publicPath().replaceAll("/+$", "") + "/**";
        Path uploadDirectory = Path.of(uploadProperties.directory()).toAbsolutePath().normalize();
        registry.addResourceHandler(pattern)
                .addResourceLocations(uploadDirectory.toUri().toString());
    }
}
