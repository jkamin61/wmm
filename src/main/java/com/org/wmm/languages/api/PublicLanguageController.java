package com.org.wmm.languages.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.languages.dto.LanguageDto;
import com.org.wmm.languages.service.LanguageQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/languages")
@RequiredArgsConstructor
@Tag(name = "Public — Languages", description = "Available languages for multi-language content")
public class PublicLanguageController {

    private final LanguageQueryService languageQueryService;

    @Operation(
            summary = "Get active languages",
            description = "Returns all active languages ordered by display_order. " +
                    "Use the `code` value as the `lang` query parameter on other public endpoints."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of active languages"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @SecurityRequirement(name = "")
    @GetMapping
    public ResponseEntity<BaseResponse<List<LanguageDto>>> getActiveLanguages() {
        List<LanguageDto> languages = languageQueryService.getActiveLanguages();
        return ResponseEntity.ok(BaseResponse.success(languages));
    }
}
