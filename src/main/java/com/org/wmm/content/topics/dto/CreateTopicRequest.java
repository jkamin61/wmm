package com.org.wmm.content.topics.dto;

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

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new topic")
public class CreateTopicRequest {

    @NotNull(message = "Category ID is required")
    @Schema(description = "Parent category ID", example = "1")
    private Long categoryId;

    @NotBlank(message = "Slug is required")
    @Size(max = 150)
    @Schema(description = "URL-friendly identifier (unique within category)", example = "single-malt")
    private String slug;

    @Size(max = 100)
    @Schema(description = "Icon name", example = "glass")
    private String icon;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @NotEmpty(message = "At least one translation is required")
    @Valid
    @Schema(description = "Translations for the topic")
    private List<TranslationRequest> translations;
}


