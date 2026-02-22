package com.org.wmm.tasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tasting note translation input (notes per language)")
public class TastingNoteTranslationRequest {

    @NotBlank(message = "Language code is required")
    @Size(max = 10)
    @Schema(description = "Language code", example = "pl")
    private String languageCode;

    @Schema(description = "Aroma notes description", example = "Intensywny dym, morskie powietrze, miód")
    private String aromaNotes;

    @Schema(description = "Taste notes description", example = "Pieprz, słodowy, cytrusy")
    private String tasteNotes;

    @Schema(description = "Finish notes description", example = "Długi, dymny, lekko słony")
    private String finishNotes;

    @Schema(description = "Overall impression", example = "Kompleksowa whisky z charakterem Isle of Skye")
    private String overallImpression;
}

