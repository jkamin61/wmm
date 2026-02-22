package com.org.wmm.media.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.media.dto.*;
import com.org.wmm.media.service.ImageAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin — Images", description = "Upload, manage, and translate images for items")
@SecurityRequirement(name = "bearerAuth")
public class AdminImageController {

    private final ImageAdminService imageAdminService;

    // ─── LIST ──────────────────────────────────────────────────────

    @Operation(summary = "List images for an item",
            description = "Returns all images for a given item, ordered by display_order")
    @ApiResponse(responseCode = "200", description = "List of images")
    @ApiResponse(responseCode = "404", description = "Item not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/items/{itemId}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<List<AdminImageDto>>> getImagesByItem(
            @Parameter(description = "Item ID") @PathVariable Long itemId) {
        List<AdminImageDto> images = imageAdminService.getImagesByItemId(itemId);
        return ResponseEntity.ok(BaseResponse.success(images));
    }

    // ─── UPLOAD ────────────────────────────────────────────────────

    @Operation(summary = "Upload images for an item",
            description = "Uploads one or more images (multipart). First image becomes primary if no primary exists. " +
                    "Allowed types: JPEG, PNG, WebP, GIF, AVIF. Max 10 MB per file.")
    @ApiResponse(responseCode = "201", description = "Images uploaded")
    @ApiResponse(responseCode = "400", description = "Invalid file (empty, too large, wrong type)",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @ApiResponse(responseCode = "404", description = "Item not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PostMapping(value = "/items/{itemId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<List<AdminImageDto>>> uploadImages(
            @Parameter(description = "Item ID") @PathVariable Long itemId,
            @Parameter(description = "Image files to upload") @RequestParam("files") MultipartFile[] files) {
        List<AdminImageDto> uploaded = imageAdminService.uploadImages(itemId, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(uploaded, "Uploaded " + uploaded.size() + " image(s)"));
    }

    // ─── UPDATE METADATA ───────────────────────────────────────────

    @Operation(summary = "Update image metadata",
            description = "Update display_order and/or is_primary. Setting is_primary=true unsets primary from other images.")
    @ApiResponse(responseCode = "200", description = "Image updated")
    @ApiResponse(responseCode = "404", description = "Image not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PatchMapping("/images/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminImageDto>> updateImage(
            @Parameter(description = "Image ID") @PathVariable Long imageId,
            @Valid @RequestBody UpdateImageRequest request) {
        AdminImageDto updated = imageAdminService.updateImage(imageId, request);
        return ResponseEntity.ok(BaseResponse.success(updated, "Image updated successfully"));
    }

    // ─── REORDER ───────────────────────────────────────────────────

    @Operation(summary = "Reorder images for an item",
            description = "Sets display_order based on the position in the provided list. " +
                    "All image IDs of the item must be included.")
    @ApiResponse(responseCode = "200", description = "Images reordered")
    @ApiResponse(responseCode = "400", description = "Invalid image IDs (missing, wrong item)",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @ApiResponse(responseCode = "404", description = "Item not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PutMapping("/items/{itemId}/images/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<List<AdminImageDto>>> reorderImages(
            @Parameter(description = "Item ID") @PathVariable Long itemId,
            @Valid @RequestBody ReorderImagesRequest request) {
        List<AdminImageDto> reordered = imageAdminService.reorderImages(itemId, request);
        return ResponseEntity.ok(BaseResponse.success(reordered, "Images reordered successfully"));
    }

    // ─── TRANSLATIONS ──────────────────────────────────────────────

    @Operation(summary = "Update image translations (alt/caption)",
            description = "Upserts alt text and caption per language for an image")
    @ApiResponse(responseCode = "200", description = "Translations updated")
    @ApiResponse(responseCode = "404", description = "Image not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PutMapping("/images/{imageId}/translations")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminImageDto>> updateImageTranslations(
            @Parameter(description = "Image ID") @PathVariable Long imageId,
            @Valid @RequestBody UpdateImageTranslationsRequest request) {
        AdminImageDto updated = imageAdminService.updateImageTranslations(imageId, request);
        return ResponseEntity.ok(BaseResponse.success(updated, "Image translations updated"));
    }

    // ─── DELETE ────────────────────────────────────────────────────

    @Operation(summary = "Delete an image",
            description = "Removes the image from storage and database. If it was primary, the next image is promoted.")
    @ApiResponse(responseCode = "200", description = "Image deleted")
    @ApiResponse(responseCode = "404", description = "Image not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteImage(
            @Parameter(description = "Image ID") @PathVariable Long imageId) {
        imageAdminService.deleteImage(imageId);
        return ResponseEntity.ok(BaseResponse.success(null, "Image deleted successfully"));
    }
}

