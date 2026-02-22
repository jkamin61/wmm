package com.org.wmm.content.topics.mapper;

import com.org.wmm.content.topics.dto.TopicDto;
import com.org.wmm.content.topics.entity.TopicEntity;
import com.org.wmm.content.topics.entity.TopicTranslationEntity;
import org.springframework.stereotype.Component;

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

