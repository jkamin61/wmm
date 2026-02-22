package com.org.wmm.content.topics.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.content.topics.dto.TopicDto;
import com.org.wmm.content.topics.service.TopicQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/categories")
@RequiredArgsConstructor
@Tag(name = "Public — Topics", description = "Topics within a category")
public class PublicTopicController {

    private final TopicQueryService topicQueryService;

    @Operation(
            summary = "Get topics by category",
            description = "Returns active published topics for a given category slug, " +
                    "translated into the requested language. " +
                    "The category must exist and be active, otherwise 404 is returned."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of topics (may be empty if category has no published topics)"),
            @ApiResponse(responseCode = "404", description = "Category not found or inactive",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @SecurityRequirement(name = "")
    @GetMapping("/{categorySlug}/topics")
    public ResponseEntity<BaseResponse<List<TopicDto>>> getTopicsByCategory(
            @Parameter(description = "Category slug", example = "whisky", required = true)
            @PathVariable String categorySlug,
            @Parameter(description = "Language code (e.g. `pl`, `en`). Falls back to default if missing or invalid.")
            @RequestParam(required = false) String lang
    ) {
        List<TopicDto> topics = topicQueryService.getTopicsByCategorySlug(categorySlug, lang);
        return ResponseEntity.ok(BaseResponse.success(topics));
    }
}
