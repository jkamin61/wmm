package com.org.wmm.content.items.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Full detail DTO for single item view (includes description, images, tasting note).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full item details including images and tasting note")
public class ItemDetailDto {

    @Schema(description = "Item ID", example = "1")
    private Long id;

    @Schema(description = "URL-friendly identifier", example = "talisker-10")
    private String slug;

    // Translation
    @Schema(description = "Translated title", example = "Talisker 10 Year Old")
    private String title;

    @Schema(description = "Translated subtitle", example = "The Classic Isle of Skye")
    private String subtitle;

    @Schema(description = "Translated short excerpt", example = "Maritime whisky with intense smoke and pepper.")
    private String excerpt;

    @Schema(description = "Translated full description (may contain HTML)", example = "<p>Talisker 10 is the flagship whisky…</p>")
    private String description;

    @Schema(description = "SEO meta title", example = "Talisker 10 — Review and Rating")
    private String metaTitle;

    @Schema(description = "SEO meta description", example = "Talisker 10 Year Old review — maritime, smoky whisky from Isle of Skye.")
    private String metaDescription;

    // Product attributes
    @Schema(description = "Alcohol by volume (%)", example = "45.80")
    private BigDecimal abv;

    @Schema(description = "Vintage year", example = "2018")
    private Integer vintage;

    @Schema(description = "Volume in millilitres", example = "700")
    private Integer volumeMl;

    @Schema(description = "Price in PLN", example = "189.99")
    private BigDecimal pricePln;

    @Schema(description = "Whether this item is featured", example = "true")
    private boolean featured;

    @Schema(description = "Publication date", example = "2026-02-20T10:00:00+01:00")
    private OffsetDateTime publishedAt;

    // Category / Topic context
    @Schema(description = "Parent category slug", example = "whisky")
    private String categorySlug;

    @Schema(description = "Translated parent category title", example = "Whisky")
    private String categoryTitle;

    @Schema(description = "Parent topic slug", example = "single-malt")
    private String topicSlug;

    @Schema(description = "Translated parent topic title", example = "Single Malt")
    private String topicTitle;

    @Schema(description = "Parent subtopic slug (may be null)", example = "islay")
    private String subtopicSlug;

    @Schema(description = "Translated parent subtopic title (may be null)", example = "Islay")
    private String subtopicTitle;

    // Images
    @Schema(description = "Item images ordered by primary first, then display_order")
    private List<ImageDto> images;

    // Tasting note (optional)
    @Schema(description = "Tasting note with scores and translated notes (null if not reviewed)")
    private TastingNoteDto tastingNote;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Image with translated alt text and caption")
    public static class ImageDto {

        @Schema(description = "Image ID", example = "1")
        private Long id;

        @Schema(description = "File path", example = "/uploads/items/talisker-10/main.jpg")
        private String filePath;

        @Schema(description = "Original file name", example = "main.jpg")
        private String fileName;

        @Schema(description = "Whether this is the primary (hero) image", example = "true")
        private boolean primary;

        @Schema(description = "Display order in the gallery", example = "0")
        private Integer displayOrder;

        @Schema(description = "Translated alt text for accessibility", example = "Talisker 10 bottle")
        private String altText;

        @Schema(description = "Translated image caption", example = "Talisker 10 — bottle and packaging")
        private String caption;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tasting note with scores and sensory notes")
    public static class TastingNoteDto {

        @Schema(description = "Overall score (0–100)", example = "88.50")
        private BigDecimal overallScore;

        @Schema(description = "Aroma score (0–100)", example = "87.00")
        private BigDecimal aromaScore;

        @Schema(description = "Taste score (0–100)", example = "89.00")
        private BigDecimal tasteScore;

        @Schema(description = "Finish score (0–100)", example = "90.00")
        private BigDecimal finishScore;

        @Schema(description = "Intensity (1 = light, 2 = medium, 3 = bold)", example = "2")
        private Short intensity;

        @Schema(description = "Date when the tasting took place", example = "2026-02-15")
        private LocalDate tastingDate;

        @Schema(description = "Name of the person who tasted", example = "Admin")
        private String tastedBy;

        // Translation
        @Schema(description = "Translated aroma tasting notes", example = "Sea breeze, peat smoke, citrus, black pepper.")
        private String aromaNotes;

        @Schema(description = "Translated taste tasting notes", example = "Intense smoke, sweet honey, pepper, sea salt.")
        private String tasteNotes;

        @Schema(description = "Translated finish tasting notes", example = "Long, warm, with smoke and pepper.")
        private String finishNotes;

        @Schema(description = "Translated overall impression", example = "A classic island whisky — bold and unforgettable.")
        private String overallImpression;
    }
}
