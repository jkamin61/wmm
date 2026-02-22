package com.org.wmm.content.topics.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.content.topics.dto.AdminTopicDto;
import com.org.wmm.content.topics.dto.CreateTopicRequest;
import com.org.wmm.content.topics.dto.UpdateTopicRequest;
import com.org.wmm.content.topics.service.TopicAdminService;
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
@RequestMapping("/admin/topics")
@RequiredArgsConstructor
@Tag(name = "Admin — Topics", description = "CRUD operations for topics (requires ADMIN or EDITOR role)")
@SecurityRequirement(name = "bearerAuth")
public class AdminTopicController {

    private final TopicAdminService topicAdminService;

    @Operation(summary = "List topics", description = "Returns all topics, optionally filtered by category ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of topics"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<List<AdminTopicDto>>> getAllTopics(
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId) {
        List<AdminTopicDto> topics = topicAdminService.getAllTopics(categoryId);
        return ResponseEntity.ok(BaseResponse.success(topics));
    }

    @Operation(summary = "Get topic by ID", description = "Returns a single topic with all translations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic details"),
            @ApiResponse(responseCode = "404", description = "Topic not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'VIEWER')")
    public ResponseEntity<BaseResponse<AdminTopicDto>> getTopicById(
            @Parameter(description = "Topic ID", required = true) @PathVariable Long id) {
        AdminTopicDto topic = topicAdminService.getTopicById(id);
        return ResponseEntity.ok(BaseResponse.success(topic));
    }

    @Operation(summary = "Create a new topic", description = "Creates a topic in draft status. Category must exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Topic created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminTopicDto>> createTopic(
            @Valid @RequestBody CreateTopicRequest request) {
        AdminTopicDto created = topicAdminService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(created, "Topic created successfully"));
    }

    @Operation(summary = "Update a topic", description = "Partially updates topic fields and/or translations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Topic not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminTopicDto>> updateTopic(
            @Parameter(description = "Topic ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateTopicRequest request) {
        AdminTopicDto updated = topicAdminService.updateTopic(id, request);
        return ResponseEntity.ok(BaseResponse.success(updated, "Topic updated successfully"));
    }

    @Operation(summary = "Delete (soft) a topic", description = "Deactivates and archives the topic")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic deleted"),
            @ApiResponse(responseCode = "404", description = "Topic not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteTopic(
            @Parameter(description = "Topic ID", required = true) @PathVariable Long id) {
        topicAdminService.deleteTopic(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Topic deleted successfully"));
    }

    @Operation(summary = "Publish a topic", description = "Changes status to published. Requires default-language translation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topic published"),
            @ApiResponse(responseCode = "400", description = "Publish conditions not met",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Topic not found",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminTopicDto>> publishTopic(
            @Parameter(description = "Topic ID", required = true) @PathVariable Long id) {
        AdminTopicDto published = topicAdminService.publishTopic(id);
        return ResponseEntity.ok(BaseResponse.success(published, "Topic published successfully"));
    }

    @Operation(summary = "Unpublish a topic", description = "Reverts status from published to draft")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminTopicDto>> unpublishTopic(
            @Parameter(description = "Topic ID", required = true) @PathVariable Long id) {
        AdminTopicDto unpublished = topicAdminService.unpublishTopic(id);
        return ResponseEntity.ok(BaseResponse.success(unpublished, "Topic unpublished successfully"));
    }

    @Operation(summary = "Archive a topic", description = "Changes status to archived")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<BaseResponse<AdminTopicDto>> archiveTopic(
            @Parameter(description = "Topic ID", required = true) @PathVariable Long id) {
        AdminTopicDto archived = topicAdminService.archiveTopic(id);
        return ResponseEntity.ok(BaseResponse.success(archived, "Topic archived successfully"));
    }
}

