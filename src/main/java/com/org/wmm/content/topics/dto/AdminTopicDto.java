package com.org.wmm.content.topics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin view of a topic with all translations")
public class AdminTopicDto {

    @Schema(description = "Topic ID", example = "1")
    private Long id;

    @Schema(description = "Parent category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Parent category slug", example = "whisky")
    private String categorySlug;

    @Schema(description = "URL-friendly identifier", example = "single-malt")
    private String slug;

    @Schema(description = "Icon name", example = "glass")
    private String icon;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Whether the topic is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Content status", example = "draft")
    private String status;

    @Schema(description = "When the topic was published")
    private OffsetDateTime publishedAt;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private OffsetDateTime updatedAt;

    @Schema(description = "All translations for this topic")
    private List<TranslationDto> translations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Topic translation details")
    public static class TranslationDto {

        @Schema(description = "Translation ID", example = "1")
        private Long id;

        @Schema(description = "Language code", example = "pl")
        private String languageCode;

        @Schema(description = "Language name", example = "Polish")
        private String languageName;

        @Schema(description = "Translated title", example = "Single Malt")
        private String title;

        @Schema(description = "Translated subtitle")
        private String subtitle;

        @Schema(description = "Translated description")
        private String description;

        @Schema(description = "SEO meta title")
        private String metaTitle;

        @Schema(description = "SEO meta description")
        private String metaDescription;
    }
}

