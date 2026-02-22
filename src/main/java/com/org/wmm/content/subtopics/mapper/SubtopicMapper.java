package com.org.wmm.content.subtopics.mapper;

import com.org.wmm.content.subtopics.dto.AdminSubtopicDto;
import com.org.wmm.content.subtopics.entity.SubtopicEntity;
import com.org.wmm.content.subtopics.entity.SubtopicTranslationEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubtopicMapper {

    /**
     * Maps a SubtopicEntity to AdminSubtopicDto (includes all translations).
     */
    public AdminSubtopicDto toAdminDto(SubtopicEntity entity) {
        List<AdminSubtopicDto.TranslationDto> translationDtos = entity.getTranslations() != null
                ? entity.getTranslations().stream()
                .map(this::toTranslationDto)
                .collect(Collectors.toList())
                : List.of();

        return AdminSubtopicDto.builder()
                .id(entity.getId())
                .topicId(entity.getTopic() != null ? entity.getTopic().getId() : null)
                .topicSlug(entity.getTopic() != null ? entity.getTopic().getSlug() : null)
                .slug(entity.getSlug())
                .icon(entity.getIcon())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.getIsActive())
                .status(entity.getStatus())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .translations(translationDtos)
                .build();
    }

    private AdminSubtopicDto.TranslationDto toTranslationDto(SubtopicTranslationEntity translation) {
        return AdminSubtopicDto.TranslationDto.builder()
                .id(translation.getId())
                .languageCode(translation.getLanguage().getCode())
                .languageName(translation.getLanguage().getName())
                .title(translation.getTitle())
                .subtitle(translation.getSubtitle())
                .description(translation.getDescription())
                .metaTitle(translation.getMetaTitle())
                .metaDescription(translation.getMetaDescription())
                .build();
    }
}

