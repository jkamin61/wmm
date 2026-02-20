package com.org.wmm.auth.service;

import com.org.wmm.auth.domain.RefreshTokenEntity;
import com.org.wmm.auth.dto.AuthResponse;
import com.org.wmm.auth.dto.LoginRequest;
import com.org.wmm.auth.dto.RegisterRequest;
import com.org.wmm.auth.dto.UserInfo;
import com.org.wmm.auth.repository.RefreshTokenRepository;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.UnauthorizedException;
import com.org.wmm.security.JwtTokenProvider;
import com.org.wmm.users.entity.UserEntity;
import com.org.wmm.users.repository.UserRepository;
import com.org.wmm.users.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.stream.Collectors;

import static com.org.wmm.common.constants.SecurityConstants.BCRYPT;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Register user
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest, HttpServletRequest httpServletRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        UserEntity user = UserEntity.builder()
                .displayName(registerRequest.getDisplayName())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .passwordAlgo(BCRYPT)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        LoginRequest loginRequest = new LoginRequest(registerRequest.getEmail(), registerRequest.getPassword());
        return login(loginRequest, httpServletRequest);
    }

    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserEntity user = userDetailsService.getUserByEmail(userDetails.getUsername());

        // Update last login
        user.setLastLoginAt(OffsetDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        // Save refresh token
        saveRefreshToken(user, refreshToken, httpRequest);

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                .user(mapToUserInfo(user))
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        log.debug("Refreshing token");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String tokenHash = hashToken(refreshToken);
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (!tokenEntity.isValid()) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        // Get user and generate new access token
        String email = jwtTokenProvider.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UserEntity user = userDetailsService.getUserByEmail(email);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);

        log.info("Token refreshed for user: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return the same refresh token
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .user(mapToUserInfo(user))
                .build();
    }

    /**
     * Logout user by revoking refresh token
     */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("Refresh token is required");
        }

        String tokenHash = hashToken(refreshToken);
        refreshTokenRepository.revokeToken(tokenHash, OffsetDateTime.now());

        log.info("User logged out, refresh token revoked");
    }

    /**
     * Logout from all devices by revoking all refresh tokens
     */
    @Transactional
    public void logoutAll(String email) {
        UserEntity user = userDetailsService.getUserByEmail(email);
        refreshTokenRepository.deleteAllByUserId(user.getId());

        log.info("User logged out from all devices: {}", email);
    }

    /**
     * Save refresh token to database
     */
    private void saveRefreshToken(UserEntity user, String refreshToken, HttpServletRequest request) {
        String tokenHash = hashToken(refreshToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .build();

        refreshTokenRepository.save(tokenEntity);
    }

    /**
     * Hash token using SHA-256
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Map UserEntity to UserInfo DTO
     */
    private UserInfo mapToUserInfo(UserEntity user) {
        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .roles(user.getUserRoles().stream()
                        .map(ur -> ur.getRole().getName())
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Clean up expired refresh tokens (scheduled task can call this)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(OffsetDateTime.now());
        log.info("Cleaned up expired refresh tokens");
    }
}

