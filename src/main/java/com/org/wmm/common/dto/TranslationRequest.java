package com.org.wmm.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Translation input for content entities")
public class TranslationRequest {

    @NotBlank(message = "Language code is required")
    @Size(max = 10)
    @Schema(description = "Language code", example = "pl")
    private String languageCode;

    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Schema(description = "Translated title", example = "Whisky")
    private String title;

    @Size(max = 255)
    @Schema(description = "Translated subtitle", example = "Najlepsza whisky")
    private String subtitle;

    @Schema(description = "Translated description")
    private String description;

    @Schema(description = "Translated excerpt (short description)")
    private String excerpt;

    @Size(max = 255)
    @Schema(description = "SEO meta title", example = "Whisky — WilliamMacMiron")
    private String metaTitle;

    @Size(max = 500)
    @Schema(description = "SEO meta description", example = "Discover the world of whisky.")
    private String metaDescription;

    @Size(max = 500)
    @Schema(description = "SEO meta keywords", example = "whisky, single malt, scotch")
    private String metaKeywords;
}


