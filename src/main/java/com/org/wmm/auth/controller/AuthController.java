package com.org.wmm.auth.controller;

import com.org.wmm.auth.dto.AuthResponse;
import com.org.wmm.auth.dto.LoginRequest;
import com.org.wmm.auth.dto.RefreshTokenRequest;
import com.org.wmm.auth.dto.RegisterRequest;
import com.org.wmm.auth.service.AuthService;
import com.org.wmm.common.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, token refresh and logout")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register", description = "Register new user with username, email and password. Logs in after successful registration and returns access + refresh tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @SecurityRequirement(name = "")
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest httpServletRequest
    ) {
        log.info("Register request: {}", registerRequest);
        AuthResponse response = authService.register(registerRequest, httpServletRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(response, "Registration successful"));
    }


    @Operation(summary = "Login", description = "Authenticate with email and password. Returns access + refresh tokens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
    })
    @SecurityRequirement(name = "")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(BaseResponse.success(response, "Login successful"));
    }

    @Operation(summary = "Refresh token", description = "Exchange a valid refresh token for a new access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @SecurityRequirement(name = "")
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("Token refresh request");
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(BaseResponse.success(response, "Token refreshed"));
    }

    @Operation(summary = "Logout", description = "Revoke the given refresh token. Access token remains valid until expiry.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out"),
            @ApiResponse(responseCode = "400", description = "Missing refresh token")
    })
    @SecurityRequirement(name = "")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("Logout request");
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(BaseResponse.success(null, "Logged out successfully"));
    }
}
