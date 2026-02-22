package com.org.wmm.content.items.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin view of an item with all translations and metadata")
public class AdminItemDto {

    @Schema(description = "Item ID", example = "1")
    private Long id;

    @Schema(description = "URL-friendly identifier", example = "talisker-10")
    private String slug;

    // Hierarchy context
    @Schema(description = "Parent category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Parent category slug", example = "whisky")
    private String categorySlug;

    @Schema(description = "Parent topic ID", example = "1")
    private Long topicId;

    @Schema(description = "Parent topic slug", example = "single-malt")
    private String topicSlug;

    @Schema(description = "Parent subtopic ID (may be null)", example = "1")
    private Long subtopicId;

    @Schema(description = "Parent subtopic slug (may be null)", example = "islay")
    private String subtopicSlug;

    @Schema(description = "Partner ID (may be null)", example = "1")
    private Long partnerId;

    // Product attributes
    @Schema(description = "ABV (%)", example = "45.80")
    private BigDecimal abv;

    @Schema(description = "Vintage year", example = "2018")
    private Integer vintage;

    @Schema(description = "Volume (ml)", example = "700")
    private Integer volumeMl;

    @Schema(description = "Price (PLN)", example = "189.99")
    private BigDecimal pricePln;

    @Schema(description = "Whether featured", example = "false")
    private Boolean isFeatured;

    // Status
    @Schema(description = "Content status", example = "draft")
    private String status;

    @Schema(description = "When the item was published")
    private OffsetDateTime publishedAt;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private OffsetDateTime updatedAt;

    // Translations
    @Schema(description = "All translations for this item")
    private List<TranslationDto> translations;

    // Images count (lightweight)
    @Schema(description = "Number of images attached", example = "3")
    private int imageCount;

    @Schema(description = "Whether a tasting note exists", example = "true")
    private boolean hasTastingNote;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Item translation details")
    public static class TranslationDto {

        @Schema(description = "Translation ID", example = "1")
        private Long id;

        @Schema(description = "Language code", example = "pl")
        private String languageCode;

        @Schema(description = "Language name", example = "Polish")
        private String languageName;

        @Schema(description = "Translated title", example = "Talisker 10 Year Old")
        private String title;

        @Schema(description = "Translated subtitle")
        private String subtitle;

        @Schema(description = "Translated excerpt")
        private String excerpt;

        @Schema(description = "Translated full description")
        private String description;

        @Schema(description = "SEO meta title")
        private String metaTitle;

        @Schema(description = "SEO meta description")
        private String metaDescription;

        @Schema(description = "SEO meta keywords")
        private String metaKeywords;
    }
}

