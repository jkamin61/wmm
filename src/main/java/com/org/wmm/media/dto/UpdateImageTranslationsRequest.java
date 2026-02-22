package com.org.wmm.media.dto;

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
@Schema(description = "Request to upsert image translations (alt/caption per language)")
public class UpdateImageTranslationsRequest {

    @NotEmpty(message = "At least one translation is required")
    @Valid
    @Schema(description = "Translations to set (upsert per language)")
    private List<ImageTranslationRequest> translations;
}

