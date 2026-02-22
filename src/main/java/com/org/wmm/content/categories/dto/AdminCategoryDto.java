package com.org.wmm.content.categories.dto;

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
@Schema(description = "Admin view of a category with all translations")
public class AdminCategoryDto {

    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "URL-friendly identifier", example = "whisky")
    private String slug;

    @Schema(description = "Icon name", example = "whiskey-glass")
    private String icon;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Content status (draft/published/archived)", example = "draft")
    private String status;

    @Schema(description = "When the category was published", example = "2026-02-20T10:00:00+01:00")
    private OffsetDateTime publishedAt;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private OffsetDateTime updatedAt;

    @Schema(description = "All translations for this category")
    private List<TranslationDto> translations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Category translation details")
    public static class TranslationDto {

        @Schema(description = "Translation ID", example = "1")
        private Long id;

        @Schema(description = "Language code", example = "pl")
        private String languageCode;

        @Schema(description = "Language name", example = "Polish")
        private String languageName;

        @Schema(description = "Translated title", example = "Whisky")
        private String title;

        @Schema(description = "Translated description", example = "Świat whisky")
        private String description;

        @Schema(description = "SEO meta title")
        private String metaTitle;

        @Schema(description = "SEO meta description")
        private String metaDescription;
    }
}

