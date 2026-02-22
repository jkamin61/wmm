package com.org.wmm.tasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tasting note with scores, translations, and flavor profile")
public class TastingNoteDto {

    @Schema(description = "Tasting note ID")
    private Long id;

    @Schema(description = "Item ID")
    private Long itemId;

    @Schema(description = "Overall score (0–100)", example = "87.50")
    private BigDecimal overallScore;

    @Schema(description = "Aroma score (0–100)")
    private BigDecimal aromaScore;

    @Schema(description = "Taste score (0–100)")
    private BigDecimal tasteScore;

    @Schema(description = "Finish score (0–100)")
    private BigDecimal finishScore;

    @Schema(description = "Overall intensity (1–3)")
    private Short intensity;

    @Schema(description = "Date of tasting")
    private LocalDate tastingDate;

    @Schema(description = "Tasted by")
    private String tastedBy;

    @Schema(description = "Created at")
    private OffsetDateTime createdAt;

    @Schema(description = "Updated at")
    private OffsetDateTime updatedAt;

    @Schema(description = "Translations (notes per language)")
    private List<TranslationDto> translations;

    @Schema(description = "Flavor profile")
    private FlavorProfileDto flavorProfile;

    // ─── NESTED DTOs ──────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tasting note translation")
    public static class TranslationDto {
        private Long id;
        private String languageCode;
        private String languageName;
        private String aromaNotes;
        private String tasteNotes;
        private String finishNotes;
        private String overallImpression;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Flavor profile (aroma, taste, finish)")
    public static class FlavorProfileDto {
        private List<FlavorEntryDto> aroma;
        private List<FlavorEntryDto> taste;
        private List<FlavorEntryDto> finish;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Single flavor entry in the profile")
    public static class FlavorEntryDto {
        @Schema(description = "Flavor ID")
        private Long flavorId;
        @Schema(description = "Flavor slug", example = "smoke")
        private String flavorSlug;
        @Schema(description = "Flavor color (hex)", example = "#808080")
        private String flavorColor;
        @Schema(description = "Flavor icon")
        private String flavorIcon;
        @Schema(description = "Intensity (1–3)")
        private Short intensity;
        @Schema(description = "Display order")
        private Integer displayOrder;
    }
}

