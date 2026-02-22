package com.org.wmm.content.categories.api;

import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.content.categories.dto.CategoryMenuDto;
import com.org.wmm.content.categories.service.CategoryQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Tag(name = "Public — Menu", description = "Site navigation menu (categories with topics)")
public class PublicCategoryController {

    private final CategoryQueryService categoryQueryService;

    @Operation(
            summary = "Get menu",
            description = "Returns active published categories with their published topics, " +
                    "translated into the requested language (falls back to system default). " +
                    "Categories and topics are sorted by display_order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu with categories and topics (may be an empty list)"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @SecurityRequirement(name = "")
    @GetMapping("/menu")
    public ResponseEntity<BaseResponse<List<CategoryMenuDto>>> getMenu(
            @Parameter(description = "Language code (e.g. `pl`, `en`). Falls back to default if missing or invalid.")
            @RequestParam(required = false) String lang
    ) {
        List<CategoryMenuDto> menu = categoryQueryService.getMenu(lang);
        return ResponseEntity.ok(BaseResponse.success(menu));
    }
}
