package com.org.wmm.content.items.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.common.dto.PageResponse;
import com.org.wmm.content.items.dto.ItemDetailDto;
import com.org.wmm.content.items.dto.ItemSummaryDto;
import com.org.wmm.content.items.service.ItemQueryService;
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

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Tag(name = "Public — Items", description = "Published item listings and details")
public class PublicItemController {

    private final ItemQueryService itemQueryService;

    @Operation(
            summary = "List items by topic",
            description = "Returns a paginated list of published items for a given topic slug. " +
                    "Supports sorting by `newest` (default), `oldest`, or `score`. " +
                    "Optionally filter by `featured=true`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of items (may be empty)"),
            @ApiResponse(responseCode = "404", description = "Topic not found or inactive",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @SecurityRequirement(name = "")
    @GetMapping("/topics/{topicSlug}/items")
    public ResponseEntity<BaseResponse<PageResponse<ItemSummaryDto>>> getItemsByTopic(
            @Parameter(description = "Topic slug", example = "single-malt", required = true)
            @PathVariable String topicSlug,
            @Parameter(description = "Language code (e.g. `pl`, `en`). Falls back to default if missing or invalid.")
            @RequestParam(required = false) String lang,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort order: `newest` (default), `oldest`, `score`", example = "newest",
                    schema = @Schema(allowableValues = {"newest", "oldest", "score"}))
            @RequestParam(defaultValue = "newest") String sort,
            @Parameter(description = "Filter by featured items only")
            @RequestParam(required = false) Boolean featured
    ) {
        PageResponse<ItemSummaryDto> result = itemQueryService.getItemsByTopicSlug(
                topicSlug, null, featured, lang, page, size, sort);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "Get item details",
            description = "Returns full details of a published item by slug, " +
                    "including translated content, images (primary first), " +
                    "category/topic/subtopic context, and tasting note with scores."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item details"),
            @ApiResponse(responseCode = "404", description = "Item not found, not published, or archived",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @SecurityRequirement(name = "")
    @GetMapping("/items/{slug}")
    public ResponseEntity<BaseResponse<ItemDetailDto>> getItemBySlug(
            @Parameter(description = "Item slug", example = "talisker-10", required = true)
            @PathVariable String slug,
            @Parameter(description = "Language code (e.g. `pl`, `en`). Falls back to default if missing or invalid.")
            @RequestParam(required = false) String lang
    ) {
        ItemDetailDto item = itemQueryService.getItemBySlug(slug, lang);
        return ResponseEntity.ok(BaseResponse.success(item));
    }
}
