package com.org.wmm.auth.service;

import com.org.wmm.auth.domain.RefreshTokenEntity;
import com.org.wmm.auth.dto.AuthResponse;
import com.org.wmm.auth.dto.LoginRequest;
import com.org.wmm.auth.dto.RegisterRequest;
import com.org.wmm.auth.repository.RefreshTokenRepository;
import com.org.wmm.common.error.BadRequestException;
import com.org.wmm.common.error.UnauthorizedException;
import com.org.wmm.security.JwtTokenProvider;
import com.org.wmm.users.entity.RoleEntity;
import com.org.wmm.users.entity.UserEntity;
import com.org.wmm.users.entity.UserRoleEntity;
import com.org.wmm.users.repository.RoleRepository;
import com.org.wmm.users.repository.UserRepository;
import com.org.wmm.users.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthService service;

    private UserEntity adminUser;
    private UserDetails adminUserDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "refreshTokenExpiration", 604800000L);
        ReflectionTestUtils.setField(service, "accessTokenExpiration", 900000L);

        RoleEntity adminRole = RoleEntity.builder().id(1L).name("ROLE_ADMIN").build();
        UserRoleEntity adminUserRole = UserRoleEntity.builder()
                .userId(1L).roleId(1L).role(adminRole).build();

        adminUser = UserEntity.builder()
                .id(1L)
                .email("admin@test.com")
                .passwordHash("$2a$10$hashed")
                .passwordAlgo("bcrypt")
                .displayName("Admin")
                .isActive(true)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .userRoles(Set.of(adminUserRole))
                .build();

        adminUserDetails = User.builder()
                .username("admin@test.com")
                .password("$2a$10$hashed")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should return tokens and user info on successful login")
        void shouldReturnTokensOnSuccess() {
            LoginRequest request = new LoginRequest("admin@test.com", "password123");

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(adminUserDetails);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            when(userDetailsService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
            when(jwtTokenProvider.generateAccessToken(adminUserDetails)).thenReturn("access.token.jwt");
            when(jwtTokenProvider.generateRefreshToken(adminUserDetails)).thenReturn("refresh.token.jwt");
            when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            AuthResponse result = service.login(request, httpServletRequest);

            assertThat(result.getAccessToken()).isEqualTo("access.token.jwt");
            assertThat(result.getRefreshToken()).isEqualTo("refresh.token.jwt");
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getExpiresIn()).isEqualTo(900L);
            assertThat(result.getUser().getEmail()).isEqualTo("admin@test.com");
            assertThat(result.getUser().getDisplayName()).isEqualTo("Admin");
            assertThat(result.getUser().getRoles()).contains("ROLE_ADMIN");

            verify(userRepository).save(adminUser);
            verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
        }

        @Test
        @DisplayName("should update last login and reset failed attempts")
        void shouldUpdateLastLogin() {
            LoginRequest request = new LoginRequest("admin@test.com", "password123");
            adminUser.setFailedLoginAttempts(3);

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(adminUserDetails);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(userDetailsService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
            when(jwtTokenProvider.generateAccessToken(any())).thenReturn("token");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh");
            when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            service.login(request, httpServletRequest);

            assertThat(adminUser.getFailedLoginAttempts()).isZero();
            assertThat(adminUser.getLastLoginAt()).isNotNull();
            verify(userRepository).save(adminUser);
        }

        @Test
        @DisplayName("should throw when credentials are invalid")
        void shouldThrowOnBadCredentials() {
            LoginRequest request = new LoginRequest("admin@test.com", "wrong");

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> service.login(request, httpServletRequest))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("should extract client IP from X-Forwarded-For header")
        void shouldExtractIpFromForwardedHeader() {
            LoginRequest request = new LoginRequest("admin@test.com", "password123");

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(adminUserDetails);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(userDetailsService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
            when(jwtTokenProvider.generateAccessToken(any())).thenReturn("token");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh");
            when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.50, 70.41.3.18");

            service.login(request, httpServletRequest);

            verify(refreshTokenRepository).save(argThat(entity ->
                    "203.0.113.50".equals(entity.getIpAddress())
            ));
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        private RoleEntity viewerRole;

        @BeforeEach
        void setUpRole() {
            viewerRole = RoleEntity.builder().id(3L).name("ROLE_VIEWER").build();
        }

        @Test
        @DisplayName("should register user with ROLE_VIEWER and return tokens")
        void shouldRegisterAndReturnTokens() {
            RegisterRequest request = new RegisterRequest("New User", "new@test.com", "password123");

            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
            when(roleRepository.findByName("ROLE_VIEWER")).thenReturn(Optional.of(viewerRole));

            // After save, login is called internally
            Authentication authentication = mock(Authentication.class);
            UserRoleEntity assignedRole = UserRoleEntity.builder()
                    .userId(2L).roleId(3L).role(viewerRole).build();
            when(authentication.getPrincipal()).thenReturn(
                    User.builder().username("new@test.com").password("$2a$10$encoded")
                            .authorities(List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))).build()
            );
            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            UserEntity savedUser = UserEntity.builder()
                    .id(2L).email("new@test.com").displayName("New User")
                    .passwordHash("$2a$10$encoded").passwordAlgo("bcrypt")
                    .isActive(true).failedLoginAttempts(0)
                    .userRoles(Set.of(assignedRole))
                    .build();
            when(userDetailsService.getUserByEmail("new@test.com")).thenReturn(savedUser);
            when(jwtTokenProvider.generateAccessToken(any())).thenReturn("access.token");
            when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh.token");
            when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

            AuthResponse result = service.register(request, httpServletRequest);

            assertThat(result.getAccessToken()).isEqualTo("access.token");
            assertThat(result.getUser().getEmail()).isEqualTo("new@test.com");
            assertThat(result.getUser().getDisplayName()).isEqualTo("New User");
            assertThat(result.getUser().getRoles()).contains("ROLE_VIEWER");

            // save called 3 times: create user, save with role, update lastLoginAt in login
            verify(userRepository, times(3)).save(any(UserEntity.class));
            verify(roleRepository).findByName("ROLE_VIEWER");
        }

        @Test
        @DisplayName("should throw when email already in use")
        void shouldThrowWhenEmailTaken() {
            RegisterRequest request = new RegisterRequest("User", "existing@test.com", "password123");

            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> service.register(request, httpServletRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already in use");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when ROLE_VIEWER not found in database")
        void shouldThrowWhenDefaultRoleMissing() {
            RegisterRequest request = new RegisterRequest("User", "new@test.com", "password123");

            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
            when(roleRepository.findByName("ROLE_VIEWER")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.register(request, httpServletRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ROLE_VIEWER");
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        @DisplayName("should return new access token with same refresh token")
        void shouldReturnNewAccessToken() {
            String refreshToken = "valid.refresh.token";

            when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(jwtTokenProvider.extractUsername(refreshToken)).thenReturn("admin@test.com");

            RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                    .id(1L).user(adminUser)
                    .expiresAt(OffsetDateTime.now().plusDays(7))
                    .revokedAt(null)
                    .build();
            when(refreshTokenRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.of(tokenEntity));

            when(userDetailsService.loadUserByUsername("admin@test.com")).thenReturn(adminUserDetails);
            when(userDetailsService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
            when(jwtTokenProvider.generateAccessToken(adminUserDetails)).thenReturn("new.access.token");

            AuthResponse result = service.refresh(refreshToken);

            assertThat(result.getAccessToken()).isEqualTo("new.access.token");
            assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
            assertThat(result.getExpiresIn()).isEqualTo(900L);
        }

        @Test
        @DisplayName("should throw when refresh token is invalid JWT")
        void shouldThrowWhenTokenInvalid() {
            when(jwtTokenProvider.validateToken("invalid.token")).thenReturn(false);

            assertThatThrownBy(() -> service.refresh("invalid.token"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        @DisplayName("should throw when refresh token not found in database")
        void shouldThrowWhenTokenNotInDb() {
            String refreshToken = "valid.but.unknown";
            when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(refreshTokenRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.refresh(refreshToken))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should throw when refresh token is expired")
        void shouldThrowWhenTokenExpired() {
            String refreshToken = "expired.token";
            when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);

            RefreshTokenEntity expiredEntity = RefreshTokenEntity.builder()
                    .id(1L).user(adminUser)
                    .expiresAt(OffsetDateTime.now().minusDays(1))
                    .revokedAt(null)
                    .build();
            when(refreshTokenRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.of(expiredEntity));

            assertThatThrownBy(() -> service.refresh(refreshToken))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("expired or revoked");
        }

        @Test
        @DisplayName("should throw when refresh token is revoked")
        void shouldThrowWhenTokenRevoked() {
            String refreshToken = "revoked.token";
            when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);

            RefreshTokenEntity revokedEntity = RefreshTokenEntity.builder()
                    .id(1L).user(adminUser)
                    .expiresAt(OffsetDateTime.now().plusDays(7))
                    .revokedAt(OffsetDateTime.now().minusHours(1))
                    .build();
            when(refreshTokenRepository.findByTokenHash(anyString()))
                    .thenReturn(Optional.of(revokedEntity));

            assertThatThrownBy(() -> service.refresh(refreshToken))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("expired or revoked");
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("should revoke refresh token")
        void shouldRevokeToken() {
            service.logout("some.refresh.token");

            verify(refreshTokenRepository).revokeToken(anyString(), any(OffsetDateTime.class));
        }

        @Test
        @DisplayName("should throw when refresh token is null")
        void shouldThrowWhenTokenNull() {
            assertThatThrownBy(() -> service.logout(null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("required");
        }

        @Test
        @DisplayName("should throw when refresh token is blank")
        void shouldThrowWhenTokenBlank() {
            assertThatThrownBy(() -> service.logout("   "))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("required");
        }
    }

    @Nested
    @DisplayName("logoutAll")
    class LogoutAll {

        @Test
        @DisplayName("should delete all refresh tokens for user")
        void shouldDeleteAllTokens() {
            when(userDetailsService.getUserByEmail("admin@test.com")).thenReturn(adminUser);

            service.logoutAll("admin@test.com");

            verify(refreshTokenRepository).deleteAllByUserId(1L);
        }
    }

    @Nested
    @DisplayName("cleanupExpiredTokens")
    class CleanupExpiredTokens {

        @Test
        @DisplayName("should call repository to delete expired tokens")
        void shouldCallCleanup() {
            service.cleanupExpiredTokens();

            verify(refreshTokenRepository).deleteAllExpiredTokens(any(OffsetDateTime.class));
        }
    }
}


