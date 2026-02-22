package com.org.wmm.users.controller;

import com.org.wmm.auth.dto.UserInfo;
import com.org.wmm.common.dto.BaseResponse;
import com.org.wmm.users.entity.UserEntity;
import com.org.wmm.users.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Current user profile")
public class UserController {

    private final CustomUserDetailsService userDetailsService;

    /**
     * GET /users/me - Get current authenticated user info
     */
    @Operation(
            summary = "Get current user",
            description = "Returns profile of the currently authenticated user based on the JWT bearer token. " +
                    "Includes user ID, email, display name and assigned roles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated user profile"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserInfo>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        log.debug("Getting current user info for: {}", email);

        UserEntity user = userDetailsService.getUserByEmail(email);

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .roles(user.getUserRoles().stream()
                        .map(ur -> ur.getRole().getName())
                        .collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(BaseResponse.success(userInfo));
    }
}
