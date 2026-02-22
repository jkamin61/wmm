package com.org.wmm.media.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Storage abstraction for file uploads.
 * Current implementation: LocalStorageService (filesystem).
 * Future: S3StorageService, GCSStorageService, etc.
 */
public interface StorageService {

    /**
     * Stores a file and returns the relative path (from base storage dir).
     *
     * @param file   the uploaded file
     * @param subDir subdirectory (e.g. "items/42")
     * @return relative file path (e.g. "items/42/abc123.jpg")
     * @throws IOException if storage fails
     */
    String store(MultipartFile file, String subDir) throws IOException;

    /**
     * Deletes a file by its relative path.
     *
     * @param relativePath the path returned by {@link #store}
     * @throws IOException if deletion fails
     */
    void delete(String relativePath) throws IOException;

    /**
     * Resolves a relative path to an absolute filesystem path.
     */
    Path resolve(String relativePath);

    /**
     * Returns the public URL for a stored file.
     */
    String getPublicUrl(String relativePath);
}

