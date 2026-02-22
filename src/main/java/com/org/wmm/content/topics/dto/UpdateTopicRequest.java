package com.org.wmm.content.topics.dto;

import com.org.wmm.common.dto.TranslationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Request to update an existing topic (all fields optional)")
public class UpdateTopicRequest {

    @Size(max = 150)
    @Schema(description = "New slug (unique within category)", example = "single-malt-updated")
    private String slug;

    @Size(max = 100)
    @Schema(description = "New icon name", example = "glass")
    private String icon;

    @Schema(description = "New display order", example = "2")
    private Integer displayOrder;

    @Schema(description = "Whether the topic is active", example = "true")
    private Boolean isActive;

    @Valid
    @Schema(description = "Updated translations")
    private List<TranslationRequest> translations;
}

