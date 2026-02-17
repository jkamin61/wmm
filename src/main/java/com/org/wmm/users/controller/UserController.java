package com.org.wmm.users.controller;

import com.org.wmm.auth.dto.UserInfo;
import com.org.wmm.common.dto.ApiResponse;
import com.org.wmm.users.entity.UserEntity;
import com.org.wmm.users.service.CustomUserDetailsService;
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
public class UserController {

    private final CustomUserDetailsService userDetailsService;

    /**
     * GET /users/me - Get current authenticated user info
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser() {
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

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}
