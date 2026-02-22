package com.org.wmm.tasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to set the full flavor profile (aroma, taste, finish)")
public class UpdateFlavorProfileRequest {

    @Valid
    @Schema(description = "Aroma flavors (replaces existing). Empty list = clear section.")
    private List<FlavorProfileEntry> aroma;

    @Valid
    @Schema(description = "Taste flavors (replaces existing). Empty list = clear section.")
    private List<FlavorProfileEntry> taste;

    @Valid
    @Schema(description = "Finish flavors (replaces existing). Empty list = clear section.")
    private List<FlavorProfileEntry> finish;
}

