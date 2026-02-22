package com.org.wmm.content.items.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Summary DTO for item listings (no full description, no tasting note).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item summary for listings (no full description or tasting note details)")
public class ItemSummaryDto {

    @Schema(description = "Item ID", example = "1")
    private Long id;

    @Schema(description = "URL-friendly identifier", example = "talisker-10")
    private String slug;

    @Schema(description = "Translated title", example = "Talisker 10 Year Old")
    private String title;

    @Schema(description = "Translated subtitle", example = "The Classic Isle of Skye")
    private String subtitle;

    @Schema(description = "Short translated excerpt for the listing", example = "Maritime whisky with intense smoke and pepper.")
    private String excerpt;

    @Schema(description = "Alcohol by volume (%)", example = "45.80")
    private BigDecimal abv;

    @Schema(description = "Vintage year", example = "2018")
    private Integer vintage;

    @Schema(description = "Whether this item is featured", example = "true")
    private boolean featured;

    @Schema(description = "Publication date", example = "2026-02-20T10:00:00+01:00")
    private OffsetDateTime publishedAt;

    @Schema(description = "Path to the primary image", example = "/uploads/items/talisker-10/main.jpg")
    private String primaryImagePath;

    @Schema(description = "Overall tasting score (0–100)", example = "88.50")
    private BigDecimal overallScore;
}
