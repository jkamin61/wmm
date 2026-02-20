package com.org.wmm.users.service;

import com.org.wmm.common.error.ResourceNotFoundException;
import com.org.wmm.users.entity.RoleEntity;
import com.org.wmm.users.entity.UserEntity;
import com.org.wmm.users.entity.UserRoleEntity;
import com.org.wmm.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    private UserEntity activeUser;
    private UserEntity inactiveUser;
    private UserEntity lockedUser;

    @BeforeEach
    void setUp() {
        RoleEntity adminRole = RoleEntity.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .build();

        RoleEntity editorRole = RoleEntity.builder()
                .id(2L)
                .name("ROLE_EDITOR")
                .build();

        UserRoleEntity adminUserRole = UserRoleEntity.builder()
                .userId(1L)
                .roleId(1L)
                .role(adminRole)
                .build();

        UserRoleEntity editorUserRole = UserRoleEntity.builder()
                .userId(1L)
                .roleId(2L)
                .role(editorRole)
                .build();

        activeUser = UserEntity.builder()
                .id(1L)
                .email("admin@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .passwordAlgo("bcrypt")
                .displayName("Admin")
                .isActive(true)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .userRoles(Set.of(adminUserRole, editorUserRole))
                .build();

        inactiveUser = UserEntity.builder()
                .id(2L)
                .email("inactive@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .passwordAlgo("bcrypt")
                .displayName("Inactive")
                .isActive(false)
                .isEmailVerified(false)
                .failedLoginAttempts(0)
                .userRoles(Set.of())
                .build();

        lockedUser = UserEntity.builder()
                .id(3L)
                .email("locked@test.com")
                .passwordHash("$2a$10$hashedpassword")
                .passwordAlgo("bcrypt")
                .displayName("Locked")
                .isActive(true)
                .isEmailVerified(true)
                .failedLoginAttempts(5)
                .lockedUntil(OffsetDateTime.now().plusHours(1))
                .userRoles(Set.of(adminUserRole))
                .build();
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("should return UserDetails for active user with correct roles")
        void shouldReturnUserDetailsForActiveUser() {
            when(userRepository.findByEmailWithRoles("admin@test.com"))
                    .thenReturn(Optional.of(activeUser));

            UserDetails result = service.loadUserByUsername("admin@test.com");

            assertThat(result.getUsername()).isEqualTo("admin@test.com");
            assertThat(result.getPassword()).isEqualTo("$2a$10$hashedpassword");
            assertThat(result.isEnabled()).isTrue();
            assertThat(result.isAccountNonLocked()).isTrue();
            assertThat(result.getAuthorities())
                    .extracting("authority")
                    .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_EDITOR");

            verify(userRepository).findByEmailWithRoles("admin@test.com");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByEmailWithRoles("unknown@test.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.loadUserByUsername("unknown@test.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("unknown@test.com");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user is inactive")
        void shouldThrowWhenUserInactive() {
            when(userRepository.findByEmailWithRoles("inactive@test.com"))
                    .thenReturn(Optional.of(inactiveUser));

            assertThatThrownBy(() -> service.loadUserByUsername("inactive@test.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("inactive");
        }

        @Test
        @DisplayName("should return locked UserDetails when lockedUntil is in the future")
        void shouldReturnLockedUserDetails() {
            when(userRepository.findByEmailWithRoles("locked@test.com"))
                    .thenReturn(Optional.of(lockedUser));

            UserDetails result = service.loadUserByUsername("locked@test.com");

            assertThat(result.isAccountNonLocked()).isFalse();
            assertThat(result.getUsername()).isEqualTo("locked@test.com");
        }

        @Test
        @DisplayName("should return unlocked UserDetails when lockedUntil is null")
        void shouldReturnUnlockedWhenLockedUntilNull() {
            when(userRepository.findByEmailWithRoles("admin@test.com"))
                    .thenReturn(Optional.of(activeUser));

            UserDetails result = service.loadUserByUsername("admin@test.com");

            assertThat(result.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("should return unlocked UserDetails when lockedUntil is in the past")
        void shouldReturnUnlockedWhenLockExpired() {
            activeUser.setLockedUntil(OffsetDateTime.now().minusHours(1));

            when(userRepository.findByEmailWithRoles("admin@test.com"))
                    .thenReturn(Optional.of(activeUser));

            UserDetails result = service.loadUserByUsername("admin@test.com");

            assertThat(result.isAccountNonLocked()).isTrue();
        }
    }

    @Nested
    @DisplayName("getUserByEmail")
    class GetUserByEmail {

        @Test
        @DisplayName("should return UserEntity when found")
        void shouldReturnUserEntity() {
            when(userRepository.findByEmailWithRoles("admin@test.com"))
                    .thenReturn(Optional.of(activeUser));

            UserEntity result = service.getUserByEmail("admin@test.com");

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("admin@test.com");
            assertThat(result.getDisplayName()).isEqualTo("Admin");
            verify(userRepository).findByEmailWithRoles("admin@test.com");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowResourceNotFound() {
            when(userRepository.findByEmailWithRoles("missing@test.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getUserByEmail("missing@test.com"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining("missing@test.com");
        }
    }
}

