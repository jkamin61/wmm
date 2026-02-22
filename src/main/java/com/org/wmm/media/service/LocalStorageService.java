package com.org.wmm.media.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local filesystem implementation of StorageService.
 * Stores files under the configured base path (default: ./uploads).
 */
@Slf4j
@Service
public class LocalStorageService implements StorageService {

    private final Path basePath;
    private final String baseUrl;

    public LocalStorageService(
            @Value("${media.storage.local.base-path:./uploads}") String basePath,
            @Value("${media.storage.base-url:http://localhost:8080/media}") String baseUrl
    ) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        try {
            Files.createDirectories(this.basePath);
            log.info("Media storage initialized at: {}", this.basePath);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create storage directory: " + this.basePath, e);
        }
    }

    @Override
    public String store(MultipartFile file, String subDir) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Cannot store empty file");
        }

        // Create subdirectory
        Path targetDir = basePath.resolve(subDir).normalize();
        if (!targetDir.startsWith(basePath)) {
            throw new IOException("Cannot store file outside of storage directory");
        }
        Files.createDirectories(targetDir);

        // Generate unique filename preserving extension
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String uniqueName = UUID.randomUUID() + extension;

        Path targetPath = targetDir.resolve(uniqueName).normalize();
        if (!targetPath.startsWith(basePath)) {
            throw new IOException("Cannot store file outside of storage directory");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return relative path (e.g. "items/42/abc123.jpg")
        String relativePath = basePath.relativize(targetPath).toString().replace("\\", "/");
        log.debug("Stored file: {} ({} bytes) -> {}", originalFilename, file.getSize(), relativePath);

        return relativePath;
    }

    @Override
    public void delete(String relativePath) throws IOException {
        Path filePath = resolve(relativePath);
        if (!filePath.startsWith(basePath)) {
            throw new IOException("Cannot delete file outside of storage directory");
        }

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.debug("Deleted file: {}", relativePath);
        } else {
            log.warn("File not found for deletion: {}", relativePath);
        }
    }

    @Override
    public Path resolve(String relativePath) {
        return basePath.resolve(relativePath).normalize();
    }

    @Override
    public String getPublicUrl(String relativePath) {
        return baseUrl + "/" + relativePath;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}

