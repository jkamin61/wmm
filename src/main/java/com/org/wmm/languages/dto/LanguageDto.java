package com.org.wmm.languages.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Active language available in the system")
public class LanguageDto {

    @Schema(description = "Language ID", example = "1")
    private Long id;

    @Schema(description = "ISO language code", example = "pl")
    private String code;

    @Schema(description = "Language name in English", example = "Polish")
    private String name;

    @Schema(description = "Language name in its native form", example = "Polski")
    private String nativeName;

    @Schema(description = "Whether this is the system default language", example = "true")
    private boolean isDefault;
}
