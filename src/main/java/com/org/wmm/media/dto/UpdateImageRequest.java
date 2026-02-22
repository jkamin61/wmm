package com.org.wmm.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update image metadata (display order, primary flag)")
public class UpdateImageRequest {

    @Schema(description = "New display order", example = "2")
    @Min(value = 0, message = "Display order must be >= 0")
    private Integer displayOrder;

    @Schema(description = "Whether this image should be the primary image", example = "true")
    private Boolean isPrimary;
}

