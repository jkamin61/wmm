package com.org.wmm.media.service;

import com.org.wmm.audit.service.AuditService;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.content.items.entity.ImageEntity;
import com.org.wmm.content.items.entity.ImageTranslationEntity;
import com.org.wmm.content.items.entity.ItemEntity;
import com.org.wmm.content.items.repository.ItemRepository;
import com.org.wmm.languages.entity.LanguageEntity;
import com.org.wmm.languages.service.LanguageQueryService;
import com.org.wmm.media.dto.*;
import com.org.wmm.media.mapper.ImageMapper;
import com.org.wmm.media.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageAdminService {

    private final ImageRepository imageRepository;
    private final ItemRepository itemRepository;
    private final StorageService storageService;
    private final LanguageQueryService languageQueryService;
    private final ImageMapper imageMapper;
    private final AuditService auditService;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/avif"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    // ─── LIST ──────────────────────────────────────────────────────

    /**
     * Returns all images for an item, ordered by display_order.
     */
    @Transactional(readOnly = true)
    public List<AdminImageDto> getImagesByItemId(Long itemId) {
        assertItemExists(itemId);
        return imageRepository.findByItemIdWithTranslations(itemId).stream()
                .map(imageMapper::toAdminDto)
                .collect(Collectors.toList());
    }

    // ─── UPLOAD ────────────────────────────────────────────────────

    /**
     * Uploads one or more images for an item.
     * First image on an item is automatically set as primary.
     */
    @Transactional
    public List<AdminImageDto> uploadImages(Long itemId, MultipartFile[] files) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        if (files == null || files.length == 0) {
            throw new BadRequestException("No files provided");
        }

        int currentCount = imageRepository.countByItemId(itemId);
        int nextOrder = imageRepository.findMaxDisplayOrderByItemId(itemId) + 1;
        boolean hasPrimary = imageRepository.findPrimaryByItemId(itemId).isPresent();

        List<AdminImageDto> results = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            validateFile(file);

            try {
                String subDir = "items/" + itemId;
                String relativePath = storageService.store(file, subDir);

                ImageEntity image = ImageEntity.builder()
                        .item(item)
                        .filePath(relativePath)
                        .fileName(file.getOriginalFilename())
                        .fileSizeBytes(file.getSize())
                        .mimeType(file.getContentType())
                        .isPrimary(!hasPrimary && i == 0)  // first image becomes primary if none exists
                        .displayOrder(nextOrder + i)
                        .build();

                ImageEntity saved = imageRepository.save(image);
                log.info("Uploaded image: {} for item {} (id={})", relativePath, item.getSlug(), saved.getId());

                auditService.logCreate("image", saved.getId(),
                        "{\"itemId\":" + itemId + ",\"fileName\":\"" + file.getOriginalFilename() + "\"}");

                results.add(imageMapper.toAdminDto(saved));
            } catch (IOException e) {
                log.error("Failed to store file: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
            }
        }

        return results;
    }

    // ─── UPDATE METADATA ───────────────────────────────────────────

    /**
     * Updates display_order and/or is_primary for an image.
     * Setting is_primary=true will unset primary from all other images of the same item.
     */
    @Transactional
    public AdminImageDto updateImage(Long imageId, UpdateImageRequest request) {
        ImageEntity image = imageRepository.findByIdWithTranslations(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        if (request.getDisplayOrder() != null) {
            image.setDisplayOrder(request.getDisplayOrder());
        }

        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            // Unset primary on all other images of this item
            imageRepository.clearPrimaryForItem(image.getItem().getId());
            image.setIsPrimary(true);
        } else if (Boolean.FALSE.equals(request.getIsPrimary())) {
            image.setIsPrimary(false);
        }

        ImageEntity saved = imageRepository.save(image);
        log.info("Updated image metadata: id={}", imageId);

        auditService.logUpdate("image", imageId, null,
                "{\"displayOrder\":" + saved.getDisplayOrder() + ",\"isPrimary\":" + saved.getIsPrimary() + "}");

        return imageMapper.toAdminDto(
                imageRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    // ─── REORDER ───────────────────────────────────────────────────

    /**
     * Reorders images for an item. The list index becomes the new display_order.
     * All image IDs must belong to the specified item.
     */
    @Transactional
    public List<AdminImageDto> reorderImages(Long itemId, ReorderImagesRequest request) {
        assertItemExists(itemId);

        List<ImageEntity> images = imageRepository.findByItemIdOrdered(itemId);
        Map<Long, ImageEntity> imageMap = images.stream()
                .collect(Collectors.toMap(ImageEntity::getId, img -> img));

        // Validate all IDs belong to this item
        for (Long id : request.getImageIds()) {
            if (!imageMap.containsKey(id)) {
                throw new BadRequestException("Image ID " + id + " does not belong to item " + itemId);
            }
        }

        // Validate all images of the item are included
        if (request.getImageIds().size() != images.size()) {
            throw new BadRequestException("Reorder list must contain exactly " + images.size() +
                    " image IDs (all images of the item). Got " + request.getImageIds().size());
        }

        // Apply new order
        for (int i = 0; i < request.getImageIds().size(); i++) {
            ImageEntity img = imageMap.get(request.getImageIds().get(i));
            img.setDisplayOrder(i);
        }

        imageRepository.saveAll(images);
        log.info("Reordered {} images for item {}", images.size(), itemId);

        auditService.logUpdate("item", itemId, null,
                "{\"action\":\"reorder_images\",\"order\":" + request.getImageIds() + "}");

        return imageRepository.findByItemIdWithTranslations(itemId).stream()
                .map(imageMapper::toAdminDto)
                .collect(Collectors.toList());
    }

    // ─── TRANSLATIONS ──────────────────────────────────────────────

    /**
     * Upserts translations (alt/caption) for an image.
     */
    @Transactional
    public AdminImageDto updateImageTranslations(Long imageId, UpdateImageTranslationsRequest request) {
        ImageEntity image = imageRepository.findByIdWithTranslations(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        for (ImageTranslationRequest tr : request.getTranslations()) {
            LanguageEntity language = languageQueryService.resolveLanguage(tr.getLanguageCode());

            ImageTranslationEntity existing = image.getImageTranslations().stream()
                    .filter(t -> t.getLanguage().getId().equals(language.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setAltText(tr.getAltText());
                existing.setCaption(tr.getCaption());
            } else {
                ImageTranslationEntity newTranslation = ImageTranslationEntity.builder()
                        .image(image)
                        .language(language)
                        .altText(tr.getAltText())
                        .caption(tr.getCaption())
                        .build();
                image.getImageTranslations().add(newTranslation);
            }
        }

        ImageEntity saved = imageRepository.save(image);
        log.info("Updated translations for image id={}", imageId);

        auditService.logUpdate("image", imageId, null, "{\"action\":\"update_translations\"}");

        return imageMapper.toAdminDto(
                imageRepository.findByIdWithTranslations(saved.getId()).orElse(saved));
    }

    // ─── DELETE ────────────────────────────────────────────────────

    /**
     * Deletes an image (removes file from storage + DB record).
     */
    @Transactional
    public void deleteImage(Long imageId) {
        ImageEntity image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        // Delete file from storage
        try {
            storageService.delete(image.getFilePath());
        } catch (IOException e) {
            log.error("Failed to delete file from storage: {}", image.getFilePath(), e);
            // Continue with DB deletion even if file removal fails
        }

        Long itemId = image.getItem().getId();
        boolean wasPrimary = Boolean.TRUE.equals(image.getIsPrimary());

        imageRepository.delete(image);
        log.info("Deleted image: id={}, file={}", imageId, image.getFilePath());

        // If deleted image was primary, promote the next one
        if (wasPrimary) {
            imageRepository.findByItemIdOrdered(itemId).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setIsPrimary(true);
                        imageRepository.save(next);
                        log.info("Promoted image id={} to primary for item {}", next.getId(), itemId);
                    });
        }

        auditService.logDelete("image", imageId);
    }

    // ─── PRIVATE HELPERS ───────────────────────────────────────────

    private void assertItemExists(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum of 10 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported file type: " + contentType +
                    ". Allowed: " + ALLOWED_MIME_TYPES);
        }
    }
}

