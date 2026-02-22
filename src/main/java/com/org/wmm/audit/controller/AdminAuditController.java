package com.org.wmm.audit.controller;

import com.org.wmm.audit.dto.AuditLogDto;
import com.org.wmm.audit.service.AuditQueryService;
import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@Tag(name = "Admin — Audit Log", description = "Browse and filter the audit trail of all admin actions")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditController {

    private final AuditQueryService auditQueryService;

    @Operation(
            summary = "List audit log entries",
            description = """
                    Returns a paginated, filtered list of audit log entries.
                    All filters are optional — combine them to narrow results.
                    
                    **Entity types:** `category`, `topic`, `subtopic`, `item`, `image`, `tasting_note`
                    
                    **Actions:** `create`, `update`, `delete`, `publish`, `unpublish`, `archive`
                    
                    **Date range:** ISO 8601 with timezone (e.g. `2026-02-01T00:00:00+01:00`)
                    """
    )
    @ApiResponse(responseCode = "200", description = "Paginated audit log entries")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<PageResponse<AuditLogDto>>> getAuditLogs(
            @Parameter(description = "Filter by entity type (e.g. `item`, `category`, `image`)", example = "item")
            @RequestParam(required = false) String entityType,

            @Parameter(description = "Filter by entity ID", example = "42")
            @RequestParam(required = false) Long entityId,

            @Parameter(description = "Filter by action (e.g. `create`, `update`, `publish`, `delete`)", example = "create")
            @RequestParam(required = false) String action,

            @Parameter(description = "Filter by user ID", example = "1")
            @RequestParam(required = false) Long userId,

            @Parameter(description = "Start date (inclusive, ISO 8601)", example = "2026-02-01T00:00:00+01:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,

            @Parameter(description = "End date (inclusive, ISO 8601)", example = "2026-02-28T23:59:59+01:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (max 100)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AuditLogDto> result = auditQueryService.getAuditLogs(
                entityType, entityId, action, userId, from, to, page, size);
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}

