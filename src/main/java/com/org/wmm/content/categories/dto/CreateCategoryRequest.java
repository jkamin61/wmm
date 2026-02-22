package com.org.wmm.content.categories.dto;

import com.org.wmm.common.dto.TranslationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new category")
public class CreateCategoryRequest {

    @NotBlank(message = "Slug is required")
    @Size(max = 150)
    @Schema(description = "URL-friendly identifier (must be unique)", example = "whisky")
    private String slug;

    @Size(max = 100)
    @Schema(description = "Icon name", example = "whiskey-glass")
    private String icon;

    @Schema(description = "Display order in menu", example = "1")
    private Integer displayOrder;

    @NotEmpty(message = "At least one translation is required")
    @Valid
    @Schema(description = "Translations for the category (at least default language)")
    private List<TranslationRequest> translations;
}


