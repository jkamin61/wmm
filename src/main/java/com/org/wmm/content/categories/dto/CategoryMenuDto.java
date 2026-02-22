package com.org.wmm.content.categories.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category with its topics for the public menu")
public class CategoryMenuDto {

    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "URL-friendly identifier", example = "whisky")
    private String slug;

    @Schema(description = "Icon name", example = "whiskey-glass")
    private String icon;

    @Schema(description = "Translated category title", example = "Whisky")
    private String title;

    @Schema(description = "Translated category description", example = "Discover the world of whisky.")
    private String description;

    @Schema(description = "Published topics within this category")
    private List<TopicMenuDto> topics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Topic summary within the menu")
    public static class TopicMenuDto {

        @Schema(description = "Topic ID", example = "1")
        private Long id;

        @Schema(description = "URL-friendly identifier", example = "single-malt")
        private String slug;

        @Schema(description = "Icon name", example = "glass")
        private String icon;

        @Schema(description = "Translated topic title", example = "Single Malt")
        private String title;

        @Schema(description = "Translated topic subtitle", example = "Scottish single malt whisky")
        private String subtitle;
    }
}
