package com.org.wmm.common.constants;

public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ROLE_PREFIX = "ROLE_";

    // Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_EDITOR = "ROLE_EDITOR";
    public static final String ROLE_VIEWER = "ROLE_VIEWER";

    // Public endpoints (no authentication required)
    public static final String[] PUBLIC_ENDPOINTS = {
            "/health",
            "/actuator/health",
            "/auth/**",
            "/public/**"
    };

    // Admin endpoints (authentication + ADMIN/EDITOR role required)
    public static final String[] ADMIN_ENDPOINTS = {
            "/admin/**"
    };
}

