package com.org.wmm.common.dto;

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
@Schema(description = "Standard API response wrapper")
public class BaseResponse<T> {

    @Schema(description = "Whether the request was successful", example = "true")
    private boolean success;

    @Schema(description = "Optional human-readable message", example = "Login successful")
    private String message;

    @Schema(description = "Response payload (type varies per endpoint)")
    private T data;

    @Schema(description = "Error details (present only on failure)")
    private ApiError error;

    @Builder.Default
    @Schema(description = "Response timestamp (ISO 8601)", example = "2026-02-22T12:00:00+01:00")
    private OffsetDateTime timestamp = OffsetDateTime.now();

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> error(String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> BaseResponse<T> error(ApiError error) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(error.getMessage())
                .error(error)
                .build();
    }
}
