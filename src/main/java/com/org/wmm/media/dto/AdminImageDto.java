package com.org.wmm.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Image details for admin panel")
public class AdminImageDto {

    @Schema(description = "Image ID", example = "1")
    private Long id;

    @Schema(description = "Item ID", example = "1")
    private Long itemId;

    @Schema(description = "Relative file path", example = "items/1/abc123.jpg")
    private String filePath;

    @Schema(description = "Original file name", example = "talisker-bottle.jpg")
    private String fileName;

    @Schema(description = "Public URL", example = "http://localhost:8080/media/items/1/abc123.jpg")
    private String url;

    @Schema(description = "File size in bytes", example = "245760")
    private Long fileSizeBytes;

    @Schema(description = "MIME type", example = "image/jpeg")
    private String mimeType;

    @Schema(description = "Image width (px)", example = "1200")
    private Integer width;

    @Schema(description = "Image height (px)", example = "800")
    private Integer height;

    @Schema(description = "Whether this is the primary image", example = "true")
    private Boolean isPrimary;

    @Schema(description = "Display order", example = "0")
    private Integer displayOrder;

    @Schema(description = "Creation timestamp")
    private OffsetDateTime createdAt;

    @Schema(description = "All translations (alt/caption per language)")
    private List<TranslationDto> translations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Image translation (alt text + caption)")
    public static class TranslationDto {

        @Schema(description = "Translation ID", example = "1")
        private Long id;

        @Schema(description = "Language code", example = "pl")
        private String languageCode;

        @Schema(description = "Language name", example = "Polish")
        private String languageName;

        @Schema(description = "Alt text for accessibility", example = "Butelka Talisker 10")
        private String altText;

        @Schema(description = "Image caption", example = "Talisker 10 Year Old — butelka 700ml")
        private String caption;
    }
}

