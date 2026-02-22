package com.org.wmm.content.categories.mapper;

import com.org.wmm.content.categories.dto.AdminCategoryDto;
import com.org.wmm.content.categories.dto.CategoryMenuDto;
import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.entity.CategoryTranslationEntity;
import com.org.wmm.content.topics.entity.TopicEntity;
import com.org.wmm.content.topics.entity.TopicTranslationEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    /**
     * Maps a CategoryEntity to CategoryMenuDto with translation resolution.
     *
     * @param entity        the category entity (with translations loaded)
     * @param languageId    the requested language ID
     * @param defaultLangId the default language ID (fallback)
     * @param topics        active topics belonging to this category (with translations loaded)
     */
    public CategoryMenuDto toMenuDto(CategoryEntity entity, Long languageId, Long defaultLangId,
                                     List<TopicEntity> topics) {
        CategoryTranslationEntity translation = resolveTranslation(entity, languageId, defaultLangId);

        List<CategoryMenuDto.TopicMenuDto> topicDtos = topics != null
                ? topics.stream()
                .map(t -> toTopicMenuDto(t, languageId, defaultLangId))
                .collect(Collectors.toList())
                : List.of();

        return CategoryMenuDto.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .icon(entity.getIcon())
                .title(translation != null ? translation.getTitle() : entity.getSlug())
                .description(translation != null ? translation.getDescription() : null)
                .topics(topicDtos)
                .build();
    }

    private CategoryMenuDto.TopicMenuDto toTopicMenuDto(TopicEntity topic, Long languageId, Long defaultLangId) {
        TopicTranslationEntity translation = resolveTopicTranslation(topic, languageId, defaultLangId);

        return CategoryMenuDto.TopicMenuDto.builder()
                .id(topic.getId())
                .slug(topic.getSlug())
                .icon(topic.getIcon())
                .title(translation != null ? translation.getTitle() : topic.getSlug())
                .subtitle(translation != null ? translation.getSubtitle() : null)
                .build();
    }

    private CategoryTranslationEntity resolveTranslation(CategoryEntity entity, Long languageId, Long defaultLangId) {
        if (entity.getTranslations() == null || entity.getTranslations().isEmpty()) {
            return null;
        }
        // Try requested language first
        return entity.getTranslations().stream()
                .filter(t -> t.getLanguage().getId().equals(languageId))
                .findFirst()
                // Fallback to default language
                .orElseGet(() -> entity.getTranslations().stream()
                        .filter(t -> t.getLanguage().getId().equals(defaultLangId))
                        .findFirst()
                        .orElse(entity.getTranslations().get(0)));
    }

    private TopicTranslationEntity resolveTopicTranslation(TopicEntity entity, Long languageId, Long defaultLangId) {
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

    /**
     * Maps a CategoryEntity to AdminCategoryDto (includes all translations).
     */
    public AdminCategoryDto toAdminDto(CategoryEntity entity) {
        List<AdminCategoryDto.TranslationDto> translationDtos = entity.getTranslations() != null
                ? entity.getTranslations().stream()
                .map(this::toTranslationDto)
                .collect(Collectors.toList())
                : List.of();

        return AdminCategoryDto.builder()
                .id(entity.getId())
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

    private AdminCategoryDto.TranslationDto toTranslationDto(CategoryTranslationEntity translation) {
        return AdminCategoryDto.TranslationDto.builder()
                .id(translation.getId())
                .languageCode(translation.getLanguage().getCode())
                .languageName(translation.getLanguage().getName())
                .title(translation.getTitle())
                .description(translation.getDescription())
                .metaTitle(translation.getMetaTitle())
                .metaDescription(translation.getMetaDescription())
                .build();
    }
}

