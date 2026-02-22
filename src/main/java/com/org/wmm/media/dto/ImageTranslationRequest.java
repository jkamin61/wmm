package com.org.wmm.media.dto;

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
@Schema(description = "Image translation input (alt text + caption per language)")
public class ImageTranslationRequest {

    @NotBlank(message = "Language code is required")
    @Size(max = 10)
    @Schema(description = "Language code", example = "pl")
    private String languageCode;

    @Size(max = 255)
    @Schema(description = "Alt text for accessibility", example = "Butelka Talisker 10")
    private String altText;

    @Schema(description = "Image caption", example = "Talisker 10 Year Old — butelka 700ml")
    private String caption;
}

