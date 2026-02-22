package com.org.wmm.tasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create or update a tasting note for an item")
public class UpsertTastingNoteRequest {

    @DecimalMin(value = "0", message = "Overall score must be >= 0")
    @DecimalMax(value = "100", message = "Overall score must be <= 100")
    @Schema(description = "Overall score (0–100)", example = "87.50")
    private BigDecimal overallScore;

    @DecimalMin(value = "0", message = "Aroma score must be >= 0")
    @DecimalMax(value = "100", message = "Aroma score must be <= 100")
    @Schema(description = "Aroma score (0–100)", example = "88.00")
    private BigDecimal aromaScore;

    @DecimalMin(value = "0", message = "Taste score must be >= 0")
    @DecimalMax(value = "100", message = "Taste score must be <= 100")
    @Schema(description = "Taste score (0–100)", example = "86.00")
    private BigDecimal tasteScore;

    @DecimalMin(value = "0", message = "Finish score must be >= 0")
    @DecimalMax(value = "100", message = "Finish score must be <= 100")
    @Schema(description = "Finish score (0–100)", example = "85.00")
    private BigDecimal finishScore;

    @Min(value = 1, message = "Intensity must be between 1 and 3")
    @Max(value = 3, message = "Intensity must be between 1 and 3")
    @Schema(description = "Overall intensity (1=light, 2=medium, 3=bold)", example = "2")
    private Short intensity;

    @Schema(description = "Date of tasting", example = "2026-02-15")
    private LocalDate tastingDate;

    @Size(max = 150)
    @Schema(description = "Person who performed the tasting", example = "Jan Kowalski")
    private String tastedBy;
}

