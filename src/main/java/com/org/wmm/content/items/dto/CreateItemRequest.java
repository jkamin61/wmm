package com.org.wmm.content.items.dto;

import com.org.wmm.common.dto.TranslationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new item")
public class CreateItemRequest {

    @NotNull(message = "Category ID is required")
    @Schema(description = "Parent category ID", example = "1")
    private Long categoryId;

    @NotNull(message = "Topic ID is required")
    @Schema(description = "Parent topic ID", example = "1")
    private Long topicId;

    @Schema(description = "Parent subtopic ID (optional)", example = "1")
    private Long subtopicId;

    @Schema(description = "Partner ID (optional)", example = "1")
    private Long partnerId;

    @NotBlank(message = "Slug is required")
    @Size(max = 200)
    @Schema(description = "URL-friendly identifier (must be unique)", example = "talisker-10")
    private String slug;

    @Schema(description = "Alcohol by volume (%)", example = "45.80")
    private BigDecimal abv;

    @Schema(description = "Vintage year", example = "2018")
    private Integer vintage;

    @Schema(description = "Volume in millilitres", example = "700")
    private Integer volumeMl;

    @Schema(description = "Price in PLN", example = "189.99")
    private BigDecimal pricePln;

    @Schema(description = "Whether this item is featured", example = "false")
    private Boolean isFeatured;

    @NotEmpty(message = "At least one translation is required")
    @Valid
    @Schema(description = "Translations for the item")
    private List<TranslationRequest> translations;
}


