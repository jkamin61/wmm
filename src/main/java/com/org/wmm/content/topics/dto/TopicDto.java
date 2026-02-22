package com.org.wmm.content.topics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Topic with translated content")
public class TopicDto {

    @Schema(description = "Topic ID", example = "1")
    private Long id;

    @Schema(description = "URL-friendly identifier", example = "single-malt")
    private String slug;

    @Schema(description = "Icon name", example = "glass")
    private String icon;

    @Schema(description = "Translated title", example = "Single Malt")
    private String title;

    @Schema(description = "Translated subtitle", example = "Scottish single malt whisky")
    private String subtitle;

    @Schema(description = "Translated description", example = "Whisky produced at a single distillery from malted barley.")
    private String description;

    @Schema(description = "SEO meta title", example = "Single Malt Whisky — Reviews")
    private String metaTitle;

    @Schema(description = "SEO meta description", example = "Best single malt whisky reviews and ratings.")
    private String metaDescription;
}
