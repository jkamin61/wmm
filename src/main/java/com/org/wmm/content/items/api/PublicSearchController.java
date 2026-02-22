package com.org.wmm.content.items.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.common.dto.PageResponse;
import com.org.wmm.content.items.dto.ItemSummaryDto;
import com.org.wmm.content.items.service.ItemSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Tag(name = "Public — Search", description = "Full-text search and filtered browsing of published items")
public class PublicSearchController {

    private final ItemSearchService itemSearchService;

    @Operation(
            summary = "Search and filter published items",
            description = """
                    Full-text search (PostgreSQL FTS) on item titles and descriptions, 
                    with optional filters for category, topic, subtopic, featured, score range, and flavors.
                    
                    **If `q` is omitted or blank**, the endpoint works as a filtered browse (no FTS ranking).
                    
                    **Flavor filter**: pass one or more flavor slugs (e.g. `flavors=smoke&flavors=peat`). 
                    Items are matched if they have at least one of the specified flavors in any section (aroma, taste, or finish).
                    
                    Results are sorted by FTS relevance (when `q` is provided), then by `published_at DESC`.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Paginated search results (may be empty)")
    @SecurityRequirement(name = "")
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<PageResponse<ItemSummaryDto>>> search(
            @Parameter(description = "Search query (full-text)", example = "talisker")
            @RequestParam(required = false) String q,

            @Parameter(description = "Language code (e.g. `pl`, `en`). Falls back to default if missing.")
            @RequestParam(required = false) String lang,

            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Filter by topic ID")
            @RequestParam(required = false) Long topicId,

            @Parameter(description = "Filter by subtopic ID")
            @RequestParam(required = false) Long subtopicId,

            @Parameter(description = "Filter by featured items only")
            @RequestParam(required = false) Boolean featured,

            @Parameter(description = "Minimum overall tasting score (0–100)", example = "80")
            @RequestParam(required = false) BigDecimal scoreMin,

            @Parameter(description = "Maximum overall tasting score (0–100)", example = "95")
            @RequestParam(required = false) BigDecimal scoreMax,

            @Parameter(description = "Filter by flavor slugs (items with any of these flavors in aroma/taste/finish)",
                    example = "smoke")
            @RequestParam(required = false) List<String> flavors,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (max 100)", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<ItemSummaryDto> result = itemSearchService.search(
                q, lang, categoryId, topicId, subtopicId,
                featured, scoreMin, scoreMax, flavors, page, size);
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}

