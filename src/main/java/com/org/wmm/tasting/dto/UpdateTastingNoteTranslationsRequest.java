package com.org.wmm.tasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update tasting note translations")
public class UpdateTastingNoteTranslationsRequest {

    @NotEmpty(message = "At least one translation is required")
    @Valid
    @Schema(description = "Translations to upsert")
    private List<TastingNoteTranslationRequest> translations;
}

