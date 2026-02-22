package com.org.wmm.content.categories.dto;

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
@Schema(description = "Request to update an existing category (all fields optional)")
public class UpdateCategoryRequest {

    @Size(max = 150)
    @Schema(description = "New slug (must be unique)", example = "whisky-updated")
    private String slug;

    @Size(max = 100)
    @Schema(description = "New icon name", example = "whiskey-glass")
    private String icon;

    @Schema(description = "New display order", example = "2")
    private Integer displayOrder;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean isActive;

    @Valid
    @Schema(description = "Updated translations (replaces existing for given languages)")
    private List<TranslationRequest> translations;
}

