package com.org.wmm.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error details returned on failed requests")
public class ApiError {

    @Schema(description = "Error message", example = "Resource not found with slug: 'invalid'")
    private String message;

    @Schema(description = "Machine-readable error code", example = "RESOURCE_NOT_FOUND")
    private String code;

    @Schema(description = "HTTP status code", example = "404")
    private Integer status;

    @Schema(description = "Additional error details")
    private List<String> details;

    @Schema(description = "Field-level validation errors (field name → message)")
    private Map<String, String> fieldErrors;
}
