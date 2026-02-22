package com.org.wmm.health.controller;

import com.org.wmm.common.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Application health check")
public class HealthController {

    @Operation(
            summary = "Health check",
            description = "Returns application status, name and version. No authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application is healthy")
    })
    @SecurityRequirement(name = "")
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, Object>>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", OffsetDateTime.now());
        health.put("application", "WilliamMacMiron");
        health.put("version", "0.1.0");

        return ResponseEntity.ok(BaseResponse.success(health));
    }
}
