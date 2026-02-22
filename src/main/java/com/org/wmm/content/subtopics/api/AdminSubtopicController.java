package com.org.wmm.content.subtopics.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.content.subtopics.dto.AdminSubtopicDto;
import com.org.wmm.content.subtopics.dto.CreateSubtopicRequest;
import com.org.wmm.content.subtopics.dto.UpdateSubtopicRequest;
import com.org.wmm.content.subtopics.service.SubtopicAdminService;
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
@RequestMapping("/admin/subtopics")
@RequiredArgsConstructor
@Tag(name = "Admin — Subtopics", description = "CRUD operations for subtopics (requires ADMIN or EDITOR role)")
@SecurityRequirement(name = "bearerAuth")
public class AdminSubtopicController {

    private final SubtopicAdminService subtopicAdminService;

    @Operation(summary = "List subtopics", description = "Returns all subtopics, optionally filtered by topic ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of subtopics"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<List<AdminSubtopicDto>>> getAllSubtopics(
            @Parameter(description = "Filter by topic ID") @RequestParam(required = false) Long topicId) {
        List<AdminSubtopicDto> subtopics = subtopicAdminService.getAllSubtopics(topicId);
        return ResponseEntity.ok(BaseResponse.success(subtopics));
    }

    @Operation(summary = "Get subtopic by ID", description = "Returns a single subtopic with all translations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subtopic details"),
            @ApiResponse(responseCode = "404", description = "Subtopic not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<AdminSubtopicDto>> getSubtopicById(
            @Parameter(description = "Subtopic ID", required = true) @PathVariable Long id) {
        AdminSubtopicDto subtopic = subtopicAdminService.getSubtopicById(id);
        return ResponseEntity.ok(BaseResponse.success(subtopic));
    }

    @Operation(summary = "Create a new subtopic", description = "Creates a subtopic in draft status. Topic must exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Subtopic created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Topic not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminSubtopicDto>> createSubtopic(
            @Valid @RequestBody CreateSubtopicRequest request) {
        AdminSubtopicDto created = subtopicAdminService.createSubtopic(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(created, "Subtopic created successfully"));
    }

    @Operation(summary = "Update a subtopic", description = "Partially updates subtopic fields and/or translations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subtopic updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Subtopic not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminSubtopicDto>> updateSubtopic(
            @Parameter(description = "Subtopic ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateSubtopicRequest request) {
        AdminSubtopicDto updated = subtopicAdminService.updateSubtopic(id, request);
        return ResponseEntity.ok(BaseResponse.success(updated, "Subtopic updated successfully"));
    }

    @Operation(summary = "Delete (soft) a subtopic", description = "Deactivates and archives the subtopic")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subtopic deleted"),
            @ApiResponse(responseCode = "404", description = "Subtopic not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteSubtopic(
            @Parameter(description = "Subtopic ID", required = true) @PathVariable Long id) {
        subtopicAdminService.deleteSubtopic(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Subtopic deleted successfully"));
    }

    @Operation(summary = "Publish a subtopic", description = "Changes status to published")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminSubtopicDto>> publishSubtopic(
            @Parameter(description = "Subtopic ID", required = true) @PathVariable Long id) {
        AdminSubtopicDto published = subtopicAdminService.publishSubtopic(id);
        return ResponseEntity.ok(BaseResponse.success(published, "Subtopic published successfully"));
    }

    @Operation(summary = "Unpublish a subtopic", description = "Reverts status from published to draft")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminSubtopicDto>> unpublishSubtopic(
            @Parameter(description = "Subtopic ID", required = true) @PathVariable Long id) {
        AdminSubtopicDto unpublished = subtopicAdminService.unpublishSubtopic(id);
        return ResponseEntity.ok(BaseResponse.success(unpublished, "Subtopic unpublished successfully"));
    }

    @Operation(summary = "Archive a subtopic", description = "Changes status to archived")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminSubtopicDto>> archiveSubtopic(
            @Parameter(description = "Subtopic ID", required = true) @PathVariable Long id) {
        AdminSubtopicDto archived = subtopicAdminService.archiveSubtopic(id);
        return ResponseEntity.ok(BaseResponse.success(archived, "Subtopic archived successfully"));
    }
}

