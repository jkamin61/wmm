package com.org.wmm.audit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Audit log entry")
public class AuditLogDto {

    @Schema(description = "Audit log ID", example = "1")
    private Long id;

    @Schema(description = "User ID who performed the action (null if unauthenticated)", example = "1")
    private Long userId;

    @Schema(description = "User email (resolved)", example = "admin@wmm.com")
    private String userEmail;

    @Schema(description = "Entity type", example = "item")
    private String entityType;

    @Schema(description = "Entity ID", example = "42")
    private Long entityId;

    @Schema(description = "Action performed", example = "create")
    private String action;

    @Schema(description = "Old values (JSON)", example = "{\"status\":\"draft\"}")
    private String oldValues;

    @Schema(description = "New values (JSON)", example = "{\"status\":\"published\"}")
    private String newValues;

    @Schema(description = "Client IP address", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "Timestamp of the action")
    private OffsetDateTime createdAt;
}

