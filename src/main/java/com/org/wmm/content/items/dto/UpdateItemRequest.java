package com.org.wmm.content.items.dto;

import com.org.wmm.common.dto.TranslationRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Request to update an existing item (all fields optional)")
public class UpdateItemRequest {

    @Size(max = 200)
    @Schema(description = "New slug (must be unique)", example = "talisker-10-updated")
    private String slug;

    @Schema(description = "New ABV (%)", example = "45.80")
    private BigDecimal abv;

    @Schema(description = "New vintage year", example = "2019")
    private Integer vintage;

    @Schema(description = "New volume (ml)", example = "700")
    private Integer volumeMl;

    @Schema(description = "New price (PLN)", example = "199.99")
    private BigDecimal pricePln;

    @Schema(description = "Whether this item is featured", example = "true")
    private Boolean isFeatured;

    @Schema(description = "New subtopic ID (set to null to remove)", example = "2")
    private Long subtopicId;

    @Schema(description = "New partner ID", example = "1")
    private Long partnerId;

    @Valid
    @Schema(description = "Updated translations")
    private List<TranslationRequest> translations;
}

