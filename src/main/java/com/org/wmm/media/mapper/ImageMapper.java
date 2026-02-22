package com.org.wmm.media.mapper;

import com.org.wmm.content.items.entity.ImageEntity;
import com.org.wmm.content.items.entity.ImageTranslationEntity;
import com.org.wmm.media.dto.AdminImageDto;
import com.org.wmm.media.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ImageMapper {

    private final StorageService storageService;

    public AdminImageDto toAdminDto(ImageEntity entity) {
        List<AdminImageDto.TranslationDto> translations = entity.getImageTranslations() != null
                ? entity.getImageTranslations().stream()
                .map(this::toTranslationDto)
                .collect(Collectors.toList())
                : List.of();

        return AdminImageDto.builder()
                .id(entity.getId())
                .itemId(entity.getItem() != null ? entity.getItem().getId() : null)
                .filePath(entity.getFilePath())
                .fileName(entity.getFileName())
                .url(storageService.getPublicUrl(entity.getFilePath()))
                .fileSizeBytes(entity.getFileSizeBytes())
                .mimeType(entity.getMimeType())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .isPrimary(entity.getIsPrimary())
                .displayOrder(entity.getDisplayOrder())
                .createdAt(entity.getCreatedAt())
                .translations(translations)
                .build();
    }

    private AdminImageDto.TranslationDto toTranslationDto(ImageTranslationEntity translation) {
        return AdminImageDto.TranslationDto.builder()
                .id(translation.getId())
                .languageCode(translation.getLanguage().getCode())
                .languageName(translation.getLanguage().getName())
                .altText(translation.getAltText())
                .caption(translation.getCaption())
                .build();
    }
}

