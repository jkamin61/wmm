package com.org.wmm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Serves uploaded media files under /media/** as static resources.
 */
@Configuration
public class MediaWebConfig implements WebMvcConfigurer {

    @Value("${media.storage.local.base-path:./uploads}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(basePath).toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler("/media/**")
                .addResourceLocations(absolutePath);
    }
}

