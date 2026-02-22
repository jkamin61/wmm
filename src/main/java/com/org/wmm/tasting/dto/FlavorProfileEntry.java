package com.org.wmm.tasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
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
@Schema(description = "Single flavor entry in a flavor profile section")
public class FlavorProfileEntry {

    @NotNull(message = "Flavor ID is required")
    @Schema(description = "Flavor ID", example = "1")
    private Long flavorId;

    @NotNull(message = "Intensity is required")
    @Min(value = 1, message = "Intensity must be between 1 and 3")
    @Max(value = 3, message = "Intensity must be between 1 and 3")
    @Schema(description = "Intensity (1=subtle, 2=moderate, 3=dominant)", example = "2")
    private Short intensity;

    @NotNull(message = "Display order is required")
    @Min(value = 0)
    @Schema(description = "Display order within section", example = "0")
    private Integer order;
}

