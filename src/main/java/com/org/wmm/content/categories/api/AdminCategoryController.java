package com.org.wmm.content.categories.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.content.categories.dto.AdminCategoryDto;
import com.org.wmm.content.categories.dto.CreateCategoryRequest;
import com.org.wmm.content.categories.dto.UpdateCategoryRequest;
import com.org.wmm.content.categories.service.CategoryAdminService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin — Categories", description = "CRUD operations for categories (requires ADMIN or EDITOR role)")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoryController {

    private final CategoryAdminService categoryAdminService;

    @Operation(summary = "List all categories", description = "Returns all categories with translations (all statuses)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of categories"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<List<AdminCategoryDto>>> getAllCategories() {
        List<AdminCategoryDto> categories = categoryAdminService.getAllCategories();
        return ResponseEntity.ok(BaseResponse.success(categories));
    }

    @Operation(summary = "Get category by ID", description = "Returns a single category with all translations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category details"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<AdminCategoryDto>> getCategoryById(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        AdminCategoryDto category = categoryAdminService.getCategoryById(id);
        return ResponseEntity.ok(BaseResponse.success(category));
    }

    @Operation(summary = "Create a new category", description = "Creates a category in draft status with translations")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. duplicate slug)",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminCategoryDto>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        AdminCategoryDto created = categoryAdminService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(created, "Category created successfully"));
    }

    @Operation(summary = "Update a category", description = "Partially updates category fields and/or translations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminCategoryDto>> updateCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        AdminCategoryDto updated = categoryAdminService.updateCategory(id, request);
        return ResponseEntity.ok(BaseResponse.success(updated, "Category updated successfully"));
    }

    @Operation(summary = "Delete (soft) a category", description = "Deactivates and archives the category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category deleted"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        categoryAdminService.deleteCategory(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Category deleted successfully"));
    }

    @Operation(summary = "Publish a category", description = "Changes status to published. Requires default-language translation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category published"),
            @ApiResponse(responseCode = "400", description = "Publish conditions not met",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminCategoryDto>> publishCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        AdminCategoryDto published = categoryAdminService.publishCategory(id);
        return ResponseEntity.ok(BaseResponse.success(published, "Category published successfully"));
    }

    @Operation(summary = "Unpublish a category", description = "Reverts status from published to draft")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category unpublished"),
            @ApiResponse(responseCode = "400", description = "Category is not published",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminCategoryDto>> unpublishCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        AdminCategoryDto unpublished = categoryAdminService.unpublishCategory(id);
        return ResponseEntity.ok(BaseResponse.success(unpublished, "Category unpublished successfully"));
    }

    @Operation(summary = "Archive a category", description = "Changes status to archived")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category archived"),
            @ApiResponse(responseCode = "400", description = "Category is already archived",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminCategoryDto>> archiveCategory(
            @Parameter(description = "Category ID", required = true) @PathVariable Long id) {
        AdminCategoryDto archived = categoryAdminService.archiveCategory(id);
        return ResponseEntity.ok(BaseResponse.success(archived, "Category archived successfully"));
    }
}

