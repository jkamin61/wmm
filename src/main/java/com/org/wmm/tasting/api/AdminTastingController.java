package com.org.wmm.tasting.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.tasting.dto.*;
import com.org.wmm.tasting.service.TastingNoteAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/items/{itemId}")
@RequiredArgsConstructor
@Tag(name = "Admin — Tasting Notes", description = "Manage tasting notes, translations, and flavor profiles for items")
@SecurityRequirement(name = "bearerAuth")
public class AdminTastingController {

    private final TastingNoteAdminService tastingNoteAdminService;

    // ─── GET ───────────────────────────────────────────────────────

    @Operation(summary = "Get tasting note for an item",
            description = "Returns the tasting note with scores, translations, and flavor profile")
    @ApiResponse(responseCode = "200", description = "Tasting note details")
    @ApiResponse(responseCode = "404", description = "Item or tasting note not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @GetMapping("/tasting-note")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<TastingNoteDto>> getTastingNote(
            @Parameter(description = "Item ID") @PathVariable Long itemId) {
        TastingNoteDto dto = tastingNoteAdminService.getTastingNote(itemId);
        return ResponseEntity.ok(BaseResponse.success(dto));
    }

    // ─── UPSERT TASTING NOTE ──────────────────────────────────────

    @Operation(summary = "Create or update tasting note",
            description = "Upserts the tasting note for an item (scores, intensity, tasting date). " +
                    "If a tasting note already exists for this item, it is updated; otherwise a new one is created.")
    @ApiResponse(responseCode = "200", description = "Tasting note upserted")
    @ApiResponse(responseCode = "404", description = "Item not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PutMapping("/tasting-note")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<TastingNoteDto>> upsertTastingNote(
            @Parameter(description = "Item ID") @PathVariable Long itemId,
            @Valid @RequestBody UpsertTastingNoteRequest request) {
        TastingNoteDto dto = tastingNoteAdminService.upsertTastingNote(itemId, request);
        return ResponseEntity.ok(BaseResponse.success(dto, "Tasting note saved successfully"));
    }

    // ─── TRANSLATIONS ──────────────────────────────────────────────

    @Operation(summary = "Update tasting note translations",
            description = "Upserts aroma/taste/finish notes and overall impression per language. " +
                    "Tasting note must already exist (use PUT /tasting-note first).")
    @ApiResponse(responseCode = "200", description = "Translations updated")
    @ApiResponse(responseCode = "404", description = "Item or tasting note not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PutMapping("/tasting-note/translations")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<TastingNoteDto>> updateTranslations(
            @Parameter(description = "Item ID") @PathVariable Long itemId,
            @Valid @RequestBody UpdateTastingNoteTranslationsRequest request) {
        TastingNoteDto dto = tastingNoteAdminService.updateTranslations(itemId, request);
        return ResponseEntity.ok(BaseResponse.success(dto, "Tasting note translations updated"));
    }

    // ─── FLAVOR PROFILE ────────────────────────────────────────────

    @Operation(summary = "Set flavor profile",
            description = "Replaces the full flavor profile (aroma, taste, finish) for a tasting note. " +
                    "Each section is optional — only provided sections are replaced. " +
                    "Pass empty array to clear a section. Max 15 flavors per section. " +
                    "Tasting note must already exist.")
    @ApiResponse(responseCode = "200", description = "Flavor profile updated")
    @ApiResponse(responseCode = "400", description = "Validation error (duplicate flavor, too many, invalid intensity)",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @ApiResponse(responseCode = "404", description = "Item, tasting note, or flavor not found",
            content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    @PutMapping("/flavor-profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<TastingNoteDto>> updateFlavorProfile(
            @Parameter(description = "Item ID") @PathVariable Long itemId,
            @Valid @RequestBody UpdateFlavorProfileRequest request) {
        TastingNoteDto dto = tastingNoteAdminService.updateFlavorProfile(itemId, request);
        return ResponseEntity.ok(BaseResponse.success(dto, "Flavor profile updated"));
    }
}

