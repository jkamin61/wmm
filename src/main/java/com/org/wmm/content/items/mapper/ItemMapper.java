package com.org.wmm.content.items.mapper;

import com.org.wmm.content.categories.entity.CategoryEntity;
import com.org.wmm.content.categories.entity.CategoryTranslationEntity;
import com.org.wmm.content.items.dto.ItemDetailDto;
import com.org.wmm.content.items.dto.ItemSummaryDto;
import com.org.wmm.content.items.entity.*;
import com.org.wmm.content.subtopics.entity.SubtopicEntity;
import com.org.wmm.content.subtopics.entity.SubtopicTranslationEntity;
import com.org.wmm.content.topics.entity.TopicEntity;
import com.org.wmm.content.topics.entity.TopicTranslationEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemMapper {

    public ItemSummaryDto toSummaryDto(ItemEntity entity, Long languageId, Long defaultLangId) {
        ItemTranslationEntity translation = resolveTranslation(entity.getTranslations(), languageId, defaultLangId);

        String primaryImagePath = null;
        if (entity.getImages() != null) {
            primaryImagePath = entity.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .map(ImageEntity::getFilePath)
                    .findFirst()
                    .orElse(entity.getImages().isEmpty() ? null : entity.getImages().get(0).getFilePath());
        }

        return ItemSummaryDto.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .title(translation != null ? translation.getTitle() : entity.getSlug())
                .subtitle(translation != null ? translation.getSubtitle() : null)
                .excerpt(translation != null ? translation.getExcerpt() : null)
                .abv(entity.getAbv())
                .vintage(entity.getVintage())
                .featured(Boolean.TRUE.equals(entity.getIsFeatured()))
                .publishedAt(entity.getPublishedAt())
                .primaryImagePath(primaryImagePath)
                .overallScore(entity.getTastingNote() != null ? entity.getTastingNote().getOverallScore() : null)
                .build();
    }

    public ItemDetailDto toDetailDto(ItemEntity entity, Long languageId, Long defaultLangId) {
        ItemTranslationEntity translation = resolveTranslation(entity.getTranslations(), languageId, defaultLangId);

        ItemDetailDto dto = new ItemDetailDto();
        dto.setId(entity.getId());
        dto.setSlug(entity.getSlug());
        dto.setTitle(translation != null ? translation.getTitle() : entity.getSlug());
        dto.setSubtitle(translation != null ? translation.getSubtitle() : null);
        dto.setExcerpt(translation != null ? translation.getExcerpt() : null);
        dto.setDescription(translation != null ? translation.getDescription() : null);
        dto.setMetaTitle(translation != null ? translation.getMetaTitle() : null);
        dto.setMetaDescription(translation != null ? translation.getMetaDescription() : null);
        dto.setAbv(entity.getAbv());
        dto.setVintage(entity.getVintage());
        dto.setVolumeMl(entity.getVolumeMl());
        dto.setPricePln(entity.getPricePln());
        dto.setFeatured(Boolean.TRUE.equals(entity.getIsFeatured()));
        dto.setPublishedAt(entity.getPublishedAt());

        // Category context
        CategoryEntity category = entity.getCategory();
        if (category != null) {
            dto.setCategorySlug(category.getSlug());
            CategoryTranslationEntity catTrans = resolveCategoryTranslation(category, languageId, defaultLangId);
            dto.setCategoryTitle(catTrans != null ? catTrans.getTitle() : category.getSlug());
        }

        // Topic context
        TopicEntity topic = entity.getTopic();
        if (topic != null) {
            dto.setTopicSlug(topic.getSlug());
            TopicTranslationEntity topicTrans = resolveTopicTranslation(topic, languageId, defaultLangId);
            dto.setTopicTitle(topicTrans != null ? topicTrans.getTitle() : topic.getSlug());
        }

        // Subtopic context
        SubtopicEntity subtopic = entity.getSubtopic();
        if (subtopic != null) {
            dto.setSubtopicSlug(subtopic.getSlug());
            SubtopicTranslationEntity subTrans = resolveSubtopicTranslation(subtopic, languageId, defaultLangId);
            dto.setSubtopicTitle(subTrans != null ? subTrans.getTitle() : subtopic.getSlug());
        }

        // Images
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            List<ItemDetailDto.ImageDto> imageDtos = entity.getImages().stream()
                    .sorted(Comparator.comparing((ImageEntity img) -> !Boolean.TRUE.equals(img.getIsPrimary()))
                            .thenComparing(ImageEntity::getDisplayOrder))
                    .map(img -> mapImage(img, languageId, defaultLangId))
                    .collect(Collectors.toList());
            dto.setImages(imageDtos);
        }

        // Tasting note
        TastingNoteEntity tastingNote = entity.getTastingNote();
        if (tastingNote != null) {
            dto.setTastingNote(mapTastingNote(tastingNote, languageId, defaultLangId));
        }

        return dto;
    }

    private ItemDetailDto.ImageDto mapImage(ImageEntity img, Long languageId, Long defaultLangId) {
        ImageTranslationEntity imgTrans = null;
        if (img.getImageTranslations() != null && !img.getImageTranslations().isEmpty()) {
            imgTrans = img.getImageTranslations().stream()
                    .filter(t -> t.getLanguage().getId().equals(languageId))
                    .findFirst()
                    .orElseGet(() -> img.getImageTranslations().stream()
                            .filter(t -> t.getLanguage().getId().equals(defaultLangId))
                            .findFirst()
                            .orElse(null));
        }

        return ItemDetailDto.ImageDto.builder()
                .id(img.getId())
                .filePath(img.getFilePath())
                .fileName(img.getFileName())
                .primary(Boolean.TRUE.equals(img.getIsPrimary()))
                .displayOrder(img.getDisplayOrder())
                .altText(imgTrans != null ? imgTrans.getAltText() : null)
                .caption(imgTrans != null ? imgTrans.getCaption() : null)
                .build();
    }

    private ItemDetailDto.TastingNoteDto mapTastingNote(TastingNoteEntity note, Long languageId, Long defaultLangId) {
        TastingNoteTranslationEntity noteTrans = null;
        if (note.getTranslations() != null && !note.getTranslations().isEmpty()) {
            noteTrans = note.getTranslations().stream()
                    .filter(t -> t.getLanguage().getId().equals(languageId))
                    .findFirst()
                    .orElseGet(() -> note.getTranslations().stream()
                            .filter(t -> t.getLanguage().getId().equals(defaultLangId))
                            .findFirst()
                            .orElse(null));
        }

        return ItemDetailDto.TastingNoteDto.builder()
                .overallScore(note.getOverallScore())
                .aromaScore(note.getAromaScore())
                .tasteScore(note.getTasteScore())
                .finishScore(note.getFinishScore())
                .intensity(note.getIntensity())
                .tastingDate(note.getTastingDate())
                .tastedBy(note.getTastedBy())
                .aromaNotes(noteTrans != null ? noteTrans.getAromaNotes() : null)
                .tasteNotes(noteTrans != null ? noteTrans.getTasteNotes() : null)
                .finishNotes(noteTrans != null ? noteTrans.getFinishNotes() : null)
                .overallImpression(noteTrans != null ? noteTrans.getOverallImpression() : null)
                .build();
    }

    // --- Translation resolvers ---

    private ItemTranslationEntity resolveTranslation(List<ItemTranslationEntity> translations,
                                                     Long languageId, Long defaultLangId) {
        if (translations == null || translations.isEmpty()) return null;
        return translations.stream()
                .filter(t -> t.getLanguage().getId().equals(languageId))
                .findFirst()
                .orElseGet(() -> translations.stream()
                        .filter(t -> t.getLanguage().getId().equals(defaultLangId))
                        .findFirst()
                        .orElse(translations.get(0)));
    }

    private CategoryTranslationEntity resolveCategoryTranslation(CategoryEntity cat,
                                                                 Long languageId, Long defaultLangId) {
        if (cat.getTranslations() == null || cat.getTranslations().isEmpty()) return null;
        return cat.getTranslations().stream()
                .filter(t -> t.getLanguage().getId().equals(languageId))
                .findFirst()
                .orElseGet(() -> cat.getTranslations().stream()
                        .filter(t -> t.getLanguage().getId().equals(defaultLangId))
                        .findFirst()
                        .orElse(null));
    }

    private TopicTranslationEntity resolveTopicTranslation(TopicEntity topic,
                                                           Long languageId, Long defaultLangId) {
        if (topic.getTranslations() == null || topic.getTranslations().isEmpty()) return null;
        return topic.getTranslations().stream()
                .filter(t -> t.getLanguage().getId().equals(languageId))
                .findFirst()
                .orElseGet(() -> topic.getTranslations().stream()
                        .filter(t -> t.getLanguage().getId().equals(defaultLangId))
                        .findFirst()
                        .orElse(null));
    }

    private SubtopicTranslationEntity resolveSubtopicTranslation(SubtopicEntity sub,
                                                                 Long languageId, Long defaultLangId) {
        if (sub.getTranslations() == null || sub.getTranslations().isEmpty()) return null;
        return sub.getTranslations().stream()
                .filter(t -> t.getLanguage().getId().equals(languageId))
                .findFirst()
                .orElseGet(() -> sub.getTranslations().stream()
                        .filter(t -> t.getLanguage().getId().equals(defaultLangId))
                        .findFirst()
                        .orElse(null));
    }
}


