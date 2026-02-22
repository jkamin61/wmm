package com.org.wmm.content.topics.mapper;

import com.org.wmm.content.topics.dto.AdminTopicDto;
import com.org.wmm.content.topics.dto.TopicDto;
import com.org.wmm.content.topics.entity.TopicEntity;
import com.org.wmm.content.topics.entity.TopicTranslationEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TopicMapper {

    public TopicDto toDto(TopicEntity entity, Long languageId, Long defaultLangId) {
        TopicTranslationEntity translation = resolveTranslation(entity, languageId, defaultLangId);

        return TopicDto.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .icon(entity.getIcon())
                .title(translation != null ? translation.getTitle() : entity.getSlug())
                .subtitle(translation != null ? translation.getSubtitle() : null)
                .description(translation != null ? translation.getDescription() : null)
                .metaTitle(translation != null ? translation.getMetaTitle() : null)
                .metaDescription(translation != null ? translation.getMetaDescription() : null)
                .build();
    }

    /**
     * Maps a TopicEntity to AdminTopicDto (includes all translations).
     */
    public AdminTopicDto toAdminDto(TopicEntity entity) {
        List<AdminTopicDto.TranslationDto> translationDtos = entity.getTranslations() != null
                ? entity.getTranslations().stream()
                .map(this::toAdminTranslationDto)
                .collect(Collectors.toList())
                : List.of();

        return AdminTopicDto.builder()
                .id(entity.getId())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categorySlug(entity.getCategory() != null ? entity.getCategory().getSlug() : null)
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

    private AdminTopicDto.TranslationDto toAdminTranslationDto(TopicTranslationEntity translation) {
        return AdminTopicDto.TranslationDto.builder()
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

    private TopicTranslationEntity resolveTranslation(TopicEntity entity, Long languageId, Long defaultLangId) {
        if (entity.getTranslations() == null || entity.getTranslations().isEmpty()) {
            return null;
        }
        return entity.getTranslations().stream()
                .filter(t -> t.getLanguage().getId().equals(languageId))
                .findFirst()
                .orElseGet(() -> entity.getTranslations().stream()
                        .filter(t -> t.getLanguage().getId().equals(defaultLangId))
                        .findFirst()
                        .orElse(entity.getTranslations().get(0)));
    }
}

