package com.org.wmm.content.items.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.common.dto.PageResponse;
import com.org.wmm.content.items.dto.AdminItemDto;
import com.org.wmm.content.items.dto.CreateItemRequest;
import com.org.wmm.content.items.dto.UpdateItemRequest;
import com.org.wmm.content.items.service.ItemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/items")
@RequiredArgsConstructor
@Tag(name = "Admin — Items", description = "CRUD operations for items with publish workflow (requires ADMIN or EDITOR role)")
@SecurityRequirement(name = "bearerAuth")
public class AdminItemController {

    private final ItemAdminService itemAdminService;

    @Operation(summary = "List items (paginated)",
            description = "Returns paginated items. Filter by status (draft/published/archived), topic ID, category ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of items"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<PageResponse<AdminItemDto>>> getAllItems(
            @Parameter(description = "Filter by status: draft, published, archived")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by topic ID")
            @RequestParam(required = false) Long topicId,
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminItemDto> itemPage = itemAdminService.getAllItems(status, topicId, categoryId, page, size);

        PageResponse<AdminItemDto> response = PageResponse.<AdminItemDto>builder()
                .content(itemPage.getContent())
                .page(itemPage.getNumber())
                .size(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .first(itemPage.isFirst())
                .last(itemPage.isLast())
                .build();

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @Operation(summary = "Get item by ID", description = "Returns a single item with all translations and metadata")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item details"),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<AdminItemDto>> getItemById(
            @Parameter(description = "Item ID", required = true) @PathVariable Long id) {
        AdminItemDto item = itemAdminService.getItemById(id);
        return ResponseEntity.ok(BaseResponse.success(item));
    }

    @Operation(summary = "Create a new item",
            description = "Creates an item in draft status. Validates hierarchy: topic must belong to category, " +
                    "subtopic (if provided) must belong to topic.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item created"),
            @ApiResponse(responseCode = "400", description = "Validation error (duplicate slug, hierarchy mismatch)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category/Topic/Subtopic not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminItemDto>> createItem(
            @Valid @RequestBody CreateItemRequest request) {
        AdminItemDto created = itemAdminService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(created, "Item created successfully"));
    }

    @Operation(summary = "Update an item", description = "Partially updates item fields, attributes, and/or translations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminItemDto>> updateItem(
            @Parameter(description = "Item ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        AdminItemDto updated = itemAdminService.updateItem(id, request);
        return ResponseEntity.ok(BaseResponse.success(updated, "Item updated successfully"));
    }

    @Operation(summary = "Delete (soft) an item", description = "Archives the item (soft-delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item deleted"),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteItem(
            @Parameter(description = "Item ID", required = true) @PathVariable Long id) {
        itemAdminService.deleteItem(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Item deleted successfully"));
    }

    @Operation(summary = "Publish an item",
            description = "Changes status to published. Requires: default-language translation with title and description.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item published"),
            @ApiResponse(responseCode = "400", description = "Publish conditions not met",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminItemDto>> publishItem(
            @Parameter(description = "Item ID", required = true) @PathVariable Long id) {
        AdminItemDto published = itemAdminService.publishItem(id);
        return ResponseEntity.ok(BaseResponse.success(published, "Item published successfully"));
    }

    @Operation(summary = "Unpublish an item", description = "Reverts status from published to draft")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item unpublished"),
            @ApiResponse(responseCode = "400", description = "Item is not published",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminItemDto>> unpublishItem(
            @Parameter(description = "Item ID", required = true) @PathVariable Long id) {
        AdminItemDto unpublished = itemAdminService.unpublishItem(id);
        return ResponseEntity.ok(BaseResponse.success(unpublished, "Item unpublished successfully"));
    }

    @Operation(summary = "Archive an item", description = "Changes status to archived")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item archived"),
            @ApiResponse(responseCode = "400", description = "Item is already archived",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminItemDto>> archiveItem(
            @Parameter(description = "Item ID", required = true) @PathVariable Long id) {
        AdminItemDto archived = itemAdminService.archiveItem(id);
        return ResponseEntity.ok(BaseResponse.success(archived, "Item archived successfully"));
    }
}

