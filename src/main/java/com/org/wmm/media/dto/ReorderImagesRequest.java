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
@Schema(description = "Request to reorder images for an item")
public class ReorderImagesRequest {

    @NotEmpty(message = "Image IDs list cannot be empty")
    @Schema(description = "Ordered list of image IDs (new display order = list index)", example = "[3, 1, 2]")
    private List<Long> imageIds;
}

