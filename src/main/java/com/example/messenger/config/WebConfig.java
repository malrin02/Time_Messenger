package com.example.messenger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.profile-dir:uploads/profiles}")
    private String profileUploadDir;

    @Value("${app.upload.room-dir:uploads/rooms}")
    private String roomUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(profileUploadDir)
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations(uploadPath.toUri().toString());

        Path roomUploadPath = Paths.get(roomUploadDir)
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/uploads/rooms/**")
                .addResourceLocations(roomUploadPath.toUri().toString());
    }
}
