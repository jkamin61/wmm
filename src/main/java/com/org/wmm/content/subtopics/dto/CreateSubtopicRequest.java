package com.org.wmm.content.subtopics.dto;

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
@Schema(description = "Request to create a new subtopic")
public class CreateSubtopicRequest {

    @NotNull(message = "Topic ID is required")
    @Schema(description = "Parent topic ID", example = "1")
    private Long topicId;

    @NotBlank(message = "Slug is required")
    @Size(max = 150)
    @Schema(description = "URL-friendly identifier (unique within topic)", example = "islay")
    private String slug;

    @Size(max = 100)
    @Schema(description = "Icon name", example = "map-pin")
    private String icon;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @NotEmpty(message = "At least one translation is required")
    @Valid
    @Schema(description = "Translations for the subtopic")
    private List<TranslationRequest> translations;
}


